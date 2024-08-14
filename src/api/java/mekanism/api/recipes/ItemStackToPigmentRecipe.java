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
 * Output: ChemicalStack
 *
 * @apiNote Pigment Extractors can process this recipe type.
 */
@NothingNullByDefault
public abstract class ItemStackToPigmentRecipe extends ItemStackToChemicalRecipe {

    private static final Holder<Item> PIGMENT_EXTRACTOR = DeferredHolder.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(MekanismAPI.MEKANISM_MODID, "pigment_extractor"));

    @Override
    public String getGroup() {
        return "pigment_extractor";
    }

    @Override
    public ItemStack getToastSymbol() {
        return new ItemStack(PIGMENT_EXTRACTOR);
    }

    @Override
    public RecipeType<?> getType() {
        return MekanismRecipeTypes.TYPE_PIGMENT_EXTRACTING.value();
    }
}
