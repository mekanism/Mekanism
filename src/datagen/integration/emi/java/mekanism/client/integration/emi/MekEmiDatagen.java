package mekanism.client.integration.emi;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import mekanism.client.recipe_viewer.alias.IAliasMapping;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;

public class MekEmiDatagen implements IMekEmiDatagen {

    @Override
    public DataProvider aliasProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries, String modid, Supplier<IAliasMapping> mappings) {
        return new EmiAliasProvider(output, registries, modid, mappings);
    }
}