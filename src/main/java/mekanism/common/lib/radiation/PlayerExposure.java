package mekanism.common.lib.radiation;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
import java.util.UUID;
import mekanism.api.math.MathUtils;
import mekanism.api.radiation.capability.IRadiationEntity;
import mekanism.common.Mekanism;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.network.to_client.radiation.PacketEnvironmentalRadiationData;
import mekanism.common.network.to_client.radiation.PacketPlayerRadiationData;
import mekanism.common.util.MekanismUtils;
import net.minecraft.SharedConstants;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.Nullable;

@EventBusSubscriber(modid = Mekanism.MODID)
public class PlayerExposure {

    private static final Map<UUID, PreviousRadiationData> playerEnvironmentalExposureMap = new Object2ObjectOpenHashMap<>();
    private static final Map<UUID, PreviousRadiationData> playerExposureMap = new Object2ObjectOpenHashMap<>();

    public static void tickServer(ServerPlayer player) {
        updateEntityRadiation(player);
    }

    @SubscribeEvent
    public static void onLivingTick(EntityTickEvent.Post event) {
        Level world = event.getEntity().level();
        if (!world.isClientSide() && event.getEntity() instanceof LivingEntity living && !(living instanceof Player) && !world.tickRateManager().isEntityFrozen(living)) {
            //If it is a living entity that isn't a player, and the tick rate manager is functioning
            // and the entity is frozen (doesn't have a player as a passenger), then we need to update
            // the radiation level of the entity
            updateEntityRadiation(living);
        }
    }

    public static void clear() {
        playerEnvironmentalExposureMap.clear();
        playerExposureMap.clear();
    }

    private static void updateEntityRadiation(LivingEntity entity) {
        // terminate early if we're disabled
        if (!RadiationManager.isGlobalRadiationEnabled()) {
            return;
        }
        IRadiationEntity radiationCap = entity.getCapability(Capabilities.RADIATION_ENTITY);
        // each tick, there is a 1/20 chance we will apply radiation to each player
        // this helps distribute the CPU load across ticks, and makes exposure slightly inconsistent
        if (entity.level().getRandom().nextInt(SharedConstants.TICKS_PER_SECOND) == 0) {
            double magnitude = RadiationManager.get().getRadiationLevel(entity);
            if (magnitude > RadiationManager.get().baselineRadiation() && (!(entity instanceof Player player) || MekanismUtils.isPlayingMode(player))) {
                // apply radiation to the player
                RadiationManager.get().radiate(entity, magnitude / 3_600D); // convert to Sv/s
            }
            if (radiationCap != null) {
                radiationCap.decay();
            }
        }
        // update the radiation capability (decay, sync, effects)
        if (radiationCap != null) {
            radiationCap.update();
            if (entity instanceof ServerPlayer player) {
                double radiation = radiationCap.getRadiation();
                PreviousRadiationData previousRadiationData = playerExposureMap.get(player.getUUID());
                PreviousRadiationData relevantData = PreviousRadiationData.compareTo(previousRadiationData, radiation);
                if (relevantData != null) {
                    playerExposureMap.put(player.getUUID(), relevantData);
                    PacketDistributor.sendToPlayer(player, new PacketPlayerRadiationData(radiation));
                }
            }
        }
    }

    public static void resetPlayer(UUID uuid) {
        playerEnvironmentalExposureMap.remove(uuid);
        playerExposureMap.remove(uuid);
    }

    public static void updateClientRadiation(ServerPlayer player) {
        LevelAndMaxMagnitude levelAndMaxMagnitude = RadiationManager.get().getRadiationLevelAndMaxMagnitude(player);
        PreviousRadiationData previousRadiationData = playerEnvironmentalExposureMap.get(player.getUUID());
        PreviousRadiationData relevantData = PreviousRadiationData.compareTo(previousRadiationData, levelAndMaxMagnitude.level());
        if (relevantData != null) {
            playerEnvironmentalExposureMap.put(player.getUUID(), relevantData);
            PacketDistributor.sendToPlayer(player, new PacketEnvironmentalRadiationData(levelAndMaxMagnitude));
        }
    }

    static void updateClientRadiationForAll(ResourceKey<Level> dimension) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            //Validate it is not null in case we somehow are being called from the client or at some other unexpected time
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                if (player.level().dimension() == dimension) {
                    updateClientRadiation(player);
                }
            }
        }
    }

    static void updateClientRadiationForAll() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            //Validate it is not null in case we somehow are being called from the client or at some other unexpected time
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                updateClientRadiation(player);
            }
        }
    }

    private record PreviousRadiationData(double magnitude, int power, double base) {

        private static int getPower(double magnitude) {
            return MathUtils.clampToInt(Math.floor(Math.log10(magnitude)));
        }

        @Nullable
        private static PreviousRadiationData compareTo(@Nullable PreviousRadiationData previousRadiationData, double magnitude) {
            if (previousRadiationData == null || Math.abs(magnitude - previousRadiationData.magnitude) >= previousRadiationData.base) {
                //No cached value or the magnitude changed by more than the smallest unit we display
                return getData(magnitude, getPower(magnitude));
            } else if (magnitude < previousRadiationData.magnitude) {
                //Magnitude has decreased, and by a smaller amount than the smallest unit we currently are displaying
                int power = getPower(magnitude);
                if (power < previousRadiationData.power) {
                    //Check if the number of digits decreased, in which case even if we potentially only decreased by a tiny amount
                    // we still need to sync and update it
                    return getData(magnitude, power);
                }
            }
            //No need to sync
            return null;
        }

        private static PreviousRadiationData getData(double magnitude, int power) {
            //Unit display happens using SI units which is in factors of 1,000 (10^3) convert our power to the current SI unit it is for
            int siPower = Math.floorDiv(power, 3) * 3;
            //Note: We subtract two from the power because for places we sync to and read from on the client side
            // we have two decimal places, so we need to shift our target to include those decimals
            double base = Math.pow(10, siPower - 2);
            return new PreviousRadiationData(magnitude, power, base);
        }
    }
}
