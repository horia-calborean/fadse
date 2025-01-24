package ro.ulbsibiu.fadse.io;

import org.w3c.dom.*;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import ro.ulbsibiu.fadse.environment.Objective;
import ro.ulbsibiu.fadse.environment.document.InputDocument;
import ro.ulbsibiu.fadse.environment.parameters.*;
import ro.ulbsibiu.fadse.environment.relation.IfRelation;
import ro.ulbsibiu.fadse.environment.relation.Relation;
import ro.ulbsibiu.fadse.environment.rule.AndRule;
import ro.ulbsibiu.fadse.environment.rule.IfRule;
import ro.ulbsibiu.fadse.environment.rule.RelationRule;
import ro.ulbsibiu.fadse.environment.rule.Rule;
import ro.ulbsibiu.fadse.extended.base.relation.RelationTree;

import java.nio.file.Paths;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static ro.ulbsibiu.fadse.io.XMLInputReader.metaheuristicConfigBasePath;

public class GAPXMLParser implements XMLInputReaderInterface{

    private final HashMap<String, Class<? extends Parameter>> parameterClassMap =  new HashMap<String, Class<? extends Parameter>>();
    private String xmlFilePath;
    private InputDocument inputDoc;
    private Document doc;

    public GAPXMLParser(String XmlPath){
        this.xmlFilePath = XmlPath;
        parameterClassMap.put("integer", IntegerParameter.class);
        parameterClassMap.put("float", DoubleParameter.class);//
        parameterClassMap.put("exp2", Exp2Parameter.class);
        parameterClassMap.put("constant", ConstantParameter.class);//
        parameterClassMap.put("expression", ExpresionParameter.class);//
        parameterClassMap.put("permutation", PermutationParameter.class);//
        parameterClassMap.put("string", StringParameter.class);
        parameterClassMap.put("virtual", VirtualParameter.class);//
        // parameterClassMap.put("checkpoint", CheckpointFileParameter.class);// not working

        // NO VIRTUAL PARAMETERS, RULES, RELATIONS
        try {
            this.inputDoc = new InputDocument();
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            this.doc = docBuilder.parse(new File(xmlFilePath));
            // normalize text representation
            this.doc.getDocumentElement().normalize();
            parseSimulator();
            parseBenchmarks();
            parseDatabase();
            parseMetaheuristic();
            parseParameters();;
            parseVirtualParameters(inputDoc.getParameters());
            parseSystemMetrics();
            parseRules(inputDoc.getParameters());
            parseRelations(inputDoc.getParameters());
            parseOutput();
        }
        catch (SAXParseException err) {
            System.out.println("** Parsing error" + ", line "
                    + err.getLineNumber() + ", uri " + err.getSystemId());
            System.out.println(" " + err.getMessage());

        } catch (SAXException e) {
            Exception x = e.getException();
            ((x == null) ? e : x).printStackTrace();

        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
    public InputDocument getInputDocument(){
        return this.inputDoc;
    }

    // Create a parameter object dynamically
    private Parameter createParameter(String name, String type, String description) throws Exception {
        Class<? extends Parameter> clazz = parameterClassMap.get(type);
        if (clazz != null) {
            return clazz.getDeclaredConstructor(String.class, String.class, String.class).newInstance(name, type, description);
        }
        throw new IllegalArgumentException("Unknown parameter type: " + type);
    }

    private int getValue(String value, Parameter[] params) {
        int valueI = 0;
        if (value.startsWith("@")) {//
            for (Parameter param : params) {
                if (param.getName().equalsIgnoreCase(value.substring(1))) {
                    valueI = (Integer) param.getValue();
                }
            }
        } else {
            valueI = Integer.parseInt(value);
        }
        return valueI;
    }

    @Override
    public void parseParameters() {
        try {
            //PARAMETERS
            NodeList parameters = ((Element) doc.getElementsByTagName("parameters").item(0)).getElementsByTagName("parameter");
            Parameter[] params = new Parameter[parameters.getLength()];

            for (int i = 0; i < parameters.getLength(); i++) {
                Node parameter = parameters.item(i);
                NamedNodeMap attributes = parameter.getAttributes();
                String name = attributes.getNamedItem("name").getNodeValue();
                String type = attributes.getNamedItem("type").getNodeValue();
                String description = "";
                //Parameter p = null;
                if (attributes.getNamedItem("description") != null) {
                    description = attributes.getNamedItem("description").getNodeValue();
                }

                Parameter p = createParameter(name, type, description);
                String minValue = attributes.getNamedItem("min").getNodeValue();
                String maxValue = attributes.getNamedItem("max").getNodeValue();

                switch(type){
                    case "integer":
                        //IMPORTANT SET STEP BEFORE SET MAX AND MIN
                        int stepI = 1;//default step
                        if (attributes.getNamedItem("step") != null) {
                            String step = attributes.getNamedItem("step").getNodeValue();
                            stepI = getValue(step, params);
                        }
                        p.setStep(stepI);

                        p.setLowerBound(getValue(minValue, params));
                        p.setUpperBound(getValue(maxValue, params));

                        int divideBy = 1;
                        if (attributes.getNamedItem("divideBy") != null) {
                            String divide = attributes.getNamedItem("divideBy").getNodeValue();
                            divideBy = getValue(divide, params);
                        }
                        p.setDivideBy(divideBy); //not working
                        break;
                    case "string":
                        NodeList items = ((Element) parameter).getElementsByTagName("item");
                        LinkedList<String> values = new LinkedList<String>();
                        for (int j = 0; j < items.getLength(); j++) {
                            values.add(items.item(j).getAttributes().getNamedItem("value").getNodeValue());
                        }
                        p.setValues(values);
                        break;
                    case "exp2":
                        p.setLowerBound(getValue(minValue, params));
                        p.setUpperBound(getValue(maxValue, params));
                        break;
                }

                if (p != null) {
                    params[i] = p;
                }
                inputDoc.setParameters(params);
            }
        } catch (SAXParseException err) {
            System.out.println("** Parsing error" + ", line "
                    + err.getLineNumber() + ", uri " + err.getSystemId());
            System.out.println(" " + err.getMessage());

        } catch (SAXException e) {
            Exception x = e.getException();
            ((x == null) ? e : x).printStackTrace();

        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    @Override
    public void parseSimulator() {
        NodeList simulator = doc.getElementsByTagName("simulator");
        NamedNodeMap simulatorattributes = simulator.item(0).getAttributes();
        String simulatorName = simulatorattributes.getNamedItem("name").getNodeValue();
        String simulatorType = simulatorattributes.getNamedItem("type").getNodeValue();
        inputDoc.setSimulatorName(simulatorName);
        inputDoc.setSimulatorType(simulatorType);

        NodeList simulatorParams = ((Element) simulator.item(0)).getElementsByTagName("parameter");
        for (int i = 0; i < simulatorParams.getLength(); i++) {
            NamedNodeMap simulatorParamattributes = simulatorParams.item(i).getAttributes();
            inputDoc.addSimulatorParameter(simulatorParamattributes.getNamedItem("name").getNodeValue(), simulatorParamattributes.getNamedItem("value").getNodeValue());
        }
    }

    @Override
    public void parseBenchmarks(){
        NodeList benchmarksNode = doc.getElementsByTagName("benchmarks");
        if (benchmarksNode != null && benchmarksNode.getLength() > 0) {
            NodeList benchmarks = ((Element) benchmarksNode.item(0)).getElementsByTagName("item");
            LinkedList<String> values = new LinkedList<String>();
            for (int i = 0; i < benchmarks.getLength(); i++) {
                values.add(benchmarks.item(i).getAttributes().getNamedItem("name").getNodeValue());
            }
            inputDoc.setBenchmarks(values);
        }
    }

    @Override
    public void parseDatabase(){
        //<database ip="127.0.0.1" port="1527" name="FADS_DB" user="fadse" password="fadse"/>
        NodeList databaseNode = doc.getElementsByTagName("database");
        NamedNodeMap databaseattributes = databaseNode.item(0).getAttributes();
        String databaseIp = databaseattributes.getNamedItem("ip").getNodeValue();
        String databasePort = databaseattributes.getNamedItem("port").getNodeValue();
        String databaseName = databaseattributes.getNamedItem("name").getNodeValue();
        String databaseUser = databaseattributes.getNamedItem("user").getNodeValue();
        String databasePassword = databaseattributes.getNamedItem("password").getNodeValue();
        inputDoc.setDatabaseIp(databaseIp);
        inputDoc.setDatabaseName(databaseName);
        inputDoc.setDatabasePassword(databasePassword);
        inputDoc.setDatabasePort(databasePort);
        inputDoc.setDatabaseUser(databaseUser);
    }

    @Override
    public void parseMetaheuristic(){
        NodeList metaheuristicNode = doc.getElementsByTagName("metaheuristic");
        NamedNodeMap metaheuristicattributes = metaheuristicNode.item(0).getAttributes();
        String metaheuristicName = metaheuristicattributes.getNamedItem("name").getNodeValue();
        String metaheuristicConfigPath = metaheuristicattributes.getNamedItem("config_path").getNodeValue();
        inputDoc.setMetaheuristicName(metaheuristicName);
        if(Paths.get(metaheuristicConfigPath).isAbsolute()) { //
            inputDoc.setMetaheuristicConfigPath(metaheuristicConfigPath);
        } else {
            inputDoc.setMetaheuristicConfigPath(metaheuristicConfigBasePath + metaheuristicConfigPath);
        }
    }

    @Override
    public void parseSystemMetrics() {
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
    public void parseOutput() {
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

    public void parseVirtualParameters(Parameter[] params){
        //VIRTUAL PARAMETERS
        try {
            System.out.println("EXTRACTING THE VIRTUAL PARAMS");
            NodeList virtualParameters = ((Element) doc.getElementsByTagName("virtual_parameters").item(0)).getElementsByTagName("parameter");
            Parameter[] virtualParams = new Parameter[virtualParameters.getLength()];
            for (int i = 0; i < virtualParameters.getLength(); i++) {
                Node parameter = virtualParameters.item(i);
                NamedNodeMap attributes = parameter.getAttributes();
                String name = attributes.getNamedItem("name").getNodeValue();
                String description = "";
                Parameter p = createParameter(name, "virtual", description);
                if (p != null) {
                    virtualParams[i] = p;
                }
            }
            System.out.println("FOUND: "+virtualParams.length);
            inputDoc.setVirtualParameters(virtualParams);
            Parameter[] paramsTemp = new Parameter[params.length+virtualParams.length];
            System.out.println("NORMAL PARAMS: "+params.length);
            System.arraycopy(params, 0, paramsTemp, 0, params.length);
            System.arraycopy(virtualParams, 0, paramsTemp, params.length, virtualParams.length);
            params = paramsTemp;
            System.out.println("NORMAL PARAMS (after): "+params.length);
        } catch (Exception e) {
            System.out.println("Problem at the virtual parameters (not fatal if you are not using them): " + e.getMessage());
        }
    }

    public void parseRules(Parameter[] params){
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
