/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ro.ulbsibiu.fadse.io;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;

import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import ro.ulbsibiu.fadse.environment.Objective;
import ro.ulbsibiu.fadse.environment.document.InputDocument;
import ro.ulbsibiu.fadse.environment.parameters.ConstantParameter;
import ro.ulbsibiu.fadse.environment.parameters.DoubleParameter;
import ro.ulbsibiu.fadse.environment.parameters.Exp2Parameter;
import ro.ulbsibiu.fadse.environment.parameters.ExpresionParameter;
import ro.ulbsibiu.fadse.environment.parameters.IntegerParameter;
import ro.ulbsibiu.fadse.environment.parameters.Parameter;
import ro.ulbsibiu.fadse.environment.parameters.PermutationParameter;
import ro.ulbsibiu.fadse.environment.parameters.StringParameter;
import ro.ulbsibiu.fadse.environment.parameters.VirtualParameter;
import ro.ulbsibiu.fadse.environment.relation.IfRelation;
import ro.ulbsibiu.fadse.environment.relation.Relation;
import ro.ulbsibiu.fadse.environment.rule.AndRule;
import ro.ulbsibiu.fadse.environment.rule.IfRule;
import ro.ulbsibiu.fadse.environment.rule.RelationRule;
import ro.ulbsibiu.fadse.environment.rule.Rule;
import ro.ulbsibiu.fadse.extended.base.relation.RelationTree;

/**
 *
 * @author Horia
 */
public class XMLInputReader {
	public final static String metaheuristicConfigBasePath = 
			System.getProperty("file.separator") + "configs" 
    		+ System.getProperty("file.separator") + "metaheuristicConfig"
    		+ System.getProperty("file.separator");
	
    public InputDocument parse(String xmlFilePath) {
        try {
            InputDocument inputDoc = new InputDocument();
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(new File(xmlFilePath));

            // normalize text representation
            doc.getDocumentElement().normalize();

//SIMULATOR
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
//BENCHMARKS
            NodeList benchmarksNode = doc.getElementsByTagName("benchmarks");
            if (benchmarksNode != null && benchmarksNode.getLength() > 0) {
                NodeList benchmarks = ((Element) benchmarksNode.item(0)).getElementsByTagName("item");
                LinkedList<String> values = new LinkedList<String>();
                for (int i = 0; i < benchmarks.getLength(); i++) {
                    values.add(benchmarks.item(i).getAttributes().getNamedItem("name").getNodeValue());
                }
                inputDoc.setBenchmarks(values);
            }
//DATABASE
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
//METAHEURISTIC
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
//PARAMETERS

            NodeList parameters = ((Element) doc.getElementsByTagName("parameters").item(0)).getElementsByTagName("parameter");
            Parameter[] params = new Parameter[parameters.getLength()];
            for (int i = 0; i < parameters.getLength(); i++) {
                Node parameter = parameters.item(i);
                NamedNodeMap attributes = parameter.getAttributes();
                String name = attributes.getNamedItem("name").getNodeValue();
                String type = attributes.getNamedItem("type").getNodeValue();
                String description = "";
                Parameter p = null;
                if (attributes.getNamedItem("description") != null) {
                    description = attributes.getNamedItem("description").getNodeValue();
                }
                if (type.equalsIgnoreCase("integer")) {
                    p = createIntegerParameter(name, type, description, parameter, params);
                } else if (type.equalsIgnoreCase("string")) {
                    p = createStringParameter(name, type, description, parameter);
                } else if (type.equalsIgnoreCase("exp2")) {
                    p = createExp2Parameter(name, type, description, parameter, params);
                } else if (type.equalsIgnoreCase("permutation")) {
                    p = createPermutationParameter(name, type, description, parameter, params);
                } else if (type.equalsIgnoreCase("boolean")) {//it is an Integer parameter with 0/1 min/max value
                    p = createBooleanParameter(name, "boolean", description, parameter);
                } else if (type.equalsIgnoreCase("on_off_mask")) {
                    System.err.println("Unsuported parameter type: " + type);
                } else if (type.equalsIgnoreCase("float")) {
                    p = createFloatParameter(name, "float", description, parameter, params);
                } else {
                    System.err.println("Unsuported parameter type: " + type);
                }
                if (p != null) {
                    params[i] = p;
                }

            }
            inputDoc.setParameters(params);
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
                    Parameter p = createVirtualParameter(name, description, parameter, virtualParams);
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
//SYSTEM METRICS
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

            //OUTPUT
            NodeList outputNode = doc.getElementsByTagName("output");
            NamedNodeMap outputAttributes = outputNode.item(0).getAttributes();
            String outputPath = outputAttributes.getNamedItem("output_path").getNodeValue();           
            inputDoc.setOutputPath(outputPath);                        
            
            return inputDoc;
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
        //System.exit (0);
        return null;

    }

    /**
     * Creates an integer parameter
     * @param name name of the parameter - very important - used to identify the parameter from now on
     * @param type type of the parameter - can be only "integer"
     * @param description - description of the parameter
     * @param parameter - the Node so that this method can extract other attributes such as min/max value and the "step"
     * @return the newly created Parameter, Attention Parameters should be created only here since these objects are searched when creating rules
     */
    private Parameter createIntegerParameter(String name, String type, String description, Node parameter, Parameter[] params) {
        IntegerParameter p = new IntegerParameter(name, type, description);
        NamedNodeMap attributes = parameter.getAttributes();

        //IMPORTANT SET STEP BEFORE SET MAX AND MIN
        int stepI = 1;//default step
        if (attributes.getNamedItem("step") != null) {
            String step = attributes.getNamedItem("step").getNodeValue();
            stepI = getValue(step, params);
        }
        p.setStep(stepI);
        String minValue = attributes.getNamedItem("min").getNodeValue();
        String maxValue = attributes.getNamedItem("max").getNodeValue();

        p.setMinValue(getValue(minValue, params));
        p.setMaxValue(getValue(maxValue, params));

        return p;
    }

    /**
     *Creates a Boolean parameter. In fact this is an integer parameter with min = 0 and max = 1
     * @param name name of the parameter - very important - used to identify the parameter from now on
     * @param type type of the parameter - can be only "boolean"
     * @param description - description of the parameter
     * @param parameter - the Node so that this method can extract other attributes (not necessary for Boolean but I wanted to cheep the common list of params for further improvements)
     * @return the newly created Parameter, Attention Parameters should be created only here since these objects are searched when creating rules
     */
    private Parameter createBooleanParameter(String name, String type, String description, Node parameter) {
        IntegerParameter p = new IntegerParameter(name, type, description);
        p.setMinValue(0);
        p.setMaxValue(1);
        p.setStep(1);
        return p;
    }

    /**
     *Creates a String parameter. It holds a string of possible values (items)
     * @param name name of the parameter - very important - used to identify the parameter from now on
     * @param type type of the parameter - can be only "string"
     * @param description - description of the parameter
     * @param parameter - the Node so that this method can extract other attributes
     * @return the newly created Parameter, Attention Parameters should be created only here since these objects are searched when creating rules
     */
    private Parameter createStringParameter(String name, String type, String description, Node parameter) {
        StringParameter p = new StringParameter(name, type, description);
        NodeList items = ((Element) parameter).getElementsByTagName("item");
        LinkedList<String> values = new LinkedList<String>();
        for (int i = 0; i < items.getLength(); i++) {
            values.add(items.item(i).getAttributes().getNamedItem("value").getNodeValue());
        }
        p.setValues(values);
//        try {
//            System.out.println("....");
//            System.out.println(p.getVariable().getLowerBound());
//            System.out.println(p.getVariable().getUpperBound());
//        } catch (JMException ex) {
//            Logger.getLogger(XMLInputReader.class.getName()).log(Level.SEVERE, null, ex);
//        }
        return p;
    }

    public static void main(String args[]) {
        XMLInputReader inputReader = new XMLInputReader();
        InputDocument id = inputReader.parse("configs/falsesimin.xml");
        System.out.println(id.getRelationTree1().findNode(0));
        System.out.println(id.getRelationTree1().findNode(1));

        System.out.println(id.getRelationTree1().findNode(2));


    }

    /**
     *Creates a Exp2Parameter parameter. it will produce values like: 2,4,8,16,32,64 if min = 2 and max = 64
     * @param name name of the parameter - very important - used to identify the parameter from now on
     * @param type type of the parameter - can be only "exp2"
     * @param description - description of the parameter
     * @param parameter - the Node so that this method can extract other attributes (min max values)
     * @return the newly created Parameter, Attention Parameters should be created only here since these objects are searched when creating rules
     */
    private Exp2Parameter createExp2Parameter(String name, String type, String description, Node parameter, Parameter[] params) {
        Exp2Parameter p = new Exp2Parameter(name, type, description);
        NamedNodeMap attributes = parameter.getAttributes();
        String minValue = attributes.getNamedItem("min").getNodeValue();
        String maxValue = attributes.getNamedItem("max").getNodeValue();
        p.setMinValue(getValue(minValue, params));
        p.setMaxValue(getValue(maxValue, params));
        return p;
    }

    private Parameter createFloatParameter(String name, String type, String description, Node parameter, Parameter[] params) {
        DoubleParameter p = new DoubleParameter(name, type, description);
        NamedNodeMap attributes = parameter.getAttributes();
        String minValue = attributes.getNamedItem("min").getNodeValue();
        String maxValue = attributes.getNamedItem("max").getNodeValue();
        double stepI = 0.01;//default step
//        if (attributes.getNamedItem("step") != null) {
//            String step = attributes.getNamedItem("step").getNodeValue();
//            stepI = Double.parseDouble(step);
//        }
        p.setMinValue(getValue(minValue, params));
        p.setMaxValue(getValue(maxValue, params));
//        p.setStep(stepI);
        return p;
    }

    private Parameter createPermutationParameter(String name, String type, String description, Node parameter, Parameter[] params) {
        PermutationParameter p = new PermutationParameter(name, type, description);
        NamedNodeMap attributes = parameter.getAttributes();
        String size = attributes.getNamedItem("dimension").getNodeValue();
        int value = getValue(size, params);
        p.setSize(value);
        return p;
    }

    private Parameter createVirtualParameter(String name, String description, Node parameter, Parameter[] params) {
        NamedNodeMap attributes = parameter.getAttributes();
        String expression = attributes.getNamedItem("value").getNodeValue();
        VirtualParameter p = new VirtualParameter(name,expression, description);
        return p;
    }

    /**
     * finds all the children of a given Element rule that match a certain tag
     * it searches for all the elements that match the tag under the "Element rule" but only retains the ones that are direct children to the Element
     * @param node the parent element
     * @param tag the tag to search for
     * @return list of direct child elements
     */
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

    /**
     * Used to discover all the relation rules under an element node
     * If the element node is a "rule" then only one Relation rule is found
     * If this method is used to discover the rules in an AND rule then multiple rules will be found
     * @param ruleNode the parent node
     * @param relationType what type of relation should this function search (equal, greater-equal ...)
     * @param params the list of already discovered parameters - these parameters will be stored as a reference in the rule also and later when the rule is validated the rule will extract the current value of the parameter
     * @return a list of relation rules if it founds one or an empty list
     */
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

    /**
     * searches for an AND rule under the current node and if it founds it returns it
     * @param ruleNode the parent node where the rule will be searched
     * @param ruleTypes a list of all the relation rules that can be found in the and rule
     * @param params the parameters so that the included relation rules can add them
     * @return and And rule if it founds one or null
     */
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
}
