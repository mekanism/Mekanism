package mekanism.client.recipe_viewer.jei;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import mekanism.api.recipes.MekanismRecipe;
import mekanism.api.recipes.RotaryRecipe;
import mekanism.client.recipe_viewer.type.IRecipeViewerRecipeType;
import mekanism.client.recipe_viewer.type.RecipeViewerRecipeType;
import mekanism.common.recipe.IMekanismRecipeTypeProvider;
import mekanism.common.recipe.MekanismRecipeType;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.recipe.vanilla.IVanillaRecipeFactory;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jspecify.annotations.Nullable;

public class RecipeRegistryHelper {

    private RecipeRegistryHelper() {
    }

    public static void registerCondensentrator(IRecipeRegistration registry) {
        List<RecipeHolder<RotaryRecipe>> condensentratorRecipes = new ArrayList<>();
        List<RecipeHolder<RotaryRecipe>> decondensentratorRecipes = new ArrayList<>();
        for (RecipeHolder<RotaryRecipe> recipeHolder : MekanismRecipeType.ROTARY.getRecipes()) {
            RotaryRecipe recipe = recipeHolder.value();
            if (recipe.hasChemicalToFluid()) {
                condensentratorRecipes.add(recipeHolder);
            }
            if (recipe.hasFluidToChemical()) {
                decondensentratorRecipes.add(recipeHolder);
            }
        }
        registry.addRecipes(MekanismJEI.holderRecipeType(RecipeViewerRecipeType.CONDENSENTRATING), condensentratorRecipes);
        registry.addRecipes(MekanismJEI.holderRecipeType(RecipeViewerRecipeType.DECONDENSENTRATING), decondensentratorRecipes);
    }

    public static <RECIPE extends MekanismRecipe<?>> void register(IRecipeRegistration registry, IRecipeViewerRecipeType<RECIPE> recipeType,
          IMekanismRecipeTypeProvider<?, RECIPE, ?> type) {
        registry.addRecipes(MekanismJEI.holderRecipeType(recipeType), type.getRecipes());
    }

    public static <RECIPE> void register(IRecipeRegistration registry, IRecipeViewerRecipeType<RECIPE> recipeType, Map<Identifier, RECIPE> recipes) {
        register(registry, recipeType, List.copyOf(recipes.values()));
    }

    public static <RECIPE> void register(IRecipeRegistration registry, IRecipeViewerRecipeType<RECIPE> recipeType, List<RECIPE> recipes) {
        registry.addRecipes(MekanismJEI.recipeType(recipeType), recipes);
    }

    public static void addAnvilRecipes(IRecipeRegistration registry, Holder<Item> item, Function<Item, @Nullable HolderSet<Item>> repairMaterials) {
        IVanillaRecipeFactory factory = registry.getVanillaRecipeFactory();
        //Based off of how JEI adds for Vanilla items
        ItemStack damaged2 = new ItemStack(item);
        damaged2.setDamageValue(damaged2.getMaxDamage() * 3 / 4);
        ItemStack damaged3 = new ItemStack(item);
        damaged3.setDamageValue(damaged3.getMaxDamage() * 2 / 4);
        //Two damaged items combine to undamaged
        Identifier itemId = Objects.requireNonNull(item.getKey(), "expected bound").identifier();
        registry.addRecipes(RecipeTypes.ANVIL, List.of(factory.createAnvilRecipe(damaged2, List.of(damaged2), List.of(damaged3), itemId.withSuffix("_two_damaged"))));
        HolderSet<Item> repairItems = repairMaterials.apply(item.value());
        //Damaged item + the repair material
        if (repairItems != null && repairItems.size() > 0) {
            //While this is damaged1 it is down here as we don't need to bother creating the reference if we don't have a repair material
            ItemStack damaged1 = new ItemStack(item);
            damaged1.setDamageValue(damaged1.getMaxDamage());
            List<ItemStack> repairStacks = repairItems.stream().map(holder -> holder.value().getDefaultInstance()).toList();
            registry.addRecipes(RecipeTypes.ANVIL, List.of(factory.createAnvilRecipe(damaged1, repairStacks, List.of(damaged2), itemId.withSuffix("_repair_material"))));
        }
    }
}