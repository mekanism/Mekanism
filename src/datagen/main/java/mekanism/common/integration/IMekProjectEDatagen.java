package mekanism.common.integration;

import java.util.concurrent.CompletableFuture;
import mekanism.api.MekanismAPI;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;

public interface IMekProjectEDatagen {

    IMekProjectEDatagen INSTANCE = MekanismAPI.getService(IMekProjectEDatagen.class);

    DataProvider customConversionProvider(PackOutput output, CompletableFuture<Provider> lookupProvider);
}