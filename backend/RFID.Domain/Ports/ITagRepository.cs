using RFID.Domain.Entities;

namespace RFID.Domain.Ports;

// Puerto de salida: el dominio pide persistir lecturas, no sabe que existe PostgreSQL.
public interface ITagRepository
{
    // Idempotente: reenviar el mismo lote produce el mismo estado (no acumula read_count).
    Task UpsertBatchAsync(IReadOnlyCollection<Tag> tags, CancellationToken cancellationToken = default);
}
