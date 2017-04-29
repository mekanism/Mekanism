package mekanism.common.util;

import mekanism.common.tile.prefab.TileEntityContainerBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.ItemHandlerHelper;

public final class FluidContainerUtils 
{
	public static boolean isFluidContainer(ItemStack stack)
	{
		return stack != null && stack.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null);
	}
	
	public static FluidStack extractFluid(FluidTank tileTank, TileEntityContainerBlock tile, int slotID)
	{
		return extractFluid(tileTank, tile, slotID, FluidChecker.check(tileTank.getFluid()));
	}
	
	public static FluidStack extractFluid(FluidTank tileTank, TileEntityContainerBlock tile, int slotID, FluidChecker checker)
	{
		return extractFluid(tileTank.getCapacity()-tileTank.getFluidAmount(), tile.inventory, slotID, checker);
	}
	
	public static FluidStack extractFluid(int needed, ItemStack[] inv, int slotID, FluidChecker checker)
	{
		IFluidHandler handler = FluidUtil.getFluidHandler(inv[slotID]);
		
		if(handler == null || FluidUtil.getFluidContained(inv[slotID]) == null)
		{
			return null;
		}
		
		if(checker != null && !checker.isValid(FluidUtil.getFluidContained(inv[slotID]).getFluid()))
		{
			return null;
		}
		
		FluidStack ret = handler.drain(needed, true);
		inv[slotID] = inv[slotID].getItem().getContainerItem(inv[slotID]);
		
		return ret;
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
			ItemStack inputCopy = StackUtils.size(inventory[inSlot].copy(), 1);
			
			int drained = insertFluid(stack, inputCopy);
			inputCopy = inputCopy.getItem().getContainerItem(inputCopy);
			
			if(inventory[outSlot] != null && (!ItemHandlerHelper.canItemStacksStack(inventory[outSlot], inputCopy) || inventory[outSlot].stackSize == inventory[outSlot].getMaxStackSize()))
			{
				return stack;
			}
			
			stack.amount -= drained;
			
			if(inventory[outSlot] == null)
			{
				inventory[outSlot] = inputCopy;
			}
			else if(ItemHandlerHelper.canItemStacksStack(inventory[outSlot], inputCopy))
			{
				inventory[outSlot].stackSize++;
			}
			
			inventory[inSlot].stackSize--;
			
			if(inventory[inSlot].stackSize == 0)
			{
				inventory[inSlot] = null;
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
		final ItemStack input = StackUtils.size(inventory[inSlot].copy(), 1);
		
		FluidStack ret = extractFluid(needed, inventory, inSlot, new FluidChecker() {
			@Override
			public boolean isValid(Fluid f)
			{
				return (checker == null || checker.isValid(f)) && (storedFinal == null || storedFinal == f);
			}
		});
		
		ItemStack inputCopy = input.copy();
		
		if(inputCopy.stackSize == 0)
		{
			inputCopy = null;
		}
		
		if(FluidUtil.getFluidContained(inputCopy) == null && inputCopy != null)
		{
			if(inventory[outSlot] != null && (!ItemHandlerHelper.canItemStacksStack(inventory[outSlot], inputCopy) || inventory[outSlot].stackSize == inventory[outSlot].getMaxStackSize()))
			{
				return stored;
			}
		}
		
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
		
		if(FluidUtil.getFluidContained(inputCopy) == null || needed == 0)
		{
			if(inputCopy != null)
			{
				if(inventory[outSlot] == null)
				{
					inventory[outSlot] = inputCopy;
				}
				else if(ItemHandlerHelper.canItemStacksStack(inventory[outSlot], inputCopy))
				{
					inventory[outSlot].stackSize++;
				}
			}
			
			inventory[inSlot].stackSize--;
			
			if(inventory[inSlot].stackSize == 0)
			{
				inventory[inSlot] = null;
			}
			
			tileEntity.markDirty();
		}
		else {
			inventory[inSlot] = inputCopy;
		}
		
		return stored;
	}
	
	public static void handleContainerItem(TileEntityContainerBlock tileEntity, ContainerEditMode editMode, FluidTank tank, int inSlot, int outSlot)
	{
		handleContainerItem(tileEntity, editMode, tank, inSlot, outSlot, null);
	}
	
	public static void handleContainerItem(TileEntityContainerBlock tileEntity, ContainerEditMode editMode, FluidTank tank, int inSlot, int outSlot, FluidChecker checker)
	{
		tank.setFluid(handleContainerItem(tileEntity, tileEntity.inventory, editMode, tank.getFluid(), tank.getCapacity()-tank.getFluidAmount(), inSlot, outSlot, checker));
	}
	
	public static FluidStack handleContainerItem(TileEntity tileEntity, ItemStack[] inventory, ContainerEditMode editMode, FluidStack stack, int needed, int inSlot, int outSlot, final FluidChecker checker)
	{
		FluidStack fluidStack = FluidUtil.getFluidContained(inventory[inSlot]);
		
		if(editMode == ContainerEditMode.FILL || (editMode == ContainerEditMode.BOTH && fluidStack == null))
		{
			return handleContainerItemFill(tileEntity, inventory, stack, inSlot, outSlot);
		}
		else if(editMode == ContainerEditMode.EMPTY || (editMode == ContainerEditMode.BOTH && fluidStack != null))
		{
			return handleContainerItemEmpty(tileEntity, inventory, stack, needed, inSlot, outSlot, checker);
		}
		
		return stack;
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
