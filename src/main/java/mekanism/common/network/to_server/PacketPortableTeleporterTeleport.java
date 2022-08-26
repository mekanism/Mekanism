package mekanism.common.network.to_server;

import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.Coord4D;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.math.FloatingLong;
import mekanism.common.Mekanism;
import mekanism.common.content.teleporter.TeleporterFrequency;
import mekanism.common.item.ItemPortableTeleporter;
import mekanism.common.lib.frequency.Frequency.FrequencyIdentity;
import mekanism.common.lib.frequency.FrequencyType;
import mekanism.common.network.IMekanismPacket;
import mekanism.common.network.to_client.PacketPortalFX;
import mekanism.common.tile.TileEntityTeleporter;
import mekanism.common.util.StorageUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.server.ServerLifecycleHooks;

public class PacketPortableTeleporterTeleport implements IMekanismPacket {

    private final FrequencyIdentity identity;
    private final InteractionHand currentHand;

    public PacketPortableTeleporterTeleport(InteractionHand hand, FrequencyIdentity identity) {
        currentHand = hand;
        this.identity = identity;
    }

    @Override
    public void handle(NetworkEvent.Context context) {
        ServerPlayer player = context.getSender();
        if (player == null) {
            return;
        }
        ItemStack stack = player.getItemInHand(currentHand);
        if (!stack.isEmpty() && stack.getItem() instanceof ItemPortableTeleporter) {
            //Note: We make use of the player's own UUID, given they shouldn't be allowed to teleport to a private frequency of another player
            TeleporterFrequency found = FrequencyType.TELEPORTER.getFrequency(identity, player.getUUID());
            if (found == null) {
                return;
            }
            Coord4D coords = found.getClosestCoords(new Coord4D(player));
            if (coords != null) {
                Level teleWorld = ServerLifecycleHooks.getCurrentServer().getLevel(coords.dimension);
                TileEntityTeleporter teleporter = WorldUtils.getTileEntity(TileEntityTeleporter.class, teleWorld, coords.getPos());
                if (teleporter != null) {
                    if (!player.isCreative()) {
                        FloatingLong energyCost = TileEntityTeleporter.calculateEnergyCost(player, teleWorld, coords);
                        IEnergyContainer energyContainer = StorageUtils.getEnergyContainer(stack, 0);
                        if (energyContainer == null || energyContainer.extract(energyCost, Action.SIMULATE, AutomationType.MANUAL).smallerThan(energyCost)) {
                            return;
                        }
                        energyContainer.extract(energyCost, Action.EXECUTE, AutomationType.MANUAL);
                    }
                    //TODO: Figure out what this try catch is meant to be catching as I don't see much of a reason for it to exist
                    try {
                        teleporter.didTeleport.add(player.getUUID());
                        teleporter.teleDelay = 5;
                        player.connection.aboveGroundTickCount = 0;
                        player.closeContainer();
                        Mekanism.packetHandler().sendToAllTracking(new PacketPortalFX(player.blockPosition()), player.level, coords.getPos());
                        if (player.isPassenger()) {
                            player.stopRiding();
                        }
                        double oldX = player.getX();
                        double oldY = player.getY();
                        double oldZ = player.getZ();
                        Level oldWorld = player.level;
                        BlockPos teleporterTargetPos = teleporter.getTeleporterTargetPos();
                        TileEntityTeleporter.teleportEntityTo(player, teleWorld, teleporterTargetPos);
                        TileEntityTeleporter.alignPlayer(player, teleporterTargetPos, teleporter);
                        if (player.level != oldWorld || player.distanceToSqr(oldX, oldY, oldZ) >= 25) {
                            //If the player teleported over 5 blocks, play the sound at both the destination and the source
                            oldWorld.playSound(null, oldX, oldY, oldZ, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
                        }
                        player.level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
                        teleporter.sendTeleportParticles();
                    } catch (Exception ignored) {
                    }
                }
            }
        }
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeEnum(currentHand);
        FrequencyType.TELEPORTER.getIdentitySerializer().write(buffer, identity);
    }

    public static PacketPortableTeleporterTeleport decode(FriendlyByteBuf buffer) {
        return new PacketPortableTeleporterTeleport(buffer.readEnum(InteractionHand.class), FrequencyType.TELEPORTER.getIdentitySerializer().read(buffer));
    }
}