# 📌 Checkpoint: Exportação de Workflow - Em Progresso

**Data:** 2026-11-06 22:01 UTC-3  
**Status:** ⏳ Aguardando Agent de Browser  
**Agent ID:** d369c49f-746e-449a-86ae-f16d7739a986  

---

## 🎯 Objetivo desta Fase

Obter dados críticos para funcionar a integração ComfyUI:
1. ✅ URL real do ComfyUI (porta 8188)
2. ✅ Workflow JSON em formato API

**Sem esses dados, não podemos testar a integração localmente.**

---

## 📋 O que já foi feito

### Código Implementado ✅

| Arquivo | Linhas | Função | Status |
|---------|--------|--------|--------|
| `web/app/api/generate-image-comfyui/route.ts` | 450+ | Backend ComfyUI com polling | 🟢 Criado (placeholder) |
| `web/lib/generation-backend.ts` | 45 | Seleção dinâmica backend | 🟢 Criado (funcional) |
| `web/app/page.tsx` | ~600 | Frontend roteador dinâmico | 🟡 Modificado (aguarda config) |
| `web/components/ResultsView.tsx` | ~50 | Renderização de output | 🟡 Modificado (aceita comfyui) |

### Variáveis de Ambiente Preparadas ✅

```env
# Necessário para ComfyUI
COMFYUI_API_URL=         # ⏳ Aguardando URL (agent de browser)
COMFYUI_API_KEY=         # ⏳ Opcional (agent informará)

# Opcional (controlar backend)
NEXT_PUBLIC_GENERATION_BACKEND=fooocus  # Padrão: Fooocus (preservado)
# Mudar para 'comfyui' quando configurado
```

### Documentação Criada ✅

| Documento | Propósito | Status |
|-----------|-----------|--------|
| `ACOMPANHAMENTO_CUSTOS.md` | Billing do Pod | 🟢 Ativo |
| `PLANO_POS_WORKFLOW.md` | Fases após receber dados | 🟢 Pronto |
| `WORKFLOW_EXPORT_GUIDE.md` | Como exportar (passado) | 📚 Referência |
| `INTEGRATION_STATUS.md` | Status MCP do Pod | 📚 Referência |
| `COMFYUI_INTEGRATION.md` | Setup técnico completo | 📚 Referência |

---

## ⏳ O Que Agent de Browser Está Fazendo

### Tarefa Enviada

```
OBJETIVO: Verificar URL real + Exportar workflow API
RESTRIÇÕES:
- Apenas leitura (sem alterar Pod/configuração)
- Sem execução de geração
- Sem exposição de credenciais
- Sem invenção de dados
```

### Etapas

1. **Acessar Runpod Console** (app.runpod.io)
   - Status: ⏳ Em andamento
   - Esperado: Autenticado automaticamente

2. **Localizar Pod** (pb9too1432d9ip)
   - Status: ⏳ Em andamento
   - Procurar: "comfyui-a40-migration"

3. **Obter URL da Porta 8188**
   - Status: ⏳ Em andamento
   - Esperado: `https://pb9too1432d9ip-8188.runpod.io`

4. **Acessar Interface ComfyUI**
   - Status: ⏳ Bloqueado por URL
   - Testar: Browser acessa interface

5. **Exportar Workflow API**
   - Status: ⏳ Bloqueado por interface
   - Formato: JSON com nós e class_type (não visual)

6. **Validar JSON**
   - Status: ⏳ Bloqueado por export
   - Verificar: Tem LoadImage, prompts, output

7. **Relatar para nós**
   - Status: ⏳ Último passo
   - Retornar: URL + JSON + nós

---

## 💰 Custos em Tempo Real

**Pod ID:** pb9too1432d9ip  
**Status:** RUNNING (sem alterações)  
**GPU:** A40 (50GB)

| Período | Taxa | Custo Acumulado |
|---------|------|-----------------|
| 00:02 - 01:00 UTC | $0.59/hr | ~$0.02 |
| 01:00 - 02:00 UTC | $0.59/hr | ~$0.04 |
| 22:01 UTC-3 (agora) | $0.59/hr | ~$0.04+ |

**Projeção:**
- Se terminar em 10 min: +$0.10 = $0.14 total
- Se terminar em 30 min: +$0.30 = $0.34 total
- Se levar 1 hora: +$0.59 = $0.63 total

**Nota:** Só custos de estar RUNNING (sem geração)

---

## 🔍 Dados Esperados do Agent

### URL
```
Tipo: https://pb9too1432d9ip-8188.runpod.io
Uso: Adicionar a COMFYUI_API_URL no .env.local
Teste: Deve aceitar POST /prompt
```

### Workflow JSON
```json
{
  "1": {
    "class_type": "LoadImage",
    "inputs": {
      "image": "input.png"
    }
  },
  "2": {
    "class_type": "CLIPTextEncode (Positive)",
    "inputs": {
      "text": "POSITIVE_PROMPT",
      "clip": ["3", 0]
    }
  },
  "3": {
    "class_type": "CLIPTextEncode (Negative)",
    "inputs": {
      "text": "NEGATIVE_PROMPT",
      "clip": ["4", 0]
    }
  },
  "4": {
    "class_type": "KSampler",
    "inputs": {
      "seed": 12345,
      "steps": 30,
      "cfg": 7.5,
      "positive": ["2", 0],
      "negative": ["3", 0],
      "latent_image": ["1", 0]
    }
  },
  "5": {
    "class_type": "SaveImage",
    "inputs": {
      "images": ["4", 0],
      "filename_prefix": "output"
    }
  }
}
```

**Validação esperada:**
- [ ] JSON válido (sem parse error)
- [ ] Tem nó LoadImage (ou CheckpointLoaderSimple)
- [ ] Tem nós CLIPTextEncode x2 (positive + negative)
- [ ] Tem nó KSampler (ou DPMSampler, etc)
- [ ] Tem nó SaveImage (ou PreviewImage)
- [ ] Nós conectados corretamente
- [ ] Marcadores "POSITIVE_PROMPT" e "NEGATIVE_PROMPT" identificáveis
- [ ] Modelo nomeado (ex: "sd_xl_base_1.0.safetensors")

---

## 📊 Próximas Fases (Quando Agent Terminar)

### Fase 1: Configuração Local (~5 min)
```bash
# 1. Criar arquivo de config
mkdir -p web/config
# Salvar workflow em web/config/comfyui-workflow-api.json

# 2. Atualizar .env.local
COMFYUI_API_URL=https://pb9too1432d9ip-8188.runpod.io
COMFYUI_API_KEY=         # Se necessário

# 3. Atualizar route.ts
# - Importar workflow real
# - Verificar nós de entrada/saída

# 4. Validar build
npm run build
```

### Fase 2: Teste Controlado (~10 min)
```bash
# 1. Iniciar dev
npm run dev

# 2. Teste básico (sem geração)
curl -X GET http://localhost:3000/api/system/stats \
  -H "Authorization: Bearer COMFYUI_API_KEY"

# 3. Teste com geração (1 imagem pequena)
# - Upload foto
# - Gerar com ComfyUI
# - Monitorar custo + tempo

# 4. Validar output
# - Imagem retornada?
# - Dimensions corretas?
# - Sem erros?
```

### Fase 3: Decisão (~5 min)
- [ ] Tudo funciona: Parar Pod ou deixar rodando?
- [ ] Algo falha: Ajustar e retry
- [ ] Falta dados: Pedir informação específica

---

## ✅ Confirmações (Neste Checkpoint)

- ✅ Pod **não foi iniciado** por nós (já estava RUNNING)
- ✅ **Nenhuma alteração** em Pod, volumes, modelos
- ✅ **Nenhuma geração** foi executada
- ✅ **Apenas leitura + exportação** (não-destrutivo)
- ✅ **Código funcional** (awaiting workflow JSON)
- ✅ **Fooocus preservado** intacto (`web/app/api/generate-image/route.ts`)
- ✅ **Custo documentado** e monitorado
- ✅ **Sem commit** (conforme pedido)
- ✅ **Sem deploy** (conforme pedido)

---

## 📞 Como Prosseguir

### Quando Agent Terminar

1. **Receberemos:**
   - ✅ URL real do ComfyUI
   - ✅ Workflow JSON
   - ✅ Status de acesso

2. **Executaremos:**
   - Fase 1: Config local (5 min)
   - Fase 2: Teste (10 min)
   - Fase 3: Decisão (parar Pod?)

3. **Você decidirá:**
   - Parar Pod (economizar: ~$0.59/hr)
   - Deixar rodando (teste rápido depois)
   - Próximas ações

---

## 🚨 Se Agent Falhar

**Possíveis bloqueios:**

1. **URL não acessível**
   - Solução: Aguardar DNS propagar (1-5 min) ou tentar IP local
   - Fallback: Dados de Pod (já temos da consulta MCP)

2. **Interface ComfyUI não carrega**
   - Solução: Verificar Pod status
   - Fallback: Usar API REST diretamente (sem UI)

3. **Workflow não exportável**
   - Solução: Procurar alternativa (API query, arquivo, etc)
   - Fallback: Usar workflow padrão + descrever entrada/saída

4. **Credenciais requeridas**
   - Solução: Informar qual tipo (token, API key, etc)
   - Fallback: Usar credenciais do Runpod panel (se público)

**Se qualquer bloqueio ocorrer:**
- Agent relatará exatamente o que falhou
- Você receberá dados bloqueadores específicos
- Prosseguiremos com alternativa ou plano B

---

## 📌 Status Atual

| Item | Status | Próximo |
|------|--------|---------|
| Backend Código | 🟢 Pronto | Usar workflow real |
| Frontend Roteador | 🟢 Pronto | Usar endpoint real |
| Variáveis Env | 🟡 Template | Preencher URL |
| Workflow JSON | ⏳ Exportando | Salvar + validar |
| Arquivo Config | ⏳ Bloqueado | Criar pós-export |
| Teste Local | ⏳ Bloqueado | Executar Fase 2 |
| Relatório Final | ⏳ Bloqueado | Confirmações Fase 3 |

---

**Atualizado:** 2026-11-06 22:01 UTC-3  
**Agent:** d369c49f-746e-449a-86ae-f16d7739a986 (⏳ RUNNING)  
**Próxima notificação:** Quando agent terminar
