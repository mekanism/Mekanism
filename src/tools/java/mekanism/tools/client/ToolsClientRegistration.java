package mekanism.tools.client;

import mekanism.tools.client.render.item.RenderMekanismShieldItem.UnbakedShield;
import mekanism.tools.common.MekanismTools;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterSpecialModelRendererEvent;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

@EventBusSubscriber(modid = MekanismTools.MODID, value = Dist.CLIENT)
public class ToolsClientRegistration {

    private ToolsClientRegistration() {
    }

    //TODO - 26.1: item property JSON. See net.minecraft.client.data.models.ItemModelGenerators.generateShield as we'll need to do the same (including transform)
    /*@SubscribeEvent
    public static void init(FMLClientSetupEvent event) {
        event.enqueueWork(() -> addShieldPropertyOverrides(MekanismTools.rl("blocking"),
              (stack, world, entity, seed) -> entity != null && entity.isUsingItem() && entity.getUseItem() == stack ? 1.0F : 0.0F,
              ToolsItems.BRONZE_SHIELD, ToolsItems.LAPIS_LAZULI_SHIELD, ToolsItems.OSMIUM_SHIELD, ToolsItems.REFINED_GLOWSTONE_SHIELD,
              ToolsItems.REFINED_OBSIDIAN_SHIELD, ToolsItems.STEEL_SHIELD));
    }

    @SafeVarargs
    private static void addShieldPropertyOverrides(Identifier override, ClampedItemPropertyFunction propertyGetter, Holder<Item>... shields) {
        for (Holder<Item> shield : shields) {
            ClientRegistrationUtil.setPropertyOverride(shield, override, propertyGetter);
        }
    }*/

    @SubscribeEvent
    public static void registerClientExtensions(RegisterClientExtensionsEvent event) {
        //TODO - 26.1: code updated to compile, but a singleton doesn't seem right? Probably should make a new instance for each?
        /*event.registerItem(new IClientItemExtensions() {
            @NotNull
            @Override
            public Model getHumanoidArmorModel(ItemStack itemStack, EquipmentClientInfo.LayerType layerType, Model original) {
                if (!(original instanceof HumanoidModel<?> humanoidModel)) {
                    return original;
                }
                return GlowArmor.wrap(humanoidModel);
            }
        }, ToolsItems.REFINED_GLOWSTONE_HELMET, ToolsItems.REFINED_GLOWSTONE_CHESTPLATE, ToolsItems.REFINED_GLOWSTONE_LEGGINGS, ToolsItems.REFINED_GLOWSTONE_BOOTS);*/


    }

    @SubscribeEvent
    public static void registerSpecialRenderer(RegisterSpecialModelRendererEvent event) {
        event.register(UnbakedShield.ID, UnbakedShield.MAP_CODEC);//todo - 26.1: register this in shield JSON
    }
}