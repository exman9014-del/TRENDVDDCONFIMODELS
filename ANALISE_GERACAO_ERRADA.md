# 🔍 ANÁLISE TÉCNICA: Por que a geração saiu tão diferente da foto original

**Status:** Somente leitura (sem edições)  
**Data:** 2026-11-07 03:18 UTC-3  
**Workflow:** `web/config/comfyui-workflow-api.json` (cópia local conectada)

---

## 📊 VISUAL OBSERVADO

| Imagem | Descrição |
|--------|-----------|
| **Original** (4/4) | Rosto de mulher real, tons naturais, boa qualidade |
| **Gerada** (1/4 e 2/4) | Montagem colorida/neon, geometrias caóticas, cores saturadas, nada parecido com original |

**Conclusão visual:** Transformação RADICALMENTE diferente — não parece derivar da foto original.

---

## 🔧 PIPELINE DE CONEXÕES (CONFIRMADO CORRETO)

```
LoadImage (1)
    ↓ (pixels)
VAEEncode (9)
    ↓ (latent)
KSampler (5)  ← recebe foto via latent_image
    ↓ (samples)
VAEDecode (7)
    ↓ (images)
SaveImage (8)
```

✅ **Conexões:** Todas as referências de nó estão válidas e conectadas corretamente.  
✅ **Prompts:** Conectados aos mesmos nós (2 e 3).  
✅ **VAE:** Ambos (Encode 9 e Decode 7) usam VAE do checkpoint 4.

**Diagnóstico de conexões:** Não é bloqueio de conexão. A foto ESTÁ entrando no pipeline.

---

## ⚠️ PROBLEMA 1: DENOISE = 0.95 (MUITO ALTO)

### O que é denoise?

Parâmetro do KSampler que controla **quanto da imagem original é preservado vs. quanto é regenerado**:

| Valor | Comportamento | Resultado |
|-------|---------------|-----------| 
| **1.0** | Ignora latent_image completamente | Gera do zero (totalmente novo) |
| **0.95** | Regenera 95% da imagem, preserva 5% | ⚠️ **MUITO AGRESSIVO** |
| **0.7-0.8** | Regenera 70-80%, preserva 20-30% | Transformação forte mas reconhecível |
| **0.5** | Regenera 50%, preserva 50% | Edição moderada, foto claramente reconhecível |
| **0.2-0.3** | Regenera 20-30%, preserva 70-80% | Edição sutil |
| **0.0** | Preserva 100% da imagem | Nenhuma alteração |

### Conexão com resultado observado

**denoise: 0.95** significa:
- 95% da imagem é **regenerada novo** usando o diffusion model
- Apenas 5% da estrutura original é preservada
- Com este valor, um rosto pode virar uma montagem abstrata

**Hipótese:** Se o KSampler regenera 95%, a imagem original fica quase completamente apagada, e o modelo gera nova estrutura com base no prompt + ruído. Cores vibrantes e geometrias caóticas são típicas de **regeneração de alto denoise com prompts genéricos**.

---

## ⚠️ PROBLEMA 2: DIVERGÊNCIA DE MODELO

### Que modelo está sendo usado?

JSON declare:
```json
"ckpt_name": "sd_xl_base_1.0.safetensors"
```

**Observação anterior (na captura de interface):**
- Interface mostrava: `z_image_turbo_bf16.safetensors`
- JSON atual declara: `sd_xl_base_1.0.safetensors`

### Por que isto importa?

| Modelo | Características | Esperado |
|--------|------------------|----------|
| **SDXL Base 1.0** | Modelo de propósito geral, bom em resolução alta (1024+), requer mais passos para detalhe | Rosto mais realista, mas precisa de steps suficientes |
| **z Image Turbo BF16** | Modelo de turbo/speed, otimizado para poucas iterações, pode ser menos preciso em retratos | Potencialmente mais artefatos/distorções |

**Possibilidade:** Se a interface usou um modelo diferente que está otimizado para velocidade (turbo), ele pode gerar resultados mais abstratos com denoise alto.

---

## ⚠️ PROBLEMA 3: OUTROS PARÂMETROS DO SAMPLER

```json
"5": {
  "inputs": {
    "seed": 0,                    // ← Sempre o MESMO seed
    "steps": 30,                  // OK para SDXL, mínimo 20
    "cfg": 7.0,                   // OK, controla força do prompt
    "sampler_name": "euler",      // OK, rápido e estável
    "scheduler": "normal",        // OK
    "denoise": 0.95,              // ⚠️ PROBLEMA CRÍTICO
    "latent_image": ["9", 0]      // ✓ Conectado
  }
}
```

| Parâmetro | Valor | Análise |
|-----------|-------|--------|
| **seed** | 0 (fixo) | Sempre vai gerar o MESMO resultado para o mesmo input. Sem variação, é impossível saber se é erro do modelo ou determinístico. |
| **steps** | 30 | OK, suficiente para SDXL |
| **cfg** | 7.0 | Moderado, seguro |
| **sampler** | euler | Simples e rápido, OK |

---

## 🧠 ANÁLISE CAUSAL: Por que saiu tão diferente?

### Cenário mais provável (HIGH CONFIDENCE):

1. **VAEEncode funcionou:** Foto foi convertida do espaço pixel para latent
2. **KSampler recebeu a foto:** latent_image conectado a nó 9 (correto)
3. **Problema: denoise 0.95 + modelo/seed** 
   - KSampler foi instruído para **regenerar 95% da imagem**
   - Com denoise=0.95 + seed=0, o modelo ignora quase tudo da foto original
   - A saída passa por VAEDecode, que tenta reconstruir pixels
   - Resultado: Geometrias caóticas (artefatos do diffusion) em cores saturadas

### Por que não parece foto original editada?

Transformações de denoise 0.95 frequentemente resultam em:
- **Cores saturadas** (modelo tenta "desenhar novo")
- **Estruturas caóticas** (diffusion ainda tem "ruído" de 95% regeneração)
- **Nenhum traço da foto original** (5% preservação é insuficiente)

---

## 🎯 HIPÓTESES A CONFIRMAR

### Hipótese A: Denoise muito alto
**Evidência:** Montagem colorida típica de regeneração agressiva  
**Como confirmar:** Reduzir denoise para 0.5-0.7 SEM mudar modelo e testar novamente

### Hipótese B: Modelo diferente foi usado
**Evidência:** JSON declara SDXL Base 1.0, mas interface mostrava z_image_turbo  
**Como confirmar:** Verificar qual modelo está REALMENTE carregado no Pod ComfyUI (`/system` endpoint ou workflow live)

### Hipótese C: Seed fixo mascarando problema
**Evidência:** seed=0 (fixo) sempre produz o mesmo resultado  
**Como confirmar:** Se Model B for confirmado, tentar outra foto com seed diferente

---

## 📋 PRÓXIMOS PASSOS RECOMENDADOS (PARA COPILOT ANTERIOR)

**SEM EDITAR OU EXECUTAR NADA AGORA:**

1. **Confirmar modelo:** 
   - Checar na interface ComfyUI qual modelo está realmente carregado
   - Comparar com JSON (`sd_xl_base_1.0.safetensors`)
   - Se divergir, anotar qual é o real

2. **Análise de denoise:**
   - 0.95 é MUITO alto para edição de foto
   - Típico range para edição: 0.5-0.8
   - Colocar em cópia: `"denoise": 0.7` (moderado, preserva 30% original)

3. **Depois testar COM UMA SÓ GERAÇÃO:**
   - Mesmo arquivo JSON com denoise reduzido
   - Mesma foto original
   - Observar se saída é mais parecida com original

---

## ✅ O QUE FOI CONFIRMADO (CORRETO)

| Item | Status | Evidência |
|------|--------|-----------|
| Conexões pipeline | ✅ Correto | Nós conectados, sem referências inválidas |
| VAEEncode input | ✅ Correto | `pixels: ["1", 0]` referencia LoadImage |
| KSampler latent | ✅ Correto | `latent_image: ["9", 0]` referencia VAEEncode |
| VAEDecode + SaveImage | ✅ Correto | Encadeados sem erro |
| Prompts | ✅ Conectados | CLIPTextEncode referencia checkpoint |

---

## ❌ O QUE ESTÁ SUSPEITO (BLOQUEADOR)

| Item | Problema | Impacto |
|------|----------|--------|
| **denoise = 0.95** | Muito alto, regenera 95% | ⚠️ Resultado não reconhecível |
| **Modelo divergente?** | JSON vs. interface mismatch | ⚠️ Pode estar usando modelo errado |
| **seed = 0 fixo** | Sem variação | 🟡 Dificulta debug (sempre mesmo resultado) |

---

## 📝 RESUMO EXECUTIVO

**A pipeline está CORRETAMENTE conectada. A foto ESTÁ entrando no KSampler via VAEEncode.**

**O resultado degradado é causado por:**

1. **denoise = 0.95** (95% regeneração = quase tudo novo) 
   → Típico de parâmetro mal calibrado para "edição de foto"

2. **Possível modelo turbo/speed** (se z_image_turbo estiver sendo usado)
   → Menos preciso que SDXL Base para retratos complexos

3. **Interação denoise + modelo**
   → Denoise muito alto + modelo turbo = resultado caótico

**Próximo passo seguro:** Reduzir denoise na cópia para 0.7 (regenera 70%, preserva 30%) e confirmar modelo real no Pod.

---

**Documento preparado para revisão pelo Copilot anterior. Nenhuma edição ou execução foi feita.**
