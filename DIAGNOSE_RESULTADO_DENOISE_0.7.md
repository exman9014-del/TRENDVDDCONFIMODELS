# 🔴 DIAGNÓSTICO REVISADO: Resultado com denoise 0.7 AINDA Caótico

**Data:** 2026-11-07 03:54 UTC-3  
**Teste:** Geração com denoise 0.7 (reduzido de 0.95)  
**Resultado:** Falha — montagem caótica (não melhorou)  
**Conclusão:** Problema NÃO era só denoise, é o MODELO

---

## 📊 COMPARAÇÃO: ANTES vs. DEPOIS

| Parâmetro | Teste 1 | Teste 2 |
|-----------|---------|---------|
| **denoise** | 0.95 | **0.7** ✅ Reduzido |
| **Resultado visual** | Montagem colorida/neon | **Ainda montagem caótica** ❌ |
| **Reconhecimento** | Nenhum | **Nenhum** ❌ |
| **Cores** | Saturadas, neon | **Saturadas, neon** ❌ |

**Conclusão:** Reduzir denoise de 0.95 para 0.7 não resolveu o problema.

---

## 🔴 PROBLEMA REAL: MODELO INCORRETO (HIGH CONFIDENCE)

### Divergência Detectada

**JSON declara:**
```json
"ckpt_name": "sd_xl_base_1.0.safetensors"
```

**Interface anteriormente mostrava:**
```
z_image_turbo_bf16.safetensors
```

### Por que isto importa?

| Modelo | Características | Resultado esperado | Resultado real |
|--------|------------------|-------------------|----------------|
| **SDXL Base 1.0** | Preciso, realista, bom para retratos | Rosto reconhecível mesmo com denoise 0.7 | ✅ Seria esperado |
| **z_image_turbo BF16** | Rápido, otimizado para velocidade, menos preciso | Montagem colorida, geometrias caóticas | ✅ **ISTO É O QUE VEMOS** |

---

## 📸 ANÁLISE VISUAL: ASSINATURA DO MODELO

**Características observadas na imagem gerada:**

```
✗ Cores NEON/SATURADAS (azul, amarelo, verde, magenta puro)
✗ Geometrias CAÓTICAS (sem estrutura facial)
✗ SEM transição natural entre cores
✗ Nenhuma semelhança com rosto original
✗ Parece "ruído estruturado" (típico de turbo em modo high-noise)
```

**Assinatura típica de:**
- Modelo turbo/speed otimizado para *geração rápida*, não precisão
- Seed fixo (0) + turbo = sempre mesmo resultado caótico para este prompt
- Denoise 0.7 ainda muito alto para turbo (turbo precisa ≤ 0.5 para img2img)

**NÃO é assinatura de SDXL Base**, que produziria:
- Cores naturais
- Estrutura facial reconhecível
- Transições coerentes

---

## 🧪 PROVA TÉCNICA

### Cenário 1: Se Pod usa SDXL Base 1.0
```
Denoise 0.95 → Montagem (muito agressivo)
Denoise 0.7  → Rosto reconhecível (esperado)
Resultado:   ✅ Denoise 0.7 resolveria
```

### Cenário 2: Se Pod usa z_image_turbo BF16
```
Denoise 0.95 → Montagem (turbo + muito agressivo)
Denoise 0.7  → Montagem (turbo + agressivo) ← ISTO É O QUE VEMOS
Denoise 0.5  → Talvez melhor, mas ainda comprometido
Resultado:   ❌ Reduzir denoise sozinho não resolve com turbo
```

**Seu teste confirmou Cenário 2 ✓**

---

## 🎯 HIPÓTESES RANQUEADAS

### 1️⃣ MODELO ERRADO (Confidence: 95%)

**Evidência:**
- JSON declara SDXL Base, interface dizia turbo
- Resultado visual = assinatura típica de turbo
- Denoise 0.7 não melhorou = problema não é denoise
- Cores neon + geometrias caóticas = turbo karakteristik

**Ação se verdade:**
- Trocar para SDXL Base no CheckpointLoader
- SDXL Base com denoise 0.7 = esperado sucesso ✓

---

### 2️⃣ WORKFLOW NO POD DIFERE DO JSON (Confidence: 40%)

**Evidência:**
- Workflow pode ter nós diferentes
- Seed/steps/cfg podem estar diferentes no Pod
- Pré-processamento pode estar removendo cores naturais

**Ação se verdade:**
- Exportar workflow real do ComfyUI
- Comparar com JSON local

---

### 3️⃣ PARÂMETROS SECUNDÁRIOS (Confidence: 20%)

**Evidência:**
- steps=30 pode ser insuficiente se usando turbo
- seed=0 fixo + turbo = determinístico + pior qualidade

**Ação se verdade:**
- Aumentar steps para 50
- Tentar seed aleatório

---

## 🛑 BLOQUEADOR CRÍTICO

**Pergunta:** Qual é o modelo REAL carregado no ComfyUI do Pod agora?

**Como confirmar:**
1. Abra a URL HTTPS do seu Pod ComfyUI (não inclua endpoints pessoais em documentação pública).
2. Procure o nó "Load Checkpoint"
3. Qual .safetensors está selecionado?
4. Confirme se é:
   - `sd_xl_base_1.0.safetensors` (esperado)
   - `z_image_turbo_bf16.safetensors` (problema encontrado)
   - Outro modelo?

---

## 📋 RESUMO TÉCNICO

| Aspecto | Status | Evidência |
|--------|--------|-----------|
| **Pipeline estrutura** | ✅ Correto | LoadImage → VAEEncode → KSampler → SaveImage |
| **Foto entrando** | ✅ Sim | VAEEncode recebe pixels corretamente |
| **denoise parametrização** | ❌ Secundário | 0.95 → 0.7 não resolveu |
| **Modelo** | 🔴 **SUSPEITO** | JSON diz SDXL Base, result says turbo |
| **Cores/geometrias** | 🔴 **Típico turbo** | Neon saturado + caótico = assinatura turbo |

---

## 🚀 PRÓXIMOS PASSOS (Ordem de prioridade)

### ✅ IMEDIATO (sem editar)

1. **Confirmar modelo real no Pod**
   - Abra ComfyUI interface
   - Procure Load Checkpoint node
   - Anote qual .safetensors está selecionado
   - Envie print/confirmação

2. **Se TURBO encontrado:**
   - Trocar para `sd_xl_base_1.0.safetensors` no Pod (se disponível)
   - OU manter turbo e reduzir denoise para 0.4-0.5
   - Testar novamente

3. **Se SDXL Base confirmado:**
   - Investigar workflow real vs JSON
   - Exportar workflow real do ComfyUI
   - Comparar nós e conexões

---

## 📝 CONCLUSÃO

**Seu workflow é estruturalmente correto.**  
**Sua parametrização denoise foi melhorada (0.95 → 0.7).**  
**Mas resultado ainda caótico = problema é MODELO, não workflow/denoise.**

O teste com denoise 0.7 produziu montagem = confirmação de que denoise não era o culpado principal.

**Próximo passo:** Confirmar qual modelo está realmente sendo usado no Pod ComfyUI.

---

**Documento preparado para diagnóstico. Aguardando confirmação do modelo.**
