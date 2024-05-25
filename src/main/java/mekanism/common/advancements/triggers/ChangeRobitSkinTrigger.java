package mekanism.common.advancements.triggers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import mekanism.api.MekanismAPI;
import mekanism.api.SerializationConstants;
import mekanism.api.robit.RobitSkin;
import mekanism.common.advancements.MekanismCriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

public class ChangeRobitSkinTrigger extends SimpleCriterionTrigger<ChangeRobitSkinTrigger.TriggerInstance> {

    @NotNull
    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer player, ResourceKey<RobitSkin> skin) {
        this.trigger(player, instance -> instance.skin.isEmpty() || instance.skin.get() == skin);
    }

    public record TriggerInstance(Optional<ContextAwarePredicate> player, Optional<ResourceKey<RobitSkin>> skin) implements SimpleCriterionTrigger.SimpleInstance {

        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf(SerializationConstants.PLAYER).forGetter(TriggerInstance::player),
                    ResourceKey.codec(MekanismAPI.ROBIT_SKIN_REGISTRY_NAME).optionalFieldOf(SerializationConstants.SKIN).forGetter(TriggerInstance::skin)
              ).apply(instance, TriggerInstance::new)
        );

        public static Criterion<TriggerInstance> toAny() {
            return MekanismCriteriaTriggers.CHANGE_ROBIT_SKIN.createCriterion(new TriggerInstance(Optional.empty(), Optional.empty()));
        }

        public static Criterion<TriggerInstance> toSkin(ResourceKey<RobitSkin> skin) {
            return MekanismCriteriaTriggers.CHANGE_ROBIT_SKIN.createCriterion(new TriggerInstance(Optional.empty(), Optional.of(skin)));
        }
    }
}