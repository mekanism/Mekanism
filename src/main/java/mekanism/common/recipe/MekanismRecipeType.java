package mekanism.common.recipe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.inventory.IgnoredIInventory;
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
import mekanism.api.recipes.ItemStackToGasRecipe;
import mekanism.api.recipes.ItemStackToInfuseTypeRecipe;
import mekanism.api.recipes.ItemStackToItemStackRecipe;
import mekanism.api.recipes.MekanismRecipe;
import mekanism.api.recipes.MetallurgicInfuserRecipe;
import mekanism.api.recipes.NucleosynthesizingRecipe;
import mekanism.api.recipes.PressurizedReactionRecipe;
import mekanism.api.recipes.RotaryRecipe;
import mekanism.api.recipes.SawmillRecipe;
import mekanism.api.recipes.inputs.ItemStackIngredient;
import mekanism.client.MekanismClient;
import mekanism.common.Mekanism;
import mekanism.common.recipe.impl.SmeltingIRecipe;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import net.minecraftforge.registries.IForgeRegistry;

public class MekanismRecipeType<RECIPE_TYPE extends MekanismRecipe> implements IRecipeType<RECIPE_TYPE> {

    private static final List<MekanismRecipeType<? extends MekanismRecipe>> types = new ArrayList<>();

    public static final MekanismRecipeType<ItemStackToItemStackRecipe> CRUSHING = create("crushing");
    public static final MekanismRecipeType<ItemStackToItemStackRecipe> ENRICHING = create("enriching");
    public static final MekanismRecipeType<ItemStackToItemStackRecipe> SMELTING = create("smelting");

    public static final MekanismRecipeType<ChemicalInfuserRecipe> CHEMICAL_INFUSING = create("chemical_infusing");

    public static final MekanismRecipeType<CombinerRecipe> COMBINING = create("combining");

    public static final MekanismRecipeType<ElectrolysisRecipe> SEPARATING = create("separating");

    public static final MekanismRecipeType<FluidSlurryToSlurryRecipe> WASHING = create("washing");

    public static final MekanismRecipeType<FluidToFluidRecipe> EVAPORATING = create("evaporating");

    public static final MekanismRecipeType<GasToGasRecipe> ACTIVATING = create("activating");
    public static final MekanismRecipeType<GasToGasRecipe> CENTRIFUGING = create("centrifuging");

    public static final MekanismRecipeType<ChemicalCrystallizerRecipe> CRYSTALLIZING = create("crystallizing");

    public static final MekanismRecipeType<ChemicalDissolutionRecipe> DISSOLUTION = create("dissolution");

    public static final MekanismRecipeType<ItemStackGasToItemStackRecipe> COMPRESSING = create("compressing");
    public static final MekanismRecipeType<ItemStackGasToItemStackRecipe> PURIFYING = create("purifying");
    public static final MekanismRecipeType<ItemStackGasToItemStackRecipe> INJECTING = create("injecting");

    public static final MekanismRecipeType<NucleosynthesizingRecipe> NUCLEOSYNTHESIZING = create("nucleosynthesizing");

    public static final MekanismRecipeType<ItemStackToEnergyRecipe> ENERGY_CONVERSION = create("energy_conversion");

    public static final MekanismRecipeType<ItemStackToGasRecipe> GAS_CONVERSION = create("gas_conversion");
    public static final MekanismRecipeType<ItemStackToGasRecipe> OXIDIZING = create("oxidizing");

    public static final MekanismRecipeType<ItemStackToInfuseTypeRecipe> INFUSION_CONVERSION = create("infusion_conversion");

    public static final MekanismRecipeType<MetallurgicInfuserRecipe> METALLURGIC_INFUSING = create("metallurgic_infusing");

    public static final MekanismRecipeType<PressurizedReactionRecipe> REACTION = create("reaction");

    public static final MekanismRecipeType<RotaryRecipe> ROTARY = create("rotary");

    public static final MekanismRecipeType<SawmillRecipe> SAWING = create("sawing");

    private static <RECIPE_TYPE extends MekanismRecipe> MekanismRecipeType<RECIPE_TYPE> create(String name) {
        MekanismRecipeType<RECIPE_TYPE> type = new MekanismRecipeType<>(name);
        types.add(type);
        return type;
    }

    //TODO: Convert this to using the proper forge registry once we stop needing to directly use the vanilla registry as a work around
    public static void registerRecipeTypes(IForgeRegistry<IRecipeSerializer<?>> registry) {
        types.forEach(type -> Registry.register(Registry.RECIPE_TYPE, type.registryName, type));
    }

    public static void clearCache() {
        //TODO: Does this need to also get cleared on disconnect
        types.forEach(type -> type.cachedRecipes.clear());
    }

    private List<RECIPE_TYPE> cachedRecipes = Collections.emptyList();
    private final ResourceLocation registryName;

    private MekanismRecipeType(String name) {
        this.registryName = Mekanism.rl(name);
    }

    @Override
    public String toString() {
        return registryName.toString();
    }

    public ResourceLocation getRegistryName() {
        return registryName;
    }

    @Nonnull
    public List<RECIPE_TYPE> getRecipes(@Nullable World world) {
        if (world == null) {
            //Try to get a fallback world if we are in a context that may not have one
            //If we are on the client get the client's world, if we are on the server get the current server's world
            world = DistExecutor.safeRunForDist(() -> MekanismClient::tryGetClientWorld, () -> () -> ServerLifecycleHooks.getCurrentServer().func_241755_D_());
            if (world == null) {
                //If we failed, then return no recipes
                return Collections.emptyList();
            }
        }
        if (cachedRecipes.isEmpty()) {
            RecipeManager recipeManager = world.getRecipeManager();
            //TODO: Should we use the getRecipes(RecipeType) that we ATd so that our recipes don't have to always return true for matching?
            List<RECIPE_TYPE> recipes = recipeManager.getRecipes(this, IgnoredIInventory.INSTANCE, world);
            if (this == SMELTING) {
                Map<ResourceLocation, IRecipe<IInventory>> smeltingRecipes = recipeManager.getRecipes(IRecipeType.SMELTING);
                //Copy recipes our recipes to make sure it is mutable
                recipes = new ArrayList<>(recipes);
                for (Entry<ResourceLocation, IRecipe<IInventory>> entry : smeltingRecipes.entrySet()) {
                    IRecipe<IInventory> smeltingRecipe = entry.getValue();
                    //TODO: Allow for specifying not copying all smelting recipes, maybe do it by the resource location
                    ItemStack recipeOutput = smeltingRecipe.getRecipeOutput();
                    if (!smeltingRecipe.isDynamic() && !recipeOutput.isEmpty()) {
                        //TODO: Can Smelting recipes even "dynamic", if so can we add some sort of checker to make getOutput return the correct result
                        NonNullList<Ingredient> ingredients = smeltingRecipe.getIngredients();
                        ItemStackIngredient input;
                        if (ingredients.isEmpty()) {
                            //Something went wrong
                            continue;
                        } else if (ingredients.size() == 1) {
                            input = ItemStackIngredient.from(ingredients.get(0));
                        } else {
                            ItemStackIngredient[] itemIngredients = new ItemStackIngredient[ingredients.size()];
                            for (int i = 0; i < ingredients.size(); i++) {
                                itemIngredients[i] = ItemStackIngredient.from(ingredients.get(i));
                            }
                            input = ItemStackIngredient.createMulti(itemIngredients);
                        }
                        recipes.add((RECIPE_TYPE) new SmeltingIRecipe(entry.getKey(), input, recipeOutput));
                    }
                }
            }
            cachedRecipes = recipes;
        }
        return cachedRecipes;
    }

    public Stream<RECIPE_TYPE> stream(@Nullable World world) {
        return getRecipes(world).stream();
    }

    @Nullable
    public RECIPE_TYPE findFirst(@Nullable World world, Predicate<RECIPE_TYPE> matchCriteria) {
        return stream(world).filter(matchCriteria).findFirst().orElse(null);
    }

    public boolean contains(@Nullable World world, Predicate<RECIPE_TYPE> matchCriteria) {
        return stream(world).anyMatch(matchCriteria);
    }
}