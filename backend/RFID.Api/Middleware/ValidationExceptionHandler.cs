using Microsoft.AspNetCore.Diagnostics;
using RFID.Application;

namespace RFID.Api.Middleware;

// Convierte ValidationException de los casos de uso en 400 { error } (contrato que ya lee la app Android).
internal sealed class ValidationExceptionHandler(ILogger<ValidationExceptionHandler> logger) : IExceptionHandler
{
    public async ValueTask<bool> TryHandleAsync(
        HttpContext httpContext,
        Exception exception,
        CancellationToken cancellationToken)
    {
        if (exception is not ValidationException validation)
            return false;

        logger.LogWarning("Validación rechazada: {Message}", validation.Message);
        httpContext.Response.StatusCode = StatusCodes.Status400BadRequest;
        await httpContext.Response.WriteAsJsonAsync(new { error = validation.Message }, cancellationToken);
        return true;
    }
}
