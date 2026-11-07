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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CoralRed
import com.example.ui.theme.PrimaryGreen
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun AddTraitDialog(
    onDismiss: () -> Unit,
    onAddTrait: (name: String, isBeauty: Boolean) -> Unit
) {
    var traitName by remember { mutableStateOf("") }
    var isBeauty by remember { mutableStateOf(true) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("add_trait_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Criar Traço Personalizado",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Fechar")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Categoria do Traço",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isBeauty) Color(0xFFF0FDF4) else Color.White)
                            .border(
                                width = if (isBeauty) 2.dp else 1.dp,
                                color = if (isBeauty) PrimaryGreen else CardBorder,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable { isBeauty = true }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "+ Beleza",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isBeauty) PrimaryGreen else TextSecondary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (!isBeauty) Color(0xFFFEE2E2) else Color.White)
                            .border(
                                width = if (!isBeauty) 2.dp else 1.dp,
                                color = if (!isBeauty) CoralRed else CardBorder,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable { isBeauty = false }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "+ Feiura",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (!isBeauty) CoralRed else TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Nome do Traço (ex: piercing no nariz, sardas)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = traitName,
                    onValueChange = { traitName = it },
                    placeholder = { Text("Ex: maxilar marcado") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (isBeauty) PrimaryGreen else CoralRed,
                        unfocusedBorderColor = CardBorder
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("trait_name_input")
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        if (traitName.isNotBlank()) {
                            onAddTrait(traitName.trim(), isBeauty)
                            onDismiss()
                        }
                    },
                    enabled = traitName.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isBeauty) PrimaryGreen else CoralRed
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("save_trait_button")
                ) {
                    Text(
                        text = "Adicionar ao Seletor",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}
