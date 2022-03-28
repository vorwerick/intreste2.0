package ui.game

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import service.hw.ModuleSensorService
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import service.game.data.GameObject
import service.hw.Panel

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ConfigureDialog(onDismiss: () -> Unit, onPositive: () -> Unit) {

    val timeout = rememberSaveable { mutableStateOf<String>(GameObject.defaultTimeout.toString()) }
    val missPoints = rememberSaveable { mutableStateOf<String>(GameObject.defaultMissPoints.toString()) }
    val hitPoints = rememberSaveable { mutableStateOf<String>(GameObject.defaultHitPoints.toString()) }




    AlertDialog(
        onDismissRequest = {},
        title = {
            Text(text = "Configure Game")
        },

        text = {
            Box(modifier = Modifier.width(300.dp)) {
                Column {
                    TextField(
                        value = hitPoints.value.toString(),
                        onValueChange = {
                            hitPoints.value = it
                        },
                        label = { Text("Bodů za zásah") }
                    )
                    TextField(
                        value = missPoints.value.toString(),
                        onValueChange = {
                            missPoints.value = it
                        },
                        label = { Text("Bodů za minutí") }
                    )
                    TextField(
                        value = timeout.value.toString(),
                        onValueChange = {
                            timeout.value = it
                        },
                        label = { Text("Časomíra") }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    GameObject.defaultHitPoints = hitPoints.value.toIntOrNull() ?: 1
                    GameObject.defaultMissPoints = missPoints.value.toIntOrNull() ?: -2
                    GameObject.defaultTimeout = timeout.value.toIntOrNull() ?: 60
                    onPositive()
                }
            ) {
                Text("Uložit")
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss
            ) {
                Text("Zpět")
            }
        }
    )
}