package ui.game

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import service.led.LCDDisplayListener

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SettingsDialog(onDismiss: () -> Unit, onPositive: () -> Unit) {

    val ledAddress = rememberSaveable { mutableStateOf(Service.settingsService.lcdDisplayAddress) }
    val ledPort = rememberSaveable { mutableStateOf(Service.settingsService.lcdDisplayPort.toString()) }
    val brightness = rememberSaveable { mutableStateOf(Service.settingsService.brightness) }
    val threshold = rememberSaveable { mutableStateOf(Service.settingsService.threshold) }

    val lcdIsConnected = rememberSaveable { mutableStateOf(Service.settingsService.lcdDisplayConnected) }
    DisposableEffect("Settings-disposable-effect") {
        val lcdDisplayListener = object : LCDDisplayListener {
            override fun onLCDDisplayConnected() {
                lcdIsConnected.value = true
            }

            override fun onLCDDisplayDisconnected(reason: String) {
                lcdIsConnected.value = false
            }

        }
        Service.externalDisplayService.setLCDDisplayListener(lcdDisplayListener)
        onDispose {
            Service.externalDisplayService.setLCDDisplayListener(null)
        }
    }



    AlertDialog(
        onDismissRequest = {},
        title = {
            Text(text = "Nastavení")
        },

        text = {
            Box(modifier = Modifier.width(600.dp)) {
                Column {

                    Text("Počet připojených senzorů " + Service.moduleSensorService.sensors.size.toString())
                    Text("Version Intreste ${Build.VERSION_NAME} build ${Build.VERSION_NUMBER}")

                    TextField(
                        value = ledAddress.value.toString(),
                        onValueChange = {
                            ledAddress.value = it
                        },
                        label = { Text("LED Display address") }
                    )
                    TextField(
                        value = ledPort.value.toString(),
                        onValueChange = {
                            ledPort.value = it
                        },
                        label = { Text("LED display port") }
                    )
                    Button(onClick = {
                        if (lcdIsConnected.value) {
                            Service.externalDisplayService.disconnect("")
                        } else {
                            Service.externalDisplayService.connect(
                                ledAddress.value,
                                ledPort.value.toInt()
                            )
                        }
                    }) {
                        val text = if (lcdIsConnected.value) "LED Display disconnect" else "LED Display connect"
                        Text(text)
                    }
                    Button(onClick = { Service.externalDisplayService.testDisplay() }, enabled = lcdIsConnected.value) {
                        Text("LED Display test")
                    }
                    /** Button(onClick = { Service.externalDisplayService.testDisplay() }, enabled = lcdIsConnected.value) {
                    Text("Update")
                    } */
                    /**Box {
                    Slider(1f, onValueChange = {}, Modifier.background(Color.Cyan)) //brightness

                    }
                    Box {
                    Slider(1f, onValueChange = {}) //threshold

                    } */
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    Service.settingsService.lcdDisplayAddress = ledAddress.value
                    Service.settingsService.lcdDisplayPort = ledPort.value.toInt()
                    Service.settingsService.brightness = brightness.value.toInt()
                    Service.settingsService.threshold = threshold.value.toInt()
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