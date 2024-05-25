package mekanism.common.advancements.triggers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import mekanism.api.SerializationConstants;
import mekanism.common.advancements.MekanismCriteriaTriggers;
import mekanism.common.registries.MekanismDamageTypes;
import mekanism.common.registries.MekanismDamageTypes.MekanismDamageType;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;
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
                return instance.damageType.key() == damageType.key();
            }
            return false;
        });
    }

    public record TriggerInstance(Optional<ContextAwarePredicate> player, MekanismDamageType damageType,
                                  boolean killed) implements SimpleCriterionTrigger.SimpleInstance {

        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf(SerializationConstants.PLAYER).forGetter(TriggerInstance::player),
                    MekanismDamageTypes.CODEC.fieldOf(SerializationConstants.DAMAGE).forGetter(TriggerInstance::damageType),
                    Codec.BOOL.fieldOf(SerializationConstants.KILLED).forGetter(TriggerInstance::killed)
              ).apply(instance, TriggerInstance::new)
        );

        public static Criterion<TriggerInstance> damaged(MekanismDamageType damageType) {
            return MekanismCriteriaTriggers.DAMAGE.createCriterion(new TriggerInstance(Optional.empty(), damageType, false));
        }

        public static Criterion<TriggerInstance> killed(MekanismDamageType damageType) {
            return MekanismCriteriaTriggers.DAMAGE.createCriterion(new TriggerInstance(Optional.empty(), damageType, true));
        }
    }
}