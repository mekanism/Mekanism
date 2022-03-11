package mekanism.generators.common;

import mekanism.api.chemical.ChemicalTags;
import mekanism.api.chemical.gas.Gas;
import mekanism.common.Mekanism;
import mekanism.common.tags.LazyTagLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.registries.ForgeRegistries;

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

        public static final TagKey<Fluid> BIOETHANOL = forgeTag("bioethanol");
        public static final LazyTagLookup<Fluid> BIOETHANOL_LOOKUP = LazyTagLookup.create(ForgeRegistries.FLUIDS, BIOETHANOL);
        public static final TagKey<Fluid> DEUTERIUM = forgeTag("deuterium");
        public static final TagKey<Fluid> FUSION_FUEL = forgeTag("fusion_fuel");
        public static final TagKey<Fluid> TRITIUM = forgeTag("tritium");

        private static TagKey<Fluid> forgeTag(String name) {
            return FluidTags.create(new ResourceLocation("forge", name));
        }
    }

    public static class Gases {

        private static void init() {
        }

        private Gases() {
        }

        public static final TagKey<Gas> DEUTERIUM = tag("deuterium");
        public static final LazyTagLookup<Gas> DEUTERIUM_LOOKUP = LazyTagLookup.create(ChemicalTags.GAS, DEUTERIUM);
        public static final TagKey<Gas> TRITIUM = tag("tritium");
        public static final LazyTagLookup<Gas> TRITIUM_LOOKUP = LazyTagLookup.create(ChemicalTags.GAS, TRITIUM);
        public static final TagKey<Gas> FUSION_FUEL = tag("fusion_fuel");
        public static final LazyTagLookup<Gas> FUSION_FUEL_LOOKUP = LazyTagLookup.create(ChemicalTags.GAS, FUSION_FUEL);

        private static TagKey<Gas> tag(String name) {
            return ChemicalTags.GAS.tag(Mekanism.rl(name));
        }
    }
}