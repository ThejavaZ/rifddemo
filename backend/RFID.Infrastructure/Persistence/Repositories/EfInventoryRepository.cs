using Microsoft.EntityFrameworkCore;
using RFID.Domain.Entities;
using RFID.Domain.Ports;
using RFID.Infrastructure.Persistence;

namespace RFID.Infrastructure.Persistence.Repositories;

public sealed class EfInventoryRepository(AppDbContext context) : IInventoryRepository
{
    public Task<InventoryItem?> GetByEpcAsync(string epc, CancellationToken cancellationToken = default)
        => context.Inventory.FirstOrDefaultAsync(i => i.Epc == epc, cancellationToken);
}
