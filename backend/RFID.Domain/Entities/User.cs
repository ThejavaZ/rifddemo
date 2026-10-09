namespace RFID.Domain.Entities;

// Solo guarda el hash: la password en claro nunca llega al dominio.
public class User
{
    public string Id { get; set; } = default!;

    public string Username { get; set; } = default!;

    public string PasswordHash { get; set; } = default!;

    public DateTimeOffset CreatedAt { get; set; }

    private User()
    {
    }

    public User(string id, string username, string passwordHash)
    {
        if (string.IsNullOrWhiteSpace(id))
            throw new ArgumentException("Id es requerido.", nameof(id));
        if (string.IsNullOrWhiteSpace(username))
            throw new ArgumentException("Username es requerido.", nameof(username));
        if (string.IsNullOrWhiteSpace(passwordHash))
            throw new ArgumentException("PasswordHash es requerido.", nameof(passwordHash));

        Id = id;
        Username = username;
        PasswordHash = passwordHash;
    }
}
