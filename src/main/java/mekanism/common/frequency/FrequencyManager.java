package mekanism.common.frequency;

import io.netty.buffer.ByteBuf;
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
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.util.Constants.NBT;

public class FrequencyManager {

    public static final int MAX_FREQ_LENGTH = 16;
    public static final List<Character> SPECIAL_CHARS = Arrays
          .asList('-', ' ', '|', '\'', '\"', '_', '+', ':', '(', ')',
                '?', '!', '/', '@', '$', '`', '~', ',', '.', '#');

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
            dataHandler = (FrequencyDataHandler) world.getPerWorldStorage()
                  .getOrLoadData(FrequencyDataHandler.class, name);

            if (dataHandler == null) {
                dataHandler = new FrequencyDataHandler(name);
                dataHandler.setManager(this);
                world.getPerWorldStorage().setData(name, dataHandler);
            } else {
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

                if (coord.dimensionId == world.provider.getDimension()) {
                    if (!coord.exists(world)) {
                        iter.remove();
                    } else {
                        TileEntity tile = coord.getTileEntity(world);

                        if (!(tile instanceof IFrequencyHandler)) {
                            iter.remove();
                        } else {
                            Frequency freq = ((IFrequencyHandler) tile).getFrequency(this);

                            if (freq == null || !freq.equals(iterFreq)) {
                                iter.remove();
                            }
                        }
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

    public Set<Frequency> readFrequencies(ByteBuf dataStream) {
        Set<Frequency> ret = new HashSet<>();
        int size = dataStream.readInt();

        try {
            for (int i = 0; i < size; i++) {
                Frequency freq = frequencyClass.getConstructor(new Class[]{ByteBuf.class}).newInstance(dataStream);
                freq.read(dataStream);
                ret.add(freq);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ret;
    }

    public String getName() {
        return ownerUUID != null ? (ownerUUID.toString() + "_" + name + "FrequencyHandler")
              : (name + "FrequencyHandler");
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
        public void readFromNBT(@Nonnull NBTTagCompound nbtTags) {
            try {
                String frequencyClass = nbtTags.getString("frequencyClass");

                if (nbtTags.hasKey("ownerUUID")) {
                    loadedOwner = UUID.fromString(nbtTags.getString("ownerUUID"));
                }

                NBTTagList list = nbtTags.getTagList("freqList", NBT.TAG_COMPOUND);

                loadedFrequencies = new HashSet<>();

                for (int i = 0; i < list.tagCount(); i++) {
                    NBTTagCompound compound = list.getCompoundTagAt(i);

                    Constructor<?> c = Class.forName(frequencyClass).getConstructor(NBTTagCompound.class);
                    Frequency freq = (Frequency) c.newInstance(compound);

                    loadedFrequencies.add(freq);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Nonnull
        @Override
        public NBTTagCompound writeToNBT(@Nonnull NBTTagCompound nbtTags) {
            nbtTags.setString("frequencyClass", manager.frequencyClass.getName());

            if (manager.ownerUUID != null) {
                nbtTags.setString("ownerUUID", manager.ownerUUID.toString());
            }

            NBTTagList list = new NBTTagList();

            for (Frequency freq : manager.getFrequencies()) {
                NBTTagCompound compound = new NBTTagCompound();
                freq.write(compound);
                list.appendTag(compound);
            }

            nbtTags.setTag("freqList", list);

            return nbtTags;
        }
    }
}
