package mekanism.api.recipes;

import java.util.List;
import mekanism.api.MekanismAPI;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.recipes.chemical.ChemicalChemicalToChemicalRecipe;
import mekanism.api.recipes.ingredients.PigmentStackIngredient;
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
public abstract class PigmentMixingRecipe extends ChemicalChemicalToChemicalRecipe<Pigment, PigmentStack, PigmentStackIngredient> {

    private static final Holder<Item> PIGMENT_MIXER = DeferredHolder.create(Registries.ITEM, new ResourceLocation(MekanismAPI.MEKANISM_MODID, "pigment_mixer"));

    @Override
    public abstract boolean test(PigmentStack input1, PigmentStack input2);

    @Override
    @Contract(value = "_, _ -> new", pure = true)
    public abstract PigmentStack getOutput(PigmentStack input1, PigmentStack input2);

    @Override
    public abstract PigmentStackIngredient getLeftInput();

    @Override
    public abstract PigmentStackIngredient getRightInput();

    @Override
    public abstract List<PigmentStack> getOutputDefinition();

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
