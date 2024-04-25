package mekanism.common.network.to_server;

import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.event.MekanismTeleportEvent;
import mekanism.api.math.FloatingLong;
import mekanism.common.Mekanism;
import mekanism.common.content.teleporter.TeleporterFrequency;
import mekanism.common.item.ItemPortableTeleporter;
import mekanism.common.lib.frequency.Frequency.FrequencyIdentity;
import mekanism.common.lib.frequency.FrequencyType;
import mekanism.common.network.IMekanismPacket;
import mekanism.common.network.PacketUtils;
import mekanism.common.network.to_client.PacketPortalFX;
import mekanism.common.tile.TileEntityTeleporter;
import mekanism.common.util.StorageUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record PacketPortableTeleporterTeleport(InteractionHand currentHand, FrequencyIdentity identity) implements IMekanismPacket {

    public static final CustomPacketPayload.Type<PacketPortableTeleporterTeleport> TYPE = new CustomPacketPayload.Type<>(Mekanism.rl("portable_teleport"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PacketPortableTeleporterTeleport> STREAM_CODEC = StreamCodec.composite(
          PacketUtils.INTERACTION_HAND_STREAM_CODEC, PacketPortableTeleporterTeleport::currentHand,
          FrequencyType.TELEPORTER.getIdentitySerializer().streamCodec(), PacketPortableTeleporterTeleport::identity,
          PacketPortableTeleporterTeleport::new
    );

    @NotNull
    @Override
    public CustomPacketPayload.Type<PacketPortableTeleporterTeleport> type() {
        return TYPE;
    }

    @Override
    public void handle(IPayloadContext context) {
        ServerPlayer player = (ServerPlayer) context.player();
        ItemStack stack = player.getItemInHand(currentHand);
        if (!stack.isEmpty() && stack.getItem() instanceof ItemPortableTeleporter) {
            TeleporterFrequency found = FrequencyType.TELEPORTER.getFrequency(identity, player.getUUID());
            if (found == null) {
                return;
            }
            GlobalPos coords = found.getClosestCoords(GlobalPos.of(player.level().dimension(), player.blockPosition()));
            if (coords != null) {
                MinecraftServer server = player.level().getServer();
                Level teleWorld = server == null ? null : server.getLevel(coords.dimension());
                TileEntityTeleporter teleporter = WorldUtils.getTileEntity(TileEntityTeleporter.class, teleWorld, coords.pos());
                if (teleporter != null) {
                    FloatingLong energyCost;
                    Runnable energyExtraction = null;
                    if (!player.isCreative()) {
                        energyCost = TileEntityTeleporter.calculateEnergyCost(player, teleWorld, coords);
                        IEnergyContainer energyContainer = StorageUtils.getEnergyContainer(stack, 0);
                        if (energyContainer == null || energyContainer.extract(energyCost, Action.SIMULATE, AutomationType.MANUAL).smallerThan(energyCost)) {
                            return;
                        }
                        energyExtraction = () -> energyContainer.extract(energyCost, Action.EXECUTE, AutomationType.MANUAL);
                    } else {
                        energyCost = FloatingLong.ZERO;
                    }
                    //TODO: Figure out what this try catch is meant to be catching as I don't see much of a reason for it to exist
                    try {
                        teleporter.didTeleport.add(player.getUUID());
                        teleporter.teleDelay = 5;
                        BlockPos teleporterTargetPos = teleporter.getTeleporterTargetPos();
                        MekanismTeleportEvent.PortableTeleporter event = new MekanismTeleportEvent.PortableTeleporter(player, teleporterTargetPos, coords.dimension(), stack, energyCost);
                        if (NeoForge.EVENT_BUS.post(event).isCanceled()) {
                            //Fail if the event was cancelled
                            return;
                        }
                        if (energyExtraction != null) {
                            energyExtraction.run();
                        }
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
                        TileEntityTeleporter.teleportEntityTo(player, teleWorld, event, false);
                        TileEntityTeleporter.alignPlayer(player, event, teleporter);
                        if (player.level() != oldWorld || player.distanceToSqr(oldX, oldY, oldZ) >= 25) {
                            //If the player teleported over 5 blocks, play the sound at both the destination and the source
                            oldWorld.playSound(null, oldX, oldY, oldZ, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
                        }
                        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
                        teleporter.sendTeleportParticles();
                    } catch (Exception ignored) {
                    }
                }
            }
        }
    }
}