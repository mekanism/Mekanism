package mekanism.induction.common.wire;

import java.util.List;

import mekanism.common.Mekanism;
import mekanism.induction.client.render.RenderPartWire;
import mekanism.induction.common.MekanismInduction;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Icon;
import net.minecraft.world.World;
import net.minecraftforge.common.Configuration;
import universalelectricity.core.electricity.ElectricityDisplay;
import universalelectricity.core.electricity.ElectricityDisplay.ElectricUnit;
import codechicken.lib.vec.BlockCoord;
import codechicken.lib.vec.Vector3;
import codechicken.multipart.JItemMultiPart;
import codechicken.multipart.TMultiPart;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemPartWire extends JItemMultiPart
{
	private Icon[] icons = new Icon[EnumWireMaterial.values().length];

	public ItemPartWire(int id)
	{
		super(Mekanism.configuration.get(Configuration.CATEGORY_ITEM, "wireMultipart", id).getInt(id));
		setCreativeTab(Mekanism.tabMekanism);
		setHasSubtypes(true);
		setMaxDamage(0);
	}

	@Override
	public TMultiPart newPart(ItemStack arg0, EntityPlayer arg1, World arg2, BlockCoord arg3, int arg4, Vector3 arg5)
	{
		return new PartWire(getDamage(arg0));
	}

	@Override
	public int getMetadata(int damage)
	{
		return damage;
	}

	@Override
	public String getUnlocalizedName(ItemStack itemStack)
	{
		return "tile.Wire." + EnumWireMaterial.values()[itemStack.getItemDamage()].getName() + "Wire";
	}

	@Override
	public void addInformation(ItemStack itemstack, EntityPlayer player, List list, boolean par4)
	{
		list.add("Resistance: " + ElectricityDisplay.getDisplay(EnumWireMaterial.values()[itemstack.getItemDamage()].resistance, ElectricUnit.RESISTANCE));
		list.add("Max Amperage: " + ElectricityDisplay.getDisplay(EnumWireMaterial.values()[itemstack.getItemDamage()].maxAmps, ElectricUnit.AMPERE));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister register)
	{	
		for(EnumWireMaterial material : EnumWireMaterial.values())
		{
			icons[material.ordinal()] = register.registerIcon("mekanism:" + material.getName() + "Wire");
		}

		RenderPartWire.registerIcons(register);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Icon getIconFromDamage(int meta)
	{
		return icons[meta];
	}

	@Override
	public void getSubItems(int itemID, CreativeTabs tab, List listToAddTo)
	{
		for(EnumWireMaterial mat : EnumWireMaterial.values())
		{
			listToAddTo.add(new ItemStack(itemID, 1, mat.ordinal()));
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getSpriteNumber()
	{
		return 0;
	}
}
