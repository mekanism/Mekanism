package mekanism.common.radiation;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import mekanism.api.Chunk3D;
import mekanism.api.Coord4D;
import mekanism.api.NBTConstants;
import mekanism.client.sound.GeigerSound;
import mekanism.client.sound.SoundHandler;
import mekanism.common.HashList;
import mekanism.common.Mekanism;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.network.PacketRadiationData;
import mekanism.common.registries.MekanismParticleTypes;
import mekanism.common.registries.MekanismSounds;
import mekanism.common.util.CapabilityUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.ai.attributes.RangedAttribute;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.particles.BasicParticleType;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.storage.DimensionSavedDataManager;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

/**
 * The RadiationManager handles radiation across all in-game dimensions. Radiation exposure levels are
 * provided in _sieverts, defining a rate of accumulation of equivalent dose. For reference,
 * here are examples of equivalent dose (credit: wikipedia)
 *
 * 100 nSv: baseline dose (banana equivalent dose)
 * 250 nSv: airport security screening
 *   1 mSv: annual total civilian dose equivalent
 *  50 mSv: annual total occupational equivalent dose limit
 * 250 mSv: total dose equivalent from 6-month trip to mars
 *    1 Sv: maximum allowed dose allowed for NASA astronauts over their careers
 *    5 Sv: dose required to (50% chance) kill human if received over 30-day period
 *   50 Sv: dose received after spending 10 min next to Chernobyl reactor core directly after meltdown
 *
 * For defining rate of accumulation, we use _sieverts per hour_ (Sv/h). Here are examples of dose
 * accumulation rates.
 *
 * 100 nSv/h: max recommended human irradiation
 * 2.7 uSv/h: irradiation from airline at cruise altitude
 * 190 mSv/h: highest reading from fallout of Trinity (Manhattan project test) bomb, _20 miles away_, 3 hours after detonation
 * ~500 Sv/h: irradiation inside primary containment vessel of Fukushima power station
 *       (at this rate, it takes 30 seconds to accumulate a median lethal dose)
 *
 * @author aidancbrady
 *
 */
public class RadiationManager {

    private static final String DATA_HANDLER_NAME = "radiation_manager";
    private static final int CHUNK_CHECK_RADIUS = 5;
    private static final int MAX_RANGE = CHUNK_CHECK_RADIUS * 16;
    private static final int PARTICLE_RADIUS = 30;
    private static final int PARTICLE_COUNT = 100;

    public static final IAttribute ATTRIBUTE_RADIATION = (new RangedAttribute((IAttribute)null, "generic.radiation", 0.0D, 0.0D, 1000.0D)).setShouldWatch(true);

    public static final double BASELINE = 0.0000001; // 100 nSv/h
    public static final double MIN_SRC_MAGNITUDE = 0.00001; // 10 uSv/h
    public static final double DECAY_RATE = 0.9995; // decay multiplier per second, will take about 10 days to remove a 1000 Sv/h source

    private boolean loaded;

    private Map<Chunk3D, Map<Coord4D, RadiationSource>> radiationMap = new Object2ObjectOpenHashMap<>();

    private Map<UUID, RadiationScale> playerExposureMap = new Object2ObjectOpenHashMap<>();

    // client fields
    private RadiationScale clientRadiationScale = RadiationScale.NONE;
    private Map<RadiationScale, GeigerSound> soundMap = new HashMap<>();

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
     * @return radiation level (in sV)
     */
    public double getRadiationLevel(Coord4D coord) {
        Set<Chunk3D> checkChunks = new Chunk3D(coord).expand(CHUNK_CHECK_RADIUS);
        double level = BASELINE;

        for (Chunk3D chunk : checkChunks) {
            if (radiationMap.containsKey(chunk)) {
                for (RadiationSource src : radiationMap.get(chunk).values()) {
                    // we only compute exposure when within the MAX_RANGE bounds
                    if (src.getPos().distanceTo(coord) <= MAX_RANGE) {
                        double add = computeExposure(coord, src);
                        level += add;
                    }
                }
            }
        }

        return level;
    }

    public void radiate(Coord4D coord, double magnitude) {
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

    public void clearSources() {
        radiationMap.clear();
    }

    private double computeExposure(Coord4D coord, RadiationSource source) {
        return source.getMagnitude() / Math.max(1, Math.pow(coord.distanceTo(source.getPos()), 2));
    }

    private double getRadiationResistance(PlayerEntity player) {
        double resistance = 0;
        for (ItemStack stack : player.inventory.armorInventory) {
            IRadiationShielding shielding = CapabilityUtils.getCapability(stack, Capabilities.RADIATION_SHIELDING_CAPABILITY, null).orElse(null);
            if (shielding != null) {
                resistance += shielding.getRadiationShielding();
            }
        }
        return resistance;
    }

    private void applyRadiation(double magnitude, PlayerEntity player) {
        magnitude *= 1 - Math.min(1, getRadiationResistance(player));
        magnitude /= 3600D; // convert to Sv/s
        IAttributeInstance attribute = player.getAttributes().getAttributeInstance(ATTRIBUTE_RADIATION);
        if (attribute == null) {
            player.getAttributes().registerAttribute(ATTRIBUTE_RADIATION);
            attribute = player.getAttributes().getAttributeInstance(ATTRIBUTE_RADIATION);
        }
        attribute.setBaseValue(attribute.getBaseValue() + magnitude);
    }

    private void decayRadiation(PlayerEntity player) {
        IAttributeInstance attribute = player.getAttributes().getAttributeInstance(ATTRIBUTE_RADIATION);

        if (attribute != null) {
            attribute.setBaseValue(attribute.getBaseValue() * DECAY_RATE);
        }
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
            int count = player.world.getRandom().nextInt(clientRadiationScale.ordinal() * PARTICLE_COUNT);
            for (int i = 0; i < count; i++) {
                double x = player.getPosX() + player.world.getRandom().nextDouble() * PARTICLE_RADIUS * 2 - PARTICLE_RADIUS;
                double y = player.getPosY() + player.world.getRandom().nextDouble() * PARTICLE_RADIUS * 2 - PARTICLE_RADIUS;
                double z = player.getPosZ() + player.world.getRandom().nextDouble() * PARTICLE_RADIUS * 2 - PARTICLE_RADIUS;
                player.world.addParticle((BasicParticleType) MekanismParticleTypes.RADIATION.getParticleType(), x, y, z, 0, 0, 0);
            }
        }

        if (soundMap.isEmpty()) {
            for (RadiationScale scale : RadiationScale.values()) {
                if (scale != RadiationScale.NONE) {
                    GeigerSound sound = new GeigerSound(player, scale);
                    soundMap.put(scale, sound);
                    SoundHandler.playSound(sound);
                }
            }
        }
    }

    public void tickServer(PlayerEntity player) {
        // each tick, there is a 1/20 chance we will apply radiation to each player
        // this helps distribute the CPU load across ticks, and makes exposure slightly inconsistent
        if (player.world.getRandom().nextInt(20) == 0) {
            double magnitude = getRadiationLevel(new Coord4D(player));
            if (magnitude > BASELINE && !player.isCreative()) {
                applyRadiation(magnitude, player);
            }
            decayRadiation(player);
            RadiationScale scale = RadiationScale.get(magnitude);
            if (playerExposureMap.get(player.getUniqueID()) != scale) {
                playerExposureMap.put(player.getUniqueID(), scale);
                Mekanism.packetHandler.sendTo(new PacketRadiationData(scale), (ServerPlayerEntity) player);
            }
        }
    }

    public void tickServer(World world) {
        if (!loaded) {
            createOrLoad();
        }

        // each tick, there's a 1/20 chance we'll decay radiation sources (averages to 1 decay operation per second)
        if (world.getRandom().nextInt(20) == 0) {
            for (Map<Coord4D, RadiationSource> set : radiationMap.values()) {
                for (Iterator<Map.Entry<Coord4D, RadiationSource>> iter = set.entrySet().iterator(); iter.hasNext();) {
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
            DimensionSavedDataManager savedData = ServerLifecycleHooks.getCurrentServer().getWorld(DimensionType.OVERWORLD).getSavedData();
            dataHandler = savedData.getOrCreate(() -> new RadiationDataHandler(), DATA_HANDLER_NAME);
            dataHandler.setManager(this);
            dataHandler.syncManager();
        }

        loaded = true;
    }

    public void reset() {
        clearSources();
        playerExposureMap.clear();
        dataHandler = null;
        loaded = false;
    }

    public void resetClient() {
        clientRadiationScale = RadiationScale.NONE;
        soundMap.clear();
    }

    public static enum RadiationScale {
        NONE,
        LOW,
        MEDIUM,
        ELEVATED,
        HIGH;

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
            } else if (magnitude < 100) { // 100 Sv/h
                return ELEVATED;
            } else {
                return HIGH;
            }
        }

        public SoundEvent getSoundEvent() {
            switch(this) {
                case LOW:
                    return MekanismSounds.GEIGER_SLOW.get();
                case MEDIUM:
                    return MekanismSounds.GEIGER_MEDIUM.get();
                case ELEVATED:
                    return MekanismSounds.GEIGER_ELEVATED.get();
                case HIGH:
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
            if (loadedSources != null) {
                for (RadiationSource source : loadedSources) {
                    Chunk3D chunk = new Chunk3D(source.getPos());
                    manager.radiationMap.computeIfAbsent(chunk, c -> new Object2ObjectOpenHashMap<>()).put(source.getPos(), source);
                }
            }
        }

        @Override
        public void read(@Nonnull CompoundNBT nbtTags) {
            if (nbtTags.contains(NBTConstants.RADIATION_LIST)) {
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
