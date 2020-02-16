package mekanism.common.integration.projecte.mappers;

import java.util.HashMap;
import java.util.Map;
import mekanism.api.recipes.SawmillRecipe;
import mekanism.api.recipes.SawmillRecipe.ChanceOutput;
import mekanism.api.recipes.inputs.ItemStackIngredient;
import mekanism.common.recipe.MekanismRecipeType;
import moze_intel.projecte.api.mapper.collector.IMappingCollector;
import moze_intel.projecte.api.mapper.recipe.IRecipeTypeMapper;
import moze_intel.projecte.api.mapper.recipe.RecipeTypeMapper;
import moze_intel.projecte.api.nss.NSSItem;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeType;
import org.apache.commons.lang3.math.Fraction;

@RecipeTypeMapper
public class SawmillRecipeMapper implements IRecipeTypeMapper {

    @Override
    public String getName() {
        return "MekSawmill";
    }

    @Override
    public String getDescription() {
        return "Maps Mekanism sawmill recipes. (Disabled by default, due to causing various EMC values to be removed pertaining to charcoal/wood)";
    }

    @Override
    public boolean isAvailable() {
        return false;
    }

    @Override
    public boolean canHandle(IRecipeType<?> recipeType) {
        return recipeType == MekanismRecipeType.SAWING;
    }

    @Override
    public boolean handleRecipe(IMappingCollector<NormalizedSimpleStack, Long> mapper, IRecipe<?> iRecipe) {
        if (!(iRecipe instanceof SawmillRecipe)) {
            //Double check that we have a type of recipe we know how to handle
            return false;
        }
        SawmillRecipe recipe = (SawmillRecipe) iRecipe;
        ItemStackIngredient input = recipe.getInput();
        int primaryMultiplier = 1;
        int secondaryMultiplier = 1;
        if (recipe.getSecondaryChance() > 0 && recipe.getSecondaryChance() < 1) {
            Fraction multiplier;
            try {
                multiplier = Fraction.getFraction(recipe.getSecondaryChance()).invert();
            } catch (ArithmeticException e) {
                //If we couldn't turn it into a fraction, then note we failed to convert the recipe
                return false;
            }
            primaryMultiplier = multiplier.getNumerator();
            secondaryMultiplier = multiplier.getDenominator();
        }
        for (ItemStack representation : input.getRepresentations()) {
            ChanceOutput output = recipe.getOutput(representation);
            ItemStack mainOutput = output.getMainOutput();
            ItemStack secondaryOutput = output.getMaxSecondaryOutput();
            NormalizedSimpleStack nssInput = NSSItem.createItem(representation);
            Map<NormalizedSimpleStack, Integer> ingredientMap = new HashMap<>();
            if (secondaryOutput.isEmpty()) {
                //We only have a main output
                ingredientMap.put(nssInput, representation.getCount());
                mapper.addConversion(mainOutput.getCount(), NSSItem.createItem(mainOutput), ingredientMap);
            } else if (mainOutput.isEmpty()) {
                //We only have a secondary output
                ingredientMap.put(nssInput, representation.getCount() * primaryMultiplier);
                mapper.addConversion(secondaryOutput.getCount() * secondaryMultiplier, NSSItem.createItem(secondaryOutput), ingredientMap);
            } else {
                NormalizedSimpleStack nssMainOutput = NSSItem.createItem(mainOutput);
                NormalizedSimpleStack nssSecondaryOutput = NSSItem.createItem(secondaryOutput);
                //We have both so do our best guess by trying to subtract them from each other
                //Add trying to calculate the main output (using it as if we needed negative of secondary output)
                ingredientMap.put(nssInput, representation.getCount() * primaryMultiplier);
                ingredientMap.put(nssSecondaryOutput, -secondaryOutput.getCount() * secondaryMultiplier);
                mapper.addConversion(mainOutput.getCount() * primaryMultiplier, nssMainOutput, ingredientMap);
                //Add trying to calculate secondary output (using it as if we needed negative of main output)
                ingredientMap = new HashMap<>();
                ingredientMap.put(nssInput, representation.getCount() * primaryMultiplier);
                ingredientMap.put(nssMainOutput, -mainOutput.getCount() * primaryMultiplier);
                mapper.addConversion(secondaryOutput.getCount() * secondaryMultiplier, nssSecondaryOutput, ingredientMap);
            }
        }
        return true;
    }
}