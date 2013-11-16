package calclavia.lib;

import ic2.api.item.Items;

import java.util.Arrays;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

import com.google.common.collect.Lists;

import cpw.mods.fml.common.FMLLog;

/**
 * Allows recipes that are compatible with UE -> IC2 -> Buildcraft.
 * 
 * @author Calclavia
 * 
 */
public class UniversalRecipes
{
	private static final String PREFIX = "calclavia:";
	/**
	 * Primary Metal: Steel
	 */
	public static final String PRIMARY_METAL = "ingotSteel";
	public static final String PRIMARY_PLATE = "plateSteel";

	/**
	 * Secondary Metal: Bronze
	 */
	public static final String SECONDARY_METAL = "ingotBronze";
	public static final String SECONDARY_PLATE = "plateBronze";

	/**
	 * Circuits
	 */
	public static final String CIRCUIT_T1 = PREFIX + "CIRCUIT_T1";
	public static final String CIRCUIT_T2 = PREFIX + "CIRCUIT_T2";
	public static final String CIRCUIT_T3 = PREFIX + "CIRCUIT_T3";

	/**
	 * Battery
	 */
	public static String ADVANCED_BATTERY = PREFIX + "ADVANCED_BATTERY";
	public static String BATTERY = PREFIX + "BATTERY";
	public static String BATTERY_BOX = PREFIX + "BATTERY_BOX";

	/**
	 * Misc
	 */
	public static final String WRENCH = PREFIX + "WRENCH";
	public static final String WIRE = PREFIX + "WIRE";
	public static final String MOTOR = PREFIX + "MOTOR";

	public static boolean isInit = false;

	public static void init()
	{
		if (!isInit)
		{
			// Metals
			/*
			 * register(PRIMARY_METAL, "ingotSteel", "ingotRefinedIron", new
			 * ItemStack(Item.ingotIron)); register(SECONDARY_METAL, "ingotBronze", new
			 * ItemStack(Item.brick));
			 */
			
			// Plates
			register(PRIMARY_PLATE, "plateSteel", Items.getItem("advancedAlloy"), new ItemStack(Block.blockIron));
			register(SECONDARY_PLATE, "plateBronze", Items.getItem("carbonPlate"), new ItemStack(Block.brick));
			// Miscs
			register(CIRCUIT_T1, "circuitBasic", Items.getItem("electronicCircuit"), new ItemStack(Block.torchRedstoneActive));
			register(CIRCUIT_T2, "circuitAdvanced", Items.getItem("advancedCircuit"), new ItemStack(Item.redstoneRepeater));
			register(CIRCUIT_T3, "circuitElite", Items.getItem("iridiumPlate"), new ItemStack(Item.comparator));

			register(ADVANCED_BATTERY, "advancedBattery", Items.getItem("energyCrystal"), "battery", new ItemStack(Item.redstoneRepeater));
			register(BATTERY, "battery", Items.getItem("reBattery"), new ItemStack(Item.redstoneRepeater));
			register(BATTERY_BOX, "batteryBox", Items.getItem("batBox"), new ItemStack(Block.blockGold));

			register(WRENCH, "wrench", Items.getItem("wrench"), new ItemStack(Item.axeIron));
			register(WIRE, "copperWire", "copperCableBlock", new ItemStack(Item.redstone));

			register(MOTOR, "motor", Items.getItem("generator"), new ItemStack(Block.pistonBase));

			isInit = true;
		}

	}

	public static void register(String name, Object... possiblities)
	{
		for (Object possiblity : possiblities)
		{
			if (possiblity instanceof ItemStack)
			{
				if (registerItemStacksToDictionary(name, (ItemStack) possiblity))
				{
					break;
				}

				continue;
			}
			else if (possiblity instanceof String)
			{
				if (registerItemStacksToDictionary(name, (String) possiblity))
				{
					break;
				}

				continue;
			}

			FMLLog.severe("Universal Recipes: Error Registering " + name);

		}
	}

	public static boolean registerItemStacksToDictionary(String name, List<ItemStack> itemStacks)
	{
		boolean returnValue = false;

		if (itemStacks != null)
		{
			if (itemStacks.size() > 0)
			{
				for (ItemStack stack : itemStacks.toArray(new ItemStack[0]))
				{
					if (stack != null)
					{
						OreDictionary.registerOre(name, stack);
						returnValue = true;
					}
				}
			}
		}

		return returnValue;
	}

	public static boolean registerItemStacksToDictionary(String name, ItemStack... itemStacks)
	{
		return registerItemStacksToDictionary(name, Arrays.asList(itemStacks));
	}

	public static boolean registerItemStacksToDictionary(String name, String stackName)
	{
		return registerItemStacksToDictionary(name, OreDictionary.getOres(stackName));
	}
}