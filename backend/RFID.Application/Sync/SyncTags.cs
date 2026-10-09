using RFID.Domain.Entities;
using RFID.Domain.Ports;

namespace RFID.Application.Sync;

public record SyncTagInput(string Epc, int Rssi, int Antenna, int ReadCount, long LastSeenTimestamp);

public record SyncTagsResult(int SyncedCount);

public sealed class SyncTags
{
    public const int MaxBatchSize = 500;

    private readonly ITagRepository _tags;

    public SyncTags(ITagRepository tags) => _tags = tags;

    public async Task<SyncTagsResult> ExecuteAsync(
        IReadOnlyCollection<SyncTagInput>? input,
        CancellationToken cancellationToken = default)
    {
        if (input is null || input.Count == 0)
            throw new ValidationException("Se requiere al menos un tag");

        if (input.Count > MaxBatchSize)
            throw new ValidationException($"El lote no puede superar {MaxBatchSize} tags");

        if (input.Any(tag => string.IsNullOrWhiteSpace(tag.Epc)))
            throw new ValidationException("Todo tag requiere un epc");

        var tags = input
            .GroupBy(tag => tag.Epc)
            .Select(group => group.Last())
            .Select(tag => new Tag(
                tag.Epc,
                tag.Rssi,
                tag.Antenna,
                tag.ReadCount,
                DateTimeOffset.FromUnixTimeMilliseconds(tag.LastSeenTimestamp)))
            .ToList();

        await _tags.UpsertBatchAsync(tags, cancellationToken);

        return new SyncTagsResult(input.Count);
    }
}
