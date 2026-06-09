package mekanism.common.advancements.triggers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import mekanism.api.SerializationConstants;
import mekanism.common.advancements.MekanismCriteriaTriggers;
import mekanism.common.advancements.triggers.ConfigurationCardTrigger.TriggerInstance;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.criterion.ContextAwarePredicate;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.advancements.criterion.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;

public class ConfigurationCardTrigger extends SimpleCriterionTrigger<TriggerInstance> {

    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer player, boolean copy) {
        this.trigger(player, instance -> instance.copy == copy);
    }

    public record TriggerInstance(Optional<ContextAwarePredicate> player, boolean copy) implements SimpleCriterionTrigger.SimpleInstance {

        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf(SerializationConstants.PLAYER).forGetter(TriggerInstance::player),
                    Codec.BOOL.fieldOf(SerializationConstants.COPY).forGetter(TriggerInstance::copy)
              ).apply(instance, TriggerInstance::new)
        );

        public static Criterion<TriggerInstance> copyTrigger() {
            return MekanismCriteriaTriggers.CONFIGURATION_CARD.createCriterion(new TriggerInstance(Optional.empty(), true));
        }

        public static Criterion<TriggerInstance> pasteTrigger() {
            return MekanismCriteriaTriggers.CONFIGURATION_CARD.createCriterion(new TriggerInstance(Optional.empty(), false));
        }
    }
}