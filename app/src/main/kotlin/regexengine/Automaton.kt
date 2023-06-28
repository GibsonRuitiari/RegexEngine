package regexengine

/*a transition must have an initial state as the input,
 a symbol associated with the initial state and the combination of
 which will lead to the output/resulting state
 ag a---->b */
data class Transition(val from:Int, val label:String,val to:Int)
const val EpsilonLabel="eof"
data class Automaton(val states:List<Int> = emptyList(),
                     val initialStates:Set<Int> = emptySet(),
                     val finalStates:Set<Int> = emptySet(),
                     val transitions:Set<Transition> = emptySet()
) {
    companion object{
        fun buildAutomatonFromAllAlphabetsOfALanguage(alphabet:Set<Char>){
            buildAutomatonFromSymbolSet(alphabet)
        }
        fun buildAutomatonFromEpsilon():Automaton= buildAutomatonFromWord("")
        fun buildAutomatonFromWord(word:String):Automaton{
            var initialState=0
            val transitions = mutableSetOf<Transition>()
            val states= mutableListOf<Int>(initialState)
            val initialStates = mutableSetOf<Int>(initialState)
            word.toCharArray().forEach {symbol->
                val nextState = initialState+1
                transitions.add(Transition(from = initialState, label = symbol.toString(), to = nextState))
                states.add(nextState)
                initialState= nextState
            }
            return Automaton(states=states, initialStates =initialStates,
                finalStates = setOf(initialState), transitions = transitions)
        }
        fun buildAutomatonFromSymbol(symbol:Char):Automaton = buildAutomatonFromSymbolSet(setOf(symbol))
        fun buildAutomatonFromSymbolSet(symbols:Set<Char>):Automaton{
            val initialState=0
            val finalState=1
            val transitions = symbols.map {symbol->
                Transition(from=initialState, label =symbol.toString(), to = finalState)
            }.toMutableSet()
           return Automaton(states = listOf(initialState,finalState),
                initialStates = setOf(initialState),
                finalStates = setOf(finalState),
                transitions = transitions
            )
        }
    }

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

