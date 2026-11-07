# Status da integração de geração

## Implementado no código

- `/api/generate-image` seleciona server-side `comfyui` (padrão) ou `fooocus` via `IMAGE_GENERATION_BACKEND`.
- Fooocus foi preservado em `/api/generate-image-fooocus`, com sua própria configuração server-side `FOOOCUS_API_URL` e `FOOOCUS_API_KEY`.
- A rota ComfyUI faz upload multipart para `/upload/image`, injeta prompts nos marcadores do workflow API, envia o envelope `{ prompt: workflow }`, consulta `/history/{prompt_id}` e recupera imagens dos nós `SaveImage`/`PreviewImage`.
- A interface identifica o provedor pela resposta do servidor; rótulos e resultados deixam de assumir Fooocus.
- Nenhum segredo deve ser exposto ao navegador ou enviado em chats.

## Ainda bloqueado por dados reais

- Workflow ComfyUI exportado em formato API, com os nós `LoadImage`, prompts positivos/negativos e nó de saída requeridos.
- URL real HTTPS do endpoint ComfyUI configurada no ambiente server-side.
- Confirmação de que o gateway/Pod está acessível da Vercel e de qual esquema de autenticação (se houver) aceita.
- Geração real end-to-end. Não foi feita nesta implementação.

Não há evidência nesta sessão para declarar qualquer Pod Runpod ativo, desligado ou com custo específico. Consulte o painel Runpod antes de iniciar um teste com GPU.

## Configuração

Ver [README](README.md) e [guia ComfyUI](../COMFYUI_INTEGRATION.md). Variáveis principais:

- `IMAGE_GENERATION_BACKEND=comfyui` (default) ou `fooocus`
- `COMFYUI_API_URL`
- `COMFYUI_WORKFLOW_FILE` ou `COMFYUI_WORKFLOW_JSON`
- `COMFYUI_API_KEY` somente quando um gateway configurado explicitamente aceita Bearer
- `FOOOCUS_API_URL` e `FOOOCUS_API_KEY` para Fooocus

O build verifica compilação, não o Pod, o workflow, a autenticação nem a geração. A API ainda não fornece autenticação de usuário ou rate limiting distribuído; proteja o acesso antes de expor geração de GPU publicamente.
