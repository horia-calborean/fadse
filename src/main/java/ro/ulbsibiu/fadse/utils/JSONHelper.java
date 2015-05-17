/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ro.ulbsibiu.fadse.utils;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.simple.parser.ContainerFactory;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import ro.ulbsibiu.fadse.extended.problems.simulators.sniper.SniperGroupResults;

/**
 *
 * @author radu
 */
public class JSONHelper {

    static ContainerFactory containerFactory = new ContainerFactory() {
        public List creatArrayContainer() {
            return new LinkedList();
        }

        public Map createObjectContainer() {
            return new LinkedHashMap();
        }

    };

    public static Object GetValue(String jsonString, String property) {

        try {
            JSONParser parser = new JSONParser();

            Map jsonMap = (Map) parser.parse(jsonString, containerFactory);

            String[] propertyPath = property.split(":");

            for(int i= 0 ;i<propertyPath.length;i++){                
                String prop = propertyPath[i];       
                if(i == propertyPath.length - 1){
                    return jsonMap.get(prop);
                }
                if (!jsonMap.containsKey(prop)) {
                    return null;
                }
                jsonMap = (Map)jsonMap.get(prop);           
            }                        
        } catch (ParseException ex) {
            Logger.getLogger(SniperGroupResults.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
}
