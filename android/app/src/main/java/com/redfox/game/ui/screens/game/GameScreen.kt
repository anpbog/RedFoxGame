package com.redfox.game.ui.screens.game

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.redfox.game.R
import com.redfox.game.domain.model.BetDirection
import com.redfox.game.domain.model.Round
import com.redfox.game.domain.model.RoundPhase
import com.redfox.game.ui.components.BtcChart
import com.redfox.game.ui.components.DemoDepositDialog
import com.redfox.game.ui.theme.AccentGold
import com.redfox.game.ui.theme.DarkBackground
import com.redfox.game.ui.theme.DarkCard
import com.redfox.game.ui.theme.DarkSurface
import com.redfox.game.ui.theme.Disabled
import com.redfox.game.ui.theme.PoolDown
import com.redfox.game.ui.theme.PoolUp
import com.redfox.game.ui.theme.TextPrimary
import com.redfox.game.ui.theme.TextSecondary
import java.text.NumberFormat
import java.util.Locale

@Composable
fun GameScreen(
    mode: String,
    onNavigateToDeposit: () -> Unit,
    onNavigateToWithdraw: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: GameViewModel = hiltViewModel()
) {
    val balance by viewModel.balance.collectAsState()
    val round by viewModel.currentRound.collectAsState()
    val timer by viewModel.timer.collectAsState()
    val playerBet by viewModel.playerBet.collectAsState()
    val bots by viewModel.bots.collectAsState()
    val priceBuffer by viewModel.priceBuffer.collectAsState()
    val betAmount by viewModel.betAmount.collectAsState()
    val roundHistory by viewModel.roundHistory.collectAsState()
    val showResult by viewModel.showResultOverlay.collectAsState()
    val lastResult by viewModel.lastResult.collectAsState()
    val isConnected by viewModel.isConnected.collectAsState()
    val isPaused by viewModel.isPaused.collectAsState()
    var showDepositDialog by remember { mutableStateOf(false) }
    var showEmptyBalanceDialog by remember { mutableStateOf(false) }
    var lastKnownBalance by remember { mutableStateOf(balance) }

    // Отслеживаем баланс = 0 после проигрыша
    if (balance <= 0.0 && lastKnownBalance > 0.0) {
        showEmptyBalanceDialog = true
    }
    lastKnownBalance = balance

    // Диалог пополнения демо-счёта
    if (showDepositDialog) {
        DemoDepositDialog(
            currentBalance = balance,
            onDeposit = { amount -> viewModel.addDemoBalance(amount) },
            onDismiss = { showDepositDialog = false }
        )
    }

    // Диалог «Баланс исчерпан»
    if (showEmptyBalanceDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showEmptyBalanceDialog = false },
            containerColor = DarkSurface,
            title = {
                Text(
                    text = stringResource(R.string.balance_empty_title),
                    color = com.redfox.game.ui.theme.ErrorRed
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.balance_empty_message, String.format("%,.0f", com.redfox.game.util.Constants.DEMO_START_BALANCE)),
                    color = com.redfox.game.ui.theme.TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.addDemoBalance(com.redfox.game.util.Constants.DEMO_START_BALANCE)
                        showEmptyBalanceDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentGold)
                ) {
                    Text(stringResource(R.string.demo_deposit_button), color = Color.Black)
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(
                    onClick = { showEmptyBalanceDialog = false }
                ) {
                    Text(stringResource(R.string.back), color = TextSecondary)
                }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // === Верхняя панель ===
            TopBar(
                balance = balance,
                timer = timer,
                phase = round.phase,
                payoutUp = round.payoutUp,
                payoutDown = round.payoutDown,
                onDeposit = { showDepositDialog = true }
            )

            // === График BTC + плашка соединения ===
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                BtcChart(
                    trades = priceBuffer,
                    startPrice = round.startPrice,
                    phase = round.phase,
                    modifier = Modifier.fillMaxSize(),
                    labelWaiting = stringResource(R.string.chart_waiting),
                    labelBtcLive = stringResource(R.string.chart_btc_live),
                    labelStart = stringResource(R.string.chart_start)
                )

                // Плашка «Нет соединения»
                androidx.compose.animation.AnimatedVisibility(
                    visible = !isConnected,
                    enter = fadeIn(),
                    exit = fadeOut(),
                    modifier = Modifier.align(Alignment.TopCenter)
                ) {
                    Box(
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(com.redfox.game.ui.theme.ErrorRed.copy(alpha = 0.9f))
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.no_connection),
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // === Лента истории раундов ===
            RoundHistoryRow(history = roundHistory)

            // === Панель пулов ===
            PoolPanel(
                round = round,
                bots = bots,
                playerBet = playerBet
            )

            // === Панель ставки ===
            BetPanel(
                betAmount = betAmount,
                balance = balance,
                phase = round.phase,
                playerBet = playerBet,
                onPlaceBet = { direction -> viewModel.placeBet(direction) },
                onIncrease = { viewModel.increaseBet() },
                onDecrease = { viewModel.decreaseBet() },
                onMin = { viewModel.setMinBet() },
                onMax = { viewModel.setMaxBet() }
            )
        }

        // === Оверлей результата ===
        AnimatedVisibility(
            visible = showResult,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            lastResult?.let { result ->
                ResultOverlay(
                    isWin = result.isWin,
                    amount = if (result.isWin) result.playerPayout else (result.playerBet?.amount ?: 0.0),
                    onDismiss = { viewModel.dismissResultOverlay() }
                )
            }
        }
    }
}

// === Верхняя панель ===
@Composable
private fun TopBar(
    balance: Double,
    timer: Int,
    phase: RoundPhase,
    payoutUp: Double,
    payoutDown: Double,
    onDeposit: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkSurface)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Баланс
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.balance),
                color = TextSecondary,
                fontSize = 10.sp
            )
            Text(
                text = formatMoney(balance),
                color = AccentGold,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Кнопка пополнить (демо)
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(AccentGold.copy(alpha = 0.2f))
                .clickable { onDeposit() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = stringResource(R.string.deposit),
                tint = AccentGold,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Payout UP/DOWN
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "↑ ${String.format(Locale.US, "%.1fx", payoutUp)}",
                color = PoolUp,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "↓ ${String.format(Locale.US, "%.1fx", payoutDown)}",
                color = PoolDown,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Таймер
        TimerCircle(seconds = timer, phase = phase)
    }
}

// === Круглый таймер с пульсацией ===
@Composable
private fun TimerCircle(seconds: Int, phase: RoundPhase) {
    val color = when (phase) {
        RoundPhase.BETTING -> AccentGold
        RoundPhase.ACTIVE -> PoolUp
        RoundPhase.CALCULATING -> PoolDown
    }

    // Пульсация на последних 10 секундах
    val isPulsing = seconds in 1..10
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )
    val scale = if (isPulsing) pulseScale else 1f

    Box(
        modifier = Modifier
            .size(44.dp)
            .then(
                if (isPulsing) Modifier.graphicsLayer(scaleX = scale, scaleY = scale)
                else Modifier
            )
            .border(2.dp, color, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$seconds",
            color = color,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

// === Лента истории раундов ===
@Composable
private fun RoundHistoryRow(history: List<Round>) {
    if (history.isEmpty()) return

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        contentPadding = PaddingValues(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(history) { round ->
            val isUp = round.result == BetDirection.UP
            val color = if (isUp) PoolUp else PoolDown
            val arrow = if (isUp) "↑" else "↓"
            val payout = if (isUp) round.payoutUp else round.payoutDown

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(color.copy(alpha = 0.15f))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$arrow ${String.format(Locale.US, "%.1fx", payout)}",
                    color = color,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

// === Панель пулов ===
@Composable
private fun PoolPanel(
    round: Round,
    bots: List<com.redfox.game.domain.model.BotPlayer>,
    playerBet: com.redfox.game.domain.model.Bet?
) {
    val botsUp = bots.filter { it.bet.direction == BetDirection.UP }
    val botsDown = bots.filter { it.bet.direction == BetDirection.DOWN }
    val playerInUp = playerBet?.direction == BetDirection.UP
    val playerInDown = playerBet?.direction == BetDirection.DOWN

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Pool UP
        PoolCard(
            label = "POOL UP",
            color = PoolUp,
            totalAmount = round.poolUp,
            payout = round.payoutUp,
            botCount = botsUp.size + if (playerInUp) 1 else 0,
            botNames = botsUp.map { "${it.countryFlag} ${it.name}" },
            hasPlayer = playerInUp,
            modifier = Modifier.weight(1f)
        )

        // Pool DOWN
        PoolCard(
            label = "POOL DOWN",
            color = PoolDown,
            totalAmount = round.poolDown,
            payout = round.payoutDown,
            botCount = botsDown.size + if (playerInDown) 1 else 0,
            botNames = botsDown.map { "${it.countryFlag} ${it.name}" },
            hasPlayer = playerInDown,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun PoolCard(
    label: String,
    color: Color,
    totalAmount: Double,
    payout: Double,
    botCount: Int,
    botNames: List<String>,
    hasPlayer: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(DarkCard)
            .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
            .padding(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = label,
                color = color,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "${String.format(Locale.US, "%.1f", payout)}x",
                color = color,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Text(
            text = formatMoney(totalAmount),
            color = TextPrimary,
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Аватары (показываем первых 3 + счётчик)
        Row(
            horizontalArrangement = Arrangement.spacedBy((-4).dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val displayNames = botNames.take(3)
            displayNames.forEach { name ->
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(color.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = name.take(2),
                        color = TextPrimary,
                        fontSize = 8.sp
                    )
                }
            }
            if (hasPlayer) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(AccentGold.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "Я", color = TextPrimary, fontSize = 8.sp)
                }
            }
            if (botCount > 3) {
                Text(
                    text = " +${botCount - 3}",
                    color = TextSecondary,
                    fontSize = 10.sp
                )
            }
        }
    }
}

// === Панель ставки ===
@Composable
private fun BetPanel(
    betAmount: Double,
    balance: Double,
    phase: RoundPhase,
    playerBet: com.redfox.game.domain.model.Bet?,
    onPlaceBet: (BetDirection) -> Unit,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    onMin: () -> Unit,
    onMax: () -> Unit
) {
    val canBet = phase == RoundPhase.BETTING && playerBet == null

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkSurface)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        // Регулятор суммы
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            // MIN
            SmallButton(
                text = stringResource(R.string.min_bet),
                enabled = canBet,
                onClick = onMin
            )

            Spacer(modifier = Modifier.width(4.dp))

            // −
            SmallButton(
                text = "−",
                enabled = canBet,
                onClick = onDecrease
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Сумма
            Text(
                text = formatMoney(betAmount),
                color = if (canBet) TextPrimary else Disabled,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.width(100.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            // +
            SmallButton(
                text = "+",
                enabled = canBet,
                onClick = onIncrease
            )

            Spacer(modifier = Modifier.width(4.dp))

            // MAX
            SmallButton(
                text = stringResource(R.string.max_bet),
                enabled = canBet,
                onClick = onMax
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Кнопки UP / DOWN
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // UP
            Button(
                onClick = { onPlaceBet(BetDirection.UP) },
                enabled = canBet,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PoolUp,
                    disabledContainerColor = Disabled
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowUp,
                    contentDescription = null,
                    tint = Color.Black
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = stringResource(R.string.pool_up),
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            // DOWN
            Button(
                onClick = { onPlaceBet(BetDirection.DOWN) },
                enabled = canBet,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PoolDown,
                    disabledContainerColor = Disabled
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = stringResource(R.string.pool_down),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }
}

// === Маленькая кнопка ===
@Composable
private fun SmallButton(text: String, enabled: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(if (enabled) DarkCard else Disabled.copy(alpha = 0.3f))
            .clickable(enabled = enabled) { onClick() }
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (enabled) TextSecondary else Disabled,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

// === Оверлей результата ===
@Composable
private fun ResultOverlay(
    isWin: Boolean,
    amount: Double,
    onDismiss: () -> Unit
) {
    val bgColor = if (isWin) PoolUp.copy(alpha = 0.85f) else PoolDown.copy(alpha = 0.85f)
    val title = if (isWin) "WIN!" else "LOSE"
    val sign = if (isWin) "+" else "-"

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .clickable { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "$sign ${formatMoney(amount)}",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

// === Форматирование денег ===
private fun formatMoney(amount: Double): String {
    val format = NumberFormat.getNumberInstance(Locale.US).apply {
        minimumFractionDigits = 2
        maximumFractionDigits = 2
    }
    return "$ ${format.format(amount)}"
}
