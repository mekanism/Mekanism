package mekanism.common.lib.radiation;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import mekanism.api.Chunk3D;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalHandler;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.radiation.IRadiationManager;
import mekanism.api.radiation.IRadiationSource;
import mekanism.api.radiation.capability.IRadiationEntity;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.config.MekanismConfig;
import mekanism.common.registries.MekanismAttachmentTypes;
import mekanism.common.registries.MekanismDamageTypes;
import mekanism.common.util.MekanismUtils;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
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
import org.jetbrains.annotations.Nullable;

/**
 * The RadiationManager handles radiation across all in-game dimensions. Radiation exposure levels are provided in _sieverts, defining a rate of accumulation of
 * equivalent dose. For reference, here are examples of equivalent dose (credit: wikipedia)
 * <ul>
 * <li>100 nSv: baseline dose (banana equivalent dose)</li>
 * <li>250 nSv: airport security screening</li>
 * <li>1 mSv: annual total civilian dose equivalent</li>
 * <li>50 mSv: annual total occupational equivalent dose limit</li>
 * <li>250 mSv: total dose equivalent from 6-month trip to mars</li>
 * <li>1 Sv: maximum allowed dose allowed for NASA astronauts over their careers</li>
 * <li>5 Sv: dose required to (50% chance) kill human if received over 30-day period</li>
 * <li>50 Sv: dose received after spending 10 min next to Chernobyl reactor core directly after meltdown</li>
 * </ul>
 * For defining rate of accumulation, we use _sieverts per hour_ (Sv/h). Here are examples of dose accumulation rates.
 * <ul>
 * <li>100 nSv/h: max recommended human irradiation</li>
 * <li>2.7 uSv/h: irradiation from airline at cruise altitude</li>
 * <li>190 mSv/h: highest reading from fallout of Trinity (Manhattan project test) bomb, _20 miles away_, 3 hours after detonation</li>
 * <li>~500 Sv/h: irradiation inside primary containment vessel of Fukushima power station (at this rate, it takes 30 seconds to accumulate a median lethal dose)</li>
 * </ul>
 *
 * @apiNote Do not instantiate this class directly as it will be done via the service loader. Instead, access instances of this via {@link IRadiationManager#INSTANCE}
 */
@NothingNullByDefault
public final class RadiationManager implements IRadiationManager {

    /**
     * RadiationManager for handling radiation across all dimensions
     */
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

    @SuppressWarnings("removal")//backcompat
    @Deprecated(forRemoval = true, since = "10.7.15")
    @Override
    public Table<Chunk3D, GlobalPos, IRadiationSource> getRadiationSources() {
        HashBasedTable<Chunk3D, GlobalPos, IRadiationSource> table = HashBasedTable.create();

        MinecraftServer currentServer = ServerLifecycleHooks.getCurrentServer();
        if (currentServer == null) {
            return table;
        }

        for (ServerLevel level : currentServer.getAllLevels()) {
            RadiationLevelData radiationLevelData = getData(level);
            if (radiationLevelData == null || radiationLevelData.isEmpty()) {
                continue;
            }
            for (RadiationSource value : radiationLevelData.values()) {
                GlobalPos globalPos = new GlobalPos(level.dimension(), value.getPosition());
                table.put(new Chunk3D(globalPos), globalPos, new RadiationSource(value.getPosition(), value.getMagnitude()) {
                    @Override
                    public GlobalPos getPos() {
                        return globalPos;
                    }
                });
            }
        }

        return table;
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
    public void dumpRadiation(Level level, BlockPos pos, IChemicalHandler chemicalHandler, boolean clearRadioactive) {
        for (int tank = 0, gasTanks = chemicalHandler.getChemicalTanks(); tank < gasTanks; tank++) {
            if (dumpRadiation(level, pos, chemicalHandler.getChemicalInTank(tank)) && clearRadioactive) {
                chemicalHandler.setChemicalInTank(tank, ChemicalStack.EMPTY);
            }
        }
    }

    @Override
    public void dumpRadiation(Level level, BlockPos pos, List<IChemicalTank> chemicalTanks, boolean clearRadioactive) {
        for (IChemicalTank gasTank : chemicalTanks) {
            if (dumpRadiation(level, pos, gasTank.getStack()) && clearRadioactive) {
                gasTank.setEmpty();
            }
        }
    }

    @Override
    public boolean dumpRadiation(Level level, BlockPos pos, ChemicalStack stack) {
        //Note: We only attempt to dump and mark that we did if radiation is enabled in order to allow persisting radioactive
        // substances when radiation is disabled
        if (isGlobalRadiationEnabled() && !stack.isEmpty()) {
            double radioactivity = stack.getRadioactivity();
            if (radioactivity > 0) {
                radiate(level, pos, radioactivity);
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
