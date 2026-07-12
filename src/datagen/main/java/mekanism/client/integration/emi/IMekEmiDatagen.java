package mekanism.client.integration.emi;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import mekanism.api.MekanismAPI;
import mekanism.client.recipe_viewer.alias.IAliasMapping;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;

public interface IMekEmiDatagen {

    IMekEmiDatagen INSTANCE = MekanismAPI.getService(IMekEmiDatagen.class);

    DataProvider aliasProvider(PackOutput output, CompletableFuture<Provider> registries, String modid, Supplier<IAliasMapping> mappings);
}