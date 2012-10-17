package universalelectricity.prefab;

import net.minecraftforge.common.ForgeDirection;
import universalelectricity.electricity.ElectricityManager;
import universalelectricity.implement.IElectricityReceiver;

/**
 * An easier way to implement the methods from IElectricityReceiver with default values set.
 * @author Calclavia
 */
public abstract class TileEntityElectricityReceiver extends TileEntityDisableable implements IElectricityReceiver
{
    public TileEntityElectricityReceiver()
    {
        ElectricityManager.instance.registerElectricUnit(this);
    }
    
    @Override
    public void updateEntity()
    {
    	 super.updateEntity();
    }
    
    @Override
    public boolean canConnect(ForgeDirection side)
    {
        return this.canReceiveFromSide(side);
    }

    @Override
    public double getVoltage()
    {
        return 120;
    }
}
