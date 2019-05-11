package mekanism.common.voice;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import mekanism.common.Mekanism;
import mekanism.common.item.ItemWalkieTalkie;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class VoiceConnection extends Thread {

    private MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
    private DataOutputStream output;
    private DataInputStream input;
    private boolean open = true;
    private Socket socket;
    private UUID uuid;

    public VoiceConnection(Socket s) {
        socket = s;
    }

    @Override
    public void run() {
        try {
            input = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            output = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));

            synchronized (Mekanism.voiceManager) {
                int retryCount = 0;

                while (uuid == null && retryCount <= 100) {
                    try {
                        List<EntityPlayerMP> l = Collections
                              .synchronizedList(new ArrayList<>(server.getPlayerList().getPlayers()));

                        for (EntityPlayerMP playerMP : l) {
                            String playerIP = playerMP.getPlayerIP();

                            if (!server.isDedicatedServer() && playerIP.equals("local") && !Mekanism.voiceManager
                                  .isFoundLocal()) {
                                Mekanism.voiceManager.setFoundLocal(true);
                                uuid = playerMP.getUniqueID();
                                break;
                            } else if (playerIP.equals(socket.getInetAddress().getHostAddress())) {
                                uuid = playerMP.getUniqueID();
                                break;
                            }
                        }

                        retryCount++;
                        Thread.sleep(50);
                    } catch (Exception ignored) {
                    }
                }

                if (uuid == null) {
                    Mekanism.logger.error("VoiceServer: Unable to trace connection's IP address.");
                    kill();
                    return;
                } else {
                    Mekanism.logger.info("VoiceServer: Traced IP in " + retryCount + " attempts.");
                }
            }
        } catch (Exception e) {
            Mekanism.logger.error("VoiceServer: Error while starting server-based connection.", e);
            open = false;
        }

        // Main client listen thread
        new Thread(() -> {
            while (open) {
                try {
                    short byteCount = VoiceConnection.this.input.readShort();
                    byte[] audioData = new byte[byteCount];
                    VoiceConnection.this.input.readFully(audioData);

                    if (byteCount > 0) {
                        Mekanism.voiceManager.sendToPlayers(byteCount, audioData, VoiceConnection.this);
                    }
                } catch (Exception e) {
                    open = false;
                }
            }

            if (!open) {
                kill();
            }
        }).start();
    }

    private void kill() {
        try {
            if (input != null) {
                input.close();
            }
            if (output != null) {
                output.close();
            }
            if (socket != null) {
                socket.close();
            }
            Mekanism.voiceManager.removeConnection(this);
        } catch (Exception e) {
            Mekanism.logger.error("VoiceServer: Error while stopping server-based connection.", e);
        }
    }

    public void sendToPlayer(short byteCount, byte[] audioData, VoiceConnection connection) {
        if (!open) {
            kill();
        }

        try {
            output.writeShort(byteCount);
            output.write(audioData);

            output.flush();
        } catch (Exception e) {
            Mekanism.logger.error("VoiceServer: Error while sending data to player.", e);
        }
    }

    public boolean canListen(int channel) {
        return getPlayer().inventory.mainInventory.stream().anyMatch(itemStack -> canListen(channel, itemStack))
               || getPlayer().inventory.offHandInventory.stream().anyMatch(itemStack -> canListen(channel, itemStack));
    }

    private boolean canListen(int channel, ItemStack itemStack) {
        if (!itemStack.isEmpty() && itemStack.getItem() instanceof ItemWalkieTalkie) {
            ItemWalkieTalkie walkieTalkie = (ItemWalkieTalkie) itemStack.getItem();
            return walkieTalkie.getOn(itemStack) && walkieTalkie.getChannel(itemStack) == channel;
        }

        return false;
    }

    public int getCurrentChannel() {
        ItemStack itemStack = getPlayer().inventory.getCurrentItem();

        if (!itemStack.isEmpty() && itemStack.getItem() instanceof ItemWalkieTalkie) {
            ItemWalkieTalkie walkieTalkie = (ItemWalkieTalkie) itemStack.getItem();
            if (walkieTalkie.getOn(itemStack)) {
                return walkieTalkie.getChannel(itemStack);
            }
        }

        return 0;
    }

    public EntityPlayerMP getPlayer() {
        return server.getPlayerList().getPlayerByUUID(uuid);
    }
}