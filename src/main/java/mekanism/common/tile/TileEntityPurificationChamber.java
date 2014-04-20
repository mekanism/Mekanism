package mekanism.common.tile;

import java.util.Map;

import mekanism.api.gas.Gas;
import mekanism.api.gas.GasRegistry;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasTransmission;
import mekanism.api.gas.IGasHandler;
import mekanism.api.gas.IGasItem;
import mekanism.api.gas.ITubeConnection;
import mekanism.client.gui.GuiProgress.ProgressBar;
import mekanism.common.Mekanism;
import mekanism.common.block.BlockMachine.MachineType;
import mekanism.common.recipe.RecipeHandler.Recipe;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.ForgeDirection;

public class TileEntityPurificationChamber extends TileEntityAdvancedElectricMachine implements IGasHandler, ITubeConnection
{
	public TileEntityPurificationChamber()
	{
		super("PurificationChamber.ogg", "PurificationChamber", Mekanism.purificationChamberUsage, 1, 200, MachineType.PURIFICATION_CHAMBER.baseEnergy);
	}

	@Override
	public Map getRecipes()
	{
		return Recipe.PURIFICATION_CHAMBER.get();
	}

	@Override
	public GasStack getItemGas(ItemStack itemstack)
	{
		if(itemstack.isItemEqual(new ItemStack(Items.flint))) return new GasStack(GasRegistry.getGas("oxygen"), 10);
		if(itemstack.itemID == Mekanism.GasTank.blockID && ((IGasItem)itemstack.getItem()).getGas(itemstack) != null &&
				((IGasItem)itemstack.getItem()).getGas(itemstack).getGas() == GasRegistry.getGas("oxygen")) return new GasStack(GasRegistry.getGas("oxygen"), 1);

		return null;
	}

	@Override
	public int receiveGas(ForgeDirection side, GasStack stack)
	{
		if(stack.getGas() == GasRegistry.getGas("oxygen"))
		{
			return gasTank.receive(stack, true);
		}

		return 0;
	}

	@Override
	public boolean canReceiveGas(ForgeDirection side, Gas type)
	{
		return type == GasRegistry.getGas("oxygen");
	}

	@Override
	public void handleSecondaryFuel()
	{
		if(inventory[1] != null && gasTank.getNeeded() > 0 && inventory[1].getItem() instanceof IGasItem)
		{
			GasStack removed = GasTransmission.removeGas(inventory[1], GasRegistry.getGas("oxygen"), gasTank.getNeeded());
			gasTank.receive(removed, true);
			return;
		}

		super.handleSecondaryFuel();
	}

	@Override
	public boolean canTubeConnect(ForgeDirection side)
	{
		return true;
	}

	@Override
	public boolean isValidGas(Gas gas)
	{
		return gas == GasRegistry.getGas("oxygen");
	}

	@Override
	public ProgressBar getProgressType()
	{
		return ProgressBar.RED;
	}
}
