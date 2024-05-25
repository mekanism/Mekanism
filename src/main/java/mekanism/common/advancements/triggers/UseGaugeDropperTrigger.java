package mekanism.common.advancements.triggers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Locale;
import java.util.Optional;
import mekanism.api.SerializationConstants;
import mekanism.common.advancements.MekanismCriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public class UseGaugeDropperTrigger extends SimpleCriterionTrigger<UseGaugeDropperTrigger.TriggerInstance> {

    @NotNull
    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer player, UseDropperAction action) {
        this.trigger(player, instance -> instance.action == UseDropperAction.ANY || instance.action == action);
    }

    public enum UseDropperAction implements StringRepresentable {
        ANY,
        FILL,
        DRAIN,
        DUMP;

        public static final Codec<UseDropperAction> CODEC = StringRepresentable.fromEnum(UseDropperAction::values);

        @NotNull
        @Override
        public String getSerializedName() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    public record TriggerInstance(Optional<ContextAwarePredicate> player, UseDropperAction action) implements SimpleCriterionTrigger.SimpleInstance {

        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf(SerializationConstants.PLAYER).forGetter(TriggerInstance::player),
                    UseDropperAction.CODEC.fieldOf(SerializationConstants.ACTION).forGetter(TriggerInstance::action)
              ).apply(instance, TriggerInstance::new)
        );

        public static Criterion<TriggerInstance> any() {
            return MekanismCriteriaTriggers.USE_GAUGE_DROPPER.createCriterion(new TriggerInstance(Optional.empty(), UseDropperAction.ANY));
        }
    }
}