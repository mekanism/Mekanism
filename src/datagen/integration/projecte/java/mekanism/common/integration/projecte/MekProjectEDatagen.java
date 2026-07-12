package mekanism.common.integration.projecte;

import java.util.concurrent.CompletableFuture;
import mekanism.common.integration.IMekProjectEDatagen;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;

public class MekProjectEDatagen implements IMekProjectEDatagen {

    @Override
    public DataProvider customConversionProvider(PackOutput output, CompletableFuture<Provider> lookupProvider) {
        return new MekanismCustomConversions(output, lookupProvider);
    }
}