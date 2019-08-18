package mekanism.common.network;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;
import mekanism.api.Coord4D;
import mekanism.api.TileNetworkList;
import mekanism.common.Mekanism;
import mekanism.common.PacketHandler;
import mekanism.common.frequency.Frequency;
import mekanism.common.frequency.FrequencyManager;
import mekanism.common.item.ItemPortableTeleporter;
import mekanism.common.tile.TileEntityTeleporter;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent.Context;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

public class PacketPortableTeleporter {

    private PortableTeleporterPacketType packetType;
    private List<Frequency> publicCache = new ArrayList<>();
    private List<Frequency> privateCache = new ArrayList<>();
    private Frequency frequency;
    private Hand currentHand;
    private byte status;

    public PacketPortableTeleporter(PortableTeleporterPacketType type, Hand hand, Frequency freq) {
        packetType = type;
        currentHand = hand;
        if (type == PortableTeleporterPacketType.DATA_REQUEST) {
            frequency = freq;
        } else if (type == PortableTeleporterPacketType.SET_FREQ) {
            frequency = freq;
        } else if (type == PortableTeleporterPacketType.DEL_FREQ) {
            frequency = freq;
        } else if (type == PortableTeleporterPacketType.TELEPORT) {
            frequency = freq;
        }
    }

    public Frequency getFrequency() {
        return frequency;
    }

    public byte getStatus() {
        return status;
    }

    public List<Frequency> getPrivateCache() {
        return privateCache;
    }

    public List<Frequency> getPublicCache() {
        return publicCache;
    }

    private PacketPortableTeleporter(Hand hand, Frequency freq, byte b, List<Frequency> publicFreqs, List<Frequency> privateFreqs) {
        packetType = PortableTeleporterPacketType.DATA_RESPONSE;

        currentHand = hand;
        frequency = freq;
        status = b;

        publicCache = publicFreqs;
        privateCache = privateFreqs;
    }

    public static void handle(PacketPortableTeleporter message, Supplier<Context> context) {
        PlayerEntity player = PacketHandler.getPlayer(context);
        if (player == null) {
            return;
        }
        context.get().enqueueWork(() -> {
            ItemStack itemstack = player.getHeldItem(message.currentHand);
            World world = player.world;
            if (!itemstack.isEmpty() && itemstack.getItem() instanceof ItemPortableTeleporter) {
                ItemPortableTeleporter item = (ItemPortableTeleporter) itemstack.getItem();
                switch (message.packetType) {
                    case DATA_REQUEST:
                        sendDataResponse(message.frequency, world, player, item, itemstack, message.currentHand);
                        break;
                    case DATA_RESPONSE:
                        Mekanism.proxy.handleTeleporterUpdate(message);
                        break;
                    case SET_FREQ:
                        FrequencyManager manager1 = getManager(message.frequency.isPublic() ? null : player.getUniqueID(), world);
                        Frequency toUse = null;
                        for (Frequency freq : manager1.getFrequencies()) {
                            if (freq.name.equals(message.frequency.name)) {
                                toUse = freq;
                                break;
                            }
                        }
                        if (toUse == null) {
                            toUse = new Frequency(message.frequency.name, player.getUniqueID()).setPublic(message.frequency.isPublic());
                            manager1.addFrequency(toUse);
                        }
                        item.setFrequency(itemstack, toUse);
                        sendDataResponse(toUse, world, player, item, itemstack, message.currentHand);
                        break;
                    case DEL_FREQ:
                        FrequencyManager manager = getManager(message.frequency.isPublic() ? null : player.getUniqueID(), world);
                        manager.remove(message.frequency.name, player.getUniqueID());
                        item.setFrequency(itemstack, null);
                        break;
                    case TELEPORT:
                        FrequencyManager manager2 = getManager(message.frequency.isPublic() ? null : player.getUniqueID(), world);
                        Frequency found = null;
                        for (Frequency freq : manager2.getFrequencies()) {
                            if (message.frequency.name.equals(freq.name)) {
                                found = freq;
                                break;
                            }
                        }
                        if (found == null) {
                            break;
                        }
                        Coord4D coords = found.getClosestCoords(new Coord4D(player));
                        if (coords != null) {
                            World teleWorld = ServerLifecycleHooks.getCurrentServer().getWorld(coords.dimension);
                            TileEntityTeleporter teleporter = (TileEntityTeleporter) coords.getTileEntity(teleWorld);
                            if (teleporter != null) {
                                try {
                                    teleporter.didTeleport.add(player.getUniqueID());
                                    teleporter.teleDelay = 5;
                                    item.setEnergy(itemstack, item.getEnergy(itemstack) - ItemPortableTeleporter.calculateEnergyCost(player, coords));
                                    if (player instanceof ServerPlayerEntity) {
                                        ((ServerPlayerEntity) player).connection.floatingTickCount = 0;
                                    }
                                    player.closeScreen();
                                    Mekanism.packetHandler.sendToAllTracking(new PacketPortalFX(new Coord4D(player)), coords);
                                    if (player instanceof ServerPlayerEntity) {
                                        TileEntityTeleporter.teleportPlayerTo((ServerPlayerEntity) player, coords, teleporter);
                                        TileEntityTeleporter.alignPlayer((ServerPlayerEntity) player, coords);
                                    }
                                    world.playSound(player, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1.0F, 1.0F);
                                    Mekanism.packetHandler.sendToAllTracking(new PacketPortalFX(coords), coords);
                                } catch (Exception ignored) {
                                }
                            }
                        }
                        break;
                }
            }
        });
    }

    public static void encode(PacketPortableTeleporter pkt, PacketBuffer buf) {
        buf.writeEnumValue(pkt.packetType);
        buf.writeEnumValue(pkt.currentHand);
        if (pkt.packetType == PortableTeleporterPacketType.DATA_REQUEST) {
            if (pkt.frequency != null) {
                buf.writeBoolean(true);
                buf.writeString(pkt.frequency.name);
                buf.writeBoolean(pkt.frequency.publicFreq);
            } else {
                buf.writeBoolean(false);
            }
        } else if (pkt.packetType == PortableTeleporterPacketType.DATA_RESPONSE) {
            if (pkt.frequency != null) {
                buf.writeBoolean(true);
                buf.writeString(pkt.frequency.name);
                buf.writeBoolean(pkt.frequency.publicFreq);
            } else {
                buf.writeBoolean(false);
            }
            buf.writeByte(pkt.status);

            TileNetworkList data = new TileNetworkList();
            data.add(pkt.publicCache.size());
            for (Frequency freq : pkt.publicCache) {
                freq.write(data);
            }
            data.add(pkt.privateCache.size());

            for (Frequency freq : pkt.privateCache) {
                freq.write(data);
            }

            PacketHandler.encode(data.toArray(), buf);
        } else if (pkt.packetType == PortableTeleporterPacketType.SET_FREQ) {
            buf.writeString(pkt.frequency.name);
            buf.writeBoolean(pkt.frequency.publicFreq);
        } else if (pkt.packetType == PortableTeleporterPacketType.DEL_FREQ) {
            buf.writeString(pkt.frequency.name);
            buf.writeBoolean(pkt.frequency.publicFreq);
        } else if (pkt.packetType == PortableTeleporterPacketType.TELEPORT) {
            buf.writeString(pkt.frequency.name);
            buf.writeBoolean(pkt.frequency.publicFreq);
        }
    }

    public static PacketPortableTeleporter decode(PacketBuffer buf) {
        PortableTeleporterPacketType packetType = buf.readEnumValue(PortableTeleporterPacketType.class);
        Hand currentHand = buf.readEnumValue(Hand.class);
        List<Frequency> publicCache = new ArrayList<>();
        List<Frequency> privateCache = new ArrayList<>();
        Frequency frequency = null;
        byte status = 0;
        if (packetType == PortableTeleporterPacketType.DATA_REQUEST) {
            if (buf.readBoolean()) {
                frequency = new Frequency(buf.readString(), null).setPublic(buf.readBoolean());
            }
        } else if (packetType == PortableTeleporterPacketType.DATA_RESPONSE) {
            if (buf.readBoolean()) {
                frequency = new Frequency(buf.readString(), null).setPublic(buf.readBoolean());
            }
            status = buf.readByte();

            int amount = buf.readInt();
            for (int i = 0; i < amount; i++) {
                publicCache.add(new Frequency(buf));
            }
            amount = buf.readInt();
            for (int i = 0; i < amount; i++) {
                privateCache.add(new Frequency(buf));
            }
        } else if (packetType == PortableTeleporterPacketType.SET_FREQ) {
            frequency = new Frequency(buf.readString(), null).setPublic(buf.readBoolean());
        } else if (packetType == PortableTeleporterPacketType.DEL_FREQ) {
            frequency = new Frequency(buf.readString(), null).setPublic(buf.readBoolean());
        } else if (packetType == PortableTeleporterPacketType.TELEPORT) {
            frequency = new Frequency(buf.readString(), null).setPublic(buf.readBoolean());
        }
        return new PacketPortableTeleporter(currentHand, frequency, status, publicCache, privateCache);
    }

    public static void sendDataResponse(Frequency given, World world, PlayerEntity player, ItemPortableTeleporter item, ItemStack itemstack, Hand hand) {
        List<Frequency> publicFreqs = new ArrayList<>(getManager(null, world).getFrequencies());
        List<Frequency> privateFreqs = new ArrayList<>(getManager(player.getUniqueID(), world).getFrequencies());
        byte status = 3;
        if (given != null) {
            FrequencyManager manager = given.isPublic() ? getManager(null, world) : getManager(player.getUniqueID(), world);
            boolean found = false;
            for (Frequency iterFreq : manager.getFrequencies()) {
                if (given.equals(iterFreq)) {
                    given = iterFreq;
                    found = true;
                    break;
                }
            }
            if (!found) {
                given = null;
            }
        }

        if (given != null) {
            if (given.activeCoords.size() == 0) {
                status = 3;
            } else {
                Coord4D coords = given.getClosestCoords(new Coord4D(player));
                double energyNeeded = ItemPortableTeleporter.calculateEnergyCost(player, coords);
                if (energyNeeded > item.getEnergy(itemstack)) {
                    status = 4;
                } else {
                    status = 1;
                }
            }
        }
        Mekanism.packetHandler.sendTo(new PacketPortableTeleporter(hand, given, status, publicFreqs, privateFreqs), (ServerPlayerEntity) player);
    }

    public static FrequencyManager getManager(UUID owner, World world) {
        if (owner == null) {
            return Mekanism.publicTeleporters;
        } else if (!Mekanism.privateTeleporters.containsKey(owner)) {
            FrequencyManager manager = new FrequencyManager(Frequency.class, Frequency.TELEPORTER, owner);
            Mekanism.privateTeleporters.put(owner, manager);
            manager.createOrLoad(world);
        }
        return Mekanism.privateTeleporters.get(owner);
    }

    public enum PortableTeleporterPacketType {
        DATA_REQUEST,
        DATA_RESPONSE,
        SET_FREQ,
        DEL_FREQ,
        TELEPORT
    }
}