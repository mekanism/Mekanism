package mekanism.client.jei;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import mekanism.api.providers.IItemProvider;
import mekanism.api.recipes.ChemicalCrystallizerRecipe;
import mekanism.api.recipes.ChemicalDissolutionRecipe;
import mekanism.api.recipes.ChemicalInfuserRecipe;
import mekanism.api.recipes.CombinerRecipe;
import mekanism.api.recipes.ElectrolysisRecipe;
import mekanism.api.recipes.FluidSlurryToSlurryRecipe;
import mekanism.api.recipes.FluidToFluidRecipe;
import mekanism.api.recipes.GasToGasRecipe;
import mekanism.api.recipes.ItemStackGasToItemStackRecipe;
import mekanism.api.recipes.ItemStackToEnergyRecipe;
import mekanism.api.recipes.ItemStackToFluidRecipe;
import mekanism.api.recipes.ItemStackToGasRecipe;
import mekanism.api.recipes.ItemStackToInfuseTypeRecipe;
import mekanism.api.recipes.ItemStackToItemStackRecipe;
import mekanism.api.recipes.ItemStackToPigmentRecipe;
import mekanism.api.recipes.MetallurgicInfuserRecipe;
import mekanism.api.recipes.NucleosynthesizingRecipe;
import mekanism.api.recipes.PaintingRecipe;
import mekanism.api.recipes.PigmentMixingRecipe;
import mekanism.api.recipes.PressurizedReactionRecipe;
import mekanism.api.recipes.RotaryRecipe;
import mekanism.api.recipes.SawmillRecipe;
import mekanism.client.jei.recipe.BoilerJEIRecipe;
import mekanism.client.jei.recipe.SPSJEIRecipe;
import mekanism.common.Mekanism;
import mekanism.common.registries.MekanismBlocks;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraftforge.common.util.Lazy;
import org.jetbrains.annotations.Nullable;

//Note: Do not use any classes from JEI here as this is to allow us to safely keep JEI optional while referencing from our GUIs
//Note: the lazy uid is only lazy before any lookups by rl have been performed, and afterwards it is assumed everything is initialized
// and it is instantly resolved to provide access to a reverse lookup map
public record MekanismJEIRecipeType<RECIPE>(Lazy<ResourceLocation> lazyUid, Class<? extends RECIPE> recipeClass) {

    private static final Map<ResourceLocation, MekanismJEIRecipeType<?>> knownTypes = new HashMap<>();
    @Nullable
    private static Set<MekanismJEIRecipeType<?>> allKnownTypes = new HashSet<>();

    //This exists for use in ensuring optional JEI support
    public static final MekanismJEIRecipeType<CraftingRecipe> VANILLA_CRAFTING = new MekanismJEIRecipeType<>(new ResourceLocation("crafting"), CraftingRecipe.class);

    public static final MekanismJEIRecipeType<ItemStackToItemStackRecipe> CRUSHING = new MekanismJEIRecipeType<>(MekanismBlocks.CRUSHER, ItemStackToItemStackRecipe.class);
    public static final MekanismJEIRecipeType<ItemStackToItemStackRecipe> ENRICHING = new MekanismJEIRecipeType<>(MekanismBlocks.ENRICHMENT_CHAMBER, ItemStackToItemStackRecipe.class);
    public static final MekanismJEIRecipeType<ItemStackToItemStackRecipe> SMELTING = new MekanismJEIRecipeType<>(MekanismBlocks.ENERGIZED_SMELTER, ItemStackToItemStackRecipe.class);

    public static final MekanismJEIRecipeType<ChemicalInfuserRecipe> CHEMICAL_INFUSING = new MekanismJEIRecipeType<>(MekanismBlocks.CHEMICAL_INFUSER, ChemicalInfuserRecipe.class);

    public static final MekanismJEIRecipeType<CombinerRecipe> COMBINING = new MekanismJEIRecipeType<>(MekanismBlocks.COMBINER, CombinerRecipe.class);

    public static final MekanismJEIRecipeType<ElectrolysisRecipe> SEPARATING = new MekanismJEIRecipeType<>(MekanismBlocks.ELECTROLYTIC_SEPARATOR, ElectrolysisRecipe.class);

    public static final MekanismJEIRecipeType<FluidSlurryToSlurryRecipe> WASHING = new MekanismJEIRecipeType<>(MekanismBlocks.CHEMICAL_WASHER, FluidSlurryToSlurryRecipe.class);

    public static final MekanismJEIRecipeType<FluidToFluidRecipe> EVAPORATING = new MekanismJEIRecipeType<>(MekanismBlocks.THERMAL_EVAPORATION_CONTROLLER, FluidToFluidRecipe.class);

    public static final MekanismJEIRecipeType<GasToGasRecipe> ACTIVATING = new MekanismJEIRecipeType<>(MekanismBlocks.SOLAR_NEUTRON_ACTIVATOR, GasToGasRecipe.class);
    public static final MekanismJEIRecipeType<GasToGasRecipe> CENTRIFUGING = new MekanismJEIRecipeType<>(MekanismBlocks.ISOTOPIC_CENTRIFUGE, GasToGasRecipe.class);

    public static final MekanismJEIRecipeType<ChemicalCrystallizerRecipe> CRYSTALLIZING = new MekanismJEIRecipeType<>(MekanismBlocks.CHEMICAL_CRYSTALLIZER, ChemicalCrystallizerRecipe.class);

    public static final MekanismJEIRecipeType<ChemicalDissolutionRecipe> DISSOLUTION = new MekanismJEIRecipeType<>(MekanismBlocks.CHEMICAL_DISSOLUTION_CHAMBER, ChemicalDissolutionRecipe.class);

    public static final MekanismJEIRecipeType<ItemStackGasToItemStackRecipe> COMPRESSING = new MekanismJEIRecipeType<>(MekanismBlocks.OSMIUM_COMPRESSOR, ItemStackGasToItemStackRecipe.class);
    public static final MekanismJEIRecipeType<ItemStackGasToItemStackRecipe> PURIFYING = new MekanismJEIRecipeType<>(MekanismBlocks.PURIFICATION_CHAMBER, ItemStackGasToItemStackRecipe.class);
    public static final MekanismJEIRecipeType<ItemStackGasToItemStackRecipe> INJECTING = new MekanismJEIRecipeType<>(MekanismBlocks.CHEMICAL_INJECTION_CHAMBER, ItemStackGasToItemStackRecipe.class);

    public static final MekanismJEIRecipeType<NucleosynthesizingRecipe> NUCLEOSYNTHESIZING = new MekanismJEIRecipeType<>(MekanismBlocks.ANTIPROTONIC_NUCLEOSYNTHESIZER, NucleosynthesizingRecipe.class);

    public static final MekanismJEIRecipeType<ItemStackToEnergyRecipe> ENERGY_CONVERSION = new MekanismJEIRecipeType<>(Mekanism.rl("energy_conversion"), ItemStackToEnergyRecipe.class);

    public static final MekanismJEIRecipeType<ItemStackToGasRecipe> GAS_CONVERSION = new MekanismJEIRecipeType<>(Mekanism.rl("gas_conversion"), ItemStackToGasRecipe.class);
    public static final MekanismJEIRecipeType<ItemStackToGasRecipe> OXIDIZING = new MekanismJEIRecipeType<>(MekanismBlocks.CHEMICAL_OXIDIZER, ItemStackToGasRecipe.class);

    public static final MekanismJEIRecipeType<ItemStackToInfuseTypeRecipe> INFUSION_CONVERSION = new MekanismJEIRecipeType<>(Mekanism.rl("infusion_conversion"), ItemStackToInfuseTypeRecipe.class);

    public static final MekanismJEIRecipeType<ItemStackToPigmentRecipe> PIGMENT_EXTRACTING = new MekanismJEIRecipeType<>(MekanismBlocks.PIGMENT_EXTRACTOR, ItemStackToPigmentRecipe.class);

    public static final MekanismJEIRecipeType<PigmentMixingRecipe> PIGMENT_MIXING = new MekanismJEIRecipeType<>(MekanismBlocks.PIGMENT_MIXER, PigmentMixingRecipe.class);

    public static final MekanismJEIRecipeType<MetallurgicInfuserRecipe> METALLURGIC_INFUSING = new MekanismJEIRecipeType<>(MekanismBlocks.METALLURGIC_INFUSER, MetallurgicInfuserRecipe.class);

    public static final MekanismJEIRecipeType<PaintingRecipe> PAINTING = new MekanismJEIRecipeType<>(MekanismBlocks.PAINTING_MACHINE, PaintingRecipe.class);

    public static final MekanismJEIRecipeType<PressurizedReactionRecipe> REACTION = new MekanismJEIRecipeType<>(MekanismBlocks.PRESSURIZED_REACTION_CHAMBER, PressurizedReactionRecipe.class);

    public static final MekanismJEIRecipeType<RotaryRecipe> CONDENSENTRATING = new MekanismJEIRecipeType<>(Mekanism.rl("condensentrating"), RotaryRecipe.class);
    public static final MekanismJEIRecipeType<RotaryRecipe> DECONDENSENTRATING = new MekanismJEIRecipeType<>(Mekanism.rl("decondensentrating"), RotaryRecipe.class);

    public static final MekanismJEIRecipeType<SawmillRecipe> SAWING = new MekanismJEIRecipeType<>(MekanismBlocks.PRECISION_SAWMILL, SawmillRecipe.class);

    public static final MekanismJEIRecipeType<BoilerJEIRecipe> BOILER = new MekanismJEIRecipeType<>(MekanismBlocks.BOILER_CASING, BoilerJEIRecipe.class);
    public static final MekanismJEIRecipeType<SPSJEIRecipe> SPS = new MekanismJEIRecipeType<>(MekanismBlocks.SPS_CASING, SPSJEIRecipe.class);

    public static final MekanismJEIRecipeType<ItemStackToFluidRecipe> NUTRITIONAL_LIQUIFICATION = new MekanismJEIRecipeType<>(MekanismBlocks.NUTRITIONAL_LIQUIFIER, ItemStackToFluidRecipe.class);

    public MekanismJEIRecipeType(IItemProvider item, Class<? extends RECIPE> recipeClass) {
        this(Lazy.of(item::getRegistryName), recipeClass);
    }

    public MekanismJEIRecipeType(ResourceLocation uid, Class<? extends RECIPE> recipeClass) {
        this(Lazy.of(() -> uid), recipeClass);
    }

    public MekanismJEIRecipeType {
        if (allKnownTypes == null) {
            knownTypes.put(uid(), this);
        } else {
            allKnownTypes.add(this);
        }
    }

    public ResourceLocation uid() {
        return lazyUid.get();
    }

    public static MekanismJEIRecipeType<?> findType(ResourceLocation name) {
        if (allKnownTypes != null) {
            for (MekanismJEIRecipeType<?> type : allKnownTypes) {
                knownTypes.put(type.uid(), type);
            }
            allKnownTypes = null;
        }
        return knownTypes.computeIfAbsent(name, uid -> {
            throw new IllegalArgumentException("No matching recipe type found.");
        });
    }
}