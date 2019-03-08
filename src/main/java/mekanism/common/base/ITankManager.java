package mekanism.common.base;

import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasTank;
import mekanism.common.item.ItemGaugeDropper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidUtil;

public interface ITankManager 
{
	Object[] getTanks();
	
	class DropperHandler
	{
		public static void useDropper(EntityPlayer player, Object tank, int button)
		{
			ItemStack stack = player.inventory.getItemStack();
			
			if(stack.isEmpty() || !(stack.getItem() instanceof ItemGaugeDropper))
			{
				return;
			}
			
			ItemGaugeDropper dropper = (ItemGaugeDropper)stack.getItem();
			
			if(!stack.isEmpty())
			{
				if(tank instanceof GasTank)
				{
					GasTank gasTank = (GasTank)tank;
					int dropperStored = dropper.getGas(stack) != null ? dropper.getGas(stack).amount : 0;
					
					if(dropper.getGas(stack) != null && gasTank.getGas() != null && !dropper.getGas(stack).isGasEqual(gasTank.getGas()))
					{
						return;
					}
					
					if(button == 0) //Insert gas into dropper
					{
						if(FluidUtil.getFluidContained(stack) != null || gasTank.getGas() == null)
						{
							return;
						}
						
						int toInsert = Math.min(gasTank.getStored(), ItemGaugeDropper.CAPACITY-dropperStored);
						GasStack drawn = gasTank.draw(toInsert, true);
						
						if(drawn != null)
						{
							dropper.setGas(stack, new GasStack(drawn.getGas(), dropperStored + drawn.amount));
						}
						
						((EntityPlayerMP)player).sendContainerToPlayer(player.openContainer);
					}
					else if(button == 1) //Extract gas from dropper
					{
						if(FluidUtil.getFluidContained(stack) != null || gasTank.getNeeded() == 0)
						{
							return;
						}
						
						int toExtract = Math.min(gasTank.getNeeded(), dropperStored);
						toExtract = gasTank.receive(new GasStack(dropper.getGas(stack).getGas(), toExtract), true);
						dropper.setGas(stack, new GasStack(dropper.getGas(stack).getGas(), dropperStored-toExtract));
						
						((EntityPlayerMP)player).sendContainerToPlayer(player.openContainer);
					}
					else if(button == 2) //Dump the tank
					{
						gasTank.setGas(null);
					}
				}
				else if(tank instanceof FluidTank)
				{
					FluidTank fluidTank = (FluidTank)tank;
					int dropperStored = FluidUtil.getFluidContained(stack) != null ? FluidUtil.getFluidContained(stack).amount : 0;
					
					if(FluidUtil.getFluidContained(stack) != null && fluidTank.getFluid() != null && !FluidUtil.getFluidContained(stack).isFluidEqual(fluidTank.getFluid()))
					{
						return;
					}
					
					if(button == 0) //Insert fluid into dropper
					{
						if(dropper.getGas(stack) != null || fluidTank.getFluid() == null)
						{
							return;
						}
						
						int toInsert = Math.min(fluidTank.getFluidAmount(), ItemGaugeDropper.CAPACITY-dropperStored);
						FluidUtil.getFluidHandler(stack).fill(fluidTank.drain(toInsert, true), true);

						((EntityPlayerMP)player).sendContainerToPlayer(player.openContainer);
					}
					else if(button == 1) //Extract fluid from dropper
					{
						if(dropper.getGas(stack) != null || fluidTank.getCapacity()-fluidTank.getFluidAmount() == 0)
						{
							return;
						}
						
						int toExtract = Math.min(fluidTank.getCapacity()-fluidTank.getFluidAmount(), dropperStored);
						fluidTank.fill(FluidUtil.getFluidHandler(stack).drain(toExtract, true), true);
						
						((EntityPlayerMP)player).sendContainerToPlayer(player.openContainer);
					}
					else if(button == 2) //Dump the tank
					{
						fluidTank.setFluid(null);
					}
				}
			}
		}
	}
}
