using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using RFID.Application.Sync;

namespace RFID.Api.Controllers;

public record SyncTagRequest(string Epc, int Rssi, int Antenna, int ReadCount, long LastSeenTimestamp);

public record SyncTagsRequest(List<SyncTagRequest> Tags);

[ApiController]
[Authorize]
[Route("api/v1/sync")]
public class SyncController : ControllerBase
{
    private readonly SyncTags _syncTags;

    public SyncController(SyncTags syncTags) => _syncTags = syncTags;

    [HttpPost("tags")]
    public async Task<IActionResult> Tags([FromBody] SyncTagsRequest? request, CancellationToken cancellationToken)
    {
        var inputs = request?.Tags?
            .Select(tag => new SyncTagInput(tag.Epc, tag.Rssi, tag.Antenna, tag.ReadCount, tag.LastSeenTimestamp))
            .ToList();

        var result = await _syncTags.ExecuteAsync(inputs, cancellationToken);

        return Ok(result);
    }
}
