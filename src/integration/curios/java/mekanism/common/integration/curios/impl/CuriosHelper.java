package mekanism.common.integration.curios.impl;

import java.util.Optional;
import java.util.function.Predicate;
import mekanism.common.integration.curios.ICuriosHelper;
import mekanism.common.util.ItemAccessUtils;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import org.jspecify.annotations.Nullable;
import top.theillusivec4.curios.api.CuriosCapability;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.SlotResult;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

public class CuriosHelper implements ICuriosHelper {

    private Optional<SlotResult> findFirstCurioAsResult(LivingEntity entity, Predicate<ItemStack> filter) {
        ICuriosItemHandler capability = entity.getCapability(CuriosCapability.INVENTORY);
        if (capability == null) {
            return Optional.empty();
        }
        return capability.findFirstCurio(filter);
    }

    @Override
    public Optional<CuriosSlotTarget> findFirstCurioSlotTarget(LivingEntity entity, Predicate<ItemStack> filter) {
        return findFirstCurioAsResult(entity, filter).map(result -> {
            SlotContext slotContext = result.slotContext();
            return new CuriosSlotTarget(slotContext.identifier(), slotContext.index());
        });
    }

    @Nullable
    @Override
    public ItemAccess findFirstCurio(LivingEntity entity, Predicate<ItemAccess> filter) {
        return findFirstCurioAsResult(entity, stack -> filter.test(ItemAccessUtils.sideEffectFreeAccess(stack)))
              .map(SlotResult::stack)
              .map(ItemAccess::forStack)
              .orElse(null);
    }

    @Override
    public ItemStack getCurioStack(LivingEntity entity, String slotType, int slot) {
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