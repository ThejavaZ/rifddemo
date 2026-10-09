using System.IdentityModel.Tokens.Jwt;
using System.Security.Claims;
using System.Security.Cryptography;
using System.Text;
using Microsoft.IdentityModel.Tokens;

namespace RFID.Api.Features.Auth;

public record AuthUser(string Username, string Password);

public class TokenService
{
    private readonly IConfiguration _configuration;
    private readonly SymmetricSecurityKey _signingKey;

    public TokenService(IConfiguration configuration)
    {
        _configuration = configuration;
        // Sin SigningKey configurado se genera una clave efímera: nunca se hardcodea en el repo.
        var configured = configuration["Auth:SigningKey"];
        var keyBytes = string.IsNullOrWhiteSpace(configured)
            ? RandomNumberGenerator.GetBytes(32)
            : Encoding.UTF8.GetBytes(configured);
        _signingKey = new SymmetricSecurityKey(keyBytes);
    }

    public bool ValidateCredentials(string username, string password)
    {
        var users = _configuration.GetSection("Auth:Users").Get<List<AuthUser>>() ?? [];
        return users.Any(user => user.Username == username && user.Password == password);
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
