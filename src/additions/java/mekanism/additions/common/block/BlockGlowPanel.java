package mekanism.additions.common.block;

import mekanism.additions.common.registries.AdditionsBlockTypes;
import mekanism.api.block.IColoredBlock;
import mekanism.api.text.EnumColor;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.attribute.AttributeStateFacing;
import mekanism.common.block.machine.prefab.BlockBase.BlockBaseModel;
import mekanism.common.content.blocktype.BlockType;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

public class BlockGlowPanel extends BlockBaseModel<BlockType> implements IColoredBlock {

    private final EnumColor color;

    public BlockGlowPanel(EnumColor color) {
        super(AdditionsBlockTypes.GLOW_PANEL, Block.Properties.create(Material.PISTON, color.getMapColor()).hardnessAndResistance(1F, 10F).lightValue(15));
        this.color = color;
    }

    @Override
    public EnumColor getColor() {
        return color;
    }

    @Override
    @Deprecated
    public void neighborChanged(BlockState state, World world, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean isMoving) {
        if (!world.isRemote) {
            Direction side = Attribute.get(state.getBlock(), AttributeStateFacing.class).getDirection(state);
            BlockPos adj = pos.offset(side.getOpposite());
            if (!Block.hasSolidSide(world.getBlockState(adj), world, adj, side)) {
                Block.spawnDrops(world.getBlockState(pos), world, pos, null);
                world.removeBlock(pos, isMoving);
            }
        }
    }

    @Override
    public boolean isValidPosition(BlockState state, IWorldReader world, BlockPos pos) {
        Direction side = Attribute.get(state.getBlock(), AttributeStateFacing.class).getDirection(state);
        BlockPos positionOn = pos.offset(side.getOpposite());
        //TODO: Maybe improve this so it matches the shape of the glow panel for what it checks
        // This commented out thing is more or less how the torch checks it
        //return !VoxelShapes.compare(state.getCollisionShape(world, pos).project(side), field_220084_c, IBooleanFunction.ONLY_SECOND);
        return Block.hasSolidSide(world.getBlockState(positionOn), world, positionOn, side);
    }
}