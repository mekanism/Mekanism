package mekanism.common.integration;

import mekanism.api.IMekWrench;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

public class GenericWrench implements IMekWrench {

    public static final GenericWrench INSTANCE = new GenericWrench();

    private GenericWrench() {
    }

    @Override
    public boolean canUseWrench(ItemStack stack, PlayerEntity player, BlockPos pos) {
        return true;
    }
}