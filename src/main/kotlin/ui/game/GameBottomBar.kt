package ui.game

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import darkGreen
import lightGreen
import service.app.StatusMessage
import service.app.StatusMessagingService
import kotlin.system.exitProcess

@Composable
fun GameBottomBar(
    onRestartGame: () -> Unit,
    onConfigure: () -> Unit,
    onSettings: () -> Unit,
    onSortPanels: () -> Unit,
    onInfo: () -> Unit
) {

    val currentMessageState = remember { mutableStateOf("") }
    DisposableEffect("GameBottomBar-disposable-effect") {
        val statusMessageListener = object : StatusMessagingService.StatusMessageListener {
            override fun onMessageAdded(statusMessage: StatusMessage) {
                currentMessageState.value = statusMessage.message
            }

            override fun onMessageRemoved(statusMessage: StatusMessage) {
            }

        }
        Service.statusMessagingService.addStatusMessageListener(statusMessageListener)
        onDispose {
            Service.statusMessagingService.removeStatusMessageListener(statusMessageListener)
        }
    }
    BottomAppBar(backgroundColor = lightGreen, elevation = 0.dp) {
        Button(
            colors = ButtonDefaults.buttonColors(backgroundColor = darkGreen, contentColor = Color.White),
            onClick = onRestartGame,
            // Uses ButtonDefaults.ContentPadding by default
            contentPadding = PaddingValues(
                start = 20.dp,
                top = 12.dp,
                end = 20.dp,
                bottom = 12.dp
            )
        ) {
            // Inner content including an icon and a text label
            Icon(
                Icons.Filled.Refresh,
                contentDescription = "Start/Restart game",
                modifier = Modifier.size(ButtonDefaults.IconSize)
            )
            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
            Text("Spustit hru")
        }
        Spacer(Modifier.width(12.dp))
        Button(
            colors = ButtonDefaults.buttonColors(backgroundColor = darkGreen, contentColor = Color.White),

            onClick = onConfigure,
            // Uses ButtonDefaults.ContentPadding by default
            contentPadding = PaddingValues(
                start = 20.dp,
                top = 12.dp,
                end = 20.dp,
                bottom = 12.dp
            )
        ) {
            // Inner content including an icon and a text label
            Icon(
                Icons.Filled.Star,
                contentDescription = "Configure",
                modifier = Modifier.size(ButtonDefaults.IconSize)
            )
            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
            Text("Nakonfigurovat hru")
        }
        Spacer(Modifier.width(12.dp))
        Button(
            colors = ButtonDefaults.buttonColors(backgroundColor = darkGreen, contentColor = Color.White),

            onClick = onSettings,
            // Uses ButtonDefaults.ContentPadding by default
            contentPadding = PaddingValues(
                start = 20.dp,
                top = 12.dp,
                end = 20.dp,
                bottom = 12.dp
            )
        ) {
            // Inner content including an icon and a text label
            Icon(
                Icons.Filled.Settings,
                contentDescription = "Settings",
                modifier = Modifier.size(ButtonDefaults.IconSize)
            )
            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
            Text("Nastavení")
        }
        Spacer(Modifier.width(12.dp))

        Button(
            colors = ButtonDefaults.buttonColors(backgroundColor = darkGreen, contentColor = Color.White),

            onClick = onSortPanels,
            // Uses ButtonDefaults.ContentPadding by default
            contentPadding = PaddingValues(
                start = 20.dp,
                top = 12.dp,
                end = 20.dp,
                bottom = 12.dp
            )
        ) {
            // Inner content including an icon and a text label
            Icon(
                Icons.Filled.Place,
                contentDescription = "Sort panels",
                modifier = Modifier.size(ButtonDefaults.IconSize)
            )
            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
            Text("Seřadit panely")
        }
        Spacer(Modifier.width(12.dp))

        /** Button(
        colors = ButtonDefaults.buttonColors(backgroundColor = darkGreen, contentColor = Color.White),

        onClick = onInfo,
        // Uses ButtonDefaults.ContentPadding by default
        contentPadding = PaddingValues(
        start = 20.dp,
        top = 12.dp,
        end = 20.dp,
        bottom = 12.dp
        )
        ) {
        // Inner content including an icon and a text label
        Icon(
        Icons.Filled.Info,
        contentDescription = "Info",
        modifier = Modifier.size(ButtonDefaults.IconSize)
        )
        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
        Text("Info")
        }
        Spacer(Modifier.width(12.dp)) */

        Button(
            colors = ButtonDefaults.buttonColors(backgroundColor = darkGreen, contentColor = Color.White),

            onClick = { exitProcess(0) },
            // Uses ButtonDefaults.ContentPadding by default
            contentPadding = PaddingValues(
                start = 20.dp,
                top = 12.dp,
                end = 20.dp,
                bottom = 12.dp
            )
        ) {
            // Inner content including an icon and a text label
            Icon(
                Icons.Filled.ExitToApp,
                contentDescription = "Exit",
                modifier = Modifier.size(ButtonDefaults.IconSize)
            )
            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
            Text("Vypnout aplikaci")
        }
        Spacer(Modifier.width(20.dp))
        Box {
            Text(
                text = currentMessageState.value,
                modifier = Modifier.align(Alignment.Center),
                color = Color(0xffffffff),
                fontSize = 20.sp,
                fontWeight = FontWeight.W700
            )
        }
    }
}