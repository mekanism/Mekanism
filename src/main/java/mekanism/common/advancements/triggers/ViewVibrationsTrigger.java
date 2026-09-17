package mekanism.common.advancements.triggers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import mekanism.api.SerializationConstants;
import mekanism.api.functions.ConstantPredicates;
import mekanism.common.advancements.MekanismCriteriaTriggers;
import mekanism.common.advancements.triggers.ViewVibrationsTrigger.TriggerInstance;
import net.minecraft.advancements.triggers.Criterion;
import net.minecraft.advancements.triggers.SimpleCriterionTrigger;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class ViewVibrationsTrigger extends SimpleCriterionTrigger<TriggerInstance> {

    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer player) {
        this.trigger(player, ConstantPredicates.alwaysTrue());
    }

    public record TriggerInstance(Optional<Holder<LootItemCondition>> player) implements SimpleCriterionTrigger.SimpleInstance {

        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
              LootItemCondition.CODEC.optionalFieldOf(SerializationConstants.PLAYER).forGetter(TriggerInstance::player)
        ).apply(instance, TriggerInstance::new));

        public static Criterion<TriggerInstance> view() {
            return MekanismCriteriaTriggers.VIEW_VIBRATIONS.createCriterion(new TriggerInstance(Optional.empty()));
        }
    }
}