package utils

object AppPreferences {

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

    /**

    private fun getPreferences(context: Context): SharedPreferences? {
        return context.getSharedPreferences(APP_PREFERENCES_KEY, Context.MODE_PRIVATE)
    }

    fun setPanelSorting(context: Context, configuredPanels: List<Panel>): Boolean? {
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
        return getPreferences(context)?.edit()?.putString(PANEL_SORTING, stringBuilder.toString())
            ?.commit()
    }

    fun getPanelSorting(context: Context): List<Panel>? {
        val encodedString = getPreferences(context)?.getString(PANEL_SORTING, "") ?: ""
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
            Log.error(Log.MessageGroup.SYSTEM, e.message)
            return null
        }
        return null
    }

    fun getBrightness(context: Context): Int {
        return getPreferences(context)?.getInt(SETTINGS_BRIGHTNESS, Constants.DEFAULT_BRIGHTNESS)
            ?: Constants.DEFAULT_BRIGHTNESS
    }

    fun setBrightness(context: Context, brightness: Int): Boolean? {
        return getPreferences(context)?.edit()?.putInt(SETTINGS_BRIGHTNESS, brightness)?.commit()
    }

    fun getLoggingEnabled(context: Context): Boolean {
        return getPreferences(context)?.getBoolean(
            SETTINGS_LOGGING_ENABLED,
            Constants.DEFAULT_LOGGING_ENABLED
        )
            ?: Constants.DEFAULT_LOGGING_ENABLED
    }

    fun setLoggingEnabled(context: Context, loggingEnabled: Boolean): Boolean? {
        return getPreferences(context)?.edit()?.putBoolean(SETTINGS_LOGGING_ENABLED, loggingEnabled)
            ?.commit()
    }

    fun getThreshold(context: Context): Int {
        return getPreferences(context)?.getInt(
            SETTINGS_THRESHOLD,
            Constants.DEFAULT_THRESHOLD
        )
            ?: Constants.DEFAULT_THRESHOLD
    }

    fun setThreshold(context: Context, threshold: Int): Boolean? {
        return getPreferences(context)?.edit()?.putInt(SETTINGS_THRESHOLD, threshold)?.commit()
    }

    fun getLCDDisplayAddress(context: Context): String {
        return getPreferences(context)?.getString(
            SETTINGS_LCD_DISPLAY_ADDRESS,
            Constants.DEFAULT_LCD_DISPLAY_ADDRESS
        )
            ?: Constants.DEFAULT_LCD_DISPLAY_ADDRESS
    }

    fun setLCDDisplayAddress(context: Context, address: String): Boolean? {
        return getPreferences(context)?.edit()?.putString(SETTINGS_LCD_DISPLAY_ADDRESS, address)
            ?.commit()
    }

    fun getLCDDisplayPort(context: Context): Int {
        return getPreferences(context)?.getInt(
            SETTINGS_LCD_DISPLAY_PORT,
            Constants.DEFAULT_LCD_DISPLAY_PORT
        )
            ?: Constants.DEFAULT_LCD_DISPLAY_PORT
    }

    fun setLCDDisplayPort(context: Context, port: Int): Boolean? {
        return getPreferences(context)?.edit()?.putInt(SETTINGS_LCD_DISPLAY_PORT, port)?.commit()
    }

    fun getLCDDisplayBrightness(context: Context): Int {
        return getPreferences(context)?.getInt(
            SETTINGS_LCD_DISPLAY_BRIGHTNESS,
            Constants.DEFAULT_LCD_DISPLAY_BRIGHTNESS
        )
            ?: Constants.DEFAULT_LCD_DISPLAY_BRIGHTNESS
    }

    fun setLCDDisplayBrightness(context: Context, brightness: Int): Boolean? {
        return getPreferences(context)?.edit()?.putInt(SETTINGS_LCD_DISPLAY_BRIGHTNESS, brightness)
            ?.commit()
    }

    fun getLCDDisplaySize(context: Context): ExternalDisplayService.DisplaySize {
        return ExternalDisplayService.DisplaySize.valueOf(
            getPreferences(context)?.getString(
                SETTINGS_LCD_DISPLAY_SIZE,
                Constants.DEFAULT_LCD_DISPLAY_SIZE.name
            ) ?: Constants.DEFAULT_LCD_DISPLAY_SIZE.name
        )
    }

    fun setLCDDisplaySize(
        context: Context,
        externalDisplaySize: ExternalDisplayService.DisplaySize
    ): Boolean? {
        return getPreferences(context)?.edit()
            ?.putString(SETTINGS_LCD_DISPLAY_SIZE, externalDisplaySize.name)?.commit()
    }
    */
}