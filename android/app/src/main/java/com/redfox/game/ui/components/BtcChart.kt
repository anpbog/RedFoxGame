package com.redfox.game.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
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

    // Анимация плавного появления только последнего сегмента линии
    val animatedFraction = remember { androidx.compose.animation.core.Animatable(0f) }
    LaunchedEffect(trades.size) {
        if (trades.size <= 2) {
            animatedFraction.snapTo(1f)
        } else {
            animatedFraction.snapTo(0f)
            animatedFraction.animateTo(1f, animationSpec = androidx.compose.animation.core.tween(durationMillis = 300))
        }
    }
    val latestPrice = trades.last().price

    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(16.dp))) {
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
            // Увеличенный padding 40% для плавного визуального восприятия
            val padding = if (priceRange > 0) priceRange * 0.40 else maxPrice * 0.001
            val yMin = minPrice - padding
            val yMax = maxPrice + padding
            val yRange = yMax - yMin

            if (yRange <= 0) return@Canvas

            // Функция: цена → Y-координата
            fun priceToY(price: Double): Float {
                return topPadding + chartHeight * (1 - ((price - yMin) / yRange)).toFloat()
            }

            // Точка BTC всегда в центре графика по X
            // Слева — история цен, справа — пустое пространство
            // Полное временное окно = 120 сек, последняя точка = середина
            val VISIBLE_HISTORY_MS = 60_000f // 60 сек истории слева от точки
            val lastTs = trades.last().timestamp.toFloat()
            // Окно: от (lastTs - 60 сек) до (lastTs + 60 сек)
            // Последняя точка (lastTs) попадает ровно на chartWidth / 2
            val windowStartTs = lastTs - VISIBLE_HISTORY_MS
            val windowDuration = VISIBLE_HISTORY_MS * 2f // полное окно = 120 сек

            fun tradeToX(trade: BtcTrade): Float {
                return chartWidth * (trade.timestamp.toFloat() - windowStartTs) / windowDuration
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

            // Зелёная зона UP — градиент: прозрачный сверху -> зелёный полупрозрачный снизу
            val upZoneHeight = (dividerY - topPadding).coerceAtLeast(0f)
            if (upZoneHeight > 0f) {
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0x00000000),              // прозрачный сверху
                            PoolUp.copy(alpha = 0.4f)       // зелёный полупрозрачный снизу
                        ),
                        startY = topPadding,
                        endY = dividerY
                    ),
                    topLeft = Offset(0f, topPadding),
                    size = Size(chartWidth, upZoneHeight)
                )
            }

            // Красная зона DOWN — градиент: красный полупрозрачный сверху -> тёмно-серый снизу
            val downZoneHeight = (topPadding + chartHeight - dividerY).coerceAtLeast(0f)
            if (downZoneHeight > 0f) {
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            PoolDown.copy(alpha = 0.4f),    // красный полупрозрачный сверху
                            Color(0xFF1A1A1A)               // тёмно-серый снизу
                        ),
                        startY = dividerY,
                        endY = topPadding + chartHeight
                    ),
                    topLeft = Offset(0f, dividerY),
                    size = Size(chartWidth, downZoneHeight)
                )
            }

            // --- Плавная анимированная линия цены ---

            if (trades.size >= 2) {
                // Точки для построения пути
                val points = trades.map { t -> Offset(tradeToX(t), priceToY(t.price)) }

                // Функция: создаём сглаженный путь (приближённый Catmull-Rom) через кубические сегменты
                fun createSmoothedPath(pts: List<Offset>): Path {
                    val p = Path()
                    if (pts.isEmpty()) return p
                    p.moveTo(pts[0].x, pts[0].y)
                    if (pts.size == 1) return p
                    for (i in 0 until pts.size - 1) {
                        val p0 = if (i - 1 >= 0) pts[i - 1] else pts[i]
                        val p1 = pts[i]
                        val p2 = pts[i + 1]
                        val p3 = if (i + 2 < pts.size) pts[i + 2] else p2

                        val cp1x = p1.x + (p2.x - p0.x) / 6f
                        val cp1y = p1.y + (p2.y - p0.y) / 6f
                        val cp2x = p2.x - (p3.x - p1.x) / 6f
                        val cp2y = p2.y - (p3.y - p1.y) / 6f

                        p.cubicTo(cp1x, cp1y, cp2x, cp2y, p2.x, p2.y)
                    }
                    return p
                }

                val smoothPath = createSmoothedPath(points)

                // Рисуем всю линию сразу (без анимации для старых точек)
                if (points.size >= 2) {
                    // Путь без последней точки — рисуется мгновенно
                    val stablePoints = points.dropLast(1)
                    if (stablePoints.size >= 2) {
                        val stablePath = createSmoothedPath(stablePoints)
                        drawPath(
                            path = stablePath,
                            color = ChartLine,
                            style = Stroke(width = 2.dp.toPx())
                        )
                    }

                    // Последний сегмент — плавно появляется
                    val lastIdx = points.size - 1
                    val prevPoint = points[lastIdx - 1]
                    val currPoint = points[lastIdx]
                    val animX = prevPoint.x + (currPoint.x - prevPoint.x) * animatedFraction.value
                    val animY = prevPoint.y + (currPoint.y - prevPoint.y) * animatedFraction.value

                    clipRect(left = 0f, top = 0f, right = animX, bottom = topPadding + chartHeight) {
                        drawPath(
                            path = smoothPath,
                            color = ChartLine,
                            style = Stroke(width = 2.dp.toPx())
                        )
                    }
                }
            }

            // --- Иконка BTC — золотая монета на текущей (анимированной) точке ---
            if (trades.isNotEmpty()) {
                val lastX: Float
                val lastY: Float
                if (trades.size >= 2) {
                    val prevPt = Offset(tradeToX(trades[trades.size - 2]), priceToY(trades[trades.size - 2].price))
                    val currPt = Offset(tradeToX(trades.last()), priceToY(latestPrice))
                    lastX = prevPt.x + (currPt.x - prevPt.x) * animatedFraction.value
                    lastY = prevPt.y + (currPt.y - prevPt.y) * animatedFraction.value
                } else {
                    lastX = tradeToX(trades.first())
                    lastY = priceToY(latestPrice)
                }
                val outerRadius = 12.dp.toPx()
                val innerRadius = 10.dp.toPx()

                // Внешний круг — золотой
                drawCircle(
                    color = AccentGold,
                    radius = outerRadius,
                    center = Offset(lastX, lastY)
                )
                // Внутренний круг — чуть темнее золотого
                drawCircle(
                    color = AccentGold.copy(alpha = 0.8f),
                    radius = innerRadius,
                    center = Offset(lastX, lastY)
                )
                // Символ ₿ — белый, жирный, по центру
                drawContext.canvas.nativeCanvas.drawText(
                    "₿",
                    lastX,
                    lastY + 5.dp.toPx(),
                    android.graphics.Paint().apply {
                        color = android.graphics.Color.WHITE
                        textSize = 14.sp.toPx()
                        isAntiAlias = true
                        isFakeBoldText = true
                        textAlign = android.graphics.Paint.Align.CENTER
                    }
                )
            }

            // --- Горизонтальная линия START (пунктирная жёлтая) ---
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

                // --- Вертикальная пунктирная линия START ---
                val startX = tradeToX(trades.first())
                drawLine(
                    color = AccentGold,
                    start = Offset(startX, topPadding),
                    end = Offset(startX, topPadding + chartHeight),
                    strokeWidth = 1.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f))
                )

                // --- Точка фиксации цены (кружок) на пересечении линии цены и линии START ---
                drawCircle(
                    color = AccentGold,
                    radius = 6.dp.toPx(),
                    center = Offset(startX, startY)
                )
            }

            // --- Линия FINISH (вертикальная пунктирная справа) ---
            if (phase == RoundPhase.ACTIVE) {
                val finishX = chartWidth - 2.dp.toPx()
                drawLine(
                    color = AccentGold,
                    start = Offset(finishX, topPadding),
                    end = Offset(finishX, topPadding + chartHeight),
                    strokeWidth = 2.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f))
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
