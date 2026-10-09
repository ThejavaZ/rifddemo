namespace RFID.Domain.Entities;

// Lectura persistida por el sync: el EPC es la clave (upsert idempotente).
public class Tag
{
    public string Epc { get; set; } = default!;

    public int Rssi { get; set; }

    public int Antenna { get; set; }

    public int ReadCount { get; set; }

    public DateTimeOffset LastSeenAt { get; set; }

    public DateTimeOffset ReceivedAt { get; set; }

    private Tag()
    {
    }

    public Tag(string epc, int rssi, int antenna, int readCount, DateTimeOffset lastSeenAt)
    {
        if (string.IsNullOrWhiteSpace(epc))
            throw new ArgumentException("EPC es requerido.", nameof(epc));

        Epc = epc;
        Rssi = rssi;
        Antenna = antenna;
        ReadCount = readCount;
        LastSeenAt = lastSeenAt;
    }
}
