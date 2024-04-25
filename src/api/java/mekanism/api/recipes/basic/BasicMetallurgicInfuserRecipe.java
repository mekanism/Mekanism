package mekanism.api.recipes.basic;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.recipes.MekanismRecipeSerializers;
import mekanism.api.recipes.MetallurgicInfuserRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient.InfusionStackIngredient;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

@NothingNullByDefault
public class BasicMetallurgicInfuserRecipe extends MetallurgicInfuserRecipe implements IBasicItemStackOutput {

    protected final ItemStackIngredient itemInput;
    protected final InfusionStackIngredient infusionInput;
    protected final ItemStack output;

    /**
     * @param itemInput     Item input.
     * @param infusionInput Infusion input.
     * @param output        Output.
     */
    public BasicMetallurgicInfuserRecipe(ItemStackIngredient itemInput, InfusionStackIngredient infusionInput, ItemStack output) {
        this.itemInput = Objects.requireNonNull(itemInput, "Item input cannot be null.");
        this.infusionInput = Objects.requireNonNull(infusionInput, "Chemical input cannot be null.");
        Objects.requireNonNull(output, "Output cannot be null.");
        if (output.isEmpty()) {
            throw new IllegalArgumentException("Output cannot be empty.");
        }
        this.output = output.copy();
    }

    @Override
    public ItemStackIngredient getItemInput() {
        return itemInput;
    }

    @Override
    public InfusionStackIngredient getChemicalInput() {
        return infusionInput;
    }

    @Override
    @Contract(value = "_, _ -> new", pure = true)
    public ItemStack getOutput(ItemStack inputItem, InfusionStack inputChemical) {
        return output.copy();
    }

    @NotNull
    @Override
    public ItemStack getResultItem(@NotNull HolderLookup.Provider provider) {
        return output.copy();
    }

    @Override
    public boolean test(ItemStack itemStack, InfusionStack gasStack) {
        return itemInput.test(itemStack) && infusionInput.test(gasStack);
    }

    @Override
    public List<@NotNull ItemStack> getOutputDefinition() {
        return Collections.singletonList(output);
    }

    @Override
    public ItemStack getOutputRaw() {
        return output;
    }

    @Override
    public RecipeSerializer<BasicMetallurgicInfuserRecipe> getSerializer() {
        return MekanismRecipeSerializers.METALLURGIC_INFUSING.get();
    }
}