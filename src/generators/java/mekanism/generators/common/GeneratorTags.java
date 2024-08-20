package mekanism.generators.common;

import mekanism.api.MekanismAPI;
import mekanism.api.chemical.Chemical;
import mekanism.common.Mekanism;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;

public class GeneratorTags {

    private GeneratorTags() {
    }

    public static class Fluids {

        private Fluids() {
        }

        public static final TagKey<Fluid> BIOETHANOL = commonTag("bioethanol");
        public static final TagKey<Fluid> DEUTERIUM = commonTag("deuterium");
        public static final TagKey<Fluid> FUSION_FUEL = commonTag("fusion_fuel");
        public static final TagKey<Fluid> TRITIUM = commonTag("tritium");

        private static TagKey<Fluid> commonTag(String name) {
            return FluidTags.create(ResourceLocation.fromNamespaceAndPath("c", name));
        }
    }

    public static class Chemicals {

        private Chemicals() {
        }

        public static final TagKey<Chemical> DEUTERIUM = tag("deuterium");
        public static final TagKey<Chemical> TRITIUM = tag("tritium");
        public static final TagKey<Chemical> FUSION_FUEL = tag("fusion_fuel");

        private static TagKey<Chemical> tag(String name) {
            return TagKey.create(MekanismAPI.CHEMICAL_REGISTRY_NAME, Mekanism.rl(name));
        }
    }
}