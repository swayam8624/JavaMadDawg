package com.singal.maddog.server;

import com.singal.maddog.net.GameServer;
import java.util.Scanner;

/**
 * standalone execution host wrapper for the multiplayer UDP game server.
 */
public class RainServer {
    public static void main(String[] args) {
        GameServer server = new GameServer(8192);
        server.start();

        System.out.println("Rain Server is running on port 8192.");
        System.out.println("Type 'exit' or 'quit' to terminate the server.");

        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine().trim();
            if ("exit".equalsIgnoreCase(line) || "quit".equalsIgnoreCase(line)) {
                break;
            }
        }

        System.out.println("Shutting down server...");
        server.stop();
        System.out.println("Server terminated successfully.");
        System.exit(0);
    }
}
