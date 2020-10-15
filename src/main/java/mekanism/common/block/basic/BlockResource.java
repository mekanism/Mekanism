package mekanism.common.block.basic;

import javax.annotation.Nonnull;
import mekanism.common.block.BlockMekanism;
import mekanism.common.resource.BlockResourceInfo;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.PushReaction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.common.ToolType;

public class BlockResource extends BlockMekanism {

    @Nonnull
    private final BlockResourceInfo resource;

    //TODO: Isn't as "generic"? So make it be from one BlockType thing?
    public BlockResource(@Nonnull BlockResourceInfo resource) {
        super(AbstractBlock.Properties.create(Material.IRON).hardnessAndResistance(resource.getHardness(), resource.getResistance())
              .setLightLevel(state -> resource.getLightValue()).setRequiresTool().harvestTool(ToolType.PICKAXE).harvestLevel(resource.getHarvestLevel()));
        this.resource = resource;
    }

    @Nonnull
    public BlockResourceInfo getResourceInfo() {
        return resource;
    }

    @Nonnull
    @Override
    @Deprecated
    public PushReaction getPushReaction(@Nonnull BlockState state) {
        return resource.getPushReaction();
    }

    @Override
    public boolean isPortalFrame(BlockState state, IBlockReader world, BlockPos pos) {
        return resource.isPortalFrame();
    }
}