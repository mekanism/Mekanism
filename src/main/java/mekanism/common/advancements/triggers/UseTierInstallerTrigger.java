package mekanism.common.advancements.triggers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Locale;
import java.util.Optional;
import mekanism.api.SerializationConstants;
import mekanism.api.tier.BaseTier;
import mekanism.common.advancements.MekanismCriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class UseTierInstallerTrigger extends SimpleCriterionTrigger<UseTierInstallerTrigger.TriggerInstance> {

    @NotNull
    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer player, BaseTier tier) {
        this.trigger(player, instance -> instance.action == TierUsed.ANY || instance.action.baseTier == tier);
    }

    public enum TierUsed implements StringRepresentable {
        ANY(null),
        BASIC(BaseTier.BASIC),
        ADVANCED(BaseTier.ADVANCED),
        ELITE(BaseTier.ELITE),
        ULTIMATE(BaseTier.ULTIMATE);

        public static final Codec<TierUsed> CODEC = StringRepresentable.fromEnum(TierUsed::values);
        final @Nullable BaseTier baseTier;

        TierUsed(@Nullable BaseTier baseTier) {
            this.baseTier = baseTier;
        }

        @NotNull
        @Override
        public String getSerializedName() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    public record TriggerInstance(Optional<ContextAwarePredicate> player, TierUsed action) implements SimpleCriterionTrigger.SimpleInstance {

        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf(SerializationConstants.PLAYER).forGetter(TriggerInstance::player),
                    TierUsed.CODEC.fieldOf(SerializationConstants.ACTION).forGetter(TriggerInstance::action)
              ).apply(instance, TriggerInstance::new)
        );

        public static Criterion<TriggerInstance> any() {
            return MekanismCriteriaTriggers.USE_TIER_INSTALLER.createCriterion(new TriggerInstance(Optional.empty(), TierUsed.ANY));
        }
    }
}