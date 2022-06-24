package mekanism.common.item.predicate;

import com.google.gson.JsonObject;
import javax.annotation.Nonnull;
import mekanism.api.JsonConstants;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public abstract class CustomItemPredicate extends ItemPredicate {

    protected CustomItemPredicate() {
    }

    protected abstract ResourceLocation getID();

    @Override
    public abstract boolean matches(@Nonnull ItemStack stack);

    @Nonnull
    @Override
    public JsonObject serializeToJson() {
        JsonObject object = new JsonObject();
        object.addProperty(JsonConstants.TYPE, getID().toString());
        return object;
    }
}