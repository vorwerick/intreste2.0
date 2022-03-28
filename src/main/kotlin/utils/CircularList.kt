package utils

class CircularList {

    val activeIndexes = mutableListOf<Int>()

    var indexes = 0

    fun addActiveIndex(gap: Int){
        val possibleIndexes = mutableListOf<Int>()
        for(i in 0 until indexes){
            if(!activeIndexes.contains(i)){

            }
        }
        activeIndexes.forEach { activeIndex ->

        }
    }

    fun removeActiveIndex(){

    }
}

class CircularNumber(val cap: Int){



    fun circularAddition(value: Int, cap: Int, addition: Int): Int{
        var x = value + addition
        if(x > cap){
            return x - cap
        }
        return x
    }

    fun circularSubtraction(value: Int, cap: Int, subtraction: Int): Int{
        var x = value - subtraction
        if(x < cap){
            return x - cap
        }
        return x
    }
}