namespace RFID.Domain.Entities;

public class InventoryItem
{
    public string Id { get; set; } = default!;

    public string Epc { get; set; } = default!;

    public string Name { get; set; } = default!;

    public int Quantity { get; set; }

    public DateTimeOffset UpdatedAt { get; set; }

    private InventoryItem()
    {
    }

    public InventoryItem(string id, string epc, string name, int quantity)
    {
        if (string.IsNullOrWhiteSpace(id))
            throw new ArgumentException("Id es requerido.", nameof(id));
        if (string.IsNullOrWhiteSpace(epc))
            throw new ArgumentException("EPC es requerido.", nameof(epc));
        if (string.IsNullOrWhiteSpace(name))
            throw new ArgumentException("Nombre es requerido.", nameof(name));
        if (quantity < 0)
            throw new ArgumentException("Cantidad no puede ser negativa.", nameof(quantity));

        Id = id;
        Epc = epc;
        Name = name;
        Quantity = quantity;
    }
}
