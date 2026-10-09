using Dapper;
using Npgsql;

namespace RFID.Api.Features.Sync;

public record SyncTagRequest(string Epc, int Rssi, int Antenna, int ReadCount, long LastSeenTimestamp);

public record SyncTagsRequest(List<SyncTagRequest> Tags);

public record SyncTagsResponse(int SyncedCount);

public static class SyncEndpoints
{
    private const int MaxBatchSize = 500;

    // Idempotente: reenviar el mismo lote produce el mismo estado (no acumula read_count).
    private const string UpsertSql = """
        INSERT INTO tags (epc, rssi, antenna, read_count, last_seen_at)
        VALUES (@Epc, @Rssi, @Antenna, @ReadCount, @LastSeenAt)
        ON CONFLICT (epc) DO UPDATE SET
            rssi         = EXCLUDED.rssi,
            antenna      = EXCLUDED.antenna,
            read_count   = EXCLUDED.read_count,
            last_seen_at = EXCLUDED.last_seen_at,
            received_at  = NOW()
        """;

    public static IEndpointRouteBuilder MapSyncEndpoints(this IEndpointRouteBuilder app)
    {
        app.MapPost("/api/v1/sync/tags", Handle);
        return app;
    }

    private static async Task<IResult> Handle(
        SyncTagsRequest? request,
        NpgsqlDataSource db,
        CancellationToken cancellationToken)
    {
        if (request?.Tags is null || request.Tags.Count == 0)
            return Results.BadRequest(new { error = "Se requiere al menos un tag" });

        if (request.Tags.Count > MaxBatchSize)
            return Results.BadRequest(new { error = $"El lote no puede superar {MaxBatchSize} tags" });

        if (request.Tags.Any(tag => string.IsNullOrWhiteSpace(tag.Epc)))
            return Results.BadRequest(new { error = "Todo tag requiere un epc" });

        await using var connection = await db.OpenConnectionAsync(cancellationToken);
        await using var transaction = await connection.BeginTransactionAsync(cancellationToken);

        foreach (var tag in request.Tags)
        {
            await connection.ExecuteAsync(new CommandDefinition(
                UpsertSql,
                new
                {
                    tag.Epc,
                    tag.Rssi,
                    tag.Antenna,
                    tag.ReadCount,
                    LastSeenAt = DateTimeOffset.FromUnixTimeMilliseconds(tag.LastSeenTimestamp)
                },
                transaction: transaction,
                cancellationToken: cancellationToken));
        }

        await transaction.CommitAsync(cancellationToken);
        return Results.Ok(new SyncTagsResponse(request.Tags.Count));
    }
}
