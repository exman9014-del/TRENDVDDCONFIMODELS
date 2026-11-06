import { NextResponse } from 'next/server';

export const runtime = 'nodejs';
export const maxDuration = 60;

const MAX_IMAGE_BYTES = 3 * 1024 * 1024;
const MAX_REQUEST_BYTES = 4.5 * 1024 * 1024;
const MAX_OUTPUT_BYTES = 20 * 1024 * 1024;
const allowedMimeTypes = new Set(['image/jpeg', 'image/png', 'image/webp']);
const submitTimeoutMs = 12_000;
const queryTimeoutMs = 8_000;
const generationTimeoutMs = 54_000;

type FooocusImageResult = {
  base64?: unknown;
  url?: unknown;
  finish_reason?: unknown;
};

type FooocusJob = {
  job_id?: unknown;
  job_stage?: unknown;
  job_status?: unknown;
  job_result?: unknown;
  error?: unknown;
  detail?: unknown;
  msg?: unknown;
};

function isFooocusJob(value: unknown): value is FooocusJob {
  return typeof value === 'object' && value !== null;
}

function getErrorMessage(job: FooocusJob) {
  for (const value of [job.error, job.detail, job.msg, job.job_status]) {
    if (typeof value === 'string' && value.length > 0) return value.slice(0, 400);
  }
  return 'Fooocus não conseguiu gerar a imagem.';
}

async function fetchFooocusImage(
  result: FooocusImageResult,
  baseUrl: URL
): Promise<{ image: string } | null> {
  if (typeof result.base64 === 'string' && result.base64.length > 0) {
    const data = result.base64.replace(/^data:image\/(?:jpeg|png|webp);base64,/, '');
    if (!/^[A-Za-z0-9+/]+={0,2}$/.test(data)) return null;

    const mimeType = result.base64.match(/^data:(image\/(?:jpeg|png|webp));base64,/)?.[1] ?? 'image/png';
    const outputBytes = Buffer.from(data, 'base64');
    if (outputBytes.byteLength > MAX_OUTPUT_BYTES) return null;

    return { image: `data:${mimeType};base64,${outputBytes.toString('base64')}` };
  }

  if (typeof result.url !== 'string') return null;

  let outputUrl: URL;
  try {
    const returnedUrl = new URL(result.url, baseUrl);
    if (returnedUrl.origin !== baseUrl.origin) return null;
    outputUrl = new URL(`${returnedUrl.pathname}${returnedUrl.search}`, baseUrl);
  } catch {
    return null;
  }

  const response = await fetch(outputUrl, {
    signal: AbortSignal.timeout(queryTimeoutMs),
  });
  if (!response.ok) return null;

  const mimeType = response.headers.get('content-type')?.split(';')[0].trim();
  if (!mimeType || !allowedMimeTypes.has(mimeType)) return null;

  const output = await response.arrayBuffer();
  if (output.byteLength === 0 || output.byteLength > MAX_OUTPUT_BYTES) return null;

  return {
    image: `data:${mimeType};base64,${Buffer.from(output).toString('base64')}`,
  };
}

export async function POST(request: Request) {
  const contentLength = Number(request.headers.get('content-length'));
  if (Number.isFinite(contentLength) && contentLength > MAX_REQUEST_BYTES) {
    return NextResponse.json({ error: 'A foto deve ter no máximo 3 MB.' }, { status: 413 });
  }

  const apiUrl = process.env.FOOOCUS_API_URL;
  const apiKey = process.env.FOOOCUS_API_KEY;
  if (!apiUrl || !apiKey) {
    return NextResponse.json(
      { error: 'A geração Fooocus ainda não está configurada no servidor. Configure a URL e a chave da API do RunPod.' },
      { status: 503 }
    );
  }

  let baseUrl: URL;
  try {
    baseUrl = new URL(apiUrl.endsWith('/') ? apiUrl : `${apiUrl}/`);
    const isLocalHttp =
      baseUrl.protocol === 'http:' &&
      ['localhost', '127.0.0.1', '[::1]'].includes(baseUrl.hostname);
    if (
      (baseUrl.protocol !== 'https:' && !isLocalHttp) ||
      baseUrl.username ||
      baseUrl.password ||
      baseUrl.search ||
      baseUrl.hash
    ) {
      throw new Error('Invalid Fooocus URL');
    }
  } catch {
    return NextResponse.json(
      { error: 'A URL pública do Fooocus está inválida. Configure uma URL HTTPS do RunPod.' },
      { status: 503 }
    );
  }

  let body: { image?: unknown; traits?: unknown; postType?: unknown };
  try {
    body = await request.json();
  } catch {
    return NextResponse.json({ error: 'Requisição inválida.' }, { status: 400 });
  }

  if (
    typeof body.image !== 'string' ||
    typeof body.postType !== 'string' ||
    !['UGLY_TO_BEAUTIFUL', 'BEAUTIFUL_TO_UGLY'].includes(body.postType) ||
    !Array.isArray(body.traits) ||
    body.traits.length > 12 ||
    !body.traits.every((trait) => typeof trait === 'string' && trait.length <= 80)
  ) {
    return NextResponse.json({ error: 'Dados da imagem ou dos traços inválidos.' }, { status: 400 });
  }

  const match = body.image.match(/^data:(image\/(?:jpeg|png|webp));base64,([A-Za-z0-9+/]+={0,2})$/);
  if (!match || !allowedMimeTypes.has(match[1])) {
    return NextResponse.json(
      { error: 'Envie uma imagem JPEG, PNG ou WebP válida.' },
      { status: 400 }
    );
  }

  const [, , imageData] = match;
  const imageSize =
    Math.floor((imageData.length * 3) / 4) -
    (imageData.endsWith('==') ? 2 : imageData.endsWith('=') ? 1 : 0);
  if (imageSize > MAX_IMAGE_BYTES) {
    return NextResponse.json({ error: 'A foto deve ter no máximo 3 MB.' }, { status: 413 });
  }

  const traits = (body.traits as string[]).map((trait) => trait.trim()).filter(Boolean);
  const direction =
    body.postType === 'UGLY_TO_BEAUTIFUL'
      ? 'Transforme a pessoa em uma versão mais bonita e bem cuidada.'
      : 'Transforme a pessoa em uma versão mais desleixada e menos produzida.';
  const prompt = [
    'Create one photorealistic variation based on the provided reference photo.',
    'Keep the same recognizable person, face, approximate age, skin tone, pose, and framing.',
    'Do not replace the person with a different person or use stock/example imagery.',
    direction,
    traits.length > 0
      ? `Apply only these visual traits: ${traits.join(', ')}.`
      : 'Make a subtle transformation consistent with the requested direction.',
    'Do not add text, frames, collages, extra people, or unrelated elements.',
  ].join(' ');

  const headers = {
    'Content-Type': 'application/json',
    'X-API-KEY': apiKey,
  };

  try {
    const generateUrl = new URL('v2/generation/image-prompt', baseUrl);
    const generationResponse = await fetch(generateUrl, {
      method: 'POST',
      headers,
      signal: AbortSignal.timeout(submitTimeoutMs),
      body: JSON.stringify({
        prompt,
        negative_prompt: 'text, watermark, frame, collage, extra people, different identity',
        image_prompts: [
          { cn_img: imageData, cn_stop: 0.9, cn_weight: 1.0, cn_type: 'FaceSwap' },
          { cn_img: imageData, cn_stop: 0.9, cn_weight: 0.6, cn_type: 'ImagePrompt' },
        ],
        performance_selection: 'Speed',
        image_number: 1,
        require_base64: true,
        async_process: true,
      }),
    });

    if (!generationResponse.ok) {
      console.error('Fooocus generation submission failed:', {
        httpStatus: generationResponse.status,
      });
      return NextResponse.json(
        {
          error:
            generationResponse.status === 401 || generationResponse.status === 403
              ? 'O Fooocus recusou a chave configurada. Confira FOOOCUS_API_KEY no servidor.'
              : 'O servidor Fooocus não aceitou a solicitação. Confira se a API está disponível no RunPod.',
        },
        { status: 502 }
      );
    }

    let job: unknown;
    try {
      job = await generationResponse.json();
    } catch {
      return NextResponse.json(
        { error: 'O Fooocus retornou uma resposta inválida ao iniciar a geração.' },
        { status: 502 }
      );
    }

    if (!isFooocusJob(job)) {
      return NextResponse.json(
        { error: 'O Fooocus retornou uma resposta inválida ao iniciar a geração.' },
        { status: 502 }
      );
    }

    const deadline = Date.now() + generationTimeoutMs;
    while (true) {
      const results = Array.isArray(job.job_result) ? job.job_result : [];
      const successfulResult = results.find(
        (item): item is FooocusImageResult =>
          typeof item === 'object' &&
          item !== null &&
          (item as FooocusImageResult).finish_reason !== 'ERROR'
      );

      if (job.job_stage === 'FAILED' || job.job_stage === 'ERROR') {
        console.error('Fooocus image generation failed:', {
          jobStage: job.job_stage,
          jobStatus: job.job_status,
        });
        return NextResponse.json(
          { error: 'O Fooocus não conseguiu gerar a imagem. Tente novamente com outros traços.' },
          { status: 502 }
        );
      }

      if (successfulResult) {
        const generated = await fetchFooocusImage(successfulResult, baseUrl);
        if (!generated) {
          console.error('Fooocus returned an unsupported image result.');
          return NextResponse.json(
            { error: 'O Fooocus concluiu a geração, mas não retornou uma imagem válida.' },
            { status: 502 }
          );
        }

        console.info('Fooocus image generation succeeded:', {
          outputBytes: Math.floor((generated.image.length * 3) / 4),
        });
        return NextResponse.json(generated);
      }

      if (typeof job.job_id !== 'string' || job.job_id.length === 0) {
        console.error('Fooocus response did not include a job ID or image result.');
        return NextResponse.json(
          { error: getErrorMessage(job) },
          { status: 502 }
        );
      }

      if (Date.now() >= deadline) {
        return NextResponse.json(
          { error: 'A geração no servidor GPU está demorando mais que o esperado. Aguarde um pouco e tente novamente.' },
          { status: 504 }
        );
      }

      await new Promise((resolve) => setTimeout(resolve, 1500));
      const queryUrl = new URL('v1/generation/query-job', baseUrl);
      queryUrl.searchParams.set('job_id', job.job_id);
      queryUrl.searchParams.set('require_step_preview', 'false');
      const queryResponse = await fetch(queryUrl, {
        headers: { 'X-API-KEY': apiKey },
        signal: AbortSignal.timeout(queryTimeoutMs),
      });

      if (!queryResponse.ok) {
        console.error('Fooocus job query failed:', { httpStatus: queryResponse.status });
        return NextResponse.json(
          { error: 'Não foi possível consultar o andamento da geração no servidor Fooocus.' },
          { status: 502 }
        );
      }

      job = await queryResponse.json();
      if (!isFooocusJob(job)) {
        return NextResponse.json(
          { error: 'O Fooocus retornou um status de geração inválido.' },
          { status: 502 }
        );
      }
    }
  } catch (error) {
    console.error('Fooocus image generation request failed:', error);
    return NextResponse.json(
      { error: 'Não foi possível conectar ao servidor Fooocus no RunPod. Verifique a instância e tente novamente.' },
      { status: 502 }
    );
  }
}
