using Dapper;
using Npgsql;

namespace RFID.Api.Infrastructure;

public static class DatabaseInitializer
{
    private const string SchemaSql = """
        CREATE TABLE IF NOT EXISTS tags (
            epc          TEXT PRIMARY KEY,
            rssi         INTEGER NOT NULL,
            antenna      INTEGER NOT NULL,
            read_count   INTEGER NOT NULL,
            last_seen_at TIMESTAMPTZ NOT NULL,
            received_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
        );

        CREATE TABLE IF NOT EXISTS inventory (
            id         TEXT PRIMARY KEY,
            epc        TEXT NOT NULL UNIQUE,
            name       TEXT NOT NULL,
            quantity   INTEGER NOT NULL,
            updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
        );

        CREATE TABLE IF NOT EXISTS users (
            id            TEXT PRIMARY KEY,
            username      TEXT NOT NULL UNIQUE,
            password_hash TEXT NOT NULL,
            created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW()
        );
        """;

    private const string SeedSql = """
        INSERT INTO inventory (id, epc, name, quantity) VALUES
            ('inv-0001', 'E280116000000209', 'Producto Demo A', 10),
            ('inv-0002', 'E280116000000217', 'Producto Demo B', 5)
        ON CONFLICT (epc) DO NOTHING;
        """;

    public static async Task InitializeAsync(IServiceProvider services)
    {
        var db = services.GetRequiredService<NpgsqlDataSource>();
        await using var connection = await db.OpenConnectionAsync();
        await connection.ExecuteAsync(SchemaSql);
        await connection.ExecuteAsync(SeedSql);
        await SeedUserAsync(connection, services.GetRequiredService<IConfiguration>());
    }

    // Crea el usuario inicial solo si no existe; su password se guarda hasheada con BCrypt.
    private static async Task SeedUserAsync(NpgsqlConnection connection, IConfiguration configuration)
    {
        var username = configuration["Auth:SeedUser:Username"];
        var password = configuration["Auth:SeedUser:Password"];
        if (string.IsNullOrWhiteSpace(username) || string.IsNullOrWhiteSpace(password))
            return;

        const string existsSql = "SELECT 1 FROM users WHERE username = @Username";
        var exists = await connection.ExecuteScalarAsync<int?>(
            new CommandDefinition(existsSql, new { Username = username }));
        if (exists is not null)
            return;

        const string insertSql =
            "INSERT INTO users (id, username, password_hash) VALUES (@Id, @Username, @PasswordHash)";
        await connection.ExecuteAsync(new CommandDefinition(insertSql, new
        {
            Id = $"usr-{Guid.NewGuid():N}",
            Username = username,
            PasswordHash = BCrypt.Net.BCrypt.HashPassword(password),
        }));
    }
}
