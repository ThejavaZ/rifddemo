using Microsoft.EntityFrameworkCore;
using RFID.Domain.Entities;

namespace RFID.Infrastructure.Persistence;

public sealed class AppDbContext(DbContextOptions<AppDbContext> options) : DbContext(options)
{
    public DbSet<Tag> Tags => Set<Tag>();

    public DbSet<InventoryItem> Inventory => Set<InventoryItem>();

    public DbSet<User> Users => Set<User>();

    protected override void OnModelCreating(ModelBuilder modelBuilder)
        => modelBuilder.ApplyConfigurationsFromAssembly(typeof(AppDbContext).Assembly);
}
