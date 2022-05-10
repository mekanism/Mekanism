package mekanism.common.integration.curios;

import java.util.Optional;
import java.util.function.Predicate;
import javax.annotation.Nonnull;
import mekanism.client.render.MekanismCurioRenderer;
import mekanism.client.render.armor.ISpecialGear;
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

public class CuriosIntegration {

    public static void sendIMC() {
        InterModComms.sendTo(MekanismHooks.CURIOS_MODID, "register_type", () -> SlotTypePreset.BODY.getMessageBuilder().build());
    }

    public static void addListeners(IEventBus bus) {
        bus.addListener((FMLClientSetupEvent event) -> registerRenderers(MekanismItems.JETPACK, MekanismItems.ARMORED_JETPACK));
    }

    private static void registerRenderers(ItemLike... items) {
        for (ItemLike item : items) {
            if (item.asItem() instanceof ArmorItem armor && RenderProperties.get(armor) instanceof ISpecialGear gear) {
                CuriosRendererRegistry.register(armor, () -> new MekanismCurioRenderer(gear.getGearModel(armor.getSlot())));
            } else {
                Mekanism.logger.warn("Attempted to register Curios renderer for non-special gear item: {}.", item.asItem().getRegistryName());
            }
        }
    }

    public static Optional<? extends IItemHandler> getCuriosInventory(LivingEntity entity) {
        return CuriosApi.getCuriosHelper().getEquippedCurios(entity).resolve();
    }

    public static Optional<SlotResult> findFirstCurioAsResult(@Nonnull LivingEntity entity, Predicate<ItemStack> filter) {
        return CuriosApi.getCuriosHelper().findFirstCurio(entity, filter);
    }

    public static ItemStack findFirstCurio(@Nonnull LivingEntity entity, Predicate<ItemStack> filter) {
        return findFirstCurioAsResult(entity, filter)
              .map(SlotResult::stack)
              .orElse(ItemStack.EMPTY);
    }

    public static ItemStack getCurioStack(@Nonnull LivingEntity entity, String slotType, int slot) {
        return CuriosApi.getCuriosHelper().getCuriosHandler(entity)
              .resolve()
              .flatMap(handler -> handler.getStacksHandler(slotType))
              .map(handler -> handler.getStacks().getStackInSlot(slot))
              .orElse(ItemStack.EMPTY);
    }
}