package service.game.data

class GameLibrary {

    companion object {
        fun all(): Collection<GameObject> {
            return listOf(
                GameObject(
                    "Zasáhni co nejvíc",
                    "description",
                    GameObject.Type.CLASSIC_RANDOM_TIMEOUT,
                    GameObject.Rules(0, 0, 2, 2, 1)
                )
            )
        }

    }
}
