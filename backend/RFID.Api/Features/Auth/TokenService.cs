using System.IdentityModel.Tokens.Jwt;
using System.Security.Claims;
using System.Security.Cryptography;
using System.Text;
using Dapper;
using Microsoft.IdentityModel.Tokens;
using Npgsql;

namespace RFID.Api.Features.Auth;

public class TokenService
{
    private readonly IConfiguration _configuration;
    private readonly NpgsqlDataSource _dataSource;
    private readonly SymmetricSecurityKey _signingKey;

    public TokenService(IConfiguration configuration, NpgsqlDataSource dataSource)
    {
        _configuration = configuration;
        _dataSource = dataSource;
        // Sin SigningKey configurado se genera una clave efímera: nunca se hardcodea en el repo.
        var configured = configuration["Auth:SigningKey"];
        var keyBytes = string.IsNullOrWhiteSpace(configured)
            ? RandomNumberGenerator.GetBytes(32)
            : Encoding.UTF8.GetBytes(configured);
        _signingKey = new SymmetricSecurityKey(keyBytes);
    }

    // Los usuarios viven en PostgreSQL con hash BCrypt; la config solo siembra el usuario inicial.
    public async Task<bool> ValidateCredentialsAsync(string username, string password)
    {
        const string sql = "SELECT password_hash FROM users WHERE username = @Username";
        await using var connection = await _dataSource.OpenConnectionAsync();
        var passwordHash = await connection.QuerySingleOrDefaultAsync<string>(
            new CommandDefinition(sql, new { Username = username }));

        return passwordHash is not null && BCrypt.Net.BCrypt.Verify(password, passwordHash);
    }

    public (string Token, DateTimeOffset ExpiresAt) CreateToken(string username)
    {
        var expires = DateTimeOffset.UtcNow.AddHours(1);
        var credentials = new SigningCredentials(_signingKey, SecurityAlgorithms.HmacSha256);
        var token = new JwtSecurityToken(
            issuer: _configuration["Auth:Issuer"],
            audience: _configuration["Auth:Audience"],
            claims: [new Claim(ClaimTypes.Name, username)],
            expires: expires.UtcDateTime,
            signingCredentials: credentials);

        return (new JwtSecurityTokenHandler().WriteToken(token), expires);
    }
}
