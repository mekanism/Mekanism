package mekanism.common.recipe;

import com.google.gson.JsonObject;
import java.util.function.BooleanSupplier;
import javax.annotation.Nonnull;
import mekanism.api.block.IBlockDisableable;
import net.minecraft.block.Block;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.IConditionSerializer;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Used as a condition in mekanism _factories.json
 *
 * WARNING: Only one of these values could apply!
 */
public class MekanismRecipeEnabledCondition implements IConditionSerializer {

    @Nonnull
    @Override
    public BooleanSupplier parse(@Nonnull JsonObject json) {
        if (JSONUtils.hasField(json, "block")) {
            Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(JSONUtils.getString(json, "block")));
            if (block instanceof IBlockDisableable) {
                return ((IBlockDisableable) block)::isEnabled;
            }
            //TODO: Should this also throw an illegal state exception?
            return () -> true;
        }
        throw new IllegalStateException("Config defined with recipe_enabled condition without a valid field defined! Valid values: \"block\"");
    }
}