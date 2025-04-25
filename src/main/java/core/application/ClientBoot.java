package core.application;

import core.model.clients.FadseClient;

public class ClientBoot {
    public static void main(String[] args) {
        System.out.println("main(String[] args) from ClientBoot -> started");

        int clientPort = (args.length >= 2) ? Integer.parseInt(args[1]) : 4445;

        FadseClient fadseClient = new FadseClient();
    }
}