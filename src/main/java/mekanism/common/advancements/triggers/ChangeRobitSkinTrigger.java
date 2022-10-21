package mekanism.common.advancements.triggers;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import mekanism.api.JsonConstants;
import mekanism.api.MekanismAPI;
import mekanism.api.providers.IRobitSkinProvider;
import mekanism.api.robit.RobitSkin;
import mekanism.common.advancements.MekanismCriteriaTriggers;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SerializationContext;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.registries.IForgeRegistry;
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
    protected TriggerInstance createInstance(@NotNull JsonObject json, @NotNull EntityPredicate.Composite playerPredicate, @NotNull DeserializationContext context) {
        RobitSkin skin;
        if (json.has(JsonConstants.SKIN)) {
            String name = GsonHelper.getAsString(json, JsonConstants.SKIN);
            ResourceLocation registryName = ResourceLocation.tryParse(name);
            if (registryName == null) {
                throw new JsonSyntaxException("Expected property '" + JsonConstants.SKIN + "' to be a valid resource location, was: '" + name + "'.");
            }
            IForgeRegistry<RobitSkin> registry = MekanismAPI.robitSkinRegistry();
            if (!registry.containsKey(registryName)) {
                throw new JsonSyntaxException("No robit skin registered for name '" + registryName + "'.");
            }
            skin = registry.getValue(registryName);
        } else {
            skin = null;
        }
        return new TriggerInstance(playerPredicate, skin);
    }

    public void trigger(ServerPlayer player, RobitSkin skin) {
        this.trigger(player, instance -> instance.skin == null || instance.skin == skin);
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {

        @Nullable
        private final RobitSkin skin;

        public TriggerInstance(EntityPredicate.Composite playerPredicate, @Nullable RobitSkin skin) {
            super(MekanismCriteriaTriggers.CHANGE_ROBIT_SKIN.getId(), playerPredicate);
            this.skin = skin;
        }

        @NotNull
        @Override
        public JsonObject serializeToJson(@NotNull SerializationContext context) {
            JsonObject json = super.serializeToJson(context);
            if (skin != null) {
                json.addProperty(JsonConstants.SKIN, skin.getRegistryName().toString());
            }
            return json;
        }

        public static ChangeRobitSkinTrigger.TriggerInstance toAny() {
            return new ChangeRobitSkinTrigger.TriggerInstance(EntityPredicate.Composite.ANY, null);
        }

        public static ChangeRobitSkinTrigger.TriggerInstance toSkin(IRobitSkinProvider skinProvider) {
            return new ChangeRobitSkinTrigger.TriggerInstance(EntityPredicate.Composite.ANY, skinProvider.getSkin());
        }
    }
}