package mekanism.common.frequency;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nonnull;
import mekanism.api.Coord4D;
import mekanism.api.TileNetworkList;
import mekanism.common.Mekanism;
import mekanism.common.util.MekanismUtils;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.world.chunk.AbstractChunkProvider;
import net.minecraft.world.server.ServerChunkProvider;
import net.minecraft.world.storage.DimensionSavedDataManager;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.util.Constants.NBT;

public class FrequencyManager {

    public static final int MAX_FREQ_LENGTH = 16;
    public static final List<Character> SPECIAL_CHARS = Arrays.asList('-', ' ', '|', '\'', '\"', '_', '+', ':', '(', ')', '?', '!', '/', '@', '$', '`', '~', ',', '.', '#');

    public static boolean loaded;

    private static Set<FrequencyManager> managers = new HashSet<>();

    private Set<Frequency> frequencies = new HashSet<>();

    private FrequencyDataHandler dataHandler;

    private UUID ownerUUID;

    private String name;

    private Class<? extends Frequency> frequencyClass;

    public FrequencyManager(Class<? extends Frequency> c, String n) {
        frequencyClass = c;
        name = n;
        managers.add(this);
    }

    public FrequencyManager(Class<? extends Frequency> c, String n, UUID uuid) {
        this(c, n);
        ownerUUID = uuid;
    }

    public static void load(World world) {
        loaded = true;
        for (FrequencyManager manager : managers) {
            manager.createOrLoad(world);
        }
    }

    public static void tick(World world) {
        if (!loaded) {
            load(world);
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
        for (Frequency iterFreq : frequencies) {
            if (freq.equals(iterFreq)) {
                iterFreq.activeCoords.add(coord);
                dataHandler.markDirty();
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
                dataHandler.markDirty();
            }
        }
    }

    public void remove(String name) {
        for (Iterator<Frequency> iter = getFrequencies().iterator(); iter.hasNext(); ) {
            Frequency iterFreq = iter.next();
            if (iterFreq.name.equals(name)) {
                iter.remove();
                dataHandler.markDirty();
            }
        }
    }

    public void deactivate(Coord4D coord) {
        for (Frequency freq : frequencies) {
            freq.activeCoords.remove(coord);
            dataHandler.markDirty();
        }
    }

    public Frequency validateFrequency(UUID uuid, Coord4D coord, Frequency freq) {
        for (Frequency iterFreq : frequencies) {
            if (freq.equals(iterFreq)) {
                iterFreq.activeCoords.add(coord);
                dataHandler.markDirty();
                return iterFreq;
            }
        }

        if (uuid.equals(freq.ownerUUID)) {
            freq.activeCoords.add(coord);
            freq.valid = true;
            frequencies.add(freq);
            dataHandler.markDirty();
            return freq;
        }
        return null;
    }

    public void createOrLoad(World world) {
        String name = getName();
        if (dataHandler == null) {
            AbstractChunkProvider chunkProvider = world.getChunkProvider();
            //TODO: Is this fine or do we have to handle the other cases also
            if (chunkProvider instanceof ServerChunkProvider) {
                DimensionSavedDataManager savedData = ((ServerChunkProvider) chunkProvider).getSavedData();
                dataHandler = savedData.getOrCreate(() -> new FrequencyDataHandler(name), name);
                //TODO: Do we have to save it if it didn't exist before, or does this automatically happen
                /*if (dataHandler == null) {
                    dataHandler = new FrequencyDataHandler(name);
                    dataHandler.setManager(this);
                    world.getPerWorldStorage().setData(name, dataHandler);
                } else {*/
                dataHandler.setManager(this);
                dataHandler.syncManager();
            }
        }
    }

    public Set<Frequency> getFrequencies() {
        return frequencies;
    }

    public void addFrequency(Frequency freq) {
        frequencies.add(freq);
        dataHandler.markDirty();
    }

    public boolean containsFrequency(String name) {
        for (Frequency freq : frequencies) {
            if (freq.name.equals(name)) {
                return true;
            }
        }
        return false;
    }

    public void tickSelf(World world) {
        for (Frequency iterFreq : frequencies) {
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

    public void writeFrequencies(TileNetworkList data) {
        data.add(frequencies.size());
        for (Frequency freq : frequencies) {
            freq.write(data);
        }
    }

    public Set<Frequency> readFrequencies(PacketBuffer dataStream) {
        Set<Frequency> ret = new HashSet<>();
        int size = dataStream.readInt();
        try {
            for (int i = 0; i < size; i++) {
                Frequency freq = frequencyClass.getConstructor(new Class[]{PacketBuffer.class}).newInstance(dataStream);
                freq.read(dataStream);
                ret.add(freq);
            }
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
        return ret;
    }

    public String getName() {
        return ownerUUID != null ? (ownerUUID.toString() + "_" + name + "FrequencyHandler") : (name + "FrequencyHandler");
    }

    public static class FrequencyDataHandler extends WorldSavedData {

        public FrequencyManager manager;

        public Set<Frequency> loadedFrequencies;
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
            try {
                String frequencyClass = nbtTags.getString("frequencyClass");//todo fix this using a classname from nbt!
                if (nbtTags.contains("ownerUUID")) {
                    loadedOwner = UUID.fromString(nbtTags.getString("ownerUUID"));
                }
                ListNBT list = nbtTags.getList("freqList", NBT.TAG_COMPOUND);
                loadedFrequencies = new HashSet<>();
                for (int i = 0; i < list.size(); i++) {
                    CompoundNBT compound = list.getCompound(i);
                    Constructor<?> c = Class.forName(frequencyClass).getConstructor(CompoundNBT.class);
                    Frequency freq = (Frequency) c.newInstance(compound);
                    loadedFrequencies.add(freq);
                }
            } catch (ReflectiveOperationException e) {
                Mekanism.logger.error("Couldn't load frequency data", e);
            }
        }

        @Nonnull
        @Override
        public CompoundNBT write(@Nonnull CompoundNBT nbtTags) {
            nbtTags.putString("frequencyClass", manager.frequencyClass.getName());
            if (manager.ownerUUID != null) {
                nbtTags.putString("ownerUUID", manager.ownerUUID.toString());
            }
            ListNBT list = new ListNBT();
            for (Frequency freq : manager.getFrequencies()) {
                CompoundNBT compound = new CompoundNBT();
                freq.write(compound);
                list.add(compound);
            }
            nbtTags.put("freqList", list);
            return nbtTags;
        }
    }
}