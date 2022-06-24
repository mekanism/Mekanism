package mekanism.common.integration.projecte;

import java.io.IOException;
import javax.annotation.Nonnull;
import mekanism.common.Mekanism;
import mekanism.common.registries.MekanismItems;
import moze_intel.projecte.api.data.CustomConversionProvider;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;

public class MekanismCustomConversions extends CustomConversionProvider {

    public MekanismCustomConversions(DataGenerator generator) {
        super(generator);
    }

    @Override
    protected void addCustomConversions() {
        createConversionBuilder(Mekanism.rl("defaults"))
              .comment("Default values for Mekanism items.")
              .before(MekanismItems.SALT, 8)
              .before(MekanismItems.FLUORITE_GEM, 576)
              //Give hdpe pellets a lowish emc value so that things like plastic have EMC values
              .before(MekanismItems.HDPE_PELLET, 32)
        ;
    }

    @Override
    public void run(@Nonnull CachedOutput cache) throws IOException {
        //TODO - 1.19: Remove this after ProjectE updates and accounts for the change in the path
    }

    @Nonnull
    @Override
    public String getName() {
        return super.getName() + ": " + Mekanism.MODID;
    }
}