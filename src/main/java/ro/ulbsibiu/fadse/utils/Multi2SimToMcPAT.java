/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ro.ulbsibiu.fadse.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.ini4j.Wini;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author Horia Calborean
 */
public class Multi2SimToMcPAT {

    public static void main(String[] args) {
        //./m2s -report:pipeline outp2.txt -report:cache outc2.txt ~/multi2sim-2.3.2/minibench/test-sort.i386

        String mapPath = "multi2sim_mcpat_param_map.txt";
        String outCachePath = "outc2.txt";
        String outPipelinePath = "outp2.txt";
        String xmlPath = "mcpat.xml";
        String core = "0";
        try {
            Wini cache = new Wini(new File(outCachePath));
            Wini pipeline = new Wini(new File(outPipelinePath));
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(new File(xmlPath));
            // normalize text representation
            doc.getDocumentElement().normalize();
            BufferedReader map = new BufferedReader(new FileReader(mapPath));
            String str;
            int step = 0;
            while ((str = map.readLine()) != null) {
                System.out.println("["+(step++)+"]");
                str = str.replace("$", core);
                StringTokenizer tokenizer = new StringTokenizer(str, ";");
                while (tokenizer.hasMoreElements()) {
                    String mcpatRegion = tokenizer.nextToken().trim();
                    String mcpatName = tokenizer.nextToken().trim();
                    String multi2simRegion = tokenizer.nextToken().trim();
                    String multi2simName = tokenizer.nextToken().trim();
//                    if (multi2simRegion.equalsIgnoreCase("global")) {
//                        //TODO
//                    } else {
//                        mcpatRegion = "system";
//                        mcpatName = "total_cycles";
                        Element elem = getElement(mcpatRegion, mcpatName, doc);
                        if (cache.get(multi2simRegion, multi2simName) != null && !cache.get(multi2simRegion, multi2simName).equals("")) {
                            System.out.println("["+multi2simRegion+"]"+multi2simName);
                            elem.getAttributes().getNamedItem("value").setTextContent(cache.get(multi2simRegion, multi2simName));
                            System.out.printf("replaced %s with %s",elem.getAttributes().getNamedItem("name"),cache.get(multi2simRegion, multi2simName));
                        } else if (pipeline.get(multi2simRegion, multi2simName) != null && !pipeline.get(multi2simRegion, multi2simName).equals("")) {
                            System.out.println("["+multi2simRegion+"]"+multi2simName);
                            elem.getAttributes().getNamedItem("value").setTextContent(pipeline.get(multi2simRegion, multi2simName));
                            System.out.printf("replaced %s with %s",elem.getAttributes().getNamedItem("name"),pipeline.get(multi2simRegion, multi2simName));
                        } else {
                            Logger.getLogger(Multi2SimToMcPAT.class.getName()).log(Level.SEVERE, "Missed: ["+multi2simRegion+"] - "+multi2simName);
                        }
//                    }
                }
            }
            map.close();

            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
//initialize StreamResult with File object to save to file
            StreamResult result = new StreamResult(new StringWriter());
            DOMSource source = new DOMSource(doc);
            transformer.transform(source, result);

            String xmlString = result.getWriter().toString();
             // Create file
    FileWriter fstream = new FileWriter("m2sformcpat.xml");
        BufferedWriter out = new BufferedWriter(fstream);
    out.write(xmlString);
    //Close the output stream
    out.close();
           // System.out.println(xmlString);
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(Multi2SimToMcPAT.class.getName()).log(Level.SEVERE, null, ex);
        } catch (TransformerException ex) {
            Logger.getLogger(Multi2SimToMcPAT.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SAXException ex) {
            Logger.getLogger(Multi2SimToMcPAT.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Multi2SimToMcPAT.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static Element getElement(String mcpatRegion, String mcpatName, Document doc) {
       // System.out.println(mcpatRegion + " " + mcpatName);

        NodeList regions = doc.getElementsByTagName("component");
        Node region = null;
        for (int i = 0; i < regions.getLength(); i++) {
            if (regions.item(i).getAttributes().getNamedItem("id").getNodeValue().equals(mcpatRegion)) {
                region = regions.item(i);
                break;
            }
        }
//        System.out.println(region.getAttribute("name"));
        NodeList stats = doc.getElementsByTagName("stat");
        Element stat_total_cycles = null;
        for (int i = 0; i < stats.getLength(); i++) {
            if (stats.item(i).getAttributes().getNamedItem("name").getNodeValue().equals(mcpatName) && stats.item(i).getParentNode().equals(region)) {
                stat_total_cycles = (Element) stats.item(i);
            }
        }
        //System.out.println(stat_total_cycles.getAttributes().getNamedItem("name").getNodeValue());
//            stat_total_cycles.getAttributes().getNamedItem("value").setTextContent("654321");
        return stat_total_cycles;
    }
}
