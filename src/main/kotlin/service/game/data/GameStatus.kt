package service.game.data

class GameStatus(gameObject: GameObject) {

    var timeout: Int = 0
    var secondsPassed: Int = 0
    var hitCount: Int = 0
    var missCount: Int = 0
    var activePanel: Int = -1
    var hitPanelId: Int = -1
    var hitPanelIndex: Int = -1
    var hitPanelForce: Int = -1

}