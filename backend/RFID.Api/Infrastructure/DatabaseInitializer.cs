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
    }
}
