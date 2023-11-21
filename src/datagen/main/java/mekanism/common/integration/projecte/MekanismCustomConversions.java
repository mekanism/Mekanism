//TODO - 1.20.2: Re-enable after ProjectE updates
/*package mekanism.common.integration.projecte;

import java.util.concurrent.CompletableFuture;
import mekanism.common.Mekanism;
import mekanism.common.registries.MekanismItems;
import moze_intel.projecte.api.data.CustomConversionProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.PackOutput;
import org.jetbrains.annotations.NotNull;

public class MekanismCustomConversions extends CustomConversionProvider {

    public MekanismCustomConversions(PackOutput output, CompletableFuture<Provider> lookupProvider) {
        super(output, lookupProvider);
    }

    @Override
    protected void addCustomConversions(HolderLookup.Provider registries) {
        createConversionBuilder(Mekanism.rl("defaults"))
              .comment("Default values for Mekanism items.")
              .before(MekanismItems.SALT, 8)
              .before(MekanismItems.FLUORITE_GEM, 576)
              //Give hdpe pellets a lowish emc value so that things like plastic have EMC values
              .before(MekanismItems.HDPE_PELLET, 32)
        ;
    }

    @NotNull
    @Override
    public String getName() {
        return super.getName() + ": " + Mekanism.MODID;
    }
}*/