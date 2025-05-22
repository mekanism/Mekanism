package mekanism.api;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public interface IWrenchable {

    WrenchResult tryWrench(BlockState state, Player player, ItemStack stack);
}
