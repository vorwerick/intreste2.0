package service.remote


class BluetoothApi {

    object Endpoint {
        const val CONNECT = "connect"
        const val DISCONNECT = "disconnect"
        const val PING = "ping" // both direction
        const val GET_GAMES = "get_games"
        const val SELECT_GAME = "select_game"
        const val PREPARE_GAME = "prepare_game"
        const val GET_GAME_STATUS = "get_game_status"
        const val CANCEL_GAME = "cancel_game"
        const val RESTART_GAME = "restart_game"
        const val PANEL_SORTING = "panel_sorting"
        const val GET_CURRENT_GAME = "get_current_game"
    }
}