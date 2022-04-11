package mekanism.common.integration.curios;

import mekanism.client.render.MekanismCurioRenderer;
import mekanism.client.render.armor.JetpackArmor;
import mekanism.common.integration.MekanismHooks;
import mekanism.common.registries.MekanismItems;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.items.IItemHandler;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;
import top.theillusivec4.curios.api.SlotTypePreset;
import top.theillusivec4.curios.api.client.CuriosRendererRegistry;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class CuriosIntegration {

    public static void sendIMC() {
        InterModComms.sendTo(MekanismHooks.CURIOS_MODID, "register_type", () -> SlotTypePreset.BODY.getMessageBuilder().build());
    }

    public static void addListeners(final IEventBus bus) {
        bus.addListener((FMLClientSetupEvent event) -> {
            CuriosRendererRegistry.register(MekanismItems.JETPACK.asItem(), () -> new MekanismCurioRenderer(JetpackArmor.JETPACK));
            CuriosRendererRegistry.register(MekanismItems.ARMORED_JETPACK.asItem(), () -> new MekanismCurioRenderer(JetpackArmor.ARMORED_JETPACK));
        });
    }

    public static Optional<? extends IItemHandler> getCuriosInventory(LivingEntity living) {
        return CuriosApi.getCuriosHelper().getEquippedCurios(living).resolve();
    }

    public static Optional<ItemStack> findFirstCurio(@Nonnull LivingEntity livingEntity, Predicate<ItemStack> filter) {
        return findFirstCurioAsResult(livingEntity, filter).map(SlotResult::stack);
    }

    public static Optional<SlotResult> findFirstCurioAsResult(@Nonnull LivingEntity livingEntity, Predicate<ItemStack> filter) {
        return CuriosApi.getCuriosHelper().findFirstCurio(livingEntity, filter);
    }

    public static List<SlotResult> findCurio(@Nonnull LivingEntity livingEntity, Predicate<ItemStack> filter) {
        return CuriosApi.getCuriosHelper().findCurios(livingEntity, filter);
    }
}
