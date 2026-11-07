# Integração ComfyUI

## Situação real

O app tem uma rota de geração ComfyUI que envia a foto ao endpoint `/upload/image`, substitui os marcadores do workflow, envia `{ "prompt": workflow }` a `/prompt`, consulta `/history/{prompt_id}` e busca a imagem de saída em `/view`.

A integração de código está preparada, mas **não está pronta para gerar até receber o workflow API real e configurar o endpoint do ComfyUI**. Não há workflow de exemplo embutido nem URL/estado de Pod verificados neste documento. O build confirma compilação, não uma geração real.

Fooocus foi preservado em `/api/generate-image-fooocus`. O app chama `/api/generate-image`, cuja seleção é server-side por `IMAGE_GENERATION_BACKEND`; o padrão é `comfyui`, e o único outro valor aceito é `fooocus`.

## O que o workflow precisa ter

Exporte do ComfyUI o **API format JSON**, não o JSON visual do canvas. O objeto de nível superior deve mapear IDs de nós para objetos com `class_type` e `inputs`. Configure nele:

- Um ou mais nós `LoadImage`, cujo campo `inputs.image` seja uma string.
- O texto de cada condicionamento positivo que deve receber a solicitação com o valor literal `POSITIVE_PROMPT`.
- O texto do condicionamento negativo com o valor literal `NEGATIVE_PROMPT`.
- Pelo menos um nó final `SaveImage` ou `PreviewImage`.

A rota substitui `LoadImage.inputs.image` pelo nome retornado no upload, injeta os dois prompts e procura imagens apenas nos nós de saída identificados. Todos os nós/modelos e conexões restantes precisam pertencer ao workflow real e ser compatíveis com o Pod. Não use um JSON inventado ou visual como configuração de produção.

## Configuração server-side

No `web/.env.local` para desenvolvimento, ou nas variáveis do projeto Vercel:

```env
IMAGE_GENERATION_BACKEND=comfyui
COMFYUI_API_URL=https://<URL-HTTPS-DO-POD>
COMFYUI_WORKFLOW_FILE=config/comfyui-workflow-api.json
```

Coloque o workflow API real no caminho configurado, relativo a `process.cwd()`, ou forneça o JSON inteiro na variável server-side `COMFYUI_WORKFLOW_JSON`. O padrão do arquivo é `config/comfyui-workflow-api.json`. O arquivo do workflow não contém credenciais; ainda assim, confirme que pode ser incluído no repositório antes de adicioná-lo.

`COMFYUI_API_KEY` é opcional e a rota a envia como `Authorization: Bearer`. O ComfyUI padrão não habilita essa autenticação por si só; só configure a variável se o gateway/proxy do endpoint aceitar esse esquema. Nunca coloque URL, chave ou JSON em variáveis `NEXT_PUBLIC_*`, nem cole tokens em chats, issues ou prompts.

A rota limita endpoints remotos a HTTPS em hosts sob `runpod.io` ou `runpod.net` (e permite `localhost` HTTP em desenvolvimento). Se o Pod não for acessível pela Vercel, confirme URL e porta no painel, sem iniciar ou alterar infraestrutura durante esta etapa.

## Segurança e custo

A rota web ainda não implementa autenticação de usuário nem rate limiting distribuído. Portanto, antes de habilitar a geração em um site público, proteja o acesso à funcionalidade do app e ao endpoint do ComfyUI contra chamadas não autorizadas e consumo de GPU. Não exponha uma porta ComfyUI aberta à internet como se `COMFYUI_API_KEY` adicionasse autenticação automaticamente. Não execute um teste pago sem autorização explícita e confirmação de que o Pod já está ativo.

Fotos são enviadas ao servidor web e, em seguida, ao ComfyUI. A rota aceita JPEG, PNG ou WebP de até 3 MB e devolve a imagem gerada em base64. Não use fotos de pessoas reais em testes sem consentimento.

## Handoff para a IA com acesso ao Runpod

Peça à outra IA somente uma inspeção **read-only** do ComfyUI/Pod já existente e a entrega dos dados técnicos abaixo. Não solicite criação, inicialização, parada, exclusão ou alteração de recursos billable; não peça nem envie API keys, tokens ou fotos pessoais.

1. Confirme se a interface ComfyUI já responde e registre apenas o estado atual do Pod e a porta acessível; não altere o estado.
2. No workflow de edição de imagem existente, exporte o **API format JSON**. Se não existir um workflow adequado, pare e informe isso; não invente nós/modelos nem instale arquivos.
3. Identifique, no JSON, os IDs/tipos dos nós `LoadImage`, os inputs de texto positivo e negativo, os nós `SaveImage`/`PreviewImage`, e os arquivos de modelo referenciados (nomes apenas).
4. Entregue o JSON API e indique onde inserir os marcadores `POSITIVE_PROMPT` e `NEGATIVE_PROMPT`. Remova qualquer token, URL assinada ou dado de usuário antes de compartilhar.
5. Não cole credenciais no chat. Informe somente se o endpoint exige autenticação e qual esquema/header o gateway espera, sem revelar o valor secreto.

## Validação depois de obter os dados

1. Confirme o JSON estruturalmente e instale-o em `web/config/comfyui-workflow-api.json` ou configure `COMFYUI_WORKFLOW_JSON` no servidor.
2. Configure as variáveis server-side e mantenha Fooocus selecionável com `IMAGE_GENERATION_BACKEND=fooocus`.
3. Rode `npm run build` em `web` para verificar compilação.
4. Só então, com autorização do usuário e consentimento para a foto, faça uma única geração real e confirme upload, prompt, conclusão e imagem de saída. Build aprovado sozinho não atesta funcionamento com o Pod.
