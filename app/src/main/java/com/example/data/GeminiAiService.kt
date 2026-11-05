package com.example.data

import android.util.Log
import com.example.BuildConfig
import com.example.model.FeatureTrait
import com.example.model.PostType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
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
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    suspend fun generateTrendCaptions(
        postType: PostType,
        activeTraits: List<FeatureTrait>
    ): GeminiGenerationOutput = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Throwable) {
            ""
        }

        val hasValidKey = apiKey.isNotBlank() &&
                apiKey != "MY_GEMINI_API_KEY" &&
                !apiKey.contains("PLACEHOLDER")

        if (hasValidKey) {
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
                    Crie 4 legendas curtas e 1 título viral para a trend 'Na Verdade'.
                    Tipo de postagem: $typeDescription.
                    Traços de beleza destacados: ${beautyTraits.joinToString(", ")}.
                    Traços de feiura destacados: ${uglyTraits.joinToString(", ")}.
                    
                    A trend funciona assim:
                    - Foto 1: Uma frase irônica ou inicial despretensiosa (ex: 'nem é magra', 'achei que era modelo')
                    - Foto 2: A revelação 'na verdade...' (ex: 'na verdade eu não era loiro', 'na verdade uso 3 filtros')
                    - Foto 3: Piada ou risada (ex: 'desculpa kkkkk', 'desculpaaaaa')
                    - Foto 4: Conclusão cômica ou deboche (ex: 'desculpa kkkkk', 'não me cancela')
                    
                    Responda ESTRITAMENTE em formato JSON com o seguinte formato, sem texto antes ou depois:
                    {
                      "line1": "texto da linha 1 (max 45 caracteres)",
                      "line2": "texto da linha 2 (max 45 caracteres)",
                      "line3": "texto da linha 3 (max 45 caracteres)",
                      "line4": "texto da linha 4 (max 45 caracteres)",
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
                            val l1 = parsed.optString("line1", "nem é magra")
                            val l2 = parsed.optString("line2", "na verdade eu não era loiro")
                            val l3 = parsed.optString("line3", "desculpa kkkkk")
                            val l4 = parsed.optString("line4", "desculpa kkkkk")
                            val title = parsed.optString("tiktok_title", "POV: na verdade... kkkk #NaVerdade #Trend")

                            Log.d(TAG, "Gemini generated successfully: $l1 | $l2 | $l3 | $l4")
                            return@withContext GeminiGenerationOutput(
                                captionLines = listOf(l1, l2, l3, l4),
                                tiktokTitle = title,
                                isRealAi = true
                            )
                        }
                    } else {
                        Log.w(TAG, "Gemini request failed: ${response.code} ${response.message}")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Gemini error: ${e.message}", e)
            }
        } else {
            Log.d(TAG, "No GEMINI_API_KEY provided in BuildConfig, using contextual generator fallback")
        }

        // Contextual dynamic fallback tailored to user traits
        val output = generateContextualCaptions(postType, activeTraits)
        return@withContext GeminiGenerationOutput(
            captionLines = output.first,
            tiktokTitle = output.second,
            isRealAi = false
        )
    }

    private fun generateContextualCaptions(
        postType: PostType,
        traits: List<FeatureTrait>
    ): Pair<List<String>, String> {
        val enabledNames = traits.filter { it.isEnabled }.map { it.name }
        val hasBeard = enabledNames.any { it.contains("barba") }
        val hasBlond = enabledNames.any { it.contains("loiro") }
        val hasMuscle = enabledNames.any { it.contains("musculoso") }
        val hasEyes = enabledNames.any { it.contains("olhos") }

        return if (postType == PostType.UGLY_TO_BEAUTIFUL) {
            when {
                hasBlond -> Pair(
                    listOf("nem é magra", "na verdade eu não era loiro", "desculpa kkkkk", "desculpa kkkkk"),
                    "POV: na verdade eu não era loiro... kkkkk #NaVerdade #TrendViral #fyp"
                )
                hasMuscle -> Pair(
                    listOf("fase do desleixo total", "na verdade comecei a treinar", "olha o glow up", "desculpaaaaa kkkk"),
                    "POV: você duvidou da academia né? Na verdade... 💪🔥 #NaVerdade #GlowUp"
                )
                hasBeard -> Pair(
                    listOf("achou que era menino", "na verdade deixei a barba crescer", "mudou foi tudo", "desculpa kkkkk"),
                    "POV: a barba é a maquiagem do homem na verdade 😂 #NaVerdade #Barba"
                )
                hasEyes -> Pair(
                    listOf("achou que era castanho", "na verdade olhos verdes na luz", "desculpa aí", "desculpa kkkkk"),
                    "POV: na verdade os olhos verdes mudam tudo 👁️✨ #NaVerdade #Viral"
                )
                else -> Pair(
                    listOf("achei que era feio", "mas na verdade esse é meu auge", "desculpaaaaa", "desculpa kkkkk"),
                    "POV: quando o glow up chega sem avisar 😳✨ #NaVerdade #Trend"
                )
            }
        } else {
            Pair(
                listOf("achou que era modelo?", "na verdade era só ângulo e filtro", "barba mal feita total", "desculpa kkkkk"),
                "POV: Bonito pra Feio na verdade kkkkk #Trend #NaVerdade #Realidade"
            )
        }
    }
}
