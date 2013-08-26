package mekanism.common.item;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import mekanism.api.EnumColor;
import mekanism.api.IConfigurable;
import mekanism.api.Object3D;
import mekanism.api.transmitters.ITransmitter;
import mekanism.common.PacketHandler;
import mekanism.common.PacketHandler.Transmission;
import mekanism.common.network.PacketTileEntity;
import mekanism.common.tileentity.TileEntityBasicBlock;
import mekanism.common.tileentity.TileEntityContainerBlock;
import mekanism.common.tileentity.TileEntityElectricChest;
import mekanism.common.tileentity.TileEntityElectricPump;
import mekanism.common.tileentity.TileEntityLogisticalTransporter;
import mekanism.common.tileentity.TileEntityMechanicalPipe;
import mekanism.common.util.MekanismUtils;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatMessageComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;

public class ItemConfigurator extends ItemEnergized
{
	public final int ENERGY_PER_CONFIGURE = 400;
	public final int ENERGY_PER_ITEM_DUMP = 8;
	
    public ItemConfigurator(int id)
    {
        super(id, 60000, 120);
    }
    
	@Override
	public void addInformation(ItemStack itemstack, EntityPlayer entityplayer, List list, boolean flag)
	{
		super.addInformation(itemstack, entityplayer, list, flag);
		list.add(EnumColor.PINK + "State: " + EnumColor.GREY + getState(getState(itemstack)));
	}
    
    @Override
    public boolean onItemUseFirst(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ)
    {
    	if(!world.isRemote)
    	{
    		if(player.isSneaking())
    		{
	    		if(world.getBlockTileEntity(x, y, z) instanceof TileEntityMechanicalPipe)
	    		{
	    			TileEntityMechanicalPipe tileEntity = (TileEntityMechanicalPipe)world.getBlockTileEntity(x, y, z);
	    			tileEntity.isActive = !tileEntity.isActive;
	    			PacketHandler.sendPacket(Transmission.ALL_CLIENTS, new PacketTileEntity().setParams(Object3D.get(tileEntity), tileEntity.getNetworkedData(new ArrayList())));
	    			return true;
	    		}
	    		else if(world.getBlockTileEntity(x, y, z) instanceof TileEntityLogisticalTransporter)
	    		{
	    			TileEntityLogisticalTransporter tileEntity = (TileEntityLogisticalTransporter)world.getBlockTileEntity(x, y, z);
	    			tileEntity.isActive = !tileEntity.isActive;
	    			PacketHandler.sendPacket(Transmission.ALL_CLIENTS, new PacketTileEntity().setParams(Object3D.get(tileEntity), tileEntity.getNetworkedData(new ArrayList())));
	    			return true;
	    		}
	    		else if(world.getBlockTileEntity(x, y, z) instanceof TileEntityElectricPump)
	    		{
	    			TileEntityElectricPump tileEntity = (TileEntityElectricPump)world.getBlockTileEntity(x, y, z);
	    			tileEntity.recurringNodes.clear();
	    			tileEntity.cleaningNodes.clear();
	    			
	    			player.sendChatToPlayer(ChatMessageComponent.func_111066_d(EnumColor.DARK_BLUE + "[Mekanism] " + EnumColor.GREY + "Reset Electric Pump calculation."));
	    			return true;
	    		}
    		}
    		else if(world.getBlockTileEntity(x, y, z) instanceof ITransmitter)
    		{
    			((ITransmitter)world.getBlockTileEntity(x, y, z)).fixTransmitterNetwork();
    		}
    		
    		if(getState(stack) == 0)
    		{
	    		if(world.getBlockTileEntity(x, y, z) instanceof IConfigurable)
	    		{
	    			IConfigurable config = (IConfigurable)world.getBlockTileEntity(x, y, z);
	    			
	    			if(!player.isSneaking())
	    			{
	        			player.sendChatToPlayer(ChatMessageComponent.func_111066_d(EnumColor.DARK_BLUE + "[Mekanism]" + EnumColor.GREY + " Current color: " + config.getSideData().get(config.getConfiguration()[MekanismUtils.getBaseOrientation(side, config.getOrientation())]).color.getName()));
	        			return true;
	    			}
	    			else {
	    				if(getEnergy(stack) >= ENERGY_PER_CONFIGURE)
	    				{
	    					setEnergy(stack, getEnergy(stack) - ENERGY_PER_CONFIGURE);
		    				MekanismUtils.incrementOutput(config, MekanismUtils.getBaseOrientation(side, config.getOrientation()));
		    				player.sendChatToPlayer(ChatMessageComponent.func_111066_d(EnumColor.DARK_BLUE + "[Mekanism]" + EnumColor.GREY + " Color bumped to: " + config.getSideData().get(config.getConfiguration()[MekanismUtils.getBaseOrientation(side, config.getOrientation())]).color.getName()));
		    				
		    				if(config instanceof TileEntityBasicBlock)
		    				{
		    					TileEntityBasicBlock tileEntity = (TileEntityBasicBlock)config;
		    					PacketHandler.sendPacket(Transmission.CLIENTS_RANGE, new PacketTileEntity().setParams(Object3D.get(tileEntity), tileEntity.getNetworkedData(new ArrayList())), Object3D.get(tileEntity), 50D);
		    				}
		    				return true;
	    				}
	    			}
	    		}
    		}
    		else if(getState(stack) == 1)
    		{
    			if(world.getBlockTileEntity(x, y, z) instanceof TileEntityContainerBlock)
    			{
    				int itemAmount = 0;
    				Random random = new Random();
    				TileEntityContainerBlock tileEntity = (TileEntityContainerBlock)world.getBlockTileEntity(x, y, z);
    				
    				if(!(tileEntity instanceof TileEntityElectricChest) || (((TileEntityElectricChest)tileEntity).canAccess()))
    				{
	    				for(int i = 0; i < tileEntity.getSizeInventory(); i++)
	    	            {
	    	                ItemStack slotStack = tileEntity.getStackInSlot(i);
	
	    	                if(slotStack != null)
	    	                {
	    	                    if(getEnergy(stack) < ENERGY_PER_ITEM_DUMP)
		    	                {
		    	                	break;
		    	                }
	    	                    
	    	                    float xRandom = random.nextFloat() * 0.8F + 0.1F;
	    	                    float yRandom = random.nextFloat() * 0.8F + 0.1F;
	    	                    float zRandom = random.nextFloat() * 0.8F + 0.1F;
	
	    	                    while(slotStack.stackSize > 0)
	    	                    {
	    	                        int j = random.nextInt(21) + 10;
	
	    	                        if(j > slotStack.stackSize)
	    	                        {
	    	                            j = slotStack.stackSize;
	    	                        }
	
	    	                        slotStack.stackSize -= j;
	    	                        EntityItem item = new EntityItem(world, x + xRandom, y + yRandom, z + zRandom, new ItemStack(slotStack.itemID, j, slotStack.getItemDamage()));
	
	    	                        if(slotStack.hasTagCompound())
	    	                        {
	    	                            item.getEntityItem().setTagCompound((NBTTagCompound)slotStack.getTagCompound().copy());
	    	                        }
	
	    	                        float k = 0.05F;
	    	                        item.motionX = random.nextGaussian() * k;
	    	                        item.motionY = random.nextGaussian() * k + 0.2F;
	    	                        item.motionZ = random.nextGaussian() * k;
	    	                        world.spawnEntityInWorld(item);
	    	                        
	    	                        tileEntity.inventory[i] = null;
	    	                        setEnergy(stack, getEnergy(stack) - ENERGY_PER_ITEM_DUMP);
	    	                    }
	    	                }
	    	            }
	    				
	    				return true;
    				}
    				else {
    					player.addChatMessage(EnumColor.DARK_BLUE + "[Mekanism] " + EnumColor.GREY + "You are not authenticated on this chest.");
	    				return true;
    				}
    			}
    		}
    		else if(getState(stack) == 2)
    		{
    			TileEntity tileEntity = world.getBlockTileEntity(x, y, z);
    			
    			if(tileEntity instanceof TileEntityBasicBlock)
    			{
    				TileEntityBasicBlock basicBlock = (TileEntityBasicBlock)tileEntity;
    				int newSide = basicBlock.facing;
    				
    				if(!player.isSneaking())
    				{
    					newSide = side;
    				}
    				else {
    					newSide = ForgeDirection.OPPOSITES[side];
    				}
    				
    				if(basicBlock.canSetFacing(newSide))
    				{
    					basicBlock.setFacing((short)newSide);
	    				world.playSoundEffect(x, y, z, "random.click", 1.0F, 1.0F);
    				}
    				
    				return true;
    			}
    		}
    	}
    	
        return false;
    }
    
    public String getState(int state)
    {
    	switch(state)
    	{
    		case 0:
    			return "modify";
    		case 1:
    			return "empty";
    		case 2:
    			return "wrench";
    	}
    	
    	return "unknown";
    }
    
    public EnumColor getColor(int state)
    {
    	switch(state)
    	{
    		case 0:
    			return EnumColor.BRIGHT_GREEN;
    		case 1:
    			return EnumColor.AQUA;
    		case 2:
    			return EnumColor.YELLOW;
    	}
    	
    	return EnumColor.GREY;
    }
    
    public void setState(ItemStack itemstack, byte state)
    {
		if(itemstack.stackTagCompound == null)
		{
			itemstack.setTagCompound(new NBTTagCompound());
		}
		
		itemstack.stackTagCompound.setByte("state", state);
    }
    
    public byte getState(ItemStack itemstack)
    {
		if(itemstack.stackTagCompound == null)
		{
			return 0;
		}
		
		byte state = 0;
		
		if(itemstack.stackTagCompound.getTag("state") != null)
		{
			state = itemstack.stackTagCompound.getByte("state");
		}
		
		return state;
    }
	
	@Override
	public boolean canSend(ItemStack itemStack)
	{
		return false;
	}
}
