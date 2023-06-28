package regexengine

import java.nio.file.attribute.FileStoreAttributeView

object AutomatonOperations {
    // makes a copy of an automaton by renaming its states
    fun Automaton.remapAutomaton(statesToUse:List<Int>):Automaton{
        val statesCount = statesToUse.count()
        return Automaton(
        states.map {state-> statesCount.plus(state)},
        initialStates.map {state->statesCount.plus(state)}.toMutableSet(),
        finalStates.map {state->statesCount.plus(state)}.toMutableSet(),
        transitions.map {transition->
            Transition(transition.from.plus(statesCount),
                    transition.label,
                    transition.to.plus(statesCount))
        }.toMutableSet())
    }
    fun joinAutomatons(first: Automaton,second: Automaton):Automaton{
        val secondAutomaton = second.remapAutomaton(first.states)

        return Automaton(states = first.states.plus(secondAutomaton.states),
            initialStates = first.initialStates.plus(secondAutomaton.initialStates),
            finalStates = first.finalStates.plus(secondAutomaton.finalStates),
            transitions = first.transitions.plus(secondAutomaton.transitions))
    }
    fun kleeneStarOperation(automaton: Automaton):Automaton{
        val initial = automaton.states.count()
        val initialStates = mutableListOf(initial)
        val transitions = mutableListOf<Transition>()
        automaton.initialStates.forEach {state->
            //
            transitions.add(Transition(state,"eof",initial))
        }
        automaton.finalStates.forEach {state->
            transitions.add(Transition(state,"eof",initial))
        }
        return Automaton(automaton.states.plus(initialStates),
            initialStates = initialStates.toSet(),
            finalStates = automaton.finalStates.plus(initialStates),
            transitions = automaton.transitions.plus(transitions))
    }
    fun kNewStates(k:Int, states:List<Int>) = states.count()..k

    fun expandAutomaton(automaton: Automaton):Automaton{
        val multiSymbolTransitions = automaton.transitions.filter {it.label.length>1 }
        val newStates = automaton.states.toMutableList()
        var newTransitions = automaton.transitions.minus(multiSymbolTransitions.toSet())
            .toSet()
        multiSymbolTransitions.forEach {transition->
            val wordLength = transition.label.length
            val intermediateStates = kNewStates(wordLength-1,newStates).toList()
            val statesSequence = mutableListOf(transition.from)
            statesSequence+=intermediateStates
            statesSequence+=transition.to

            newStates.addAll(intermediateStates)
            val paths=(0 until statesSequence.count())
                .map {
                    statesSequence[it]
                    Transition(statesSequence[it],transition.label[it].toString(),
                        statesSequence[it+1])
                }
            newTransitions=newTransitions.union(paths)
        }
        return Automaton(newStates,automaton.initialStates,
            automaton.finalStates,newTransitions)
    }
    fun determinizeAutomaton(automaton: Automaton){
        val _automaton= expandAutomaton(removeEpsilonTransitionsFromAutomaton(automaton))
        val subsetStates = _automaton.initialStates.toMutableSet()
        val dfsaTransitions = mutableMapOf<Pair<Int,Char>,Int>()
        val stateTransitionsMap =_automaton.transitions
            .groupBy(keySelector = {it.from},
                valueTransform = {it.label to it.to})
        subsetStates.forEach {

        }
    }
    fun unionOperation(first:Automaton,second: Automaton):Automaton{
        val remappedSecondAutomaton = second.remapAutomaton(first.states)
        return Automaton(states=first.states.plus(second.states),
            initialStates = first.initialStates.plus(second.initialStates),
            finalStates = first.finalStates.plus(second.finalStates),
            transitions = first.transitions.plus(second.transitions))
    }
    fun optionalOperation(automaton: Automaton):Automaton{
        val state = mutableSetOf<Int>(automaton.states.count())
        return Automaton(automaton.states.union(state).toList(),
        automaton.initialStates.union(state),
        automaton.finalStates.union(state),
        automaton.transitions)
    }
    fun removeEpsilonTransitionsFromAutomaton(automaton: Automaton):Automaton{
        val initial = automaton.initialStates.flatMap {initialState->
            automaton.getEpsilonTransitionOfGivenState(initialState)
        }
        val transitions= automaton.transitions.filter { it.label!="eof" }
            .flatMap {transition->
                automaton.getEpsilonTransitionOfGivenState(transition.to)
                    .map {closure->
                        Transition(transition.from
                        ,transition.label,closure)}
            }
        return Automaton(automaton.states, initialStates = initial.toSet(),
            finalStates = automaton.finalStates,transitions.toSet())
    }
    fun plusOperation(automaton: Automaton):Automaton{
        val initialState = automaton.states.count()
        val initialStates = arrayOf(initialState)
        val newTransitions = mutableListOf<Transition>()
        automaton.initialStates.mapTo(newTransitions) { state-> Transition(initialState,"eof",state) }
        automaton.finalStates.mapTo(newTransitions) {state->Transition(state,"eof",initialState)  }
        return Automaton(automaton.states.plus(initialStates),
            initialStates = initialStates.toSet(), finalStates = automaton.finalStates,
            transitions = automaton.transitions.plus(newTransitions))
    }

    fun concatenateAutomatons(first:Automaton,second:Automaton):Automaton{
        val firstAutomatonFinalState = first.finalStates
        val secondAutomaton = second.remapAutomaton(first.states)
        val resultingAutomatonInitialStates=if(first.initialStates.firstOrNull { it in firstAutomatonFinalState } == null){
            first.initialStates
        }else{ first.initialStates.union(secondAutomaton.initialStates) }
        val transitions = first.transitions.union(secondAutomaton.transitions).toMutableList()
        first.transitions
            .filter {transition-> first.finalStates.contains(transition.to)}
            .forEach {transition ->
            secondAutomaton.initialStates.forEach {state->
                transitions.add(Transition(from = transition.from,to=state, label = transition.label))
            }
        }
        return Automaton(states = first.states.union(secondAutomaton.states).toList(),
            initialStates = resultingAutomatonInitialStates,
            finalStates = secondAutomaton.finalStates, transitions = transitions.toSet())
    }

}