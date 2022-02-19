package mekanism.common.integration.projecte;

import javax.annotation.Nonnull;
import mekanism.common.Mekanism;
import mekanism.common.registries.MekanismItems;
import moze_intel.projecte.api.data.CustomConversionProvider;
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

    @Nonnull
    @Override
    public String getName() {
        return super.getName() + ": " + Mekanism.MODID;
    }
}