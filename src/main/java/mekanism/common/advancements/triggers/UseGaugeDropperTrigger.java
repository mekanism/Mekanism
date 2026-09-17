package mekanism.common.advancements.triggers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Locale;
import java.util.Optional;
import mekanism.api.SerializationConstants;
import mekanism.common.advancements.MekanismCriteriaTriggers;
import mekanism.common.advancements.triggers.UseGaugeDropperTrigger.TriggerInstance;
import net.minecraft.advancements.triggers.Criterion;
import net.minecraft.advancements.triggers.SimpleCriterionTrigger;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class UseGaugeDropperTrigger extends SimpleCriterionTrigger<TriggerInstance> {

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

        @Override
        public String getSerializedName() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    public record TriggerInstance(Optional<Holder<LootItemCondition>> player, UseDropperAction action) implements SimpleCriterionTrigger.SimpleInstance {

        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
              LootItemCondition.CODEC.optionalFieldOf(SerializationConstants.PLAYER).forGetter(TriggerInstance::player),
              UseDropperAction.CODEC.fieldOf(SerializationConstants.ACTION).forGetter(TriggerInstance::action)
        ).apply(instance, TriggerInstance::new));

        public static Criterion<TriggerInstance> any() {
            return MekanismCriteriaTriggers.USE_GAUGE_DROPPER.createCriterion(new TriggerInstance(Optional.empty(), UseDropperAction.ANY));
        }
    }
}