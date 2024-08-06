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
 * Input: Two pigments. The order of them does not matter.
 * <br>
 * Output: PigmentStack
 *
 * @apiNote Pigment Mixers can process this recipe type and the pigments can be put in any order into the mixer.
 */
@NothingNullByDefault
public abstract class PigmentMixingRecipe extends ChemicalChemicalToChemicalRecipe {

    private static final Holder<Item> PIGMENT_MIXER = DeferredHolder.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(MekanismAPI.MEKANISM_MODID, "pigment_mixer"));

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
    public final RecipeType<PigmentMixingRecipe> getType() {
        return MekanismRecipeTypes.TYPE_PIGMENT_MIXING.value();
    }

    @Override
    public String getGroup() {
        return "pigment_mixer";
    }

    @Override
    public ItemStack getToastSymbol() {
        return new ItemStack(PIGMENT_MIXER);
    }
}
