import { NextResponse } from 'next/server';
import { POST as generateWithComfyUI } from '../generate-image-comfyui/route';
import { POST as generateWithFooocus } from '../generate-image-fooocus/route';

export const runtime = 'nodejs';
export const maxDuration = 60;

function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === 'object' && value !== null && !Array.isArray(value);
}

export async function POST(request: Request) {
  const backend = process.env.IMAGE_GENERATION_BACKEND ?? 'comfyui';
  const handler =
    backend === 'comfyui'
      ? generateWithComfyUI
      : backend === 'fooocus'
        ? generateWithFooocus
        : null;

  if (!handler) {
    return NextResponse.json(
      { error: 'IMAGE_GENERATION_BACKEND deve ser comfyui ou fooocus.' },
      { status: 503 }
    );
  }

  const response = await handler(request);
  if (!response.ok) return response;

  let result: unknown;
  try {
    result = await response.json();
  } catch {
    return NextResponse.json(
      { error: 'O serviço de geração retornou uma resposta inválida.' },
      { status: 502 }
    );
  }

  if (!isRecord(result) || typeof result.image !== 'string') {
    return NextResponse.json(
      { error: 'O serviço de geração não retornou uma imagem válida.' },
      { status: 502 }
    );
  }

  return NextResponse.json({
    ...result,
    backend,
  });
}
