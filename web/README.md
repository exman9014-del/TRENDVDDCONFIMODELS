# Na Verdade - Versão Web (Next.js 14)

Versão web completa do aplicativo **Na Verdade** com design mobile-first e responsivo, conectada ao Supabase e pronta para deploy na Vercel.

## 🚀 Telas Implementadas

1. **Início (Landing)**:
   - Indicadores ao vivo de usuários online, visualizações e novas entradas.
   - Hero com chamada viral e foto de destaque.
   - Carrossel automático das fotos da trend que desliza sozinho.
   - Seção "O que está bombando na internet agora" (músicas, desafios, nichos).
   - Banner de leilão ao vivo integrado com lances em tempo real.
   - Inscrição rápida por e-mail para receber trends em primeira mão.

2. **Criar Post**:
   - Alternância de fluxo: **Feio → Bonito** (verde) e **Bonito → Feio** (vermelho).
   - Upload de foto com visualizador principal e 4 miniaturas em grade.
   - Seletor de traços de beleza (+ verde) e feiura (+ vermelho) com toggle instantâneo.
   - Adição de traços personalizados.
   - 4 campos de legenda com botão de auto-geração por IA.
   - Banner de leilão em miniatura.
   - Botão de ação "Gerar com IA".

3. **Resultados**:
   - Carrossel interativo das 4 imagens geradas com legendas sobrepostas.
   - Título otimizado para TikTok com botão de cópia com 1 clique.
   - Banner de leilão integrado com contador regressivo.
   - Campo para postagem automática futura via e-mail.
   - Botão para download das 4 imagens em alta qualidade.

4. **Leilão de Espaço**:
   - Indicadores globais em tempo real (12.4k visitas, 3.2k posts criados).
   - 87 anunciantes com fotos dos participantes.
   - 4 banners publicitários reais:
     - `Banner Home` (Página inicial)
     - `Banner Criar Post` (Tela de criação)
     - `Banner Resultados` (Após geração com IA)
     - `Banner Feed` (Intercalado entre posts no feed)
   - Sistema de lances rápidos sem necessidade de login (apenas e-mail).

5. **Feed TikTok**:
   - Feed vertical estilo TikTok com publicações de 4 imagens.
   - Linha de stories com status "AO VIVO".
   - Curtidas, comentários e compartilhamento.
   - Banner de leilão integrado no feed.

6. **Perfil & Autenticação (Opcional)**:
   - Login e cadastro opcionais via Supabase Auth.
   - Estatísticas de alcance e lances.

---

## 🛠️ Tecnologias

- **Next.js 14** (App Router)
- **React 18**
- **Tailwind CSS** com paleta verde esmeralda (`#00A86B`, `#046A4E`, `#10B981`)
- **Lucide React** para ícones modernos
- **@supabase/supabase-js** conectado ao projeto Supabase configurado

---

## ⚡ Como Rodar Localmente

```bash
cd web
npm install
npm run dev
```

Acesse: `http://localhost:3000`

---

## ☁️ Deploy na Vercel

### Opção 1: Diretório raiz
Basta importar o repositório na Vercel. O arquivo `vercel.json` na raiz já direciona para `web`.

### Opção 2: Root Directory na Vercel
Ao importar no painel da Vercel, defina **Root Directory** como `web`.
Framework Preset: **Next.js**.
