package com.redfox.game.data.remote.websocket

import com.redfox.game.domain.model.BetDirection
import com.redfox.game.domain.model.Round
import com.redfox.game.domain.model.RoundPhase
import com.redfox.game.util.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GameStateSocket @Inject constructor(
    private val okHttpClient: OkHttpClient
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _currentRound = MutableStateFlow(Round())
    val currentRound: StateFlow<Round> = _currentRound.asStateFlow()

    private val _timer = MutableStateFlow(0)
    val timer: StateFlow<Int> = _timer.asStateFlow()

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private var webSocket: WebSocket? = null
    private var shouldReconnect = true

    fun connect(serverUrl: String) {
        shouldReconnect = true
        val url = "$serverUrl/ws/game"
        val request = Request.Builder().url(url).build()

        webSocket = okHttpClient.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                _isConnected.value = true
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                try {
                    val json = JSONObject(text)
                    val type = json.optString("type", "")

                    when (type) {
                        "round_state" -> {
                            val phase = when (json.getString("phase")) {
                                "BETTING" -> RoundPhase.BETTING
                                "ACTIVE" -> RoundPhase.ACTIVE
                                else -> RoundPhase.CALCULATING
                            }
                            val result = json.optString("result", "").let {
                                when (it) {
                                    "UP" -> BetDirection.UP
                                    "DOWN" -> BetDirection.DOWN
                                    else -> null
                                }
                            }
                            _currentRound.value = Round(
                                id = json.getLong("round_id").toInt(),
                                phase = phase,
                                startPrice = json.optDouble("start_price").takeIf { !it.isNaN() },
                                poolUp = json.getDouble("pool_up"),
                                poolDown = json.getDouble("pool_down"),
                                payoutUp = json.getDouble("payout_up"),
                                payoutDown = json.getDouble("payout_down"),
                                result = result,
                                timerSeconds = json.getInt("timer")
                            )
                            _timer.value = json.getInt("timer")
                        }
                    }
                } catch (_: Exception) { }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                _isConnected.value = false
                if (shouldReconnect) {
                    scope.launch {
                        delay(3000)
                        if (shouldReconnect) connect(serverUrl)
                    }
                }
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                _isConnected.value = false
            }
        })
    }

    fun disconnect() {
        shouldReconnect = false
        webSocket?.close(1000, "Closed by client")
        webSocket = null
        _isConnected.value = false
    }

    fun destroy() {
        disconnect()
        scope.cancel()
    }
}
