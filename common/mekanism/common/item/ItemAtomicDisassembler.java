package mekanism.common.item;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import mekanism.api.Coord4D;
import mekanism.api.EnumColor;
import mekanism.common.util.MekanismUtils;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;

public class ItemAtomicDisassembler extends ItemEnergized
{
	public double ENERGY_USAGE = 200;
	public ItemAtomicDisassembler(int id)
	{
		super(id, 1000000, 120);
	}
	
    @Override
    public boolean canHarvestBlock(Block block)
    {
    	return block != Block.bedrock;
    }
    
    @Override
	public void addInformation(ItemStack itemstack, EntityPlayer entityplayer, List list, boolean flag)
	{
    	super.addInformation(itemstack, entityplayer, list, flag);
    	
    	list.add("Mode: " + EnumColor.INDIGO + (getMode(itemstack) == 0 ? "normal" : "vein"));
	}
    
    @Override
    public boolean hitEntity(ItemStack itemstack, EntityLivingBase hitEntity, EntityLivingBase player)
    {
    	if(getEnergy(itemstack) > 0)
    	{
			hitEntity.attackEntityFrom(DamageSource.causePlayerDamage((EntityPlayer)player), 20);
			setEnergy(itemstack, getEnergy(itemstack) - 2000);
    	}
    	else {
    		hitEntity.attackEntityFrom(DamageSource.causePlayerDamage((EntityPlayer)player), 4);
    	}
    	
        return false;
    }
    
    @Override
    public float getStrVsBlock(ItemStack itemstack, Block block)
    {
    	return getEnergy(itemstack) != 0 ? 128 : 1F;
    }
    
    @Override
    public boolean onBlockDestroyed(ItemStack itemstack, World world, int id, int x, int y, int z, EntityLivingBase entityliving)
    {
        if(Block.blocksList[id].getBlockHardness(world, x, y, z) != 0.0D)
        {
        	setEnergy(itemstack, getEnergy(itemstack) - ENERGY_USAGE);
        }
        else {
        	setEnergy(itemstack, getEnergy(itemstack) - (ENERGY_USAGE/2));
        }

        return true;
    }
    
    @Override
    public boolean onBlockStartBreak(ItemStack itemstack, int x, int y, int z, EntityPlayer player)
    {
    	super.onBlockStartBreak(itemstack, x, y, z, player);
    	
    	if(!player.worldObj.isRemote)
    	{
    		int id = player.worldObj.getBlockId(x, y, z);
    		int meta = player.worldObj.getBlockMetadata(x, y, z);
    		
    		ItemStack stack = new ItemStack(id, 1, meta);
    		Coord4D orig = new Coord4D(x, y, z, player.worldObj.provider.dimensionId);
    		
    		List<String> names = MekanismUtils.getOreDictName(stack);
    		
    		boolean isOre = false;
    		
    		for(String s : names)
    		{
    			if(s.contains("ore"))
    			{
    				isOre = true;
    			}
    		}
    		
    		if(getMode(itemstack) == 1 && isOre && !player.capabilities.isCreativeMode)
    		{
	    		Set<Coord4D> found = new Finder(player.worldObj, stack, new Coord4D(x, y, z, player.worldObj.provider.dimensionId)).calc();
	    		
	    		for(Coord4D coord : found)
	    		{
	    			if(coord.equals(orig) || getEnergy(itemstack) < ENERGY_USAGE)
	    			{
	    				continue;
	    			}
	    			
	    			Block block = coord.getBlock(player.worldObj);
	    			
	    			block.onBlockDestroyedByPlayer(player.worldObj, coord.xCoord, coord.yCoord, coord.zCoord, meta);
	    			player.worldObj.playAuxSFXAtEntity(null, 2001, coord.xCoord, coord.yCoord, coord.zCoord, id + (meta << 12));
	    			player.worldObj.setBlockToAir(coord.xCoord, coord.yCoord, coord.zCoord);
	    			block.breakBlock(player.worldObj, coord.xCoord, coord.yCoord, coord.zCoord, id, meta);
	    			block.dropBlockAsItem(player.worldObj, coord.xCoord, coord.yCoord, coord.zCoord, meta, 0);
	    			
	    			setEnergy(itemstack, getEnergy(itemstack) - ENERGY_USAGE);
	    		}
    		}
    	}
    	
        return false;
    }
    
    @Override
    public boolean isFull3D()
    {
        return true;
    }
    
    @Override
    public ItemStack onItemRightClick(ItemStack itemstack, World world, EntityPlayer entityplayer)
    {
		if(!world.isRemote && entityplayer.isSneaking())
		{
			toggleMode(itemstack);
    		entityplayer.addChatMessage(EnumColor.DARK_BLUE + "[Mekanism] " + EnumColor.GREY + "Mode toggled to " + EnumColor.INDIGO + (getMode(itemstack) == 0 ? "normal" : "vein"));
		}
		
        return itemstack;
    }
    
    public int getMode(ItemStack itemStack)
    {
		if(itemStack.stackTagCompound == null)
		{
			return 0;
		}
		
		return itemStack.stackTagCompound.getInteger("mode");
    }
    
    public void toggleMode(ItemStack itemStack)
    {
		if(itemStack.stackTagCompound == null)
		{
			itemStack.setTagCompound(new NBTTagCompound());
		}
		
		itemStack.stackTagCompound.setInteger("mode", getMode(itemStack) == 0 ? 1 : 0);
    }
    
    @Override
	public boolean canSend(ItemStack itemStack)
	{
		return false;
	}
    
    public class Finder
    {
    	public World world;
    	
    	public ItemStack stack;
    	
    	public Coord4D location;
    	
    	public Set<Coord4D> found = new HashSet<Coord4D>();
    	
    	public Finder(World w, ItemStack s, Coord4D loc)
    	{
    		world = w;
    		stack = s;
    		location = loc;
    	}
    	
    	public void loop(Coord4D pointer)
    	{
    		if(found.contains(pointer) || found.size() > 128)
    		{
    			return;
    		}
    		
    		found.add(pointer);
    		
    		for(ForgeDirection side : ForgeDirection.VALID_DIRECTIONS)
    		{
    			Coord4D coord = pointer.getFromSide(side);
    			
    			if(coord.exists(world) && coord.getBlockId(world) == stack.itemID && coord.getMetadata(world) == stack.getItemDamage())
    			{
    				loop(coord);
    			}
    		}
    	}
    	
    	public Set<Coord4D> calc()
    	{
    		loop(location);
    		
    		return found;
    	}
    }
}
