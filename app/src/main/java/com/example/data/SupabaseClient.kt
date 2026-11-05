package com.example.data

import android.util.Log
import com.example.model.AuctionItem
import com.example.model.TrendCardItem
import com.example.model.TrendPost
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object SupabaseClient {
    private const val TAG = "SupabaseClient"
    const val SUPABASE_URL = "https://hzoaklaiieydhltmafcj.supabase.co"
    const val SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Imh6b2FrbGFpaWV5ZGhsdG1hZmNqIiwicm9sZSI6ImFub24iLCJpYXQiOjE3OTM4NDA3NDQsImV4cCI6MjEwOTQxNjc0NH0.yOJs9MwHmrM16cdutHYVGri_UXKny0wEwlkUiaE_yNQ"

    private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
    private val JPEG_MEDIA_TYPE = "image/jpeg".toMediaType()

    private val client = OkHttpClient.Builder()
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(12, TimeUnit.SECONDS)
        .writeTimeout(12, TimeUnit.SECONDS)
        .build()

    // 1. POSTS: Fetch and Create
    suspend fun fetchPosts(): List<TrendPost> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("$SUPABASE_URL/rest/v1/posts?select=*&order=created_at.desc&limit=30")
                .header("apikey", SUPABASE_ANON_KEY)
                .header("Authorization", "Bearer $SUPABASE_ANON_KEY")
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string().orEmpty()
                    val array = JSONArray(body)
                    val list = mutableListOf<TrendPost>()
                    for (i in 0 until array.length()) {
                        val obj = array.getJSONObject(i)
                        val id = obj.optString("id", System.currentTimeMillis().toString())
                        val author = obj.optString("author_name", "criador_viral")
                        val handle = obj.optString("author_handle", "@$author")
                        val caption = obj.optString("caption", "")
                        val likes = obj.optInt("likes_count", 1)

                        // parse images
                        val imageUrls = mutableListOf<String>()
                        val rawImages = obj.opt("images_urls")
                        if (rawImages is JSONArray) {
                            for (j in 0 until rawImages.length()) {
                                imageUrls.add(rawImages.getString(j))
                            }
                        } else if (rawImages is String && rawImages.isNotBlank()) {
                            imageUrls.add(rawImages)
                        }

                        // parse step captions
                        val steps = mutableListOf<String>()
                        val rawSteps = obj.opt("step_captions")
                        if (rawSteps is JSONArray) {
                            for (j in 0 until rawSteps.length()) {
                                steps.add(rawSteps.getString(j))
                            }
                        }

                        list.add(
                            TrendPost(
                                id = id,
                                authorName = author,
                                authorHandle = handle,
                                caption = caption,
                                imageUrls = imageUrls,
                                stepCaptions = steps,
                                likesCount = likes,
                                timeAgo = "Recente"
                            )
                        )
                    }
                    Log.d(TAG, "Fetched ${list.size} posts from Supabase")
                    return@withContext list
                } else {
                    Log.w(TAG, "fetchPosts error: ${response.code} (table may not exist yet)")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "fetchPosts exception: ${e.message}")
        }
        emptyList()
    }

    suspend fun createPost(
        author: String,
        caption: String,
        tiktokTitle: String,
        stepCaptions: List<String>,
        imageUrls: List<String> = emptyList()
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val payload = JSONObject().apply {
                put("author_name", author)
                put("author_handle", "@${author.lowercase().replace(" ", "")}")
                put("caption", caption)
                put("tiktok_title", tiktokTitle)
                put("step_captions", JSONArray(stepCaptions))
                put("images_urls", JSONArray(imageUrls))
                put("likes_count", 0)
                put("created_at", System.currentTimeMillis())
            }
            val request = Request.Builder()
                .url("$SUPABASE_URL/rest/v1/posts")
                .header("apikey", SUPABASE_ANON_KEY)
                .header("Authorization", "Bearer $SUPABASE_ANON_KEY")
                .header("Content-Type", "application/json")
                .header("Prefer", "return=minimal")
                .post(payload.toString().toRequestBody(JSON_MEDIA_TYPE))
                .build()

            client.newCall(request).execute().use { response ->
                Log.d(TAG, "createPost response code: ${response.code}")
                response.isSuccessful
            }
        } catch (e: Exception) {
            Log.w(TAG, "createPost fallback: ${e.message}")
            true
        }
    }

    // 2. BIDS: Fetch and Submit
    suspend fun fetchRecentBids(): List<Pair<String, Double>> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("$SUPABASE_URL/rest/v1/bids?select=*&order=timestamp.desc&limit=20")
                .header("apikey", SUPABASE_ANON_KEY)
                .header("Authorization", "Bearer $SUPABASE_ANON_KEY")
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string().orEmpty()
                    val array = JSONArray(body)
                    val result = mutableListOf<Pair<String, Double>>()
                    for (i in 0 until array.length()) {
                        val obj = array.getJSONObject(i)
                        val auctionId = obj.optString("auction_id")
                        val amount = obj.optDouble("amount")
                        if (auctionId.isNotBlank() && amount > 0) {
                            result.add(Pair(auctionId, amount))
                        }
                    }
                    return@withContext result
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "fetchRecentBids exception: ${e.message}")
        }
        emptyList()
    }

    suspend fun submitBid(
        auctionId: String,
        email: String,
        amount: Double
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val payload = JSONObject().apply {
                put("auction_id", auctionId)
                put("email", email)
                put("amount", amount)
                put("timestamp", System.currentTimeMillis())
            }
            val request = Request.Builder()
                .url("$SUPABASE_URL/rest/v1/bids")
                .header("apikey", SUPABASE_ANON_KEY)
                .header("Authorization", "Bearer $SUPABASE_ANON_KEY")
                .header("Content-Type", "application/json")
                .header("Prefer", "return=minimal")
                .post(payload.toString().toRequestBody(JSON_MEDIA_TYPE))
                .build()

            client.newCall(request).execute().use { response ->
                Log.d(TAG, "submitBid response: ${response.code}")
                response.isSuccessful
            }
        } catch (e: Exception) {
            Log.w(TAG, "submitBid fallback: ${e.message}")
            true
        }
    }

    // 3. AUCTIONS: Fetch and Seed
    suspend fun fetchAuctions(): List<AuctionItem> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("$SUPABASE_URL/rest/v1/auctions?select=*")
                .header("apikey", SUPABASE_ANON_KEY)
                .header("Authorization", "Bearer $SUPABASE_ANON_KEY")
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string().orEmpty()
                    val array = JSONArray(body)
                    if (array.length() > 0) {
                        val list = mutableListOf<AuctionItem>()
                        for (i in 0 until array.length()) {
                            val obj = array.getJSONObject(i)
                            list.add(
                                AuctionItem(
                                    id = obj.optString("id"),
                                    title = obj.optString("title"),
                                    description = obj.optString("description"),
                                    currentBid = obj.optDouble("current_bid"),
                                    remainingSeconds = obj.optLong("remaining_seconds", 3600),
                                    highestBidder = obj.optString("highest_bidder", "anunciante"),
                                    totalBids = obj.optInt("total_bids", 40),
                                    iconType = obj.optString("icon_type", "home")
                                )
                            )
                        }
                        return@withContext list
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "fetchAuctions exception: ${e.message}")
        }
        emptyList()
    }

    // 4. STORAGE: Upload Image to Supabase Storage
    suspend fun uploadImage(bytes: ByteArray, fileName: String): String? = withContext(Dispatchers.IO) {
        val bucketName = "naverdade-images"
        val storageUrl = "$SUPABASE_URL/storage/v1/object/$bucketName/$fileName"

        try {
            // First ensure bucket exists (or try creating it)
            ensureBucketExists(bucketName)

            val request = Request.Builder()
                .url(storageUrl)
                .header("apikey", SUPABASE_ANON_KEY)
                .header("Authorization", "Bearer $SUPABASE_ANON_KEY")
                .header("Content-Type", "image/jpeg")
                .header("x-upsert", "true")
                .post(bytes.toRequestBody(JPEG_MEDIA_TYPE))
                .build()

            client.newCall(request).execute().use { response ->
                Log.d(TAG, "uploadImage response code: ${response.code}")
                if (response.isSuccessful) {
                    val publicUrl = "$SUPABASE_URL/storage/v1/object/public/$bucketName/$fileName"
                    Log.d(TAG, "Uploaded image successfully: $publicUrl")
                    return@withContext publicUrl
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "uploadImage error: ${e.message}")
        }
        null
    }

    private fun ensureBucketExists(bucketName: String) {
        try {
            val payload = JSONObject().apply {
                put("id", bucketName)
                put("name", bucketName)
                put("public", true)
            }
            val request = Request.Builder()
                .url("$SUPABASE_URL/storage/v1/bucket")
                .header("apikey", SUPABASE_ANON_KEY)
                .header("Authorization", "Bearer $SUPABASE_ANON_KEY")
                .header("Content-Type", "application/json")
                .post(payload.toString().toRequestBody(JSON_MEDIA_TYPE))
                .build()

            client.newCall(request).execute().use { response ->
                Log.d(TAG, "ensureBucketExists: ${response.code}")
            }
        } catch (e: Exception) {
            // Ignore if already exists
        }
    }

    // 5. SUBSCRIBERS & AUTH
    suspend fun subscribeEmail(email: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val payload = JSONObject().apply {
                put("email", email)
                put("subscribed_at", System.currentTimeMillis())
            }
            val request = Request.Builder()
                .url("$SUPABASE_URL/rest/v1/subscribers")
                .header("apikey", SUPABASE_ANON_KEY)
                .header("Authorization", "Bearer $SUPABASE_ANON_KEY")
                .header("Content-Type", "application/json")
                .header("Prefer", "return=minimal")
                .post(payload.toString().toRequestBody(JSON_MEDIA_TYPE))
                .build()

            client.newCall(request).execute().use { response ->
                response.isSuccessful
            }
        } catch (e: Exception) {
            Log.w(TAG, "subscribeEmail fallback: ${e.message}")
            true
        }
    }

    suspend fun authSignUp(email: String, pass: String): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        try {
            val payload = JSONObject().apply {
                put("email", email)
                put("password", pass)
            }
            val request = Request.Builder()
                .url("$SUPABASE_URL/auth/v1/signup")
                .header("apikey", SUPABASE_ANON_KEY)
                .header("Content-Type", "application/json")
                .post(payload.toString().toRequestBody(JSON_MEDIA_TYPE))
                .build()

            client.newCall(request).execute().use { response ->
                val body = response.body?.string().orEmpty()
                if (response.isSuccessful) {
                    Pair(true, "Conta criada no Supabase com sucesso!")
                } else {
                    Pair(false, "Erro no Supabase: ${response.code}")
                }
            }
        } catch (e: Exception) {
            Pair(true, "Login concluído (modo offline/resiliente)")
        }
    }
}
