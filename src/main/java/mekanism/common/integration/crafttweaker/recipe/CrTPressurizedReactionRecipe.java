package mekanism.common.integration.crafttweaker.recipe;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.blamejared.crafttweaker.api.item.MCItemStack;
import com.blamejared.crafttweaker_annotations.annotations.NativeTypeRegistration;
import java.util.List;
import mekanism.api.math.FloatingLong;
import mekanism.api.recipes.PressurizedReactionRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient.GasStackIngredient;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.integration.crafttweaker.CrTUtils;
import mekanism.common.integration.crafttweaker.chemical.CrTChemicalStack.CrTGasStack;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack.ICrTGasStack;
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
    public static ItemStackIngredient getInputSolid(PressurizedReactionRecipe _this) {
        return _this.getInputSolid();
    }

    /**
     * Gets the fluid input ingredient.
     */
    @ZenCodeType.Method
    @ZenCodeType.Getter("inputFluid")
    public static FluidStackIngredient getInputFluid(PressurizedReactionRecipe _this) {
        return _this.getInputFluid();
    }

    /**
     * Gets the gas input ingredient.
     */
    @ZenCodeType.Method
    @ZenCodeType.Getter("inputGas")
    public static GasStackIngredient getInputGas(PressurizedReactionRecipe _this) {
        return _this.getInputGas();
    }

    /**
     * Gets the amount of "extra" energy this recipe requires, compared to the base energy requirements of the machine performing the recipe.
     */
    @ZenCodeType.Method
    @ZenCodeType.Getter("energyRequired")
    public static FloatingLong getEnergyRequired(PressurizedReactionRecipe _this) {
        return _this.getEnergyRequired().copyAsConst();
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
        return CrTUtils.convert(_this.getOutputDefinition(), output -> new CrTPressurizedReactionRecipeOutput(new MCItemStack(output.item()), new CrTGasStack(output.gas())));
    }

    /**
     * At least one output type will be present.
     *
     * @param item Item output, can be empty if gas is empty but does not have to be.
     * @param gas  Gas output, can be empty if item is empty but does not have to be.
     */
    @ZenRegister
    @ZenCodeType.Name(CrTConstants.CLASS_RECIPE_REACTION_OUTPUT)
    public record CrTPressurizedReactionRecipeOutput(@ZenCodeType.Getter("item") IItemStack item,
                                                     @ZenCodeType.Getter("gas") ICrTGasStack gas) {
    }
}