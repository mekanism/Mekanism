package mekanism.common.advancements.triggers;

import com.google.gson.JsonObject;
import java.util.Optional;
import mekanism.api.JsonConstants;
import mekanism.common.advancements.MekanismCriteriaTriggers;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import org.jetbrains.annotations.NotNull;

public class ConfigurationCardTrigger extends SimpleCriterionTrigger<ConfigurationCardTrigger.TriggerInstance> {
    @NotNull
    @Override
    protected TriggerInstance createInstance(@NotNull JsonObject json, @NotNull Optional<ContextAwarePredicate> playerPredicate, @NotNull DeserializationContext context) {
        return new TriggerInstance(playerPredicate, GsonHelper.getAsBoolean(json, JsonConstants.COPY));
    }

    public void trigger(ServerPlayer player, boolean copy) {
        this.trigger(player, instance -> instance.copy == copy);
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {

        private final boolean copy;

        protected TriggerInstance(Optional<ContextAwarePredicate> playerPredicate, boolean copy) {
            super(playerPredicate);
            this.copy = copy;
        }

        @NotNull
        @Override
        public JsonObject serializeToJson() {
            JsonObject json = super.serializeToJson();
            json.addProperty(JsonConstants.COPY, copy);
            return json;
        }

        public static ConfigurationCardTrigger.TriggerInstance copy() {
            return new ConfigurationCardTrigger.TriggerInstance(Optional.empty(), true);
        }

        public static ConfigurationCardTrigger.TriggerInstance paste() {
            return new ConfigurationCardTrigger.TriggerInstance(Optional.empty(), false);
        }
    }
}