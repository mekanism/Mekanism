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
import mekanism.additions.common.item.ItemWalkieTalkie.WalkieData;
import mekanism.additions.common.registries.AdditionsDataComponents;
import mekanism.common.Mekanism;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.PlayerInventoryWrapper;
import org.jspecify.annotations.Nullable;

public class VoiceConnection extends Thread {

    private final MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
    @Nullable
    private DataOutputStream output;
    @Nullable
    private DataInputStream input;
    private boolean open = true;
    private final Socket socket;
    @Nullable
    private UUID uuid;

    VoiceConnection(Socket s) {
        super("VoiceServer Connection Thread");
        socket = s;
        //Note: This is set as a Daemon thread because the parent thread is a daemon thread
    }

    @Override
    public void run() {
        try {
            input = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            output = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
            if (MekanismAdditions.voiceManager != null) {
                synchronized (MekanismAdditions.voiceManager) {
                    int retryCount = 0;
                    while (uuid == null && retryCount <= 100) {
                        try {
                            List<ServerPlayer> players = Collections.synchronizedList(new ArrayList<>(server.getPlayerList().getPlayers()));
                            for (ServerPlayer player : players) {
                                String playerIP = player.getIpAddress();
                                if (!server.isDedicatedServer() && playerIP.equals("local") && !MekanismAdditions.voiceManager.isFoundLocal()) {
                                    MekanismAdditions.voiceManager.setFoundLocal(true);
                                    uuid = player.getUUID();
                                    break;
                                } else if (playerIP.equals(socket.getInetAddress().getHostAddress())) {
                                    uuid = player.getUUID();
                                    break;
                                }
                            }
                            retryCount++;
                            sleep(50);
                        } catch (Exception _) {
                        }
                    }

                    if (uuid == null) {
                        Mekanism.logger.error("VoiceServer: Unable to trace connection's IP address.");
                        kill();
                        return;
                    } else {
                        Mekanism.logger.info("VoiceServer: Traced IP for {} in {} attempts.", uuid, retryCount);
                    }
                }
            }
        } catch (Exception e) {
            Mekanism.logger.error("VoiceServer: Error while starting server-based connection.", e);
            open = false;
        }

        // Main client listen thread (set to daemon because the voice connection is a daemon)
        new Thread(() -> {
            while (open) {
                if (this.input != null && MekanismAdditions.voiceManager != null) {
                    try {
                        short byteCount = this.input.readShort();
                        byte[] audioData = new byte[byteCount];
                        this.input.readFully(audioData);
                        if (byteCount > 0) {
                            MekanismAdditions.voiceManager.sendToPlayers(byteCount, audioData, this);
                        }
                    } catch (Exception e) {
                        open = false;
                    }
                } else {
                    open = false;
                }
            }
            //No longer open, kill it
            kill();
        }, "Voice Server Client Listen Thread").start();
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
            if (MekanismAdditions.voiceManager != null) {//Should never be null
                MekanismAdditions.voiceManager.removeConnection(this);
            }
        } catch (Exception e) {
            Mekanism.logger.error("VoiceServer: Error while stopping server-based connection.", e);
        }
    }

    public void sendToPlayer(short byteCount, byte[] audioData, VoiceConnection connection) {
        if (!open) {
            kill();
            return;
        } else if (output == null) {
            Mekanism.logger.error("VoiceServer: Error while sending data to player.");
            return;
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
        PlayerInventoryWrapper playerInv = PlayerInventoryWrapper.of(getPlayer());
        //TODO: Should we just check the entire inventory and maybe curios as well?
        return canListen(playerInv.getMainSlots(), channel) || canListen(playerInv.getHandSlot(InteractionHand.OFF_HAND), channel);
    }

    private boolean canListen(ResourceHandler<ItemResource> itemHandler, int channel) {
        for (int slot = 0, size = itemHandler.size(); slot < size; slot++) {
            ItemResource itemType = itemHandler.getResource(slot);
            if (!itemType.isEmpty()) {
                WalkieData data = itemType.getOrDefault(AdditionsDataComponents.WALKIE_DATA, WalkieData.DEFAULT);
                if (data.running() && data.channel() == channel) {
                    return true;
                }
            }
        }
        return false;
    }

    public int getCurrentChannel() {
        ServerPlayer player = getPlayer();
        if (player == null) {
            return 0;
        }
        int channel = getCurrentChannel(player.getMainHandItem());
        if (channel == 0) {
            channel = getCurrentChannel(player.getOffhandItem());
        }
        return channel;
    }

    private int getCurrentChannel(ItemStack stack) {
        WalkieData data = stack.getOrDefault(AdditionsDataComponents.WALKIE_DATA, WalkieData.DEFAULT);
        return data.running() ? data.channel() : 0;
    }

    @Nullable
    public ServerPlayer getPlayer() {
        return uuid == null ? null : server.getPlayerList().getPlayer(uuid);
    }
}