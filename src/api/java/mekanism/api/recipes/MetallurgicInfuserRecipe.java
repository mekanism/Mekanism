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
 * @apiNote Metallurgic Infusers and Infusing Factories can process this recipe type.
 */
@NothingNullByDefault
public abstract class MetallurgicInfuserRecipe extends ItemStackChemicalToItemStackRecipe {

    private static final Holder<Item> METALLURGIC_INFUSER = DeferredHolder.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(MekanismAPI.MEKANISM_MODID, "metallurgic_infuser"));

    @Override
    public final RecipeType<MetallurgicInfuserRecipe> getType() {
        return MekanismRecipeTypes.TYPE_METALLURGIC_INFUSING.value();
    }

    @Override
    public String getGroup() {
        return "metallurgic_infuser";
    }

    @Override
    public ItemStack getToastSymbol() {
        return new ItemStack(METALLURGIC_INFUSER);
    }
}
