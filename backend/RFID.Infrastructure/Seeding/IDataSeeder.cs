using RFID.Infrastructure.Persistence;

namespace RFID.Infrastructure.Seeding;

// Puerto de infraestructura: cada seeder rellena sus datos de forma idempotente.
public interface IDataSeeder
{
    Task SeedAsync(AppDbContext context, CancellationToken cancellationToken = default);
}
