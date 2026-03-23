package com.redfox.game.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.redfox.game.ui.theme.DarkCard
import com.redfox.game.ui.theme.DarkSurface
import com.redfox.game.ui.theme.Disabled
import com.redfox.game.ui.theme.TextPrimary
import com.redfox.game.ui.theme.TextSecondary
import com.redfox.game.util.Constants
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DemoDepositDialog(
    currentBalance: Double,
    onDeposit: (Double) -> Unit,
    onDismiss: () -> Unit
) {
    var amount by remember { mutableDoubleStateOf(100.0) }
    val maxDeposit = (Constants.DEMO_MAX_BALANCE - currentBalance).coerceAtLeast(0.0)
    val canDeposit = amount > 0 && amount <= maxDeposit

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = {
            Text(
                text = stringResource(R.string.demo_deposit_title),
                color = AccentGold,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                // Текст-подсказка
                Text(
                    text = stringResource(R.string.demo_deposit_hint),
                    color = TextSecondary,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Регулятор суммы
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    SmallBtn(
                        text = "−",
                        enabled = amount > Constants.MIN_BET,
                        onClick = { amount = (amount - Constants.MIN_BET).coerceAtLeast(Constants.MIN_BET) }
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = formatMoney(amount),
                        color = TextPrimary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.width(120.dp)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    SmallBtn(
                        text = "+",
                        enabled = amount < maxDeposit,
                        onClick = { amount = (amount + Constants.MIN_BET).coerceAtMost(maxDeposit) }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Быстрые кнопки
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(100.0, 500.0, 1000.0, 5000.0).forEach { quickAmount ->
                        val enabled = quickAmount <= maxDeposit
                        QuickButton(
                            amount = quickAmount,
                            enabled = enabled,
                            isSelected = amount == quickAmount,
                            onClick = { amount = quickAmount }
                        )
                    }
                }

                if (maxDeposit <= 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.demo_deposit_max_reached),
                        color = Color(0xFFFF6B6B),
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onDeposit(amount)
                    onDismiss()
                },
                enabled = canDeposit,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentGold,
                    disabledContainerColor = Disabled
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = stringResource(R.string.demo_deposit_button),
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.back), color = TextSecondary)
            }
        }
    )
}

@Composable
private fun SmallBtn(text: String, enabled: Boolean, onClick: () -> Unit) {
    Text(
        text = text,
        color = if (enabled) TextPrimary else Disabled,
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(DarkCard)
            .clickable(enabled = enabled) { onClick() }
            .padding(horizontal = 12.dp, vertical = 4.dp)
    )
}

@Composable
private fun QuickButton(amount: Double, enabled: Boolean, isSelected: Boolean, onClick: () -> Unit) {
    val bg = when {
        isSelected -> AccentGold.copy(alpha = 0.2f)
        enabled -> DarkCard
        else -> Disabled.copy(alpha = 0.2f)
    }
    val textColor = when {
        isSelected -> AccentGold
        enabled -> TextPrimary
        else -> Disabled
    }

    Text(
        text = "$${amount.toInt()}",
        color = textColor,
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .clickable(enabled = enabled) { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp)
    )
}

private fun formatMoney(amount: Double): String {
    val format = NumberFormat.getNumberInstance(Locale.US).apply {
        minimumFractionDigits = 0
        maximumFractionDigits = 0
    }
    return "$ ${format.format(amount)}"
}
