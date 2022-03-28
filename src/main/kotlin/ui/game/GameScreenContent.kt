package ui.game

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import darkGreen
import lightGreen
import service.game.GameProcessListener
import service.game.data.GameObject
import service.game.data.GameStatus


@Composable
fun GameScreenContent(
    openSortingDialog: MutableState<Boolean>,
    openInfoDialog: MutableState<Boolean>,
    settingsDialog: MutableState<Boolean>,
    configureGameDialog: MutableState<Boolean>
) {

    var score by remember { mutableStateOf(0) }
    var hitCount by remember { mutableStateOf(0) }
    var missCount by remember { mutableStateOf(0) }
    var timeout by remember { mutableStateOf(0) }
    var timeoutTotal by remember { mutableStateOf(0) }

    fun updateValues(gameObject: GameObject, gameStatus: GameStatus) {
        score =
            ((gameStatus.hitCount * gameObject.configuration.hitPoints) + (gameStatus.missCount * gameObject.configuration.missesPoints))
        hitCount = gameStatus.hitCount
        missCount = gameStatus.missCount
        timeout = gameStatus.timeout
    }

    fun prepareValues(gameObject: GameObject) {
        score = 0
        hitCount = 0
        missCount = 0
        timeout = gameObject.configuration.timeoutSeconds
    }

    Service.gameService.setGameListener(object : GameProcessListener {
        override fun onGamePrepared(gameObject: GameObject) {
            prepareValues(gameObject)
        }

        override fun onGameStartedUpProgress(secondsPassed: Int, readyCountdown: Int) {
        }

        override fun onGameStarted(gameObject: GameObject) {
            prepareValues(gameObject)
        }

        override fun onGameHit(gameStatus: GameStatus, gameObject: GameObject) {
            updateValues(gameObject, gameStatus)
        }

        override fun onGameMiss(gameStatus: GameStatus, gameObject: GameObject) {
            updateValues(gameObject, gameStatus)
        }

        override fun onGameTimeStep(gameStatus: GameStatus, gameObject: GameObject) {
            updateValues(gameObject, gameStatus)
        }

        override fun onGameFinished(gameObject: GameObject, gameStatus: GameStatus) {
            updateValues(gameObject, gameStatus)
        }

    })




    if (openSortingDialog.value) {
        SortingDialog(onDismiss = {
            openSortingDialog.value = false
        }, onPositive = {
            openSortingDialog.value = false
        })
    }

    if (openInfoDialog.value) {
        /** InfoDialog(onDismiss = {
            openInfoDialog.value = false
        }) */
    }

    if (configureGameDialog.value) {
        ConfigureDialog(onDismiss = {
            configureGameDialog.value = false
        }, onPositive = {

            configureGameDialog.value = false
        })
    }

    if (settingsDialog.value) {
        SettingsDialog(onDismiss = {
            settingsDialog.value = false
        }, onPositive = {

            settingsDialog.value = false
        })
    }
    Column(
        modifier = Modifier
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        darkGreen,
                        lightGreen,
                    ),

                    )
            ).fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = score.toString(),
            modifier = Modifier.padding(16.dp),
            color = Color(0xffffffff),
            fontSize = 160.sp
        )
        Row {
            Box(modifier = Modifier.background(Color(0xFF009000)).padding(12.dp).width(128.dp)) {
                Text(
                    text = "$hitCount",
                    color = Color(0xffffffff),
                    fontSize = 32.sp,
                    modifier = Modifier.align(Alignment.Center),
                    fontWeight = FontWeight.W700
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Box(modifier = Modifier.background(Color(0xFFcf352e)).padding(12.dp).width(128.dp)) {
                Text(
                    text = "$missCount",
                    color = Color(0xffffffff),
                    fontSize = 32.sp,
                    modifier = Modifier.align(Alignment.Center),
                    fontWeight = FontWeight.W700
                )
            }

        }
        Row(modifier = Modifier.padding(20.dp)) {
            Box {
                Text(
                    text = "TIMEOUT",
                    modifier = Modifier.align(Alignment.Center),
                    color = Color(0xffffffff),
                    fontSize = 64.sp,
                    fontStyle = FontStyle.Italic,
                    fontWeight = FontWeight.W700
                )
            }

            Spacer(Modifier.width(16.dp))
            Box {
                Text(
                    text = (timeout).toString(),
                    modifier = Modifier.align(Alignment.Center),
                    color = Color(0xffffffff),
                    fontSize = 64.sp,
                    fontStyle = FontStyle.Italic,
                    fontWeight = FontWeight.W700
                )
            }

        }

    }
}
