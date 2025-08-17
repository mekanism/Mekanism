package mekanism.additions.client.voice;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.TargetDataLine;
import mekanism.additions.client.AdditionsKeyHandler;
import mekanism.common.Mekanism;

public class VoiceInput extends Thread {

    private final VoiceClient voiceClient;
    private final DataLine.Info microphone;
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
                                voiceClient.getOutputStream().writeShort(audioData.length);
                                voiceClient.getOutputStream().write(audioData);
                            }
                        } catch (Exception ignored) {
                        }
                    }
                    try {
                        sleep(200L);
                    } catch (InterruptedException ignored) {
                    }
                    doFlush = true;
                } else if (doFlush) {
                    try {
                        voiceClient.getOutputStream().flush();
                    } catch (Exception ignored) {
                    }
                    doFlush = false;
                }
                try {
                    sleep(20L);
                } catch (InterruptedException ignored) {
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