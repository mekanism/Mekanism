package mekanism.common.frequency;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import mekanism.api.Coord4D;
import mekanism.api.NBTConstants;
import mekanism.common.lib.HashList;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.NBTUtils;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.storage.DimensionSavedDataManager;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

public class FrequencyManager<FREQ extends Frequency> {

    public static final int MAX_FREQ_LENGTH = 16;
    public static final List<Character> SPECIAL_CHARS = Arrays.asList('-', ' ', '|', '\'', '\"', '_', '+', ':', '(', ')', '?', '!', '/', '@', '$', '`', '~', ',', '.', '#');

    public static boolean loaded;

    private static Set<FrequencyManager<?>> managers = new ObjectOpenHashSet<>();

    private Map<Object, FREQ> frequencies = new LinkedHashMap<>();

    /**
     * Note: This can and will be null on the client side
     */
    @Nullable
    private FrequencyDataHandler dataHandler;

    private UUID ownerUUID;

    private final FrequencyType<FREQ> frequencyType;

    public FrequencyManager(FrequencyType<FREQ> frequencyType) {
        this.frequencyType = frequencyType;
        managers.add(this);
    }

    public FrequencyManager(FrequencyType<FREQ> frequencyType, UUID uuid) {
        this(frequencyType);
        ownerUUID = uuid;
    }

    /**
     * Note: This should only be called from the server side
     */
    public static void load() {
        loaded = true;
        managers.forEach(manager -> manager.createOrLoad());
    }

    public static void tick(World world) {
        if (!loaded && !world.isRemote()) {
            load();
        }
        managers.forEach(manager -> manager.tickSelf(world));
    }

    public static void reset() {
        for (FrequencyManager<?> manager : managers) {
            manager.frequencies.clear();
            manager.dataHandler = null;
        }
        loaded = false;
    }

    public FREQ update(Coord4D coord, FREQ freq) {
        FREQ storedFreq = getFrequency(freq.getKey());
        if (storedFreq != null) {
            storedFreq.activeCoords.add(coord);
            if (dataHandler != null) {
                dataHandler.markDirty();
            }
            return storedFreq;
        }

        deactivate(freq, coord);
        return null;
    }

    public void remove(Object key, UUID ownerUUID) {
        FREQ freq = getFrequency(key);
        if (freq != null && freq.ownerUUID.equals(ownerUUID)) {
            frequencies.remove(key);
            if (dataHandler != null) {
                dataHandler.markDirty();
            }
        }
    }

    public void deactivate(Frequency freq, Coord4D coord) {
        if (freq != null) {
            freq.activeCoords.remove(coord);
            if (dataHandler != null) {
                dataHandler.markDirty();
            }
        }
    }

    public FREQ validateFrequency(UUID uuid, Coord4D coord, FREQ freq) {
        FREQ storedFreq = getFrequency(freq.getKey());
        if (storedFreq != null) {
            storedFreq.activeCoords.add(coord);
            if (dataHandler != null) {
                dataHandler.markDirty();
            }
            return storedFreq;
        }

        if (uuid.equals(freq.ownerUUID)) {
            freq.activeCoords.add(coord);
            freq.valid = true;
            addFrequency(freq);
            return freq;
        }
        return null;
    }

    /**
     * Note: This should only be called from the server side
     */
    public void createOrLoad() {
        if (dataHandler == null) {
            String name = getName();
            //Always associate the world with the over world as the frequencies are global
            DimensionSavedDataManager savedData = ServerLifecycleHooks.getCurrentServer().getWorld(DimensionType.OVERWORLD).getSavedData();
            dataHandler = savedData.getOrCreate(() -> new FrequencyDataHandler(name), name);
            dataHandler.syncManager();
        }
    }

    public Collection<FREQ> getFrequencies() {
        return frequencies.values();
    }

    public FREQ getFrequency(Object key) {
        return frequencies.get(key);
    }

    public void addFrequency(FREQ freq) {
        frequencies.put(freq.getKey(), freq);
        if (dataHandler != null) {
            dataHandler.markDirty();
        }
    }

    private void tickSelf(World world) {
        for (FREQ iterFreq : getFrequencies()) {
            for (Iterator<Coord4D> iter = iterFreq.activeCoords.iterator(); iter.hasNext(); ) {
                Coord4D coord = iter.next();
                if (coord.dimension.equals(world.getDimension().getType())) {
                    //Note: We will check if the block is loaded while getting the tile so we don't need to
                    // specifically have that case as all it did was also remove iter
                    TileEntity tile = MekanismUtils.getTileEntity(world, coord.getPos());
                    if (tile instanceof IFrequencyHandler) {
                        FREQ freq = ((IFrequencyHandler) tile).getFrequency(frequencyType);
                        if (freq == null || !freq.equals(iterFreq)) {
                            iter.remove();
                        }
                    } else {
                        iter.remove();
                    }
                }
            }
        }
    }

    public String getName() {
        return ownerUUID != null ? (ownerUUID.toString() + "_" + frequencyType.getName() + "FrequencyHandler") : (frequencyType.getName() + "FrequencyHandler");
    }

    public class FrequencyDataHandler extends WorldSavedData {

        public List<FREQ> loadedFrequencies;
        public UUID loadedOwner;

        public FrequencyDataHandler(String tagName) {
            super(tagName);
        }

        public void syncManager() {
            if (loadedFrequencies != null) {
                loadedFrequencies.forEach(freq -> frequencies.put(freq.getKey(), freq));
                ownerUUID = loadedOwner;
            }
        }

        @Override
        public void read(@Nonnull CompoundNBT nbtTags) {
            NBTUtils.setUUIDIfPresent(nbtTags, NBTConstants.OWNER_UUID, uuid -> loadedOwner = uuid);
            Function<CompoundNBT, FREQ> creatorFunction = (nbt) -> frequencyType.create(nbt, false);
            ListNBT list = nbtTags.getList(NBTConstants.FREQUENCY_LIST, NBT.TAG_COMPOUND);
            loadedFrequencies = new HashList<>();
            if (creatorFunction != null) {
                for (int i = 0; i < list.size(); i++) {
                    loadedFrequencies.add(creatorFunction.apply(list.getCompound(i)));
                }
            }
        }

        @Nonnull
        @Override
        public CompoundNBT write(@Nonnull CompoundNBT nbtTags) {
            if (ownerUUID != null) {
                nbtTags.putUniqueId(NBTConstants.OWNER_UUID, ownerUUID);
            }
            ListNBT list = new ListNBT();
            for (FREQ freq : getFrequencies()) {
                CompoundNBT compound = new CompoundNBT();
                freq.write(compound);
                list.add(compound);
            }
            nbtTags.put(NBTConstants.FREQUENCY_LIST, list);
            return nbtTags;
        }
    }
}