package ui.game

import kotlin.random.Random

class NewGameGenerator {

    companion object {
        const val MODE_RANDOM = 0
    }

    val gamePanels = 16
    val gapSize = 1

    val panelsBuffer = mutableListOf<Int>()
    val allPanels = mutableListOf<Int>()
    val disabledPanels = mutableListOf<Int>()

    fun start(mode: Int, gamePanels: Int, gapSize: Int) {
        allPanels.clear()
        repeat(gamePanels) {
            allPanels.add(it)
        }
    }

    fun nextPanel() {
        panelsBuffer.clear()
        panelsBuffer.addAll(allPanels)
        disabledPanels.clear()

        disabledPanels.forEach { disabledPanel ->
            panelsBuffer.removeIf {
                it == disabledPanel
            }
        }
        val randomPanel = panelsBuffer[Random(System.currentTimeMillis()).nextInt(0, panelsBuffer.size)]


    }

    fun removePanel() {

    }

    private fun getDisabledPanels(list: MutableList<Int>, gapSize: Int, middleIndex: Int): MutableList<Int> {
        val disabledIndexes = mutableListOf<Int>()
        disabledIndexes.add(middleIndex)
        repeat(gapSize){
            var gapIndexUp = list[middleIndex + it]
            if(gapIndexUp >= list.size){
                 gapIndexUp = list[it - 1]
            }
            disabledIndexes.add(gapIndexUp)

            var gapIndexDown = list[middleIndex - it]
            if(gapIndexUp < 0){
                gapIndexUp = list[list.size - it]
            }
            disabledIndexes.add(gapIndexUp)
        }

        return disabledIndexes
    }
}