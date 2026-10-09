using Microsoft.EntityFrameworkCore;
using RFID.Domain.Entities;
using RFID.Domain.Ports;
using RFID.Infrastructure.Persistence;

namespace RFID.Infrastructure.Persistence.Repositories;

// Adaptador de salida: implementa ITagRepository con EF Core sobre PostgreSQL.
public sealed class EfTagRepository(AppDbContext context) : ITagRepository
{
    public async Task UpsertBatchAsync(IReadOnlyCollection<Tag> tags, CancellationToken cancellationToken = default)
    {
        var epcs = tags.Select(t => t.Epc).ToList();
        var stored = await context.Tags
            .Where(t => epcs.Contains(t.Epc))
            .ToListAsync(cancellationToken);
        var storedByEpc = stored.ToDictionary(t => t.Epc);

        foreach (var tag in tags)
        {
            if (storedByEpc.TryGetValue(tag.Epc, out var existing))
            {
                existing.Rssi = tag.Rssi;
                existing.Antenna = tag.Antenna;
                existing.ReadCount = tag.ReadCount;
                existing.LastSeenAt = tag.LastSeenAt;
                existing.ReceivedAt = DateTimeOffset.UtcNow;
            }
            else
            {
                context.Tags.Add(tag);
            }
        }

        // Un solo SaveChanges = una transacción (mismo comportamiento que el upsert SQL anterior).
        await context.SaveChangesAsync(cancellationToken);
    }
}
