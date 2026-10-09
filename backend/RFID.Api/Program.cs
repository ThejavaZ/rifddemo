using Npgsql;
using RFID.Api.Features.Auth;
using RFID.Api.Features.Inventory;
using RFID.Api.Features.Sync;
using RFID.Api.Infrastructure;

var builder = WebApplication.CreateBuilder(args);

builder.Services.AddSingleton(_ => NpgsqlDataSource.Create(
    builder.Configuration.GetConnectionString("Database")
        ?? throw new InvalidOperationException("ConnectionStrings:Database es requerido")));
builder.Services.AddSingleton<TokenService>();

var app = builder.Build();

await DatabaseInitializer.InitializeAsync(app.Services);

app.MapAuthEndpoints();
app.MapSyncEndpoints();
app.MapInventoryEndpoints();

app.Run();
