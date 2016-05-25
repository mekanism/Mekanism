package mekanism.tools.item;

import java.util.List;
import java.util.Set;

import mekanism.api.util.StackUtils;
import mekanism.common.Mekanism;
import mekanism.common.MekanismItems;
import mekanism.common.util.LangUtils;
import mekanism.tools.common.MekanismTools;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTool;

public class ItemMekanismTool extends ItemTool
{
	public ItemMekanismTool(int mobBoost, ToolMaterial toolMaterial, Set<Block> effectiveBlocksIn)
	{
		super(mobBoost, toolMaterial, effectiveBlocksIn);
		setCreativeTab(Mekanism.tabMekanism);
	}

	@Override
	public void addInformation(ItemStack itemstack, EntityPlayer entityplayer, List list, boolean flag)
	{
		list.add(LangUtils.localize("tooltip.hp") + ": " + (itemstack.getMaxDamage() - itemstack.getItemDamage()));
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
    		return new ItemStack(MekanismItems.Ingot, 1, 0);
    	}
    	else if(material == MekanismTools.toolLAZULI || material == MekanismTools.toolLAZULI2)
    	{
    		return new ItemStack(Items.DYE, 1, 4);
    	}
    	else if(material == MekanismTools.toolOSMIUM || material == MekanismTools.toolOSMIUM2)
    	{
    		return new ItemStack(MekanismItems.Ingot, 1, 1);
    	}
    	else if(material == MekanismTools.toolBRONZE || material == MekanismTools.toolBRONZE2)
    	{
    		return new ItemStack(MekanismItems.Ingot, 1, 2);
    	}
    	else if(material == MekanismTools.toolGLOWSTONE || material == MekanismTools.toolGLOWSTONE2)
    	{
    		return new ItemStack(MekanismItems.Ingot, 1, 3);
    	}
    	else if(material == MekanismTools.toolSTEEL || material == MekanismTools.toolSTEEL2)
    	{
    		return new ItemStack(MekanismItems.Ingot, 1, 4);
    	}
    	
    	return material.getRepairItemStack();
    }
}
