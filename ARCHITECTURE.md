# Arquitetura de geração de imagens

```text
Browser
  POST /api/generate-image
        |
        | IMAGE_GENERATION_BACKEND (server-only)
        +-- comfyui (default) --> /api/generate-image-comfyui
        |                         upload -> /upload/image
        |                         POST { prompt: workflow } -> /prompt
        |                         poll -> /history/{prompt_id}
        |                         output -> /view
        |
        +-- fooocus ------------> /api/generate-image-fooocus
                                  existing Fooocus API flow

Success response: { image: "data:image/...;base64,...", backend: "comfyui" | "fooocus" }
```

The browser always calls one same-origin route. It does not choose the provider or receive provider credentials. `ResultsView` uses the backend field returned by the server to label generated images; original photos are labeled separately.

## Server-side settings

ComfyUI:

- `IMAGE_GENERATION_BACKEND=comfyui` (default)
- `COMFYUI_API_URL`
- `COMFYUI_WORKFLOW_FILE` or `COMFYUI_WORKFLOW_JSON`
- `COMFYUI_API_KEY` only when an explicitly configured gateway accepts Bearer authentication

Fooocus:

- `IMAGE_GENERATION_BACKEND=fooocus`
- `FOOOCUS_API_URL`
- `FOOOCUS_API_KEY`

Do not use `NEXT_PUBLIC_*` for these settings. The current app does not have user authentication or distributed rate limiting, so production exposure requires additional access protection. Pod status and cost are not tracked or verified by this architecture document.

See [COMFYUI_INTEGRATION.md](COMFYUI_INTEGRATION.md) for workflow requirements and Runpod handoff.
