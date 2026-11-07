# Dados técnicos pendentes da integração ComfyUI

Use este checklist para registrar informações **não secretas**. Não cole tokens, API keys, fotos pessoais nem URLs assinadas neste arquivo ou em chats.

## Pod/endpoint existente (inspeção read-only)

- Estado observado e data/hora:
- Porta do ComfyUI:
- URL pública HTTPS configurada no ambiente do servidor (não precisa ser reproduzida aqui):
- O endpoint responde sem alteração/inicialização do Pod?:
- Autenticação requerida? (sim/não; não inclua o valor da credencial):
- Se autenticado, esquema/header aceito pelo gateway:

Não trate um relatório antigo como prova do estado atual. Confirme no painel Runpod antes de qualquer teste que possa custar créditos.

## Workflow API real

- Workflow existente e testado no próprio ComfyUI?:
- Exportado em API format (não visual)?:
- JSON guardado em local privado/aprovado:
- Nó(s) `LoadImage` e input `image`:
- Nó(s)/inputs para o marcador `POSITIVE_PROMPT`:
- Nó(s)/inputs para o marcador `NEGATIVE_PROMPT`:
- Nó(s) de saída `SaveImage`/`PreviewImage`:
- Nomes dos modelos exigidos:
- Restrições observadas:

## Próximos passos no código

- Colocar o JSON aprovado em `web/config/comfyui-workflow-api.json`, ou configurar `COMFYUI_WORKFLOW_JSON` no ambiente server-side.
- Configurar `COMFYUI_API_URL` e `IMAGE_GENERATION_BACKEND=comfyui` no servidor.
- Nunca enviar segredos via chat, issue, prompt ou variável `NEXT_PUBLIC_*`.
- Fazer build e, com autorização explícita e consentimento para a imagem, um teste real controlado.

Fooocus permanece disponível por `/api/generate-image-fooocus` e pode ser selecionado com `IMAGE_GENERATION_BACKEND=fooocus`.
