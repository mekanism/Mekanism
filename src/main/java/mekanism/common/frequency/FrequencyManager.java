package mekanism.common.frequency;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Coord4D;
import mekanism.api.NBTConstants;
import mekanism.common.HashList;
import mekanism.common.content.entangloporter.InventoryFrequency;
import mekanism.common.security.SecurityFrequency;
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

public class FrequencyManager {

    public static final int MAX_FREQ_LENGTH = 16;
    public static final List<Character> SPECIAL_CHARS = Arrays.asList('-', ' ', '|', '\'', '\"', '_', '+', ':', '(', ')', '?', '!', '/', '@', '$', '`', '~', ',', '.', '#');

    public static boolean loaded;

    private static Set<FrequencyManager> managers = new ObjectOpenHashSet<>();

    private List<Frequency> frequencies = new HashList<>();

    /**
     * Note: This can and will be null on the client side
     */
    @Nullable
    private FrequencyDataHandler dataHandler;

    private UUID ownerUUID;

    private String name;
    private final FrequencyType frequencyType;

    public FrequencyManager(FrequencyType frequencyType, String name) {
        this.frequencyType = frequencyType;
        this.name = name;
        managers.add(this);
    }

    public FrequencyManager(FrequencyType frequencyType, String name, UUID uuid) {
        this(frequencyType, name);
        ownerUUID = uuid;
    }

    /**
     * Note: This should only be called from the server side
     */
    public static void load() {
        loaded = true;
        for (FrequencyManager manager : managers) {
            manager.createOrLoad();
        }
    }

    public static void tick(World world) {
        if (!loaded && !world.isRemote()) {
            load();
        }
        for (FrequencyManager manager : managers) {
            manager.tickSelf(world);
        }
    }

    public static void reset() {
        for (FrequencyManager manager : managers) {
            manager.frequencies.clear();
            manager.dataHandler = null;
        }
        loaded = false;
    }

    public Frequency update(Coord4D coord, Frequency freq) {
        for (Frequency iterFreq : getFrequencies()) {
            if (freq.equals(iterFreq)) {
                iterFreq.activeCoords.add(coord);
                if (dataHandler != null) {
                    dataHandler.markDirty();
                }
                return iterFreq;
            }
        }
        deactivate(coord);
        return null;
    }

    public void remove(String name, UUID owner) {
        for (Iterator<Frequency> iter = getFrequencies().iterator(); iter.hasNext(); ) {
            Frequency iterFreq = iter.next();
            if (iterFreq.name.equals(name) && iterFreq.ownerUUID.equals(owner)) {
                iter.remove();
                if (dataHandler != null) {
                    dataHandler.markDirty();
                }
            }
        }
    }

    public void remove(String name) {
        for (Iterator<Frequency> iter = getFrequencies().iterator(); iter.hasNext(); ) {
            Frequency iterFreq = iter.next();
            if (iterFreq.name.equals(name)) {
                iter.remove();
                if (dataHandler != null) {
                    dataHandler.markDirty();
                }
            }
        }
    }

    public void deactivate(Coord4D coord) {
        for (Frequency freq : getFrequencies()) {
            freq.activeCoords.remove(coord);
            if (dataHandler != null) {
                dataHandler.markDirty();
            }
        }
    }

    public Frequency validateFrequency(UUID uuid, Coord4D coord, Frequency freq) {
        for (Frequency iterFreq : getFrequencies()) {
            if (freq.equals(iterFreq)) {
                iterFreq.activeCoords.add(coord);
                if (dataHandler != null) {
                    dataHandler.markDirty();
                }
                return iterFreq;
            }
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
            dataHandler.setManager(this);
            dataHandler.syncManager();
        }
    }

    public List<Frequency> getFrequencies() {
        return frequencies;
    }

    public void addFrequency(Frequency freq) {
        frequencies.add(freq);
        if (dataHandler != null) {
            dataHandler.markDirty();
        }
    }

    public boolean containsFrequency(String name) {
        for (Frequency freq : getFrequencies()) {
            if (freq.name.equals(name)) {
                return true;
            }
        }
        return false;
    }

    private void tickSelf(World world) {
        for (Frequency iterFreq : getFrequencies()) {
            for (Iterator<Coord4D> iter = iterFreq.activeCoords.iterator(); iter.hasNext(); ) {
                Coord4D coord = iter.next();
                if (coord.dimension.equals(world.getDimension().getType())) {
                    //Note: We will check if the block is loaded while getting the tile so we don't need to
                    // specifically have that case as all it did was also remove iter
                    TileEntity tile = MekanismUtils.getTileEntity(world, coord.getPos());
                    if (tile instanceof IFrequencyHandler) {
                        Frequency freq = ((IFrequencyHandler) tile).getFrequency(this);
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
        return ownerUUID != null ? (ownerUUID.toString() + "_" + name + "FrequencyHandler") : (name + "FrequencyHandler");
    }

    public static class FrequencyDataHandler extends WorldSavedData {

        public FrequencyManager manager;

        public List<Frequency> loadedFrequencies;
        public UUID loadedOwner;

        public FrequencyDataHandler(String tagName) {
            super(tagName);
        }

        public void setManager(FrequencyManager m) {
            manager = m;
        }

        public void syncManager() {
            if (loadedFrequencies != null) {
                manager.frequencies = loadedFrequencies;
                manager.ownerUUID = loadedOwner;
            }
        }

        @Override
        public void read(@Nonnull CompoundNBT nbtTags) {
            NBTUtils.setUUIDIfPresent(nbtTags, NBTConstants.OWNER_UUID, uuid -> loadedOwner = uuid);
            if (nbtTags.contains(NBTConstants.TYPE, NBT.TAG_INT)) {
                FrequencyType type = FrequencyType.byIndexStatic(nbtTags.getInt(NBTConstants.TYPE));
                Function<CompoundNBT, Frequency> creatorFunction;
                switch (type) {
                    case INVENTORY:
                        creatorFunction = nbt -> new InventoryFrequency(nbt, false);
                        break;
                    case SECURITY:
                        creatorFunction = nbt -> new SecurityFrequency(nbt, false);
                        break;
                    case BASE:
                    default:
                        creatorFunction = nbt -> new Frequency(nbt, false);
                        break;
                }
                ListNBT list = nbtTags.getList(NBTConstants.FREQUENCY_LIST, NBT.TAG_COMPOUND);
                loadedFrequencies = new HashList<>();
                for (int i = 0; i < list.size(); i++) {
                    loadedFrequencies.add(creatorFunction.apply(list.getCompound(i)));
                }
            }
        }

        @Nonnull
        @Override
        public CompoundNBT write(@Nonnull CompoundNBT nbtTags) {
            nbtTags.putInt(NBTConstants.TYPE, manager.frequencyType.ordinal());
            if (manager.ownerUUID != null) {
                nbtTags.putUniqueId(NBTConstants.OWNER_UUID, manager.ownerUUID);
            }
            ListNBT list = new ListNBT();
            for (Frequency freq : manager.getFrequencies()) {
                CompoundNBT compound = new CompoundNBT();
                freq.write(compound);
                list.add(compound);
            }
            nbtTags.put(NBTConstants.FREQUENCY_LIST, list);
            return nbtTags;
        }
    }
}