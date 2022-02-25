package mekanism.tools.client;

import mekanism.api.providers.IItemProvider;
import mekanism.client.ClientRegistrationUtil;
import mekanism.tools.client.render.item.RenderMekanismShieldItem;
import mekanism.tools.common.MekanismTools;
import mekanism.tools.common.registries.ToolsItems;
import net.minecraft.client.renderer.item.ItemPropertyFunction;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = MekanismTools.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ToolsClientRegistration {

    private ToolsClientRegistration() {
    }

    @SubscribeEvent
    public static void init(FMLClientSetupEvent event) {
        event.enqueueWork(() -> addShieldPropertyOverrides(MekanismTools.rl("blocking"),
              (stack, world, entity, seed) -> entity != null && entity.isUsingItem() && entity.getUseItem() == stack ? 1.0F : 0.0F,
              ToolsItems.BRONZE_SHIELD, ToolsItems.LAPIS_LAZULI_SHIELD, ToolsItems.OSMIUM_SHIELD, ToolsItems.REFINED_GLOWSTONE_SHIELD,
              ToolsItems.REFINED_OBSIDIAN_SHIELD, ToolsItems.STEEL_SHIELD));
    }

    private static void addShieldPropertyOverrides(ResourceLocation override, ItemPropertyFunction propertyGetter, IItemProvider... shields) {
        for (IItemProvider shield : shields) {
            ClientRegistrationUtil.setPropertyOverride(shield, override, propertyGetter);
        }
    }

    @SubscribeEvent
    public static void registerClientReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(RenderMekanismShieldItem.RENDERER);
    }

    @SubscribeEvent
    public static void onStitch(TextureStitchEvent.Pre event) {
        if (event.getAtlas().location().equals(TextureAtlas.LOCATION_BLOCKS)) {
            for (ShieldTextures textures : ShieldTextures.values()) {
                event.addSprite(textures.getBase().texture());
            }
        }
    }
}