# 🚀 GUIA DE TESTE - INTEGRAÇÃO COMFYUI

**Iniciado:** 2026-11-07 00:47 UTC-3  
**Modo:** Teste Local (npm run dev)  

---

## ✅ SERVIDOR INICIADO

O servidor Next.js foi iniciado em background:
```
npm run dev
```

**Aguardando inicialização...**

---

## 📝 INSTRUÇÕES DE TESTE

### FASE 1: Acesso Básico (5 min)

1. **Abra seu navegador:**
   ```
   http://localhost:3000
   ```

2. **Verifique a página:**
   - Interface deve carregar normalmente
   - Botões visíveis
   - Sem erros de conexão

3. **Monitore console (F12):**
   - Abra Developer Tools (F12)
   - Vá para aba "Console"
   - Procure por erros em vermelho
   - Se houver: anote a mensagem

4. **Verificar:**
   - [ ] Página carrega sem erros
   - [ ] Interface funciona
   - [ ] Nenhuma mensagem de erro crítico

---

### FASE 2: Teste com Geração (10-15 min)

**Se Fase 1 passou:**

1. **Prepare uma foto pequena (~500 KB)**
   - JPEG, PNG ou WebP
   - Tamanho mínimo: 200x200px
   - Tamanho máximo: 3 MB

2. **Clique em "Upload Image"**
   - Selecione a foto

3. **Escolha transformação:**
   - "Transformar para Beleza" OU
   - "Transformar para Menos Produzida"

4. **(Opcional) Adicione traços:**
   - Ex: "cabelo curto", "óculos", etc.
   - Máximo 12 traços, 80 caracteres cada

5. **Clique em "Gerar Imagem"**
   - Verifique que usa **ComfyUI** (não Fooocus)
   - Se houver seletor, escolha ComfyUI

6. **Aguarde resultado:**
   - Pode levar 30-60 segundos
   - Monitore console (F12) para progresso
   - Network tab (F12) mostra requisições

7. **Resultado esperado:**
   - [ ] Imagem retornada (1024x1024)
   - [ ] Qualidade aceitável
   - [ ] Sem erros no console

---

### FASE 3: Teste Fooocus (Regressão - 10 min)

**Se Fase 2 passou:**

1. **Verifique se Fooocus ainda funciona**
   - Teste novamente com mesma foto
   - Selecione "Fooocus" (se houver seletor)
   - Gerar e validar resultado

2. **Validar:**
   - [ ] Fooocus retorna imagem
   - [ ] Sem erros
   - [ ] Ambos backends funcionam

---

## 🔧 RESOLVER PROBLEMAS

### Se página não carregar
```
ERR_REFUSED_STREAM / CONNECTION_REFUSED
```
→ Servidor ainda iniciando, aguarde 10-15s
→ Tente F5 (refresh)

### Se houver erro "Cannot find module"
```
Error: Cannot find module 'X'
```
→ Dependência faltando
→ Execute: npm install
→ Tente novamente

### Se erro no ComfyUI
```
Error: Configure COMFYUI_API_URL
```
→ Variável de ambiente não carregou
→ Verifique web/.env.local
→ Reinicie: npm run dev

### Se timeout
```
Timeout waiting for ComfyUI
```
→ Pod pode estar lento
→ Tente novamente
→ Se persistir: Pod pode ter caído

### Se imagem não gerada
```
Error: Failed to fetch image
```
→ Verificar se Pod está RUNNING
→ Verificar logs do ComfyUI
→ Tentar foto menor (~200KB)

---

## 📊 MONITORAR PERFORMANCE

**Console do Navegador (F12):**
```
✓ Sem erros em vermelho
✓ Warnings são OK (amarelo)
✓ Info/Debug é OK (cinza)
```

**Network Tab (F12):**
```
✓ POST /api/generate-image-comfyui → 200 OK
✓ Tempo resposta: 30-60 segundos (geração)
✓ Tamanho resposta: ~50-500 KB (imagem base64)
```

**Terminal (npm run dev):**
```
✓ Sem errors críticos
✓ Avisos de build OK
✓ Server rodando em http://localhost:3000
```

---

## 📝 RELATO DE RESULTADO

Quando terminar testes, anote:

1. **Fase 1 (Acesso):**
   - [ ] Página carregou?
   - [ ] Erros no console?

2. **Fase 2 (ComfyUI):**
   - [ ] Geração iniciou?
   - [ ] Tempo de espera?
   - [ ] Imagem retornou?
   - [ ] Qualidade OK?
   - [ ] Erros?

3. **Fase 3 (Fooocus):**
   - [ ] Fooocus ainda funciona?
   - [ ] Ambos backends OK?

4. **Custos:**
   - [ ] Quanto tempo rodou?
   - [ ] Quantas gerações?

---

## ⏱️ CRONOGRAMA

```
00:47 - Servidor iniciado
00:52 - Fase 1 (acesso) completa
01:02 - Fase 2 (geração) completa
01:12 - Fase 3 (regressão) completa
01:17 - Teste pronto
```

---

## 💾 PRÓXIMOS PASSOS

**Se tudo funcionar:**
1. Parar servidor (Ctrl+C)
2. Parar Pod (economizar custos)
3. Documentar resultado
4. Pronto para commit (próxima sessão)

**Se algo falhar:**
1. Eu ajudo a diagnosticar
2. Ajustamos configuração
3. Tentamos novamente

---

## 🎯 COMECE!

Abra seu navegador em:
```
http://localhost:3000
```

Avisa quando conseguir acessar! 🚀
