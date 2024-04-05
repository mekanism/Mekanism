package mekanism.common.lib.radiation;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.google.common.collect.Tables;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import mekanism.api.Chunk3D;
import mekanism.api.NBTConstants;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasHandler;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.api.chemical.gas.attribute.GasAttributes.Radiation;
import mekanism.api.math.MathUtils;
import mekanism.api.radiation.IRadiationManager;
import mekanism.api.radiation.IRadiationSource;
import mekanism.api.radiation.capability.IRadiationEntity;
import mekanism.api.radiation.capability.IRadiationShielding;
import mekanism.api.text.EnumColor;
import mekanism.api.text.ITooltipHelper;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.config.MekanismConfig;
import mekanism.common.lib.MekanismSavedData;
import mekanism.common.lib.collection.HashList;
import mekanism.common.network.PacketUtils;
import mekanism.common.network.to_client.radiation.PacketEnvironmentalRadiationData;
import mekanism.common.network.to_client.radiation.PacketPlayerRadiationData;
import mekanism.common.registries.MekanismDamageTypes;
import mekanism.common.registries.MekanismParticleTypes;
import mekanism.common.registries.MekanismSounds;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent.LivingTickEvent;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
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
 * @apiNote Do not instantiate this class directly as it will be done via the service loader. Instead, access instances of this via {@link ITooltipHelper#INSTANCE}
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

    public static final double BASELINE = 0.000_000_100; // 100 nSv/h
    public static final double MIN_MAGNITUDE = 0.000_010; // 10 uSv/h

    private boolean loaded;

    private final Table<Chunk3D, GlobalPos, RadiationSource> radiationTable = HashBasedTable.create();
    private final Table<Chunk3D, GlobalPos, IRadiationSource> radiationView = Tables.unmodifiableTable(radiationTable);
    private final Map<ResourceLocation, List<Meltdown>> meltdowns = new Object2ObjectOpenHashMap<>();

    private final Map<UUID, PreviousRadiationData> playerEnvironmentalExposureMap = new Object2ObjectOpenHashMap<>();
    private final Map<UUID, PreviousRadiationData> playerExposureMap = new Object2ObjectOpenHashMap<>();

    // client fields
    private RadiationScale clientRadiationScale = RadiationScale.NONE;
    private double clientEnvironmentalRadiation = BASELINE;
    private double clientMaxMagnitude = BASELINE;

    /**
     * Note: This can and will be null on the client side
     */
    @Nullable
    private RadiationDataHandler dataHandler;

    @Override
    public boolean isRadiationEnabled() {
        //Get the default value for cases when we may call this early such as via chemical attributes
        return MekanismConfig.general.radiationEnabled.getOrDefault();
    }

    private void markDirty() {
        if (dataHandler != null) {
            dataHandler.setDirty();
        }
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
            return BASELINE;
        }
        return getRadiationLevel(GlobalPos.of(entity.level().dimension(), entity.blockPosition()));
    }

    /**
     * Calculates approximately how long in seconds radiation will take to decay
     *
     * @param magnitude Magnitude
     * @param source    {@code true} for if it is a {@link IRadiationSource} or an {@link IRadiationEntity} decaying
     */
    public int getDecayTime(double magnitude, boolean source) {
        double decayRate = source ? MekanismConfig.general.radiationSourceDecayRate.get() : MekanismConfig.general.radiationTargetDecayRate.get();
        int seconds = 0;
        double localMagnitude = magnitude;
        while (localMagnitude > RadiationManager.MIN_MAGNITUDE) {
            localMagnitude *= decayRate;
            seconds++;
        }
        return seconds;
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
            updateClientRadiationForAll(chunk.dimension);
        }
    }

    @Override
    public void removeRadiationSource(GlobalPos pos) {
        Chunk3D chunk = new Chunk3D(pos);
        if (radiationTable.contains(chunk, pos)) {
            radiationTable.remove(chunk, pos);
            markDirty();
            updateClientRadiationForAll(pos.dimension());
        }
    }

    @Override
    public double getRadiationLevel(GlobalPos pos) {
        if (radiationTable.isEmpty()) {//Short circuit when the radiation table is empty
            return BASELINE;
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
        double level = BASELINE;
        double maxMagnitude = BASELINE;
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
                        level += computeExposure(pos, source);
                        maxMagnitude = Math.max(maxMagnitude, source.getMagnitude());
                    }
                }
            }
        }
        return new LevelAndMaxMagnitude(level, maxMagnitude);
    }

    @Override
    public void radiate(GlobalPos pos, double magnitude) {
        if (!isRadiationEnabled()) {
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
        updateClientRadiationForAll(pos.dimension());
    }

    @Override
    public void radiate(LivingEntity entity, double magnitude) {
        if (!isRadiationEnabled()) {
            return;
        }
        if (!(entity instanceof Player player) || MekanismUtils.isPlayingMode(player)) {
            IRadiationEntity radiationEntity = entity.getCapability(Capabilities.RADIATION_ENTITY);
            if (radiationEntity != null) {
                radiationEntity.radiate(magnitude * (1 - Math.min(1, getRadiationResistance(entity))));
            }
        }
    }

    @Override
    public void dumpRadiation(GlobalPos pos, IGasHandler gasHandler, boolean clearRadioactive) {
        for (int tank = 0, gasTanks = gasHandler.getTanks(); tank < gasTanks; tank++) {
            if (dumpRadiation(pos, gasHandler.getChemicalInTank(tank)) && clearRadioactive) {
                gasHandler.setChemicalInTank(tank, GasStack.EMPTY);
            }
        }
    }

    @Override
    public void dumpRadiation(GlobalPos pos, List<IGasTank> gasTanks, boolean clearRadioactive) {
        for (IGasTank gasTank : gasTanks) {
            if (dumpRadiation(pos, gasTank.getStack()) && clearRadioactive) {
                gasTank.setEmpty();
            }
        }
    }

    @Override
    public boolean dumpRadiation(GlobalPos pos, GasStack stack) {
        //Note: We only attempt to dump and mark that we did if radiation is enabled in order to allow persisting radioactive
        // substances when radiation is disabled
        if (isRadiationEnabled() && !stack.isEmpty()) {
            double radioactivity = stack.mapAttributeToDouble(Radiation.class, (stored, attribute) -> stored.getAmount() * attribute.getRadioactivity());
            if (radioactivity > 0) {
                radiate(pos, radioactivity);
                return true;
            }
        }
        return false;
    }

    public void createMeltdown(Level world, BlockPos minPos, BlockPos maxPos, double magnitude, double chance, float radius, UUID multiblockID) {
        meltdowns.computeIfAbsent(world.dimension().location(), id -> new ArrayList<>()).add(new Meltdown(minPos, maxPos, magnitude, chance, radius, multiblockID));
        markDirty();
    }

    public void clearSources() {
        if (!radiationTable.isEmpty()) {
            radiationTable.clear();
            markDirty();
            updateClientRadiationForAll();
        }
    }

    private double computeExposure(GlobalPos pos, RadiationSource source) {
        return source.getMagnitude() / Math.max(1, pos.pos().distSqr(source.getPos().pos()));
    }

    private double getRadiationResistance(LivingEntity entity) {
        double resistance = 0;
        for (EquipmentSlot type : EnumUtils.ARMOR_SLOTS) {
            ItemStack stack = entity.getItemBySlot(type);
            if (!stack.isEmpty()) {
                IRadiationShielding shielding = stack.getCapability(Capabilities.RADIATION_SHIELDING);
                if (shielding != null) {
                    resistance += shielding.getRadiationShielding();
                }
            }
        }
        return resistance;
    }

    private void updateClientRadiationForAll(ResourceKey<Level> dimension) {
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

    private void updateClientRadiationForAll() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            //Validate it is not null in case we somehow are being called from the client or at some other unexpected time
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                updateClientRadiation(player);
            }
        }
    }

    public void updateClientRadiation(ServerPlayer player) {
        LevelAndMaxMagnitude levelAndMaxMagnitude = getRadiationLevelAndMaxMagnitude(player);
        PreviousRadiationData previousRadiationData = playerEnvironmentalExposureMap.get(player.getUUID());
        PreviousRadiationData relevantData = PreviousRadiationData.compareTo(previousRadiationData, levelAndMaxMagnitude.level());
        if (relevantData != null) {
            playerEnvironmentalExposureMap.put(player.getUUID(), relevantData);
            PacketUtils.sendTo(new PacketEnvironmentalRadiationData(levelAndMaxMagnitude), player);
        }
    }

    public void setClientEnvironmentalRadiation(double radiation, double maxMagnitude) {
        clientEnvironmentalRadiation = radiation;
        clientMaxMagnitude = maxMagnitude;
        clientRadiationScale = RadiationScale.get(clientEnvironmentalRadiation);
    }

    public double getClientEnvironmentalRadiation() {
        return isRadiationEnabled() ? clientEnvironmentalRadiation : BASELINE;
    }

    public double getClientMaxMagnitude() {
        return isRadiationEnabled() ? clientMaxMagnitude : BASELINE;
    }

    public RadiationScale getClientScale() {
        return isRadiationEnabled() ? clientRadiationScale : RadiationScale.NONE;
    }

    public void tickClient(Player player) {
        // terminate early if we're disabled
        if (!isRadiationEnabled()) {
            return;
        }
        // perhaps also play Geiger counter sound effect, even when not using item (similar to fallout)
        RandomSource randomSource = player.level().getRandom();
        if (clientRadiationScale != RadiationScale.NONE && MekanismConfig.client.radiationParticleCount.get() != 0 && randomSource.nextInt(2) == 0) {
            int count = randomSource.nextInt(clientRadiationScale.ordinal() * MekanismConfig.client.radiationParticleCount.get());
            int radius = MekanismConfig.client.radiationParticleRadius.get();
            for (int i = 0; i < count; i++) {
                double x = player.getX() + randomSource.nextDouble() * radius * 2 - radius;
                double y = player.getY() + randomSource.nextDouble() * radius * 2 - radius;
                double z = player.getZ() + randomSource.nextDouble() * radius * 2 - radius;
                player.level().addParticle(MekanismParticleTypes.RADIATION.get(), x, y, z, 0, 0, 0);
            }
        }
    }

    public void tickServer(ServerPlayer player) {
        updateEntityRadiation(player);
    }

    private void updateEntityRadiation(LivingEntity entity) {
        // terminate early if we're disabled
        if (!isRadiationEnabled()) {
            return;
        }
        IRadiationEntity radiationCap = entity.getCapability(Capabilities.RADIATION_ENTITY);
        // each tick, there is a 1/20 chance we will apply radiation to each player
        // this helps distribute the CPU load across ticks, and makes exposure slightly inconsistent
        if (entity.level().getRandom().nextInt(SharedConstants.TICKS_PER_SECOND) == 0) {
            double magnitude = getRadiationLevel(entity);
            if (magnitude > BASELINE && (!(entity instanceof Player player) || MekanismUtils.isPlayingMode(player))) {
                // apply radiation to the player
                radiate(entity, magnitude / 3_600D); // convert to Sv/s
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
                    PacketUtils.sendTo(new PacketPlayerRadiationData(radiation), player);
                }
            }
        }
    }

    public void tickServerWorld(ServerLevel world) {
        // terminate early if we're disabled
        if (!isRadiationEnabled()) {
            return;
        }
        if (!loaded) {
            createOrLoad();
        }

        // update meltdowns
        List<Meltdown> dimensionMeltdowns = meltdowns.getOrDefault(world.dimension().location(), Collections.emptyList());
        if (!dimensionMeltdowns.isEmpty()) {
            //noinspection Java8CollectionRemoveIf - We can't replace it with removeIf as it has a capturing lambda
            for (Iterator<Meltdown> iterator = dimensionMeltdowns.iterator(); iterator.hasNext(); ) {
                Meltdown meltdown = iterator.next();
                if (meltdown.update(world)) {
                    iterator.remove();
                }
            }
            //If we have/had any meltdowns mark our data handler as dirty as when a meltdown updates
            // the number of ticks it has been around for will change
            markDirty();
        }
    }

    public void tickServer() {
        // terminate early if we're disabled or there is no radiation spots
        if (!isRadiationEnabled() || radiationTable.isEmpty()) {
            return;
        }
        // each tick, there's a 1/20 chance we'll decay radiation sources (averages to 1 decay operation per second)
        if (RAND.nextInt(SharedConstants.TICKS_PER_SECOND) == 0) {
            Collection<RadiationSource> sources = radiationTable.values();
            if (!sources.isEmpty()) {
                // remove if source gets too low
                sources.removeIf(RadiationSource::decay);
                //Mark dirty regardless if we have any sources as magnitude changes or radiation sources change
                markDirty();
                //Update radiation levels for any players where it has changed
                updateClientRadiationForAll();
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
        playerEnvironmentalExposureMap.clear();
        playerExposureMap.clear();
        meltdowns.clear();
        dataHandler = null;
        loaded = false;
    }

    public void resetClient() {
        setClientEnvironmentalRadiation(BASELINE, BASELINE);
    }

    public void resetPlayer(UUID uuid) {
        playerEnvironmentalExposureMap.remove(uuid);
        playerExposureMap.remove(uuid);
    }

    @SubscribeEvent
    public void onLivingTick(LivingTickEvent event) {
        Level world = event.getEntity().level();
        if (!world.isClientSide() && !(event.getEntity() instanceof Player)) {
            updateEntityRadiation(event.getEntity());
        }
    }

    public record LevelAndMaxMagnitude(double level, double maxMagnitude) {

        private static final LevelAndMaxMagnitude BASELINE = new LevelAndMaxMagnitude(RadiationManager.BASELINE, RadiationManager.BASELINE);
    }

    public enum RadiationScale {
        NONE,
        LOW,
        MEDIUM,
        ELEVATED,
        HIGH,
        EXTREME;

        /**
         * Get the corresponding RadiationScale from an equivalent dose rate (Sv/h)
         */
        public static RadiationScale get(double magnitude) {
            if (magnitude < 0.00001) { // 10 uSv/h
                return NONE;
            } else if (magnitude < 0.001) { // 1 mSv/h
                return LOW;
            } else if (magnitude < 0.1) { // 100 mSv/h
                return MEDIUM;
            } else if (magnitude < 10) { // 100 Sv/h
                return ELEVATED;
            } else if (magnitude < 100) {
                return HIGH;
            }
            return EXTREME;
        }

        /**
         * For both Sv and Sv/h.
         */
        public static EnumColor getSeverityColor(double magnitude) {
            if (magnitude <= BASELINE) {
                return EnumColor.BRIGHT_GREEN;
            } else if (magnitude < 0.00001) { // 10 uSv/h
                return EnumColor.GRAY;
            } else if (magnitude < 0.001) { // 1 mSv/h
                return EnumColor.YELLOW;
            } else if (magnitude < 0.1) { // 100 mSv/h
                return EnumColor.ORANGE;
            } else if (magnitude < 10) { // 100 Sv/h
                return EnumColor.RED;
            }
            return EnumColor.DARK_RED;
        }

        private static final double LOG_BASELINE = Math.log10(MIN_MAGNITUDE);
        private static final double LOG_MAX = Math.log10(100); // 100 Sv
        private static final double SCALE = LOG_MAX - LOG_BASELINE;

        /**
         * Gets the severity of a dose (between 0 and 1) from a provided dosage in Sv.
         */
        public static double getScaledDoseSeverity(double magnitude) {
            if (magnitude < MIN_MAGNITUDE) {
                return 0;
            }
            return Math.min(1, Math.max(0, (-LOG_BASELINE + Math.log10(magnitude)) / SCALE));
        }

        public SoundEvent getSoundEvent() {
            return switch (this) {
                case LOW -> MekanismSounds.GEIGER_SLOW.get();
                case MEDIUM -> MekanismSounds.GEIGER_MEDIUM.get();
                case ELEVATED, HIGH -> MekanismSounds.GEIGER_ELEVATED.get();
                case EXTREME -> MekanismSounds.GEIGER_FAST.get();
                default -> null;
            };
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

    public static class RadiationDataHandler extends MekanismSavedData {

        private Map<ResourceLocation, List<Meltdown>> savedMeltdowns = Collections.emptyMap();
        public Set<RadiationSource> loadedSources = Collections.emptySet();
        @Nullable
        public RadiationManager manager;

        public void setManagerAndSync(RadiationManager m) {
            manager = m;
            // don't sync the manager if radiation has been disabled
            if (IRadiationManager.INSTANCE.isRadiationEnabled()) {
                for (RadiationSource source : loadedSources) {
                    manager.radiationTable.put(new Chunk3D(source.getPos()), source.getPos(), source);
                }
                for (Map.Entry<ResourceLocation, List<Meltdown>> entry : savedMeltdowns.entrySet()) {
                    List<Meltdown> meltdowns = manager.meltdowns.get(entry.getKey());
                    if (meltdowns == null) {
                        manager.meltdowns.put(entry.getKey(), new ArrayList<>(entry.getValue()));
                    } else {
                        meltdowns.addAll(entry.getValue());
                    }
                }
            }
        }

        public void clearCached() {
            //Clear cached sources and meltdowns after loading them to not keep pointers in our data handler
            // that are referencing objects that eventually will be removed
            loadedSources = Collections.emptySet();
            savedMeltdowns = Collections.emptyMap();
        }

        @Override
        public void load(@NotNull CompoundTag nbtTags) {
            if (nbtTags.contains(NBTConstants.RADIATION_LIST, Tag.TAG_LIST)) {
                ListTag list = nbtTags.getList(NBTConstants.RADIATION_LIST, Tag.TAG_COMPOUND);
                loadedSources = new HashList<>();
                for (Tag nbt : list) {
                    RadiationSource.load((CompoundTag) nbt).ifPresent(loadedSources::add);
                }
            } else {
                loadedSources = Collections.emptySet();
            }
            if (nbtTags.contains(NBTConstants.MELTDOWNS, Tag.TAG_COMPOUND)) {
                CompoundTag meltdownNBT = nbtTags.getCompound(NBTConstants.MELTDOWNS);
                savedMeltdowns = new HashMap<>(meltdownNBT.size());
                for (String dim : meltdownNBT.getAllKeys()) {
                    ResourceLocation dimension = ResourceLocation.tryParse(dim);
                    if (dimension != null) {
                        //It should be a valid dimension, but validate it just in case
                        ListTag meltdowns = meltdownNBT.getList(dim, Tag.TAG_COMPOUND);
                        savedMeltdowns.put(dimension, meltdowns.stream().map(nbt -> Meltdown.load((CompoundTag) nbt)).collect(Collectors.toList()));
                    }
                }
            } else {
                savedMeltdowns = Collections.emptyMap();
            }
        }

        @NotNull
        @Override
        public CompoundTag save(@NotNull CompoundTag nbtTags) {
            if (manager != null && !manager.radiationTable.isEmpty()) {
                ListTag list = new ListTag();
                for (RadiationSource source : manager.radiationTable.values()) {
                    list.add(source.write());
                }
                nbtTags.put(NBTConstants.RADIATION_LIST, list);
            }
            if (manager != null && !manager.meltdowns.isEmpty()) {
                CompoundTag meltdownNBT = new CompoundTag();
                for (Map.Entry<ResourceLocation, List<Meltdown>> entry : manager.meltdowns.entrySet()) {
                    List<Meltdown> meltdowns = entry.getValue();
                    if (!meltdowns.isEmpty()) {
                        ListTag list = new ListTag();
                        for (Meltdown meltdown : meltdowns) {
                            CompoundTag compound = new CompoundTag();
                            meltdown.write(compound);
                            list.add(compound);
                        }
                        meltdownNBT.put(entry.getKey().toString(), list);
                    }
                }
                if (!meltdownNBT.isEmpty()) {
                    nbtTags.put(NBTConstants.MELTDOWNS, meltdownNBT);
                }
            }
            return nbtTags;
        }
    }
}
