package com.redfox.game.ui.screens.withdraw

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
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.redfox.game.R
import com.redfox.game.ui.theme.AccentGold
import com.redfox.game.ui.theme.DarkBackground
import com.redfox.game.ui.theme.DarkCard
import com.redfox.game.ui.theme.DarkSurface
import com.redfox.game.ui.theme.Disabled
import com.redfox.game.ui.theme.ErrorRed
import com.redfox.game.ui.theme.TextPrimary
import com.redfox.game.ui.theme.TextSecondary
import com.redfox.game.ui.theme.WarningYellow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WithdrawScreen(
    onNavigateBack: () -> Unit
) {
    var selectedNetwork by remember { mutableStateOf("TRC20") }
    var address by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    val networks = listOf("TRC20", "ERC20", "BTC")
    val kycApproved = false // TODO: из ViewModel

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .systemBarsPadding()
    ) {
        TopAppBar(
            title = { Text(stringResource(R.string.withdraw_title), color = TextPrimary) },
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
                .padding(16.dp)
        ) {
            // KYC плашка
            if (!kycApproved) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(WarningYellow.copy(alpha = 0.15f))
                        .border(1.dp, WarningYellow, RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.kyc_required),
                        color = WarningYellow,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(Modifier.height(16.dp))
            }

            // Выбор сети
            Text(stringResource(R.string.select_network), color = TextSecondary, fontSize = 14.sp)
            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                networks.forEach { network ->
                    val isSelected = selectedNetwork == network
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) DarkCard else DarkSurface)
                            .border(1.dp, if (isSelected) AccentGold else Disabled, RoundedCornerShape(8.dp))
                            .clickable { selectedNetwork = network }
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(network, color = if (isSelected) AccentGold else TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Адрес
            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text(stringResource(R.string.enter_address)) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AccentGold,
                    unfocusedBorderColor = Disabled,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedLabelColor = AccentGold,
                    unfocusedLabelColor = TextSecondary
                ),
                singleLine = true
            )

            Spacer(Modifier.height(12.dp))

            // Сумма
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text(stringResource(R.string.amount)) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AccentGold,
                    unfocusedBorderColor = Disabled,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedLabelColor = AccentGold,
                    unfocusedLabelColor = TextSecondary
                ),
                singleLine = true
            )

            Spacer(Modifier.height(8.dp))
            Text("${stringResource(R.string.commission)}: 1 USDT", color = TextSecondary, fontSize = 13.sp)

            Spacer(Modifier.height(24.dp))

            // Кнопка подтверждения
            Button(
                onClick = { /* TODO: вывод */ },
                enabled = kycApproved && address.isNotBlank() && amount.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentGold,
                    disabledContainerColor = Disabled
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text(stringResource(R.string.confirm), color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
    }
}
