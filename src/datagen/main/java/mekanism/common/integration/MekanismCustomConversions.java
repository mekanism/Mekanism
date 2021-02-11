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
import net.minecraftforge.common.Tags;

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
              //TODO - 10.1: Remove the below conversions and the like, as by the time 10.1 is released the
              // next version of projecte that includes these by default should have been released. In the
              // meantime this can exist here and in ProjectE with the tag versions of things, as there is
              // no real harm in them being defined twice
              .before(ingot(PrimaryResource.URANIUM), 4_096)
              .conversion(ingot(PrimaryResource.OSMIUM))
              .ingredient(Tags.Items.INGOTS_IRON, 2)
              .end()
              .conversion(ingot(PrimaryResource.COPPER), 2)
              .ingredient(Tags.Items.INGOTS_IRON)
              .end()
              .conversion(ingot(PrimaryResource.TIN))
              .ingredient(Tags.Items.INGOTS_IRON)
              .end()
              .conversion(ingot(PrimaryResource.LEAD), 2)
              .ingredient(Tags.Items.INGOTS_IRON)
              .end()
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