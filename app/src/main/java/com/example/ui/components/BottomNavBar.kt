package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material.icons.outlined.Gavel
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Whatshot
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.PrimaryGreen
import com.example.ui.theme.TextMuted
import com.example.viewmodel.ScreenDestination

data class NavItem(
    val destination: ScreenDestination,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val isProminent: Boolean = false
)

@Composable
fun BottomNavBar(
    currentScreen: ScreenDestination,
    onNavigate: (ScreenDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        NavItem(
            destination = ScreenDestination.LANDING,
            label = "Início",
            selectedIcon = Icons.Filled.Home,
            unselectedIcon = Icons.Outlined.Home
        ),
        NavItem(
            destination = ScreenDestination.FEED,
            label = "Feed",
            selectedIcon = Icons.Filled.Whatshot,
            unselectedIcon = Icons.Outlined.Whatshot
        ),
        NavItem(
            destination = ScreenDestination.CREATE_POST,
            label = "Criar",
            selectedIcon = Icons.Filled.AddCircle,
            unselectedIcon = Icons.Filled.AddCircle,
            isProminent = true
        ),
        NavItem(
            destination = ScreenDestination.AUCTIONS,
            label = "Leilões",
            selectedIcon = Icons.Filled.Gavel,
            unselectedIcon = Icons.Outlined.Gavel
        ),
        NavItem(
            destination = ScreenDestination.PROFILE,
            label = "Perfil",
            selectedIcon = Icons.Filled.Person,
            unselectedIcon = Icons.Outlined.Person
        )
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White)
            .navigationBarsPadding()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color(0xFFE5E7EB))
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val isSelected = currentScreen == item.destination

                if (item.isProminent) {
                    // Center highlighted "+" button
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clickable { onNavigate(item.destination) }
                            .padding(vertical = 4.dp)
                            .testTag("nav_${item.label.lowercase()}")
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(PrimaryGreen),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = item.label,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = item.label,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) PrimaryGreen else TextMuted
                        )
                    }
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clickable { onNavigate(item.destination) }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .testTag("nav_${item.label.lowercase()}")
                    ) {
                        Icon(
                            imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                            contentDescription = item.label,
                            tint = if (isSelected) PrimaryGreen else TextMuted,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = item.label,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) PrimaryGreen else TextMuted
                        )
                    }
                }
            }
        }
    }
}
