package mekanism.common.integration.curios;

import java.util.Optional;
import java.util.function.Predicate;
import mekanism.client.render.MekanismCurioRenderer;
import mekanism.client.render.armor.ISpecialGear;
import mekanism.common.Mekanism;
import mekanism.common.registries.MekanismItems;
import mekanism.common.util.RegistryUtils;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;
import top.theillusivec4.curios.api.client.CuriosRendererRegistry;

public class CuriosIntegration {

    public static void addListeners(IEventBus bus) {
        bus.addListener((FMLClientSetupEvent event) -> registerRenderers(MekanismItems.JETPACK, MekanismItems.ARMORED_JETPACK));
    }

    private static void registerRenderers(ItemLike... items) {
        for (ItemLike item : items) {
            if (item.asItem() instanceof ArmorItem armor && IClientItemExtensions.of(armor) instanceof ISpecialGear gear) {
                CuriosRendererRegistry.register(armor, () -> new MekanismCurioRenderer(gear.getGearModel(armor.getType())));
            } else {
                Mekanism.logger.warn("Attempted to register Curios renderer for non-special gear item: {}.", RegistryUtils.getName(item.asItem()));
            }
        }
    }

    public static Optional<? extends IItemHandler> getCuriosInventory(LivingEntity entity) {
        return Optional.empty();//TODO Curios update
        //return CuriosApi.getCuriosHelper().getEquippedCurios(entity).resolve();
    }

    public static Optional<SlotResult> findFirstCurioAsResult(@NotNull LivingEntity entity, Predicate<ItemStack> filter) {
        return Optional.empty();//TODO Curios update
        //return CuriosApi.getCuriosHelper().findFirstCurio(entity, filter);
    }

    public static ItemStack findFirstCurio(@NotNull LivingEntity entity, Predicate<ItemStack> filter) {
        return findFirstCurioAsResult(entity, filter)
              .map(SlotResult::stack)
              .orElse(ItemStack.EMPTY);
    }

    public static ItemStack getCurioStack(@NotNull LivingEntity entity, String slotType, int slot) {
        return ItemStack.EMPTY;//todo Curios update
        //return CuriosApi.getCuriosHelper().getCuriosHandler(entity)
        //      .resolve()
        //      .flatMap(handler -> handler.getStacksHandler(slotType))
        //      .map(handler -> handler.getStacks().getStackInSlot(slot))
        //      .orElse(ItemStack.EMPTY);
    }
}