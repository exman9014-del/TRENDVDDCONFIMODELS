package com.example.data

import android.util.Log
import com.example.model.TrendPost
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class SupabaseRealtimeClient(
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    private val TAG = "SupabaseRealtime"
    private var webSocket: WebSocket? = null
    private var heartbeatJob: Job? = null
    private var isConnected = false

    var onBidInserted: ((auctionId: String, email: String, amount: Double) -> Unit)? = null
    var onPostInserted: ((TrendPost) -> Unit)? = null
    var onConnectionStateChanged: ((Boolean) -> Unit)? = null

    private val client = OkHttpClient.Builder()
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .connectTimeout(10, TimeUnit.SECONDS)
        .build()

    fun connect() {
        if (isConnected) return

        val wsUrl = "${SupabaseClient.SUPABASE_URL.replace("https://", "wss://")}/realtime/v1/websocket?apikey=${SupabaseClient.SUPABASE_ANON_KEY}&vsn=1.0.0"
        val request = Request.Builder().url(wsUrl).build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d(TAG, "Supabase Realtime connected!")
                isConnected = true
                onConnectionStateChanged?.invoke(true)

                // Join bids channel
                val joinBids = JSONObject().apply {
                    put("topic", "realtime:public:bids")
                    put("event", "phx_join")
                    put("payload", JSONObject().apply {
                        put("config", JSONObject().apply {
                            put("postgres_changes", org.json.JSONArray().apply {
                                put(JSONObject().apply {
                                    put("event", "*")
                                    put("schema", "public")
                                    put("table", "bids")
                                })
                            })
                        })
                    })
                    put("ref", "bids_sub")
                }
                webSocket.send(joinBids.toString())

                // Join posts channel
                val joinPosts = JSONObject().apply {
                    put("topic", "realtime:public:posts")
                    put("event", "phx_join")
                    put("payload", JSONObject().apply {
                        put("config", JSONObject().apply {
                            put("postgres_changes", org.json.JSONArray().apply {
                                put(JSONObject().apply {
                                    put("event", "*")
                                    put("schema", "public")
                                    put("table", "posts")
                                })
                            })
                        })
                    })
                    put("ref", "posts_sub")
                }
                webSocket.send(joinPosts.toString())

                // Start heartbeat
                startHeartbeat(webSocket)
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                try {
                    val root = JSONObject(text)
                    val event = root.optString("event")
                    val payload = root.optJSONObject("payload")

                    if (event == "postgres_changes" && payload != null) {
                        val data = payload.optJSONObject("data")
                        val record = data?.optJSONObject("record")
                        val table = data?.optString("table")

                        if (table == "bids" && record != null) {
                            val auctionId = record.optString("auction_id", "banner_home")
                            val email = record.optString("email", "anunciante@web.com")
                            val amount = record.optDouble("amount", 0.0)
                            if (amount > 0) {
                                Log.d(TAG, "Realtime bid received: $auctionId, $email, $amount")
                                onBidInserted?.invoke(auctionId, email, amount)
                            }
                        } else if (table == "posts" && record != null) {
                            val id = record.optString("id", System.currentTimeMillis().toString())
                            val author = record.optString("author_name", "criador")
                            val caption = record.optString("caption", "")
                            val likes = record.optInt("likes_count", 0)

                            val newPost = TrendPost(
                                id = id,
                                authorName = author,
                                authorHandle = "@${author.lowercase().replace(" ", "")}",
                                caption = caption,
                                likesCount = likes,
                                timeAgo = "Agora"
                            )
                            Log.d(TAG, "Realtime post received: ${newPost.authorName}")
                            onPostInserted?.invoke(newPost)
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing realtime message: ${e.message}")
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.w(TAG, "Supabase Realtime failure: ${t.message}")
                isConnected = false
                onConnectionStateChanged?.invoke(false)
                heartbeatJob?.cancel()
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.d(TAG, "Supabase Realtime closed: $reason")
                isConnected = false
                onConnectionStateChanged?.invoke(false)
                heartbeatJob?.cancel()
            }
        })
    }

    private fun startHeartbeat(ws: WebSocket) {
        heartbeatJob?.cancel()
        heartbeatJob = scope.launch {
            var ref = 1
            while (isActive && isConnected) {
                delay(25000)
                try {
                    val hb = JSONObject().apply {
                        put("topic", "phoenix")
                        put("event", "heartbeat")
                        put("payload", JSONObject())
                        put("ref", "hb_${ref++}")
                    }
                    ws.send(hb.toString())
                } catch (e: Exception) {
                    Log.w(TAG, "Heartbeat failed: ${e.message}")
                }
            }
        }
    }

    fun disconnect() {
        heartbeatJob?.cancel()
        webSocket?.close(1000, "App closed")
        isConnected = false
    }
}
