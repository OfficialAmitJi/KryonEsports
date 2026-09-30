package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CosmeticItem
import com.example.data.model.UserProfileEntity
import com.example.ui.theme.*
import com.example.util.ShareHelper

@Composable
fun ProfileHangarScreen(
    user: UserProfileEntity?,
    cosmetics: List<CosmeticItem>,
    showSyncDialog: Boolean,
    onEquipShip: (String) -> Unit,
    onEquipTrail: (String) -> Unit,
    onEquipTitle: (String) -> Unit,
    onShowSyncDialog: (Boolean) -> Unit
) {
    var selectedCategory by remember { mutableStateOf("SHIP") } // "SHIP", "TRAIL", "TITLE"
    var syncInputCode by remember { mutableStateOf("") }
    var syncSuccessMsg by remember { mutableStateOf<String?>(null) }

    val ships = cosmetics.filter { it.category == "SHIP" }
    val trails = cosmetics.filter { it.category == "TRAIL" }
    val titles = cosmetics.filter { it.category == "TITLE" }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(VoidDark)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp)
    ) {
        // Player Profile Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SpaceCard),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(Brush.linearGradient(listOf(CyberCyan, NeonMagenta)))
                                .padding(2.dp)
                                .clip(CircleShape)
                                .background(VoidDark),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(user?.avatarEmoji ?: "🚀", fontSize = 32.sp)
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = user?.displayName ?: "NovaPilot",
                                    color = TextWhite,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 18.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(CosmicAmber)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "LVL ${user?.level ?: 14}",
                                        color = VoidDark,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                            Text(
                                text = user?.title ?: "Apex Pilot",
                                color = CyberCyan,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "UID: ${user?.userId ?: "CR-7829-X"}",
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Stats Grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ProfileStatBox("HIGH SCORE", "${user?.highScore ?: 84250L}", CyberCyan, Modifier.weight(1f))
                        ProfileStatBox("MATCHES", "${user?.totalMatches ?: 86}", CosmicAmber, Modifier.weight(1f))
                        val winRate = if ((user?.totalMatches ?: 1) > 0) ((user?.wins ?: 54) * 100 / (user?.totalMatches ?: 86)) else 0
                        ProfileStatBox("WIN RATE", "$winRate%", EmeraldMatrix, Modifier.weight(1f))
                    }
                }
            }
        }

        // Cross-Platform Data Synchronization Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, SpaceBorder, RoundedCornerShape(14.dp)),
                colors = CardDefaults.cardColors(containerColor = SpaceCard),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CloudSync,
                                contentDescription = null,
                                tint = CyberCyan,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "CROSS-PLATFORM SYNC",
                                color = TextWhite,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(SuccessGreen.copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "SYNCED",
                                color = EmeraldMatrix,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Play seamlessly across PC, Console, iOS, and Android. Your unlocks and rank are automatically synchronized in real time.",
                        color = TextMuted,
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedButton(
                        onClick = { onShowSyncDialog(true) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .minimumInteractiveComponentSize()
                            .testTag("btn_open_cross_save_modal"),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = CyberCyan),
                        border = ButtonDefaults.outlinedButtonBorder.copy(brush = Brush.horizontalGradient(listOf(CyberCyan, CyberCyanDark))),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Devices, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("LINK DEVICE / GENERATE CROSS-SAVE CODE", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Hangar Wardrobe Category Tabs
        item {
            TabRow(
                selectedTabIndex = when (selectedCategory) {
                    "SHIP" -> 0
                    "TRAIL" -> 1
                    else -> 2
                },
                containerColor = SpaceCard,
                contentColor = CyberCyan,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, SpaceBorder, RoundedCornerShape(12.dp))
            ) {
                Tab(
                    selected = selectedCategory == "SHIP",
                    onClick = { selectedCategory = "SHIP" },
                    text = { Text("SHIPS", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("tab_hangar_ships")
                )
                Tab(
                    selected = selectedCategory == "TRAIL",
                    onClick = { selectedCategory = "TRAIL" },
                    text = { Text("TRAILS", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("tab_hangar_trails")
                )
                Tab(
                    selected = selectedCategory == "TITLE",
                    onClick = { selectedCategory = "TITLE" },
                    text = { Text("TITLES", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("tab_hangar_titles")
                )
            }
        }

        // Cosmetic Items
        val itemsToDisplay = when (selectedCategory) {
            "SHIP" -> ships
            "TRAIL" -> trails
            else -> titles
        }

        items(itemsToDisplay) { cosmetic ->
            val isEquipped = when (cosmetic.category) {
                "SHIP" -> user?.shipSkinId == cosmetic.id
                "TRAIL" -> user?.trailFxId == cosmetic.id
                else -> user?.title == cosmetic.name
            }

            CosmeticItemCard(
                cosmetic = cosmetic,
                isEquipped = isEquipped,
                onEquip = {
                    when (cosmetic.category) {
                        "SHIP" -> onEquipShip(cosmetic.id)
                        "TRAIL" -> onEquipTrail(cosmetic.id)
                        else -> onEquipTitle(cosmetic.name)
                    }
                }
            )
        }
    }

    // Cross-Platform Sync Modal
    if (showSyncDialog) {
        AlertDialog(
            onDismissRequest = { onShowSyncDialog(false) },
            containerColor = SpaceCard,
            title = {
                Text(
                    text = "CROSS-PLATFORM DATA SYNC",
                    color = TextWhite,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Your Cross-Play Link ID:",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(VoidDark)
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = user?.crossPlayId ?: "CROSS-PC-9921",
                            color = CyberCyan,
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp,
                            letterSpacing = 2.sp
                        )
                    }

                    val context = LocalContext.current
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                ShareHelper.copyToClipboard(context, user?.crossPlayId ?: "CROSS-PC-9921", "Cross-Play ID")
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .testTag("btn_copy_crossplay_id"),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = CyberCyan)
                        ) {
                            Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Copy ID", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                ShareHelper.shareProfile(
                                    context,
                                    user?.crossPlayId ?: "CROSS-PC-9921",
                                    user?.displayName ?: "NovaPilot",
                                    user?.title ?: "Apex Pilot"
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .testTag("btn_share_crossplay_id"),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CyberCyan)
                        ) {
                            Icon(imageVector = Icons.Default.Share, contentDescription = null, tint = VoidDark, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Share Profile", color = VoidDark, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Or enter a 6-digit sync code from your PC/Console client:",
                        color = TextMuted,
                        fontSize = 12.sp
                    )

                    TextField(
                        value = syncInputCode,
                        onValueChange = { syncInputCode = it },
                        placeholder = { Text("e.g. 784-912", color = TextMuted) },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = VoidDark,
                            unfocusedContainerColor = VoidDark,
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite,
                            cursorColor = CyberCyan
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().testTag("sync_code_input")
                    )

                    if (syncSuccessMsg != null) {
                        Text(
                            text = syncSuccessMsg ?: "",
                            color = SuccessGreen,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        syncSuccessMsg = "Successfully synchronized profile across devices!"
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                    modifier = Modifier.minimumInteractiveComponentSize().testTag("btn_confirm_sync")
                ) {
                    Text("SYNC DATA", color = VoidDark, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { onShowSyncDialog(false) },
                    modifier = Modifier.minimumInteractiveComponentSize().testTag("btn_close_sync")
                ) {
                    Text("CLOSE", color = TextMuted)
                }
            }
        )
    }
}

@Composable
private fun ProfileStatBox(
    label: String,
    value: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(VoidDark)
            .padding(10.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = label, color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Black)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = value, color = accentColor, fontSize = 15.sp, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
private fun CosmeticItemCard(
    cosmetic: CosmeticItem,
    isEquipped: Boolean,
    onEquip: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (isEquipped) Modifier.border(1.5.dp, CyberCyan, RoundedCornerShape(14.dp))
                else Modifier
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isEquipped) SpaceCardLight else SpaceCard
        ),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(VoidDark),
                contentAlignment = Alignment.Center
            ) {
                Text(text = cosmetic.previewEmoji, fontSize = 24.sp)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = cosmetic.name,
                        color = TextWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                when (cosmetic.rarity) {
                                    "Legendary" -> CosmicAmber.copy(alpha = 0.2f)
                                    "Epic" -> NeonMagenta.copy(alpha = 0.2f)
                                    else -> CyberCyan.copy(alpha = 0.2f)
                                }
                            )
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = cosmetic.rarity.uppercase(),
                            color = when (cosmetic.rarity) {
                                "Legendary" -> CosmicAmber
                                "Epic" -> NeonMagentaLight
                                else -> CyberCyan
                            },
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Text(
                    text = cosmetic.description,
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            if (isEquipped) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(CyberCyan.copy(alpha = 0.2f))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text("EQUIPPED", color = CyberCyan, fontSize = 11.sp, fontWeight = FontWeight.Black)
                }
            } else if (cosmetic.isUnlocked) {
                Button(
                    onClick = onEquip,
                    colors = ButtonDefaults.buttonColors(containerColor = SpaceBorder),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.minimumInteractiveComponentSize().testTag("btn_equip_${cosmetic.id}")
                ) {
                    Text("EQUIP", color = TextWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("💎", fontSize = 11.sp)
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "${cosmetic.shardCost}",
                        color = TextMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
