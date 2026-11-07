import { readFile } from 'node:fs/promises';
import path from 'node:path';
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

type WorkflowNode = {
  class_type: string;
  inputs: Record<string, unknown>;
};

type ComfyUIWorkflow = Record<string, WorkflowNode>;

type ComfyUIImage = {
  filename?: unknown;
  subfolder?: unknown;
  type?: unknown;
};

type ComfyUIHistoryEntry = {
  outputs?: Record<string, { images?: ComfyUIImage[] }>;
  status?: {
    status_str?: unknown;
    completed?: unknown;
    messages?: unknown;
  };
};

function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === 'object' && value !== null && !Array.isArray(value);
}

function isTraitList(value: unknown): value is string[] {
  return (
    Array.isArray(value) &&
    value.length <= 12 &&
    value.every((trait) => typeof trait === 'string' && trait.length <= 80)
  );
}

async function readJsonBody(request: Request): Promise<unknown> {
  const reader = request.body?.getReader();
  if (!reader) throw new Error('Invalid request body');

  const chunks: Uint8Array[] = [];
  let totalBytes = 0;
  try {
    while (true) {
      const { done, value } = await reader.read();
      if (done) break;
      totalBytes += value.byteLength;
      if (totalBytes > MAX_REQUEST_BYTES) {
        await reader.cancel();
        throw new RangeError('Request body too large');
      }
      chunks.push(value);
    }
  } finally {
    reader.releaseLock();
  }

  const content = new Uint8Array(totalBytes);
  let offset = 0;
  for (const chunk of chunks) {
    content.set(chunk, offset);
    offset += chunk.byteLength;
  }
  const result = JSON.parse(new TextDecoder().decode(content));
  // Clear chunks from memory
  chunks.length = 0;
  return result;
}

async function loadWorkflow(): Promise<ComfyUIWorkflow> {
  const fromEnvironment = process.env.COMFYUI_WORKFLOW_JSON;
  let serialized: string;

  if (fromEnvironment) {
    serialized = fromEnvironment;
  } else {
    const workflowFile =
      process.env.COMFYUI_WORKFLOW_FILE ?? 'config/comfyui-workflow-api.json';
    serialized = await readFile(path.resolve(process.cwd(), workflowFile), 'utf8');
  }

  const parsed: unknown = JSON.parse(serialized);
  if (!isRecord(parsed) || Object.keys(parsed).length === 0) {
    throw new Error('The ComfyUI workflow must be a non-empty API-format object.');
  }

  for (const [nodeId, node] of Object.entries(parsed)) {
    if (!isRecord(node) || typeof node.class_type !== 'string' || !isRecord(node.inputs)) {
      throw new Error(`Invalid ComfyUI API node: ${nodeId}`);
    }
  }

  return parsed as ComfyUIWorkflow;
}

function prepareWorkflow(
  source: ComfyUIWorkflow,
  positivePrompt: string,
  negativePrompt: string,
  uploadedFilename: string
): { workflow: ComfyUIWorkflow; outputNodeIds: string[] } {
  const workflow = JSON.parse(JSON.stringify(source)) as ComfyUIWorkflow;
  let positivePromptNodes = 0;
  let negativePromptNodes = 0;
  let imageNodes = 0;
  const outputNodeIds: string[] = [];

  for (const [nodeId, node] of Object.entries(workflow)) {
    const inputs = node.inputs;
    if (node.class_type === 'LoadImage' && typeof inputs.image === 'string') {
      inputs.image = uploadedFilename;
      imageNodes += 1;
    }

    for (const [inputName, value] of Object.entries(inputs)) {
      if (value === 'POSITIVE_PROMPT') {
        inputs[inputName] = positivePrompt;
        positivePromptNodes += 1;
      } else if (value === 'NEGATIVE_PROMPT') {
        inputs[inputName] = negativePrompt;
        negativePromptNodes += 1;
      }
    }

    if (node.class_type === 'SaveImage' || node.class_type === 'PreviewImage') {
      outputNodeIds.push(nodeId);
    }
  }

  if (!imageNodes || !positivePromptNodes || !negativePromptNodes || !outputNodeIds.length) {
    throw new Error(
      'Workflow requirements missing: one LoadImage, POSITIVE_PROMPT, NEGATIVE_PROMPT, and SaveImage or PreviewImage.'
    );
  }

  return { workflow, outputNodeIds };
}

function getProviderUrl(value: string): URL | null {
  try {
    const url = new URL(value);
    const isLocalHttp =
      process.env.NODE_ENV !== 'production' &&
      url.protocol === 'http:' &&
      ['localhost', '127.0.0.1', '[::1]'].includes(url.hostname);
    const isRunpodHttps =
      url.protocol === 'https:' &&
      (url.hostname.endsWith('.runpod.io') || url.hostname.endsWith('.runpod.net'));
    if (
      (!isLocalHttp && !isRunpodHttps) ||
      url.username ||
      url.password ||
      url.search ||
      url.hash
    ) {
      return null;
    }
    return new URL(`${url.origin}/`);
  } catch {
    return null;
  }
}

async function readLimitedImage(response: Response): Promise<string | null> {
  if (!response.ok) return null;
  const mimeType = response.headers.get('content-type')?.split(';')[0].trim();
  if (!mimeType || !allowedMimeTypes.has(mimeType)) return null;

  const length = Number(response.headers.get('content-length'));
  if (Number.isFinite(length) && length > MAX_OUTPUT_BYTES) return null;

  try {
    const buffer = await response.arrayBuffer();
    if (buffer.byteLength === 0 || buffer.byteLength > MAX_OUTPUT_BYTES) return null;
    const base64 = Buffer.from(buffer).toString('base64');
    return `data:${mimeType};base64,${base64}`;
  } catch {
    return null;
  }
}

export async function POST(request: Request) {
  const contentLength = Number(request.headers.get('content-length'));
  if (Number.isFinite(contentLength) && contentLength > MAX_REQUEST_BYTES) {
    return NextResponse.json({ error: 'A foto deve ter no máximo 3 MB.' }, { status: 413 });
  }

  const comfyUIUrlValue = process.env.COMFYUI_API_URL;
  if (!comfyUIUrlValue) {
    return NextResponse.json(
      { error: 'Configure COMFYUI_API_URL no ambiente do servidor para habilitar o ComfyUI.' },
      { status: 503 }
    );
  }

  const comfyUIUrl = getProviderUrl(comfyUIUrlValue);
  if (!comfyUIUrl) {
    return NextResponse.json(
      { error: 'COMFYUI_API_URL deve ser uma URL HTTPS do Runpod (ou localhost HTTP em desenvolvimento).' },
      { status: 503 }
    );
  }

  let workflowTemplate: ComfyUIWorkflow;
  try {
    workflowTemplate = await loadWorkflow();
  } catch (error) {
    console.error('ComfyUI workflow configuration is unavailable or invalid:', error);
    return NextResponse.json(
      {
        error:
          'O workflow API do ComfyUI não está configurado. Forneça COMFYUI_WORKFLOW_JSON ou web/config/comfyui-workflow-api.json.',
      },
      { status: 503 }
    );
  }

  let body: unknown;
  try {
    body = await readJsonBody(request);
  } catch (error) {
    const tooLarge = error instanceof RangeError && error.message === 'Request body too large';
    return NextResponse.json(
      { error: tooLarge ? 'A foto deve ter no máximo 3 MB.' : 'Requisição inválida.' },
      { status: tooLarge ? 413 : 400 }
    );
  }

  if (
    !isRecord(body) ||
    typeof body.image !== 'string' ||
    typeof body.postType !== 'string' ||
    !['UGLY_TO_BEAUTIFUL', 'BEAUTIFUL_TO_UGLY'].includes(body.postType) ||
    !isTraitList(body.traits)
  ) {
    return NextResponse.json({ error: 'Dados da imagem ou dos traços inválidos.' }, { status: 400 });
  }

  const imageMatch = body.image.match(
    /^data:(image\/(?:jpeg|png|webp));base64,([A-Za-z0-9+/]+={0,2})$/
  );
  if (!imageMatch || !allowedMimeTypes.has(imageMatch[1])) {
    return NextResponse.json(
      { error: 'Envie uma imagem JPEG, PNG ou WebP válida.' },
      { status: 400 }
    );
  }

  const [, mimeType, imageData] = imageMatch;
  const imageBuffer = Buffer.from(imageData, 'base64');
  if (imageBuffer.byteLength > MAX_IMAGE_BYTES) {
    return NextResponse.json({ error: 'A foto deve ter no máximo 3 MB.' }, { status: 413 });
  }

  const traits = body.traits.map((trait) => trait.trim()).filter(Boolean);
  const direction =
    body.postType === 'UGLY_TO_BEAUTIFUL'
      ? 'Transforme a pessoa em uma versão mais bonita e bem cuidada.'
      : 'Transforme a pessoa em uma versão mais desleixada e menos produzida.';
  const positivePrompt = [
    'Photorealistically edit the supplied input photo and keep the same recognizable person, approximate age, skin tone, pose, and framing.',
    'Do not replace the person with someone else or use stock/example imagery.',
    direction,
    traits.length ? `Apply these visual traits: ${traits.join(', ')}.` : '',
    'Do not add text, frames, collages, or extra people.',
  ]
    .filter(Boolean)
    .join(' ');
  const negativePrompt =
    'text, watermark, frame, collage, extra people, different identity, blurry, low quality';
  const headers: Record<string, string> = {};
  const apiKey = process.env.COMFYUI_API_KEY;
  if (apiKey) headers.Authorization = `Bearer ${apiKey}`;

  try {
    const uploadForm = new FormData();
    uploadForm.append(
      'image',
      new Blob([imageBuffer], { type: mimeType }),
      `upload.${mimeType === 'image/jpeg' ? 'jpg' : mimeType.split('/')[1]}`
    );
    uploadForm.append('type', 'input');
    uploadForm.append('overwrite', 'true');

    const uploadResponse = await fetch(new URL('upload/image', comfyUIUrl), {
      method: 'POST',
      headers,
      body: uploadForm,
      signal: AbortSignal.timeout(submitTimeoutMs),
    });
    if (!uploadResponse.ok) {
      console.error('ComfyUI image upload failed:', { httpStatus: uploadResponse.status });
      return NextResponse.json(
        {
          error:
            uploadResponse.status === 401 || uploadResponse.status === 403
              ? 'O servidor ComfyUI recusou a autenticação. Confira o gateway protegido do Pod.'
              : 'Não foi possível enviar a foto ao ComfyUI.',
        },
        { status: 502 }
      );
    }

    const uploadResult: unknown = await uploadResponse.json();
    if (
      !isRecord(uploadResult) ||
      typeof uploadResult.name !== 'string' ||
      (uploadResult.subfolder !== undefined && typeof uploadResult.subfolder !== 'string')
    ) {
      return NextResponse.json(
        { error: 'O ComfyUI não retornou o nome do arquivo enviado.' },
        { status: 502 }
      );
    }
    const uploadedFilename = uploadResult.subfolder
      ? `${uploadResult.subfolder}/${uploadResult.name}`
      : uploadResult.name;

    let prepared: ReturnType<typeof prepareWorkflow>;
    try {
      prepared = prepareWorkflow(
        workflowTemplate,
        positivePrompt,
        negativePrompt,
        uploadedFilename
      );
    } catch (error) {
      console.error('ComfyUI workflow is missing required input markers:', error);
      return NextResponse.json(
        { error: 'O workflow precisa ter um nó LoadImage e os marcadores de prompt exigidos.' },
        { status: 503 }
      );
    }

    const submitResponse = await fetch(new URL('prompt', comfyUIUrl), {
      method: 'POST',
      headers: { ...headers, 'Content-Type': 'application/json' },
      body: JSON.stringify({ prompt: prepared.workflow }),
      signal: AbortSignal.timeout(submitTimeoutMs),
    });
    if (!submitResponse.ok) {
      console.error('ComfyUI workflow submission failed:', {
        httpStatus: submitResponse.status,
      });
      return NextResponse.json(
        {
          error:
            submitResponse.status === 401 || submitResponse.status === 403
              ? 'O servidor ComfyUI recusou a autenticação. Confira o gateway protegido do Pod.'
              : 'O ComfyUI recusou o workflow. Verifique os nós e modelos configurados nele.',
        },
        { status: 502 }
      );
    }

    const submission: unknown = await submitResponse.json();
    if (!isRecord(submission) || typeof submission.prompt_id !== 'string') {
      return NextResponse.json(
        { error: 'O ComfyUI não retornou um identificador de execução válido.' },
        { status: 502 }
      );
    }
    const promptId = submission.prompt_id;
    const deadline = Date.now() + generationTimeoutMs;

    while (Date.now() < deadline) {
      await new Promise((resolve) => setTimeout(resolve, 1200));
      const historyResponse = await fetch(
        new URL(`history/${encodeURIComponent(promptId)}`, comfyUIUrl),
        { headers, signal: AbortSignal.timeout(queryTimeoutMs) }
      );
      if (!historyResponse.ok) {
        console.error('ComfyUI history request failed:', {
          httpStatus: historyResponse.status,
        });
        return NextResponse.json(
          { error: 'Não foi possível consultar o andamento da geração no ComfyUI.' },
          { status: 502 }
        );
      }

      const history: unknown = await historyResponse.json();
      if (!isRecord(history) || !isRecord(history[promptId])) continue;
      const entry = history[promptId] as ComfyUIHistoryEntry;
      const status = entry.status;
      if (status?.status_str === 'error') {
        console.error('ComfyUI reported a workflow execution error:', {
          promptId,
          messages: status.messages,
        });
        return NextResponse.json(
          { error: 'A execução do workflow falhou no ComfyUI. Confira os nós e modelos do workflow.' },
          { status: 502 }
        );
      }

      if (!status?.completed) continue;

      const outputs = entry.outputs;
      const images = prepared.outputNodeIds.flatMap((nodeId) => outputs?.[nodeId]?.images ?? []);
      const outputImage = images.find(
        (image) =>
          typeof image.filename === 'string' &&
          (image.type === undefined || image.type === 'output' || image.type === 'temp') &&
          (image.subfolder === undefined || typeof image.subfolder === 'string')
      );
      if (!outputImage || typeof outputImage.filename !== 'string') {
        return NextResponse.json(
          { error: 'O workflow terminou, mas não retornou imagem em um nó SaveImage ou PreviewImage.' },
          { status: 502 }
        );
      }

      const viewUrl = new URL('view', comfyUIUrl);
      viewUrl.searchParams.set('filename', outputImage.filename);
      viewUrl.searchParams.set('subfolder', typeof outputImage.subfolder === 'string' ? outputImage.subfolder : '');
      viewUrl.searchParams.set('type', outputImage.type === 'temp' ? 'temp' : 'output');
      const imageResponse = await fetch(viewUrl, {
        headers,
        signal: AbortSignal.timeout(queryTimeoutMs),
      });
      const image = await readLimitedImage(imageResponse);
      if (!image) {
        return NextResponse.json(
          { error: 'O ComfyUI terminou, mas a imagem retornada é inválida ou excede o limite.' },
          { status: 502 }
        );
      }

      console.info('ComfyUI image generation succeeded:', { promptId });
      const response = NextResponse.json({ image });
      
      // Explicitly trigger garbage collection if available (for memory pressure relief)
      if (global.gc) {
        setImmediate(() => global.gc?.());
      }
      
      return response;
    }

    return NextResponse.json(
      { error: 'A geração no servidor GPU demorou mais que o limite da solicitação.' },
      { status: 504 }
    );
  } catch (error) {
    console.error('ComfyUI image generation request failed:', error);
    return NextResponse.json(
      { error: 'Não foi possível conectar ao servidor ComfyUI. Confira o Pod e tente novamente.' },
      { status: 502 }
    );
  }
}
