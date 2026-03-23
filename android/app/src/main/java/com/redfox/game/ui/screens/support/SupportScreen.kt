package com.redfox.game.ui.screens.support

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.redfox.game.R
import com.redfox.game.ui.theme.AccentGold
import com.redfox.game.ui.theme.DarkBackground
import com.redfox.game.ui.theme.DarkCard
import com.redfox.game.ui.theme.DarkSurface
import com.redfox.game.ui.theme.PoolUp
import com.redfox.game.ui.theme.TextPrimary
import com.redfox.game.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupportScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        TopAppBar(
            title = { Text(stringResource(R.string.support_title), color = TextPrimary) },
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
                .padding(16.dp)
        ) {
            // Telegram
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkCard)
                    .clickable {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/redfoxgame_support")))
                    }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, null, tint = AccentGold, modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(stringResource(R.string.telegram_support), color = TextPrimary, fontWeight = FontWeight.SemiBold)
                    Text("@redfoxgame_support", color = TextSecondary, fontSize = 13.sp)
                }
            }

            Spacer(Modifier.height(12.dp))

            // Email
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkCard)
                    .clickable {
                        context.startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:support@redfoxgame.com")))
                    }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Email, null, tint = AccentGold, modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(stringResource(R.string.email_support), color = TextPrimary, fontWeight = FontWeight.SemiBold)
                    Text("support@redfoxgame.com", color = TextSecondary, fontSize = 13.sp)
                }
            }

            Spacer(Modifier.height(24.dp))

            // FAQ
            Text(
                stringResource(R.string.faq),
                color = AccentGold,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(12.dp))

            FaqItem("Что такое RedFox Game?", "RedFox Game — это платформа для прогнозирования движения цены Bitcoin. Делайте ставки UP или DOWN и выигрывайте.")
            FaqItem("Как работает демо-режим?", "Демо-режим использует виртуальные деньги ($5000 на старте). Играйте без риска для тренировки.")
            FaqItem("Как пополнить счёт?", "Перейдите в раздел «Пополнить», выберите сеть (TRC20, ERC20, BTC) и переведите криптовалюту на указанный адрес.")
            FaqItem("Как вывести средства?", "Для вывода необходимо пройти KYC верификацию. После этого перейдите в «Вывести» и укажите адрес.")
            FaqItem("Какая комиссия?", "Комиссия платформы — 15% от проигравшего пула. Она автоматически вычитается при расчёте выигрыша.")
        }
    }
}

@Composable
private fun FaqItem(question: String, answer: String) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(DarkCard)
            .clickable { expanded = !expanded }
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(question, color = TextPrimary, fontSize = 14.sp, modifier = Modifier.weight(1f))
            Icon(
                if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                null, tint = TextSecondary
            )
        }
        AnimatedVisibility(visible = expanded) {
            Text(answer, color = TextSecondary, fontSize = 13.sp, modifier = Modifier.padding(top = 8.dp))
        }
    }
}
