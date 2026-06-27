package mekanism.common.integration.curios;

import java.util.Optional;
import java.util.function.Predicate;
import mekanism.api.MekanismAPI;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import org.jspecify.annotations.Nullable;

public interface ICuriosHelper {

    @Nullable
    ICuriosHelper INSTANCE = MekanismAPI.getOptionalService(ICuriosHelper.class);

    Optional<CuriosSlotTarget> findFirstCurioSlotTarget(LivingEntity entity, Predicate<ItemStack> filter);

    @Nullable
    ItemAccess findFirstCurio(LivingEntity entity, Predicate<ItemAccess> filter);

    ItemStack getCurioStack(LivingEntity entity, String slotType, int slot);

    record CuriosSlotTarget(String identifier, int index) {
    }
}