package mekanism.common.advancements.triggers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import mekanism.api.SerializationConstants;
import mekanism.common.advancements.MekanismCriteriaTriggers;
import mekanism.common.registration.impl.MekanismDamageType;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageType;
import org.jetbrains.annotations.NotNull;

public class MekanismDamageTrigger extends SimpleCriterionTrigger<MekanismDamageTrigger.TriggerInstance> {

    @NotNull
    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer player, MekanismDamageType damageType, boolean hardcoreTotem) {
        this.trigger(player, instance -> {
            //If it is just any damage regardless of killed or the player is dead (or is on hardcore and used up a totem of undying)
            if (!instance.killed || player.isDeadOrDying() || hardcoreTotem) {
                //And the damage source matches
                return instance.damageType == damageType.key();
            }
            return false;
        });
    }

    public record TriggerInstance(Optional<ContextAwarePredicate> player, ResourceKey<DamageType> damageType, boolean killed)
          implements SimpleCriterionTrigger.SimpleInstance {

        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf(SerializationConstants.PLAYER).forGetter(TriggerInstance::player),
                    ResourceKey.codec(Registries.DAMAGE_TYPE).fieldOf(SerializationConstants.DAMAGE).forGetter(TriggerInstance::damageType),
                    Codec.BOOL.fieldOf(SerializationConstants.KILLED).forGetter(TriggerInstance::killed)
              ).apply(instance, TriggerInstance::new)
        );

        public static Criterion<TriggerInstance> damaged(MekanismDamageType damageType) {
            return MekanismCriteriaTriggers.DAMAGE.createCriterion(new TriggerInstance(Optional.empty(), damageType.key(), false));
        }

        public static Criterion<TriggerInstance> killed(MekanismDamageType damageType) {
            return MekanismCriteriaTriggers.DAMAGE.createCriterion(new TriggerInstance(Optional.empty(), damageType.key(), true));
        }
    }
}