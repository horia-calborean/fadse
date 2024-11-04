package ro.ulbsibiu.fadse.simulationIO;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import ro.ulbsibiu.fadse.simulationIO.document.InputDocument;
import ro.ulbsibiu.fadse.simulationIO.parameters.simulator.SimulatorParameter;
import ro.ulbsibiu.fadse.extended.problems.simulators.gap.GapLogger;

public class Individual implements Cloneable, Serializable {

    private SimulatorParameter[] parameters;
    private String benchmark;
    private LinkedList<Objective> objectives;
    private int offspringCount;
    private SimulationIO environment;
    private boolean feasible = true;

    public Individual(SimulationIO env, String benchmark) {
        this.benchmark = benchmark;
        parameters = new SimulatorParameter[env.getDesignSpaceDocument().getParameters().length];
        objectives = new LinkedList<Objective>();
        for (Objective o : env.getDesignSpaceDocument().getObjectives().values()) {
            objectives.add(new Objective(o.getName(), o.getType(), o.getUnit(), o.getDescription(), o.isMaximize()));
        }
        InputDocument init = env.getDesignSpaceDocument();
        int i = 0;
        for (SimulatorParameter p : init.getParameters()) {
            try {
                parameters[i] = ((SimulatorParameter) p.clone());
            } catch (CloneNotSupportedException ex) {
                Logger.getLogger(Individual.class.getName()).log(Level.SEVERE, null, ex);
            }
            i++;
        }
        this.environment = env;
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

    public SimulatorParameter[] getParameters() {
        return parameters;
    }

    public List<Objective> getObjectives() {
        return objectives;
    }

    public void setObjectives(LinkedList<Objective> objectives) {
        this.objectives = objectives;
    }

    public void setParameters(SimulatorParameter[] parameters) {
        this.parameters = parameters;
    }

    @Override
    public Individual clone() throws CloneNotSupportedException {
        Individual newInd = new Individual(environment, benchmark);
        SimulatorParameter[] newParameters = new SimulatorParameter[environment.getDesignSpaceDocument().getParameters().length];
        LinkedList<Objective> newObjectives = new LinkedList<Objective>();
        Objective newObjective;
        for (Objective o : objectives) {
            newObjective = new Objective(o.getName(), o.getType(), o.getUnit(), o.getDescription(), o.isMaximize());
            newObjective.setValue(o.getValue());
            newObjectives.add(newObjective);
        }
        SimulatorParameter temp;
        int i = 0;
        for (SimulatorParameter p : parameters) {
            temp = (SimulatorParameter) p.clone();
            newParameters[i] = (temp);
            i++;
        }
        newInd.setObjectives(newObjectives);
        newInd.setParameters(newParameters);
        newInd.setBenchmark(benchmark);
        newInd.setFeasible(feasible);
        return newInd;
    }

    public boolean isDominatedBy(Individual offspring) {
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
        /*System.out.println("domination count:" + dominationCount);
        System.out.println("is domianted: "+ (dominationCount==objectives.size()));*/
        return dominationCount >= 1 && (dominationCount + equalCount == objectives.size());
    }

    @Override
    public String toString() {
        LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();

        for (int i = 0; i < parameters.length; i++) {
            map.put("P" + i, parameters[i].toString());
        }
        map.put("B", benchmark);
        for (int i = 0; i < objectives.size(); i++) {
            map.put("O" + i, objectives.get(i).toString());
        }

        return map.toString();

        /* return "[o" + objectives.toString() + " # " + parameters.toString() + "]"; */
    }

    public SimulationIO getEnvironment() {
        return environment;
    }

    private void markAsInfeasible() {
        if (feasible) {
//            System.out.println("## INDIVIDUAL " + this.toString() + " IS INFEASIBLE: unknown reason");
        }
        feasible = false;
    }

    public void markAsInfeasibleAndSetBadValuesForObjectives(String reason) {
        this.markAsInfeasible(reason);
        this.setObjectives(new LinkedList<Objective>());
        for (Objective o : environment.getDesignSpaceDocument().getObjectives().values()) {
            this.getObjectives().add(new Objective(o.getName(), o.getType(), o.getUnit(), o.getDescription(), o.isMaximize()));
        }
        this.setBadValuesForObjectives();

    }

    private void markAsInfeasible(String reason) {
        if (feasible) {
//            System.out.println("## INDIVIDUAL " + this + " IS INFEASIBLE: " + reason);
            GapLogger.logInfeasible(this, reason);
        }
        feasible = false;
    }

    public boolean isFeasible() {
        return feasible;
    }

    public void setFeasible(boolean feasible) {
        this.feasible = feasible;
    }

    private void setBadValuesForObjectives() {
        for (Objective o : objectives) {
            if (o.isMaximize()) {
                o.setValue(Double.MIN_VALUE);
            } else {
                o.setValue(Double.MAX_VALUE);
            }
        }
    }

    public String getBenchmark() {
        return benchmark;
    }

    public void setBenchmark(String benchmark) {
        this.benchmark = benchmark;
    }
}
