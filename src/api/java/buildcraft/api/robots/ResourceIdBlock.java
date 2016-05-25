/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.api.robots;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

import buildcraft.api.core.EnumPipePart;

public class ResourceIdBlock extends ResourceId {

    public BlockPos pos = new BlockPos(0, 0, 0);
    public EnumPipePart side = EnumPipePart.CENTER;

    public ResourceIdBlock() {

    }

    public ResourceIdBlock(int x, int y, int z) {
        pos = new BlockPos(x, y, z);
    }

    public ResourceIdBlock(BlockPos iIndex) {
        pos = iIndex;
    }

    public ResourceIdBlock(TileEntity tile) {
        pos = tile.getPos();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || obj.getClass() != getClass()) {
            return false;
        }

        ResourceIdBlock compareId = (ResourceIdBlock) obj;

        return pos.equals(compareId.pos) && side == compareId.side;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(pos.hashCode()).append(side != null ? side.ordinal() : 0).build();
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);

        int[] arr = new int[] { pos.getX(), pos.getY(), pos.getZ() };
        nbt.setIntArray("pos", arr);

        nbt.setTag("side", side.writeToNBT());
    }

    @Override
    protected void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        int[] arr = nbt.getIntArray("pos");
        pos = new BlockPos(arr[0], arr[1], arr[2]);

        side = EnumPipePart.readFromNBT(nbt.getTag("side"));
    }
}
