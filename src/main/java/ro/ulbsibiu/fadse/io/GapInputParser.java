package ro.ulbsibiu.fadse.io;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import ro.ulbsibiu.fadse.environment.Objective;
import ro.ulbsibiu.fadse.environment.parameters.*;
import ro.ulbsibiu.fadse.environment.relation.IfRelation;
import ro.ulbsibiu.fadse.environment.relation.Relation;
import ro.ulbsibiu.fadse.environment.rule.AndRule;
import ro.ulbsibiu.fadse.environment.rule.IfRule;
import ro.ulbsibiu.fadse.environment.rule.RelationRule;
import ro.ulbsibiu.fadse.environment.rule.Rule;
import ro.ulbsibiu.fadse.extended.base.relation.RelationTree;
import ro.ulbsibiu.fadse.io.parser.DbParser;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class GapInputParser extends MicroArchDseInputParser implements DbParser {
    public GapInputParser(String dseXmlPath) {
        super(dseXmlPath);
        collectSimulationInput();

        parseSimulatorParametersTag();
        parseBenchmarksTag();
        parseDatabaseTag();
        parseMetaheuristicTag();
        parseParametersTag();

        parseVirtualParameters(inputDoc.getParameters());
        parseSystemMetricsTag();
        parseRules(inputDoc.getParameters());
        parseRelations(inputDoc.getParameters());
        parseOutputTag();
    }

    @Override
    protected void collectSimulationInput() {

    }

    @Override
    public Map<String, String> parseDbConnectionData() {
        NodeList databaseNode = dseXmlDocument.getElementsByTagName("database");

        NamedNodeMap attributes = databaseNode.item(0).getAttributes();

        Map<String, String> data = new HashMap<>();

        String ip = attributes.getNamedItem("ip").getNodeValue();
        String port = attributes.getNamedItem("port").getNodeValue();
        String name = attributes.getNamedItem("name").getNodeValue();
        String user = attributes.getNamedItem("user").getNodeValue();
        String password = attributes.getNamedItem("password").getNodeValue();

        data.put("ip", ip);
        data.put("port", port);
        data.put("name", name);
        data.put("user", user);
        data.put("password", password);

        return data;
    }

    @Override
    public void parseSystemMetricsTag() {
        NodeList systemMetrics = ((Element) doc.getElementsByTagName("system_metrics").item(0)).getElementsByTagName("system_metric");
        Map<String, Objective> objectives = new HashMap<String, Objective>();
        for (int i = 0; i < systemMetrics.getLength(); i++) {
            Node metric = systemMetrics.item(i);
            NamedNodeMap attributes = metric.getAttributes();
            String name = attributes.getNamedItem("name").getNodeValue();
            String type = attributes.getNamedItem("type").getNodeValue();
            String unit = "";
            if (attributes.getNamedItem("unit") != null) {
                unit = attributes.getNamedItem("unit").getNodeValue();
            }
            String desired = "small";//default small if not specified
            if (attributes.getNamedItem("desired") != null) {
                desired = attributes.getNamedItem("desired").getNodeValue();
            }
            String description = "";
            if (attributes.getNamedItem("description") != null) {
                description = attributes.getNamedItem("description").getNodeValue();
            }
            Objective obj = new Objective(name, type, unit, description, !desired.equalsIgnoreCase("small"));
            objectives.put(name, obj);
        }
        inputDoc.setObjectives(objectives);
    }

    @Override
    public void parseOutputTag() {
        NodeList outputNode = doc.getElementsByTagName("output");
        NamedNodeMap outputAttributes = outputNode.item(0).getAttributes();
        String outputPath = outputAttributes.getNamedItem("output_path").getNodeValue();
        inputDoc.setOutputPath(outputPath);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public void parseRelations(Parameter[] params) throws Exception {
        //RELATIONS
        NodeList relations = ((Element) doc.getElementsByTagName("relations").item(0)).getElementsByTagName("relation");
        List<Relation> relationsList = new LinkedList<Relation>();
        for (int i = 0; i < relations.getLength(); i++) {//takes each relation
            Element relationNode = (Element) relations.item(i);//relation node repesents a <relation> element
            Relation relation = getIfRelation(relationNode, params);//find relations of type<if>
            relationsList.add(relation);
            System.out.println(relation);
        }
        //build the trees using the paramters and the relations
        RelationTree relationTree = new RelationTree();
        RelationTree relationTreeCopy = new RelationTree();
        //detect the root nodes
        for (int i = 0; i < params.length; i++) {
            boolean isRoot = true;
            for (int j = 0; j < relationsList.size(); j++) {
                String[] dependentParams = relationsList.get(j).getChildrenNames();
                for (int k = 0; k < dependentParams.length; k++) {
                    if (params[i].getName().equals(dependentParams[k])) {
                        isRoot = false;
                    }
                }
            }
            if (isRoot) {
                System.out.println("ADD ROOT: " + i);
                relationTree.addRootNode(i);
                relationTreeCopy.addRootNode(i);
            }
            //add the subnodes
            addSubNodesToRelationTrees(relationTree, params, relationsList, i);
            addSubNodesToRelationTrees(relationTreeCopy, params, relationsList, i);
        }
        System.out.println("RelationTree: " + relationTree);
        inputDoc.setRelationTree1(relationTree);
        inputDoc.setRelationTree2(relationTreeCopy);

        // Uncomment this to see graphical representation
        // relationTree.printToScreen();
    }

    public void parseRules(Parameter[] params) {
        //RULES
        NodeList rules = ((Element) doc.getElementsByTagName("rules").item(0)).getElementsByTagName("rule");
        List<Rule> rulesList = new LinkedList<Rule>();
        String[] ruleTypes = {"greater-equal", "greater", "equal", "less-equal", "less", "not-equal"};
        for (int i = 0; i < rules.getLength(); i++) {//takes each rule
            Element ruleNode = (Element) rules.item(i);//rule node repesents a <rule> element

            Rule rule;
            //RELATION RULES
            //identify rule type
            for (int j = 0; j < ruleTypes.length; j++) {//see if the current rule contains elements such as <equal>, <greater-equal> ...
                List<RelationRule> rulz = getRelationRule(ruleNode, ruleTypes[j], params);//get all the elements of type <equal> that are child of the current rule
//the above function is used in other situations too (for and rules) where multiple sub relation rules can exist
                if (rulz != null && rulz.size() > 0) {//if the current rule has a relationRule (= , >=, <= ...) child
                    rule = rulz.get(0);//in this situation it should be only one element in each rule
                    if (rule != null) {
                        rulesList.add(rule);//add the new found rule to the final list of rules
//                            System.out.println(rule);
                    }
                }
            }
            //AND RULES
            rule = getAndRule(ruleNode, ruleTypes, params);//find rules of type<and>
            if (rule != null) {
                rulesList.add(rule);
//                    System.out.println("and rule" + rule);
            }
            //IF RULES
            rule = getIfRule(ruleNode, ruleTypes, params);//find rules of type<if>
            if (rule != null) {
                rulesList.add(rule);
//                    System.out.println("if rule" + rule);
            }
        }
//            System.out.println(rulesList);
        inputDoc.setRules(rulesList);
    }

    private List<Element> findNodeSubElements(Element node, String tag) {
        NodeList tags = node.getElementsByTagName(tag);
        List<Element> subNodes = new LinkedList<Element>();
        for (int j = 0; j < tags.getLength(); j++) {
            if (tags.item(j).getParentNode() == node) {//this tagcan be found on children not on the first level, but only the children that are directly connected to this node are retained
                subNodes.add((Element) tags.item(j));
            }
        }
        List<Element> r = null;
        if (subNodes != null && subNodes.size() > 0) {
            r = subNodes;
        }
        return r;
    }

    private AndRule getAndRule(Element ruleNode, String[] ruleTypes, Parameter[] params) {
        List<Element> rs = findNodeSubElements(ruleNode, "and");//
        AndRule andRule = null;
        Element r = null;
        if (rs != null && rs.size() > 0) {
            r = rs.get(0);
            if (r != null) {
                List<Rule> innerRelationRules = new LinkedList<Rule>();
                for (int j = 0; j < ruleTypes.length; j++) {
                    List<RelationRule> temps = getRelationRule(r, ruleTypes[j], params);
                    if (temps != null) {
                        innerRelationRules.addAll(temps);
                    }
                }
                Rule temp = getAndRule(r, ruleTypes, params);
                if (temp != null) {
                    innerRelationRules.add(temp);
                }
                temp = getIfRule(r, ruleTypes, params);
                if (temp != null) {
                    innerRelationRules.add(temp);
                }

                andRule = new AndRule("and", "", innerRelationRules);//TODO search for description
            }
        }
        return andRule;
    }

    private IfRule getIfRule(Element ruleNode, String[] ruleTypes, Parameter[] params) {
        List<Element> rs = findNodeSubElements(ruleNode, "if");
        Element r = null;
        IfRule ifRule = null;
        if (rs != null && rs.size() > 0) {
            r = rs.get(0);
            //get the if condition
            List<Rule> innerRelationRules = new LinkedList<Rule>();
            for (int j = 0; j < ruleTypes.length; j++) {
                List<RelationRule> temps = getRelationRule(r, ruleTypes[j], params);
                if (temps != null) {
                    innerRelationRules.addAll(temps);
                }
            }
            Rule temp = getAndRule(r, ruleTypes, params);
            if (temp != null) {
                innerRelationRules.add(temp);
            }
            temp = getIfRule(r, ruleTypes, params);
            if (temp != null) {
                innerRelationRules.add(temp);
            }
            rs = findNodeSubElements(r, "then");//get the then node
            Element then = rs.get(0);
            List<Rule> innerThenRelationRules = new LinkedList<Rule>();
            for (int j = 0; j < ruleTypes.length; j++) {
                List<RelationRule> temps = getRelationRule(then, ruleTypes[j], params);
                if (temps != null) {
                    innerThenRelationRules.addAll(temps);
                }
            }
            temp = getAndRule(then, ruleTypes, params);
            if (temp != null) {
                innerThenRelationRules.add(temp);
            }
            temp = getIfRule(then, ruleTypes, params);
            if (temp != null) {
                innerThenRelationRules.add(temp);
            }

            ifRule = new IfRule("if", "", innerRelationRules.get(0), innerThenRelationRules.get(0));
        }

        return ifRule;
    }

    private List<RelationRule> getRelationRule(Element ruleNode, String relationType, Parameter[] params) {
        List<Element> rs = findNodeSubElements(ruleNode, relationType);//ginds <equal> or <greater> or ...
        List<RelationRule> relationRules = new LinkedList<RelationRule>();
        if (rs != null) {
            for (Element r : rs) {//take each relation rule
                RelationRule rule = null;
                if (r != null) {
                    NodeList ruleParameters = r.getElementsByTagName("parameter");//search for parameters in the relation rule
                    Parameter p1 = null;
                    Parameter p2 = null;
                    if (ruleParameters != null && ruleParameters.getLength() > 0) {//TODO replace with for
                        if (ruleParameters.item(0) != null) {
                            String parameterName = ruleParameters.item(0).getAttributes().getNamedItem("name").getNodeValue();
                            for (int t = 0; t < params.length; t++) {
                                if (params[t].getName().equals(parameterName)) {
                                    p1 = params[t];
                                }
                            }
                            if (p1 == null) {
                                System.err.println("Parameter 1 " + parameterName + " used in the relation was not defined in the parameter list. Setting it as an expresion");
                                p1 = new ExpresionParameter(parameterName, "");
                            }
                        }
                        if (ruleParameters.item(1) != null) {
                            String parameterName = ruleParameters.item(1).getAttributes().getNamedItem("name").getNodeValue();
                            for (int t = 0; t < params.length; t++) {
                                if (params[t].getName().equals(parameterName)) {
                                    p2 = params[t];
                                }
                            }
                            if (p2 == null) {
                                System.err.println("Parameter 2 " + parameterName + " used in the relation was not defined in the parameter list. Setting it as an expresion");
                                p2 = new ExpresionParameter(parameterName, "");
                            }
                        }
                    } else {
                        System.out.println("[Warning] No parameters found in the relation rule. Something might be wrong");
                    }
                    NodeList ruleConstants = r.getElementsByTagName("constant");
                    if (ruleConstants != null) {//TODO replace with for
                        for (int k = 0; k < ruleConstants.getLength(); k++) {
                            if (ruleConstants.item(k) != null) {
                                String parameterValue = ruleConstants.item(k).getAttributes().getNamedItem("value").getNodeValue();

                                Parameter c = null;
                                c = new ConstantParameter("", "constant", "");//TODO assign name, description
                                c.setValue(parameterValue);
                                if (p1 == null) {
                                    p1 = c;
                                } else if (p2 == null) {
                                    p2 = c;
                                }
                            }
                        }
                    }
                    if (p1 == null || p2 == null) {
                        System.err.println("Parameters not initialized corectly " + ruleNode.getTagName() + " " + p1 + " " + p2);
                    }
                    rule = new RelationRule(relationType, "", p1, p2);//TODO assign a name
                    relationRules.add(rule);
                }
            }
        }
        return relationRules;
    }

    private void addSubNodesToRelationTrees(RelationTree relationTree, Parameter[] params, List<Relation> relationsList, int parentPosition) throws Exception {
        //get the relations that are dependent of this node
        for (int i = 0; i < relationsList.size(); i++) {
            Relation relation = relationsList.get(i);
            if (relation.getParentName().equals(params[parentPosition].getName())) {
                //find the position of this child
                int childPosition = -1;
                for (int j = 0; j < params.length; j++) {
                    for (int k = 0; k < relation.getChildrenNames().length; k++) {
                        if (params[j].getName().equals(relation.getChildrenNames()[k])) {
                            childPosition = j;
                            double deactivationValue = relation.getDeactivationValue();
                            //if type of parametr is exp2 then find the integer value
                            if (params[j] instanceof Exp2Parameter) {
                                deactivationValue = (int) (Math.log(deactivationValue) / Math.log(2));
                            } else if (params[j] instanceof IntegerParameter) {
                                deactivationValue = (int) (deactivationValue / ((IntegerParameter) params[j]).getStep());
                            } else {
                                throw new Exception("Unsupported parameter in relation");
                            }
                            //add this node as a child
                            relationTree.addNode(childPosition, parentPosition, deactivationValue);
                            //add all the children for this node
                            System.out.println("ADD: " + childPosition + " " + parentPosition + " " + deactivationValue);
                            //      addSubNodesToRelationTrees(relationTree, params, relationsList, childPosition);

                        }
                    }
                }
            }
        }

    }

    private Relation getIfRelation(Element relationNode, Parameter[] params) {
        List<Element> rs = findNodeSubElements(relationNode, "if");
        IfRelation ifRelation = null;
        if (rs != null && rs.size() > 0) {
            String parameterName = rs.get(0).getAttribute("parameter");
            Double invalidationValue = Double.parseDouble(rs.get(0).getAttribute("value"));
            List<Element> subElements = findNodeSubElements(findNodeSubElements(rs.get(0), "then_invalidate").get(0), "parameter");
            String[] consequentParameters = new String[subElements.size()];
            for (int i = 0; i < subElements.size(); i++) {
                Element consequent = subElements.get(i);
                String consequentParameter = consequent.getAttribute("name");
                consequentParameters[i] = consequentParameter;
            }
            ifRelation = new IfRelation(parameterName, invalidationValue, consequentParameters);
        }
        return ifRelation;
    }
    //    public static void main(String args[]) {
//        XMLInputReader inputReader = new XMLInputReader();
//        InputDocument id = inputReader.parse("configs/falsesimin.xml");
//        System.out.println(id.getRelationTree1().findNode(0));
//        System.out.println(id.getRelationTree1().findNode(1));
//
//        System.out.println(id.getRelationTree1().findNode(2));
//    }

}
