package mekanism.common.tile;

import java.util.ArrayList;

import mekanism.api.EnumColor;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasTank;
import mekanism.api.gas.IGasHandler;
import mekanism.api.gas.ITubeConnection;
import mekanism.common.Mekanism;
import mekanism.common.SideData;
import mekanism.common.recipe.RecipeHandler;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.tile.component.TileComponentUpgrade;
import mekanism.common.util.ChargeUtils;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeDirection;

import com.google.common.io.ByteArrayDataInput;

import dan200.computer.api.IComputerAccess;
import dan200.computer.api.ILuaContext;

public abstract class TileEntityAdvancedElectricMachine extends TileEntityBasicMachine implements IGasHandler, ITubeConnection
{
	/** How much secondary energy (fuel) this machine uses per tick. */
	public int SECONDARY_ENERGY_PER_TICK;
	
	public int MAX_SECONDARY_ENERGY = 200;
	
	public GasTank gasTank;
	
	/**
	 * Advanced Electric Machine -- a machine like this has a total of 4 slots. Input slot (0), fuel slot (1), output slot (2), 
	 * energy slot (3), and the upgrade slot (4). The machine will not run if it does not have enough electricity, or if it doesn't have enough
	 * fuel ticks.
	 * 
	 * @param soundPath - location of the sound effect
	 * @param name - full name of this machine
	 * @param location - GUI texture path of this machine
	 * @param perTick - how much energy this machine uses per tick.
	 * @param secondaryPerTick - how much secondary energy (fuel) this machine uses per tick.
	 * @param ticksRequired - how many ticks it takes to smelt an item.
	 * @param maxEnergy - maximum amount of energy this machine can hold.
	 * @param maxSecondaryEnergy - maximum amount of secondary energy (fuel) this machine can hold.
	 */
	public TileEntityAdvancedElectricMachine(String soundPath, String name, ResourceLocation location, double perTick, int secondaryPerTick, int ticksRequired, double maxEnergy)
	{
		super(soundPath, name, location, perTick, ticksRequired, maxEnergy);
		
		sideOutputs.add(new SideData(EnumColor.GREY, InventoryUtils.EMPTY));
		sideOutputs.add(new SideData(EnumColor.DARK_RED, new int[] {0}));
		sideOutputs.add(new SideData(EnumColor.PURPLE, new int[] {1}));
		sideOutputs.add(new SideData(EnumColor.DARK_BLUE, new int[] {2}));
		sideOutputs.add(new SideData(EnumColor.DARK_GREEN, new int[] {3}));
		sideOutputs.add(new SideData(EnumColor.ORANGE, new int[] {4}));
		
		sideConfig = new byte[] {2, 1, 0, 4, 5, 3};
		
		gasTank = new GasTank(MAX_SECONDARY_ENERGY);
		
		inventory = new ItemStack[5];
		
		SECONDARY_ENERGY_PER_TICK = secondaryPerTick;
		
		upgradeComponent = new TileComponentUpgrade(this, 4);
		ejectorComponent = new TileComponentEjector(this, sideOutputs.get(3));
	}
    
    /**
     * Gets the amount of ticks the declared itemstack can fuel this machine.
     * @param itemstack - itemstack to check with
     * @return fuel ticks
     */
    public abstract GasStack getItemGas(ItemStack itemstack);
	
    @Override
	public void onUpdate()
	{
		super.onUpdate();
		
		if(!worldObj.isRemote)
		{
			ChargeUtils.discharge(3, this);
			
			handleSecondaryFuel();
			
			if(canOperate() && MekanismUtils.canFunction(this) && getEnergy() >= MekanismUtils.getEnergyPerTick(getSpeedMultiplier(), getEnergyMultiplier(), ENERGY_PER_TICK) && gasTank.getStored() >= SECONDARY_ENERGY_PER_TICK)
			{
			    setActive(true);
			    
				operatingTicks++;
				
				gasTank.draw(SECONDARY_ENERGY_PER_TICK, true);
				electricityStored -= MekanismUtils.getEnergyPerTick(getSpeedMultiplier(), getEnergyMultiplier(), ENERGY_PER_TICK);
				
				if(operatingTicks >= MekanismUtils.getTicks(getSpeedMultiplier(), TICKS_REQUIRED))
				{
					operate();
					
					operatingTicks = 0;
				}
			}
			else {
				if(prevEnergy >= getEnergy())
				{
					setActive(false);
				}
			}
			
			if(!canOperate())
			{
				operatingTicks = 0;
			}
			
			prevEnergy = getEnergy();
		}
	}
    
    public void handleSecondaryFuel()
    {
		if(inventory[1] != null)
		{
			GasStack stack = getItemGas(inventory[1]);
			int gasNeeded = gasTank.getNeeded();
			
			if(stack != null && stack.amount <= gasNeeded)
			{
				gasTank.receive(stack, true);
				
				inventory[1].stackSize--;
				
				if(inventory[1].stackSize == 0)
				{
					inventory[1] = null;
				}
			}
		}
    }
    
	@Override
	public boolean isItemValidForSlot(int slotID, ItemStack itemstack)
	{
		if(slotID == 2)
		{
			return false;
		}
		else if(slotID == 4)
		{
			return itemstack.itemID == Mekanism.SpeedUpgrade.itemID || itemstack.itemID == Mekanism.EnergyUpgrade.itemID;
		}
		else if(slotID == 0)
		{
			return RecipeHandler.getOutput(itemstack, false, getRecipes()) != null;
		}
		else if(slotID == 3)
		{
			return ChargeUtils.canBeDischarged(itemstack);
		}
		else if(slotID == 1)
		{
			return getItemGas(itemstack) != null;
		}
		
		return false;
	}

    @Override
    public void operate()
    {
        ItemStack itemstack = RecipeHandler.getOutput(inventory[0], true, getRecipes());

        if(inventory[0].stackSize <= 0)
        {
            inventory[0] = null;
        }

        if(inventory[2] == null)
        {
            inventory[2] = itemstack;
        }
        else {
            inventory[2].stackSize += itemstack.stackSize;
        }
        
        onInventoryChanged();
        ejectorComponent.onOutput();
    }

    @Override
    public boolean canOperate()
    {
        if(inventory[0] == null)
        {
            return false;
        }

        ItemStack itemstack = RecipeHandler.getOutput(inventory[0], false, getRecipes());

        if(itemstack == null)
        {
            return false;
        }

        if(inventory[2] == null)
        {
            return true;
        }

        if(!inventory[2].isItemEqual(itemstack))
        {
            return false;
        }
        else {
            return inventory[2].stackSize + itemstack.stackSize <= inventory[2].getMaxStackSize();
        }
    }
    
	@Override
	public void handlePacketData(ByteArrayDataInput dataStream)
	{
		super.handlePacketData(dataStream);
		
		if(dataStream.readBoolean())
		{
			gasTank.setGas(new GasStack(dataStream.readInt(), dataStream.readInt()));
		}
		else {
			gasTank.setGas(null);
		}
	}
	
	@Override
	public ArrayList getNetworkedData(ArrayList data)
	{
		super.getNetworkedData(data);
		
		if(gasTank.getGas() != null)
		{
			data.add(true);
			data.add(gasTank.getGas().getGas().getID());
			data.add(gasTank.getStored());
		}
		else {
			data.add(false);
		}
		
		return data;
	}
	
    @Override
    public void readFromNBT(NBTTagCompound nbtTags)
    {
    	super.readFromNBT(nbtTags);
    	
        gasTank.read(nbtTags.getCompoundTag("gasTank"));
    }

    @Override
    public void writeToNBT(NBTTagCompound nbtTags)
    {
        super.writeToNBT(nbtTags);
        
        nbtTags.setCompoundTag("gasTank", gasTank.write(new NBTTagCompound()));
    }
	
	/**
	 * Gets the scaled secondary energy level for the GUI.
	 * @param i - multiplier
	 * @return scaled secondary energy
	 */
	public int getScaledGasLevel(int i)
	{
		return gasTank.getStored()*i / gasTank.getMaxGas();
	}
	
	@Override
	public boolean canExtractItem(int slotID, ItemStack itemstack, int side)
	{
		if(slotID == 3)
		{
			return ChargeUtils.canBeOutputted(itemstack, false);
		}
		else if(slotID == 2)
		{
			return true;
		}
		
		return false;
	}
	
	@Override
	public boolean canTubeConnect(ForgeDirection side)
	{
		return false;
	}

	@Override
	public int receiveGas(ForgeDirection side, GasStack stack)
	{
		return 0;
	}

	@Override
	public GasStack drawGas(ForgeDirection side, int amount)
	{
		return null;
	}

	@Override
	public boolean canReceiveGas(ForgeDirection side, Gas type)
	{
		return false;
	}

	@Override
	public boolean canDrawGas(ForgeDirection side, Gas type)
	{
		return false;
	}

	@Override
	public String[] getMethodNames() 
	{
		return new String[] {"getStored", "getSecondaryStored", "getProgress", "isActive", "facing", "canOperate", "getMaxEnergy", "getEnergyNeeded"};
	}

	@Override
	public Object[] callMethod(IComputerAccess computer, ILuaContext context, int method, Object[] arguments) throws Exception 
	{
		switch(method)
		{
			case 0:
				return new Object[] {getEnergy()};
			case 1:
				return new Object[] {gasTank.getStored()};
			case 2:
				return new Object[] {operatingTicks};
			case 3:
				return new Object[] {isActive};
			case 4:
				return new Object[] {facing};
			case 5:
				return new Object[] {canOperate()};
			case 6:
				return new Object[] {MekanismUtils.getMaxEnergy(getEnergyMultiplier(), getMaxEnergy())};
			case 7:
				return new Object[] {(MekanismUtils.getMaxEnergy(getEnergyMultiplier(), getMaxEnergy())-getEnergy())};
			default:
				System.err.println("[Mekanism] Attempted to call unknown method with computer ID " + computer.getID());
				return new Object[] {"Unknown command."};
		}
	}
}
