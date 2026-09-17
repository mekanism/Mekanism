package mekanism.tools.client;

import mekanism.tools.client.render.GlowArmor;
import mekanism.tools.client.render.item.RenderMekanismShieldItem.UnbakedShield;
import mekanism.tools.common.MekanismTools;
import mekanism.tools.common.registries.ToolsItems;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterSpecialModelRendererEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

@EventBusSubscriber(modid = MekanismTools.MODID, value = Dist.CLIENT)
public class ToolsClientRegistration {

    private ToolsClientRegistration() {
    }

    @SubscribeEvent
    public static void registerClientExtensions(RegisterClientExtensionsEvent event) {
        ToolsItems.REFINED_GLOWSTONE_ARMOR.forEach(item -> event.registerItem(new IClientItemExtensions() {
            @Override
            public Model<?> getHumanoidArmorModel(ItemStack itemStack, EquipmentClientInfo.LayerType layerType, Model original) {
                return original instanceof HumanoidModel<?> humanoidModel ? new GlowArmor<>(humanoidModel) : original;
            }
        }, item));
    }

    @SubscribeEvent
    public static void registerSpecialRenderer(RegisterSpecialModelRendererEvent event) {
        event.register(UnbakedShield.ID, UnbakedShield.MAP_CODEC);
    }
}