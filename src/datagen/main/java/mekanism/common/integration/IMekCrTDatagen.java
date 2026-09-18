package mekanism.common.integration;

import java.util.concurrent.CompletableFuture;
import mekanism.api.MekanismAPI;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;

public interface IMekCrTDatagen {

    IMekCrTDatagen INSTANCE = MekanismAPI.getService(IMekCrTDatagen.class);

    DataProvider exampleProvider(PackOutput output, CompletableFuture<Provider> registries);
}