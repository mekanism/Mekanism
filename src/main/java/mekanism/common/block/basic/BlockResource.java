package mekanism.common.block.basic;

import javax.annotation.Nonnull;
import mekanism.common.block.BlockMekanism;
import mekanism.common.resource.BlockResourceInfo;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;
import net.minecraftforge.common.ToolType;

public class BlockResource extends BlockMekanism {

    @Nonnull
    private final BlockResourceInfo resource;

    //TODO: Isn't as "generic"? So make it be from one BlockType thing?
    public BlockResource(@Nonnull BlockResourceInfo resource) {
        super(Block.Properties.create(Material.IRON).hardnessAndResistance(resource.getHardness(), resource.getResistance()).lightValue(resource.getLightValue())
            .harvestTool(ToolType.PICKAXE).harvestLevel(resource.getHarvestLevel()));
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
    public boolean isPortalFrame(BlockState state, IWorldReader world, BlockPos pos) {
        return resource.isPortalFrame();
    }
}