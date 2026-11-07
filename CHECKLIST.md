# Checklist ComfyUI

## Código

- [x] Seleção de backend server-side (`IMAGE_GENERATION_BACKEND`).
- [x] Fooocus preservado em uma rota própria.
- [x] Contrato ComfyUI: upload, `POST /prompt` com `{ prompt }`, polling e leitura da saída.
- [x] Interface e resultados sem rótulo fixo Fooocus.
- [x] Documentação corrigida para não pedir token via chat nem afirmar estado de Pod não verificado.

## Falta antes de geração real

- [ ] Workflow API real aprovado, com `LoadImage`, `POSITIVE_PROMPT`, `NEGATIVE_PROMPT` e nó de imagem de saída.
- [ ] `COMFYUI_API_URL` configurada no ambiente server-side e acessível pela implantação.
- [ ] Autenticação/proteção do ComfyUI confirmada; não assumir que o servidor nativo aceita `COMFYUI_API_KEY`.
- [ ] Autenticação e rate limiting para a rota pública planejados antes de produção.
- [ ] Build atualizado concluído.
- [ ] Teste end-to-end autorizado usando imagem com consentimento; sem iniciar ou alterar infraestrutura automaticamente.

Build aprovado não prova que o workflow, endpoint ou Pod real funcionam. Consulte [INTEGRATION_STATUS.md](INTEGRATION_STATUS.md).
