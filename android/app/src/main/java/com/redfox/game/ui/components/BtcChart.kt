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
import androidx.compose.ui.geometry.Size
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
import com.redfox.game.ui.theme.PoolDown
import com.redfox.game.ui.theme.PoolUp
import com.redfox.game.ui.theme.TextSecondary
import com.redfox.game.ui.theme.TextTertiary
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
            var minPrice = prices.min()
            var maxPrice = prices.max()

            // Гарантируем, что startPrice попадает в видимый диапазон Y
            if (startPrice != null) {
                if (startPrice < minPrice) minPrice = startPrice
                if (startPrice > maxPrice) maxPrice = startPrice
            }

            val priceRange = maxPrice - minPrice
            // Увеличенный padding 25% для плавного визуального восприятия
            val padding = if (priceRange > 0) priceRange * 0.25 else maxPrice * 0.001
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

            // --- Зоны UP/DOWN (сплошная заливка) ---
            // В фазе ACTIVE: зоны привязаны к горизонтальной линии startPrice
            //   Зелёная = от startPrice ВВЕРХ (до верхнего края графика)
            //   Красная = от startPrice ВНИЗ (до нижнего края графика)
            // В фазе BETTING: зоны делятся пополам от текущей цены (50/50)
            val isActivePhase = phase == RoundPhase.ACTIVE || phase == RoundPhase.CALCULATING
            val dividerY = if (isActivePhase && startPrice != null) {
                priceToY(startPrice)
            } else {
                // BETTING: делим пополам от текущей цены
                priceToY(latestPrice)
            }

            // Зелёная зона UP — от верхнего края графика до линии-разделителя
            drawRect(
                color = PoolUp.copy(alpha = 0.65f),
                topLeft = Offset(0f, topPadding),
                size = Size(
                    chartWidth,
                    (dividerY - topPadding).coerceAtLeast(0f)
                )
            )

            // Красная зона DOWN — от линии-разделителя до нижнего края графика
            drawRect(
                color = PoolDown.copy(alpha = 0.65f),
                topLeft = Offset(0f, dividerY),
                size = Size(
                    chartWidth,
                    (topPadding + chartHeight - dividerY).coerceAtLeast(0f)
                )
            )

            // --- Линия цены ---
            if (trades.size >= 2) {
                val pricePath = Path()
                pricePath.moveTo(indexToX(0), priceToY(trades[0].price))
                for (i in 1 until trades.size) {
                    pricePath.lineTo(indexToX(i), priceToY(trades[i].price))
                }
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
