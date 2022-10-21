package mekanism.api.recipes.chemical;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.BiPredicate;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.MekanismRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Base class for defining fluid chemical to chemical recipes.
 * <br>
 * Input: FluidStack
 * <br>
 * Input: Chemical
 * <br>
 * Output: ChemicalStack of the same chemical type as the input chemical
 *
 * @param <INGREDIENT> Input Ingredient type
 */
@NothingNullByDefault
public abstract class FluidChemicalToChemicalRecipe<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>,
      INGREDIENT extends ChemicalStackIngredient<CHEMICAL, STACK>> extends MekanismRecipe implements BiPredicate<@NotNull FluidStack, @NotNull STACK> {

    private final FluidStackIngredient fluidInput;
    private final INGREDIENT chemicalInput;
    protected final STACK output;

    /**
     * @param id            Recipe name.
     * @param fluidInput    Fluid input.
     * @param chemicalInput Chemical input.
     * @param output        Output.
     */
    public FluidChemicalToChemicalRecipe(ResourceLocation id, FluidStackIngredient fluidInput, INGREDIENT chemicalInput, STACK output) {
        super(id);
        this.fluidInput = Objects.requireNonNull(fluidInput, "Fluid input cannot be null.");
        this.chemicalInput = Objects.requireNonNull(chemicalInput, "Chemical input cannot be null.");
        Objects.requireNonNull(output, "Output cannot be null.");
        if (output.isEmpty()) {
            throw new IllegalArgumentException("Output cannot be empty.");
        }
        this.output = (STACK) output.copy();
    }

    @Override
    public boolean test(FluidStack fluidStack, STACK chemicalStack) {
        return fluidInput.test(fluidStack) && chemicalInput.test(chemicalStack);
    }

    /**
     * Gets the input fluid ingredient.
     */
    public FluidStackIngredient getFluidInput() {
        return fluidInput;
    }

    /**
     * Gets the input chemical ingredient.
     */
    public INGREDIENT getChemicalInput() {
        return chemicalInput;
    }

    /**
     * For JEI, gets the output representations to display.
     *
     * @return Representation of the output, <strong>MUST NOT</strong> be modified.
     */
    public List<STACK> getOutputDefinition() {
        return Collections.singletonList(output);
    }

    /**
     * Gets a new output based on the given inputs.
     *
     * @param fluidStack    Specific fluid input.
     * @param chemicalStack Specific chemical input.
     *
     * @return New output.
     *
     * @apiNote While Mekanism does not currently make use of the inputs, it is important to support it and pass the proper value in case any addons define input based
     * outputs where things like NBT may be different.
     * @implNote The passed in inputs should <strong>NOT</strong> be modified.
     */
    @Contract(value = "_, _ -> new", pure = true)
    public STACK getOutput(FluidStack fluidStack, STACK chemicalStack) {
        return (STACK) output.copy();
    }

    @Override
    public boolean isIncomplete() {
        return fluidInput.hasNoMatchingInstances() || chemicalInput.hasNoMatchingInstances();
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        fluidInput.write(buffer);
        chemicalInput.write(buffer);
        output.writeToPacket(buffer);
    }
}