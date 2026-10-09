using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Metadata.Builders;
using RFID.Domain.Entities;

namespace RFID.Infrastructure.Persistence.Configurations;

public sealed class InventoryItemConfiguration : IEntityTypeConfiguration<InventoryItem>
{
    public void Configure(EntityTypeBuilder<InventoryItem> builder)
    {
        builder.HasIndex(i => i.Epc).IsUnique();
        builder.Property(i => i.UpdatedAt).HasDefaultValueSql("NOW()");
    }
}
