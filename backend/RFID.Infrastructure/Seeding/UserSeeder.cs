using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.Configuration;
using RFID.Application.Ports;
using RFID.Domain.Entities;
using RFID.Infrastructure.Persistence;

namespace RFID.Infrastructure.Seeding;

// Crea el usuario inicial solo si no existe; su password se guarda hasheada (nunca en claro).
public sealed class UserSeeder(IConfiguration configuration, IPasswordHasher passwordHasher) : IDataSeeder
{
    public async Task SeedAsync(AppDbContext context, CancellationToken cancellationToken = default)
    {
        var username = configuration["Auth:SeedUser:Username"];
        var password = configuration["Auth:SeedUser:Password"];
        if (string.IsNullOrWhiteSpace(username) || string.IsNullOrWhiteSpace(password))
            return;

        var exists = await context.Users.AnyAsync(u => u.Username == username, cancellationToken);
        if (exists)
            return;

        context.Users.Add(new User($"usr-{Guid.NewGuid():N}", username, passwordHasher.Hash(password)));
        await context.SaveChangesAsync(cancellationToken);
    }
}
