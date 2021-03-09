package mekanism.common.integration;

import javax.annotation.Nonnull;
import mekanism.common.Mekanism;
import mekanism.common.registration.impl.ItemRegistryObject;
import mekanism.common.registries.MekanismItems;
import mekanism.common.resource.PrimaryResource;
import mekanism.common.resource.ResourceType;
import moze_intel.projecte.api.data.CustomConversionProvider;
import net.minecraft.data.DataGenerator;
import net.minecraft.item.Item;

public class MekanismCustomConversions extends CustomConversionProvider {

    public MekanismCustomConversions(DataGenerator generator) {
        super(generator);
    }

    @Override
    protected void addCustomConversions() {
        //A lot of these values are based on ProjectE's 1.12 values
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

    private static ItemRegistryObject<Item> ingot(PrimaryResource resource) {
        return MekanismItems.PROCESSED_RESOURCES.get(ResourceType.INGOT, resource);
    }
}