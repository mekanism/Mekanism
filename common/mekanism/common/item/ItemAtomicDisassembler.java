package mekanism.common.item;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import mekanism.api.Coord4D;
import mekanism.api.EnumColor;
import mekanism.api.ListUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.Event.Result;
import net.minecraftforge.event.entity.player.UseHoeEvent;

public class ItemAtomicDisassembler extends ItemEnergized
{
	public double ENERGY_USAGE = 10;
	public double HOE_USAGE = 100;
	
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
    	list.add("Efficiency: " + EnumColor.INDIGO + getEfficiency(itemstack));
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
    	return getEnergy(itemstack) != 0 ? getEfficiency(itemstack) : 1F;
    }
    
    @Override
    public boolean onBlockDestroyed(ItemStack itemstack, World world, int id, int x, int y, int z, EntityLivingBase entityliving)
    {
        if(Block.blocksList[id].getBlockHardness(world, x, y, z) != 0.0D)
        {
        	setEnergy(itemstack, getEnergy(itemstack) - (ENERGY_USAGE*getEfficiency(itemstack)));
        }
        else {
        	setEnergy(itemstack, getEnergy(itemstack) - (ENERGY_USAGE*(getEfficiency(itemstack))/2));
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
    		
    		if(getMode(itemstack) == 3 && isOre && !player.capabilities.isCreativeMode)
    		{
	    		Set<Coord4D> found = new Finder(player.worldObj, stack, new Coord4D(x, y, z, player.worldObj.provider.dimensionId)).calc();
	    		
	    		for(Coord4D coord : found)
	    		{
	    			if(coord.equals(orig) || getEnergy(itemstack) < (ENERGY_USAGE*getEfficiency(itemstack)))
	    			{
	    				continue;
	    			}
	    			
	    			Block block = coord.getBlock(player.worldObj);
	    			
	    			block.onBlockDestroyedByPlayer(player.worldObj, coord.xCoord, coord.yCoord, coord.zCoord, meta);
	    			player.worldObj.playAuxSFXAtEntity(null, 2001, coord.xCoord, coord.yCoord, coord.zCoord, id + (meta << 12));
	    			player.worldObj.setBlockToAir(coord.xCoord, coord.yCoord, coord.zCoord);
	    			block.breakBlock(player.worldObj, coord.xCoord, coord.yCoord, coord.zCoord, id, meta);
	    			block.dropBlockAsItem(player.worldObj, coord.xCoord, coord.yCoord, coord.zCoord, meta, 0);
	    			
	    			setEnergy(itemstack, getEnergy(itemstack) - (ENERGY_USAGE*getEfficiency(itemstack)));
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
    		entityplayer.addChatMessage(EnumColor.DARK_BLUE + "[Mekanism] " + EnumColor.GREY + "Mode toggled to " + EnumColor.INDIGO + getModeName(itemstack) + EnumColor.AQUA + " (" + getEfficiency(itemstack) + ")");
		}
		
        return itemstack;
    }
    
	@Override
	public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ)
	{
		if(!player.isSneaking())
		{
			if(!useHoe(stack, player, world, x, y, z, side))
			{
				if(world.getBlockId(x, y, z) != Block.tilledField.blockID)
				{
					return false;
				}
			}
			
			switch(getEfficiency(stack))
			{
				case 20:
					for(int x1 = x-1; x1 <= x+1; x1++)
					{
						for(int z1 = z-1; z1 <= z+1; z1++)
						{
							useHoe(stack, player, world, x1, y, z1, side);
						}
					}
					
					break;
				case 128:
					for(int x1 = x-2; x1 <= x+2; x1++)
					{
						for(int z1 = z-2; z1 <= z+2; z1++)
						{
							useHoe(stack, player, world, x1, y, z1, side);
						}
					}
					
					break;
			}
			
			return true;
		}
		
    	return false;
    }
	
	private boolean useHoe(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side)
	{
		if(!player.canPlayerEdit(x, y, z, side, stack))
        {
            return false;
        }
        else {
            UseHoeEvent event = new UseHoeEvent(player, stack, world, x, y, z);
            
            if(MinecraftForge.EVENT_BUS.post(event))
            {
                return false;
            }

            if(event.getResult() == Result.ALLOW)
            {
            	setEnergy(stack, getEnergy(stack)-HOE_USAGE);
                return true;
            }

            int id = world.getBlockId(x, y, z);
            boolean air = world.isAirBlock(x, y + 1, z);

            if(side != 0 && air && (id == Block.grass.blockID || id == Block.dirt.blockID))
            {
                Block block = Block.tilledField;
                world.playSoundEffect(x + 0.5F, y + 0.5F, z + 0.5F, block.stepSound.getStepSound(), (block.stepSound.getVolume() + 1.0F) / 2.0F, block.stepSound.getPitch() * 0.8F);

                if(world.isRemote)
                {
                    return true;
                }
                else {
                    world.setBlock(x, y, z, block.blockID);
                 	setEnergy(stack, getEnergy(stack)-HOE_USAGE);
                    return true;
                }
            }
            else {
                return false;
            }
        }
	}
    
    public int getEfficiency(ItemStack itemStack)
    {
    	switch(getMode(itemStack))
    	{
    		case 0:
    			return 20;
    		case 1:
    			return 8;
    		case 2:
    			return 128;
    		case 3:
    			return 20;
    		case 4:
    			return 0;
    	}
    	
    	return 0;
    }
    
    public int getMode(ItemStack itemStack)
    {
		if(itemStack.stackTagCompound == null)
		{
			return 0;
		}
		
		return itemStack.stackTagCompound.getInteger("mode");
    }
    
    public String getModeName(ItemStack itemStack)
    {
    	int mode = getMode(itemStack);
    	
    	switch(mode)
    	{
    		case 0:
    			return "normal";
    		case 1:
    			return "slow";
    		case 2:
    			return "fast";
    		case 3:
    			return "vein";
    		case 4:
    			return "off";
    	}
    	
    	return null;
    }
    
    public void toggleMode(ItemStack itemStack)
    {
		if(itemStack.stackTagCompound == null)
		{
			itemStack.setTagCompound(new NBTTagCompound());
		}
		
		itemStack.stackTagCompound.setInteger("mode", getMode(itemStack) < 4 ? getMode(itemStack)+1 : 0);
    }
    
    @Override
	public boolean canSend(ItemStack itemStack)
	{
		return false;
	}
    
    public static class Finder
    {
    	public World world;
    	
    	public ItemStack stack;
    	
    	public Coord4D location;
    	
    	public Set<Coord4D> found = new HashSet<Coord4D>();
    	
    	public static Map<Integer, List<Integer>> ignoreID = new HashMap<Integer, List<Integer>>();
    	
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
    			
    			if(coord.exists(world) && checkID(coord.getBlockId(world)) && coord.getMetadata(world) == stack.getItemDamage())
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
    	
    	public boolean checkID(int id)
    	{
    		int origId = location.getBlockId(world);
    		return (ignoreID.get(origId) == null && id == origId) || (ignoreID.get(origId) != null && ignoreID.get(origId).contains(id));
    	}
    	
    	static {
    		ignoreID.put(Block.oreRedstone.blockID, ListUtils.asList(Block.oreRedstone.blockID, Block.oreRedstoneGlowing.blockID));
    		ignoreID.put(Block.oreRedstoneGlowing.blockID, ListUtils.asList(Block.oreRedstone.blockID, Block.oreRedstoneGlowing.blockID));
    	}
    }
}
