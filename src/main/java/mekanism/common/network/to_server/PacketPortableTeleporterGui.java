package mekanism.common.network.to_server;

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
import mekanism.common.network.BasePacketHandler;
import mekanism.common.network.IMekanismPacket;
import mekanism.common.network.to_client.PacketPortableTeleporter;
import mekanism.common.network.to_client.PacketPortalFX;
import mekanism.common.tile.TileEntityTeleporter;
import mekanism.common.util.StorageUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

public class PacketPortableTeleporterGui implements IMekanismPacket {

    private final PortableTeleporterPacketType packetType;
    private final TeleporterFrequency frequency;
    private final Hand currentHand;

    public PacketPortableTeleporterGui(PortableTeleporterPacketType type, Hand hand, TeleporterFrequency freq) {
        packetType = type;
        currentHand = hand;
        frequency = freq;
    }

    @Override
    public void handle(NetworkEvent.Context context) {
        ServerPlayerEntity player = context.getSender();
        if (player == null) {
            return;
        }
        ItemStack stack = player.getItemInHand(currentHand);
        if (!stack.isEmpty() && stack.getItem() instanceof ItemPortableTeleporter) {
            if (packetType == PortableTeleporterPacketType.DATA_REQUEST) {
                sendDataResponse(frequency, player, stack);
            } else if (packetType == PortableTeleporterPacketType.TELEPORT) {
                FrequencyManager<TeleporterFrequency> manager2 = FrequencyType.TELEPORTER.getManager(frequency.isPublic() ? null : player.getUUID());
                TeleporterFrequency found = manager2.getFrequency(frequency.getName());
                if (found == null) {
                    return;
                }
                Coord4D coords = found.getClosestCoords(new Coord4D(player));
                if (coords != null) {
                    World teleWorld = ServerLifecycleHooks.getCurrentServer().getLevel(coords.dimension);
                    TileEntityTeleporter teleporter = WorldUtils.getTileEntity(TileEntityTeleporter.class, teleWorld, coords.getPos());
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
                            teleporter.didTeleport.add(player.getUUID());
                            teleporter.teleDelay = 5;
                            player.connection.aboveGroundTickCount = 0;
                            player.closeContainer();
                            Mekanism.packetHandler.sendToAllTracking(new PacketPortalFX(player.blockPosition()), player.level, coords.getPos());
                            if (player.isPassenger()) {
                                player.stopRiding();
                            }
                            double oldX = player.getX();
                            double oldY = player.getY();
                            double oldZ = player.getZ();
                            World oldWorld = player.level;
                            TileEntityTeleporter.teleportEntityTo(player, coords, teleporter);
                            BlockPos coordsPos = coords.getPos();
                            Direction frameDirection = teleporter.frameDirection();
                            if (frameDirection != null) {
                                coordsPos = coordsPos.below().relative(frameDirection);
                            }
                            TileEntityTeleporter.alignPlayer(player, coordsPos);
                            if (player.level != oldWorld || player.distanceToSqr(oldX, oldY, oldZ) >= 25) {
                                //If the player teleported over 5 blocks, play the sound at both the destination and the source
                                oldWorld.playSound(null, oldX, oldY, oldZ, SoundEvents.ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1.0F, 1.0F);
                            }
                            player.level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1.0F, 1.0F);
                            Mekanism.packetHandler.sendToAllTracking(new PacketPortalFX(coordsPos), teleWorld, coordsPos);
                        } catch (Exception ignored) {
                        }
                    }
                }
            }
        }
    }

    @Override
    public void encode(PacketBuffer buffer) {
        buffer.writeEnum(packetType);
        buffer.writeEnum(currentHand);
        if (frequency == null) {
            buffer.writeBoolean(false);
        } else {
            buffer.writeBoolean(true);
            buffer.writeUtf(frequency.getName());
            buffer.writeBoolean(frequency.isPublic());
        }

    }

    public static PacketPortableTeleporterGui decode(PacketBuffer buffer) {
        PortableTeleporterPacketType packetType = buffer.readEnum(PortableTeleporterPacketType.class);
        Hand currentHand = buffer.readEnum(Hand.class);
        TeleporterFrequency frequency = null;
        if (buffer.readBoolean()) {
            frequency = new TeleporterFrequency(BasePacketHandler.readString(buffer), null);
            frequency.setPublic(buffer.readBoolean());
        }
        return new PacketPortableTeleporterGui(packetType, currentHand, frequency);
    }

    private static void sendDataResponse(TeleporterFrequency given, PlayerEntity player, ItemStack stack) {
        byte status = 3;
        if (given != null) {
            FrequencyManager<TeleporterFrequency> manager = FrequencyType.TELEPORTER.getManager(given.isPublic() ? null : player.getUUID());
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