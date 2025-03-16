package mekanism.common.integration.crafttweaker;

import com.blamejared.crafttweaker.api.fluid.CTFluidIngredient;
import com.blamejared.crafttweaker.api.fluid.IFluidStack;
import com.blamejared.crafttweaker.api.ingredient.IIngredientWithAmount;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.blamejared.crafttweaker.api.recipe.component.IDecomposedRecipe;
import com.blamejared.crafttweaker.api.recipe.component.IRecipeComponent;
import com.blamejared.crafttweaker.api.tag.CraftTweakerTagRegistry;
import com.blamejared.crafttweaker.api.tag.manager.type.KnownTagManager;
import com.blamejared.crafttweaker.api.tag.type.KnownTag;
import com.blamejared.crafttweaker.natives.ingredient.ExpandCTFluidIngredientNeoForge;
import com.blamejared.crafttweaker.natives.ingredient.ExpandIIngredientWithAmountNeoForge;
import com.blamejared.crafttweaker.natives.ingredient.ExpandSizedFluidIngredient;
import com.blamejared.crafttweaker.natives.ingredient.ExpandSizedIngredient;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Function;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.common.integration.crafttweaker.chemical.CrTChemicalStack;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;

public class CrTUtils {

    private static final Function<ChemicalStack, ICrTChemicalStack> CHEMICAL_CONVERTER = CrTChemicalStack::new;

    /**
     * Helper method to convert a {@link Chemical} holder to an {@link ICrTChemicalStack}.
     */
    public static ICrTChemicalStack fromChemical(Holder<Chemical> chemical, int size) {
        return new CrTChemicalStack(new ChemicalStack(chemical, size));
    }

    /**
     * Converts a CrT item ingredient to one of ours.
     */
    public static ItemStackIngredient fromCrT(IIngredientWithAmount ingredient) {
        return IngredientCreatorAccess.item().from(ExpandIIngredientWithAmountNeoForge.asSizedIngredient(ingredient));
    }

    /**
     * Converts one of our item ingredients to a CrT item ingredient.
     */
    public static IIngredientWithAmount toCrT(ItemStackIngredient ingredient) {
        return ExpandSizedIngredient.asIIngredientWithAmount(ingredient.ingredient());
    }

    /**
     * Converts a CrT fluid ingredient to one of ours.
     */
    public static FluidStackIngredient fromCrT(CTFluidIngredient ingredient) {
        return IngredientCreatorAccess.fluid().from(ExpandCTFluidIngredientNeoForge.asSizedFluidIngredient(ingredient));
    }

    /**
     * Converts one of our fluid ingredients to a CrT fluid ingredient.
     */
    public static CTFluidIngredient toCrT(FluidStackIngredient ingredient) {
        return ExpandSizedFluidIngredient.asCTFluidIngredient(ingredient.ingredient());
    }

    /**
     * Helper method to get a single output from a recipe component if it is present.
     *
     * @param recipe    Decomposed recipe
     * @param component Recipe component
     *
     * @throws IllegalArgumentException if component is present but result is not single.
     */
    public static <C> Optional<C> getSingleIfPresent(IDecomposedRecipe recipe, IRecipeComponent<C> component) {
        List<C> values = recipe.get(component);
        if (values == null) {
            return Optional.empty();
        }
        if (values.size() != 1) {
            final String message = String.format(Locale.ROOT,
                  "Expected a list with a single element for %s, but got %d-sized list: %s",
                  component.getCommandString(),
                  values.size(),
                  values
            );
            throw new IllegalArgumentException(message);
        }
        return Optional.of(values.getFirst());
    }

    /**
     * Helper method to get a pair based output from a recipe component if it is present.
     *
     * @param recipe    Decomposed recipe
     * @param component Recipe component
     *
     * @throws IllegalArgumentException if component is not present or doesn't have two elements.
     */
    public static <C> UnaryTypePair<C> getPair(IDecomposedRecipe recipe, IRecipeComponent<C> component) {
        List<C> list = recipe.getOrThrow(component);
        if (list.size() != 2) {
            final String message = String.format(Locale.ROOT,
                  "Expected a list with two elements element for %s, but got %d-sized list: %s",
                  component.getCommandString(),
                  list.size(),
                  list
            );
            throw new IllegalArgumentException(message);
        }
        return new UnaryTypePair<>(list.get(0), list.get(1));
    }

    /**
     * Helper method for describing the outputs of a recipe that may have multiple outputs.
     */
    public static String describeOutputs(List<ChemicalStack> outputs) {
        if (outputs.isEmpty()) {
            return "";
        }
        return describeOutputs(outputs, CHEMICAL_CONVERTER);
    }

    /**
     * Helper method for describing the outputs of a recipe that may have multiple outputs.
     */
    public static <TYPE> String describeOutputs(List<TYPE> outputs, Function<TYPE, ?> converter) {
        int size = outputs.size();
        if (size == 0) {
            return "";
        } else if (size == 1) {
            return converter.apply(outputs.getFirst()).toString();
        }
        //Note: This isn't the best way to describe multiple outputs, but it is probably as close as we can get
        StringBuilder description = new StringBuilder();
        for (int i = 0; i < size; i++) {
            if (i > 0) {
                description.append(", or ");
            }
            description.append(converter.apply(outputs.get(i)));
        }
        return description.toString();
    }

    /**
     * Helper method for describing the outputs of a recipe that may have multiple outputs.
     */
    public static String describeOutputs(long[] outputs) {
        int size = outputs.length;
        if (size == 0) {
            return "";
        } else if (size == 1) {
            return Long.toString(outputs[0]);
        }
        //Note: This isn't the best way to describe multiple outputs, but it is probably as close as we can get
        StringBuilder description = new StringBuilder();
        for (int i = 0; i < size; i++) {
            if (i > 0) {
                description.append(", or ");
            }
            description.append(outputs[i]);
        }
        return description.toString();
    }

    /**
     * Helper to convert a CraftTweaker type tag to a regular tag and validate it exists
     */
    public static <TYPE> TagKey<TYPE> validateTagAndGet(KnownTag<TYPE> tag) {
        if (tag.exists()) {
            return tag.getTagKey();
        }
        throw new IllegalArgumentException("Tag " + tag.getCommandString() + " does not exist.");
    }

    /**
     * Helper to convert a list of one type to a list of another.
     */
    public static <TYPE, CRT_TYPE> List<CRT_TYPE> convert(List<TYPE> elements, Function<TYPE, CRT_TYPE> converter) {
        return elements.stream().map(converter).toList();
    }

    /**
     * Helper to convert a list of chemicals to a list of crafttweaker chemicals.
     */
    public static List<ICrTChemicalStack> convertChemical(List<ChemicalStack> elements) {
        if (elements.isEmpty()) {
            return Collections.emptyList();
        }
        return convert(elements, CHEMICAL_CONVERTER);
    }

    /**
     * Helper to convert a list of items to a list of crafttweaker items.
     */
    public static List<IItemStack> convertItems(List<ItemStack> elements) {
        return convert(elements, IItemStack::of);
    }

    /**
     * Helper to convert a list of items to a list of crafttweaker items.
     */
    public static List<IFluidStack> convertFluids(List<FluidStack> elements) {
        return convert(elements, IFluidStack::of);
    }

    /**
     * Helper to get CraftTweaker's item tag manager.
     */
    public static KnownTagManager<Item> itemTags() {
        return CraftTweakerTagRegistry.INSTANCE.knownTagManager(Registries.ITEM);
    }

    /**
     * Helper to get CraftTweaker's fluid tag manager.
     */
    public static KnownTagManager<Fluid> fluidTags() {
        return CraftTweakerTagRegistry.INSTANCE.knownTagManager(Registries.FLUID);
    }

    /**
     * Helper to get CraftTweaker's chemical tag manager.
     */
    public static KnownTagManager<Chemical> chemicalTags() {
        return CraftTweakerTagRegistry.INSTANCE.knownTagManager(MekanismAPI.CHEMICAL_REGISTRY_NAME);
    }

    public record UnaryTypePair<TYPE>(TYPE a, TYPE b) {
    }
}