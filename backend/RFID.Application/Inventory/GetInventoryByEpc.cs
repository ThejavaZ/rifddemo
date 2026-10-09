using RFID.Domain.Entities;
using RFID.Domain.Ports;

namespace RFID.Application.Inventory;

public sealed class GetInventoryByEpc
{
    private readonly IInventoryRepository _inventory;

    public GetInventoryByEpc(IInventoryRepository inventory) => _inventory = inventory;

    public Task<InventoryItem?> ExecuteAsync(string epc, CancellationToken cancellationToken = default)
        => _inventory.GetByEpcAsync(epc, cancellationToken);
}
