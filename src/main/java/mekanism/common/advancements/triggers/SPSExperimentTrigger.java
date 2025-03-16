package mekanism.common.advancements.triggers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import mekanism.api.SerializationConstants;
import mekanism.common.advancements.MekanismCriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.EntityTypePredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import org.jetbrains.annotations.NotNull;

public class SPSExperimentTrigger extends SimpleCriterionTrigger<SPSExperimentTrigger.TriggerInstance> {

    @NotNull
    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer player, EntityType<?> experimentedUpon) {
        this.trigger(player, instance -> instance.entityType.matches(experimentedUpon));
    }

    public record TriggerInstance(Optional<ContextAwarePredicate> player, EntityTypePredicate entityType) implements SimpleInstance {

        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf(SerializationConstants.PLAYER).forGetter(TriggerInstance::player),
                    EntityTypePredicate.CODEC.fieldOf(SerializationConstants.TYPE).forGetter(TriggerInstance::entityType)
              ).apply(instance, TriggerInstance::new)
        );

        public static Criterion<TriggerInstance> create(EntityType<?> entityType) {
            return MekanismCriteriaTriggers.SPS_EXPERIMENT.createCriterion(new TriggerInstance(Optional.empty(), EntityTypePredicate.of(entityType)));
        }

        public static Criterion<TriggerInstance> create(TagKey<EntityType<?>> entityType) {
            return MekanismCriteriaTriggers.SPS_EXPERIMENT.createCriterion(new TriggerInstance(Optional.empty(), EntityTypePredicate.of(entityType)));
        }
    }
}