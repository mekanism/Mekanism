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
 * @apiNote Chemical Infusers can process this recipe type and the chemicals can be put in any order into the infuser.
 */
@NothingNullByDefault
public abstract class ChemicalInfuserRecipe extends ChemicalChemicalToChemicalRecipe {

    private static final Holder<Item> CHEMICAL_INFUSER = DeferredHolder.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(MekanismAPI.MEKANISM_MODID, "chemical_infuser"));

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
