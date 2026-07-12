package mekanism.common.integration.crafttweaker;

import java.util.concurrent.CompletableFuture;
import mekanism.common.integration.IMekCrTDatagen;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.server.packs.resources.ResourceManager;

public class MekCrTDatagen implements IMekCrTDatagen {

    @Override
    public DataProvider exampleProvider(PackOutput output, ResourceManager serverResources, CompletableFuture<Provider> registries) {
        return new MekanismCrTExampleProvider(output, serverResources, registries);
    }
}