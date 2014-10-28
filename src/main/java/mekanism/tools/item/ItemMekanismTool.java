package mekanism.tools.item;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import ca.bradj.orecore.item.OreCoreItems;
import ca.bradj.orecoreext.item.OreCoreExtendedItems;
import mekanism.common.Mekanism;
import mekanism.common.item.ItemIngot;
import mekanism.common.util.MekanismUtils;
import mekanism.api.StackUtils;
import mekanism.tools.common.MekanismTools;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTool;

public class ItemMekanismTool extends ItemTool
{
	public ItemMekanismTool(int mobBoost, ToolMaterial toolMaterial, Block[] effectiveBlocks)
	{
		super(mobBoost, toolMaterial, new HashSet<Block>(Arrays.asList(effectiveBlocks)));
		setCreativeTab(Mekanism.tabMekanism);
	}

	@Override
	public void addInformation(ItemStack itemstack, EntityPlayer entityplayer, List list, boolean flag)
	{
		list.add(MekanismUtils.localize("tooltip.hp") + ": " + (itemstack.getMaxDamage() - itemstack.getItemDamage()));
	}
	
	@Override
    public boolean getIsRepairable(ItemStack stack1, ItemStack stack2)
    {
        return StackUtils.equalsWildcard(getRepairStack(), stack2) ? true : super.getIsRepairable(stack1, stack2);
    }
    
    private ItemStack getRepairStack()
    {
    	return getRepairStack(toolMaterial);
    }
    
    public static ItemStack getRepairStack(ToolMaterial material)
    {
    	if(material == MekanismTools.toolOBSIDIAN || material == MekanismTools.toolOBSIDIAN2)
    	{
    		return new ItemStack(OreCoreExtendedItems.obsidianIngot, 1);
    	}
    	else if(material == MekanismTools.toolLAZULI || material == MekanismTools.toolLAZULI2)
    	{
    		return new ItemStack(Items.dye, 1, 4);
    	}
    	else if(material == MekanismTools.toolOSMIUM || material == MekanismTools.toolOSMIUM2)
    	{
    		return new ItemStack(OreCoreItems.osmiumIngot, 1);
    	}
    	else if(material == MekanismTools.toolBRONZE || material == MekanismTools.toolBRONZE2)
    	{
    		return new ItemStack(OreCoreItems.bronzeIngot, 1);
    	}
    	else if(material == MekanismTools.toolGLOWSTONE || material == MekanismTools.toolGLOWSTONE2)
    	{
    		return new ItemStack(Mekanism.GlowstoneIngot, 1, ItemIngot.R_GLOWSTONE);
    	}
    	else if(material == MekanismTools.toolSTEEL || material == MekanismTools.toolSTEEL2)
    	{
    		return new ItemStack(OreCoreItems.steelIngot, 1);
    	}
    	
    	return new ItemStack(material.func_150995_f());
    }

	@Override
	public void registerIcons(IIconRegister register)
	{
		itemIcon = register.registerIcon("mekanism:" + getUnlocalizedName().replace("item.", ""));
	}
}
