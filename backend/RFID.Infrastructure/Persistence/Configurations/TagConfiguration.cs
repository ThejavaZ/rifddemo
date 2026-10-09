using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Metadata.Builders;
using RFID.Domain.Entities;

namespace RFID.Infrastructure.Persistence.Configurations;

public sealed class TagConfiguration : IEntityTypeConfiguration<Tag>
{
    public void Configure(EntityTypeBuilder<Tag> builder)
    {
        builder.HasKey(t => t.Epc);
        builder.Property(t => t.Epc).ValueGeneratedNever();
        builder.Property(t => t.ReceivedAt).HasDefaultValueSql("NOW()");
    }
}
