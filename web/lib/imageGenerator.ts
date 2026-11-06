import { Trait } from '../components/CreatePostView';

/**
 * Generates transformed images based on the user's uploaded photo and per-container traits.
 * The LAST container is always the original user photo without any changes.
 * The preceding containers are customized according to their specific active traits.
 */
export async function generateTrendImagesFromUserPhoto(
  userPhotoUrl: string,
  containersCount: number,
  imageTraitsMap: Record<number, Trait[]>,
  postType: 'UGLY_TO_BEAUTIFUL' | 'BEAUTIFUL_TO_UGLY'
): Promise<string[]> {
  return new Promise((resolve) => {
    const img = new Image();
    img.crossOrigin = 'anonymous';

    img.onload = () => {
      const generatedList: string[] = [];
      const width = img.naturalWidth || 600;
      const height = img.naturalHeight || 800;

      for (let i = 0; i < containersCount; i++) {
        // The LAST container is ALWAYS the original uploaded photo!
        if (i === containersCount - 1) {
          generatedList.push(userPhotoUrl);
          continue;
        }

        const canvas = document.createElement('canvas');
        canvas.width = width;
        canvas.height = height;
        const ctx = canvas.getContext('2d');

        if (!ctx) {
          generatedList.push(userPhotoUrl);
          continue;
        }

        const traits = imageTraitsMap[i] || [];
        const activeTraits = traits.filter((t) => t.enabled).map((t) => t.name.toLowerCase());

        const hasBlonde = activeTraits.some((t) => t.includes('loiro') || t.includes('cabelo'));
        const hasGreenEyes = activeTraits.some((t) => t.includes('olhos') || t.includes('verde'));
        const hasBeard = activeTraits.some((t) => t.includes('barba') && !t.includes('sem barba'));
        const isCleanShaven = activeTraits.some((t) => t.includes('sem barba'));
        const hasTattoo = activeTraits.some((t) => t.includes('tatuagem'));
        const isMuscular = activeTraits.some((t) => t.includes('musculoso'));
        const isPale = activeTraits.some((t) => t.includes('pálido') || t.includes('palido'));
        const isSloppy = activeTraits.some((t) => t.includes('desleixada') || t.includes('sujo') || t.includes('rasgada'));

        // Apply filters depending on traits and sequence
        if (postType === 'UGLY_TO_BEAUTIFUL') {
          if (i === 0) {
            // First photo: rougher, before glow-up
            ctx.filter = isPale
              ? 'saturate(0.65) contrast(0.9) brightness(0.92)'
              : 'saturate(0.85) contrast(0.95)';
          } else {
            // Intermediate glow-up
            ctx.filter = 'contrast(1.08) saturate(1.12) brightness(1.03)';
          }
        } else {
          // BEAUTIFUL_TO_UGLY
          if (i === 0) {
            ctx.filter = 'contrast(1.15) saturate(1.2) brightness(1.05)';
          } else {
            ctx.filter = 'contrast(0.92) saturate(0.8) brightness(0.95)';
          }
        }

        // Draw base image
        ctx.drawImage(img, 0, 0, width, height);
        ctx.filter = 'none';

        // 1. Blonde Hair Tint (Upper 35% of the head)
        if (hasBlonde) {
          ctx.save();
          ctx.globalCompositeOperation = 'color';
          const hairGrad = ctx.createLinearGradient(0, 0, 0, height * 0.45);
          hairGrad.addColorStop(0, 'rgba(234, 179, 8, 0.45)'); // Golden amber
          hairGrad.addColorStop(0.6, 'rgba(250, 204, 21, 0.28)');
          hairGrad.addColorStop(1, 'rgba(250, 204, 21, 0)');
          ctx.fillStyle = hairGrad;
          ctx.fillRect(0, 0, width, height * 0.45);

          ctx.globalCompositeOperation = 'soft-light';
          ctx.fillStyle = 'rgba(254, 240, 138, 0.35)';
          ctx.fillRect(0, 0, width, height * 0.4);
          ctx.restore();
        }

        // 2. Green Eyes Glow (Center horizontal strip around 35%-45% height)
        if (hasGreenEyes) {
          ctx.save();
          ctx.globalCompositeOperation = 'soft-light';
          const eyeGrad = ctx.createRadialGradient(
            width * 0.5,
            height * 0.38,
            width * 0.05,
            width * 0.5,
            height * 0.38,
            width * 0.35
          );
          eyeGrad.addColorStop(0, 'rgba(16, 185, 129, 0.55)');
          eyeGrad.addColorStop(1, 'rgba(16, 185, 129, 0)');
          ctx.fillStyle = eyeGrad;
          ctx.fillRect(0, height * 0.25, width, height * 0.25);
          ctx.restore();
        }

        // 3. Beard Shading or Clean Shaven tone (Lower face 50%-75% height)
        if (hasBeard) {
          ctx.save();
          ctx.globalCompositeOperation = 'multiply';
          const beardGrad = ctx.createRadialGradient(
            width * 0.5,
            height * 0.65,
            width * 0.05,
            width * 0.5,
            height * 0.65,
            width * 0.38
          );
          beardGrad.addColorStop(0, 'rgba(67, 40, 24, 0.35)');
          beardGrad.addColorStop(0.8, 'rgba(78, 48, 30, 0.15)');
          beardGrad.addColorStop(1, 'rgba(0, 0, 0, 0)');
          ctx.fillStyle = beardGrad;
          ctx.fillRect(0, height * 0.45, width, height * 0.4);
          ctx.restore();
        } else if (isCleanShaven) {
          ctx.save();
          ctx.globalCompositeOperation = 'screen';
          ctx.fillStyle = 'rgba(255, 230, 215, 0.12)';
          ctx.fillRect(width * 0.3, height * 0.52, width * 0.4, height * 0.25);
          ctx.restore();
        }

        // 4. Tattoo hint on neck/shoulder
        if (hasTattoo) {
          ctx.save();
          ctx.globalCompositeOperation = 'multiply';
          ctx.fillStyle = 'rgba(30, 41, 59, 0.25)';
          ctx.beginPath();
          ctx.arc(width * 0.25, height * 0.8, width * 0.12, 0, Math.PI * 2);
          ctx.fill();
          ctx.restore();
        }

        // 5. Aesthetic vignette / border enhancement
        ctx.save();
        const vignette = ctx.createRadialGradient(
          width / 2,
          height / 2,
          width * 0.4,
          width / 2,
          height / 2,
          width * 0.75
        );
        vignette.addColorStop(0, 'rgba(0,0,0,0)');
        vignette.addColorStop(1, 'rgba(0,0,0,0.22)');
        ctx.fillStyle = vignette;
        ctx.fillRect(0, 0, width, height);
        ctx.restore();

        try {
          const dataUrl = canvas.toDataURL('image/jpeg', 0.92);
          generatedList.push(dataUrl);
        } catch (e) {
          generatedList.push(userPhotoUrl);
        }
      }

      resolve(generatedList);
    };

    img.onerror = () => {
      // Fallback
      const fallbacks = [
        '/img_result_1.png',
        '/img_result_2.png',
        '/img_result_3.png',
        userPhotoUrl || '/img_result_4.png',
      ];
      while (fallbacks.length < containersCount) {
        fallbacks.splice(fallbacks.length - 1, 0, '/img_result_2.png');
      }
      resolve(fallbacks);
    };

    img.src = userPhotoUrl;
  });
}
