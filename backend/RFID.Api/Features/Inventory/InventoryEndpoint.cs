using Dapper;
using Npgsql;

namespace RFID.Api.Features.Inventory;

public record InventoryResponse(string Id, string Epc, string Name, int Quantity);

public static class InventoryEndpoints
{
    private const string GetByEpcSql = """
        SELECT id, epc, name, quantity
        FROM inventory
        WHERE epc = @Epc
        """;

    public static IEndpointRouteBuilder MapInventoryEndpoints(this IEndpointRouteBuilder app)
    {
        app.MapGet("/api/v1/inventory/{epc}", Handle).RequireAuthorization();
        return app;
    }

    private static async Task<IResult> Handle(string epc, NpgsqlDataSource db, CancellationToken cancellationToken)
    {
        await using var connection = await db.OpenConnectionAsync(cancellationToken);
        var item = await connection.QuerySingleOrDefaultAsync<InventoryRow>(
            new CommandDefinition(GetByEpcSql, new { Epc = epc }, cancellationToken: cancellationToken));

        return item is null
            ? Results.NotFound(new { error = $"Inventario no encontrado para epc '{epc}'" })
            : Results.Ok(new InventoryResponse(item.Id, item.Epc, item.Name, item.Quantity));
    }

    private sealed record InventoryRow(string Id, string Epc, string Name, int Quantity);
}
