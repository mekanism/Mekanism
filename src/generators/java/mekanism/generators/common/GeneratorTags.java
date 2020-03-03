package mekanism.generators.common;

import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasTags;
import mekanism.common.Mekanism;
import net.minecraft.fluid.Fluid;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.Tag;
import net.minecraft.util.ResourceLocation;

public class GeneratorTags {

    public static class Fluids {

        public static final Tag<Fluid> BIOETHANOL = forgeTag("bioethanol");
        public static final Tag<Fluid> DEUTERIUM = forgeTag("deuterium");
        public static final Tag<Fluid> FUSION_FUEL = forgeTag("fusion_fuel");
        public static final Tag<Fluid> TRITIUM = forgeTag("tritium");

        private static Tag<Fluid> forgeTag(String name) {
            return new FluidTags.Wrapper(new ResourceLocation("forge", name));
        }
    }

    public static class Gases {

        public static final Tag<Gas> DEUTERIUM = tag("deuterium");
        public static final Tag<Gas> TRITIUM = tag("tritium");
        public static final Tag<Gas> FUSION_FUEL = tag("fusion_fuel");

        private static Tag<Gas> tag(String name) {
            return new GasTags.Wrapper(Mekanism.rl(name));
        }
    }
}