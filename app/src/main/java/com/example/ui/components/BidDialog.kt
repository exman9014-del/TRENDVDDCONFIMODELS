package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.AuctionItem
import com.example.ui.theme.AuctionGold
import com.example.ui.theme.CardBorder
import com.example.ui.theme.PrimaryGreen
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun BidDialog(
    auction: AuctionItem,
    prefilledEmail: String = "",
    onDismiss: () -> Unit,
    onSubmitBid: (auctionId: String, email: String, amount: Double) -> Unit
) {
    var email by remember { mutableStateOf(prefilledEmail) }
    var bidAmountText by remember {
        mutableStateOf(String.format("%.2f", auction.currentBid + 10.0))
    }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("bid_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFFEF3C7)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Gavel,
                                contentDescription = null,
                                tint = AuctionGold,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Dar Lance no Espaço",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = auction.title,
                                fontSize = 13.sp,
                                color = TextSecondary
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Fechar")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Current Bid Info
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF9FAFB))
                        .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "Lance atual", fontSize = 12.sp, color = TextSecondary)
                            Text(
                                text = auction.formattedBid,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryGreen
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = "Tempo restante", fontSize = 12.sp, color = TextSecondary)
                            Text(
                                text = auction.formattedRemainingTime,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFFEF4444)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Email input (no login required!)
                Text(
                    text = "Seu E-mail (Sem necessidade de login)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        errorMessage = null
                    },
                    placeholder = { Text("seu@email.com") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryGreen,
                        unfocusedBorderColor = CardBorder
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("bid_email_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Bid amount input
                Text(
                    text = "Valor do seu Lance (R$)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = bidAmountText,
                    onValueChange = {
                        bidAmountText = it
                        errorMessage = null
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryGreen,
                        unfocusedBorderColor = CardBorder
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("bid_amount_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Quick Increment Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(10.0, 50.0, 100.0).forEach { inc ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFF0FDF4))
                                .border(1.dp, Color(0xFFBBF7D0), RoundedCornerShape(8.dp))
                                .clickable {
                                    val currentVal = bidAmountText.replace(",", ".").toDoubleOrNull()
                                        ?: auction.currentBid
                                    bidAmountText = String.format("%.2f", currentVal + inc)
                                }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "+ R$ ${inc.toInt()}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryGreen
                            )
                        }
                    }
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage.orEmpty(),
                        color = Color(0xFFEF4444),
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        val amount = bidAmountText.replace(",", ".").toDoubleOrNull()
                        if (email.isBlank() || !email.contains("@")) {
                            errorMessage = "Digite um e-mail válido para vincular o lance."
                            return@Button
                        }
                        if (amount == null || amount <= auction.currentBid) {
                            errorMessage = "O lance deve ser maior que ${auction.formattedBid}."
                            return@Button
                        }
                        onSubmitBid(auction.id, email, amount)
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("confirm_bid_button")
                ) {
                    Text(
                        text = "Confirmar Lance no Supabase",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}
