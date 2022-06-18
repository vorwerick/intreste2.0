import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import service.app.StatusMessage
import service.app.StatusMessagingService
import service.game.GameService
import service.game.data.GameObject
import service.hw.ModuleSensorService
import service.hw.RaspberryInfoService
import service.led.ExternalDisplayService
import service.remote.RemoteService
import service.repositories.SettingsService
import service.serial.ModuleCommunicationService
import service.serial.protocol.Commands
import ui.HomeScreen
import ui.game.GameScreen
import utils.Log
import java.awt.Dimension
import java.awt.Toolkit

val darkGreen = Color(0xff288342)
val lightGreen = Color(0xff79a97f)


@Composable
fun App() {

    val navigationState by remember { mutableStateOf("game") }

    MaterialTheme() {

        when (navigationState) {
            "home" -> HomeScreen()
            "game" -> GameScreen()
        }

    }
}

fun main(strings : Array<String>) {

    Log.instance.initialize()

    val noRemote = true

    var listSensorFirstTime = false

    return application {
        Service.initialize()
        Service.settingsService.loadSettings()
        Service.raspberryInfoService.initialize()
        if(!noRemote){
            Service.remoteMasterService.start()
        }
        Service.moduleCommunicationService.connect()
        Service.externalDisplayService.connect(Service.settingsService.lcdDisplayAddress, Service.settingsService.lcdDisplayPort)

        GlobalScope.launch(Dispatchers.Main) {
            delay(1500)
            Service.moduleCommunicationService.listSensors()
            val sortedPanels = Service.settingsService.sortedPanels
            if (sortedPanels != null) {
                Service.moduleSensorService.loadConfiguredPanels(sortedPanels.toList())
                Log.info(this.javaClass.name, "Panel indexes found and loaded from storage")
                StatusMessage.show(
                    StatusMessage.PANEL_SORTING_LOADED,
                    "Pořadí načteno. Celkem ${sortedPanels.size} panelů.",
                    StatusMessage.Level.INFO,
                    8000L
                )
            } else {
                Log.warn(this.javaClass.name, "Panel indexes not found, please configure first!")
                StatusMessage.show(
                    StatusMessage.PANEL_SORTING_LOADED,
                    "Je potřeba nakonfigurovat panely!",
                    StatusMessage.Level.WARNING,
                    15000L
                )
            }
            Service.moduleCommunicationService.addSensorListener(
                object : ModuleCommunicationService.SensorListener {
                    override fun onSensorHit(sensorIndex: Int) {

                    }

                    override fun onListSensorIds(sensorIds: List<Int>) {
                        Log.info(this.javaClass.name, "Sensors connected! Number of panels: " + sensorIds.size)
                        if(listSensorFirstTime){
                            return
                        }
                        listSensorFirstTime = true
                        Service.moduleSensorService.setSensorsConnected(sensorIds)
                        Service.moduleCommunicationService.lightOffAllPanels()
                        Service.moduleCommunicationService.stopAllAnimations()
                        Service.moduleCommunicationService.lightUpAllPanels(Commands.PanelColor.GREEN, 1000)
                        if (sortedPanels != null) {
                            GlobalScope.launch(Dispatchers.Main) {
                                delay(3000)
                                StatusMessage.show(
                                    StatusMessage.CONNECTED_TO_MODULE,
                                    "Připojeno k modulu Intreste",
                                    StatusMessage.Level.INFO,
                                    6000L
                                )
                                if(sensorIds.size == sortedPanels.size){
                                    Service.gameService.startGameProcess(
                                        GameObject(
                                            "Zasahni co nejvic",
                                            "",
                                            GameObject.Type.CLASSIC_RANDOM_TIMEOUT, GameObject.Rules(0, 0, 2, 2, 1),
                                        )
                                    )
                                }
                            }
                        }
                    }
            })

        }
        val screenSize: Dimension = Toolkit.getDefaultToolkit().screenSize
        val width: Double = screenSize.getWidth()
        val height: Double = screenSize.getHeight()
        Window(
            onCloseRequest = ::exitApplication,
            resizable = false,
            undecorated = true,
            visible = true,
            transparent = true,
            alwaysOnTop = false,
            enabled = true,
            state = WindowState(
                placement = WindowPlacement.Fullscreen,
                isMinimized = false,
                size = DpSize(width.dp, (height-100).dp )
            )
        ) {
            App()
        }
    }
}

object Service {
    lateinit var remoteMasterService: RemoteService
    lateinit var externalDisplayService: ExternalDisplayService
    lateinit var gameService: GameService
    lateinit var moduleCommunicationService: ModuleCommunicationService
    lateinit var moduleSensorService: ModuleSensorService
    lateinit var settingsService: SettingsService
    lateinit var statusMessagingService: StatusMessagingService
    lateinit var raspberryInfoService: RaspberryInfoService

    fun initialize() {
        remoteMasterService = RemoteService()
        externalDisplayService = ExternalDisplayService()
        gameService = GameService()
        moduleCommunicationService = ModuleCommunicationService()
        moduleSensorService = ModuleSensorService()
        settingsService = SettingsService()
        statusMessagingService = StatusMessagingService()
        raspberryInfoService = RaspberryInfoService()
    }
}
