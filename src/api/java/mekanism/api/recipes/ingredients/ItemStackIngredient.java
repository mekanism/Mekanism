package mekanism.api.recipes.ingredients;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import mekanism.api.IMekanismAccess;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Base implementation for how Mekanism handle's ItemStack Ingredients.
 * <p>
 * Create instances of this using {@link mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess#item()}.
 */
public abstract class ItemStackIngredient implements InputIngredient<@NotNull ItemStack> {
    @Override
    public JsonElement serialize() {
        return IMekanismAccess.INSTANCE.itemStackIngredientCreator().codec().encodeStart(JsonOps.INSTANCE, this).getOrThrow(false, e->{});
    }
}