package com.redfox.game.ui.screens.main

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.redfox.game.R
import com.redfox.game.ui.theme.AccentGold
import com.redfox.game.ui.theme.DarkBackground
import com.redfox.game.ui.theme.DarkCard
import com.redfox.game.ui.theme.DarkSurface
import com.redfox.game.ui.theme.Disabled
import com.redfox.game.ui.theme.TextPrimary
import com.redfox.game.ui.theme.TextSecondary

@Composable
fun MainScreen(
    onNavigateToGame: (String) -> Unit,
    onNavigateToSupport: () -> Unit,
    onNavigateToProfile: () -> Unit,
    viewModel: MainViewModel = hiltViewModel()
) {
    val language by viewModel.language.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Переключатель языка (правый верхний угол)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            LanguageToggle(
                currentLanguage = language,
                onToggle = { viewModel.toggleLanguage() }
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Логотип
        Text(
            text = "\uD83E\uDD8A",
            fontSize = 64.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "RedFox Game",
            color = AccentGold,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Карточка «Реальная игра» — заблокирована
        GameCard(
            title = stringResource(R.string.real_game),
            description = stringResource(R.string.real_game_desc),
            enabled = false,
            badgeText = if (language == "ru") "Скоро" else "Soon",
            onClick = { }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Карточка «Демо игра» — активная
        GameCard(
            title = stringResource(R.string.demo_game),
            description = stringResource(R.string.demo_game_desc),
            enabled = true,
            badgeText = null,
            onClick = { onNavigateToGame("demo") }
        )

        Spacer(modifier = Modifier.weight(1f))

        // Кнопка поддержки
        Button(
            onClick = { onNavigateToSupport() },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.support_title),
                color = TextSecondary,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun GameCard(
    title: String,
    description: String,
    enabled: Boolean,
    badgeText: String?,
    onClick: () -> Unit
) {
    val borderColor = if (enabled) AccentGold else Disabled
    val bgColor = if (enabled) DarkCard else DarkSurface

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(1.5.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable(enabled = enabled) { onClick() }
            .padding(20.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (enabled) Icons.Default.PlayArrow else Icons.Default.Lock,
                    contentDescription = null,
                    tint = if (enabled) AccentGold else Disabled,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    color = if (enabled) TextPrimary else Disabled,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                if (badgeText != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Disabled.copy(alpha = 0.5f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = badgeText,
                            color = TextSecondary,
                            fontSize = 10.sp
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = description,
                color = if (enabled) TextSecondary else Disabled,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun LanguageToggle(
    currentLanguage: String,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(DarkSurface)
            .clickable { onToggle() }
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "\uD83C\uDDF7\uD83C\uDDFA",
            fontSize = 16.sp,
            modifier = Modifier.padding(end = 4.dp),
            color = if (currentLanguage == "ru") TextPrimary else TextPrimary.copy(alpha = 0.4f)
        )
        Text(
            text = "|",
            color = Disabled,
            fontSize = 14.sp
        )
        Text(
            text = "\uD83C\uDDEC\uD83C\uDDE7",
            fontSize = 16.sp,
            modifier = Modifier.padding(start = 4.dp),
            color = if (currentLanguage == "en") TextPrimary else TextPrimary.copy(alpha = 0.4f)
        )
    }
}
