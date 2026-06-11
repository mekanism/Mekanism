package mekanism.common.network.to_server;

import io.netty.buffer.ByteBuf;
import mekanism.api.event.MekanismTeleportEvent;
import mekanism.common.Mekanism;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.proxy.AutomatedEnergyHandler;
import mekanism.common.content.teleporter.TeleporterFrequency;
import mekanism.common.item.ItemPortableTeleporter;
import mekanism.common.lib.frequency.Frequency.FrequencyIdentity;
import mekanism.common.lib.frequency.FrequencyTypes;
import mekanism.common.network.IMekanismPacket;
import mekanism.common.network.PacketUtils;
import mekanism.common.network.to_client.PacketPortalFX;
import mekanism.common.tile.TileEntityTeleporter;
import mekanism.common.util.ItemAccessUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.TeleportTransition;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;

public record PacketPortableTeleporterTeleport(InteractionHand currentHand, FrequencyIdentity identity) implements IMekanismPacket {

    public static final CustomPacketPayload.Type<PacketPortableTeleporterTeleport> TYPE = new CustomPacketPayload.Type<>(Mekanism.rl("portable_teleport"));
    public static final StreamCodec<ByteBuf, PacketPortableTeleporterTeleport> STREAM_CODEC = StreamCodec.composite(
          InteractionHand.STREAM_CODEC, PacketPortableTeleporterTeleport::currentHand,
          FrequencyTypes.TELEPORTER.getIdentitySerializer().streamCodec(), PacketPortableTeleporterTeleport::identity,
          PacketPortableTeleporterTeleport::new
    );

    @Override
    public CustomPacketPayload.Type<PacketPortableTeleporterTeleport> type() {
        return TYPE;
    }

    @Override
    public void handle(IPayloadContext context) {
        ServerPlayer player = (ServerPlayer) context.player();
        ItemStack stack = player.getItemInHand(currentHand);
        if (!stack.isEmpty() && stack.getItem() instanceof ItemPortableTeleporter) {
            TeleporterFrequency found = FrequencyTypes.TELEPORTER.getFrequency(identity, player.getUUID());
            if (found == null) {
                return;
            }
            GlobalPos coords = found.getClosestCoords(player.level().dimension(), player.blockPosition());
            if (coords != null) {
                ServerLevel teleWorld = player.level().getServer().getLevel(coords.dimension());
                TileEntityTeleporter teleporter = WorldUtils.getTileEntity(TileEntityTeleporter.class, teleWorld, coords.pos());
                if (teleporter != null) {
                    int energyCost;
                    try (Transaction transaction = Transaction.openRoot()) {
                        if (!player.isCreative()) {
                            energyCost = TileEntityTeleporter.calculateEnergyCost(player, teleWorld, coords);
                            EnergyHandler energyHandler = AutomatedEnergyHandler.manual(Capabilities.ENERGY.getCapability(ItemAccessUtils.playerHandAccess(player, currentHand)));
                            if (energyHandler == null || energyHandler.extract(energyCost, transaction) < energyCost) {
                                //Fail if there is not enough energy available
                                return;
                            }
                        } else {
                            energyCost = 0;
                        }
                        //TODO: Figure out what this try catch is meant to be catching as I don't see much of a reason for it to exist
                        try {
                            teleporter.didTeleport.add(player.getUUID());
                            teleporter.teleDelay = 5;
                            BlockPos teleporterTargetPos = teleporter.getTeleporterTargetPos();
                            MekanismTeleportEvent.PortableTeleporter event = new MekanismTeleportEvent.PortableTeleporter(player, teleporterTargetPos, teleWorld, stack, energyCost);
                            if (NeoForge.EVENT_BUS.post(event).isCanceled()) {
                                //Fail if the event was cancelled
                                return;
                            }
                            //Commit the energy usage
                            transaction.commit();
                            player.connection.aboveGroundTickCount = 0;
                            player.closeContainer();
                            PacketUtils.sendToAllTracking(new PacketPortalFX(player.blockPosition()), player.level(), coords.pos());
                            if (player.isPassenger()) {
                                player.stopRiding();
                            }
                            double oldX = player.getX();
                            double oldY = player.getY();
                            double oldZ = player.getZ();
                            Level oldWorld = player.level();
                            TileEntityTeleporter.teleportEntityTo(player, teleWorld, teleporter, event, false, TeleportTransition.DO_NOTHING);
                            if (player.level() != oldWorld || player.distanceToSqr(oldX, oldY, oldZ) >= 25) {
                                //If the player teleported over 5 blocks, play the sound at both the destination and the source
                                oldWorld.playSound(null, oldX, oldY, oldZ, SoundEvents.PLAYER_TELEPORT, SoundSource.PLAYERS);
                            }
                            player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_TELEPORT, SoundSource.PLAYERS);
                            teleporter.sendTeleportParticles();
                        } catch (Exception _) {//TODO - 26.1: What exception are we catching??
                        }
                    }
                }
            }
        }
    }
}