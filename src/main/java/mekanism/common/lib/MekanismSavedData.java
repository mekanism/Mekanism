package mekanism.common.lib;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.level.storage.SavedDataStorage;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.NotNull;

public abstract class MekanismSavedData extends SavedData {

    public abstract void load(@NotNull CompoundTag nbt, @NotNull HolderLookup.Provider provider);

    /**
     * Note: This should only be called from the server side
     */
    public static <DATA extends SavedData> DATA createSavedData(SavedDataType<DATA> type) {
        MinecraftServer currentServer = ServerLifecycleHooks.getCurrentServer();
        if (currentServer == null) {
            throw new IllegalStateException("Current server is null");
        }
        SavedDataStorage dataStorage = currentServer.getDataStorage();
        return dataStorage.computeIfAbsent(type);
    }

    /**
     * Note: This should only be called from the server side
     */
    public static <DATA extends SavedData> DATA createSavedData(SavedDataStorage dataStorage, SavedDataType<DATA> type) {
        return dataStorage.computeIfAbsent(type);
    }
}