package mekanism.generators.common;

import mekanism.api.chemical.ChemicalTags;
import mekanism.api.chemical.gas.Gas;
import mekanism.common.Mekanism;
import net.minecraft.fluid.Fluid;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ITag.INamedTag;

public class GeneratorTags {

    private GeneratorTags() {
    }

    public static class Fluids {

        private Fluids() {
        }

        public static final INamedTag<Fluid> BIOETHANOL = forgeTag("bioethanol");
        public static final INamedTag<Fluid> DEUTERIUM = forgeTag("deuterium");
        public static final INamedTag<Fluid> FUSION_FUEL = forgeTag("fusion_fuel");
        public static final INamedTag<Fluid> TRITIUM = forgeTag("tritium");

        private static INamedTag<Fluid> forgeTag(String name) {
            return FluidTags.makeWrapperTag("forge:" + name);
        }
    }

    public static class Gases {

        private Gases() {
        }

        public static final INamedTag<Gas> DEUTERIUM = tag("deuterium");
        public static final INamedTag<Gas> TRITIUM = tag("tritium");
        public static final INamedTag<Gas> FUSION_FUEL = tag("fusion_fuel");

        private static INamedTag<Gas> tag(String name) {
            return ChemicalTags.GAS.tag(Mekanism.rl(name));
        }
    }
}