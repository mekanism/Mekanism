package mekanism.generators.common;

import mekanism.api.chemical.ChemicalTags;
import mekanism.api.chemical.gas.Gas;
import mekanism.common.Mekanism;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.Tag.Named;
import net.minecraft.world.level.material.Fluid;

public class GeneratorTags {

    /**
     * Call to force make sure this is all initialized
     */
    public static void init() {
        Fluids.init();
        Gases.init();
    }

    private GeneratorTags() {
    }

    public static class Fluids {

        private static void init() {
        }

        private Fluids() {
        }

        public static final Named<Fluid> BIOETHANOL = forgeTag("bioethanol");
        public static final Named<Fluid> DEUTERIUM = forgeTag("deuterium");
        public static final Named<Fluid> FUSION_FUEL = forgeTag("fusion_fuel");
        public static final Named<Fluid> TRITIUM = forgeTag("tritium");

        private static Named<Fluid> forgeTag(String name) {
            return FluidTags.bind("forge:" + name);
        }
    }

    public static class Gases {

        private static void init() {
        }

        private Gases() {
        }

        public static final Named<Gas> DEUTERIUM = tag("deuterium");
        public static final Named<Gas> TRITIUM = tag("tritium");
        public static final Named<Gas> FUSION_FUEL = tag("fusion_fuel");

        private static Named<Gas> tag(String name) {
            return ChemicalTags.GAS.tag(Mekanism.rl(name));
        }
    }
}