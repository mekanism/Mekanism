package mekanism.common;

import ic2.api.Direction;
import ic2.api.ElectricItem;
import ic2.api.IElectricItem;
import ic2.api.energy.tile.IEnergySink;

import java.util.HashMap;
import java.util.Map;

import mekanism.api.IActiveState;
import mekanism.api.IMachineUpgrade;
import mekanism.api.Infusion;
import mekanism.api.InfusionType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.oredict.OreDictionary;
import universalelectricity.core.implement.IItemElectric;
import universalelectricity.core.implement.IJouleStorage;
import universalelectricity.core.implement.IVoltage;

import com.google.common.io.ByteArrayDataInput;

import dan200.computer.api.IComputerAccess;
import dan200.computer.api.IPeripheral;

public class TileEntityMetallurgicInfuser extends TileEntityElectricBlock implements IEnergySink, IJouleStorage, IVoltage, IPeripheral, IActiveState
{
	public static Map<Infusion, ItemStack> recipes = new HashMap<Infusion, ItemStack>();
	
	public InfusionType type = InfusionType.NONE;
	
	public int MAX_INFUSE = 1000;
	
	public double ENERGY_PER_TICK = 100;
	
	public int TICKS_REQUIRED = 200;
	
	public double currentMaxElectricity;
	
	public int currentTicksRequired;
	
	public int infuseStored;
	
	public int operatingTicks;
	
	public boolean isActive;
	
	public boolean prevActive;
	
	public TileEntityMetallurgicInfuser()
	{
		super("Metallurgic Infuser", 10000);
		
		inventory = new ItemStack[5];
		
		currentTicksRequired = TICKS_REQUIRED;
		currentMaxElectricity = MAX_ELECTRICITY;
	}
	
	@Override
	public void onUpdate()
	{
		super.onUpdate();
		
		boolean testActive = operatingTicks > 0;
		
		if(inventory[4] != null)
		{
			if(electricityStored < currentMaxElectricity)
			{
				if(inventory[4].getItem() instanceof IItemElectric)
				{
					IItemElectric electricItem = (IItemElectric)inventory[4].getItem();

					if (electricItem.canProduceElectricity())
					{
						double joulesNeeded = currentMaxElectricity-electricityStored;
						double joulesReceived = 0;
						
						if(electricItem.getVoltage() <= joulesNeeded)
						{
							joulesReceived = electricItem.onUse(electricItem.getVoltage(), inventory[4]);
						}
						else if(electricItem.getVoltage() > joulesNeeded)
						{
							joulesReceived = electricItem.onUse(joulesNeeded, inventory[4]);
						}
						
						setJoules(electricityStored + joulesReceived);
					}
				}
				else if(inventory[4].getItem() instanceof IElectricItem)
				{
					IElectricItem item = (IElectricItem)inventory[4].getItem();
					if(item.canProvideEnergy())
					{
						double gain = ElectricItem.discharge(inventory[4], (int)((MAX_ELECTRICITY - electricityStored)*Mekanism.TO_IC2), 3, false, false)*Mekanism.FROM_IC2;
						setJoules(electricityStored + gain);
					}
				}
			}
			if(inventory[4].itemID == Item.redstone.shiftedIndex && electricityStored <= (currentMaxElectricity-1000))
			{
				setJoules(electricityStored + 1000);
				--inventory[4].stackSize;
				
	            if (inventory[4].stackSize <= 0)
	            {
	                inventory[4] = null;
	            }
			}
		}
		
		if(inventory[0] != null && inventory[0].getItem() instanceof IMachineUpgrade)
		{
			int energyToAdd = 0;
			int ticksToRemove = 0;
			
			if(currentMaxElectricity == MAX_ELECTRICITY)
			{
				energyToAdd = ((IMachineUpgrade)inventory[0].getItem()).getEnergyBoost(inventory[0]);
			}
			
			if(currentTicksRequired == TICKS_REQUIRED)
			{
				ticksToRemove = ((IMachineUpgrade)inventory[0].getItem()).getTickReduction(inventory[0]);
			}
			
			currentMaxElectricity += energyToAdd;
			currentTicksRequired -= ticksToRemove;
		}
		else if(inventory[0] == null)
		{
			currentTicksRequired = TICKS_REQUIRED;
			currentMaxElectricity = MAX_ELECTRICITY;
		}
		
		if(inventory[1] != null)
		{
			if(inventory[1].itemID == Mekanism.CompressedCarbon.shiftedIndex && infuseStored+100 <= MAX_INFUSE)
			{
				if(type == InfusionType.NONE || type == InfusionType.COAL)
				{
					infuseStored += 100;
					inventory[1].stackSize--;
					type = InfusionType.COAL;
					
		            if (inventory[1].stackSize <= 0)
		            {
		                inventory[1] = null;
		            }
				}
			}
			else {
				if(type == InfusionType.NONE || type == InfusionType.TIN)
				{
					for(ItemStack itemStack : OreDictionary.getOres("dustTin"))
					{
						if(inventory[1].isItemEqual(itemStack) && infuseStored+100 <= MAX_INFUSE)
						{
							infuseStored += 100;
							inventory[1].stackSize--;
							type = InfusionType.TIN;
							
				            if (inventory[1].stackSize <= 0)
				            {
				                inventory[1] = null;
				            }
						}
					}
				}
			}
		}
		
		if(canOperate() && (operatingTicks+1) < currentTicksRequired)
		{
			++operatingTicks;
			electricityStored -= ENERGY_PER_TICK;
		}
		else if(canOperate() && (operatingTicks+1) >= currentTicksRequired)
		{
			if(!worldObj.isRemote)
			{
				operate();
			}
			operatingTicks = 0;
			electricityStored -= ENERGY_PER_TICK;
		}
		
		if(!canOperate())
		{
			operatingTicks = 0;
		}
		
		if(infuseStored <= 0)
		{
			infuseStored = 0;
			type = InfusionType.NONE;
		}
		
		if(!worldObj.isRemote)
		{
			if(testActive != operatingTicks > 0)
			{
				if(operatingTicks > 0)
				{
					setActive(true);
				}
				else if(!canOperate())
				{
					setActive(false);
				}
			}
		}
	}
	
	public void operate()
	{
        if (!canOperate())
        {
            return;
        }

        ItemStack itemstack = RecipeHandler.getOutput(Infusion.getInfusion(type, inventory[2]), true, recipes);
        
        infuseStored -= 10;

        if (inventory[2].stackSize <= 0)
        {
            inventory[2] = null;
        }

        if (inventory[3] == null)
        {
            inventory[3] = itemstack;
        }
        else
        {
            inventory[3].stackSize += itemstack.stackSize;
        }
	}
	
	public boolean canOperate()
	{
        if (inventory[2] == null)
        {
            return false;
        }
        
        if(electricityStored < ENERGY_PER_TICK)
        {
        	return false;
        }
        
        if(infuseStored-10 < 0)
        {
        	return false;
        }

        ItemStack itemstack = RecipeHandler.getOutput(Infusion.getInfusion(type, inventory[2]), false, recipes);

        if (itemstack == null)
        {
            return false;
        }

        if (inventory[3] == null)
        {
            return true;
        }

        if (!inventory[3].isItemEqual(itemstack))
        {
            return false;
        }
        else
        {
            return inventory[3].stackSize + itemstack.stackSize <= inventory[3].getMaxStackSize();
        }
	}
	
	public int getScaledInfuseLevel(int i)
	{
		return infuseStored*i / MAX_INFUSE;
	}
	
	public int getScaledEnergyLevel(int i)
	{
		return (int)(electricityStored*i / MAX_ELECTRICITY);
	}
	
	public int getScaledProgress(int i)
	{
		return operatingTicks*i / TICKS_REQUIRED;
	}

	@Override
	public void handlePacketData(INetworkManager network, Packet250CustomPayload packet, EntityPlayer player, ByteArrayDataInput dataStream)
	{
		if(!worldObj.isRemote)
		{
			try {
				infuseStored = dataStream.readInt();
			} catch (Exception e)
			{
				System.out.println("[Mekanism] Error while handling tile entity packet.");
				e.printStackTrace();
			}
			return;
		}
		
		try {
			facing = dataStream.readInt();
			electricityStored = dataStream.readDouble();
			currentTicksRequired = dataStream.readInt();
			currentMaxElectricity = dataStream.readDouble();
			isActive = dataStream.readBoolean();
			operatingTicks = dataStream.readInt();
			infuseStored = dataStream.readInt();
			type = InfusionType.getFromName(dataStream.readUTF());
			worldObj.markBlockForRenderUpdate(xCoord, yCoord, zCoord);
		} catch (Exception e)
		{
			System.out.println("[Mekanism] Error while handling tile entity packet.");
			e.printStackTrace();
		}
	}

	@Override
	public void sendPacket() 
	{
		PacketHandler.sendTileEntityPacketToClients(this, 0, facing, electricityStored, currentTicksRequired, currentMaxElectricity, isActive, operatingTicks, infuseStored, type.name);
	}

	@Override
	public void sendPacketWithRange() 
	{
		PacketHandler.sendTileEntityPacketToClients(this, 50, facing, electricityStored, currentTicksRequired, currentMaxElectricity, isActive, operatingTicks, infuseStored, type.name);
	}

	@Override
	public String getType()
	{
		return getInvName();
	}

	@Override
	public String[] getMethodNames() 
	{
		return new String[] {"getStored", "getProgress", "facing", "canOperate", "getMaxEnergy", "getEnergyNeeded"};
	}

	@Override
	public Object[] callMethod(IComputerAccess computer, int method, Object[] arguments) throws Exception 
	{
		switch(method)
		{
			case 0:
				return new Object[] {electricityStored};
			case 1:
				return new Object[] {operatingTicks};
			case 2:
				return new Object[] {facing};
			case 3:
				return new Object[] {canOperate()};
			case 4:
				return new Object[] {currentMaxElectricity};
			case 5:
				return new Object[] {(currentMaxElectricity-electricityStored)};
			default:
				System.err.println("[Mekanism] Attempted to call unknown method with computer ID " + computer.getID());
				return new Object[] {"Unknown command."};
		}
	}

	@Override
	public boolean canAttachToSide(int side)
	{
		return true;
	}

	@Override
	public void attach(IComputerAccess computer, String computerSide) {}

	@Override
	public void detach(IComputerAccess computer) {}

	@Override
	public double getVoltage() 
	{
		return 120;
	}

	@Override
	public double getJoules(Object... data) 
	{
		return electricityStored;
	}

	@Override
	public void setJoules(double joules, Object... data) 
	{
		electricityStored = Math.max(Math.min(joules, getMaxJoules()), 0);
	}

	@Override
	public double getMaxJoules(Object... data) 
	{
		return currentMaxElectricity;
	}

	@Override
	public boolean demandsEnergy()
	{
		return electricityStored < currentMaxElectricity;
	}
	
	@Override
    public void setActive(boolean active)
    {
    	isActive = active;
    	
    	if(prevActive != active)
    	{
    		sendPacket();
    	}
    	
    	prevActive = active;
    }
    
    @Override
    public boolean getActive()
    {
    	return isActive;
    }
    
	@Override
	public boolean acceptsEnergyFrom(TileEntity emitter, Direction direction) 
	{
		return true;
	}

	@Override
    public int injectEnergy(Direction direction, int i)
    {
		double givenEnergy = i*Mekanism.FROM_IC2;
    	double rejects = 0;
    	double neededEnergy = currentMaxElectricity-electricityStored;
    	
    	if(givenEnergy < neededEnergy)
    	{
    		electricityStored += givenEnergy;
    	}
    	else if(givenEnergy > neededEnergy)
    	{
    		electricityStored += neededEnergy;
    		rejects = givenEnergy-neededEnergy;
    	}
    	
    	return (int)(rejects*Mekanism.TO_IC2);
    }
}
