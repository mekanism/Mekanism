package mekanism.common.advancements.triggers;

import com.google.gson.JsonObject;
import java.util.Optional;
import mekanism.api.functions.ConstantPredicates;
import mekanism.common.advancements.MekanismCriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

public class BlockLaserTrigger extends SimpleCriterionTrigger<BlockLaserTrigger.TriggerInstance> {
    @NotNull
    @Override
    protected TriggerInstance createInstance(@NotNull JsonObject json, @NotNull Optional<ContextAwarePredicate> playerPredicate, @NotNull DeserializationContext context) {
        return new TriggerInstance(playerPredicate);
    }

    public void trigger(ServerPlayer player) {
        this.trigger(player, ConstantPredicates.alwaysTrue());
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {

        @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
        public TriggerInstance(Optional<ContextAwarePredicate> playerPredicate) {
            super(playerPredicate);
        }

        public static Criterion<TriggerInstance> block() {
            return MekanismCriteriaTriggers.BLOCK_LASER.createCriterion(new TriggerInstance(Optional.empty()));
        }
    }
}