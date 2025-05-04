package core.model.individual;

import core.model.objectives.Objective;
import input.model.InputData;
import input.model.setup.CommonSetupParameters;
import input.ports.parameter.problem.ProblemParameter;

import java.io.Serializable;
import java.util.*;

@SuppressWarnings("unchecked cast")
public class FadseIndividual implements Cloneable, Serializable {
    protected ProblemParameter<?>[] parameters;
    protected String selectedBenchmark;
    protected LinkedList<Objective> objectives;
    protected int offspringCount;
    protected InputData inputData;
    protected boolean feasible;

    public FadseIndividual(InputData inputData, String selectedBenchmark) {
        this.selectedBenchmark = selectedBenchmark;

        ProblemParameter<?>[] designVariables = (ProblemParameter<?>[]) inputData.get(CommonSetupParameters.PARAMETERS);
        parameters = new ProblemParameter<?>[designVariables.length];

        for (int i = 0; i < designVariables.length; i++) {
            parameters[i] = designVariables[i].clone();
        }

        Map<String, Objective> mapOfObjectives = (Map<String, Objective>) inputData.get(CommonSetupParameters.OBJECTIVES);
        objectives = new LinkedList<>();

        for (Map.Entry<String, Objective> entry : mapOfObjectives.entrySet()) {
            objectives.add(entry.getValue().clone());
        }

        inputData.get(CommonSetupParameters.OBJECTIVES);
        this.inputData = inputData;
        feasible = true;
    }

    public void increaseOffspringCount() {
        offspringCount = offspringCount + 1;
    }

    public int getOffspringCount() {
        return offspringCount;
    }

    public void setOffspringCount(int offspringCount) {
        this.offspringCount = offspringCount;
    }

    public ProblemParameter<?>[] getParameters() {
        return parameters;
    }

    public List<Objective> getObjectives() {
        return objectives;
    }

    public void setObjectives(LinkedList<Objective> objectives) {
        this.objectives = objectives;
    }

    public void setParameters(ProblemParameter<?>[] parameters) {
        this.parameters = parameters;
    }

    @Override
    public FadseIndividual clone() throws CloneNotSupportedException {
        FadseIndividual newInd = new FadseIndividual(inputData, selectedBenchmark);
        ProblemParameter<?>[] newParameters = new ProblemParameter<?>[parameters.length];
        LinkedList<Objective> newObjectives = new LinkedList<>();
        Objective newObjective;
        for (Objective obj : objectives) {
            newObjective = obj.clone();
            newObjective.setValue(obj.getValue());
            newObjectives.add(newObjective);
        }
        ProblemParameter<?> temp;
        int i = 0;
        for (ProblemParameter<?> p : parameters) {
            temp = p.clone();
            newParameters[i] = (temp);
            i++;
        }
        newInd.setObjectives(newObjectives);
        newInd.setParameters(newParameters);
        newInd.setSelectedBenchmark(selectedBenchmark);
        newInd.setFeasible(feasible);
        return newInd;
    }

    public boolean isDominatedBy(FadseIndividual offspring) {
        int dominationCount = 0;
        int equalCount = 0;
        for (int i = 0; i < objectives.size(); i++) {
            if (offspring.getObjectives().get(i).isBetter(objectives.get(i))) {//the offspring IS better
                dominationCount = dominationCount + 1;
            }
            if (objectives.get(i).getValue() == offspring.getObjectives().get(i).getValue()) {//IS equal
                equalCount = equalCount + 1;
            }
        }

        return dominationCount >= 1 && (dominationCount + equalCount == objectives.size());
    }

    @Override
    public String toString() {
        LinkedHashMap<String, String> map = new LinkedHashMap<>();

        for (int i = 0; i < parameters.length; i++) {
            map.put("P" + i, parameters[i].toString());
        }
        map.put("B", selectedBenchmark);
        for (int i = 0; i < objectives.size(); i++) {
            map.put("O" + i, objectives.get(i).toString());
        }

        return map.toString();
    }

    public InputData getInputData() {
        return inputData;
    }

    public void setBadValuesForObjectives() {
        for (Objective o : objectives) {
            if (!o.isMinimized()) {
                o.setValue(Double.MIN_VALUE);
            } else {
                o.setValue(Double.MAX_VALUE);
            }
        }

    }

    public boolean isFeasible() {
        return feasible;
    }

    public void setFeasible(boolean feasible) {
        this.feasible = feasible;
    }

    public String getSelectedBenchmark() {
        return selectedBenchmark;
    }

    public void setSelectedBenchmark(String selectedBenchmark) {
        this.selectedBenchmark = selectedBenchmark;
    }
}