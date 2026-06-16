package mekanism.common.integration.projecte;

import java.util.concurrent.CompletableFuture;
import mekanism.common.Mekanism;
import moze_intel.projecte.api.data.CustomConversionProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.PackOutput;

public class MekanismCustomConversions extends CustomConversionProvider {

    public MekanismCustomConversions(PackOutput output, CompletableFuture<Provider> lookupProvider) {
        super(output, lookupProvider, Mekanism.MODID);
    }

    @Override
    protected void addCustomConversions(HolderLookup.Provider registries) {
        //TODO - 26.2: Enable after ProjectE is updated
        /*createConversionBuilder(Mekanism.rl("defaults"))
              .comment("Default values for Mekanism items.")
              .before(MekanismItems.SALT, 8)
              .before(MekanismItems.FLUORITE_GEM, 576)
              //Give hdpe pellets a lowish emc value so that things like plastic have EMC values
              .before(MekanismItems.HDPE_PELLET, 32)
        ;*/
    }
}