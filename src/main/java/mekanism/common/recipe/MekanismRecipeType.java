package mekanism.common.recipe;

import java.util.ArrayList;
import java.util.List;
import mekanism.api.recipes.IMekanismRecipe;
import mekanism.common.Mekanism;
import mekanism.common.recipe.impl.ChemicalCrystallizerIRecipe;
import mekanism.common.recipe.impl.ChemicalInfuserIRecipe;
import mekanism.common.recipe.impl.CombinerIRecipe;
import mekanism.common.recipe.impl.ElectrolysisIRecipe;
import mekanism.common.recipe.impl.FluidGasToGasIRecipe;
import mekanism.common.recipe.impl.FluidToFluidIRecipe;
import mekanism.common.recipe.impl.GasToGasIRecipe;
import mekanism.common.recipe.impl.ItemStackGasToGasIRecipe;
import mekanism.common.recipe.impl.ItemStackGasToItemStackIRecipe;
import mekanism.common.recipe.impl.ItemStackToGasIRecipe;
import mekanism.common.recipe.impl.ItemStackToItemStackIRecipe;
import mekanism.common.recipe.impl.MetallurgicInfuserIRecipe;
import mekanism.common.recipe.impl.PressurizedReactionIRecipe;
import mekanism.common.recipe.impl.SawmillIRecipe;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.registries.IForgeRegistry;

//TODO: Decide on these names, should they match what RecipeHandler used?
//TODO: Extend ForgeRegistryEntry ??
public class MekanismRecipeType<T extends IRecipe<?> & IMekanismRecipe> implements IRecipeType<T> {

    private static final List<MekanismRecipeType<? extends IRecipe<?>>> types = new ArrayList<>();

    public static final IRecipeType<ItemStackToItemStackIRecipe> CRUSHER = create("crusher");
    public static final IRecipeType<ItemStackToItemStackIRecipe> ENRICHMENT_CHAMBER = create("enrichment_chamber");
    public static final IRecipeType<ItemStackToItemStackIRecipe> ENERGIZED_SMELTER = create("energized_smelter");

    public static final IRecipeType<ChemicalCrystallizerIRecipe> CHEMICAL_CRYSTALLIZER = create("chemical_crystallizer");

    public static final IRecipeType<ChemicalInfuserIRecipe> CHEMICAL_INFUSER = create("chemical_infuser");

    public static final IRecipeType<CombinerIRecipe> COMBINER = create("combiner");

    public static final IRecipeType<ElectrolysisIRecipe> ELECTROLYTIC_SEPARATOR = create("electrolytic_separator");

    public static final IRecipeType<FluidGasToGasIRecipe> CHEMICAL_WASHER = create("chemical_washer");

    public static final IRecipeType<FluidToFluidIRecipe> THERMAL_EVAPORATION_PLANT = create("thermal_evaporation_plant");

    public static final IRecipeType<GasToGasIRecipe> SOLAR_NEUTRON_ACTIVATOR = create("solar_neutron_activator");

    public static final IRecipeType<ItemStackGasToGasIRecipe> CHEMICAL_DISSOLUTION_CHAMBER = create("chemical_dissolution_chamber");

    public static final IRecipeType<ItemStackGasToItemStackIRecipe> OSMIUM_COMPRESSOR = create("osmium_compressor");
    public static final IRecipeType<ItemStackGasToItemStackIRecipe> PURIFICATION_CHAMBER = create("purification_chamber");
    public static final IRecipeType<ItemStackGasToItemStackIRecipe> CHEMICAL_INJECTION_CHAMBER = create("chemical_injection_chamber");

    public static final IRecipeType<ItemStackToGasIRecipe> CHEMICAL_OXIDIZER = create("chemical_oxidizer");

    public static final IRecipeType<MetallurgicInfuserIRecipe> METALLURGIC_INFUSER = create("metallurgic_infuser");

    public static final IRecipeType<PressurizedReactionIRecipe> PRESSURIZED_REACTION_CHAMBER = create("pressurized_reaction_chamber");

    public static final IRecipeType<SawmillIRecipe> PRECISION_SAWMILL = create("precision_sawmill");

    private static <T extends IRecipe<?> & IMekanismRecipe> MekanismRecipeType<T> create(String name) {
        MekanismRecipeType<T> type = new MekanismRecipeType<>(name);
        types.add(type);
        return type;
    }

    //TODO: Convert this to using the proper forge registry once we stop needing to directly use the vanilla registry as a work around
    public static void registerRecipeTypes(IForgeRegistry<IRecipeSerializer<?>> registry) {
        types.forEach(type -> Registry.register(Registry.RECIPE_TYPE, type.registryName, type));
        //TODO: Should the list be cleared afterwards as it isn't really needed anymore after registration
    }

    private final ResourceLocation registryName;
    private final String name;

    private MekanismRecipeType(String name) {
        this.name = name;
        this.registryName = new ResourceLocation(Mekanism.MODID, name);
    }

    @Override
    public String toString() {
        return name;
    }
}