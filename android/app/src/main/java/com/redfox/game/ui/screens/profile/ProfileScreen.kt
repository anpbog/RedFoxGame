package com.redfox.game.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.redfox.game.R
import com.redfox.game.ui.screens.auth.AuthViewModel
import com.redfox.game.ui.theme.AccentGold
import com.redfox.game.ui.theme.DarkBackground
import com.redfox.game.ui.theme.DarkSurface
import com.redfox.game.ui.theme.ErrorRed
import com.redfox.game.ui.theme.PoolDown
import com.redfox.game.ui.theme.PoolUp
import com.redfox.game.ui.theme.TextPrimary
import com.redfox.game.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit,
    authViewModel: AuthViewModel = hiltViewModel(),
    profileViewModel: ProfileViewModel = hiltViewModel()
) {
    val profileState by profileViewModel.state.collectAsState()

    // Инициалы из email (первые 2 символа до @)
    val initials = profileState.email
        .substringBefore("@")
        .take(2)
        .uppercase()
        .ifEmpty { "?" }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .systemBarsPadding()
    ) {
        // Верхняя панель с кнопкой назад
        TopAppBar(
            title = { Text(stringResource(R.string.profile_title), color = TextPrimary) },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = TextPrimary)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Аватар — круг с инициалами из email
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(DarkSurface)
                    .border(2.dp, AccentGold, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initials,
                    color = AccentGold,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(8.dp))

            // Email пользователя
            Text(
                text = profileState.email.ifEmpty { "---" },
                color = TextSecondary,
                fontSize = 14.sp
            )

            Spacer(Modifier.height(20.dp))

            // TabRow: Демо / Реальная
            val modeIndex = if (profileState.mode == StatsMode.DEMO) 0 else 1
            TabRow(
                selectedTabIndex = modeIndex,
                containerColor = DarkSurface,
                contentColor = AccentGold,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[modeIndex]),
                        color = AccentGold
                    )
                }
            ) {
                Tab(
                    selected = modeIndex == 0,
                    onClick = { profileViewModel.setMode(StatsMode.DEMO) },
                    text = {
                        Text(
                            stringResource(R.string.profile_demo),
                            color = if (modeIndex == 0) AccentGold else TextSecondary
                        )
                    }
                )
                Tab(
                    selected = modeIndex == 1,
                    onClick = { profileViewModel.setMode(StatsMode.REAL) },
                    text = {
                        Text(
                            stringResource(R.string.profile_real),
                            color = if (modeIndex == 1) AccentGold else TextSecondary
                        )
                    }
                )
            }

            Spacer(Modifier.height(16.dp))

            // Фильтр по периоду: День / Месяц / Всё время
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PeriodButton(
                    text = stringResource(R.string.profile_filter_day),
                    selected = profileState.period == StatsPeriod.DAY,
                    onClick = { profileViewModel.setPeriod(StatsPeriod.DAY) },
                    modifier = Modifier.weight(1f)
                )
                PeriodButton(
                    text = stringResource(R.string.profile_filter_month),
                    selected = profileState.period == StatsPeriod.MONTH,
                    onClick = { profileViewModel.setPeriod(StatsPeriod.MONTH) },
                    modifier = Modifier.weight(1f)
                )
                PeriodButton(
                    text = stringResource(R.string.profile_filter_all),
                    selected = profileState.period == StatsPeriod.ALL,
                    onClick = { profileViewModel.setPeriod(StatsPeriod.ALL) },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(20.dp))

            // Индикатор загрузки
            if (profileState.isLoading) {
                CircularProgressIndicator(color = AccentGold, modifier = Modifier.size(32.dp))
                Spacer(Modifier.height(16.dp))
            }

            // Карточки статистики
            val stats = profileState.stats
            val winPercent = if (stats.totalRounds > 0) {
                "%.1f%%".format(stats.wins * 100.0 / stats.totalRounds)
            } else "0%"
            val lossPercent = if (stats.totalRounds > 0) {
                "%.1f%%".format(stats.losses * 100.0 / stats.totalRounds)
            } else "0%"

            StatCard(
                label = stringResource(R.string.profile_total_rounds),
                value = stats.totalRounds.toString(),
                valueColor = TextPrimary
            )
            Spacer(Modifier.height(8.dp))

            StatCard(
                label = stringResource(R.string.profile_wins),
                value = "${stats.wins} ($winPercent)",
                valueColor = PoolUp
            )
            Spacer(Modifier.height(8.dp))

            StatCard(
                label = stringResource(R.string.profile_losses),
                value = "${stats.losses} ($lossPercent)",
                valueColor = PoolDown
            )
            Spacer(Modifier.height(8.dp))

            StatCard(
                label = stringResource(R.string.profile_profit),
                value = "\$${"%.2f".format(stats.profit)}",
                valueColor = if (stats.profit >= 0) PoolUp else PoolDown
            )
            Spacer(Modifier.height(8.dp))

            StatCard(
                label = stringResource(R.string.profile_avg_bet),
                value = "\$${"%.2f".format(stats.avgBet)}",
                valueColor = AccentGold
            )

            Spacer(Modifier.height(32.dp))

            // Кнопка выхода
            Button(
                onClick = {
                    authViewModel.logout()
                    onLogout()
                },
                colors = ButtonDefaults.buttonColors(containerColor = ErrorRed.copy(alpha = 0.2f)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.AutoMirrored.Filled.ExitToApp, null, tint = ErrorRed)
                Spacer(Modifier.width(8.dp))
                Text(
                    stringResource(R.string.profile_logout),
                    color = ErrorRed
                )
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

// Кнопка выбора периода
@Composable
private fun PeriodButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) AccentGold else DarkSurface
        ),
        shape = RoundedCornerShape(8.dp),
        modifier = modifier.height(36.dp)
    ) {
        Text(
            text = text,
            color = if (selected) DarkBackground else TextSecondary,
            fontSize = 12.sp,
            maxLines = 1
        )
    }
}

// Карточка статистики
@Composable
private fun StatCard(
    label: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurface)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = TextSecondary,
            fontSize = 14.sp
        )
        Text(
            text = value,
            color = valueColor,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.End
        )
    }
}
