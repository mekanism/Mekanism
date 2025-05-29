package mekanism.additions.client.voice;

import java.io.EOFException;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;
import mekanism.common.Mekanism;

public class VoiceOutput extends Thread {

    private final VoiceClient voiceClient;
    private final DataLine.Info speaker;
    private SourceDataLine sourceLine;

    public VoiceOutput(VoiceClient client) {
        super("VoiceServer Client Output Thread");
        voiceClient = client;
        speaker = new DataLine.Info(SourceDataLine.class, voiceClient.getAudioFormat(), 2_200);
        setDaemon(true);
    }

    @Override
    public void run() {
        try {
            sourceLine = (SourceDataLine) AudioSystem.getLine(speaker);
            sourceLine.open(voiceClient.getAudioFormat(), 2_200);
            sourceLine.start();
            byte[] audioData = new byte[4_096]; //less allocation/gc (if done outside the loop)
            int byteCount;
            int length;
            while (voiceClient.isRunning()) {
                try {
                    if (voiceClient.getInputStream().available() > 0) {
                        //Why would we only read signed shorts? negative amount of waiting data doesn't make sense anyway :D
                        byteCount = voiceClient.getInputStream().readUnsignedShort();
                        while (byteCount > 0 && voiceClient.isRunning()) {
                            length = audioData.length;
                            if (length > byteCount) {
                                length = byteCount;
                            }
                            //That one returns the actual read amount of data (we can begin transferring even if input
                            // is waiting/incomplete)
                            length = voiceClient.getInputStream().read(audioData, 0, length);
                            if (length < 0) {
                                throw new EOFException();
                            }
                            sourceLine.write(audioData, 0, length);
                            byteCount -= length;
                        }
                    } else {
                        sleep(20);
                    }
                } catch (EOFException eof) {
                    Mekanism.logger.error("VoiceServer: Unexpected input EOF Exception occurred.");
                    break; // voiceClient.input will continue throwing EOFs -> no need to go on checking
                } catch (InterruptedException e) {
                    break;
                } catch (Exception e) {
                    Mekanism.logger.error("VoiceServer: Unexpected Exception", e);
                }
            }
        } catch (Exception e) {
            Mekanism.logger.error("VoiceServer: Error while running client output thread.", e);
        }
    }

    public void close() {
        sourceLine.flush();
        sourceLine.close();
    }
}