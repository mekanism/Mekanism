package net.uberkat.obsidian.common;
import java.util.ArrayList;
import java.util.Random;

import cpw.mods.fml.common.registry.BlockProxy;

import net.minecraft.src.*;

public class BlockBase extends Block
{
	public BlockBase(int i, int j)
	{
		super(i, j, Material.iron);
		
		if(blockID == ObsidianIngots.platinumOreID)
		{
			setHardness(3F);
			setResistance(5F);
		}
		if(blockID == ObsidianIngots.refinedGlowstoneID || blockID == ObsidianIngots.refinedObsidianID || blockID == ObsidianIngots.coalBlockID || blockID == ObsidianIngots.redstoneBlockID || blockID == ObsidianIngots.platinumBlockID)
		{
			setHardness(5F);
			setResistance(10F);
			if(blockID == ObsidianIngots.refinedObsidianID)
			{
				setLightValue(0.5F);
			}
			if(blockID == ObsidianIngots.refinedGlowstoneID)
			{
				setLightValue(0.875F);
			}
		}
	}

	public int idDropped(int i, Random random)
	{
		return this.blockID;
	}

	public int quantityDropped(Random random)
    {
       	return 1;
    }
	
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer entityplayer, int i1, float f1, float f2, float f3)
    {
    	if(blockID == ObsidianIngots.refinedObsidianID)
    	{
    		if(entityplayer.isSneaking())
    		{
    			entityplayer.openGui(ObsidianIngots.instance, 19, world, x, y, z);
    			return true;
    		}
    	}
        return false;
    }
	
    public void addCreativeItems(ArrayList itemList)
    {
    	if(blockID != ObsidianIngots.platinumOreID)
    	{
    		itemList.add(new ItemStack(this));
    	}
    	else {
    		//Do nothing
    	}
    }
    
	public String getTextureFile() {
		return "/obsidian/terrain.png";
	}
}