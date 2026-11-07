package com.example.data

import android.content.Context
import android.util.Base64
import android.util.Log
import com.example.BuildConfig
import com.example.model.FeatureTrait
import com.example.model.PostType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

data class GeminiGenerationOutput(
    val captionLines: List<String>,
    val tiktokTitle: String,
    val isRealAi: Boolean
)

object GeminiAiService {
    private const val TAG = "GeminiAiService"
    private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private fun getApiKey(): String {
        return try {
            val key = BuildConfig.GEMINI_API_KEY
            if (key.isNotBlank() && key != "MY_GEMINI_API_KEY" && !key.contains("PLACEHOLDER")) {
                key
            } else ""
        } catch (e: Throwable) {
            ""
        }
    }

    suspend fun generateTrendCaptions(
        postType: PostType,
        activeTraits: List<FeatureTrait>,
        linesCount: Int = 4
    ): GeminiGenerationOutput = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (apiKey.isNotBlank()) {
            try {
                val beautyTraits = activeTraits.filter { it.isBeauty && it.isEnabled }.map { it.name }
                val uglyTraits = activeTraits.filter { !it.isBeauty && it.isEnabled }.map { it.name }

                val typeDescription = if (postType == PostType.UGLY_TO_BEAUTIFUL) {
                    "Feio para Bonito (Glow Up surpreendente)"
                } else {
                    "Bonito para Feio (Desilusão cômica)"
                }

                val prompt = """
                    Você é um especialista em trends virais do TikTok e Instagram Reels no Brasil.
                    Crie exatamente $linesCount legendas curtas (uma para cada foto da sequência) e 1 título viral para a trend 'Na Verdade'.
                    Tipo de postagem: $typeDescription.
                    Traços de beleza destacados: ${beautyTraits.joinToString(", ")}.
                    Traços de feiura destacados: ${uglyTraits.joinToString(", ")}.
                    
                    A trend funciona assim:
                    - Foto 1: Uma frase irônica ou inicial despretensiosa (ex: 'nem é magra', 'achei que era modelo')
                    - Foto 2: A revelação 'na verdade...' (ex: 'na verdade eu não era loiro', 'na verdade uso 3 filtros')
                    - Fotos intermediárias: Piada, choque ou risada (ex: 'desculpa kkkkk', 'desculpaaaaa', 'olha esse glow up')
                    - Última foto: Conclusão cômica ou deboche (ex: 'desculpa kkkkk', 'não me cancela')
                    
                    Responda ESTRITAMENTE em formato JSON com o seguinte formato, sem texto antes ou depois:
                    {
                      "lines": [
                        "texto foto 1 (max 45 caracteres)",
                        "texto foto 2 (max 45 caracteres)",
                        ...
                      ],
                      "tiktok_title": "POV viral com hashtags #NaVerdade #TrendViral"
                    }
                """.trimIndent()

                val requestJson = JSONObject().apply {
                    put("contents", JSONArray().apply {
                        put(JSONObject().apply {
                            put("parts", JSONArray().apply {
                                put(JSONObject().apply {
                                    put("text", prompt)
                                })
                            })
                        })
                    })
                    put("generationConfig", JSONObject().apply {
                        put("temperature", 0.7)
                        put("responseMimeType", "application/json")
                    })
                }

                val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey"
                val request = Request.Builder()
                    .url(url)
                    .post(requestJson.toString().toRequestBody(JSON_MEDIA_TYPE))
                    .build()

                httpClient.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        val body = response.body?.string().orEmpty()
                        val root = JSONObject(body)
                        val candidates = root.optJSONArray("candidates")
                        if (candidates != null && candidates.length() > 0) {
                            val candidate = candidates.getJSONObject(0)
                            val parts = candidate.getJSONObject("content").getJSONArray("parts")
                            val rawText = parts.getJSONObject(0).getString("text")

                            val parsed = JSONObject(rawText)
                            val linesArray = parsed.optJSONArray("lines")
                            val lines = mutableListOf<String>()
                            if (linesArray != null) {
                                for (i in 0 until linesArray.length()) {
                                    lines.add(linesArray.getString(i))
                                }
                            }
                            val title = parsed.optString("tiktok_title", "POV: na verdade... kkkk #NaVerdade #Trend")

                            if (lines.isNotEmpty()) {
                                Log.d(TAG, "Gemini generated ${lines.size} captions successfully")
                                return@withContext GeminiGenerationOutput(
                                    captionLines = lines,
                                    tiktokTitle = title,
                                    isRealAi = true
                                )
                            }
                        }
                    } else {
                        Log.w(TAG, "Gemini text request failed: ${response.code} ${response.message}")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Gemini text error: ${e.message}", e)
            }
        }

        // Contextual dynamic fallback
        val output = generateContextualCaptions(postType, activeTraits, linesCount)
        return@withContext GeminiGenerationOutput(
            captionLines = output.first,
            tiktokTitle = output.second,
            isRealAi = false
        )
    }

    /**
     * Generates transformation images with Google Imagen 3 API.
     */
    suspend fun generateTrendImagesPerContainer(
        context: Context,
        postType: PostType,
        perContainerTraits: Map<Int, List<FeatureTrait>>,
        totalCount: Int = 4
    ): List<String> = coroutineScope {
        val apiKey = getApiKey()
        if (apiKey.isBlank()) {
            Log.d(TAG, "No valid GEMINI_API_KEY for image generation, using curated assets")
            return@coroutineScope emptyList()
        }

        val prompts = (0 until totalCount).map { index ->
            val traitsForImage = perContainerTraits[index].orEmpty()
            val beauty = traitsForImage.filter { it.isBeauty && it.isEnabled }.joinToString(", ") { it.name }
            val ugly = traitsForImage.filter { !it.isBeauty && it.isEnabled }.joinToString(", ") { it.name }

            if (postType == PostType.UGLY_TO_BEAUTIFUL) {
                if (index == 0) {
                    "Candid realistic mobile phone photo of a Brazilian young person looking unkempt, casual selfie, slightly unflattering angle, disheveled: $ugly, natural indoor lighting, 4k"
                } else if (index == totalCount - 1) {
                    "Gorgeous glow-up portrait of the same person, handsome stylish look: $beauty, golden hour sunlight, aesthetic outdoor coastal background, high quality portrait"
                } else {
                    "Candid portrait of the same person smiling casually and cheerfully, charming natural expression, realistic mobile portrait: $beauty"
                }
            } else {
                if (index == 0) {
                    "Stunning model portrait of a gorgeous Brazilian person: $beauty, studio lighting, flawless aesthetic, elegant"
                } else if (index == totalCount - 1) {
                    "Hilarious realistic reality-check portrait of the same person looking exhausted and funny, unkempt style: $ugly, candid mobile photo"
                } else {
                    "Candid portrait of the same person taking off sunglasses and smiling wryly, natural daylight: $ugly"
                }
            }
        }

        Log.d(TAG, "Starting parallel Imagen 3 generation for $totalCount photos...")
        val jobs = prompts.mapIndexed { idx, prompt ->
            async { generateSingleImageWithImagen(context, prompt, "img_trend_${idx + 1}_${System.currentTimeMillis()}") }
        }

        val results = jobs.mapNotNull { it.await() }
        Log.d(TAG, "Imagen 3 finished: ${results.size}/$totalCount images generated")
        results
    }

    private suspend fun generateSingleImageWithImagen(
        context: Context,
        prompt: String,
        fileNamePrefix: String
    ): String? = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (apiKey.isBlank()) return@withContext null

        try {
            val url = "https://generativelanguage.googleapis.com/v1beta/models/imagen-3.0-generate-002:predict?key=$apiKey"
            val payload = JSONObject().apply {
                put("instances", JSONArray().apply {
                    put(JSONObject().apply {
                        put("prompt", prompt)
                    })
                })
                put("parameters", JSONObject().apply {
                    put("sampleCount", 1)
                    put("aspectRatio", "3:4")
                    put("outputMimeType", "image/jpeg")
                })
            }

            val request = Request.Builder()
                .url(url)
                .post(payload.toString().toRequestBody(JSON_MEDIA_TYPE))
                .build()

            httpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string().orEmpty()
                    val root = JSONObject(body)
                    val predictions = root.optJSONArray("predictions")
                    if (predictions != null && predictions.length() > 0) {
                        val first = predictions.getJSONObject(0)
                        val base64Data = first.optString("bytesBase64Encoded")
                        if (base64Data.isNotBlank()) {
                            val imageBytes = Base64.decode(base64Data, Base64.DEFAULT)

                            val supabaseUrl = SupabaseClient.uploadImage(imageBytes, "$fileNamePrefix.jpg")
                            if (supabaseUrl != null) {
                                return@withContext supabaseUrl
                            }

                            val cacheFile = File(context.cacheDir, "$fileNamePrefix.jpg")
                            FileOutputStream(cacheFile).use { it.write(imageBytes) }
                            return@withContext cacheFile.toURI().toString()
                        }
                    }
                } else {
                    Log.w(TAG, "Imagen 3 error: ${response.code} ${response.message}")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Imagen 3 generation exception: ${e.message}")
        }
        null
    }

    private fun generateContextualCaptions(
        postType: PostType,
        traits: List<FeatureTrait>,
        linesCount: Int
    ): Pair<List<String>, String> {
        val enabledNames = traits.filter { it.isEnabled }.map { it.name }
        val hasBeard = enabledNames.any { it.contains("barba") }
        val hasBlond = enabledNames.any { it.contains("loiro") }
        val hasMuscle = enabledNames.any { it.contains("musculoso") }

        val baseLines = if (postType == PostType.UGLY_TO_BEAUTIFUL) {
            when {
                hasBlond -> listOf("nem é magra", "na verdade eu não era loiro", "desculpa kkkkk", "desculpa kkkkk")
                hasMuscle -> listOf("fase do desleixo total", "na verdade comecei a treinar", "olha o glow up", "desculpaaaaa kkkk")
                hasBeard -> listOf("achou que era menino", "na verdade deixei a barba crescer", "mudou foi tudo", "desculpa kkkkk")
                else -> listOf("achei que era feio", "mas na verdade esse é meu auge", "desculpaaaaa", "desculpa kkkkk")
            }
        } else {
            listOf("achou que era modelo?", "na verdade era só ângulo e filtro", "barba mal feita total", "desculpa kkkkk")
        }

        // Expand or shrink to linesCount
        val finalLines = ArrayList(baseLines)
        while (finalLines.size < linesCount) {
            finalLines.add("desculpa kkkkk (${finalLines.size + 1})")
        }

        val title = if (postType == PostType.UGLY_TO_BEAUTIFUL) {
            "POV: na verdade eu nunca fui assim... kkkkk #NaVerdade #TrendViral #GlowUp"
        } else {
            "POV: a realidade por trás do filtro na verdade 😂 #NaVerdade #Trend"
        }

        return Pair(finalLines.take(linesCount), title)
    }
}
