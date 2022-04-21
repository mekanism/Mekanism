package mekanism.common.integration.curios;

import mekanism.client.render.MekanismCurioRenderer;
import mekanism.client.render.armor.ISpecialGear;
import mekanism.client.render.armor.JetpackArmor;
import mekanism.common.Mekanism;
import mekanism.common.integration.MekanismHooks;
import mekanism.common.registries.MekanismItems;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.client.RenderProperties;
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
        bus.addListener((FMLClientSetupEvent event) -> registerRenderers(MekanismItems.JETPACK, MekanismItems.ARMORED_JETPACK));
    }

    private static void registerRenderers(ItemLike... items) {
        for (final ItemLike item : items) {
            if (item.asItem() instanceof ArmorItem armour && RenderProperties.get(armour) instanceof ISpecialGear gear) {
                CuriosRendererRegistry.register(armour, () -> new MekanismCurioRenderer(gear.getGearModel(armour.getSlot())));
            } else {
                Mekanism.logger.warn("Attempted to register Curios renderer for a non-special gear item.");
            }
        }
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
