package mekanism.common.advancements.triggers;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import java.util.Optional;
import mekanism.api.JsonConstants;
import mekanism.common.advancements.MekanismCriteriaTriggers;
import mekanism.common.registries.MekanismDamageTypes;
import mekanism.common.registries.MekanismDamageTypes.MekanismDamageType;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import org.jetbrains.annotations.NotNull;

public class MekanismDamageTrigger extends SimpleCriterionTrigger<MekanismDamageTrigger.TriggerInstance> {
    @NotNull
    @Override
    protected TriggerInstance createInstance(@NotNull JsonObject json, @NotNull Optional<ContextAwarePredicate> playerPredicate, @NotNull DeserializationContext context) {
        String damage = GsonHelper.getAsString(json, JsonConstants.DAMAGE);
        MekanismDamageType damageType = MekanismDamageTypes.DAMAGE_TYPES.get(damage);
        if (damageType == null) {
            throw new JsonSyntaxException("Expected " + JsonConstants.DAMAGE + " to represent a Mekanism damage type.");
        }
        return new TriggerInstance(playerPredicate, damageType, GsonHelper.getAsBoolean(json, JsonConstants.KILLED));
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

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {

        private final MekanismDamageType damageType;
        private final boolean killed;

        public TriggerInstance(Optional<ContextAwarePredicate> playerPredicate, MekanismDamageType damageType, boolean killed) {
            super(playerPredicate);
            this.damageType = damageType;
            this.killed = killed;
        }

        @NotNull
        @Override
        public JsonObject serializeToJson() {
            JsonObject json = super.serializeToJson();
            json.addProperty(JsonConstants.DAMAGE, damageType.registryName().toString());
            json.addProperty(JsonConstants.KILLED, killed);
            return json;
        }

        public static Criterion<TriggerInstance> damaged(MekanismDamageType damageType) {
            return MekanismCriteriaTriggers.DAMAGE.createCriterion(new TriggerInstance(Optional.empty(), damageType, false));
        }

        public static Criterion<TriggerInstance> killed(MekanismDamageType damageType) {
            return MekanismCriteriaTriggers.DAMAGE.createCriterion(new TriggerInstance(Optional.empty(), damageType, true));
        }
    }
}