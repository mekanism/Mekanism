package mekanism.common.tile;

import javax.annotation.Nonnull;
import mekanism.api.Coord4D;
import mekanism.common.Mekanism;
import mekanism.common.MekanismBlock;
import mekanism.common.base.IActiveState;
import mekanism.common.base.IBoundingBlock;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.ChargeUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3i;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileEntitySeismicVibrator extends TileEntityMekanism implements IActiveState, IBoundingBlock {

    private static final int[] SLOTS = {0};

    public int clientPiston;

    public TileEntitySeismicVibrator() {
        super(MekanismBlock.SEISMIC_VIBRATOR);
    }

    @Override
    public void onUpdate() {
        if (world.isRemote) {
            if (getActive()) {
                clientPiston++;
            }
        } else {
            ChargeUtils.discharge(0, this);
            if (MekanismUtils.canFunction(this) && getEnergy() >= getBaseUsage()) {
                setActive(true);
                pullEnergy(null, getBaseUsage(), false);
            } else {
                setActive(false);
            }
        }
        if (getActive()) {
            Mekanism.activeVibrators.add(Coord4D.get(this));
        } else {
            Mekanism.activeVibrators.remove(Coord4D.get(this));
        }
    }

    @Override
    public void invalidate() {
        super.invalidate();
        Mekanism.activeVibrators.remove(Coord4D.get(this));
    }

    @Override
    public boolean lightUpdate() {
        return true;
    }

    @Override
    public boolean canSetFacing(@Nonnull Direction facing) {
        return facing != Direction.DOWN && facing != Direction.UP;
    }

    @Override
    public boolean canReceiveEnergy(Direction side) {
        return side == getOppositeDirection();
    }

    @Override
    public void onPlace() {
        MekanismUtils.makeBoundingBlock(world, getPos().up(), Coord4D.get(this));
    }

    @Override
    public void onBreak() {
        world.setBlockToAir(getPos().up());
        world.setBlockToAir(getPos());
    }

    @Nonnull
    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox() {
        return INFINITE_EXTENT_AABB;
    }

    @Nonnull
    @Override
    public int[] getSlotsForFace(@Nonnull Direction side) {
        return SLOTS;
    }

    @Override
    public boolean isItemValidForSlot(int slot, @Nonnull ItemStack stack) {
        return ChargeUtils.canBeDischarged(stack);
    }

    @Nonnull
    @Override
    public BlockFaceShape getOffsetBlockFaceShape(@Nonnull Direction face, @Nonnull Vec3i offset) {
        return BlockFaceShape.SOLID;
    }
}