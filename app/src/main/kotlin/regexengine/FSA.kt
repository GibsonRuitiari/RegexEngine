package regexengine

/*a transition must have an initial state as the input,
 a symbol associated with the initial state and the combination of
 which will lead to the output/resulting state
 ag a---->b */
data class Transition(val from:Int, val label:String,val to:Int)
const val EpsilonLabel="@"
data class FSA(val states:List<Int> = emptyList(),
               val initialStates:Set<Int> = emptySet(),
               val finalStates:Set<Int> = emptySet(),
               val transitions:Set<Transition> = emptySet()
) {
    fun getLabelsOfTransitions(): Set<String> = transitions.map { transition -> transition.label }.toSet()
    fun getEpsilonTransitionOfGivenState(state: Int): Set<Int> {
        return transitions.filter { transition ->
            transition.label == EpsilonLabel && transition.from == state
        }.map { transition -> transition.to }.toSet()
    }
    fun matchWord(word:String):Boolean{
        var currentStates = initialStates.toMutableSet()
        word.toCharArray().forEach {symbol->
            val nextStates = mutableSetOf<Int>()
            currentStates.forEach {state->
                nextStates.addAll(getEpsilonTransitionOfGivenState(state))
                nextStates.addAll(getTransitionsOfGivenStateAndSymbol(state,symbol.toString()))
            }
            currentStates=nextStates
            if (currentStates.isEmpty()) return@forEach
        }

        for (finalState in finalStates){
            return currentStates.contains(finalState) || getEpsilonTransitionOfGivenStates(currentStates).contains(finalState)
        }
        return false
    }
    fun getTransitionsOfGivenStateAndSymbol(state:Int,symbol:String):Set<Int>{
        return transitions
            .filter {transition ->
           transition.from ==state &&transition.label==symbol
        }.map { transition -> transition.to}.toSet()
    }

    fun getEpsilonTransitionOfGivenStates(states: Set<Int>): Set<Int> {
        val epsilonClosures = mutableSetOf<Int>()
        for (state in states) {
            epsilonClosures.addAll(getEpsilonTransitionOfGivenState(state))
        }
        return epsilonClosures
    }

}

