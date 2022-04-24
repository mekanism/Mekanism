package mekanism.generators.common.item;

import mekanism.common.util.WorldUtils;
import mekanism.generators.common.tile.turbine.TileEntityTurbineRotor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelReader;

public class ItemTurbineBlade extends Item {

    public ItemTurbineBlade(Properties properties) {
        super(properties);
    }

    @Override
    public boolean doesSneakBypassUse(ItemStack stack, LevelReader world, BlockPos pos, Player player) {
        return WorldUtils.getTileEntity(TileEntityTurbineRotor.class, world, pos) != null;
    }
}