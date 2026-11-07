# Credenciais e variáveis de configuração

Este guia explica o que o app e as ferramentas de publicação precisam. Ele lista nomes e finalidades; **não coloque valores reais de chaves, tokens ou senhas neste arquivo**. O repositório é público.

## Para o app funcionar com ComfyUI

| Variável/dado | Necessidade | Sensibilidade |
|---|---|---|
| `IMAGE_GENERATION_BACKEND=comfyui` | Seleciona o ComfyUI na rota do servidor. Esse já é o padrão no código. | Não é segredo. |
| `COMFYUI_API_URL` | Endereço HTTPS do serviço ComfyUI do Pod Runpod; a aplicação web faz as chamadas para esse endereço. | Não é chave, mas identifica seu serviço. Configure no `.env.local` ou no ambiente da Vercel, não precisa publicar seu endereço pessoal. |
| Workflow API (`COMFYUI_WORKFLOW_FILE` ou `COMFYUI_WORKFLOW_JSON`) | Define os nós/modelos e conexões que o servidor submete ao ComfyUI. O arquivo padrão é `config/comfyui-workflow-api.json`. | Não é credencial; pode revelar nomes de modelos e a estrutura do workflow. |
| `COMFYUI_API_KEY` | Só se o proxy/gateway configurado na frente do ComfyUI exigir o cabeçalho `Authorization: Bearer`. | **Se existir, é segredo.** O servidor ComfyUI padrão não cria essa autenticação automaticamente. |

O `COMFYUI_API_URL` não é a API de administração Runpod: é o endpoint HTTP do ComfyUI na porta configurada para o Pod.

## Para usar Fooocus em vez de ComfyUI

| Variável | Necessidade | Sensibilidade |
|---|---|---|
| `IMAGE_GENERATION_BACKEND=fooocus` | Seleciona Fooocus no servidor. | Não é segredo. |
| `FOOOCUS_API_URL` | Endereço HTTPS do serviço Fooocus. | Não é chave; mantenha a configuração fora do navegador. |
| `FOOOCUS_API_KEY` | Chave enviada pelo servidor no cabeçalho `X-API-KEY`. | **Segredo obrigatório para a integração Fooocus atual.** |

Não é necessário configurar Fooocus se o backend escolhido for ComfyUI.

## Supabase

| Variável | Necessidade | Sensibilidade |
|---|---|---|
| `NEXT_PUBLIC_SUPABASE_URL` | Endereço do projeto Supabase. O código atual tem um valor padrão; configure esta variável ao usar outro projeto. | URL pública do serviço, mas revela qual projeto é usado. |
| `NEXT_PUBLIC_SUPABASE_ANON_KEY` | Chave pública `anon`/publishable usada pelo cliente do navegador. O código atual tem um valor padrão. | Não é uma chave administrativa; é exposta ao navegador por definição. Configure políticas RLS e permissões mínimas no Supabase. |
| `SUPABASE_SERVICE_ROLE_KEY` | Não é usada pelo app atual. | **Segredo administrativo. Nunca adicione ao frontend, ao `.env.example` ou a este repositório.** |

Nunca use uma `service_role` como `NEXT_PUBLIC_SUPABASE_ANON_KEY`. Uma chave pública não substitui políticas de acesso seguras no banco.

## Acesso administrativo e deploy (não são configuração do app)

| Credencial | Para que serve | Regra |
|---|---|---|
| `RUNPOD_API_KEY` | Ferramentas administrativas podem usá-la para consultar ou gerenciar recursos da conta Runpod. O app de geração não a usa: ele chama diretamente o endpoint HTTP do ComfyUI/Fooocus. | **Segredo de alto impacto.** Guarde apenas no gerenciador de credenciais da ferramenta que realmente precise dela; nunca no site, navegador ou repositório. |
| `VERCEL_OIDC_TOKEN` | Token temporário gerenciado pelo CLI para autenticar operações locais/de deploy. | Não é variável da aplicação. Mantenha local e fora do Git. |
| Token GitHub / Personal Access Token | Autentica Git/CLI para operações na conta ou nos repositórios. | Não é variável da aplicação. Nunca o publique; use o armazenamento seguro do CLI/gerenciador de credenciais. |
| `GEMINI_API_KEY` | Aparece num exemplo de ambiente na raiz, mas não encontrei uso dessa variável no código web atual. | Não é requisito conhecido para este app. Não publique uma chave real; remova-a do ambiente futuro se nenhum código passar a usá-la. |

## Regras práticas

- Para desenvolvimento local, copie `web/.env.example` para `web/.env.local` e preencha apenas o que o backend escolhido realmente exige. O `.env.local` é ignorado pelo Git.
- Na Vercel, cadastre variáveis no painel do projeto. Não coloque tokens de Runpod, Vercel ou GitHub em variáveis `NEXT_PUBLIC_*`.
- Versione apenas `.env.example` com valores fictícios e comentários. Nunca use nele valores copiados do ambiente local.
- Não compartilhe credenciais em chats, screenshots, relatórios ou issues. Remover uma chave de um commit posterior não apaga sua exposição do histórico; se uma chave real for publicada, revogue/rotacione-a no serviço correspondente.
- Antes de tornar geração pública, proteja a rota contra chamadas não autorizadas e gastos inesperados de GPU; guardar uma chave no servidor, por si só, não cria autenticação para os usuários do app.

Veja também [`web/.env.example`](web/.env.example) para a lista copiável de variáveis, sem segredos.
