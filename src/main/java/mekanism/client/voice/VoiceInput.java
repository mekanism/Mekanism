package mekanism.client.voice;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.TargetDataLine;
import mekanism.client.MekanismKeyHandler;
import mekanism.common.Mekanism;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class VoiceInput extends Thread {

    private VoiceClient voiceClient;

    private DataLine.Info microphone;

    private TargetDataLine targetLine;

    public VoiceInput(VoiceClient client) {
        voiceClient = client;
        microphone = new DataLine.Info(TargetDataLine.class, voiceClient.format, 2200);

        setDaemon(true);
        setName("VoiceServer Client Input Thread");
    }

    @Override
    public void run() {
        try {
            if (!AudioSystem.isLineSupported(microphone)) {
                Mekanism.logger.info("No audio system available.");
                return;
            }
            targetLine = ((TargetDataLine) AudioSystem.getLine(microphone));
            targetLine.open(voiceClient.format, 2200);
            targetLine.start();
            AudioInputStream audioInput = new AudioInputStream(targetLine);

            boolean doFlush = false;

            while (voiceClient.running) {
                if (MekanismKeyHandler.voiceKey.isPressed()) {
                    targetLine.flush();

                    while (voiceClient.running && MekanismKeyHandler.voiceKey.isPressed()) {
                        try {
                            int availableBytes = audioInput.available();
                            byte[] audioData = new byte[availableBytes > 2200 ? 2200 : availableBytes];
                            int bytesRead = audioInput.read(audioData, 0, audioData.length);

                            if (bytesRead > 0) {
                                voiceClient.output.writeShort(audioData.length);
                                voiceClient.output.write(audioData);
                            }
                        } catch (Exception e) {
                        }
                    }

                    try {
                        Thread.sleep(200L);
                    } catch (Exception e) {
                    }

                    doFlush = true;
                } else if (doFlush) {
                    try {
                        voiceClient.output.flush();
                    } catch (Exception e) {
                    }

                    doFlush = false;
                }

                try {
                    Thread.sleep(20L);
                } catch (Exception e) {
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