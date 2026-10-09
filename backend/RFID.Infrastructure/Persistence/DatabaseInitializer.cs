using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.DependencyInjection;
using RFID.Infrastructure.Seeding;

namespace RFID.Infrastructure.Persistence;

public static class DatabaseInitializer
{
    // Al arrancar: aplica migraciones pendientes y ejecuta los seeders idempotentes.
    public static async Task InitializeAsync(IServiceProvider services)
    {
        using var scope = services.CreateScope();
        var context = scope.ServiceProvider.GetRequiredService<AppDbContext>();
        await context.Database.MigrateAsync();

        foreach (var seeder in scope.ServiceProvider.GetServices<IDataSeeder>())
            await seeder.SeedAsync(context);
    }
}
