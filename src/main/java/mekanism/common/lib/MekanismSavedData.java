package mekanism.common.lib;

import java.io.File;
import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.function.Supplier;
import mekanism.common.Mekanism;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.NotNull;

public abstract class MekanismSavedData extends SavedData {

    public abstract void load(@NotNull CompoundTag nbt, @NotNull HolderLookup.Provider provider);

    @Override
    public void save(@NotNull File file, @NotNull HolderLookup.Provider provider) {
        if (isDirty()) {
            //This is loosely based on Refined Storage's RSSavedData's system of saving first to a temp file
            // to reduce the odds of corruption if the user's computer crashes while the file is being written
            Path targetPath = file.toPath();
            Path tempPath = file.toPath().getParent().resolve(file.getName() + ".tmp");
            File tempFile = tempPath.toFile();
            super.save(tempFile, provider);
            //Based on Applied Energistics' AESavedData by starting to try with using an atomic move, and then only falling back to the replacing
            if (tempFile.exists()) {
                //Note: We check that the temp file exists, as if it doesn't that means we failed to write it and super will log the failure
                try {
                    try {
                        Files.move(tempPath, targetPath, StandardCopyOption.ATOMIC_MOVE);
                    } catch (AtomicMoveNotSupportedException ignored) {
                        Files.move(tempPath, targetPath, StandardCopyOption.REPLACE_EXISTING);
                    }
                } catch (IOException e) {
                    Mekanism.logger.error("Could not replace save data {} with new value", this, e);
                }
            }
        }
    }

    /**
     * Note: This should only be called from the server side
     */
    public static <DATA extends MekanismSavedData> DATA createSavedData(Supplier<DATA> createFunction, String name) {
        MinecraftServer currentServer = ServerLifecycleHooks.getCurrentServer();
        if (currentServer == null) {
            throw new IllegalStateException("Current server is null");
        }
        DimensionDataStorage dataStorage = currentServer.overworld().getDataStorage();
        return createSavedData(dataStorage, new Factory<>(createFunction, (tag, provider) -> {
            DATA handler = createFunction.get();
            handler.load(tag, provider);
            return handler;
        }), name);
    }

    /**
     * Note: This should only be called from the server side
     */
    public static <DATA extends MekanismSavedData> DATA createSavedData(DimensionDataStorage dataStorage, SavedData.Factory<DATA> factory, String name) {
        return dataStorage.computeIfAbsent(factory, Mekanism.MODID + "_" + name);
    }
}