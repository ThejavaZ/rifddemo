using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Design;

namespace RFID.Infrastructure.Persistence;

// Permite `dotnet ef` generar migraciones sin ejecutar Program.cs ni tocar la base real.
public sealed class AppDbContextDesignFactory : IDesignTimeDbContextFactory<AppDbContext>
{
    public AppDbContext CreateDbContext(string[] args)
    {
        var builder = new DbContextOptionsBuilder<AppDbContext>();
        builder.AddRfidDatabase("Host=localhost;Database=rfid_design");
        return new AppDbContext(builder.Options);
    }
}
