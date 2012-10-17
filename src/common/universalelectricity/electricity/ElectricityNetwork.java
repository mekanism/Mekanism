package universalelectricity.electricity;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.src.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import universalelectricity.implement.IConductor;
import universalelectricity.implement.IElectricityReceiver;

public class ElectricityNetwork
{
    private int ID;
    public List<IConductor> conductors = new ArrayList<IConductor>();

    public ElectricityNetwork(int ID, IConductor conductor)
    {
        this.ID = ID;
        this.addConductor(conductor);
    }

    public void addConductor(IConductor newConductor)
    {
        this.cleanUpArray();

        if (!conductors.contains(newConductor))
        {
            conductors.add(newConductor);
            newConductor.setConnectionID(this.ID);
        }
    }

    /**
     * Get only the electric units that can receive electricity from the given side.
     */
    public List<IElectricityReceiver> getConnectedReceivers()
    {
        this.cleanUpArray();
        List<IElectricityReceiver> returnArray = new ArrayList<IElectricityReceiver>();

        for (IConductor conductor : conductors)
        {
            for (byte i = 0; i < conductor.getConnectedBlocks().length; i++)
            {
                TileEntity tileEntity = conductor.getConnectedBlocks()[i];

                if (tileEntity != null)
                {
                    if (tileEntity instanceof IElectricityReceiver)
                    {
                        if (!returnArray.contains((IElectricityReceiver)tileEntity) && ((IElectricityReceiver)tileEntity).canReceiveFromSide(ForgeDirection.getOrientation(i).getOpposite()))
                        {
                            returnArray.add((IElectricityReceiver)tileEntity);
                        }
                    }
                }
            }
        }

        return returnArray;
    }

    public void cleanUpArray()
    {
        for (int i = 0; i < conductors.size(); i++)
        {
            if (conductors.get(i) == null)
            {
                conductors.remove(i);
            }
            else if (((TileEntity)conductors.get(i)).isInvalid())
            {
                conductors.remove(i);
            }
        }
    }

    public void setID(int ID)
    {
        this.ID = ID;
        this.cleanUpArray();

        for (IConductor conductor : this.conductors)
        {
            conductor.setConnectionID(this.ID);
        }
    }

    public int getID()
    {
        return this.ID;
    }

	public void meltDown()
	{
		this.cleanUpArray();
		
		for (int i = 0; i < conductors.size(); i++)
        {
			conductors.get(i).onConductorMelt();
        }
	}
	
	public double getLowestAmpConductor()
	{
		double lowestAmp = 0;
		
		for(IConductor conductor : conductors)
		{
			if(lowestAmp == 0 || conductor.getMaxAmps() < lowestAmp)
			{
				lowestAmp = conductor.getMaxAmps();
			}
		}
		
		return lowestAmp;
	}
}
