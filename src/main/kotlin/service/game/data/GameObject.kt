package service.game.data

data class GameObject(
    val name: String,
    val description: String,
    val type: Type,
    val rules: Rules,
) {

    companion object {
        var defaultHitPoints = 1
        var defaultMissPoints = -2
        var defaultTimeout = 60

        fun emptyGameObject(): GameObject {
            return GameObject("", "", GameObject.Type.NONE, Rules(0, 0, 2, 2, 1))
        }
    }

    var configuration: Configuration = Configuration(defaultHitPoints, defaultMissPoints, defaultTimeout)

    fun configure(config: Configuration) {
        this.configuration = config
    }

    enum class Type { NONE, CLASSIC_RANDOM_TIMEOUT }
    data class Rules(
        val maxHits: Int, val maxMisses: Int,
        val maxActivePanels: Int,
        val panelsGap: Int,
        val players: Int
    )

    data class Configuration(val hitPoints: Int, val missesPoints: Int, val timeoutSeconds: Int)


}