package mekanism.generators.common;

import net.minecraft.fluid.Fluid;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.Tag;
import net.minecraft.util.ResourceLocation;

public class GeneratorTags {

    public static class Fluids {

        public static final Tag<Fluid> BIOETHANOL = forgeTag("bioethanol");

        private static Tag<Fluid> forgeTag(String name) {
            return new FluidTags.Wrapper(new ResourceLocation("forge", name));
        }
    }
}