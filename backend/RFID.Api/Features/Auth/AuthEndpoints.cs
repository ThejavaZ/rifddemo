namespace RFID.Api.Features.Auth;

public record AuthRequest(string Username, string Password);

public record AuthResponse(string Token, DateTimeOffset ExpiresAt);

public static class AuthEndpoints
{
    public static IEndpointRouteBuilder MapAuthEndpoints(this IEndpointRouteBuilder app)
    {
        app.MapPost("/api/v1/auth/token", Handle);
        return app;
    }

    private static async Task<IResult> Handle(AuthRequest? request, TokenService tokenService)
    {
        if (request is null || string.IsNullOrWhiteSpace(request.Username))
            return Results.BadRequest(new { error = "username y password son requeridos" });

        if (!await tokenService.ValidateCredentialsAsync(request.Username, request.Password))
            return Results.Unauthorized();

        var (token, expiresAt) = tokenService.CreateToken(request.Username);
        return Results.Ok(new AuthResponse(token, expiresAt));
    }
}
