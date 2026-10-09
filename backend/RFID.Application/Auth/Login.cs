using RFID.Application.Ports;
using RFID.Domain.Ports;

namespace RFID.Application.Auth;

public sealed class Login
{
    private readonly IUserRepository _users;
    private readonly IPasswordHasher _passwordHasher;
    private readonly ITokenService _tokenService;

    public Login(IUserRepository users, IPasswordHasher passwordHasher, ITokenService tokenService)
    {
        _users = users;
        _passwordHasher = passwordHasher;
        _tokenService = tokenService;
    }

    // Devuelve null cuando las credenciales no son válidas (el adaptador HTTP responde 401).
    public async Task<TokenResult?> ExecuteAsync(
        string username,
        string password,
        CancellationToken cancellationToken = default)
    {
        var user = await _users.GetByUsernameAsync(username, cancellationToken);
        if (user is null || string.IsNullOrEmpty(password))
            return null;

        return _passwordHasher.Verify(password, user.PasswordHash)
            ? _tokenService.Generate(user.Username)
            : null;
    }
}
