package mekanism.additions.common.voice;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Set;
import mekanism.additions.common.config.MekanismAdditionsConfig;
import mekanism.common.Mekanism;

public class VoiceServerManager {

    private final Set<VoiceConnection> connections = new ObjectOpenHashSet<>();
    private ServerSocket serverSocket;
    private Thread listenThread;
    private boolean foundLocal = false;
    private boolean running;

    public void start() {
        Mekanism.logger.info("VoiceServer: Starting up server...");
        try {
            running = true;
            serverSocket = new ServerSocket(MekanismAdditionsConfig.additions.voicePort.get());
            (listenThread = new ListenThread()).start();
        } catch (Exception ignored) {
        }
    }

    public void stop() {
        try {
            Mekanism.logger.info("VoiceServer: Shutting down server...");
            try {
                listenThread.interrupt();
            } catch (Exception ignored) {
            }
            foundLocal = false;
            try {
                serverSocket.close();
                serverSocket = null;
            } catch (Exception ignored) {
            }
        } catch (Exception e) {
            Mekanism.logger.error("VoiceServer: Error while shutting down server.", e);
        }
        running = false;
    }

    public void removeConnection(VoiceConnection connection) {
        connections.remove(connection);
    }

    public boolean isFoundLocal() {
        return foundLocal;
    }

    public void setFoundLocal(boolean found) {
        foundLocal = found;
    }

    public void sendToPlayers(short byteCount, byte[] audioData, VoiceConnection connection) {
        if (connection.getPlayer() == null) {
            return;
        }
        int channel = connection.getCurrentChannel();
        if (channel == 0) {
            return;
        }
        for (VoiceConnection iterConn : connections) {
            if (iterConn.getPlayer() != null && iterConn != connection && iterConn.canListen(channel)) {
                iterConn.sendToPlayer(byteCount, audioData, connection);
            }
        }
    }

    private class ListenThread extends Thread {

        private ListenThread() {
            super("VoiceServer Listen Thread");
            setDaemon(true);
        }

        @Override
        public void run() {
            while (running) {
                try {
                    Socket s = serverSocket.accept();
                    VoiceConnection connection = new VoiceConnection(s);
                    connection.start();
                    connections.add(connection);
                    Mekanism.logger.info("VoiceServer: Accepted new connection.");
                } catch (SocketException | NullPointerException ignored) {
                } catch (Exception e) {
                    Mekanism.logger.error("VoiceServer: Error while accepting connection.", e);
                }
            }
        }
    }
}