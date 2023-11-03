package mekanism.common.advancements.triggers;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import java.util.Arrays;
import java.util.Locale;
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
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public class UseGaugeDropperTrigger extends SimpleCriterionTrigger<UseGaugeDropperTrigger.TriggerInstance> {
    @NotNull
    @Override
    protected TriggerInstance createInstance(@NotNull JsonObject json, @NotNull Optional<ContextAwarePredicate> playerPredicate, @NotNull DeserializationContext context) {
        String actionName = GsonHelper.getAsString(json, JsonConstants.ACTION);
        UseDropperAction action = Arrays.stream(UseDropperAction.ACTIONS)
              .filter(a -> a.getSerializedName().equals(actionName))
              .findFirst()
              .orElseThrow(() -> new JsonSyntaxException("Unknown dropper use action: " + actionName));
        return new TriggerInstance(playerPredicate, action);
    }

    public void trigger(ServerPlayer player, UseDropperAction action) {
        this.trigger(player, instance -> instance.action == UseDropperAction.ANY || instance.action == action);
    }

    public enum UseDropperAction implements StringRepresentable {
        ANY,
        FILL,
        DRAIN,
        DUMP;

        //Do not modify
        private static final UseDropperAction[] ACTIONS = values();

        @NotNull
        @Override
        public String getSerializedName() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {

        private final UseDropperAction action;

        public TriggerInstance(Optional<ContextAwarePredicate> playerPredicate, UseDropperAction action) {
            super(playerPredicate);
            this.action = action;
        }

        @NotNull
        @Override
        public JsonObject serializeToJson() {
            JsonObject json = super.serializeToJson();
            json.addProperty(JsonConstants.ACTION, action.getSerializedName());
            return json;
        }

        public static UseGaugeDropperTrigger.TriggerInstance any() {
            return new UseGaugeDropperTrigger.TriggerInstance(Optional.empty(), UseDropperAction.ANY);
        }
    }
}