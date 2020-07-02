package mekanism.common.lib.frequency;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.NBTConstants;
import mekanism.common.lib.HashList;
import mekanism.common.lib.frequency.Frequency.FrequencyIdentity;
import mekanism.common.util.NBTUtils;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.storage.DimensionSavedDataManager;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

public class FrequencyManager<FREQ extends Frequency> {

    public static final int MAX_FREQ_LENGTH = 16;

    private static boolean loaded;

    private static final Set<FrequencyManager<?>> managers = new ObjectOpenHashSet<>();

    private final Map<Object, FREQ> frequencies = new LinkedHashMap<>();

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
        if (!loaded) {
            loaded = true;
            managers.forEach(FrequencyManager::createOrLoad);
        }
    }

    public static void tick() {
        if (!loaded) {
            load();
        }
        managers.forEach(FrequencyManager::tickSelf);
    }

    public static void reset() {
        for (FrequencyManager<?> manager : managers) {
            manager.frequencies.clear();
            manager.dataHandler = null;
        }
        loaded = false;
    }

    public FREQ update(TileEntity tile, FREQ freq) {
        FREQ storedFreq = getFrequency(freq.getKey());
        if (storedFreq != null) {
            storedFreq.update(tile);
            if (dataHandler != null) {
                dataHandler.markDirty();
            }
            return storedFreq;
        }

        deactivate(freq, tile);
        return null;
    }

    public void remove(Object key, UUID ownerUUID) {
        FREQ freq = getFrequency(key);
        if (freq != null && freq.getOwner().equals(ownerUUID)) {
            freq.onRemove();
            frequencies.remove(key);
            if (dataHandler != null) {
                dataHandler.markDirty();
            }
        }
    }

    public void deactivate(Frequency freq, TileEntity tile) {
        if (freq != null) {
            freq.onDeactivate(tile);
            if (dataHandler != null) {
                dataHandler.markDirty();
            }
        }
    }

    public FREQ validateFrequency(TileEntity tile, FREQ freq) {
        FREQ storedFreq = getFrequency(freq.getKey());
        if (storedFreq == null) {
            freq.setValid(true);
            addFrequency(freq);
            storedFreq = freq;
        }
        storedFreq.update(tile);
        if (dataHandler != null) {
            dataHandler.markDirty();
        }
        return storedFreq;
    }

    /**
     * Note: This should only be called from the server side
     */
    public void createOrLoad() {
        if (dataHandler == null) {
            String name = getName();
            //Always associate the world with the over world as the frequencies are global
            DimensionSavedDataManager savedData = ServerLifecycleHooks.getCurrentServer().func_241755_D_().getSavedData();
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

    public FREQ getOrCreateFrequency(FrequencyIdentity identity, UUID ownerUUID) {
        FREQ freq = getFrequency(identity.getKey());
        if (freq == null) {
            freq = frequencyType.create(identity.getKey(), ownerUUID);
            freq.setPublic(identity.isPublic());
            addFrequency(freq);
        }
        return freq;
    }

    public void addFrequency(FREQ freq) {
        frequencies.put(freq.getKey(), freq);
        if (dataHandler != null) {
            dataHandler.markDirty();
        }
    }

    public FrequencyType<FREQ> getType() {
        return frequencyType;
    }

    private void tickSelf() {
        getFrequencies().forEach(Frequency::tick);
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
            Function<CompoundNBT, FREQ> creatorFunction = frequencyType::create;
            ListNBT list = nbtTags.getList(NBTConstants.FREQUENCY_LIST, NBT.TAG_COMPOUND);
            loadedFrequencies = new HashList<>();
            for (int i = 0; i < list.size(); i++) {
                loadedFrequencies.add(creatorFunction.apply(list.getCompound(i)));
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