package mekanism.common.util;

import mekanism.common.tile.TileEntityContainerBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;

public final class FluidContainerUtils 
{
	public static boolean isFluidContainer(ItemStack stack)
	{
		return stack != null && stack.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null);
	}
	
	public static FluidStack extractFluid(FluidTank tileTank, ItemStack container)
	{
		return extractFluid(tileTank, container, FluidChecker.check(tileTank.getFluid()));
	}
	
	public static FluidStack extractFluid(FluidTank tileTank, ItemStack container, FluidChecker checker)
	{
		return extractFluid(tileTank.getCapacity()-tileTank.getFluidAmount(), container, checker);
	}
	
	public static FluidStack extractFluid(int needed, ItemStack container, FluidChecker checker)
	{
		IFluidHandler handler = FluidUtil.getFluidHandler(container);
		
		if(handler == null || handler.drain(1, false) == null)
		{
			return null;
		}
		
		if(checker != null && !checker.isValid(handler.drain(1, false).getFluid()))
		{
			return null;
		}
		
		return handler.drain(needed, true);
	}
	
	public static int insertFluid(FluidTank tileTank, ItemStack container)
	{
		return insertFluid(tileTank.getFluid(), container);
	}
	
	public static int insertFluid(FluidStack fluid, ItemStack container)
	{
		IFluidHandler handler = FluidUtil.getFluidHandler(container);
		
		if(fluid == null)
		{
			return 0;
		}
		
		return handler.fill(fluid, true);
	}
	
	public static void handleContainerItemFill(TileEntityContainerBlock tileEntity, FluidTank tank, int inSlot, int outSlot)
	{
		tank.setFluid(handleContainerItemFill(tileEntity, tileEntity.inventory, tank.getFluid(), inSlot, outSlot));
	}
	
	public static FluidStack handleContainerItemFill(TileEntity tileEntity, ItemStack[] inventory, FluidStack stack, int inSlot, int outSlot)
	{
		if(stack != null)
		{
			int prev = stack.amount;
			
			stack.amount -= insertFluid(stack, inventory[inSlot]);
			
			if(prev == stack.amount || stack.amount == 0)
			{
				if(inventory[outSlot] == null)
				{
					inventory[outSlot] = inventory[inSlot].copy();
					inventory[inSlot] = null;
				}
			}
			
			if(stack.amount == 0)
			{
				stack = null;
			}
			
			tileEntity.markDirty();
		}
		
		return stack;
	}
	
	public static void handleContainerItemEmpty(TileEntityContainerBlock tileEntity, FluidTank tank, int inSlot, int outSlot)
	{
		handleContainerItemEmpty(tileEntity, tank, inSlot, outSlot, null);
	}
	
	public static void handleContainerItemEmpty(TileEntityContainerBlock tileEntity, FluidTank tank, int inSlot, int outSlot, FluidChecker checker)
	{
		tank.setFluid(handleContainerItemEmpty(tileEntity, tileEntity.inventory, tank.getFluid(), tank.getCapacity()-tank.getFluidAmount(), inSlot, outSlot, checker));
	}
	
	public static FluidStack handleContainerItemEmpty(TileEntity tileEntity, ItemStack[] inventory, FluidStack stored, int needed, int inSlot, int outSlot, final FluidChecker checker)
	{
		final Fluid storedFinal = stored != null ? stored.getFluid() : null;
		
		FluidStack ret = extractFluid(needed, inventory[inSlot], new FluidChecker() {
			@Override
			public boolean isValid(Fluid f)
			{
				return (checker == null || checker.isValid(f)) && (storedFinal == null || storedFinal == f);
			}
		});
		
		if(ret != null)
		{
			if(stored == null)
			{
				stored = ret;
			}
			else {
				stored.amount += ret.amount;
			}
			
			needed -= ret.amount;
			
			tileEntity.markDirty();
		}
		
		if(FluidUtil.getFluidContained(inventory[inSlot]) == null || needed == 0)
		{
			if(inventory[outSlot] == null)
			{
				inventory[outSlot] = inventory[inSlot].copy();
				inventory[inSlot] = null;
				
				tileEntity.markDirty();
			}
		}
		
		return stored;
	}
	
	public static enum ContainerEditMode
	{
		BOTH("fluidedit.both"),
		FILL("fluidedit.fill"),
		EMPTY("fluidedit.empty");
		
		private String display;

		public String getDisplay()
		{
			return LangUtils.localize(display);
		}

		private ContainerEditMode(String s)
		{
			display = s;
		}
	}
	
	public static class FluidChecker
	{
		public boolean isValid(Fluid f)
		{
			return true;
		}
		
		public static FluidChecker check(FluidStack fluid)
		{
			final Fluid type = fluid != null ? fluid.getFluid() : null;
			
			return new FluidChecker() {
				@Override
				public boolean isValid(Fluid f)
				{
					return type == null || type == f;
				}
			};
		}
		
		public static FluidChecker check(final Fluid type)
		{
			return new FluidChecker() {
				@Override
				public boolean isValid(Fluid f)
				{
					return type == null || type == f;
				}
			};
		}
	}
}
