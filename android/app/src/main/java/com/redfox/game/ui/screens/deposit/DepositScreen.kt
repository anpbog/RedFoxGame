package com.redfox.game.ui.screens.deposit

import android.widget.Toast
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
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
import com.redfox.game.ui.theme.TextPrimary
import com.redfox.game.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DepositScreen(
    onNavigateBack: () -> Unit
) {
    var selectedNetwork by remember { mutableStateOf("TRC20") }
    val networks = listOf("TRC20" to "USDT", "ERC20" to "USDT", "BTC" to "BTC")
    val generatedAddress = "T${(1..33).map { "0123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz".random() }.joinToString("")}"
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        TopAppBar(
            title = { Text(stringResource(R.string.deposit_title), color = TextPrimary) },
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Выбор сети
            Text(stringResource(R.string.select_network), color = TextSecondary, fontSize = 14.sp)
            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                networks.forEach { (network, currency) ->
                    val isSelected = selectedNetwork == network
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) DarkCard else DarkSurface)
                            .border(
                                1.dp,
                                if (isSelected) AccentGold else Disabled,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { selectedNetwork = network }
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(currency, color = if (isSelected) AccentGold else TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(network, color = TextSecondary, fontSize = 11.sp)
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // QR-код placeholder
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(TextPrimary),
                contentAlignment = Alignment.Center
            ) {
                Text("QR", fontSize = 32.sp, color = DarkBackground, fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(16.dp))

            // Адрес
            Text(
                text = generatedAddress,
                color = TextPrimary,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(DarkCard)
                    .clickable {
                        clipboardManager.setText(AnnotatedString(generatedAddress))
                        Toast.makeText(context, "Адрес скопирован", Toast.LENGTH_SHORT).show()
                    }
                    .padding(12.dp)
            )

            Spacer(Modifier.height(8.dp))
            Text(stringResource(R.string.copy_address), color = AccentGold, fontSize = 13.sp)

            Spacer(Modifier.height(24.dp))

            // Статус
            Text("Ожидание перевода...", color = TextSecondary, fontSize = 14.sp)
        }
    }
}
