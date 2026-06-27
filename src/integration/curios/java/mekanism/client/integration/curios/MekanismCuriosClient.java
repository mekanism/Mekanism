package mekanism.client.integration.curios;

import mekanism.client.render.armor.ICustomArmor;
import mekanism.client.render.armor.ISpecialGear;
import mekanism.common.Mekanism;
import mekanism.common.integration.MekanismHooks;
import mekanism.common.registries.MekanismItems;
import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import top.theillusivec4.curios.api.client.ICurioRenderer;

@Mod(value = Mekanism.MODID, dist = Dist.CLIENT, depends = MekanismHooks.CURIOS_MOD_ID)
public class MekanismCuriosClient {

    public MekanismCuriosClient(IEventBus modEventBus) {
        modEventBus.addListener((FMLClientSetupEvent _) -> registerRenderers(MekanismItems.JETPACK, MekanismItems.ARMORED_JETPACK));
    }

    @SafeVarargs
    private static void registerRenderers(Holder<Item>... items) {
        for (Holder<Item> holder : items) {
            Item item = holder.value();
            if (IClientItemExtensions.of(item) instanceof ISpecialGear gear) {
                ICustomArmor customArmor = gear.gearModel();
                ICurioRenderer.register(item, () -> new MekanismCurioRenderer(customArmor));
            } else {
                Mekanism.logger.warn("Attempted to register Curios renderer for non-special gear item: {}.", holder.getRegisteredName());
            }
        }
    }
}