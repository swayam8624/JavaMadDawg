package com.singal.maddog.net;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A simple UDP game server managing client connections, broadcasts, and positions.
 */
public class GameServer {
    private static final Logger LOGGER = Logger.getLogger(GameServer.class.getName());

    public static class ClientInfo {
        public final InetAddress address;
        public final int port;
        public String username;
        public float x;
        public float y;

        public ClientInfo(InetAddress address, int port, String username) {
            this.address = address;
            this.port = port;
            this.username = username;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ClientInfo)) return false;
            ClientInfo that = (ClientInfo) o;
            return port == that.port && address.equals(that.address);
        }

        @Override
        public int hashCode() {
            return Objects.hash(address, port);
        }
    }

    private final int port;
    private final List<ClientInfo> clients = new ArrayList<>();

    private DatagramSocket socket;
    private Thread listenThread;
    private volatile boolean running = false;

    public GameServer(int port) {
        this.port = port;
    }

    public void start() {
        try {
            socket = new DatagramSocket(port);
        } catch (SocketException e) {
            LOGGER.log(Level.SEVERE, "Could not start server on port: " + port, e);
            return;
        }

        running = true;
        listenThread = new Thread(this::listen, "Network-Server-ListenThread");
        listenThread.start();
        LOGGER.info("Server started on port: " + port);
    }

    public void stop() {
        if (!running) return;

        running = false;
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }

        try {
            if (listenThread != null) {
                listenThread.join(1000);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        LOGGER.info("Server stopped.");
    }

    private void listen() {
        byte[] buffer = new byte[1024];
        while (running) {
            DatagramPacket incoming = new DatagramPacket(buffer, buffer.length);
            try {
                socket.receive(incoming);
                processPacket(incoming);
            } catch (IOException e) {
                if (running) {
                    LOGGER.log(Level.WARNING, "Server failed to receive packet: ", e);
                }
            }
        }
    }

    private void processPacket(DatagramPacket datagram) {
        Packet packet = Packet.deserialize(datagram.getData(), datagram.getLength());
        InetAddress address = datagram.getAddress();
        int clientPort = datagram.getPort();

        switch (packet.getType()) {
            case LOGIN:
                String username = new String(packet.getData(), StandardCharsets.UTF_8).trim();
                ClientInfo client = new ClientInfo(address, clientPort, username);
                if (!clients.contains(client)) {
                    clients.add(client);
                    LOGGER.info("Client connected: " + username + " (" + address + ":" + clientPort + ")");
                    
                    // Broadcast join message to everyone
                    broadcast(packet, client);
                }
                break;

            case DISCONNECT:
                ClientInfo toDisconnect = getClient(address, clientPort);
                if (toDisconnect != null) {
                    clients.remove(toDisconnect);
                    LOGGER.info("Client disconnected: " + toDisconnect.username);
                    broadcast(packet, toDisconnect);
                }
                break;

            case MOVE:
                ClientInfo movingClient = getClient(address, clientPort);
                if (movingClient != null) {
                    // Update client position coordinates
                    byte[] data = packet.getData();
                    if (data.length >= 8) {
                        movingClient.x = Float.intBitsToFloat(readInt(data, 0));
                        movingClient.y = Float.intBitsToFloat(readInt(data, 4));
                    }
                    // Broadcast movement updates to other clients
                    broadcast(packet, movingClient);
                }
                break;

            default:
                LOGGER.warning("Unknown packet type received: " + packet.getType());
                break;
        }
    }

    private ClientInfo getClient(InetAddress address, int port) {
        for (ClientInfo c : clients) {
            if (c.port == port && c.address.equals(address)) {
                return c;
            }
        }
        return null;
    }

    public void send(Packet packet, ClientInfo client) {
        byte[] data = packet.serialize();
        DatagramPacket datagram = new DatagramPacket(data, data.length, client.address, client.port);
        try {
            socket.send(datagram);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error sending packet to " + client.username, e);
        }
    }

    /**
     * Broadcasts a packet to all connected clients except the sender.
     */
    private void broadcast(Packet packet, ClientInfo excludeClient) {
        for (ClientInfo client : clients) {
            if (!client.equals(excludeClient)) {
                send(packet, client);
            }
        }
    }

    private int readInt(byte[] data, int offset) {
        return ((data[offset] & 0xFF) << 24) |
               ((data[offset + 1] & 0xFF) << 16) |
               ((data[offset + 2] & 0xFF) << 8) |
               (data[offset + 3] & 0xFF);
    }
}
