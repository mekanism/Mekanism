package mekanism.induction.common.wire;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;
import universalelectricity.core.vector.Vector3;

/**
 * An enumerator for different wire materials. The metadata of the wire determines the type of the
 * wire.
 * 
 * @author Calclavia
 * 
 */

public enum EnumWireMaterial
{
	COPPER(12.5f, 3, 2, new Vector3(184, 115, 51)), TIN(13, 2, 0.5f, new Vector3(132, 132, 130)),
	IRON(0.1f, 2, 4, new Vector3(97, 102, 105)),
	ALUMINUM(0.025f, 6, 0.15f, new Vector3(215, 205, 181)),
	SILVER(0.005f, 1, 2, new Vector3(192, 192, 192)),
	SUPERCONDUCTOR(0, 8, Float.MAX_VALUE, new Vector3(212, 175, 55));

	public final float resistance;
	public final float damage;
	public final float maxAmps;
	public final Vector3 color;
	private ItemStack wire;

	EnumWireMaterial(float resist, float electrocution, float max, Vector3 vec)
	{
		resistance = resist;
		damage = electrocution;
		maxAmps = max;
		color = vec.scale(1D / 255D);
	}

	public ItemStack getWire()
	{
		return getWire(1);
	}

	public ItemStack getWire(int amount)
	{
		ItemStack returnStack = wire.copy();
		returnStack.stackSize = amount;
		
		return returnStack;
	}

	public void setWire(ItemStack item)
	{
		if(wire == null)
		{
			wire = item;
			OreDictionary.registerOre(name().toLowerCase() + "Wire", wire);
		}
	}

	public void setWire(Item item)
	{
		setWire(new ItemStack(item, 1, ordinal()));
	}

	public void setWire(Block block)
	{
		setWire(new ItemStack(block, 1, ordinal()));
	}
}