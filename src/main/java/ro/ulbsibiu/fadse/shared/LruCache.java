/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ro.ulbsibiu.fadse.shared;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ho.yaml.Yaml;
import org.ho.yaml.YamlDecoder;
import org.ho.yaml.YamlEncoder;

/**
 *
 * @author jahrralf
 */
public class LruCache<K, V> {
    // speichert ergebnisse vorher ausgeführter Pfade mit beliebigem Schlüssel und beliebigen Werten
    private HashMap<K, V[]> lookup_table = new HashMap<K, V[]>();

    // Query-History wird benötigt, um LRU bei der Lookup-Table durchzuführen
    private LinkedList<K> query_history = new LinkedList<K>();

    // Anzahl der Einträge, die dieser Cache verwalten kann
    private int table_size = 128;

    // Anzahl der Werte, die pro Schlüssel gespeichert werden können
    private int entries_per_key = 1;

    private int counter_hit = 0;
    private int counter_miss = 0;

    /** Initialisiert neuen Cache mit einer bestimmten Tabellengröße und einem Wert pro Schlüssel */
    public LruCache(int table_size) {
        new LruCache(table_size, 1);
    }

    /** Initialisiert neuen Cache mit einer bestimmten Tabellengröße und einer vorgegebenen Anzahl an Werten pro Schlüssel */
    public LruCache(int table_size, int entries_per_key) {
        this.table_size = table_size;
        this.entries_per_key = entries_per_key;
    }

    /** Gibt zurück, wieviele Werte pro Schlüssel gespeichert werden können */
    public int getEntriesForKey() {
        return entries_per_key;
    }

    /** Sucht für einen Schlüssel nach einem Wert */
    public V query(K lookup_key, int entry) {
        // System.out.print("Fetching entry " + entry + " for " + lookup_key);

        // Ergebnis holen
        V[] data = lookup_table.get(lookup_key);
        V result = null;

        if(data != null) result = data[entry];

        // Query-Schlüssel nach vorne ziehen
        if(result != null) {
            query_history.remove(lookup_key);
            query_history.addLast(lookup_key);
        }

        if(result != null) counter_hit++;
        else counter_miss++;

        // System.out.println(" => " + result);

        return result;
    }

    /** Legt einen Wert unter einem Schlüssel in den Cache */
    public void store(K key, V value) {
        // Wenn Schlüssel noch nicht enthalten...
        if(lookup_table.get(key) == null) {
            // Platz schaffen
            while(lookup_table.size() > table_size - 1) {
                K to_evict = query_history.pollFirst();
                lookup_table.remove(to_evict);
            }

            // Einfügen und Query-Schlüssel speichern
            V[] my_data =(V[]) Array.newInstance(value.getClass(), entries_per_key);
            my_data[0] = value;
            
            for(int i = 1; i < entries_per_key; i++) my_data[i] = null;

            lookup_table.put(key, my_data);

            // Query-Schlüssel hinzufügen
            query_history.addLast(key);
        } else {
            // Schlüssel schon enthalten: Eventuell als neuen Wert speichern
            V[] my_data = lookup_table.get(key);

            if(my_data[0].equals(value)) {
                // Ziel schon an erster stelle
                // System.out.println("Schon an erster Stelle: " + value);
            } else {
                // Ziel nicht an erster Stelle => Verschieben.
                // System.out.println("Nicht an erster Stelle: " + value);

                // Prüfen ob Wert bisher schon enthalten war...
                int max = my_data.length - 1;

                for(int i = 0; i < my_data.length ; i++) {
                    if(my_data[i] != null && my_data[i].equals(value)) {
                        max = i;
                        break;
                    }
                }

                if(max == 0) {
                    // Wert ist schon Kopf
                    // System.out.println("Schon Kopf: " + value + " für " + key);
                } else {
                    // Alle Werte eines weiter hinter schieben bis
                    for(int i = max - 1; i >= 0; i--) {
                        my_data[i + 1] = my_data[i];
                    }

                    // Wert am Kopf einfügen
                    my_data[0] = value;

                    // System.out.println("Jetzt gespeicherte Werte für " + key + " sind " + my_data.toString());
                }
            }

            // lookup_table.put(key, my_data);

            // Query-Schlüssel nach vorne ziehen
            query_history.remove(key);
            query_history.addLast(key);
        }

        // Query-History auf korrekte Länge bringen.
        /* while(query_history.size() > table_size)
            query_history.removeFirst(); */
    }

    public int getTableSize() {
        return table_size;
    }

    public void persist() {
        /* File file = new File("cacti_cache_" + System.currentTimeMillis() + ".yml");

        try {
            YamlEncoder enc = new YamlEncoder(new FileOutputStream(file));
            enc.writeObject(lookup_table);
            enc.writeObject(query_history);
            enc.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(LruCache.class.getName()).log(Level.SEVERE, null, ex);
        } */
    }

    public void init() throws FileNotFoundException, EOFException {
        File file = new File("cacti_cache.yml");

            YamlDecoder dec = new YamlDecoder(new FileInputStream(file));
            HashMap<K, V[]> a = dec.readObjectOfType(HashMap.class);
            LinkedList<K> b = dec.readObjectOfType(LinkedList.class);
            dec.close();

            lookup_table = a;
            query_history = b;
    }
}
