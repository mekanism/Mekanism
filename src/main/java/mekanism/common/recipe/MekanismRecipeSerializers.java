package mekanism.common.recipe;

import java.util.ArrayList;
import java.util.List;
import mekanism.common.Mekanism;
import mekanism.common.recipe.impl.ChemicalCrystallizerIRecipe;
import mekanism.common.recipe.impl.ChemicalInfuserIRecipe;
import mekanism.common.recipe.impl.CombinerIRecipe;
import mekanism.common.recipe.impl.CompressingIRecipe;
import mekanism.common.recipe.impl.CrushingIRecipe;
import mekanism.common.recipe.impl.ElectrolysisIRecipe;
import mekanism.common.recipe.impl.EnrichingIRecipe;
import mekanism.common.recipe.impl.FluidGasToGasIRecipe;
import mekanism.common.recipe.impl.FluidToFluidIRecipe;
import mekanism.common.recipe.impl.GasToGasIRecipe;
import mekanism.common.recipe.impl.InjectingIRecipe;
import mekanism.common.recipe.impl.ItemStackGasToGasIRecipe;
import mekanism.common.recipe.impl.ItemStackGasToItemStackIRecipe;
import mekanism.common.recipe.impl.ItemStackToGasIRecipe;
import mekanism.common.recipe.impl.ItemStackToItemStackIRecipe;
import mekanism.common.recipe.impl.MetallurgicInfuserIRecipe;
import mekanism.common.recipe.impl.PressurizedReactionIRecipe;
import mekanism.common.recipe.impl.PurifyingIRecipe;
import mekanism.common.recipe.impl.SawmillIRecipe;
import mekanism.common.recipe.impl.SmeltingIRecipe;
import mekanism.common.recipe.serializer.ChemicalCrystallizerRecipeSerializer;
import mekanism.common.recipe.serializer.ChemicalInfuserRecipeSerializer;
import mekanism.common.recipe.serializer.CombinerRecipeSerializer;
import mekanism.common.recipe.serializer.ElectrolysisRecipeSerializer;
import mekanism.common.recipe.serializer.FluidGasToGasRecipeSerializer;
import mekanism.common.recipe.serializer.FluidToFluidRecipeSerializer;
import mekanism.common.recipe.serializer.GasToGasRecipeSerializer;
import mekanism.common.recipe.serializer.ItemStackGasToGasRecipeSerializer;
import mekanism.common.recipe.serializer.ItemStackGasToItemStackRecipeSerializer;
import mekanism.common.recipe.serializer.ItemStackToGasRecipeSerializer;
import mekanism.common.recipe.serializer.ItemStackToItemStackRecipeSerializer;
import mekanism.common.recipe.serializer.MetallurgicInfuserRecipeSerializer;
import mekanism.common.recipe.serializer.PressurizedReactionRecipeSerializer;
import mekanism.common.recipe.serializer.SawmillRecipeSerializer;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;

//TODO: Allow for registry overrides?
//TODO: Decide on these names, should they match what RecipeHandler used?
public class MekanismRecipeSerializers {

    private static final List<IRecipeSerializer<? extends IRecipe<?>>> types = new ArrayList<>();

    public static final IRecipeSerializer<ItemStackToItemStackIRecipe> CRUSHER = create("crusher",
          new ItemStackToItemStackRecipeSerializer<>(CrushingIRecipe::new));
    public static final IRecipeSerializer<ItemStackToItemStackIRecipe> ENRICHMENT_CHAMBER = create("enrichment_chamber",
          new ItemStackToItemStackRecipeSerializer<>(EnrichingIRecipe::new));
    public static final IRecipeSerializer<ItemStackToItemStackIRecipe> ENERGIZED_SMELTER = create("energized_smelter",
          new ItemStackToItemStackRecipeSerializer<>(SmeltingIRecipe::new));

    public static final IRecipeSerializer<ChemicalCrystallizerIRecipe> CHEMICAL_CRYSTALLIZER = create("chemical_crystallizer",
          new ChemicalCrystallizerRecipeSerializer<>(ChemicalCrystallizerIRecipe::new));

    public static final IRecipeSerializer<ChemicalInfuserIRecipe> CHEMICAL_INFUSER = create("chemical_infuser",
          new ChemicalInfuserRecipeSerializer<>(ChemicalInfuserIRecipe::new));

    public static final IRecipeSerializer<CombinerIRecipe> COMBINER = create("combiner",
          new CombinerRecipeSerializer<>(CombinerIRecipe::new));

    public static final IRecipeSerializer<ElectrolysisIRecipe> ELECTROLYTIC_SEPARATOR = create("electrolytic_separator",
          new ElectrolysisRecipeSerializer<>(ElectrolysisIRecipe::new));

    public static final IRecipeSerializer<FluidGasToGasIRecipe> CHEMICAL_WASHER = create("chemical_washer",
          new FluidGasToGasRecipeSerializer<>(FluidGasToGasIRecipe::new));

    public static final IRecipeSerializer<FluidToFluidIRecipe> THERMAL_EVAPORATION_PLANT = create("thermal_evaporation_plant",
          new FluidToFluidRecipeSerializer<>(FluidToFluidIRecipe::new));

    public static final IRecipeSerializer<GasToGasIRecipe> SOLAR_NEUTRON_ACTIVATOR = create("solar_neutron_activator",
          new GasToGasRecipeSerializer<>(GasToGasIRecipe::new));

    public static final IRecipeSerializer<ItemStackGasToGasIRecipe> CHEMICAL_DISSOLUTION_CHAMBER = create("chemical_dissolution_chamber",
          new ItemStackGasToGasRecipeSerializer<>(ItemStackGasToGasIRecipe::new));

    public static final IRecipeSerializer<ItemStackGasToItemStackIRecipe> OSMIUM_COMPRESSOR = create("osmium_compressor",
          new ItemStackGasToItemStackRecipeSerializer<>(CompressingIRecipe::new));
    public static final IRecipeSerializer<ItemStackGasToItemStackIRecipe> PURIFICATION_CHAMBER = create("purification_chamber",
          new ItemStackGasToItemStackRecipeSerializer<>(PurifyingIRecipe::new));
    public static final IRecipeSerializer<ItemStackGasToItemStackIRecipe> CHEMICAL_INJECTION_CHAMBER = create("chemical_injection_chamber",
          new ItemStackGasToItemStackRecipeSerializer<>(InjectingIRecipe::new));

    public static final IRecipeSerializer<ItemStackToGasIRecipe> CHEMICAL_OXIDIZER = create("chemical_oxidizer",
          new ItemStackToGasRecipeSerializer<>(ItemStackToGasIRecipe::new));

    public static final IRecipeSerializer<MetallurgicInfuserIRecipe> METALLURGIC_INFUSER = create("metallurgic_infuser",
          new MetallurgicInfuserRecipeSerializer<>(MetallurgicInfuserIRecipe::new));

    public static final IRecipeSerializer<PressurizedReactionIRecipe> PRESSURIZED_REACTION_CHAMBER = create("pressurized_reaction_chamber",
          new PressurizedReactionRecipeSerializer<>(PressurizedReactionIRecipe::new));

    public static final IRecipeSerializer<SawmillIRecipe> PRECISION_SAWMILL = create("precision_sawmill",
          new SawmillRecipeSerializer<>(SawmillIRecipe::new));

    private static <T extends IRecipe<?>> IRecipeSerializer<T> create(String name, IRecipeSerializer<T> builder) {
        builder.setRegistryName(new ResourceLocation(Mekanism.MODID, name));
        types.add(builder);
        return builder;
    }

    public static void registerRecipeSerializers(IForgeRegistry<IRecipeSerializer<?>> registry) {
        types.forEach(registry::register);
        //TODO: Should the list be cleared afterwards as it isn't really needed anymore after registration
    }
}