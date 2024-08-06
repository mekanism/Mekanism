package mekanism.api.recipes;

import java.util.List;
import mekanism.api.MekanismAPI;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.chemical.ChemicalChemicalToChemicalRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.jetbrains.annotations.Contract;

/**
 * Input: Two gases. The order of them does not matter.
 * <br>
 * Output: GasStack
 *
 * @apiNote Chemical Infusers can process this recipe type and the gases can be put in any order into the infuser.
 */
@NothingNullByDefault
public abstract class ChemicalInfuserRecipe extends ChemicalChemicalToChemicalRecipe {

    private static final Holder<Item> CHEMICAL_INFUSER = DeferredHolder.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(MekanismAPI.MEKANISM_MODID, "chemical_infuser"));

    @Override
    public abstract boolean test(ChemicalStack input1, ChemicalStack input2);

    @Override
    @Contract(value = "_, _ -> new", pure = true)
    public abstract ChemicalStack getOutput(ChemicalStack input1, ChemicalStack input2);

    @Override
    public abstract ChemicalStackIngredient getLeftInput();

    @Override
    public abstract ChemicalStackIngredient getRightInput();

    @Override
    public abstract List<ChemicalStack> getOutputDefinition();

    @Override
    public final RecipeType<ChemicalInfuserRecipe> getType() {
        return MekanismRecipeTypes.TYPE_CHEMICAL_INFUSING.value();
    }

    @Override
    public String getGroup() {
        return "chemical_infuser";
    }

    @Override
    public ItemStack getToastSymbol() {
        return new ItemStack(CHEMICAL_INFUSER);
    }
}
