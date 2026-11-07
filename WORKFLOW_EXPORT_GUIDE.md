# Exportar o workflow API do ComfyUI

Este projeto precisa do workflow **real** do ComfyUI em formato API. A integração está bloqueada até esse JSON e a configuração server-side existirem. Leia primeiro [COMFYUI_INTEGRATION.md](COMFYUI_INTEGRATION.md) para os requisitos de segurança e execução.

1. Abra a interface do ComfyUI já existente e carregue o workflow de edição de imagem que você pretende usar.
2. Use o comando da interface para exportar/salvar em **API format**. O nome exato do item varia por versão. Não copie o JSON visual do canvas e não use snippets de JavaScript do navegador para reconstruir o grafo.
3. Confira se o JSON é um objeto de nós com `class_type` e `inputs` em cada nó.
4. No arquivo copiado, indique os valores de texto que devem receber `POSITIVE_PROMPT` e `NEGATIVE_PROMPT`. Confirme que há um `LoadImage` com `inputs.image` e um nó final `SaveImage` ou `PreviewImage`.
5. Remova credenciais, tokens, URLs assinadas e dados pessoais. Compartilhe o JSON apenas em um canal de trabalho aprovado; nunca cole segredos no chat.

**Não substitua o workflow por um exemplo genérico.** O JSON precisa corresponder aos nós, modelos e arquivos realmente instalados no Pod. Se não existir workflow adequado, peça ao responsável para criá-lo explicitamente e aguarde autorização antes de qualquer alteração ou gasto.

Quando o JSON API estiver revisado, salve-o como `web/config/comfyui-workflow-api.json` (ou configure `COMFYUI_WORKFLOW_JSON` no ambiente do servidor). O código exige os marcadores literais, faz o upload da foto, submete `{ "prompt": workflow }`, consulta o histórico e busca a imagem final. Uma compilação bem-sucedida não substitui um teste real autorizado.
