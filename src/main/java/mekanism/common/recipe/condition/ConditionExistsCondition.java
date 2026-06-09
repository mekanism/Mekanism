package mekanism.common.recipe.condition;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import mekanism.api.SerializationConstants;
import net.neoforged.neoforge.common.conditions.ICondition;
import org.jspecify.annotations.Nullable;

public record ConditionExistsCondition(@Nullable ICondition condition) implements ICondition {

    private static final ConditionExistsCondition DOES_NOT_EXIST = new ConditionExistsCondition(null);

    @Override
    public boolean test(IContext context) {
        return condition != null && condition.test(context);
    }

    @Override
    public String toString() {
        return "condition_exists(" + condition + ")";
    }

    @Override
    public MapCodec<? extends ICondition> codec() {
        return MekanismRecipeConditions.CONDITION_EXISTS.get();
    }

    public static MapCodec<ConditionExistsCondition> makeCodec() {
        return RecordCodecBuilder.mapCodec(instance -> instance.group(
              CODEC.lenientOptionalFieldOf(SerializationConstants.CONDITION).forGetter(condition -> Optional.ofNullable(condition.condition()))
        ).apply(instance, condition -> condition.map(ConditionExistsCondition::new).orElse(DOES_NOT_EXIST)));
    }
}