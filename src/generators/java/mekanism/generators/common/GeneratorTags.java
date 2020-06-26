package mekanism.generators.common;

import mekanism.api.chemical.ChemicalTags;
import mekanism.api.chemical.gas.Gas;
import mekanism.common.Mekanism;
import net.minecraft.fluid.Fluid;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ITag.INamedTag;
import net.minecraft.util.ResourceLocation;

public class GeneratorTags {

    public static class Fluids {

        public static final INamedTag<Fluid> BIOETHANOL = forgeTag("bioethanol");
        public static final INamedTag<Fluid> DEUTERIUM = forgeTag("deuterium");
        public static final INamedTag<Fluid> FUSION_FUEL = forgeTag("fusion_fuel");
        public static final INamedTag<Fluid> TRITIUM = forgeTag("tritium");

        private static INamedTag<Fluid> forgeTag(String name) {
            return new FluidTags.Wrapper(new ResourceLocation("forge", name));
        }
    }

    public static class Gases {

        public static final INamedTag<Gas> DEUTERIUM = tag("deuterium");
        public static final INamedTag<Gas> TRITIUM = tag("tritium");
        public static final INamedTag<Gas> FUSION_FUEL = tag("fusion_fuel");

        private static INamedTag<Gas> tag(String name) {
            return ChemicalTags.gasTag(Mekanism.rl(name));
        }
    }
}