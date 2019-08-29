package mekanism.common.recipe;

import com.google.gson.JsonObject;
import mekanism.api.block.IBlockDisableable;
import mekanism.common.Mekanism;
import net.minecraft.block.Block;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.IConditionSerializer;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Used as a condition in mekanism _factories.json
 *
 * WARNING: Only one of these values could apply!
 */
public class MekanismRecipeEnabledCondition implements ICondition {

    private static final ResourceLocation NAME = new ResourceLocation(Mekanism.MODID, "recipe_enabled");
    private final ResourceLocation block;

    public MekanismRecipeEnabledCondition(String location) {
        this(new ResourceLocation(location));
    }

    public MekanismRecipeEnabledCondition(String namespace, String path) {
        this(new ResourceLocation(namespace, path));
    }

    public MekanismRecipeEnabledCondition(ResourceLocation block) {
        this.block = block;
    }

    @Override
    public ResourceLocation getID() {
        return NAME;
    }

    @Override
    public boolean test() {
        Block block = ForgeRegistries.BLOCKS.getValue(this.block);
        if (block instanceof IBlockDisableable) {
            return ((IBlockDisableable) block).isEnabled();
        }
        return true;
    }

    @Override
    public String toString() {
        //TODO: Should this have block in it somehow
        return "recipe_enabled(\"" + block + "\")";
    }

    public static class Serializer implements IConditionSerializer<MekanismRecipeEnabledCondition> {

        public static final Serializer INSTANCE = new Serializer();

        @Override
        public void write(JsonObject json, MekanismRecipeEnabledCondition value) {
            json.addProperty("block", value.block.toString());
        }

        @Override
        public MekanismRecipeEnabledCondition read(JsonObject json) {
            //TODO: Should it throw an illegal state if there is no block
            return new MekanismRecipeEnabledCondition(new ResourceLocation(JSONUtils.getString(json, "block")));
        }

        @Override
        public ResourceLocation getID() {
            return MekanismRecipeEnabledCondition.NAME;
        }
    }
}