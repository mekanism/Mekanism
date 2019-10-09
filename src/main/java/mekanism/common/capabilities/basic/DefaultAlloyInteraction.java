package mekanism.common.capabilities.basic;

import mekanism.api.IAlloyInteraction;
import mekanism.common.capabilities.basic.DefaultStorageHelper.NullStorage;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraftforge.common.capabilities.CapabilityManager;

public class DefaultAlloyInteraction implements IAlloyInteraction {

    public static void register() {
        CapabilityManager.INSTANCE.register(IAlloyInteraction.class, new NullStorage<>(), DefaultAlloyInteraction::new);
    }

    @Override
    public void onAlloyInteraction(PlayerEntity player, Hand hand, ItemStack stack, int tierOrdinal) {
    }
}