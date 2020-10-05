package mekanism.common.network;

import java.util.function.Supplier;
import mekanism.api.Action;
import mekanism.api.Coord4D;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.inventory.AutomationType;
import mekanism.api.math.FloatingLong;
import mekanism.common.Mekanism;
import mekanism.common.content.teleporter.TeleporterFrequency;
import mekanism.common.item.ItemPortableTeleporter;
import mekanism.common.lib.frequency.FrequencyManager;
import mekanism.common.lib.frequency.FrequencyType;
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
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent.Context;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

public class PacketPortableTeleporterGui {

    private final PortableTeleporterPacketType packetType;
    private final TeleporterFrequency frequency;
    private final Hand currentHand;

    public PacketPortableTeleporterGui(PortableTeleporterPacketType type, Hand hand, TeleporterFrequency freq) {
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
            if (!stack.isEmpty() && stack.getItem() instanceof ItemPortableTeleporter) {
                if (message.packetType == PortableTeleporterPacketType.DATA_REQUEST) {
                    sendDataResponse(message.frequency, player, stack);
                } else if (message.packetType == PortableTeleporterPacketType.TELEPORT) {
                    FrequencyManager<TeleporterFrequency> manager2 = FrequencyType.TELEPORTER.getManager(message.frequency.isPublic() ? null : player.getUniqueID());
                    TeleporterFrequency found = manager2.getFrequency(message.frequency.getName());
                    if (found == null) {
                        return;
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
                                        return;
                                    }
                                    energyContainer.extract(energyCost, Action.EXECUTE, AutomationType.MANUAL);
                                }
                                teleporter.didTeleport.add(player.getUniqueID());
                                teleporter.teleDelay = 5;
                                if (player instanceof ServerPlayerEntity) {
                                    ((ServerPlayerEntity) player).connection.floatingTickCount = 0;
                                }
                                player.closeScreen();
                                Mekanism.packetHandler.sendToAllTracking(new PacketPortalFX(player.getPosition()), player.world, coords.getPos());
                                TileEntityTeleporter.teleportEntityTo(player, coords, teleporter);
                                BlockPos coordsPos = coords.getPos();
                                if (teleporter.frameDirection() != null) {
                                    coordsPos = coordsPos.down().offset(teleporter.frameDirection());
                                }
                                if (player instanceof ServerPlayerEntity) {
                                    TileEntityTeleporter.alignPlayer((ServerPlayerEntity) player, coordsPos);
                                }
                                player.world.playSound(null, player.getPosX(), player.getPosY(), player.getPosZ(), SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1.0F, 1.0F);
                                Mekanism.packetHandler.sendToAllTracking(new PacketPortalFX(coordsPos), teleWorld, coordsPos);
                            } catch (Exception ignored) {
                            }
                        }
                    }
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
            buf.writeString(pkt.frequency.getName());
            buf.writeBoolean(pkt.frequency.isPublic());
        }

    }

    public static PacketPortableTeleporterGui decode(PacketBuffer buf) {
        PortableTeleporterPacketType packetType = buf.readEnumValue(PortableTeleporterPacketType.class);
        Hand currentHand = buf.readEnumValue(Hand.class);
        TeleporterFrequency frequency = null;
        if (buf.readBoolean()) {
            frequency = new TeleporterFrequency(BasePacketHandler.readString(buf), null);
            frequency.setPublic(buf.readBoolean());
        }
        return new PacketPortableTeleporterGui(packetType, currentHand, frequency);
    }

    private static void sendDataResponse(TeleporterFrequency given, PlayerEntity player, ItemStack stack) {
        byte status = 3;
        if (given != null) {
            FrequencyManager<TeleporterFrequency> manager = FrequencyType.TELEPORTER.getManager(given.isPublic() ? null : player.getUniqueID());
            TeleporterFrequency freq = manager.getFrequency(given.getName());
            if (freq != null && !freq.getActiveCoords().isEmpty()) {
                status = 1;
                if (!player.isCreative()) {
                    Coord4D coords = given.getClosestCoords(new Coord4D(player));
                    if (coords != null) {
                        FloatingLong energyNeeded = TileEntityTeleporter.calculateEnergyCost(player, coords);
                        IEnergyContainer energyContainer = StorageUtils.getEnergyContainer(stack, 0);
                        if (energyContainer == null || energyContainer.extract(energyNeeded, Action.SIMULATE, AutomationType.MANUAL).smallerThan(energyNeeded)) {
                            status = 4;
                        }
                    }
                }
            }
        }
        Mekanism.packetHandler.sendTo(new PacketPortableTeleporter(status), (ServerPlayerEntity) player);
    }

    public enum PortableTeleporterPacketType {
        DATA_REQUEST,
        TELEPORT
    }
}