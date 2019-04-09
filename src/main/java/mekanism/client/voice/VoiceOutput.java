package mekanism.client.voice;

import java.io.EOFException;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;
import mekanism.common.Mekanism;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class VoiceOutput extends Thread {

    public VoiceClient voiceClient;

    public DataLine.Info speaker;

    public SourceDataLine sourceLine;

    public VoiceOutput(VoiceClient client) {
        voiceClient = client;
        speaker = new DataLine.Info(SourceDataLine.class, voiceClient.format, 2200);

        setDaemon(true);
        setName("VoiceServer Client Output Thread");
    }

    @Override
    public void run() {
        try {
            sourceLine = ((SourceDataLine) AudioSystem.getLine(speaker));
            sourceLine.open(voiceClient.format, 2200);
            sourceLine.start();
            byte[] audioData = new byte[4096]; // less allocation/gc (if done outside the loop)
            int byteCount;
            int length;
            while (voiceClient.running) {
                try {
                    byteCount = voiceClient.input.readUnsignedShort(); // Why would we only read signed shorts? negative
                    // amount of waiting data doesn't make sense
                    // anyway :D
                    while (byteCount > 0 && voiceClient.running) {
                        length = audioData.length;
                        if (length > byteCount) {
                            length = byteCount;
                        }
                        length = voiceClient.input.read(audioData, 0, length); // That one returns the actual read
                        // amount of data (we can begin
                        // transferring even if input is
                        // waiting/incomplete)
                        if (length < 0) {
                            throw new EOFException();
                        }
                        sourceLine.write(audioData, 0, length);
                        byteCount -= length;
                    }
                } catch (EOFException eof) {
                    Mekanism.logger.error("VoiceServer: Unexpected input EOF Exception occured.");
                    break; // voiceClient.input will continue throwing EOFs -> no need to go on checking
                } catch (Exception e) {
                    /* Would a debug output be good here? */
                }
            }
        } catch (Exception e) {
            Mekanism.logger.error("VoiceServer: Error while running client output thread.");
            e.printStackTrace();
        }
    }

    public void close() {
        sourceLine.flush();
        sourceLine.close();
    }
}