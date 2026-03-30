package com.redfox.game.data.remote.websocket

import com.redfox.game.util.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
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
class BtcPriceSocket @Inject constructor(
    private val okHttpClient: OkHttpClient
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _trades = MutableSharedFlow<BtcTrade>(replay = 1)
    val trades: SharedFlow<BtcTrade> = _trades.asSharedFlow()

    private val _priceBuffer = MutableStateFlow<List<BtcTrade>>(emptyList())
    val priceBuffer: StateFlow<List<BtcTrade>> = _priceBuffer.asStateFlow()

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val _latestPrice = MutableStateFlow(0.0)
    val latestPrice: StateFlow<Double> = _latestPrice.asStateFlow()

    private val bufferLock = Any()
    private var lastBufferTimestamp = 0L
    private var webSocket: WebSocket? = null
    private var shouldReconnect = true

    fun connect() {
        shouldReconnect = true
        doConnect()
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

    private fun doConnect() {
        val request = Request.Builder()
            .url(Constants.BINANCE_WS_URL)
            .build()

        webSocket = okHttpClient.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                _isConnected.value = true
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                try {
                    val json = JSONObject(text)
                    val price = json.getString("p").toDouble()
                    val timestamp = json.getLong("T")
                    val trade = BtcTrade(price, timestamp)

                    _latestPrice.value = price

                    // Субсэмплирование: добавляем в буфер 1 точку каждые 250мс
                    // Это даёт 240 точек ≈ 60 секунд данных для плавного графика
                    synchronized(bufferLock) {
                        if (timestamp - lastBufferTimestamp >= SAMPLE_INTERVAL_MS) {
                            lastBufferTimestamp = timestamp
                            val current = _priceBuffer.value.toMutableList()
                            current.add(trade)
                            if (current.size > BUFFER_SIZE) {
                                current.removeAt(0)
                            }
                            _priceBuffer.value = current
                        }
                    }

                    scope.launch {
                        _trades.emit(trade)
                    }
                } catch (_: Exception) {
                    // Пропускаем некорректные сообщения
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                _isConnected.value = false
                if (shouldReconnect) {
                    scope.launch {
                        delay(RECONNECT_DELAY_MS)
                        if (shouldReconnect) doConnect()
                    }
                }
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                _isConnected.value = false
                if (shouldReconnect) {
                    scope.launch {
                        delay(RECONNECT_DELAY_MS)
                        if (shouldReconnect) doConnect()
                    }
                }
            }
        })
    }

    companion object {
        const val BUFFER_SIZE = 240
        const val SAMPLE_INTERVAL_MS = 250L
        const val RECONNECT_DELAY_MS = 3000L
    }
}
