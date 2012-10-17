package universalelectricity.electricity;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.minecraft.src.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import universalelectricity.Ticker;
import universalelectricity.implement.IConductor;
import universalelectricity.implement.IElectricityReceiver;
import universalelectricity.prefab.TileEntityConductor;
import universalelectricity.prefab.Vector3;
import cpw.mods.fml.common.TickType;

/**
 * This class is used to manage electricity transferring and flow. It is also used to call updates on UE tile entities.
 * 
 * Electricity Manager is made for each world so it doesn't conflict with electricity devices in different dimensions.
 * @author Calclavia
 *
 */
public class ElectricityManager
{
	/**
	 * ElectricityManager exists on both client and server side.
	 * Rely on the server side one as it is more accurate! Client side only simulates.
	 */
	public static ElectricityManager instance;
	
    private List<IElectricityReceiver> electricityReceivers = new ArrayList<IElectricityReceiver>();
    private List<IConductor> electricConductors = new ArrayList<IConductor>();

    private List<ElectricityTransferData> electricityTransferQueue = new ArrayList<ElectricityTransferData>();
    private List<ElectricityNetwork> electricityNetworks = new ArrayList<ElectricityNetwork>();
    private int maxConnectionID = 0;
    
    public ElectricityManager()
    {
    	System.out.println("Universal Electricity's Electricity Manager Initiated.");
    }
    
    /**
     * Registers an electricity consumer for it to receive electricity.
     * Call it in your consumer's tile entity constructor like this:
     * ElectricityManager.registerConsumer(this);
     * @param newUnit - The consumer to be registered.
     */
    public void registerElectricUnit(IElectricityReceiver newUnit)
    {
        if (!this.electricityReceivers.contains(newUnit))
        {
        	this.electricityReceivers.add(newUnit);
        }
    }

    /**
     * Registers a UE conductor
     * @param conductor - The conductor tile entity
     * @return - The ID of the connection line that is assigned to this conductor
     */
    public void registerConductor(TileEntityConductor newConductor)
    {
        cleanUpConnections();
        this.electricityNetworks.add(new ElectricityNetwork(getMaxConnectionID(), newConductor));

        if (!this.electricConductors.contains(newConductor))
        {
        	this.electricConductors.add(newConductor);
        }
    }

    /**
     * Merges two connection lines together into one.
     * @param ID1 - ID of connection line
     * @param ID2 - ID of connection line
     */
    public void mergeConnection(int ID1, int ID2)
    {
        if(ID1 != ID2)
        {
            ElectricityNetwork connection1 = getConnectionByID(ID1);
            ElectricityNetwork connection2 = getConnectionByID(ID2);
            
            if(connection1 != null && connection2 != null)
            {
	            connection1.conductors.addAll(connection2.conductors);
	            connection1.setID(ID1);
	            this.electricityNetworks.remove(connection2);
            }
            else
            {
            	System.err.println("Failed to merge Universal Electricity wire connections!");
            }
        }
    }

    /**
     * Separate one connection line into two different ones between two conductors.
     * This function does this by resetting all wires in the connection line and
     * making them each reconnect.
     * @param conductorA - existing conductor
     * @param conductorB - broken/invalid conductor
     */
    public void splitConnection(IConductor conductorA, IConductor conductorB)
    {
        ElectricityNetwork connection = getConnectionByID(conductorA.getConnectionID());
        connection.cleanUpArray();

        for(IConductor conductor : connection.conductors)
        {
            conductor.reset();
        }
        
        for(IConductor conductor : connection.conductors)
        {
            for (byte i = 0; i < 6; i++)
            {
                conductor.updateConnectionWithoutSplit(Vector3.getConnectorFromSide(conductor.getWorld(), new Vector3(((TileEntity)conductor).xCoord, ((TileEntity)conductor).yCoord, ((TileEntity)conductor).zCoord), ForgeDirection.getOrientation(i)), ForgeDirection.getOrientation(i));
            }
        }
    }

    /**
     * Gets a electricity wire connection line by it's connection ID
     * @param ID
     * @return
     */
    public ElectricityNetwork getConnectionByID(int ID)
    {
        cleanUpConnections();

        for (int i = 0; i < this.electricityNetworks.size(); i++)
        {
            if (this.electricityNetworks.get(i).getID() == ID)
            {
                return this.electricityNetworks.get(i);
            }
        }

        return null;
    }

    /**
     * Clean up and remove useless connections
     */
    public void cleanUpConnections()
    {
        for (int i = 0; i < this.electricityNetworks.size(); i++)
        {
        	this.electricityNetworks.get(i).cleanUpArray();

            if (this.electricityNetworks.get(i).conductors.size() == 0)
            {
            	this.electricityNetworks.remove(i);
            }
        }
    }

    /**
     * Get the highest connection ID. Use this to assign new wire connection lines
     * @return
     */
    public int getMaxConnectionID()
    {
    	this.maxConnectionID ++;
        return this.maxConnectionID;
    }

    /**
     * Produces electricity into a specific wire which will be distributed across the electricity network.
     * @param sender - The machine sending the electricity.
     * @param targetConductor - The conductor receiving the electricity (or connected to the machine).
     * @param amps - The amount of amps this machine is sending.
     * @param voltage 0 The amount of volts this machine is sending.
     */
    public void produceElectricity(TileEntity sender, IConductor targetConductor, double amps, double voltage)
    {
        if(targetConductor != null && amps > 0 && voltage > 0)
        {
            //Find a path between this conductor and all connected units and try to send the electricity to them directly
            ElectricityNetwork electricityNetwork = this.getConnectionByID(targetConductor.getConnectionID());

            if(electricityNetwork != null)
            {
                List<IElectricityReceiver> allElectricUnitsInLine = electricityNetwork.getConnectedReceivers();
                double leftOverAmps = amps;

                for (IConductor conductor : electricityNetwork.conductors)
                {
                    for (byte i = 0; i < conductor.getConnectedBlocks().length; i++)
                    {
                        TileEntity tileEntity = conductor.getConnectedBlocks()[i];

                        if (tileEntity != null)
                        {
                            if (tileEntity instanceof IElectricityReceiver)
                            {
                                IElectricityReceiver receiver = (IElectricityReceiver)tileEntity;

                                if (Math.ceil(receiver.wattRequest()) > 0 && receiver.canReceiveFromSide(ForgeDirection.getOrientation(i).getOpposite()))
                                {
                                    double transferAmps = Math.max(0, Math.min(leftOverAmps, Math.min(amps / allElectricUnitsInLine.size(), ElectricInfo.getAmps(Math.ceil(receiver.wattRequest()), receiver.getVoltage()) )));
                                    leftOverAmps -= transferAmps;
                                    
                                    //Calculate electricity loss
                                    double distance = Vector3.distance(Vector3.get(sender), Vector3.get((TileEntity)receiver));
                                    double ampsReceived = transferAmps - (transferAmps * transferAmps * targetConductor.getResistance() * distance)/voltage;
                                    double voltsReceived = voltage - (transferAmps * targetConductor.getResistance() * distance);

                                    this.electricityTransferQueue.add(new ElectricityTransferData(sender, receiver, electricityNetwork, ForgeDirection.getOrientation(i).getOpposite(), ampsReceived, voltsReceived));
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Checks if the current connection line needs electricity
     * @return - The amount of watts this connection line needs
     */
    public double getElectricityRequired(int ID)
    {
        ElectricityNetwork connection = this.getConnectionByID(ID);
        double need = 0;

        if (connection != null)
        {
            for (IConductor conductor : connection.conductors)
            {
                for (byte i = 0; i < conductor.getConnectedBlocks().length; i++)
                {
                    TileEntity tileEntity = conductor.getConnectedBlocks()[i];

                    if (tileEntity != null)
                    {
                        if (tileEntity instanceof IElectricityReceiver)
                        {
                            IElectricityReceiver electricUnit = (IElectricityReceiver)tileEntity;

                            if (electricUnit.canReceiveFromSide(ForgeDirection.getOrientation(i).getOpposite()))
                            {
                                need += Math.ceil(electricUnit.wattRequest());
                            }
                        }
                    }
                }
            }
        }

        return need;
    }

    /**
     * This function is called to refresh all conductors in UE
     */
    public void refreshConductors()
    {
		for(int j = 0; j < this.electricConductors.size(); j ++)
        {
        	IConductor conductor = this.electricConductors.get(j);
            conductor.refreshConnectedBlocks();
        }
    }
    
    /**
     * Clean up and remove useless connections
     */
    public void cleanUpElectricityReceivers()
    {
    	try
    	{
	        for (int i = 0; i < this.electricityReceivers.size(); i++)
	        {
	        	IElectricityReceiver electricUnit = electricityReceivers.get(i);
	        	
	            //Cleanup useless units
	            if (electricUnit == null)
	            {
	                electricityReceivers.remove(electricUnit);
	            }
	            else if (((TileEntity)electricUnit).isInvalid())
	            {
	                electricityReceivers.remove(electricUnit);
	            }
	        }
    	}
    	catch(Exception e)
		{
    		System.err.println("Failed to clean up electricity receivers.");
			e.printStackTrace();
		}
    }

	public void tickStart(EnumSet<TickType> type, Object... tickData)
	{
		try
		{
			HashMap conductorAmpData = new HashMap<ElectricityNetwork, Double>();
			
			for (int i = 0; i < electricityTransferQueue.size(); i ++)
	        {
	        	if(electricityTransferQueue.get(i) != null)
	        	{
	            	if(electricityTransferQueue.get(i).isValid())
	            	{
	            		double amps = electricityTransferQueue.get(i).amps;
	            		
	            		if(conductorAmpData.containsKey(electricityTransferQueue.get(i).network))
	            		{
	            			amps += (Double)conductorAmpData.get(electricityTransferQueue.get(i).network);
	            		}
	            		
	            		conductorAmpData.put(electricityTransferQueue.get(i).network, amps);
	                	electricityTransferQueue.get(i).receiver.onReceive(electricityTransferQueue.get(i).sender, electricityTransferQueue.get(i).amps, electricityTransferQueue.get(i).voltage, electricityTransferQueue.get(i).side);
	            	}
	        	}
	        	
	        	electricityTransferQueue.remove(i);
	        }
			
			Iterator it = conductorAmpData.entrySet().iterator();
			
		    while (it.hasNext())
		    {
		        Map.Entry pairs = (Map.Entry)it.next();
		        
		        if(pairs.getKey() != null && pairs.getValue() != null)
		        {
		        	if(pairs.getKey() instanceof ElectricityNetwork && pairs.getValue() instanceof Double)
		        	{
		        		if(((Double)pairs.getValue()) > ((ElectricityNetwork)pairs.getKey()).getLowestAmpConductor())
		        		{
		        			((ElectricityNetwork)pairs.getKey()).meltDown();
		        		}
		        	}
		        }
		        
		        it.remove();
		    }
		}
		catch(Exception e)
		{
			System.err.println("Failed to transfer electricity to receivers.");
			e.printStackTrace();
		}
	}
	
	public void tickEnd(EnumSet<TickType> type, Object... tickData)
	{
		if(Ticker.inGameTicks == 0)
		{
			this.refreshConductors();
		}
	}
}