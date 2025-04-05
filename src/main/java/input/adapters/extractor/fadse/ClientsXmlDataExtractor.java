package input.adapters.extractor.fadse;

import core.model.clients.FadseClient;
import core.model.clients.ListOfFadseClients;
import input.adapters.document.XmlInputDocument;
import input.adapters.extractor.XmlDataExtractor;
import input.ports.extractor.fadse.ClientsExtractor;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

import java.io.IOException;
import java.net.InetAddress;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientsXmlDataExtractor extends XmlDataExtractor implements ClientsExtractor {
    public ClientsXmlDataExtractor(XmlInputDocument xmlDoc) {
        super(xmlDoc);
    }

    @Override
    public ListOfFadseClients extractClientsData() {
        ListOfFadseClients listOfFadseClients = new ListOfFadseClients();

        try {
            xmlDocument.getDocumentElement().normalize();
            NodeList neighborsList = xmlDocument.getElementsByTagName("fadseClient");
            for (int i = 0; i < neighborsList.getLength(); i++) {
                FadseClient client = new FadseClient();
                NamedNodeMap attributes = neighborsList.item(i).getAttributes();
                client.setIP(InetAddress.getByName(attributes.getNamedItem("ip").getNodeValue()));
                client.setNumberOfOccupiedSlots(0);
                client.setNumberOfSlots(Integer.parseInt(attributes.getNamedItem("availableSlots").getNodeValue()));
                client.setPort(Integer.parseInt(attributes.getNamedItem("listenPort").getNodeValue()));
                listOfFadseClients.add(client);
            }

        } catch (IOException ex) {
            Logger.getLogger(ClientsXmlDataExtractor.class.getName()).log(Level.SEVERE, null, ex);
        }
        return listOfFadseClients;
    }
}