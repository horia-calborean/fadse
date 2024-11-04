package ro.ulbsibiu.fadse.simulationIO.document;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import ro.ulbsibiu.fadse.simulationIO.Objective;
import ro.ulbsibiu.fadse.simulationIO.parameters.simulator.SimulatorParameter;
import ro.ulbsibiu.fadse.simulationIO.rule.Rule;
import ro.ulbsibiu.fadse.extended.base.relation.RelationTree;

public class InputDocument implements Serializable {
    protected SimulatorParameter[] parameters;
    protected SimulatorParameter[] virtualParameters;
    protected Map<String, Objective> objectives;
    protected List<Rule> rules;
    protected List<Rule> relations;
    protected String simulatorName;
    protected String simulatorType;
    protected String metaheuristicName;
    protected String metaheuristicConfigPath;
    protected String databaseIp;
    protected String databasePort;
    protected String databaseName;
    protected String databaseUser;
    protected String databasePassword;
    protected Map<String, String> simulatorParameters;
    protected LinkedList<String> benchmarks;
    protected RelationTree relationTree1;
    protected RelationTree relationTree2;
    protected String outputPath;

    public InputDocument() {
        this.simulatorParameters = new HashMap<>();
    }

    public Map<String, String> getSimulatorParameters() {
        return simulatorParameters;
    }

    public void addSimulatorParameter(String name, String value) {
        simulatorParameters.put(name, value);
    }

    public String getSimulatorParameter(String name) {
        return simulatorParameters.get(name);
    }

    public SimulatorParameter[] getParameters() {
        return parameters;
    }

    public void setParameters(SimulatorParameter[] parameters) {
        this.parameters = parameters;
    }

    public Map<String, Objective> getObjectives() {
        return objectives;
    }

    public void setObjectives(Map<String, Objective> objectives) {
        this.objectives = objectives;
    }

    public void setRules(List<Rule> rulesList) {
        rules = rulesList;
    }

    public List<Rule> getRules() {
        return rules;
    }

    public String getSimulatorType() {
        return simulatorType;
    }

    public void setSimulatorType(String simulatorType) {
        this.simulatorType = simulatorType;
    }

    public String getSimulatorName() {
        return simulatorName;
    }

    public void setSimulatorName(String simulatorName) {
        this.simulatorName = simulatorName;
    }

    public String getMetaheuristicName() {
        return metaheuristicName;
    }

    public void setMetaheuristicName(String metaheuristicName) {
        this.metaheuristicName = metaheuristicName;
    }

    public String getMetaheuristicConfigPath() {
        return metaheuristicConfigPath;
    }

    public void setMetaheuristicConfigPath(String metaheuristicConfigPath) {
        this.metaheuristicConfigPath = metaheuristicConfigPath;
    }

    public String getDatabaseIp() {
        return databaseIp;
    }

    public void setDatabaseIp(String databaseIp) {
        this.databaseIp = databaseIp;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public String getDatabasePassword() {
        return databasePassword;
    }

    public void setDatabasePassword(String databasePassword) {
        this.databasePassword = databasePassword;
    }

    public String getDatabasePort() {
        return databasePort;
    }

    public void setDatabasePort(String databasePort) {
        this.databasePort = databasePort;
    }

    public String getDatabaseUser() {
        return databaseUser;
    }

    public void setDatabaseUser(String databaseUser) {
        this.databaseUser = databaseUser;
    }

    public LinkedList<String> getBenchmarks() {
        return benchmarks;
    }

    public void setBenchmarks(LinkedList<String> benchmarks) {
        this.benchmarks = benchmarks;
    }

    public List<Rule> getRelations() {
        return relations;
    }

    public void setRelations(List<Rule> relations) {
        this.relations = relations;
    }

    public RelationTree getRelationTree1() {
        return relationTree1;
    }

    public void setRelationTree1(RelationTree relationTree1) {
        this.relationTree1 = relationTree1;
    }

    public RelationTree getRelationTree2() {
        return relationTree2;
    }

    public void setRelationTree2(RelationTree relationTree2) {
        this.relationTree2 = relationTree2;
    }

    public SimulatorParameter[] getVirtualParameters() {
        return virtualParameters;
    }

    public void setVirtualParameters(SimulatorParameter[] virtualParameters) {
        this.virtualParameters = virtualParameters;
    }

    public String getOutputPath() {
        return outputPath;
    }

    public void setOutputPath(String outputPath) {
        this.outputPath = outputPath;
    }
}