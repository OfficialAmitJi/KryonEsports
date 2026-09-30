package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Report
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChatMessageEntity
import com.example.data.model.UserProfileEntity
import com.example.ui.components.CrossPlayPlatformBadge
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun ChatScreen(
    user: UserProfileEntity?,
    messages: List<ChatMessageEntity>,
    selectedChannel: String,
    reportToastMessage: String?,
    onSelectChannel: (String) -> Unit,
    onSendMessage: (String) -> Unit,
    onReportMessage: (ChatMessageEntity) -> Unit
) {
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(VoidDark)
    ) {
        // Channel Tabs
        TabRow(
            selectedTabIndex = when (selectedChannel) {
                "GLOBAL" -> 0
                "ARENA" -> 1
                "CLAN" -> 2
                else -> 3
            },
            containerColor = SpaceCard,
            contentColor = CyberCyan,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp).clip(RoundedCornerShape(10.dp))
        ) {
            val channels = listOf("GLOBAL", "ARENA", "CLAN", "DIRECT")
            channels.forEachIndexed { index, ch ->
                Tab(
                    selected = selectedChannel == ch,
                    onClick = { onSelectChannel(ch) },
                    text = { Text("#$ch", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("chat_tab_$ch")
                )
            }
        }

        // Moderation Safety Notice Banner
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(SpaceCard)
                .border(1.dp, SpaceBorder, RoundedCornerShape(8.dp))
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Shield,
                contentDescription = null,
                tint = EmeraldMatrix,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Automated chat moderation active. Tap flag on any message to report violation.",
                color = TextMuted,
                fontSize = 10.sp
            )
        }

        // Report Toast banner if report was just submitted
        if (reportToastMessage != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(SuccessGreen.copy(alpha = 0.2f))
                    .border(1.dp, SuccessGreen, RoundedCornerShape(8.dp))
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = reportToastMessage,
                    color = SuccessGreen,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Messages List
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(messages) { msg ->
                ChatMessageItem(
                    message = msg,
                    isSelf = msg.senderId == (user?.userId ?: ""),
                    onReport = { onReportMessage(msg) }
                )
            }
        }

        // Quick Tactical Reaction Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val quickReactions = listOf("🔥", "🚀", "👑", "💥", "🛡️", "⚡")
            quickReactions.forEach { emoji ->
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(SpaceCard)
                        .border(1.dp, SpaceBorder, CircleShape)
                        .clickable { onSendMessage(emoji) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(emoji, fontSize = 14.sp)
                }
            }
        }

        // Input Box
        Surface(
            color = SpaceCard,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 70.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = {
                        Text(
                            "Message #${selectedChannel.lowercase()}...",
                            color = TextMuted,
                            fontSize = 13.sp
                        )
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = VoidDark,
                        unfocusedContainerColor = VoidDark,
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        cursorColor = CyberCyan,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("chat_input_field")
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        if (inputText.isNotBlank()) {
                            onSendMessage(inputText)
                            inputText = ""
                        }
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(CyberCyan)
                        .minimumInteractiveComponentSize()
                        .testTag("chat_send_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = VoidDark,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ChatMessageItem(
    message: ChatMessageEntity,
    isSelf: Boolean,
    onReport: () -> Unit
) {
    if (message.isSystem) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(SpaceCardLight)
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "⚡ SYSTEM: ${message.text}",
                color = CyberCyan,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
        return
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelf) SpaceCardLight else SpaceCard
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(message.avatarEmoji, fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = message.senderName,
                        color = if (isSelf) CyberCyan else TextWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    CrossPlayPlatformBadge(message.senderPlatform)
                }

                // Report button
                if (!isSelf) {
                    IconButton(
                        onClick = onReport,
                        modifier = Modifier
                            .size(24.dp)
                            .minimumInteractiveComponentSize()
                            .testTag("report_msg_${message.messageId}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Flag,
                            contentDescription = "Report message",
                            tint = TextMuted,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = message.text,
                color = TextWhite,
                fontSize = 13.sp
            )
        }
    }
}
