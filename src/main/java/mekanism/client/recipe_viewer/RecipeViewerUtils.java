package mekanism.client.recipe_viewer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.ChemicalType;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.api.recipes.ItemStackToFluidRecipe;
import mekanism.api.recipes.chemical.ItemStackToChemicalRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient.SlurryStackIngredient;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.client.gui.element.bar.GuiBar.IBarInfoHandler;
import mekanism.client.gui.element.progress.IProgressInfoHandler;
import mekanism.common.Mekanism;
import mekanism.common.recipe.IMekanismRecipeTypeProvider;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.impl.NutritionalLiquifierIRecipe;
import mekanism.common.registries.MekanismFluids;
import mekanism.common.tags.MekanismTags;
import mekanism.common.tier.ChemicalTankTier;
import mekanism.common.util.ChemicalUtil;
import mekanism.common.util.RegistryUtils;
import net.minecraft.SharedConstants;
import net.minecraft.core.HolderSet.Named;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.TimeUtil;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jetbrains.annotations.Nullable;

public class RecipeViewerUtils {

    private RecipeViewerUtils() {
    }

    public static final IProgressInfoHandler CONSTANT_PROGRESS = () -> 1;
    public static final IBarInfoHandler FULL_BAR = () -> 1;

    public static IProgressInfoHandler progressHandler(int processTime) {
        int time = SharedConstants.MILLIS_PER_TICK * processTime;
        return () -> {
            double subTime = System.currentTimeMillis() % (long) time;
            return subTime / time;
        };
    }

    public static IBarInfoHandler barProgressHandler(int processTime) {
        int time = SharedConstants.MILLIS_PER_TICK * processTime;
        return () -> {
            double subTime = System.currentTimeMillis() % (long) time;
            return subTime / time;
        };
    }

    public static <T> T getCurrent(List<T> elements) {
        return elements.get(getIndex(elements));
    }

    public static int getIndex(List<?> elements) {
        return (int) (System.currentTimeMillis() / TimeUtil.MILLISECONDS_PER_SECOND % elements.size());
    }

    @SuppressWarnings("unchecked")
    public static <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> List<ItemStack> getStacksFor(
          ChemicalStackIngredient<CHEMICAL, STACK> ingredient, boolean displayConversions) {
        Set<CHEMICAL> chemicals = ingredient.getRepresentations().stream().map(ChemicalStack::getType).collect(Collectors.toSet());
        if (!displayConversions) {
            return getStacksFor(chemicals, null);
        }
        ChemicalType chemicalType = ChemicalType.getTypeFor(ingredient);
        return getStacksFor(chemicals, (IMekanismRecipeTypeProvider<? extends ItemStackToChemicalRecipe<CHEMICAL, ?>, ?>)  switch (chemicalType) {
            case GAS -> MekanismRecipeType.GAS_CONVERSION;
            case INFUSION -> MekanismRecipeType.INFUSION_CONVERSION;
            default -> null;
        });
    }

    private static <CHEMICAL extends Chemical<CHEMICAL>> List<ItemStack> getStacksFor(Set<CHEMICAL> supportedTypes,
          @Nullable IMekanismRecipeTypeProvider<? extends ItemStackToChemicalRecipe<CHEMICAL, ?>, ?> recipeType) {
        List<ItemStack> stacks = new ArrayList<>();
        //Always include the chemical tank of the type to portray that we accept items
        for (CHEMICAL type : supportedTypes) {
            stacks.add(ChemicalUtil.getFullChemicalTank(ChemicalTankTier.BASIC, type));
        }
        //See if there are any chemical to item mappings
        if (recipeType != null) {
            for (RecipeHolder<? extends ItemStackToChemicalRecipe<CHEMICAL, ?>> recipeHolder : recipeType.getRecipes(null)) {
                ItemStackToChemicalRecipe<CHEMICAL, ?> recipe = recipeHolder.value();
                if (recipe.getOutputDefinition().stream().anyMatch(output -> supportedTypes.contains(output.getType()))) {
                    stacks.addAll(recipe.getInput().getRepresentations());
                }
            }
        }
        return stacks;
    }

    public static Map<ResourceLocation, ItemStackToFluidRecipe> getLiquificationRecipes() {
        Map<ResourceLocation, ItemStackToFluidRecipe> liquification = new LinkedHashMap<>();
        for (Item item : BuiltInRegistries.ITEM) {
            if (item.isEdible()) {
                ItemStack stack = new ItemStack(item);
                //TODO: If any mods adds presets to the creative menu we may want to consider gathering all
                FoodProperties food = stack.getFoodProperties(null);
                //Only display consuming foods that provide healing as otherwise no paste will be made
                if (food != null && food.getNutrition() > 0) {
                    liquification.put(Mekanism.rl("generated_liquification/" + RegistryUtils.getName(stack.getItem()).toString().replace(':', '_')),
                          new NutritionalLiquifierIRecipe(IngredientCreatorAccess.item().from(stack), MekanismFluids.NUTRITIONAL_PASTE.getFluidStack(food.getNutrition() * 50)));
                }
            }
        }
        return liquification;
    }

    public static List<ItemStack> getDisplayItems(SlurryStackIngredient ingredient) {
        Set<Named<Item>> tags = new HashSet<>();
        for (SlurryStack slurryStack : ingredient.getRepresentations()) {
            Slurry slurry = slurryStack.getType();
            if (!slurry.is(MekanismTags.Slurries.DIRTY)) {
                TagKey<Item> oreTag = slurry.getOreTag();
                if (oreTag != null) {
                    BuiltInRegistries.ITEM.getTag(oreTag).ifPresent(tags::add);
                }
            }
        }
        if (tags.size() == 1) {
            //TODO: Eventually come up with a better way to do this to allow for if there outputs based on the input and multiple input types
            return tags.stream().findFirst().map(tag -> tag.stream().map(ItemStack::new).toList()).orElse(List.of());
        }
        return List.of();
    }
}