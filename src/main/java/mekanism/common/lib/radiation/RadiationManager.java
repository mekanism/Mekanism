package mekanism.common.lib.radiation;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.function.IntSupplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Coord4D;
import mekanism.api.NBTConstants;
import mekanism.api.text.EnumColor;
import mekanism.common.Mekanism;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.config.MekanismConfig;
import mekanism.common.lib.HashList;
import mekanism.common.lib.math.voxel.Chunk3D;
import mekanism.common.lib.radiation.capability.IRadiationEntity;
import mekanism.common.lib.radiation.capability.IRadiationShielding;
import mekanism.common.network.PacketRadiationData;
import mekanism.common.registries.MekanismParticleTypes;
import mekanism.common.registries.MekanismSounds;
import mekanism.common.util.CapabilityUtils;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.particles.BasicParticleType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.storage.DimensionSavedDataManager;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

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
 * @author aidancbrady
 */
public class RadiationManager {

    private static final String DATA_HANDLER_NAME = "radiation_manager";
    private static final IntSupplier MAX_RANGE = () -> MekanismConfig.general.radiationChunkCheckRadius.get() * 16;
    private static final Random RAND = new Random();

    public static final double BASELINE = 0.0000001; // 100 nSv/h
    public static final double MIN_MAGNITUDE = 0.00001; // 10 uSv/h

    private boolean loaded;

    private final Map<Chunk3D, Map<Coord4D, RadiationSource>> radiationMap = new Object2ObjectOpenHashMap<>();
    private final Map<ResourceLocation, List<Meltdown>> meltdowns = new Object2ObjectOpenHashMap<>();

    private final Map<UUID, RadiationScale> playerExposureMap = new Object2ObjectOpenHashMap<>();

    // client fields
    private RadiationScale clientRadiationScale = RadiationScale.NONE;

    /**
     * Note: This can and will be null on the client side
     */
    @Nullable
    private RadiationDataHandler dataHandler;

    public double getRadiationLevel(Entity entity) {
        return getRadiationLevel(new Coord4D(entity));
    }

    /**
     * Get the radiation level (in sV/h) at a certain location.
     *
     * @param coord - location
     *
     * @return radiation level (in sV)
     */
    public double getRadiationLevel(Coord4D coord) {
        Set<Chunk3D> checkChunks = new Chunk3D(coord).expand(MekanismConfig.general.radiationChunkCheckRadius.get());
        double level = BASELINE;

        for (Chunk3D chunk : checkChunks) {
            if (radiationMap.containsKey(chunk)) {
                for (RadiationSource src : radiationMap.get(chunk).values()) {
                    // we only compute exposure when within the MAX_RANGE bounds
                    if (src.getPos().distanceTo(coord) <= MAX_RANGE.getAsInt()) {
                        double add = computeExposure(coord, src);
                        level += add;
                    }
                }
            }
        }

        return level;
    }

    public void radiate(Coord4D coord, double magnitude) {
        if (!MekanismConfig.general.radiationEnabled.get()) {
            return;
        }
        Chunk3D chunk = new Chunk3D(coord);
        boolean found = false;
        if (radiationMap.containsKey(chunk)) {
            RadiationSource src = radiationMap.get(chunk).get(coord);
            if (src != null) {
                src.radiate(magnitude);
                found = true;
            }
        }
        if (!found) {
            radiationMap.computeIfAbsent(new Chunk3D(coord), c -> new Object2ObjectOpenHashMap<>()).put(coord, new RadiationSource(coord, magnitude));
        }
    }

    public void radiate(LivingEntity entity, double magnitude) {
        if (!MekanismConfig.general.radiationEnabled.get()) {
            return;
        }
        if (!(entity instanceof PlayerEntity) || MekanismUtils.isPlayingMode((PlayerEntity) entity)) {
            entity.getCapability(Capabilities.RADIATION_ENTITY_CAPABILITY).ifPresent(c -> c.radiate(magnitude * (1 - Math.min(1, getRadiationResistance(entity)))));
        }
    }

    public void createMeltdown(World world, BlockPos minPos, BlockPos maxPos, double magnitude, double chance) {
        meltdowns.computeIfAbsent(world.getDimensionKey().getLocation(), id -> new ArrayList<>()).add(new Meltdown(world, minPos, maxPos, magnitude, chance));
    }

    public void clearSources() {
        radiationMap.clear();
    }

    private double computeExposure(Coord4D coord, RadiationSource source) {
        return source.getMagnitude() / Math.max(1, Math.pow(coord.distanceTo(source.getPos()), 2));
    }

    private double getRadiationResistance(LivingEntity entity) {
        double resistance = 0;
        for (EquipmentSlotType type : EnumUtils.ARMOR_SLOTS) {
            ItemStack stack = entity.getItemStackFromSlot(type);
            Optional<IRadiationShielding> shielding = CapabilityUtils.getCapability(stack, Capabilities.RADIATION_SHIELDING_CAPABILITY, null).resolve();
            if (shielding.isPresent()) {
                resistance += shielding.get().getRadiationShielding();
            }
        }
        return resistance;
    }

    public void setClientScale(RadiationScale scale) {
        clientRadiationScale = scale;
    }

    public RadiationScale getClientScale() {
        return clientRadiationScale;
    }

    public void tickClient(PlayerEntity player) {
        // perhaps also play geiger counter sound effect, even when not using item (similar to fallout)
        if (clientRadiationScale != RadiationScale.NONE && player.world.getRandom().nextInt(2) == 0) {
            int count = player.world.getRandom().nextInt(clientRadiationScale.ordinal() * MekanismConfig.client.radiationParticleCount.get());
            int radius = MekanismConfig.client.radiationParticleRadius.get();
            for (int i = 0; i < count; i++) {
                double x = player.getPosX() + player.world.getRandom().nextDouble() * radius * 2 - radius;
                double y = player.getPosY() + player.world.getRandom().nextDouble() * radius * 2 - radius;
                double z = player.getPosZ() + player.world.getRandom().nextDouble() * radius * 2 - radius;
                player.world.addParticle((BasicParticleType) MekanismParticleTypes.RADIATION.getParticleType(), x, y, z, 0, 0, 0);
            }
        }
    }

    public void tickServer(ServerPlayerEntity player) {
        updateEntityRadiation(player);
    }

    public void updateEntityRadiation(LivingEntity entity) {
        // terminate early if we're disabled
        if (!MekanismConfig.general.radiationEnabled.get()) {
            return;
        }
        LazyOptional<IRadiationEntity> radiationCap = entity.getCapability(Capabilities.RADIATION_ENTITY_CAPABILITY);
        // each tick, there is a 1/20 chance we will apply radiation to each player
        // this helps distribute the CPU load across ticks, and makes exposure slightly inconsistent
        if (entity.world.getRandom().nextInt(20) == 0) {
            double magnitude = getRadiationLevel(new Coord4D(entity));
            if (magnitude > BASELINE && (!(entity instanceof PlayerEntity) || MekanismUtils.isPlayingMode((PlayerEntity) entity))) {
                // apply radiation to the player
                radiate(entity, magnitude / 3_600D); // convert to Sv/s
            }
            radiationCap.ifPresent(IRadiationEntity::decay);
            if (entity instanceof ServerPlayerEntity) {
                ServerPlayerEntity player = (ServerPlayerEntity) entity;
                RadiationScale scale = RadiationScale.get(magnitude);
                if (playerExposureMap.get(player.getUniqueID()) != scale) {
                    playerExposureMap.put(player.getUniqueID(), scale);
                    Mekanism.packetHandler.sendTo(PacketRadiationData.create(scale), player);
                }
            }
        }
        // update the radiation capability (decay, sync, effects)
        radiationCap.ifPresent(c -> c.update(entity));
    }

    public void tickServerWorld(World world) {
        // terminate early if we're disabled
        if (!MekanismConfig.general.radiationEnabled.get()) {
            return;
        }
        if (!loaded) {
            createOrLoad();
        }

        // update meltdowns
        ResourceLocation dimension = world.getDimensionKey().getLocation();
        if (meltdowns.containsKey(dimension)) {
            meltdowns.get(dimension).removeIf(Meltdown::update);
        }
    }

    public void tickServer() {
        // terminate early if we're disabled
        if (!MekanismConfig.general.radiationEnabled.get()) {
            return;
        }
        // each tick, there's a 1/20 chance we'll decay radiation sources (averages to 1 decay operation per second)
        if (RAND.nextInt(20) == 0) {
            for (Map<Coord4D, RadiationSource> set : radiationMap.values()) {
                for (Iterator<Map.Entry<Coord4D, RadiationSource>> iter = set.entrySet().iterator(); iter.hasNext(); ) {
                    Map.Entry<Coord4D, RadiationSource> entry = iter.next();
                    if (entry.getValue().decay()) {
                        // remove if source gets too low
                        iter.remove();
                    }

                    dataHandler.markDirty();
                }
            }
        }
    }

    /**
     * Note: This should only be called from the server side
     */
    public void createOrLoad() {
        if (dataHandler == null) {
            //Always associate the world with the over world as the frequencies are global
            DimensionSavedDataManager savedData = ServerLifecycleHooks.getCurrentServer().func_241755_D_().getSavedData();
            dataHandler = savedData.getOrCreate(RadiationDataHandler::new, DATA_HANDLER_NAME);
            dataHandler.setManager(this);
            dataHandler.syncManager();
        }

        loaded = true;
    }

    public void reset() {
        clearSources();
        playerExposureMap.clear();
        meltdowns.clear();
        dataHandler = null;
        loaded = false;
    }

    public void resetClient() {
        clientRadiationScale = RadiationScale.NONE;
    }

    public void resetPlayer(UUID uuid) {
        playerExposureMap.remove(uuid);
    }

    @SubscribeEvent
    public void onLivingUpdate(LivingUpdateEvent event) {
        World world = event.getEntityLiving().getEntityWorld();
        if (!world.isRemote() && !(event.getEntityLiving() instanceof PlayerEntity) && world.getRandom().nextInt() % 20 == 0) {
            updateEntityRadiation(event.getEntityLiving());
        }
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
            } else {
                return EXTREME;
            }
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
            } else {
                return EnumColor.DARK_RED;
            }
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
            switch (this) {
                case LOW:
                    return MekanismSounds.GEIGER_SLOW.get();
                case MEDIUM:
                    return MekanismSounds.GEIGER_MEDIUM.get();
                case ELEVATED:
                case HIGH:
                    return MekanismSounds.GEIGER_ELEVATED.get();
                case EXTREME:
                    return MekanismSounds.GEIGER_FAST.get();
                default:
                    return null;
            }
        }
    }

    public static class RadiationDataHandler extends WorldSavedData {

        public RadiationManager manager;
        public List<RadiationSource> loadedSources;

        public RadiationDataHandler() {
            super(DATA_HANDLER_NAME);
        }

        public void setManager(RadiationManager m) {
            manager = m;
        }

        public void syncManager() {
            // don't sync the manager if radiation has been disabled
            if (loadedSources != null && MekanismConfig.general.radiationEnabled.get()) {
                for (RadiationSource source : loadedSources) {
                    Chunk3D chunk = new Chunk3D(source.getPos());
                    manager.radiationMap.computeIfAbsent(chunk, c -> new Object2ObjectOpenHashMap<>()).put(source.getPos(), source);
                }
            }
        }

        @Override
        public void read(@Nonnull CompoundNBT nbtTags) {
            if (nbtTags.contains(NBTConstants.RADIATION_LIST, NBT.TAG_LIST)) {
                ListNBT list = nbtTags.getList(NBTConstants.RADIATION_LIST, NBT.TAG_COMPOUND);
                loadedSources = new HashList<>();
                for (int i = 0; i < list.size(); i++) {
                    loadedSources.add(RadiationSource.load(list.getCompound(0)));
                }
            }
        }

        @Nonnull
        @Override
        public CompoundNBT write(@Nonnull CompoundNBT nbtTags) {
            ListNBT list = new ListNBT();
            for (Map<Coord4D, RadiationSource> map : manager.radiationMap.values()) {
                for (RadiationSource source : map.values()) {
                    CompoundNBT compound = new CompoundNBT();
                    source.write(compound);
                    list.add(compound);
                }
            }
            nbtTags.put(NBTConstants.RADIATION_LIST, list);
            return nbtTags;
        }
    }
}
