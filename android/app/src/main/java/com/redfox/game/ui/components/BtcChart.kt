package com.redfox.game.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.redfox.game.data.remote.websocket.BtcTrade
import com.redfox.game.domain.model.RoundPhase
import com.redfox.game.ui.theme.AccentGold
import com.redfox.game.ui.theme.ChartGrid
import com.redfox.game.ui.theme.ChartLine
import com.redfox.game.ui.theme.DarkSurface
import com.redfox.game.ui.theme.DownZoneTransparent
import com.redfox.game.ui.theme.PoolDown
import com.redfox.game.ui.theme.PoolUp
import com.redfox.game.ui.theme.TextPrimary
import com.redfox.game.ui.theme.TextSecondary
import com.redfox.game.ui.theme.UpZoneTransparent
import java.text.DecimalFormat

@Composable
fun BtcChart(
    trades: List<BtcTrade>,
    startPrice: Double? = null,
    phase: RoundPhase = RoundPhase.BETTING,
    modifier: Modifier = Modifier,
    labelWaiting: String = "Ожидание данных...",
    labelBtcLive: String = "BTC Live",
    labelStart: String = "START"
) {
    if (trades.isEmpty()) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text(labelWaiting, color = TextSecondary, fontSize = 14.sp)
        }
        return
    }

    val priceFormat = DecimalFormat("#,##0.00")
    val latestPrice = trades.last().price

    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val rightPadding = 80.dp.toPx()  // Место для шкалы цен
            val topPadding = 8.dp.toPx()
            val bottomPadding = 8.dp.toPx()

            val chartWidth = size.width - rightPadding
            val chartHeight = size.height - topPadding - bottomPadding

            if (chartWidth <= 0 || chartHeight <= 0) return@Canvas

            val prices = trades.map { it.price }
            val minPrice = prices.min()
            val maxPrice = prices.max()
            val priceRange = maxPrice - minPrice
            val padding = if (priceRange > 0) priceRange * 0.1 else maxPrice * 0.001
            val yMin = minPrice - padding
            val yMax = maxPrice + padding
            val yRange = yMax - yMin

            if (yRange <= 0) return@Canvas

            // Функция: цена → Y-координата
            fun priceToY(price: Double): Float {
                return topPadding + chartHeight * (1 - ((price - yMin) / yRange)).toFloat()
            }

            // Функция: индекс → X-координата
            fun indexToX(index: Int): Float {
                return if (trades.size <= 1) chartWidth / 2
                else chartWidth * index.toFloat() / (trades.size - 1)
            }

            // --- Сетка ---
            drawGrid(chartWidth, topPadding, chartHeight, yMin, yMax, priceFormat, ::priceToY)

            // --- Зоны UP/DOWN (градиенты) ---
            if (trades.size >= 2) {
                val pricePath = Path()
                pricePath.moveTo(indexToX(0), priceToY(trades[0].price))
                for (i in 1 until trades.size) {
                    pricePath.lineTo(indexToX(i), priceToY(trades[i].price))
                }

                // Зелёная зона UP (сверху до линии)
                val upPath = Path().apply {
                    moveTo(indexToX(0), topPadding)
                    lineTo(indexToX(0), priceToY(trades[0].price))
                    for (i in 1 until trades.size) {
                        lineTo(indexToX(i), priceToY(trades[i].price))
                    }
                    lineTo(indexToX(trades.size - 1), topPadding)
                    close()
                }
                drawPath(
                    path = upPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(UpZoneTransparent, Color.Transparent),
                        startY = topPadding,
                        endY = priceToY(latestPrice)
                    )
                )

                // Красная зона DOWN (снизу до линии)
                val downPath = Path().apply {
                    moveTo(indexToX(0), priceToY(trades[0].price))
                    for (i in 1 until trades.size) {
                        lineTo(indexToX(i), priceToY(trades[i].price))
                    }
                    lineTo(indexToX(trades.size - 1), topPadding + chartHeight)
                    lineTo(indexToX(0), topPadding + chartHeight)
                    close()
                }
                drawPath(
                    path = downPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.Transparent, DownZoneTransparent),
                        startY = priceToY(latestPrice),
                        endY = topPadding + chartHeight
                    )
                )

                // --- Линия цены ---
                drawPath(
                    path = pricePath,
                    color = ChartLine,
                    style = Stroke(width = 2.dp.toPx())
                )
            }

            // --- Линия START (пунктирная жёлтая) ---
            if (startPrice != null && (phase == RoundPhase.ACTIVE || phase == RoundPhase.CALCULATING)) {
                val startY = priceToY(startPrice)
                drawLine(
                    color = AccentGold,
                    start = Offset(0f, startY),
                    end = Offset(chartWidth, startY),
                    strokeWidth = 1.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f))
                )

                // Метка START слева
                drawContext.canvas.nativeCanvas.drawText(
                    labelStart,
                    4.dp.toPx(),
                    startY - 4.dp.toPx(),
                    android.graphics.Paint().apply {
                        color = AccentGold.toArgb()
                        textSize = 10.sp.toPx()
                        isAntiAlias = true
                    }
                )
            }

            // --- Линия FINISH (вертикальная справа) ---
            if (phase == RoundPhase.ACTIVE) {
                val finishX = chartWidth - 2.dp.toPx()
                drawLine(
                    color = AccentGold,
                    start = Offset(finishX, topPadding),
                    end = Offset(finishX, topPadding + chartHeight),
                    strokeWidth = 2.dp.toPx()
                )
                // Флажок
                val flagPath = Path().apply {
                    moveTo(finishX, topPadding)
                    lineTo(finishX + 12.dp.toPx(), topPadding + 8.dp.toPx())
                    lineTo(finishX, topPadding + 16.dp.toPx())
                    close()
                }
                drawPath(flagPath, color = AccentGold)
            }

            // --- Иконка BTC на текущей точке ---
            if (trades.isNotEmpty()) {
                val lastX = indexToX(trades.size - 1)
                val lastY = priceToY(latestPrice)
                val radius = 10.dp.toPx()

                // Внешний круг
                drawCircle(
                    color = ChartLine,
                    radius = radius,
                    center = Offset(lastX, lastY)
                )
                // Внутренний круг
                drawCircle(
                    color = DarkSurface,
                    radius = radius - 2.dp.toPx(),
                    center = Offset(lastX, lastY)
                )
                // Символ ₿
                drawContext.canvas.nativeCanvas.drawText(
                    "₿",
                    lastX - 5.dp.toPx(),
                    lastY + 5.dp.toPx(),
                    android.graphics.Paint().apply {
                        color = AccentGold.toArgb()
                        textSize = 12.sp.toPx()
                        isAntiAlias = true
                        isFakeBoldText = true
                        textAlign = android.graphics.Paint.Align.CENTER
                    }
                )
            }

            // --- Шкала цен справа ---
            drawPriceScale(chartWidth, topPadding, chartHeight, yMin, yMax, priceFormat, ::priceToY)
        }

        // --- Плашка «BTC Live $XX,XXX.XX» ---
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = 86.dp, top = 4.dp)
                .background(DarkSurface.copy(alpha = 0.9f), RoundedCornerShape(4.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = "$labelBtcLive $${priceFormat.format(latestPrice)}",
                color = ChartLine,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private fun DrawScope.drawGrid(
    chartWidth: Float,
    topPadding: Float,
    chartHeight: Float,
    yMin: Double,
    yMax: Double,
    priceFormat: DecimalFormat,
    priceToY: (Double) -> Float
) {
    val gridLines = 5
    val yRange = yMax - yMin
    for (i in 0..gridLines) {
        val price = yMin + yRange * i / gridLines
        val y = priceToY(price)
        drawLine(
            color = ChartGrid,
            start = Offset(0f, y),
            end = Offset(chartWidth, y),
            strokeWidth = 0.5f
        )
    }
}

private fun DrawScope.drawPriceScale(
    chartWidth: Float,
    topPadding: Float,
    chartHeight: Float,
    yMin: Double,
    yMax: Double,
    priceFormat: DecimalFormat,
    priceToY: (Double) -> Float
) {
    val gridLines = 5
    val yRange = yMax - yMin
    val paint = android.graphics.Paint().apply {
        color = TextTertiary.toArgb()
        textSize = 10.sp.toPx()
        isAntiAlias = true
    }

    for (i in 0..gridLines) {
        val price = yMin + yRange * i / gridLines
        val y = priceToY(price)
        drawContext.canvas.nativeCanvas.drawText(
            priceFormat.format(price),
            chartWidth + 4.dp.toPx(),
            y + 4.dp.toPx(),
            paint
        )
    }
}
