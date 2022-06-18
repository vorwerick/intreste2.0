package service.repositories

import service.hw.Panel
import service.led.ExternalDisplayService
import utils.Constants
import utils.Log
import java.util.prefs.Preferences


class SettingsService {

    companion object {
        private const val APP_PREFERENCES_KEY = "intreste_preferences"
        private const val PANEL_SORTING = "panel_sorting"
        private const val SETTINGS_BRIGHTNESS = "settings_brightness"
        private const val SETTINGS_LOGGING_ENABLED = "settings_logging"
        private const val SETTINGS_THRESHOLD = "settings_threshold"
        private const val SETTINGS_LCD_DISPLAY_ADDRESS = "settings_lcd_display_address"
        private const val SETTINGS_LCD_DISPLAY_PORT = "settings_lcd_display_port"
        private const val SETTINGS_LCD_DISPLAY_BRIGHTNESS = "settings_lcd_display_brightness"
        private const val SETTINGS_LCD_DISPLAY_SIZE = "settings_lcd_display_size"

        private const val WORD_SEPARATOR = "."
        private const val PANEL_SEPARATOR = ";"
        private const val VALUE_SEPARATOR = "-"
    }

    init {
        Log.info(this.javaClass.name, "Settings service init")
    }


    var threshold: Int = Constants.DEFAULT_THRESHOLD
    var brightness: Int = 100
    var loggingEnabled: Boolean = false
    var lcdDisplayAddress: String = Constants.DEFAULT_LCD_DISPLAY_ADDRESS
    var lcdDisplayPort: Int = Constants.DEFAULT_LCD_DISPLAY_PORT
    var lcdDisplayBrightness: Int = Constants.DEFAULT_LCD_DISPLAY_BRIGHTNESS// 0-255
    var sortedPanels: List<Panel>? = null
    var externalDisplaySize: ExternalDisplayService.DisplaySize = ExternalDisplayService.DisplaySize._96x48_TYP1

    var lcdDisplayConnected = false


    fun loadSettings() {

        val preferences = Preferences.userRoot().node(APP_PREFERENCES_KEY)

        brightness = preferences.getInt(SETTINGS_BRIGHTNESS, 100)
        loggingEnabled = preferences.getBoolean(SETTINGS_LOGGING_ENABLED, false)
        threshold = preferences.getInt(SETTINGS_THRESHOLD, Constants.DEFAULT_THRESHOLD)
        lcdDisplayAddress = preferences.get(SETTINGS_LCD_DISPLAY_ADDRESS, Constants.DEFAULT_LCD_DISPLAY_ADDRESS)
        lcdDisplayPort = preferences.getInt(SETTINGS_LCD_DISPLAY_PORT, Constants.DEFAULT_LCD_DISPLAY_PORT)
        lcdDisplayBrightness =
            preferences.getInt(SETTINGS_LCD_DISPLAY_BRIGHTNESS, Constants.DEFAULT_LCD_DISPLAY_BRIGHTNESS)
        sortedPanels = getPanelSorting(preferences.get(PANEL_SORTING, ""))
        externalDisplaySize = ExternalDisplayService.DisplaySize._96x48_TYP1
    }

    fun saveSettings() {
        val preferences = Preferences.userRoot().node(APP_PREFERENCES_KEY)

        preferences.putInt(SETTINGS_BRIGHTNESS, brightness)
        preferences.putBoolean(SETTINGS_LOGGING_ENABLED, loggingEnabled)

        preferences.putInt(SETTINGS_THRESHOLD, threshold)

        preferences.put(SETTINGS_LCD_DISPLAY_ADDRESS, lcdDisplayAddress)
        preferences.putInt(SETTINGS_LCD_DISPLAY_PORT, lcdDisplayPort)
        preferences.putInt(SETTINGS_LCD_DISPLAY_BRIGHTNESS, lcdDisplayBrightness)
        preferences.put(PANEL_SORTING, setPanelSorting(sortedPanels))
        preferences.put(SETTINGS_LCD_DISPLAY_SIZE, externalDisplaySize.name)

    }

    fun setPanelSorting(configuredPanels: List<Panel>?): String {
        if (configuredPanels == null) {
            return ""
        }
        val totalPanels = configuredPanels.size
        val stringBuilder = StringBuilder()
        stringBuilder.append(PANEL_SORTING)
        stringBuilder.append(WORD_SEPARATOR)
        stringBuilder.append(totalPanels)
        stringBuilder.append(WORD_SEPARATOR)
        configuredPanels.forEachIndexed { index, panel ->
            stringBuilder.append(panel.sensorIndex.toString())
            stringBuilder.append(VALUE_SEPARATOR)
            stringBuilder.append(panel.positionIndex.toString())
            if (index < totalPanels - 1) {
                stringBuilder.append(PANEL_SEPARATOR)
            }
        }
        return stringBuilder.toString()
    }

    fun getPanelSorting(encodedString: String): List<Panel>? {
        if (encodedString.isEmpty()) {
            return null
        }

        val configuredPanels = mutableListOf<Panel>()

        try {
            val encodedArray = encodedString.split(WORD_SEPARATOR)
            val identification = encodedArray[0]
            val totalPanels = encodedArray[1].toInt(10)
            val panelData = encodedArray[2]

            val panelArray = panelData.split(PANEL_SEPARATOR)
            panelArray.forEach { panelString ->
                val panelValues = panelString.split(VALUE_SEPARATOR)
                val panel = Panel(panelValues[1].toInt(10), panelValues[0].toInt(10))
                configuredPanels.add(panel)
            }

            if (identification == PANEL_SORTING && totalPanels == configuredPanels.size) {
                return configuredPanels
            }
        } catch (e: Exception) {
            Log.error(this.javaClass.name, e.message)
            return null
        }
        return null
    }

}