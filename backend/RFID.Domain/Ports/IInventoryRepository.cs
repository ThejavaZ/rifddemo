using RFID.Domain.Entities;

namespace RFID.Domain.Ports;

public interface IInventoryRepository
{
    Task<InventoryItem?> GetByEpcAsync(string epc, CancellationToken cancellationToken = default);
}
