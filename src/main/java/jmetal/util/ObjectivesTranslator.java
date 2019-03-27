package jmetal.util;

import jmetal.base.Solution;
import jmetal.base.SolutionSet;

import java.util.HashMap;
import java.util.List;

public class ObjectivesTranslator {


    private final List< IObjectiveTranslationFunction> translationFunctions;

    public ObjectivesTranslator(List<IObjectiveTranslationFunction> translationFunctions){
        this.translationFunctions = translationFunctions;
    }

    public SolutionSet translate(SolutionSet initial) {
        SolutionSet translatedSet = new SolutionSet(initial.size());

        for(int i = 0;i<initial.size();i++){
            Solution current = initial.get(i);
            Solution trans = new Solution(current);
            for (int objNr = 0; objNr < current.numberOfObjectives();objNr++){
                trans.setObjective(objNr, translationFunctions.get(objNr).translate(current.getObjective(objNr)));
            }
            translatedSet.add(trans);
        }


        return translatedSet;
    }
}
