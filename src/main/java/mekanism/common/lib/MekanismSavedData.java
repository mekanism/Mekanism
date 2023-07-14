package mekanism.common.lib;

import java.io.File;
import java.util.function.Supplier;
import mekanism.common.Mekanism;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.NotNull;

public abstract class MekanismSavedData extends SavedData {

    public abstract void load(@NotNull CompoundTag nbt);

    @Override
    public void save(@NotNull File file) {
        if (isDirty()) {
            //This is loosely based on Refined Storage's RSSavedData's system of saving first to a temp file
            // to reduce the odds of corruption if the user's computer crashes while the file is being written
            File tempFile = file.toPath().getParent().resolve(file.getName() + ".tmp").toFile();
            super.save(tempFile);
            if (file.exists() && !file.delete()) {
                Mekanism.logger.error("Failed to delete " + file.getName());
            }
            if (!tempFile.renameTo(file)) {
                Mekanism.logger.error("Failed to rename " + tempFile.getName());
            }
        }
    }

    /**
     * Note: This should only be called from the server side
     */
    public static <DATA extends MekanismSavedData> DATA createSavedData(Supplier<DATA> createFunction, String name) {
        DimensionDataStorage dataStorage = ServerLifecycleHooks.getCurrentServer().overworld().getDataStorage();
        return createSavedData(dataStorage, createFunction, name);
    }

    /**
     * Note: This should only be called from the server side
     */
    public static <DATA extends MekanismSavedData> DATA createSavedData(DimensionDataStorage dataStorage, Supplier<DATA> createFunction, String name) {
        return dataStorage.computeIfAbsent(tag -> {
            DATA handler = createFunction.get();
            handler.load(tag);
            return handler;
        }, createFunction, Mekanism.MODID + "_" + name);
    }
}