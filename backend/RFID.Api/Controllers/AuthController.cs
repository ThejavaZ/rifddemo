using Microsoft.AspNetCore.Mvc;
using RFID.Application.Auth;

namespace RFID.Api.Controllers;

public record LoginRequest(string Username, string Password);

// Adaptador de entrada HTTP: traduce la petición a un caso de uso.
[ApiController]
[Route("api/v1/auth")]
public class AuthController : ControllerBase
{
    private readonly Login _login;

    public AuthController(Login login) => _login = login;

    [HttpPost("token")]
    public async Task<IActionResult> Token([FromBody] LoginRequest? request, CancellationToken cancellationToken)
    {
        if (request is null || string.IsNullOrWhiteSpace(request.Username))
            return BadRequest(new { error = "username y password son requeridos" });

        var result = await _login.ExecuteAsync(request.Username, request.Password, cancellationToken);

        return result is null ? Unauthorized() : Ok(result);
    }
}
