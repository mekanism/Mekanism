package mekanism.induction.common.wire;

import java.util.List;

import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Icon;
import universalelectricity.core.electricity.ElectricityDisplay;
import universalelectricity.core.electricity.ElectricityDisplay.ElectricUnit;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemBlockWire extends ItemBlock
{
	private Icon[] icons = new Icon[EnumWireMaterial.values().length];

	public ItemBlockWire(int id)
	{
		super(id);
		this.setHasSubtypes(true);
		this.setMaxDamage(0);
	}

	@Override
	public int getMetadata(int damage)
	{
		return damage;
	}

	@Override
	public String getUnlocalizedName(ItemStack itemStack)
	{
		return this.getUnlocalizedName() + "." + EnumWireMaterial.values()[itemStack.getItemDamage()].name().toLowerCase();
	}

	@Override
	public void addInformation(ItemStack itemstack, EntityPlayer player, List par3List, boolean par4)
	{
		par3List.add("Resistance: " + ElectricityDisplay.getDisplay(EnumWireMaterial.values()[itemstack.getItemDamage()].resistance, ElectricUnit.RESISTANCE));
		par3List.add("Max Amperage: " + ElectricityDisplay.getDisplay(EnumWireMaterial.values()[itemstack.getItemDamage()].maxAmps, ElectricUnit.AMPERE));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister iconRegister)
	{
		for (int i = 0; i < EnumWireMaterial.values().length; i++)
		{
			this.icons[i] = iconRegister.registerIcon(this.getUnlocalizedName(new ItemStack(this.itemID, 1, i)).replaceAll("tile.", ""));
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Icon getIconFromDamage(int meta)
	{
		return this.icons[meta];
	}
}