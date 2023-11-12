package mekanism.api.recipes;

import java.util.List;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.recipes.chemical.ItemStackChemicalToItemStackRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient.InfusionStackIngredient;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Input: ItemStack
 * <br>
 * Input: Infuse Type
 * <br>
 * Output: ItemStack
 *
 * @apiNote Metallurgic Infusers and Infusing Factories can process this recipe type.
 */
@NothingNullByDefault
public abstract class MetallurgicInfuserRecipe extends ItemStackChemicalToItemStackRecipe<InfuseType, InfusionStack, InfusionStackIngredient> {

    @Override
    public abstract ItemStackIngredient getItemInput();

    @Override
    public abstract InfusionStackIngredient getChemicalInput();

    @Override
    @Contract(value = "_, _ -> new", pure = true)
    public abstract ItemStack getOutput(ItemStack inputItem, InfusionStack inputChemical);

    @NotNull
    @Override
    public abstract ItemStack getResultItem(@NotNull RegistryAccess registryAccess);

    @Override
    public abstract boolean test(ItemStack itemStack, InfusionStack gasStack);

    @Override
    public abstract List<@NotNull ItemStack> getOutputDefinition();

    @Override
    public final RecipeType<MetallurgicInfuserRecipe> getType() {
        return MekanismRecipeTypes.TYPE_METALLURGIC_INFUSING.get();
    }

    @Override
    public String getGroup() {
        return "metallurgic_infuser";
    }
}
