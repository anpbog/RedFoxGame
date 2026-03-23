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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.redfox.game.R
import com.redfox.game.ui.theme.AccentGold
import com.redfox.game.ui.theme.DarkBackground
import com.redfox.game.ui.theme.DarkCard
import com.redfox.game.ui.theme.DarkSurface
import com.redfox.game.ui.theme.Disabled
import com.redfox.game.ui.theme.ErrorRed
import com.redfox.game.ui.theme.PoolUp
import com.redfox.game.ui.theme.PoolDown
import com.redfox.game.ui.theme.TextPrimary
import com.redfox.game.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
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
            // Аватар
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(DarkCard)
                    .border(2.dp, AccentGold, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("\uD83E\uDD8A", fontSize = 36.sp)
            }

            Spacer(Modifier.height(12.dp))

            // Никнейм
            Text("Player_Demo", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text("demo@redfoxgame.com", color = TextSecondary, fontSize = 14.sp)

            Spacer(Modifier.height(8.dp))

            // KYC бейдж
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(Disabled.copy(alpha = 0.5f))
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text("KYC: Не пройдена", color = TextSecondary, fontSize = 12.sp)
            }

            Spacer(Modifier.height(24.dp))

            // Статистика (4 колонки)
            Text(stringResource(R.string.statistics), color = AccentGold, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(stringResource(R.string.rounds_played), "0")
                StatItem(stringResource(R.string.wins), "0", PoolUp)
                StatItem(stringResource(R.string.losses), "0", PoolDown)
                StatItem(stringResource(R.string.profit), "$0", AccentGold)
            }

            Spacer(Modifier.height(24.dp))

            // Кнопка выхода
            Button(
                onClick = { onNavigateBack() },
                colors = ButtonDefaults.buttonColors(containerColor = ErrorRed.copy(alpha = 0.2f)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.AutoMirrored.Filled.ExitToApp, null, tint = ErrorRed)
                Text(
                    stringResource(R.string.logout),
                    color = ErrorRed,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String, valueColor: Color = TextPrimary) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = valueColor, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text(label, color = TextSecondary, fontSize = 11.sp, textAlign = TextAlign.Center)
    }
}
