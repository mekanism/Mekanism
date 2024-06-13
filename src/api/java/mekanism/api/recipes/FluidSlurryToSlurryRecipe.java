package mekanism.api.recipes;

import java.util.List;
import mekanism.api.MekanismAPI;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.api.recipes.chemical.FluidChemicalToChemicalRecipe;
import mekanism.api.recipes.ingredients.SlurryStackIngredient;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.jetbrains.annotations.Contract;

/**
 * Input: FluidStack
 * <br>
 * Input: Slurry
 * <br>
 * Output: SlurryStack
 *
 * @apiNote Chemical Washers can process this recipe type.
 */
@NothingNullByDefault
public abstract class FluidSlurryToSlurryRecipe extends FluidChemicalToChemicalRecipe<Slurry, SlurryStack, SlurryStackIngredient> {

    private static final Holder<Item> CHEMICAL_WASHER = DeferredHolder.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(MekanismAPI.MEKANISM_MODID, "chemical_washer"));

    @Override
    public abstract boolean test(FluidStack fluidStack, SlurryStack chemicalStack);

    @Override
    public abstract FluidStackIngredient getFluidInput();

    @Override
    public abstract SlurryStackIngredient getChemicalInput();

    @Override
    public abstract List<SlurryStack> getOutputDefinition();

    @Override
    @Contract(value = "_, _ -> new", pure = true)
    public abstract SlurryStack getOutput(FluidStack fluidStack, SlurryStack chemicalStack);

    @Override
    public final RecipeType<FluidSlurryToSlurryRecipe> getType() {
        return MekanismRecipeTypes.TYPE_WASHING.value();
    }

    @Override
    public String getGroup() {
        return "chemical_washer";
    }

    @Override
    public ItemStack getToastSymbol() {
        return new ItemStack(CHEMICAL_WASHER);
    }
}
