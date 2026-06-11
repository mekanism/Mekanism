package mekanism.additions.client.voice;

import java.io.DataOutputStream;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.TargetDataLine;
import mekanism.additions.client.AdditionsKeyHandler;
import mekanism.common.Mekanism;
import org.jspecify.annotations.Nullable;

public class VoiceInput extends Thread {

    private final VoiceClient voiceClient;
    private final DataLine.Info microphone;
    @Nullable
    private TargetDataLine targetLine;

    public VoiceInput(VoiceClient client) {
        super("VoiceServer Client Input Thread");
        voiceClient = client;
        microphone = new DataLine.Info(TargetDataLine.class, voiceClient.getAudioFormat(), 2_200);
        setDaemon(true);
    }

    @Override
    public void run() {
        try {
            if (!AudioSystem.isLineSupported(microphone)) {
                Mekanism.logger.info("No audio system available.");
                return;
            }
            targetLine = (TargetDataLine) AudioSystem.getLine(microphone);
            targetLine.open(voiceClient.getAudioFormat(), 2_200);
            targetLine.start();
            AudioInputStream audioInput = new AudioInputStream(targetLine);

            boolean doFlush = false;
            while (voiceClient.isRunning()) {
                if (AdditionsKeyHandler.voiceKey.isDown()) {
                    targetLine.flush();
                    while (voiceClient.isRunning() && AdditionsKeyHandler.voiceKey.isDown()) {
                        try {
                            int availableBytes = audioInput.available();
                            byte[] audioData = new byte[Math.min(availableBytes, 2_200)];
                            int bytesRead = audioInput.read(audioData, 0, audioData.length);
                            if (bytesRead > 0) {
                                DataOutputStream outputStream = voiceClient.getOutputStream();
                                if (outputStream != null) {
                                    outputStream.writeShort(audioData.length);
                                    outputStream.write(audioData);
                                }
                            }
                        } catch (Exception _) {
                        }
                    }
                    try {
                        sleep(200L);
                    } catch (InterruptedException _) {
                    }
                    doFlush = true;
                } else if (doFlush) {
                    DataOutputStream outputStream = voiceClient.getOutputStream();
                    if (outputStream != null) {
                        try {
                            outputStream.flush();
                        } catch (Exception _) {
                        }
                    }
                    doFlush = false;
                }
                try {
                    sleep(20L);
                } catch (InterruptedException _) {
                }
            }
            audioInput.close();
        } catch (Exception e) {
            Mekanism.logger.error("VoiceServer: Error while running client input thread.", e);
        }
    }

    public void close() {
        if (targetLine != null) {
            targetLine.flush();
            targetLine.close();
        }
    }
}