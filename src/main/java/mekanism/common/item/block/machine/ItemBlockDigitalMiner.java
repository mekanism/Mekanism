package mekanism.common.item.block.machine;

import javax.annotation.Nonnull;
import mekanism.client.render.item.ISTERProvider;
import mekanism.common.block.machine.prefab.BlockMachine;
import mekanism.common.content.blocktype.Machine;
import mekanism.common.item.IItemEnergized;
import mekanism.common.item.IItemSustainedInventory;
import mekanism.common.security.ISecurityItem;
import mekanism.common.tile.TileEntityDigitalMiner;
import mekanism.common.util.MekanismUtils;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemBlockDigitalMiner extends ItemBlockMachine implements IItemEnergized, IItemSustainedInventory, ISecurityItem {

    public ItemBlockDigitalMiner(BlockMachine<TileEntityDigitalMiner, Machine<TileEntityDigitalMiner>> block) {
        super(block, ISTERProvider::miner);
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
