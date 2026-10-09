using Microsoft.EntityFrameworkCore;
using RFID.Domain.Entities;
using RFID.Infrastructure.Persistence;

namespace RFID.Infrastructure.Seeding;

// Productos demo: mismos EPC que la app Android usa para probar.
public sealed class InventorySeeder : IDataSeeder
{
    private static readonly InventoryItem[] DemoItems =
    [
        new("inv-0001", "E280116000000209", "Producto Demo A", 10),
        new("inv-0002", "E280116000000217", "Producto Demo B", 5),
    ];

    public async Task SeedAsync(AppDbContext context, CancellationToken cancellationToken = default)
    {
        foreach (var item in DemoItems)
        {
            var exists = await context.Inventory.AnyAsync(i => i.Epc == item.Epc, cancellationToken);
            if (!exists)
                context.Inventory.Add(item);
        }

        await context.SaveChangesAsync(cancellationToken);
    }
}
