package mekanism.api.recipes;

import mekanism.api.MekanismAPI;
import mekanism.api.annotations.NothingNullByDefault;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.registries.DeferredHolder;

/**
 * Input: Two chemicals. The order of them does not matter.
 * <br>
 * Output: ChemicalStack
 *
 * @apiNote Pigment Mixers can process this recipe type and the chemicals can be put in any order into the mixer.
 */
@NothingNullByDefault
public abstract class PigmentMixingRecipe extends ChemicalChemicalToChemicalRecipe {

    private static final Holder<Item> PIGMENT_MIXER = DeferredHolder.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(MekanismAPI.MEKANISM_MODID, "pigment_mixer"));

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
