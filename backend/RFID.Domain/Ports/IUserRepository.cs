using RFID.Domain.Entities;

namespace RFID.Domain.Ports;

public interface IUserRepository
{
    Task<User?> GetByUsernameAsync(string username, CancellationToken cancellationToken = default);
}
