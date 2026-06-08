package mekanism.common.integration.curios;

import java.util.Optional;
import java.util.function.Predicate;
import mekanism.client.render.MekanismCurioRenderer;
import mekanism.client.render.armor.ICustomArmor;
import mekanism.client.render.armor.ISpecialGear;
import mekanism.common.Mekanism;
import mekanism.common.registries.MekanismItems;
import mekanism.common.util.ItemAccessUtils;
import mekanism.common.util.StackUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.Equippable;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.theillusivec4.curios.api.CuriosCapability;
import top.theillusivec4.curios.api.SlotResult;
import top.theillusivec4.curios.api.client.ICurioRenderer;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

public class CuriosIntegration {

    public static void addListeners(IEventBus bus) {
        bus.addListener((FMLClientSetupEvent _) -> registerRenderers(MekanismItems.JETPACK, MekanismItems.ARMORED_JETPACK));
    }

    @SafeVarargs
    private static void registerRenderers(Holder<Item>... items) {
        for (Holder<Item> holder : items) {
            Item item = holder.value();
            Equippable equippable = item.components().get(DataComponents.EQUIPPABLE);
            if (StackUtils.isRenderableArmor(equippable) && IClientItemExtensions.of(item) instanceof ISpecialGear gear) {
                ICustomArmor customArmor = gear.gearModel();
                ICurioRenderer.register(item, () -> new MekanismCurioRenderer(customArmor));
            } else {
                Mekanism.logger.warn("Attempted to register Curios renderer for non-special gear item: {}.", holder.getRegisteredName());
            }
        }
    }

    @Nullable
    public static ResourceHandler<ItemResource> getCuriosInventory(LivingEntity entity) {
        return entity.getCapability(CuriosCapability.ITEM_HANDLER);
    }

    public static Optional<SlotResult> findFirstCurioAsResult(@NotNull LivingEntity entity, Predicate<ItemStack> filter) {
        ICuriosItemHandler capability = entity.getCapability(CuriosCapability.INVENTORY);
        if (capability == null) {
            return Optional.empty();
        }
        return capability.findFirstCurio(filter);
    }

    @Nullable
    public static ItemAccess findFirstCurio(@NotNull LivingEntity entity, Predicate<ItemAccess> filter) {
        return findFirstCurioAsResult(entity, stack -> filter.test(ItemAccessUtils.sideEffectFreeAccess(stack)))
              .map(SlotResult::stack)
              .map(ItemAccess::forStack)
              .orElse(null);
    }

    public static ItemStack getCurioStack(@NotNull LivingEntity entity, String slotType, int slot) {
        ICuriosItemHandler capability = entity.getCapability(CuriosCapability.INVENTORY);
        if (capability == null) {
            return ItemStack.EMPTY;
        }
        Optional<ICurioStacksHandler> stacksHandler = capability.getStacksHandler(slotType);
        //noinspection OptionalIsPresent - Capturing lambda
        if (stacksHandler.isPresent()) {
            return stacksHandler.get().getStacks().getStackInSlot(slot);
        }
        return ItemStack.EMPTY;
    }
}