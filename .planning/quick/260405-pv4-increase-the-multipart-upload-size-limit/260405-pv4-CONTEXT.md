# Quick Task 260405-pv4: increase the multipart upload size limit to 500MB by adding spring.servlet.multipart.max-file-size=500MB and spring.servlet.multipart.max-request-size=500MB to application.properties. add file format validation on POST /api/v1/sessions to reject any file that doesn't have the .s22 extension, returning a RFC-7807 problem detail response with status 400 and a clear error message if the extension is wrong

**Gathered:** 2026-04-05
**Status:** Ready for planning

<domain>
## Task Boundary

increase the multipart upload size limit to 500MB by adding spring.servlet.multipart.max-file-size=500MB and spring.servlet.multipart.max-request-size=500MB to application.properties. add file format validation on POST /api/v1/sessions to reject any file that doesn't have the .s22 extension, returning a RFC-7807 problem detail response with status 400 and a clear error message if the extension is wrong

</domain>

<decisions>
## Implementation Decisions

### Download Endpoint Behavior
- After the session file is downloaded via `GET /api/v1/sessions/{id}/download`, automatically delete the session from cache.

### Edge Case Handling
- Handle session not found with 404 Not Found.
- Handle expired session with 410 Gone.
- Handle attempting to download an already-deleted session with 404 Not Found.

### MaxUploadSizeExceededException
- Let Spring's default handler or a global exception handler deal with it unless specifically mandated, ensuring it doesn't leak internals.
</decisions>

<specifics>
## Specific Ideas

No specific requirements — open to standard approaches
</specifics>

<canonical_refs>
## Canonical References

No external specs — requirements fully captured in decisions above
</canonical_refs>