package mekanism.common.advancements.triggers;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import mekanism.api.JsonConstants;
import mekanism.common.advancements.MekanismCriteriaTriggers;
import mekanism.common.registries.MekanismDamageSource;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.EntityPredicate;
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
    protected TriggerInstance createInstance(@NotNull JsonObject json, @NotNull EntityPredicate.Composite playerPredicate, @NotNull DeserializationContext context) {
        String damageType = GsonHelper.getAsString(json, JsonConstants.DAMAGE);
        MekanismDamageSource damageSource = MekanismDamageSource.DAMAGE_SOURCES.stream().filter(source -> source.getMsgId().equals(damageType)).findFirst()
              .orElseThrow(() -> new JsonSyntaxException("Expected " + JsonConstants.DAMAGE + " to represent a Mekanism damage source."));
        return new TriggerInstance(playerPredicate, damageSource, GsonHelper.getAsBoolean(json, JsonConstants.KILLED));
    }

    public void trigger(ServerPlayer player, MekanismDamageSource damageSource, boolean hardcoreTotem) {
        this.trigger(player, instance -> {
            //If it is just any damage regardless of killed or the player is dead (or is on hardcore and used up a totem of undying)
            if (!instance.killed || player.isDeadOrDying() || hardcoreTotem) {
                //And the damage source matches
                return instance.damageType == damageSource;
            }
            return false;
        });
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {

        private final MekanismDamageSource damageType;
        private final boolean killed;

        public TriggerInstance(EntityPredicate.Composite playerPredicate, MekanismDamageSource damageType, boolean killed) {
            super(MekanismCriteriaTriggers.DAMAGE.getId(), playerPredicate);
            this.damageType = damageType;
            this.killed = killed;
        }

        @NotNull
        @Override
        public JsonObject serializeToJson(@NotNull SerializationContext context) {
            JsonObject json = super.serializeToJson(context);
            json.addProperty(JsonConstants.DAMAGE, damageType.getMsgId());
            json.addProperty(JsonConstants.KILLED, killed);
            return json;
        }

        public static MekanismDamageTrigger.TriggerInstance damaged(MekanismDamageSource damageType) {
            return new MekanismDamageTrigger.TriggerInstance(EntityPredicate.Composite.ANY, damageType, false);
        }

        public static MekanismDamageTrigger.TriggerInstance killed(MekanismDamageSource damageType) {
            return new MekanismDamageTrigger.TriggerInstance(EntityPredicate.Composite.ANY, damageType, true);
        }
    }
}