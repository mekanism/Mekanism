package mekanism.common.network;

import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;
import mekanism.api.Action;
import mekanism.api.Coord4D;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.inventory.AutomationType;
import mekanism.api.math.FloatingLong;
import mekanism.common.Mekanism;
import mekanism.common.frequency.Frequency;
import mekanism.common.frequency.FrequencyManager;
import mekanism.common.frequency.FrequencyType;
import mekanism.common.item.ItemPortableTeleporter;
import mekanism.common.tile.TileEntityTeleporter;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.StorageUtils;
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

public class PacketPortableTeleporterGui {

    private PortableTeleporterPacketType packetType;
    private Frequency frequency;
    private Hand currentHand;

    public PacketPortableTeleporterGui(PortableTeleporterPacketType type, Hand hand, Frequency freq) {
        packetType = type;
        currentHand = hand;
        frequency = freq;
    }

    public static void handle(PacketPortableTeleporterGui message, Supplier<Context> context) {
        PlayerEntity player = BasePacketHandler.getPlayer(context);
        if (player == null) {
            return;
        }
        context.get().enqueueWork(() -> {
            ItemStack stack = player.getHeldItem(message.currentHand);
            World world = player.world;
            if (!stack.isEmpty() && stack.getItem() instanceof ItemPortableTeleporter) {
                ItemPortableTeleporter item = (ItemPortableTeleporter) stack.getItem();
                switch (message.packetType) {
                    case DATA_REQUEST:
                        sendDataResponse(message.frequency, world, player, item, stack, message.currentHand);
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
                        item.setFrequency(stack, toUse);
                        sendDataResponse(toUse, world, player, item, stack, message.currentHand);
                        break;
                    case DEL_FREQ:
                        FrequencyManager manager = getManager(message.frequency.isPublic() ? null : player.getUniqueID(), world);
                        manager.remove(message.frequency.name, player.getUniqueID());
                        item.setFrequency(stack, null);
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
                            TileEntityTeleporter teleporter = MekanismUtils.getTileEntity(TileEntityTeleporter.class, teleWorld, coords.getPos());
                            if (teleporter != null) {
                                try {
                                    if (!player.isCreative()) {
                                        FloatingLong energyCost = TileEntityTeleporter.calculateEnergyCost(player, coords);
                                        IEnergyContainer energyContainer = StorageUtils.getEnergyContainer(stack, 0);
                                        if (energyContainer == null || energyContainer.extract(energyCost, Action.SIMULATE, AutomationType.MANUAL).smallerThan(energyCost)) {
                                            break;
                                        }
                                        energyContainer.extract(energyCost, Action.EXECUTE, AutomationType.MANUAL);
                                    }
                                    teleporter.didTeleport.add(player.getUniqueID());
                                    teleporter.teleDelay = 5;
                                    if (player instanceof ServerPlayerEntity) {
                                        ((ServerPlayerEntity) player).connection.floatingTickCount = 0;
                                    }
                                    player.closeScreen();
                                    Mekanism.packetHandler.sendToAllTracking(new PacketPortalFX(player.getPosition()), world, coords.getPos());
                                    TileEntityTeleporter.teleportEntityTo(player, coords, teleporter);
                                    if (player instanceof ServerPlayerEntity) {
                                        TileEntityTeleporter.alignPlayer((ServerPlayerEntity) player, coords);
                                    }
                                    world.playSound(player, player.getPosX(), player.getPosY(), player.getPosZ(), SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1.0F, 1.0F);
                                    Mekanism.packetHandler.sendToAllTracking(new PacketPortalFX(coords), teleWorld, coords.getPos());
                                } catch (Exception ignored) {
                                }
                            }
                        }
                        break;
                }
            }
        });
        context.get().setPacketHandled(true);
    }

    public static void encode(PacketPortableTeleporterGui pkt, PacketBuffer buf) {
        buf.writeEnumValue(pkt.packetType);
        buf.writeEnumValue(pkt.currentHand);
        if (pkt.frequency == null) {
            buf.writeBoolean(false);
        } else {
            buf.writeBoolean(true);
            buf.writeString(pkt.frequency.name);
            buf.writeBoolean(pkt.frequency.publicFreq);
        }

    }

    public static PacketPortableTeleporterGui decode(PacketBuffer buf) {
        PortableTeleporterPacketType packetType = buf.readEnumValue(PortableTeleporterPacketType.class);
        Hand currentHand = buf.readEnumValue(Hand.class);
        Frequency frequency = null;
        if (buf.readBoolean()) {
            frequency = new Frequency(BasePacketHandler.readString(buf), null).setPublic(buf.readBoolean());
        }
        return new PacketPortableTeleporterGui(packetType, currentHand, frequency);
    }

    private static void sendDataResponse(Frequency given, World world, PlayerEntity player, ItemPortableTeleporter item, ItemStack stack, Hand hand) {
        List<Frequency> publicFreqs = getManager(null, world).getFrequencies();
        List<Frequency> privateFreqs = getManager(player.getUniqueID(), world).getFrequencies();
        byte status = 3;
        if (given != null) {
            List<Frequency> frequencies = given.isPublic() ? publicFreqs : privateFreqs;
            for (Frequency iterFreq : frequencies) {
                if (given.equals(iterFreq)) {
                    given = iterFreq;
                    if (!given.activeCoords.isEmpty()) {
                        if (!player.isCreative()) {
                            Coord4D coords = given.getClosestCoords(new Coord4D(player));
                            if (coords != null) {
                                FloatingLong energyNeeded = TileEntityTeleporter.calculateEnergyCost(player, coords);
                                IEnergyContainer energyContainer = StorageUtils.getEnergyContainer(stack, 0);
                                if (energyContainer == null || energyContainer.extract(energyNeeded, Action.SIMULATE, AutomationType.MANUAL).smallerThan(energyNeeded)) {
                                    status = 4;
                                    break;
                                }
                            }
                        }
                        status = 1;
                    }
                    break;
                }
            }
        }
        Mekanism.packetHandler.sendTo(new PacketPortableTeleporter(hand, given, status, publicFreqs, privateFreqs), (ServerPlayerEntity) player);
    }

    public static FrequencyManager getManager(UUID owner, World world) {
        if (owner == null) {
            return Mekanism.publicTeleporters;
        } else if (!Mekanism.privateTeleporters.containsKey(owner)) {
            FrequencyManager manager = new FrequencyManager(FrequencyType.BASE, Frequency.TELEPORTER, owner);
            Mekanism.privateTeleporters.put(owner, manager);
            if (!world.isRemote()) {
                manager.createOrLoad();
            }
        }
        return Mekanism.privateTeleporters.get(owner);
    }

    public enum PortableTeleporterPacketType {
        DATA_REQUEST,
        SET_FREQ,
        DEL_FREQ,
        TELEPORT
    }
}