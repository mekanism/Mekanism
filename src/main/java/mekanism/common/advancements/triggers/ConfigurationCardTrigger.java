package mekanism.common.advancements.triggers;

import com.google.gson.JsonObject;
import mekanism.api.JsonConstants;
import mekanism.common.advancements.MekanismCriteriaTriggers;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SerializationContext;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import org.jetbrains.annotations.NotNull;

public class ConfigurationCardTrigger extends SimpleCriterionTrigger<ConfigurationCardTrigger.TriggerInstance> {

    private final ResourceLocation id;

    public ConfigurationCardTrigger(ResourceLocation id) {
        this.id = id;
    }

    @NotNull
    @Override
    public ResourceLocation getId() {
        return id;
    }

    @NotNull
    @Override
    protected TriggerInstance createInstance(@NotNull JsonObject json, @NotNull EntityPredicate.Composite playerPredicate, @NotNull DeserializationContext context) {
        return new TriggerInstance(playerPredicate, GsonHelper.getAsBoolean(json, JsonConstants.COPY));
    }

    public void trigger(ServerPlayer player, boolean copy) {
        this.trigger(player, instance -> instance.copy == copy);
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {

        private final boolean copy;

        public TriggerInstance(EntityPredicate.Composite playerPredicate, boolean copy) {
            super(MekanismCriteriaTriggers.CONFIGURATION_CARD.getId(), playerPredicate);
            this.copy = copy;
        }

        @NotNull
        @Override
        public JsonObject serializeToJson(@NotNull SerializationContext context) {
            JsonObject json = super.serializeToJson(context);
            json.addProperty(JsonConstants.COPY, copy);
            return json;
        }

        public static ConfigurationCardTrigger.TriggerInstance copy() {
            return new ConfigurationCardTrigger.TriggerInstance(EntityPredicate.Composite.ANY, true);
        }

        public static ConfigurationCardTrigger.TriggerInstance paste() {
            return new ConfigurationCardTrigger.TriggerInstance(EntityPredicate.Composite.ANY, false);
        }
    }
}