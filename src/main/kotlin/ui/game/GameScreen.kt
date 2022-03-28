package ui.game

import androidx.compose.foundation.background
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import service.game.data.GameObject

@Composable
fun GameScreen() {

    val openSortingDialog = remember { mutableStateOf(false) }
    val openInfoDialog = remember { mutableStateOf(false) }
    val onSettingsDialog = remember { mutableStateOf(false) }
    val onConfigureGameDialog = remember { mutableStateOf(false) }



    Scaffold(
        modifier = Modifier
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colors.primary,
                        MaterialTheme.colors.primaryVariant
                    )
                )
            ),
        bottomBar = {
            GameBottomBar(
                onRestartGame = {
                    Service.gameService.startGameProcess(
                        GameObject(
                            "Zasahni co nejvic",
                            "",
                            GameObject.Type.CLASSIC_RANDOM_TIMEOUT, GameObject.Rules(0, 0, 2, 2, 1),
                        )
                    )
                },
                onSettings = {
                    onSettingsDialog.value = true
                },
                onSortPanels = {
                    openSortingDialog.value = true
                },
                onInfo = {
                    openInfoDialog.value = true
                },
                onConfigure = {
                onConfigureGameDialog.value = true
                })
        },
        content = {
            GameScreenContent(openSortingDialog, openInfoDialog, onSettingsDialog, onConfigureGameDialog)
        }
    )
}


