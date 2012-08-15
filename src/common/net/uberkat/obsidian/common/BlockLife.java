package net.uberkat.obsidian.common;

import java.util.ArrayList;
import java.util.Random;

import cpw.mods.fml.common.registry.BlockProxy;

import net.minecraft.src.*;

public class BlockLife extends Block
{
	private Random lifeRand;
	
	public BlockLife(int i, int j)
	{
		super(i, j, Material.iron);
		lifeRand = new Random();
		setHardness(1F);
		setResistance(3F);
		setLightValue(2F);
	}
	
    public boolean blockActivated(World world, int i, int j, int k, EntityPlayer entityplayer)
    {
    	if(!world.isRemote)
    	{
    		int random = lifeRand.nextInt(3);
	    	world.spawnParticle("hugeexplosion", i, j, k, 0.0D, 0.0D, 0.0D);
	    	switch(random)
	    	{
	    		case 0:
	    			entityplayer.inventory.addItemStackToInventory(new ItemStack(ObsidianIngots.Stopwatch, 1));
	    			world.setBlock(i, j, k, 0);
		    		break;
	    		case 1:
	    			entityplayer.inventory.addItemStackToInventory(new ItemStack(ObsidianIngots.LightningRod, 1));
	    			world.setBlock(i, j, k, 0);
		    		break;
	    		case 2:
	    			entityplayer.inventory.addItemStackToInventory(new ItemStack(ObsidianIngots.WeatherOrb, 1));
	    			world.setBlock(i, j, k, 0);
		    		break;
	    	}
    	}
    	return true;
    }
	
	public int idDropped(int i, Random random)
	{
		return 0;
	}

	public int quantityDropped(Random random)
    {
       	return 0;
    }
	
    public void addCreativeItems(ArrayList itemList)
    {
    	itemList.add(new ItemStack(this));
    }
    
	public String getTextureFile() {
		return "/obsidian/terrain.png";
	}
}
