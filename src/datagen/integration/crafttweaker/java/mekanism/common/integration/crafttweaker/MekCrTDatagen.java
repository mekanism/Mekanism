package mekanism.common.integration.crafttweaker;

import java.util.concurrent.CompletableFuture;
import mekanism.common.integration.IMekCrTDatagen;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;

public class MekCrTDatagen implements IMekCrTDatagen {

    @Override
    public DataProvider exampleProvider(PackOutput output, CompletableFuture<Provider> reloadableLookupProvider) {
        return new MekanismCrTExampleProvider(output, reloadableLookupProvider);
    }
}