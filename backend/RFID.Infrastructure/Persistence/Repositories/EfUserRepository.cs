using Microsoft.EntityFrameworkCore;
using RFID.Domain.Entities;
using RFID.Domain.Ports;
using RFID.Infrastructure.Persistence;

namespace RFID.Infrastructure.Persistence.Repositories;

public sealed class EfUserRepository(AppDbContext context) : IUserRepository
{
    public Task<User?> GetByUsernameAsync(string username, CancellationToken cancellationToken = default)
        => context.Users.FirstOrDefaultAsync(u => u.Username == username, cancellationToken);
}
