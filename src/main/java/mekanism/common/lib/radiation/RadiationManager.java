package mekanism.common.lib.radiation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import mekanism.api.chemical.ChemicalResource;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.radiation.IRadiationManager;
import mekanism.api.radiation.IRadiationSource;
import mekanism.api.radiation.capability.IRadiationEntity;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.component.containers.type.ContainerType;
import mekanism.common.config.MekanismConfig;
import mekanism.common.registries.MekanismAttachmentTypes;
import mekanism.common.registries.MekanismDamageTypes;
import mekanism.common.util.MekanismUtils;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Range;
import org.jspecify.annotations.Nullable;

/// @apiNote Do not instantiate this class directly as it will be done via the service loader. Instead, access instances of this via [IRadiationManager#INSTANCE]
public final class RadiationManager implements IRadiationManager {

    /// RadiationManager for handling radiation across all dimensions
    public static RadiationManager get() {
        return (RadiationManager) INSTANCE;
    }

    private static final RandomSource RAND = RandomSource.create();
    private static boolean shouldDecayThisTick = false;

    static final double BASELINE = 0.000_000_100; // 100 nSv/h
    static final double MIN_MAGNITUDE = 0.000_010; // 10 uSv/h

    @Override
    public boolean isRadiationEnabled() {
        return isGlobalRadiationEnabled();
    }

    public static boolean isGlobalRadiationEnabled() {
        //Get the default value for cases when we may call this early such as via chemical attributes
        return MekanismConfig.general.radiationEnabled.getOrDefault();
    }

    @Nullable
    private static RadiationLevelData getData(Level level) {
        return level.getExistingDataOrNull(MekanismAttachmentTypes.RADIATION_LEVEL_DATA);
    }

    private static RadiationLevelData getOrCreateData(Level level) {
        return level.getData(MekanismAttachmentTypes.RADIATION_LEVEL_DATA);
    }

    @Override
    public double baselineRadiation() {
        return BASELINE;
    }

    @Override
    public double minRadiationMagnitude() {
        return MIN_MAGNITUDE;
    }

    @Override
    public DamageSource getRadiationDamageSource(RegistryAccess registryAccess) {
        return MekanismDamageTypes.RADIATION.source(registryAccess);
    }

    @Override
    public ResourceKey<DamageType> getRadiationDamageTypeKey() {
        return MekanismDamageTypes.RADIATION.key();
    }

    @Override
    public double getRadiationLevel(Entity entity) {
        return getRadiationLevel(entity.level(), entity.blockPosition());
    }

    @Override
    public List<IRadiationSource> getRadiationSources(Level level, int chunkX, int chunkZ) {
        RadiationLevelData radiationLevelData = getData(level);
        if (radiationLevelData == null) {//Short circuit when the radiation table is empty
            return Collections.emptyList();
        }
        Iterator<RadiationSource> sourceIterator = radiationLevelData.getSources(chunkX, chunkZ);
        if (!sourceIterator.hasNext()) {
            return Collections.emptyList();
        }
        List<IRadiationSource> sources = new ArrayList<>();
        while (sourceIterator.hasNext()) {
            sources.add(sourceIterator.next());
        }
        return sources;
    }

    @Override
    public void removeRadiationSources(Level level, int chunkX, int chunkZ) {
        RadiationLevelData radiationLevelData = getData(level);
        if (radiationLevelData == null) {//Short circuit when the radiation table is empty
            return;
        }
        if (radiationLevelData.removeRadiationSources(chunkX, chunkZ)) {
            PlayerExposure.updateClientRadiationForAll(level.dimension());
        }
    }

    @Override
    public void removeRadiationSource(Level level, BlockPos pos) {
        RadiationLevelData radiationLevelData = getData(level);
        if (radiationLevelData == null) {//Short circuit when the radiation table is empty
            return;
        }
        if (radiationLevelData.removeRadiationSource(pos)) {
            PlayerExposure.updateClientRadiationForAll(level.dimension());
        }
    }

    @Override
    public double getRadiationLevel(Level level, BlockPos pos) {
        RadiationLevelData radiationLevelData = getData(level);
        if (radiationLevelData == null) {//Short circuit when the radiation table is empty
            return baselineRadiation();
        }
        return radiationLevelData.getRadiationLevelAndMaxMagnitude(pos).level();
    }

    public LevelAndMaxMagnitude getRadiationLevelAndMaxMagnitude(Entity entity) {
        RadiationLevelData radiationLevelData = getData(entity.level());
        if (radiationLevelData == null) {//Short circuit when the radiation table is empty
            return LevelAndMaxMagnitude.BASELINE;
        }
        return radiationLevelData.getRadiationLevelAndMaxMagnitude(entity.blockPosition());
    }

    @Override
    public void radiate(Level level, BlockPos pos, double magnitude) {
        if (!isGlobalRadiationEnabled()) {
            return;
        }
        getOrCreateData(level).radiate(pos, magnitude);

        //Update radiation levels immediately
        PlayerExposure.updateClientRadiationForAll(level.dimension());
    }

    @Override
    public void radiate(LivingEntity entity, double magnitude) {
        if (!isGlobalRadiationEnabled()) {
            return;
        }
        if (!(entity instanceof Player player) || MekanismUtils.isPlayingMode(player)) {
            IRadiationEntity radiationEntity = entity.getCapability(Capabilities.RADIATION_ENTITY);
            if (radiationEntity != null) {
                radiationEntity.radiate(magnitude * (1 - Math.min(1, RadiationUtil.getRadiationResistance(entity))));
            }
        }
    }

    @Override
    public void dumpRadiation(Level level, BlockPos pos, ResourceHandler<ChemicalResource> chemicalHandler, @Nullable TransactionContext transaction, HandlerRadiationClearer radioactiveClearer) {
        for (int tank = 0, gasTanks = chemicalHandler.size(); tank < gasTanks; tank++) {
            if (dumpRadiation(level, pos, chemicalHandler.getResource(tank), chemicalHandler.getAmountAsLong(tank))) {
                radioactiveClearer.clear(chemicalHandler, tank, transaction);
            }
        }
    }

    @Override
    public void dumpRadiation(Level level, BlockPos pos, List<IChemicalTank> chemicalTanks, boolean clearRadioactive, @Nullable TransactionContext transaction) {
        for (IChemicalTank chemicalTank : chemicalTanks) {
            if (dumpRadiation(level, pos, chemicalTank.resource(), chemicalTank.amountAsLong()) && clearRadioactive) {
                ContainerType.CHEMICAL.clearContents(chemicalTank, transaction);
            }
        }
    }

    @Override
    public boolean dumpRadiation(Level level, BlockPos pos, ChemicalResource type, @Range(from = 0, to = Long.MAX_VALUE) long amount) {
        //Note: We only attempt to dump and mark that we did if radiation is enabled in order to allow persisting radioactive
        // substances when radiation is disabled
        if (isGlobalRadiationEnabled() && !type.isEmpty() && amount > 0) {
            double radioactivity = type.getRadioactivity(level.registryAccess());
            if (radioactivity > 0) {
                radiate(level, pos, radioactivity * amount);
                return true;
            }
        }
        return false;
    }

    public void clearSources() {
        MinecraftServer currentServer = ServerLifecycleHooks.getCurrentServer();
        if (currentServer == null) {
            return;
        }

        boolean hadEntries = false;
        for (ServerLevel level : currentServer.getAllLevels()) {
            RadiationLevelData radiationLevelData = getData(level);
            if (radiationLevelData == null || radiationLevelData.isEmpty()) {
                continue;
            }
            hadEntries = true;
            radiationLevelData.clearAll();
        }
        if (hadEntries) {
            PlayerExposure.updateClientRadiationForAll();
        }
    }

    @SubscribeEvent
    public void onTickPre(ServerTickEvent.Pre event) {
        // each tick, there's a 1/20 chance we'll decay radiation sources (averages to 1 decay operation per second)
        shouldDecayThisTick = isGlobalRadiationEnabled() &&
                              event.getServer().tickRateManager().runsNormally() &&
                              RAND.nextInt(SharedConstants.TICKS_PER_SECOND) == 0;
    }

    public void tickServerWorld(ServerLevel world) {
        if (!shouldDecayThisTick) {
            return;
        }
        RadiationLevelData radiationLevelData = getData(world);
        if (radiationLevelData == null || radiationLevelData.isEmpty()) {
            return;
        }
        radiationLevelData.decay();
        //Update radiation levels for any players where it has changed
        PlayerExposure.updateClientRadiationForAll(world.dimension());
    }
}
