package mekanism.common.advancements.triggers;

import com.google.gson.JsonObject;
import javax.annotation.Nonnull;
import mekanism.common.advancements.MekanismCriteriaTriggers;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class UnboxCardboardBoxTrigger extends SimpleCriterionTrigger<UnboxCardboardBoxTrigger.TriggerInstance> {

    private final ResourceLocation id;

    public UnboxCardboardBoxTrigger(ResourceLocation id) {
        this.id = id;
    }

    @Nonnull
    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Nonnull
    @Override
    protected TriggerInstance createInstance(@Nonnull JsonObject json, @Nonnull EntityPredicate.Composite playerPredicate, @Nonnull DeserializationContext context) {
        return new TriggerInstance(playerPredicate);
    }

    public void trigger(ServerPlayer player) {
        this.trigger(player, instance -> true);
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {

        public TriggerInstance(EntityPredicate.Composite playerPredicate) {
            super(MekanismCriteriaTriggers.UNBOX_CARDBOARD_BOX.getId(), playerPredicate);
        }

        public static UnboxCardboardBoxTrigger.TriggerInstance unbox() {
            return new UnboxCardboardBoxTrigger.TriggerInstance(EntityPredicate.Composite.ANY);
        }
    }
}