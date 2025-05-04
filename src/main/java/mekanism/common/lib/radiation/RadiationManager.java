package mekanism.common.lib.radiation;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.google.common.collect.Tables;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import mekanism.api.Chunk3D;
import mekanism.api.SerializationConstants;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalHandler;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.radiation.IRadiationManager;
import mekanism.api.radiation.IRadiationSource;
import mekanism.api.radiation.capability.IRadiationEntity;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.config.MekanismConfig;
import mekanism.common.lib.MekanismSavedData;
import mekanism.common.lib.collection.HashList;
import mekanism.common.registries.MekanismDamageTypes;
import mekanism.common.util.MekanismUtils;
import net.minecraft.SharedConstants;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
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
public class RadiationManager implements IRadiationManager {

    /**
     * RadiationManager for handling radiation across all dimensions
     */
    public static RadiationManager get() {
        return (RadiationManager) INSTANCE;
    }

    private static final String DATA_HANDLER_NAME = "radiation_manager";
    private static final RandomSource RAND = RandomSource.create();

    private static final double BASELINE = 0.000_000_100; // 100 nSv/h
    private static final double MIN_MAGNITUDE = 0.000_010; // 10 uSv/h

    private boolean loaded;

    private final Table<Chunk3D, GlobalPos, RadiationSource> radiationTable = HashBasedTable.create();
    private final Table<Chunk3D, GlobalPos, IRadiationSource> radiationView = Tables.unmodifiableTable(radiationTable);

    /**
     * Note: This can and will be null on the client side
     */
    @Nullable
    private RadiationDataHandler dataHandler;

    @Override
    public boolean isRadiationEnabled() {
        return isGlobalRadiationEnabled();
    }

    public static boolean isGlobalRadiationEnabled() {
        //Get the default value for cases when we may call this early such as via chemical attributes
        return MekanismConfig.general.radiationEnabled.getOrDefault();
    }

    private void markDirty() {
        if (dataHandler != null) {
            dataHandler.setDirty();
        }
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
        if (radiationTable.isEmpty()) {//Short circuit when the radiation table is empty
            return baselineRadiation();
        }
        return getRadiationLevel(GlobalPos.of(entity.level().dimension(), entity.blockPosition()));
    }

    @Override
    public Table<Chunk3D, GlobalPos, IRadiationSource> getRadiationSources() {
        return radiationView;
    }

    @Override
    public void removeRadiationSources(Chunk3D chunk) {
        Map<GlobalPos, RadiationSource> chunkSources = radiationTable.row(chunk);
        if (!chunkSources.isEmpty()) {
            chunkSources.clear();
            markDirty();
            PlayerExposure.updateClientRadiationForAll(chunk.dimension);
        }
    }

    @Override
    public void removeRadiationSource(GlobalPos pos) {
        Chunk3D chunk = new Chunk3D(pos);
        if (radiationTable.contains(chunk, pos)) {
            radiationTable.remove(chunk, pos);
            markDirty();
            PlayerExposure.updateClientRadiationForAll(pos.dimension());
        }
    }

    @Override
    public double getRadiationLevel(GlobalPos pos) {
        if (radiationTable.isEmpty()) {//Short circuit when the radiation table is empty
            return baselineRadiation();
        }
        return getRadiationLevelAndMaxMagnitude(pos).level();
    }

    public LevelAndMaxMagnitude getRadiationLevelAndMaxMagnitude(Entity entity) {
        if (radiationTable.isEmpty()) {//Short circuit when the radiation table is empty
            return LevelAndMaxMagnitude.BASELINE;
        }
        return getRadiationLevelAndMaxMagnitude(GlobalPos.of(entity.level().dimension(), entity.blockPosition()));
    }

    public LevelAndMaxMagnitude getRadiationLevelAndMaxMagnitude(GlobalPos pos) {
        if (radiationTable.isEmpty()) {//Short circuit when the radiation table is empty
            return LevelAndMaxMagnitude.BASELINE;
        }
        double level = baselineRadiation();
        double maxMagnitude = baselineRadiation();
        Chunk3D center = new Chunk3D(pos);
        int radius = MekanismConfig.general.radiationChunkCheckRadius.get();
        // we only compute exposure when within the MAX_RANGE bounds
        double maxRange = Mth.square(radius * 16);
        int minX = center.x - radius;
        int maxX = center.x + radius;
        int minZ = center.z - radius;
        int maxZ = center.z + radius;
        //Note: We inline the logic from Chunk3D#expand to avoid allocating a new hash set each time
        for (int i = minX; i <= maxX; i++) {
            for (int j = minZ; j <= maxZ; j++) {
                Chunk3D chunk = new Chunk3D(center.dimension, i, j);
                for (Map.Entry<GlobalPos, RadiationSource> entry : radiationTable.row(chunk).entrySet()) {
                    if (entry.getKey().pos().distSqr(pos.pos()) <= maxRange) {
                        RadiationSource source = entry.getValue();
                        level += RadiationUtil.computeExposure(pos, source);
                        maxMagnitude = Math.max(maxMagnitude, source.getMagnitude());
                    }
                }
            }
        }
        return new LevelAndMaxMagnitude(level, maxMagnitude);
    }

    @Override
    public void radiate(GlobalPos pos, double magnitude) {
        if (!isGlobalRadiationEnabled()) {
            return;
        }
        Map<GlobalPos, RadiationSource> radiationSourceMap = radiationTable.row(new Chunk3D(pos));
        RadiationSource src = radiationSourceMap.get(pos);
        if (src == null) {
            radiationSourceMap.put(pos, new RadiationSource(pos, magnitude));
        } else {
            src.radiate(magnitude);
        }
        markDirty();
        //Update radiation levels immediately
        PlayerExposure.updateClientRadiationForAll(pos.dimension());
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
    public void dumpRadiation(GlobalPos pos, IChemicalHandler chemicalHandler, boolean clearRadioactive) {
        for (int tank = 0, gasTanks = chemicalHandler.getChemicalTanks(); tank < gasTanks; tank++) {
            if (dumpRadiation(pos, chemicalHandler.getChemicalInTank(tank)) && clearRadioactive) {
                chemicalHandler.setChemicalInTank(tank, ChemicalStack.EMPTY);
            }
        }
    }

    @Override
    public void dumpRadiation(GlobalPos pos, List<IChemicalTank> chemicalTanks, boolean clearRadioactive) {
        for (IChemicalTank gasTank : chemicalTanks) {
            if (dumpRadiation(pos, gasTank.getStack()) && clearRadioactive) {
                gasTank.setEmpty();
            }
        }
    }

    @Override
    public boolean dumpRadiation(GlobalPos pos, ChemicalStack stack) {
        //Note: We only attempt to dump and mark that we did if radiation is enabled in order to allow persisting radioactive
        // substances when radiation is disabled
        if (isGlobalRadiationEnabled() && !stack.isEmpty()) {
            double radioactivity = stack.getRadioactivity();
            if (radioactivity > 0) {
                radiate(pos, radioactivity);
                return true;
            }
        }
        return false;
    }

    public void clearSources() {
        if (!radiationTable.isEmpty()) {
            radiationTable.clear();
            markDirty();
            PlayerExposure.updateClientRadiationForAll();
        }
    }

    public void tickServerWorld(ServerLevel world) {
        // terminate early if we're disabled or the world isn't ticking
        if (!isGlobalRadiationEnabled() || !world.tickRateManager().runsNormally()) {
            return;
        }
        if (!loaded) {
            createOrLoad();
        }
    }

    public void tickServer(boolean tickingNormally) {
        // terminate early if we're disabled or there is no radiation spots
        if (!isGlobalRadiationEnabled() || radiationTable.isEmpty()) {
            return;
        }
        // each tick, there's a 1/20 chance we'll decay radiation sources (averages to 1 decay operation per second)
        if (RAND.nextInt(SharedConstants.TICKS_PER_SECOND) == 0) {
            Collection<RadiationSource> sources = radiationTable.values();
            if (!sources.isEmpty()) {
                //Note: We have to wait until here to check if we are ticking normally, so that we still sync the radiation
                // near the player if they are walking around while ticks are frozen
                if (tickingNormally) {
                    // remove if source gets too low
                    sources.removeIf(RadiationSource::decay);
                    //Mark dirty regardless if we have any sources as magnitude changes or radiation sources change
                    markDirty();
                }
                //Update radiation levels for any players where it has changed
                PlayerExposure.updateClientRadiationForAll();
            }
        }
    }

    /**
     * Note: This should only be called from the server side
     */
    public void createOrLoad() {
        if (dataHandler == null) {
            //Always associate the world with the over world as the radiation manager keeps track of which dimension has which radiation
            dataHandler = MekanismSavedData.createSavedData(RadiationDataHandler::new, DATA_HANDLER_NAME);
            dataHandler.setManagerAndSync(this);
            dataHandler.clearCached();
        }

        loaded = true;
    }

    public void reset() {
        //Clear the table directly instead of via the method, so it doesn't mark it as dirty
        radiationTable.clear();
        dataHandler = null;
        loaded = false;
    }

    public record LevelAndMaxMagnitude(double level, double maxMagnitude) {

        private static final LevelAndMaxMagnitude BASELINE = new LevelAndMaxMagnitude(RadiationManager.BASELINE, RadiationManager.BASELINE);
    }

    public static class RadiationDataHandler extends MekanismSavedData {

        public Set<RadiationSource> loadedSources = Collections.emptySet();
        @Nullable
        public RadiationManager manager;

        public void setManagerAndSync(RadiationManager m) {
            manager = m;
            // don't sync the manager if radiation has been disabled
            if (RadiationManager.isGlobalRadiationEnabled()) {
                for (RadiationSource source : loadedSources) {
                    manager.radiationTable.put(new Chunk3D(source.getPos()), source.getPos(), source);
                }
            }
        }

        public void clearCached() {
            //Clear cached sources and meltdowns after loading them to not keep pointers in our data handler
            // that are referencing objects that eventually will be removed
            loadedSources = Collections.emptySet();
        }

        @Override
        public void load(@NotNull CompoundTag nbtTags, @NotNull HolderLookup.Provider provider) {
            if (nbtTags.contains(SerializationConstants.RADIATION_LIST, Tag.TAG_LIST)) {
                ListTag list = nbtTags.getList(SerializationConstants.RADIATION_LIST, Tag.TAG_COMPOUND);
                loadedSources = new HashList<>();
                RegistryOps<Tag> registryOps = provider.createSerializationContext(NbtOps.INSTANCE);
                for (Tag nbt : list) {
                    RadiationSource.load(registryOps, (CompoundTag) nbt).ifPresent(loadedSources::add);
                }
            } else {
                loadedSources = Collections.emptySet();
            }
        }

        @NotNull
        @Override
        public CompoundTag save(@NotNull CompoundTag nbtTags, @NotNull HolderLookup.Provider provider) {
            if (manager != null && !manager.radiationTable.isEmpty()) {
                RegistryOps<Tag> registryOps = provider.createSerializationContext(NbtOps.INSTANCE);
                ListTag list = new ListTag();
                for (RadiationSource source : manager.radiationTable.values()) {
                    list.add(source.write(registryOps));
                }
                nbtTags.put(SerializationConstants.RADIATION_LIST, list);
            }
            return nbtTags;
        }
    }
}
