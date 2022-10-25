package ui.game

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import service.hw.ModuleSensorService
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import service.hw.Panel

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SortingDialog(onDismiss: () -> Unit, onPositive: () -> Unit) {

    val startSorting = mutableStateOf(false)
    val sortingProgress = mutableStateOf("Not started")
    val sortedProgress = mutableStateOf<String>("---")

    if (!startSorting.value) {
        Service.gameService.interruptGameProcess()
        Service.moduleSensorService.startSorting(object : ModuleSensorService.PanelSortingListener {
            override fun onSortingFailed() {
                sortingProgress.value = "Sorting failed try again"
                startSorting.value = false
                onDismiss()
            }

            override fun onSortingFinished(configuredPanels: List<Panel>) {
                sortingProgress.value = "Sorting finished"
                startSorting.value = false
                Service.settingsService.saveSettings()
                onDismiss()
            }

            override fun onSortingProgress(sortedPanels: Int, totalPanels: Int) {
                sortedProgress.value = "Sorted ${sortedPanels}/$totalPanels"
            }

            override fun onSortingStarted(totalPanels: Int) {
                sortingProgress.value = "Started"
            }

        })
        startSorting.value = true
    }

    AlertDialog(
        onDismissRequest = {},
        title = {
            Text(text = "Panel sorting")
        },

        text = {
            Box(modifier = Modifier.width(300.dp)) {
                Text(sortingProgress.value.toString() + "\n" + sortedProgress.value)
            }
        },
        confirmButton = {},
        dismissButton = {
            Button(
                onClick = {
                    Service.moduleSensorService.cancelSorting()
                    onDismiss()
                }
            ) {
                Text("Zpět")
            }
        }
    )
}