package mekanism.common.recipe;

import java.util.ArrayList;
import java.util.List;
import mekanism.api.recipes.ChemicalInfuserRecipe;
import mekanism.api.recipes.CombinerRecipe;
import mekanism.api.recipes.ElectrolysisRecipe;
import mekanism.api.recipes.FluidGasToGasRecipe;
import mekanism.api.recipes.FluidToFluidRecipe;
import mekanism.api.recipes.GasToGasRecipe;
import mekanism.api.recipes.GasToItemStackRecipe;
import mekanism.api.recipes.ItemStackGasToGasRecipe;
import mekanism.api.recipes.ItemStackGasToItemStackRecipe;
import mekanism.api.recipes.ItemStackToGasRecipe;
import mekanism.api.recipes.ItemStackToItemStackRecipe;
import mekanism.api.recipes.MekanismRecipe;
import mekanism.api.recipes.MetallurgicInfuserRecipe;
import mekanism.api.recipes.PressurizedReactionRecipe;
import mekanism.api.recipes.SawmillRecipe;
import mekanism.common.Mekanism;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.registries.IForgeRegistry;

//TODO: Should this be moved to API package?
//TODO: Decide on final names and put them in all the places that use them
//TODO: Instead of having RecipeWrapper, Move that stuff in here?
public class MekanismRecipeType<T extends MekanismRecipe> implements IRecipeType<T> {

    private static final List<MekanismRecipeType<? extends MekanismRecipe>> types = new ArrayList<>();

    public static final IRecipeType<ItemStackToItemStackRecipe> CRUSHING = create("crushing");
    public static final IRecipeType<ItemStackToItemStackRecipe> ENRICHING = create("enriching");
    public static final IRecipeType<ItemStackToItemStackRecipe> SMELTING = create("smelting");

    public static final IRecipeType<ChemicalInfuserRecipe> CHEMICAL_INFUSING = create("chemical_infusing");

    public static final IRecipeType<CombinerRecipe> COMBINING = create("combining");

    public static final IRecipeType<ElectrolysisRecipe> SEPARATING = create("separating");

    public static final IRecipeType<FluidGasToGasRecipe> WASHING = create("washing");

    public static final IRecipeType<FluidToFluidRecipe> EVAPORATING = create("evaporating");

    public static final IRecipeType<GasToGasRecipe> ACTIVATING = create("activating");

    public static final IRecipeType<GasToItemStackRecipe> CRYSTALLIZING = create("crystallizing");

    public static final IRecipeType<ItemStackGasToGasRecipe> DISSOLUTION = create("dissolution");

    public static final IRecipeType<ItemStackGasToItemStackRecipe> COMPRESSING = create("compressing");
    public static final IRecipeType<ItemStackGasToItemStackRecipe> PURIFYING = create("purifying");
    public static final IRecipeType<ItemStackGasToItemStackRecipe> INJECTING = create("injecting");

    public static final IRecipeType<ItemStackToGasRecipe> OXIDIZING = create("oxidizing");

    public static final IRecipeType<MetallurgicInfuserRecipe> METALLURGIC_INFUSING = create("metallurgic_infusing");

    public static final IRecipeType<PressurizedReactionRecipe> REACTION = create("reaction");

    public static final IRecipeType<SawmillRecipe> SAWING = create("sawing");

    private static <T extends MekanismRecipe> MekanismRecipeType<T> create(String name) {
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