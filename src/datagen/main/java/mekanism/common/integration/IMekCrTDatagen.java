package mekanism.common.integration;

import java.util.concurrent.CompletableFuture;
import mekanism.api.MekanismAPI;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.server.packs.resources.ResourceManager;

public interface IMekCrTDatagen {

    IMekCrTDatagen INSTANCE = MekanismAPI.getService(IMekCrTDatagen.class);

    DataProvider exampleProvider(PackOutput output, ResourceManager serverResources, CompletableFuture<HolderLookup.Provider> registries);
}