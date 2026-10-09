using System.Text;
using Microsoft.AspNetCore.Authentication.JwtBearer;
using Microsoft.IdentityModel.Tokens;
using RFID.Api.Middleware;
using RFID.Application.Auth;
using RFID.Application.Inventory;
using RFID.Application.Sync;
using RFID.Infrastructure;
using RFID.Infrastructure.Persistence;

var builder = WebApplication.CreateBuilder(args);

builder.Services.AddControllers();
builder.Services.AddExceptionHandler<ValidationExceptionHandler>();
builder.Services.AddProblemDetails();

// Validación JWT: la misma clave simétrica que usa JwtTokenService para firmar.
// En desarrollo la clave vive en appsettings.Development.json; en producción se inyecta por variable de entorno.
var signingKey = builder.Configuration["Auth:SigningKey"]
    ?? throw new InvalidOperationException("Auth:SigningKey es requerido para validar JWT");

builder.Services
    .AddAuthentication(JwtBearerDefaults.AuthenticationScheme)
    .AddJwtBearer(options =>
    {
        options.TokenValidationParameters = new TokenValidationParameters
        {
            ValidateIssuer = true,
            ValidateAudience = true,
            ValidateLifetime = true,
            ValidateIssuerSigningKey = true,
            ValidIssuer = builder.Configuration["Auth:Issuer"],
            ValidAudience = builder.Configuration["Auth:Audience"],
            IssuerSigningKey = new SymmetricSecurityKey(Encoding.UTF8.GetBytes(signingKey)),
            ClockSkew = TimeSpan.FromSeconds(30),
        };
    });
builder.Services.AddAuthorization();

// Casos de uso (Application) que invocan los controllers (adaptadores de entrada).
builder.Services.AddScoped<Login>();
builder.Services.AddScoped<SyncTags>();
builder.Services.AddScoped<GetInventoryByEpc>();

// Adaptadores de salida: EF Core, BCrypt y JWT detrás de sus puertos.
builder.Services.AddInfrastructure(builder.Configuration);

var app = builder.Build();

await DatabaseInitializer.InitializeAsync(app.Services);

app.UseExceptionHandler();
app.UseAuthentication();
app.UseAuthorization();
app.MapControllers();

app.Run();
