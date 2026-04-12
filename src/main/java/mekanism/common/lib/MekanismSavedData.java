package mekanism.common.lib;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.level.storage.SavedDataStorage;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

public abstract class MekanismSavedData extends SavedData {

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