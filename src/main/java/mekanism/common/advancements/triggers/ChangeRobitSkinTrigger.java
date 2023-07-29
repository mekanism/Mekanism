package mekanism.common.advancements.triggers;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import mekanism.api.JsonConstants;
import mekanism.api.MekanismAPI;
import mekanism.api.robit.RobitSkin;
import mekanism.common.advancements.MekanismCriteriaTriggers;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.SerializationContext;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ChangeRobitSkinTrigger extends SimpleCriterionTrigger<ChangeRobitSkinTrigger.TriggerInstance> {

    private final ResourceLocation id;

    public ChangeRobitSkinTrigger(ResourceLocation id) {
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
        ResourceKey<RobitSkin> skin;
        if (json.has(JsonConstants.SKIN)) {
            String name = GsonHelper.getAsString(json, JsonConstants.SKIN);
            ResourceLocation registryName = ResourceLocation.tryParse(name);
            if (registryName == null) {
                throw new JsonSyntaxException("Expected property '" + JsonConstants.SKIN + "' to be a valid resource location, was: '" + name + "'.");
            }
            skin = ResourceKey.create(MekanismAPI.ROBIT_SKIN_REGISTRY_NAME, registryName);
        } else {
            skin = null;
        }
        return new TriggerInstance(playerPredicate, skin);
    }

    public void trigger(ServerPlayer player, ResourceKey<RobitSkin> skin) {
        this.trigger(player, instance -> instance.skin == null || instance.skin == skin);
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {

        @Nullable
        private final ResourceKey<RobitSkin> skin;

        public TriggerInstance(ContextAwarePredicate playerPredicate, @Nullable ResourceKey<RobitSkin> skin) {
            super(MekanismCriteriaTriggers.CHANGE_ROBIT_SKIN.getId(), playerPredicate);
            this.skin = skin;
        }

        @NotNull
        @Override
        public JsonObject serializeToJson(@NotNull SerializationContext context) {
            JsonObject json = super.serializeToJson(context);
            if (skin != null) {
                json.addProperty(JsonConstants.SKIN, skin.location().toString());
            }
            return json;
        }

        public static ChangeRobitSkinTrigger.TriggerInstance toAny() {
            return new ChangeRobitSkinTrigger.TriggerInstance(ContextAwarePredicate.ANY, null);
        }

        public static ChangeRobitSkinTrigger.TriggerInstance toSkin(ResourceKey<RobitSkin> skin) {
            return new ChangeRobitSkinTrigger.TriggerInstance(ContextAwarePredicate.ANY, skin);
        }
    }
}