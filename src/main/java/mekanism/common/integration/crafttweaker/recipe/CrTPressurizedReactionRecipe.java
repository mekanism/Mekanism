package mekanism.common.integration.crafttweaker.recipe;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.fluid.CTFluidIngredient;
import com.blamejared.crafttweaker.api.ingredient.IIngredientWithAmount;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.blamejared.crafttweaker_annotations.annotations.NativeTypeRegistration;
import java.util.List;
import mekanism.api.recipes.PressurizedReactionRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.integration.crafttweaker.CrTUtils;
import mekanism.common.integration.crafttweaker.chemical.CrTChemicalStack;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@NativeTypeRegistration(value = PressurizedReactionRecipe.class, zenCodeName = CrTConstants.CLASS_RECIPE_REACTION)
public class CrTPressurizedReactionRecipe {

    private CrTPressurizedReactionRecipe() {
    }

    /**
     * Gets the item input ingredient.
     */
    @ZenCodeType.Method
    @ZenCodeType.Getter("inputSolid")
    public static IIngredientWithAmount getInputSolid(PressurizedReactionRecipe _this) {
        return CrTUtils.toCrT(_this.getInputSolid());
    }

    /**
     * Gets the fluid input ingredient.
     */
    @ZenCodeType.Method
    @ZenCodeType.Getter("inputFluid")
    public static CTFluidIngredient getInputFluid(PressurizedReactionRecipe _this) {
        return CrTUtils.toCrT(_this.getInputFluid());
    }

    /**
     * Gets the chemical input ingredient.
     */
    @ZenCodeType.Method
    @ZenCodeType.Getter("inputChemical")
    public static ChemicalStackIngredient getInputChemical(PressurizedReactionRecipe _this) {
        return _this.getInputChemical();
    }

    /**
     * Gets the amount of "extra" energy this recipe requires, compared to the base energy requirements of the machine performing the recipe.
     */
    @ZenCodeType.Method
    @ZenCodeType.Getter("energyRequired")
    public static long getEnergyRequired(PressurizedReactionRecipe _this) {
        return _this.getEnergyRequired();
    }

    /**
     * Gets the base duration in ticks that this recipe takes to complete.
     */
    @ZenCodeType.Method
    @ZenCodeType.Getter("duration")
    public static int getDuration(PressurizedReactionRecipe _this) {
        return _this.getDuration();
    }

    /**
     * Output representations, this list may or may not be complete and likely only contains one element, but has the possibility of containing multiple.
     */
    @ZenCodeType.Method
    @ZenCodeType.Getter("outputs")
    public static List<CrTPressurizedReactionRecipeOutput> getOutputs(PressurizedReactionRecipe _this) {
        return CrTUtils.convert(_this.getOutputDefinition(), output -> new CrTPressurizedReactionRecipeOutput(IItemStack.of(output.item()), new CrTChemicalStack(output.chemical())));
    }

    /**
     * At least one output type will be present.
     *
     * @param item     Item output, can be empty if chemical is empty but does not have to be.
     * @param chemical Chemical output, can be empty if item is empty but does not have to be.
     */
    @ZenRegister
    @ZenCodeType.Name(CrTConstants.CLASS_RECIPE_REACTION_OUTPUT)
    public record CrTPressurizedReactionRecipeOutput(@ZenCodeType.Getter("item") IItemStack item,
                                                     @ZenCodeType.Getter("chemical") ICrTChemicalStack chemical) {
    }
}