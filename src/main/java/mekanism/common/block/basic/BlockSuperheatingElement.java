package mekanism.common.block.basic;

import javax.annotation.Nonnull;
import mekanism.common.base.IActiveState;
import mekanism.common.block.BlockBasic;
import mekanism.common.block.interfaces.IBlockDescriptive;
import mekanism.common.content.boiler.SynchronizedBoilerData;
import mekanism.common.tile.TileEntitySuperheatingElement;
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockSuperheatingElement extends BlockBasic implements IBlockDescriptive {

    public BlockSuperheatingElement() {
        super("superheating_element");
    }

    @Override
    public String getDescription() {
        //TODO: Should name just be gotten from registry name
        return LangUtils.localize("tooltip.mekanism." + this.name);
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(@Nonnull World world, @Nonnull IBlockState state) {
        return new TileEntitySuperheatingElement();
    }

    @Override
    public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos) {
        TileEntity tileEntity = MekanismUtils.getTileEntitySafe(world, pos);
        if (tileEntity instanceof IActiveState) {
            if (((IActiveState) tileEntity).getActive() && ((IActiveState) tileEntity).lightUpdate()) {
                return 15;
            }
        }
        if (tileEntity instanceof TileEntitySuperheatingElement) {
            TileEntitySuperheatingElement element = (TileEntitySuperheatingElement) tileEntity;
            if (element.multiblockUUID != null && SynchronizedBoilerData.clientHotMap.get(element.multiblockUUID) != null) {
                return SynchronizedBoilerData.clientHotMap.get(element.multiblockUUID) ? 15 : 0;
            }
        }
        return 0;
    }
}