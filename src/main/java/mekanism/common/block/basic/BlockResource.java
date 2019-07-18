package mekanism.common.block.basic;

import mekanism.common.block.BlockBasic;
import mekanism.common.block.states.BlockStateBasic.BasicBlockType;
import mekanism.common.resource.INamedResource;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockResource extends BlockBasic {

    private final INamedResource resource;

    //TODO: Isn't as "generic"? So make it be from one BlockType thing?
    public BlockResource(INamedResource resource) {
        super("block_" + resource.getRegistrySuffix());
        this.resource = resource;
    }

    @Override
    @Deprecated
    public float getBlockHardness(IBlockState blockState, World worldIn, BlockPos pos) {
        BasicBlockType type = BasicBlockType.get(getBasicBlock(), blockState.getBlock().getMetaFromState(blockState));
        if (type == BasicBlockType.REFINED_OBSIDIAN) {
            return 50.0F;
        } else if (type == BasicBlockType.OSMIUM_BLOCK) {
            return 7.5F;
        }
        return blockHardness;
    }

    @Override
    public float getExplosionResistance(World world, BlockPos pos, Entity exploder, Explosion explosion) {
        IBlockState state = world.getBlockState(pos);
        BasicBlockType type = BasicBlockType.get(getBasicBlock(), state.getBlock().getMetaFromState(state));
        float defaultResistance = blockResistance / 5F;
        if (type == null) {
            return defaultResistance;
        }
        switch (type) {
            case REFINED_OBSIDIAN:
                return 2400F;
            case OSMIUM_BLOCK:
                return 12F;
            case STEEL_BLOCK:
            case BRONZE_BLOCK:
                return 9F;
            default:
                return defaultResistance;
        }
    }

    @Override
    public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos) {
        BasicBlockType type = BasicBlockType.get(getBasicBlock(), state.getBlock().getMetaFromState(state));
        switch (type) {
            case REFINED_OBSIDIAN:
                return 8;
            case REFINED_GLOWSTONE:
                return 15;
        }
        return 0;
    }

    @Override
    public boolean isBeaconBase(IBlockAccess world, BlockPos pos, BlockPos beacon) {
        //TODO: Make the charcoal block be false (or more likely get it from the thing passed into BlockResource constructor
        return true;
    }
}