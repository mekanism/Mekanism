package mekanism.common.advancements.triggers;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import mekanism.api.JsonConstants;
import mekanism.common.advancements.MekanismCriteriaTriggers;
import mekanism.common.registries.MekanismDamageTypes;
import mekanism.common.registries.MekanismDamageTypes.MekanismDamageType;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.SerializationContext;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import org.jetbrains.annotations.NotNull;

public class MekanismDamageTrigger extends SimpleCriterionTrigger<MekanismDamageTrigger.TriggerInstance> {

    private final ResourceLocation id;

    public MekanismDamageTrigger(ResourceLocation id) {
        this.id = id;
    }

    @NotNull
    @Override
    public ResourceLocation getId() {
        return id;
    }

    @NotNull
    @Override
    protected TriggerInstance createInstance(@NotNull JsonObject json, @NotNull ContextAwarePredicate playerPredicate, @NotNull DeserializationContext context) {
        String damageType = GsonHelper.getAsString(json, JsonConstants.DAMAGE);
        //TODO - 1.20: Better filter compare
        MekanismDamageType damageSource = MekanismDamageTypes.DAMAGE_TYPES.stream()
              .filter(typeRO -> typeRO.key().location().toString().equals(damageType)).findFirst()
              .orElseThrow(() -> new JsonSyntaxException("Expected " + JsonConstants.DAMAGE + " to represent a Mekanism damage source."));
        return new TriggerInstance(playerPredicate, damageSource, GsonHelper.getAsBoolean(json, JsonConstants.KILLED));
    }

    public void trigger(ServerPlayer player, MekanismDamageType damageSource, boolean hardcoreTotem) {
        this.trigger(player, instance -> {
            //If it is just any damage regardless of killed or the player is dead (or is on hardcore and used up a totem of undying)
            if (!instance.killed || player.isDeadOrDying() || hardcoreTotem) {
                //And the damage source matches
                return instance.damageType.key() == damageSource.key();
            }
            return false;
        });
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {

        private final MekanismDamageType damageType;
        private final boolean killed;

        public TriggerInstance(ContextAwarePredicate playerPredicate, MekanismDamageType damageType, boolean killed) {
            super(MekanismCriteriaTriggers.DAMAGE.getId(), playerPredicate);
            this.damageType = damageType;
            this.killed = killed;
        }

        @NotNull
        @Override
        public JsonObject serializeToJson(@NotNull SerializationContext context) {
            JsonObject json = super.serializeToJson(context);
            json.addProperty(JsonConstants.DAMAGE, damageType.key().location().toString());
            json.addProperty(JsonConstants.KILLED, killed);
            return json;
        }

        public static MekanismDamageTrigger.TriggerInstance damaged(MekanismDamageType damageType) {
            return new MekanismDamageTrigger.TriggerInstance(ContextAwarePredicate.ANY, damageType, false);
        }

        public static MekanismDamageTrigger.TriggerInstance killed(MekanismDamageType damageType) {
            return new MekanismDamageTrigger.TriggerInstance(ContextAwarePredicate.ANY, damageType, true);
        }
    }
}