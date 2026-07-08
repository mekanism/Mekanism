package mekanism.common.lib.frequency;

import com.mojang.serialization.Codec;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import mekanism.api.security.SecurityMode;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.level.storage.SavedDataStorage;
import org.jspecify.annotations.Nullable;

public class FrequencyControllerManager {

    static final List<FrequencyLookup<?>> tickableLookups = new ArrayList<>();
    @Nullable
    private static SavedDataStorage dataStorage = null;
    private static final Map<FrequencyType<?>, FrequencyController<?>> controllers = new IdentityHashMap<>(FrequencyTypes.allRegistered().size());

    public static void serverLoad(MinecraftServer server) {
        dataStorage = server.getDataStorage();
        for (FrequencyType<?> frequencyType : FrequencyTypes.allRegistered()) {
            controllers.put(frequencyType, FrequencyController.create(frequencyType));
        }
    }

    public static void reset() {
        dataStorage = null;
        tickableLookups.clear();
        controllers.clear();
    }

    @SuppressWarnings("unchecked")
    public static <FREQ extends Frequency> FrequencyController<FREQ> getController(FrequencyType<FREQ> frequencyType) {
        //Controllers are eagerly created for all registered types in serverLoad, but a pure remote client never calls
        // serverLoad, so lazily create one here instead of returning null. The lazily created controller falls back
        // to non-persisted, in-memory lookups below since dataStorage is null on the client.
        return (FrequencyController<FREQ>) controllers.computeIfAbsent(frequencyType, type -> FrequencyController.create(type));
    }

    protected static <FREQ extends Frequency> FrequencyLookup<FREQ> createLookup(FrequencyType<FREQ> frequencyType, @Nullable UUID uuid, SecurityMode securityMode, Codec<FrequencyLookup<FREQ>> codec) {
        FrequencyLookup<FREQ> lookup;
        if (dataStorage == null) {
            //Client-side (or a controller created lazily by getController() before serverLoad ran): keep the lookup in memory only
            lookup = new FrequencyLookup<>(frequencyType, uuid, securityMode);
        } else {
            SavedDataType<FrequencyLookup<FREQ>> dataType = new SavedDataType<>(
                  FrequencyLookup.getId(uuid, securityMode, frequencyType),
                  () -> new FrequencyLookup<>(frequencyType, uuid, securityMode),
                codec
            );
            lookup = dataStorage.computeIfAbsent(dataType);
        }
        registerTickable(lookup);
        return lookup;
    }

    public static void registerTickable(FrequencyLookup<?> lookup) {
        if (lookup.getFrequencyType().needsTick()) {
            tickableLookups.add(lookup);
        }
    }

    public static void tick(boolean tickingNormally) {
        for (FrequencyLookup<?> lookup : tickableLookups) {
            lookup.tickSelf(tickingNormally);
        }
    }
}
