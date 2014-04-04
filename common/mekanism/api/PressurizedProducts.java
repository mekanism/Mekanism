package mekanism.api;

import java.util.Random;

import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasTank;

import net.minecraft.item.ItemStack;

public class PressurizedProducts
{
	private static Random rand = new Random();

	private ItemStack probabilityOutput;
	private double probability;

	private GasStack gasOutput;

	public PressurizedProducts(ItemStack item, double chance, GasStack gas)
	{
		probabilityOutput = item;
		probability = chance;
		gasOutput = gas;
	}

	public void fillTank(GasTank tank)
	{
		tank.receive(gasOutput, true);
	}

	public void addProducts(ItemStack itemStack)
	{
		if(itemStack.isItemEqual(probabilityOutput) && rand.nextDouble() <= probability)
		{
			itemStack.stackSize += probabilityOutput.stackSize;
		}
	}

}
