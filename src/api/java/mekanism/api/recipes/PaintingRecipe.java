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
 * Input: ItemStack
 * <br>
 * Input: Chemical
 * <br>
 * Output: ItemStack
 *
 * @apiNote Painting Machines can process this recipe type.
 */
@NothingNullByDefault
public abstract class PaintingRecipe extends ItemStackChemicalToItemStackRecipe {

    private static final Holder<Item> PAINTING_MACHINE = DeferredHolder.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(MekanismAPI.MEKANISM_MODID, "painting_machine"));

    @Override
    public final RecipeType<PaintingRecipe> getType() {
        return MekanismRecipeTypes.TYPE_PAINTING.value();
    }

    @Override
    public String getGroup() {
        return "painting_machine";
    }

    @Override
    public ItemStack getToastSymbol() {
        return new ItemStack(PAINTING_MACHINE);
    }
}
