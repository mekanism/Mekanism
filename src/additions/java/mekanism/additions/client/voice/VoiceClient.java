package mekanism.additions.client.voice;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.ConnectException;
import java.net.Socket;
import javax.sound.sampled.AudioFormat;
import mekanism.additions.common.config.MekanismAdditionsConfig;
import mekanism.common.Mekanism;

public class VoiceClient extends Thread {

    private final AudioFormat format = new AudioFormat(16_000F, 16, 1, true, true);
    private VoiceOutput outputThread;
    private VoiceInput inputThread;
    private DataOutputStream output;
    private DataInputStream input;
    private boolean running;
    private Socket socket;
    private final String ip;

    public VoiceClient(String ip) {
        super("VoiceServer Client Thread " + ip);
        this.ip = ip;
        setDaemon(true);
    }

    @Override
    public void run() {
        Mekanism.logger.info("VoiceServer: Starting client connection...");

        try {
            socket = new Socket(ip, MekanismAdditionsConfig.additions.voicePort.get());
            running = true;

            input = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            output = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));

            (outputThread = new VoiceOutput(this)).start();
            (inputThread = new VoiceInput(this)).start();

            Mekanism.logger.info("VoiceServer: Successfully connected to server.");
        } catch (ConnectException e) {
            Mekanism.logger.error("VoiceServer: Server's VoiceServer is disabled.");
        } catch (Exception e) {
            Mekanism.logger.error("VoiceServer: Error while starting client connection.", e);
        }
    }

    public void disconnect() {
        Mekanism.logger.info("VoiceServer: Stopping client connection...");

        try {
            if (inputThread != null) {
                inputThread.interrupt();
                inputThread.close();
            }
            if (outputThread != null) {
                outputThread.interrupt();
                outputThread.close();
            }
            if (output != null) {
                output.close();
            }
            if (input != null) {
                input.close();
            }
            if (socket != null) {
                socket.close();
            }

            interrupt();

            running = false;
        } catch (Exception e) {
            Mekanism.logger.error("VoiceServer: Error while stopping client connection.", e);
        }
    }

    public boolean isRunning() {
        return running;
    }

    public DataOutputStream getOutputStream() {
        return output;
    }

    public DataInputStream getInputStream() {
        return input;
    }

    public AudioFormat getAudioFormat() {
        return format;
    }
}