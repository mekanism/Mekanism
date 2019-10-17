package mekanism.generators.common;

import net.minecraft.fluid.Fluid;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.Tag;
import net.minecraft.util.ResourceLocation;

public class GeneratorTags {

    public static final Tag<Fluid> BIO_ETHANOL = new FluidTags.Wrapper(new ResourceLocation("forge", "bio_ethanol"));
}