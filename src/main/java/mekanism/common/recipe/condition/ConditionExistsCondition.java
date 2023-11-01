package mekanism.common.recipe.condition;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import mekanism.common.Mekanism;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.neoforged.neoforge.common.crafting.CraftingHelper;
import net.neoforged.neoforge.common.crafting.conditions.ICondition;
import net.neoforged.neoforge.common.crafting.conditions.IConditionSerializer;
import org.jetbrains.annotations.Nullable;

public record ConditionExistsCondition(@Nullable ICondition condition) implements ICondition {

    private static final ResourceLocation NAME = Mekanism.rl("condition_exists");
    private static final ConditionExistsCondition DOES_NOT_EXIST = new ConditionExistsCondition(null);

    @Override
    public ResourceLocation getID() {
        return NAME;
    }

    @Override
    public boolean test(IContext context) {
        return condition != null && condition.test(context);
    }

    @Override
    public String toString() {
        return "condition_exists(" + condition + ")";
    }

    public static class Serializer implements IConditionSerializer<ConditionExistsCondition> {

        public static final Serializer INSTANCE = new Serializer();

        private Serializer() {
        }

        @Override
        public void write(JsonObject json, ConditionExistsCondition value) {
            if (value.condition != null) {
                json.add("condition", CraftingHelper.serialize(value.condition));
            }
        }

        @Override
        public ConditionExistsCondition read(JsonObject json) {
            JsonObject condition = GsonHelper.getAsJsonObject(json, "condition", null);
            if (condition == null) {
                return DOES_NOT_EXIST;
            }
            try {
                return new ConditionExistsCondition(CraftingHelper.getCondition(condition));
            } catch (JsonParseException e) {
                //If we can't deserialize the internal condition it probably is from a mod that doesn't exist
                return DOES_NOT_EXIST;
            }
        }

        @Override
        public ResourceLocation getID() {
            return ConditionExistsCondition.NAME;
        }
    }
}