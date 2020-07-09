package mekanism.common.item.block.machine;

import javax.annotation.Nonnull;
import mekanism.common.block.prefab.BlockTile;
import mekanism.common.content.blocktype.Machine;
import mekanism.common.item.interfaces.IItemSustainedInventory;
import mekanism.common.lib.security.ISecurityItem;
import mekanism.common.tile.machine.TileEntityDigitalMiner;
import mekanism.common.util.MekanismUtils;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemBlockDigitalMiner extends ItemBlockMachine implements IItemSustainedInventory, ISecurityItem {

    public ItemBlockDigitalMiner(BlockTile<TileEntityDigitalMiner, Machine<TileEntityDigitalMiner>> block) {
        super(block);
    }

    @Override
    public boolean placeBlock(@Nonnull BlockItemUseContext context, @Nonnull BlockState state) {
        World world = context.getWorld();
        BlockPos placePos = context.getPos();
        for (int xPos = -1; xPos <= 1; xPos++) {
            for (int yPos = 0; yPos <= 1; yPos++) {
                for (int zPos = -1; zPos <= 1; zPos++) {
                    BlockPos pos = placePos.add(xPos, yPos, zPos);
                    if (!MekanismUtils.isValidReplaceableBlock(world, pos)) {
                        // If it won't fit then fail
                        return false;
                    }
                }
            }
        }
        return super.placeBlock(context, state);
    }
}
