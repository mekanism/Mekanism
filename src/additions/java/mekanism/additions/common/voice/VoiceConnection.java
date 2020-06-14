package mekanism.additions.common.voice;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import mekanism.additions.common.MekanismAdditions;
import mekanism.additions.common.item.ItemWalkieTalkie;
import mekanism.common.Mekanism;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

public class VoiceConnection extends Thread {

    private final MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
    private DataOutputStream output;
    private DataInputStream input;
    private boolean open = true;
    private final Socket socket;
    private UUID uuid;

    public VoiceConnection(Socket s) {
        socket = s;
    }

    @Override
    public void run() {
        try {
            input = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            output = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
            synchronized (MekanismAdditions.voiceManager) {
                int retryCount = 0;
                while (uuid == null && retryCount <= 100) {
                    try {
                        List<ServerPlayerEntity> l = Collections.synchronizedList(new ArrayList<>(server.getPlayerList().getPlayers()));

                        for (ServerPlayerEntity playerMP : l) {
                            String playerIP = playerMP.getPlayerIP();
                            if (!server.isDedicatedServer() && playerIP.equals("local") && !MekanismAdditions.voiceManager.isFoundLocal()) {
                                MekanismAdditions.voiceManager.setFoundLocal(true);
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
                    Mekanism.logger.info("VoiceServer: Traced IP in {} attempts.", retryCount);
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
                        MekanismAdditions.voiceManager.sendToPlayers(byteCount, audioData, VoiceConnection.this);
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
            MekanismAdditions.voiceManager.removeConnection(this);
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

    public ServerPlayerEntity getPlayer() {
        return server.getPlayerList().getPlayerByUUID(uuid);
    }
}