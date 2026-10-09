using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using RFID.Application.Inventory;

namespace RFID.Api.Controllers;

public record InventoryResponse(string Id, string Epc, string Name, int Quantity);

[ApiController]
[Authorize]
[Route("api/v1/inventory")]
public class InventoryController : ControllerBase
{
    private readonly GetInventoryByEpc _getInventoryByEpc;

    public InventoryController(GetInventoryByEpc getInventoryByEpc)
        => _getInventoryByEpc = getInventoryByEpc;

    [HttpGet("{epc}")]
    public async Task<IActionResult> GetByEpc(string epc, CancellationToken cancellationToken)
    {
        var item = await _getInventoryByEpc.ExecuteAsync(epc, cancellationToken);
        if (item is null)
            return NotFound(new { error = $"Inventario no encontrado para epc '{epc}'" });

        return Ok(new InventoryResponse(item.Id, item.Epc, item.Name, item.Quantity));
    }
}
