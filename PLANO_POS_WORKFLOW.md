# 📋 Plano de Ação - Após Receber URL e Workflow

**Status:** Aguardando resultado do agent de browser  
**Agent ID:** d369c49f-746e-449a-86ae-f16d7739a986  
**Data:** 2026-11-06 22:01 UTC-3

---

## 🎯 O que estamos aguardando

Agent de browser está verificando:

1. ✅ URL real do ComfyUI (porta 8188)
   - Esperado: `https://pb9too1432d9ip-8188.runpod.io` ou IP:porta
   - Será usado em: `COMFYUI_API_URL` no `.env.local`

2. ✅ Workflow JSON em API format
   - Formato: Object com nós numerados (`"1"`, `"2"`, etc.)
   - Cada nó: `class_type` + `inputs`
   - Será salvo em: `web/config/comfyui-workflow-api.json`

---

## ✅ Etapas após receber URL e workflow

### Fase 1: Configuração Local (5 min)

1. **Criar arquivo de config**
   ```bash
   mkdir -p web/config
   # Salvar workflow JSON em web/config/comfyui-workflow-api.json
   # Substituir "POSITIVE_PROMPT" e "NEGATIVE_PROMPT" por marcadores
   ```

2. **Atualizar .env.local**
   ```env
   COMFYUI_API_URL=https://pb9too1432d9ip-8188.runpod.io
   COMFYUI_API_KEY=     # Deixar vazio se não requer
   NEXT_PUBLIC_GENERATION_BACKEND=comfyui
   ```

3. **Atualizar route.ts**
   - Importar workflow real de `web/config/comfyui-workflow-api.json`
   - Substituir `getBaseWorkflow()` com JSON real
   - Ajustar lógica de injeção se necessário

4. **Validar build**
   ```bash
   npm run build
   ```

### Fase 2: Teste Controlado (10 min)

1. **Iniciar dev server**
   ```bash
   npm run dev
   ```

2. **Teste básico (SEM geração)**
   - Abrir http://localhost:3000
   - Verificar que formulário funciona
   - Não executar geração ainda

3. **Teste de conexão API**
   - Verificar que `COMFYUI_API_URL` está correto
   - Fazer request simples: `GET /api/system/stats`
   - Confirmar que ComfyUI responde

4. **Teste com geração (Controlado)**
   - Upload de foto pequena (test.jpg)
   - Gerar com ComfyUI backend
   - Monitorar:
     - Console para erros
     - Custo acumulado (Pod rodando)
     - Tempo de geração

### Fase 3: Otimização (5 min, se necessário)

Se teste falhar:
- Ajustar nó de entrada de imagem (LoadImage vs upload)
- Verificar índice de output (SaveImage)
- Validar nome do modelo

Se teste passar:
- Confirmar que Fooocus ainda funciona
- Documentar fluxo funcional

---

## 📊 Decisões a Fazer

### Quando tivermos workflow + URL funcional:

1. **Configuração de Backend**
   - [ ] Manter padrão: Fooocus (usuário escolhe ComfyUI)
   - [ ] Auto-detect: Tentar ComfyUI, fallback Fooocus
   - [ ] Forçar ComfyUI: Remover Fooocus (não recomendado)

2. **Pós-teste**
   - [ ] Parar Pod (economizar custos)
   - [ ] Deixar Pod rodando (teste rápido depois)
   - [ ] Aguardar sua decisão

3. **Antes de commit**
   - [ ] Documentar workflow (nós, modelos, configuração)
   - [ ] Adicionar guia de setup em README.md
   - [ ] Testar com Fooocus também (regressão)

---

## ⚙️ Checklist Técnico

Quando receber workflow, validar:

- [ ] JSON válido (parse sem erro)
- [ ] Tem nó LoadImage (ou equivalente)
- [ ] Tem nó CLIPTextEncode (ou equivalente) para positive
- [ ] Tem nó CLIPTextEncode (ou equivalente) para negative
- [ ] Tem nó KSampler (ou equivalente) para geração
- [ ] Tem nó SaveImage (ou equivalente) para output
- [ ] Nós referem-se uns aos outros corretamente
- [ ] Modelo está nomeado (ex: "sd_xl_base_1.0.safetensors")
- [ ] Nenhuma credencial/URL assinada no JSON

---

## 📚 Arquivos a Criar/Modificar

| Arquivo | Ação | Motivo |
|---------|------|--------|
| `web/config/comfyui-workflow-api.json` | ✅ Criar | Salvar workflow real |
| `web/.env.local` | ✅ Atualizar | Adicionar COMFYUI_API_URL |
| `web/app/api/generate-image-comfyui/route.ts` | ✅ Modificar | Usar workflow real |
| `web/README.md` | ✅ Atualizar | Documentar ComfyUI |
| `WORKFLOW_CONFIGURATION.md` | ✅ Criar | Documentar nós + modelos |

---

## 💰 Custo Estimado

**Enquanto aguardamos (~30 min):**
- Custo acumulado: ~$0.30 (A40 @ $0.59/hr)

**Se 2 gerações de teste (5 min cada):**
- Consumo extra: ~$0.10 (teste controlado)

**Se deixar rodando mais tempo:**
- A cada 1 hora: +$0.59
- A cada 24 horas: +$14.16

---

## ⏹️ Como Parar Pod (se necessário)

```powershell
# Usar MCP para parar sem deletar
runpod-pod-action -id pb9too1432d9ip -action stop
# Pod vai para EXITED, preserve dados
# GPU liberada, disco continua cobrado (~$0.02/hr)
```

---

## 📞 Próxima Ação

**Agent de browser** está em execução:
- ⏳ Verificando painel Runpod
- ⏳ Copiando URL real
- ⏳ Exportando workflow JSON

Quando terminar:
1. Receberemos URL e workflow
2. Executaremos Fase 1 (config)
3. Executaremos Fase 2 (teste)
4. Você decide: parar Pod ou deixar rodando

---

**Status:** ⏳ Aguardando agent de browser  
**Tempo estimado:** ~5-10 minutos  
**Próxima atualização:** Quando agent enviar resultado
