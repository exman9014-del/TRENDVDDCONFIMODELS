# Status da integração ComfyUI

## Confirmado no código

- O app escolhe backend no servidor em `/api/generate-image`: `comfyui` (padrão) ou `fooocus`, via `IMAGE_GENERATION_BACKEND`.
- Fooocus foi preservado em `/api/generate-image-fooocus`.
- A rota ComfyUI implementa upload da foto, envelope correto `{ prompt: workflow }`, polling do histórico e recuperação de imagem dos nós de saída.
- A interface usa o provider retornado pelo servidor para os rótulos.
- Workflow, URL do Pod e autenticação real ainda não foram configurados nem testados.

## Não verificado

Os relatórios anteriores tinham informações específicas sobre IDs, status, preço, região e URL de Pods. Elas não foram verificadas nesta sessão; não use esses dados como estado atual. Confirme no painel Runpod antes de qualquer teste ou ação billable. Nenhum recurso foi alterado nesta implementação.

## Bloqueadores

1. Obter o workflow API real da edição de imagem e preparar os marcadores `POSITIVE_PROMPT` / `NEGATIVE_PROMPT`, nó `LoadImage` e saída `SaveImage`/`PreviewImage`.
2. Configurar `COMFYUI_API_URL` no ambiente server-side e confirmar acessibilidade a partir da implantação web.
3. Confirmar se o gateway exige autenticação e qual esquema aceita. O ComfyUI padrão não ativa Bearer auth automaticamente.
4. Proteger o uso público com autenticação/rate limiting antes de expor a rota, pois a aplicação ainda não possui esses controles.
5. Realizar uma geração real apenas após autorização explícita e consentimento para a foto.

Veja [COMFYUI_INTEGRATION.md](COMFYUI_INTEGRATION.md) e [WORKFLOW_EXPORT_GUIDE.md](WORKFLOW_EXPORT_GUIDE.md) para os requisitos/handoff.
