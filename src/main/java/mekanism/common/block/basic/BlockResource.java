package mekanism.common.block.basic;

import javax.annotation.Nonnull;
import mekanism.api.block.IHasModel;
import mekanism.common.block.BlockMekanism;
import mekanism.common.block.PortalHelper;
import mekanism.common.resource.BlockResourceInfo;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FireBlock;
import net.minecraft.block.material.Material;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

public class BlockResource extends BlockMekanism implements IHasModel {

    @Nonnull
    private final BlockResourceInfo resource;

    //TODO: Isn't as "generic"? So make it be from one BlockType thing?
    public BlockResource(@Nonnull BlockResourceInfo resource) {
        super(Block.Properties.create(Material.IRON).hardnessAndResistance(resource.getHardness(), resource.getResistance()).lightValue(resource.getLightValue()));
        this.resource = resource;
    }

    @Nonnull
    public BlockResourceInfo getResourceInfo() {
        return resource;
    }

    @Override
    public boolean isBeaconBase(BlockState state, IWorldReader world, BlockPos pos, BlockPos beacon) {
        return resource.isBeaconBase();
    }

    @Override
    @Deprecated
    public void neighborChanged(BlockState state, World world, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean isMoving) {
        if (!world.isRemote) {
            Block newBlock = world.getBlockState(neighborPos).getBlock();
            if (resource == BlockResourceInfo.REFINED_OBSIDIAN && newBlock instanceof FireBlock) {
                PortalHelper.BlockPortalOverride.instance.trySpawnPortal(world, neighborPos);
            }
        }
    }
}