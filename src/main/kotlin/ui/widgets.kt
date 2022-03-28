package ui

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import service.game.GameProcessListener
import service.game.data.GameObject
import service.game.data.GameStatus
import service.hw.ModuleSensorService
import service.hw.Panel
import utils.mainThread

@Preview
@Composable
fun HomeScreen() {

    var isSorting by remember { mutableStateOf(false) }
    var appliactionState by remember { mutableStateOf("game not ready") }
    var gameScore by remember { mutableStateOf("0 hits 0 misses") }

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
            BottomAppBar {
                Button(
                    onClick = { /* ... */ },
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
                        Icons.Filled.Favorite,
                        contentDescription = "Favorite",
                        modifier = Modifier.size(ButtonDefaults.IconSize)
                    )
                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                    Text("Like")
                }
            }
        },
        drawerContent = { /*...*/ },
        topBar = { /*...*/ },
        content = {
            Column {
                Row {
                    Button(
                        onClick = {
                            if (isSorting) {
                                Service.moduleSensorService.cancelSorting()
                                isSorting = false
                            } else {
                                Service.moduleSensorService.startSorting(object :
                                    ModuleSensorService.PanelSortingListener {
                                    override fun onSortingFailed() {
                                        isSorting = false
                                    }

                                    override fun onSortingFinished(configuredPanels: List<Panel>) {
                                        isSorting = false
                                    }

                                    override fun onSortingProgress(sortedPanels: Int, totalPanels: Int) {
                                        isSorting = true
                                    }

                                    override fun onSortingStarted(totalPanels: Int) {
                                        isSorting = true
                                    }
                                })

                            }
                        },
                    ) {
                        var s = if (isSorting) "Sort panels" else "Stop sorting"
                        Text(s)
                    }
                    Text(appliactionState)
                }
                Row {
                    Button(
                        onClick = {
                            Service.gameService.startGameProcess(
                                GameObject(
                                    "Zasahni co nejvic",
                                    "",
                                    GameObject.Type.CLASSIC_RANDOM_TIMEOUT, GameObject.Rules(0, 0, 2, 2, 1),
                                )
                            )
                            Service.gameService.setGameListener(object : GameProcessListener {
                                override fun onGamePrepared(gameObject: GameObject) {
                                    mainThread {
                                        appliactionState = "game prepared"
                                    }
                                }

                                override fun onGameStartedUpProgress(secondsPassed: Int, readyCountdown: Int) {
                                    mainThread { appliactionState = "game started up" }

                                }

                                override fun onGameStarted(gameObject: GameObject) {
                                    mainThread { appliactionState = "game started" }
                                }

                                override fun onGameHit(gameStatus: GameStatus, gameObject: GameObject) {
                                    mainThread {
                                        gameScore =
                                            "${gameStatus.timeout}s ${gameStatus.hitCount} hits ${gameStatus.missCount} misses"
                                    }
                                }

                                override fun onGameMiss(gameStatus: GameStatus, gameObject: GameObject) {
                                    mainThread {
                                        gameScore =
                                            "${gameStatus.timeout}s ${gameStatus.hitCount} hits ${gameStatus.missCount} misses"
                                    }
                                }

                                override fun onGameTimeStep(gameStatus: GameStatus, gameObject: GameObject) {
                                    mainThread {
                                        gameScore =
                                            "${gameStatus.timeout}s ${gameStatus.hitCount} hits ${gameStatus.missCount} misses"
                                    }
                                }

                                override fun onGameFinished(gameObject: GameObject, gameStatus: GameStatus) {
                                    mainThread { appliactionState = "game finished" }
                                }

                            })
                        },
                    ) {
                        Text("Start")
                    }
                    Text(appliactionState)
                }


                Text(gameScore)
            }
        }
    )
}


@Composable
fun GameList(games: List<String>) {
    Column {
        games.forEach { game ->
            MyButton(game)
        }
    }
}


@Composable
fun MyButton(name: String) {
    Button(
        colors = ButtonDefaults.buttonColors(backgroundColor = androidx.compose.ui.graphics.Color.Green),
        onClick = { /* ... */ },
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
            Icons.Filled.Favorite,
            contentDescription = "Favorite",
            modifier = Modifier.size(ButtonDefaults.IconSize)
        )
        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
        Text(name)
    }
}