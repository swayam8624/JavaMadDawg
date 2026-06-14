package com.singal.maddog.net;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handles outgoing and incoming UDP packets on the client side.
 * Buffers received packets in a thread-safe queue to be processed on the main game thread.
 */
public class GameClient {
    private static final Logger LOGGER = Logger.getLogger(GameClient.class.getName());

    private final String ipAddress;
    private final int port;
    private final Queue<Packet> packetQueue = new ConcurrentLinkedQueue<>();

    private DatagramSocket socket;
    private InetAddress serverAddress;
    private Thread listenThread;
    private volatile boolean running = false;

    public GameClient(String host, int port) {
        this.ipAddress = host;
        this.port = port;
    }

    public boolean connect() {
        try {
            serverAddress = InetAddress.getByName(ipAddress);
            socket = new DatagramSocket();
        } catch (UnknownHostException | SocketException e) {
            LOGGER.log(Level.SEVERE, "Failed to initialize socket connection to: " + ipAddress, e);
            return false;
        }

        running = true;
        listenThread = new Thread(this::listen, "Network-Client-ListenThread");
        listenThread.start();
        LOGGER.info("Client connected to server " + ipAddress + ":" + port);
        return true;
    }

    public void disconnect() {
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
        LOGGER.info("Client disconnected.");
    }

    private void listen() {
        byte[] buffer = new byte[1024];
        while (running) {
            DatagramPacket incoming = new DatagramPacket(buffer, buffer.length);
            try {
                socket.receive(incoming);
                Packet packet = Packet.deserialize(incoming.getData(), incoming.getLength());
                packetQueue.add(packet);
            } catch (IOException e) {
                if (running) {
                    LOGGER.log(Level.WARNING, "Error receiving packet: ", e);
                }
            }
        }
    }

    public void send(Packet packet) {
        if (socket == null || socket.isClosed()) return;

        byte[] data = packet.serialize();
        DatagramPacket sendPacket = new DatagramPacket(data, data.length, serverAddress, port);
        try {
            socket.send(sendPacket);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error sending packet: ", e);
        }
    }

    /**
     * Retrieves and removes the next packet from the buffer queue.
     * Call this on the main game update thread.
     */
    public Packet pollPacket() {
        return packetQueue.poll();
    }
}
