
package thermalexpansion.api.tileentity;

import net.minecraftforge.common.ForgeDirection;

public interface IReconfigurableFacing {

    public int getFacing();

    public boolean rotateBlock();

    public boolean setFacing(int side);

    public boolean setFacing(ForgeDirection side);
}
