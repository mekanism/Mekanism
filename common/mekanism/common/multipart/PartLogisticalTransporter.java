package mekanism.common.multipart;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import mekanism.api.Coord4D;
import mekanism.api.EnumColor;
import mekanism.api.transmitters.TransmissionType;
import mekanism.client.render.PartTransmitterIcons;
import mekanism.client.render.RenderPartTransmitter;
import mekanism.common.HashList;
import mekanism.common.ILogisticalTransporter;
import mekanism.common.PacketHandler;
import mekanism.common.PacketHandler.Transmission;
import mekanism.common.network.PacketDataRequest;
import mekanism.common.network.PacketTileEntity;
import mekanism.common.tileentity.TileEntityLogisticalSorter;
import mekanism.common.transporter.TransporterManager;
import mekanism.common.transporter.TransporterStack;
import mekanism.common.transporter.TransporterStack.Path;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.PipeUtils;
import mekanism.common.util.TransporterUtils;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatMessageComponent;
import net.minecraft.util.Icon;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import buildcraft.api.transport.IPipe;
import buildcraft.api.transport.IPipeTile;
import codechicken.lib.vec.Vector3;

import com.google.common.io.ByteArrayDataInput;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class PartLogisticalTransporter extends PartSidedPipe implements ILogisticalTransporter, IPipeTile
{
	public static PartTransmitterIcons transporterIcons;
	
	public static final int SPEED = 5;
	
	public EnumColor color;
	
	public HashList<TransporterStack> transit = new HashList<TransporterStack>();
	
	public Set<TransporterStack> needsSync = new HashSet<TransporterStack>();

	@Override
	public String getType()
	{
		return "mekanism:logistical_transporter";
	}

	@Override
	public TransmitterType getTransmitter()
	{
		return TransmitterType.LOGISTICAL_TRANSPORTER;
	}
	
	@Override
	public TransmissionType getTransmissionType()
	{
		return TransmissionType.ITEM;
	}

	public static void registerIcons(IconRegister register)
	{
		transporterIcons = new PartTransmitterIcons(3, 2);
		transporterIcons.registerCenterIcons(register, new String[] {"LogisticalTransporter", "RestrictiveTransporter", "DiversionTransporter"});
		transporterIcons.registerSideIcons(register, new String[] {"LogisticalTransporterSide", "RestrictiveTransporterSide"});
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void renderDynamic(Vector3 pos, float f, int pass)
	{
		RenderPartTransmitter.getInstance().renderContents(this, f, pos);
	}
	
	@Override
	public boolean canConnect(ForgeDirection side)
	{
		testingSide = side;
		boolean unblocked = tile().canReplacePart(this, this);
		testingSide = null;
		return unblocked;
	}
	
	@Override
	public byte getPossibleTransmitterConnections()
	{
		byte connections = 0x00;

		for(ForgeDirection side : ForgeDirection.VALID_DIRECTIONS)
		{
			if(canConnectMutual(side))
			{
				TileEntity tileEntity = Coord4D.get(tile()).getFromSide(side).getTileEntity(world());

				if(TransmissionType.checkTransmissionType(tileEntity, getTransmitter().getType()) && isConnectable(tileEntity))
				{
					connections |= 1 << side.ordinal();
				}
				else {
					System.out.println("Invalid type " + getTransmitter().getType());
				}
			}
			else {
				System.out.println("can't connect");
			}
		}

		return connections;
	}
	
	@Override
	public byte getPossibleAcceptorConnections()
	{
		byte connections = 0x00;

		for(ForgeDirection side : ForgeDirection.VALID_DIRECTIONS)
		{
			if(canConnectMutual(side))
			{
				TileEntity tileEntity = Coord4D.get(tile()).getFromSide(side).getTileEntity(world());

				if(isValidAcceptor(tileEntity, side))
				{
					connections |= 1 << side.ordinal();
				}
			}
		}

		return connections;
	}

	@Override
	public Icon getCenterIcon()
	{
		return transporterIcons.getCenterIcon(0);
	}

	@Override
	public Icon getSideIcon()
	{
		return transporterIcons.getSideIcon(0);
	}

	@Override
	public boolean isValidAcceptor(TileEntity tile, ForgeDirection side)
	{
		return TransporterUtils.getConnections(this)[side.ordinal()];
	}

	@Override
	public void onModeChange(ForgeDirection side) {}
	
	@Override
	public void update()
	{
		if(world().isRemote)
		{
			for(TransporterStack stack : transit)
			{
				if(stack != null)
				{
					stack.progress = Math.min(100, stack.progress+SPEED);
				}
			}
		}
		else {
			Set<TransporterStack> remove = new HashSet<TransporterStack>();
			
			for(TransporterStack stack : transit)
			{
				if(!stack.initiatedPath)
				{
					if(!recalculate(stack, null))
					{
						remove.add(stack);
						continue;
					}
				}
				
				stack.progress += SPEED;
				
				if(stack.progress > 100)
				{
					Coord4D prevSet = null;
					
					if(stack.hasPath())
					{
						int currentIndex = stack.pathToTarget.indexOf(Coord4D.get(tile()));
						Coord4D next = stack.pathToTarget.get(currentIndex-1);
						
						if(!stack.isFinal(this))
						{
							if(next != null && stack.canInsertToTransporter(stack.getNext(this).getTileEntity(world())))
							{
								ILogisticalTransporter nextTile = (ILogisticalTransporter)next.getTileEntity(world());
								nextTile.entityEntering(stack);
								remove.add(stack);
								
								continue;
							}
							else if(next != null)
							{
								prevSet = next;
							}
						}
						else {
							if(stack.pathType != Path.NONE)
							{
								if(next != null && next.getTileEntity(world()) instanceof IInventory)
								{
									needsSync.add(stack);
									IInventory inventory = (IInventory)next.getTileEntity(world());
									
									if(inventory != null)
									{
										ItemStack rejected = InventoryUtils.putStackInInventory(inventory, stack.itemStack, stack.getSide(this), stack.pathType == Path.HOME);
										
										if(rejected == null)
										{
											TransporterManager.remove(stack);
											remove.add(stack);
											continue;
										}
										else {
											needsSync.add(stack);
											stack.itemStack = rejected;
											
											prevSet = next;
										}
									}
								}
							}
						}
					}
					
					if(!recalculate(stack, prevSet))
					{
						remove.add(stack);
						continue;
					}
					else {
						if(prevSet != null)
						{
							stack.progress = 0;
						}
						else {
							stack.progress = 50;
						}
					}
				}
				else if(stack.progress == 50)
				{
					if(stack.isFinal(this))
					{
						if(stack.pathType == Path.DEST && !InventoryUtils.canInsert(stack.getDest().getTileEntity(world()), stack.color, stack.itemStack, stack.getSide(this), false))
						{
							if(!recalculate(stack, null))
							{
								remove.add(stack);
								continue;
							}
						}
						else if(stack.pathType == Path.HOME && !InventoryUtils.canInsert(stack.getDest().getTileEntity(world()), stack.color, stack.itemStack, stack.getSide(this), true))
						{
							if(!recalculate(stack, null))
							{
								remove.add(stack);
								continue;
							}
						}
						else if(stack.pathType == Path.NONE)
						{
							if(!recalculate(stack, null))
							{
								remove.add(stack);
								continue;
							}
						}
					}
					else {
						TileEntity next = stack.getNext(this).getTileEntity(world());
						boolean recalculate = false;
						
						if(!stack.canInsertToTransporter(next))
						{
							recalculate = true;
						}
						
						if(!TransporterUtils.checkDiversionLogic(tile(), next, stack.getSide(this)))
						{
							recalculate = true;
						}
						
						if(recalculate)
						{
							if(!recalculate(stack, null))
							{
								remove.add(stack);
								continue;
							}
						}
					}
				}
			}
			
			for(TransporterStack stack : remove)
			{
				PacketHandler.sendPacket(Transmission.CLIENTS_RANGE, new PacketTileEntity().setParams(Coord4D.get(tile()), getSyncPacket(stack, true)), Coord4D.get(tile()), 50D);
				transit.remove(stack);
				MekanismUtils.saveChunk(tile());
			}
			
			for(TransporterStack stack : needsSync)
			{
				if(transit.contains(stack))
				{
					PacketHandler.sendPacket(Transmission.CLIENTS_RANGE, new PacketTileEntity().setParams(Coord4D.get(tile()), getSyncPacket(stack, false)), Coord4D.get(tile()), 50D);
				}
			}
			
			needsSync.clear();
		}
	}
	
	private boolean recalculate(TransporterStack stack, Coord4D from)
	{
		needsSync.add(stack);
		
		if(!TransporterManager.didEmit(stack.itemStack, stack.recalculatePath(this, 0)))
		{
			if(!stack.calculateIdle(this))
			{
				TransporterUtils.drop(this, stack);
				return false;
			}
		}
		
		if(from != null)
		{
			stack.originalLocation = from;
		}
		
		return true;
	}
	
	@Override
	public ItemStack insert(Coord4D original, ItemStack itemStack, EnumColor color, boolean doEmit, int min)
	{
		TransporterStack stack = new TransporterStack();
		stack.itemStack = itemStack;
		stack.originalLocation = original;
		stack.homeLocation = original;
		stack.color = color;
		
		if(!stack.canInsertToTransporter(tile()))
		{
			return itemStack;
		}
		
		ItemStack rejected = stack.recalculatePath(this, min);
		
		if(TransporterManager.didEmit(stack.itemStack, rejected))
		{
			stack.itemStack = TransporterManager.getToUse(stack.itemStack, rejected);
			
			transit.add(stack);
			TransporterManager.add(stack);
			PacketHandler.sendPacket(Transmission.CLIENTS_RANGE, new PacketTileEntity().setParams(Coord4D.get(tile()), getSyncPacket(stack, false)), Coord4D.get(tile()), 50D);
			MekanismUtils.saveChunk(tile());
			return rejected;
		}
		
		return itemStack;
	}
	
	@Override
	public ItemStack insertRR(TileEntityLogisticalSorter outputter, ItemStack itemStack, EnumColor color, boolean doEmit, int min)
	{
		TransporterStack stack = new TransporterStack();
		stack.itemStack = itemStack;
		stack.originalLocation = Coord4D.get(outputter);
		stack.homeLocation = Coord4D.get(outputter);
		stack.color = color;
		
		if(!stack.canInsertToTransporter(tile()))
		{
			return itemStack;
		}
		
		ItemStack rejected = stack.recalculateRRPath(outputter, this, min);
		
		if(TransporterManager.didEmit(stack.itemStack, rejected))
		{
			stack.itemStack = TransporterManager.getToUse(stack.itemStack, rejected);
			
			transit.add(stack);
			TransporterManager.add(stack);
			PacketHandler.sendPacket(Transmission.CLIENTS_RANGE, new PacketTileEntity().setParams(Coord4D.get(tile()), getSyncPacket(stack, false)), Coord4D.get(tile()), 50D);
			MekanismUtils.saveChunk(tile());
			return rejected;
		}
		
		return itemStack;
	}
	
	@Override
	public void entityEntering(TransporterStack stack)
	{
		stack.progress = 0;
		transit.add(stack);
		PacketHandler.sendPacket(Transmission.CLIENTS_RANGE, new PacketTileEntity().setParams(Coord4D.get(tile()), getSyncPacket(stack, false)), Coord4D.get(tile()), 50D);
		MekanismUtils.saveChunk(tile());
	}
	
	@Override
	public void onWorldJoin()
	{
		super.onWorldJoin();
		
		if(world().isRemote)
		{
			PacketHandler.sendPacket(Transmission.SERVER, new PacketDataRequest().setParams(Coord4D.get(tile())));
		}
	}
	
	@Override
	public void handlePacketData(ByteArrayDataInput dataStream)
	{
		int type = dataStream.readInt();
		
		if(type == 0)
		{
			int c = dataStream.readInt();
			
			if(c != -1)
			{
				color = TransporterUtils.colors.get(c);
			}
			else {
				color = null;
			}
			
			transit.clear();
			
			int amount = dataStream.readInt();
			
			for(int i = 0; i < amount; i++)
			{
				transit.add(TransporterStack.readFromPacket(dataStream));
			}
		}
		else if(type == 1)
		{
			boolean kill = dataStream.readBoolean();
			int index = dataStream.readInt();
			
			if(kill)
			{
				transit.remove(index);
			}
			else {
				TransporterStack stack = TransporterStack.readFromPacket(dataStream);
				
				if(stack.progress == 0)
				{
					stack.progress = 5;
				}
				
				transit.replace(index, stack);
			}
		}
	}
	
	@Override
	public ArrayList getNetworkedData(ArrayList data)
	{
		data.add(0);
		
		if(color != null)
		{
			data.add(TransporterUtils.colors.indexOf(color));
		}
		else {
			data.add(-1);
		}
		
		data.add(transit.size());
		
		for(TransporterStack stack : transit)
		{
			stack.write(this, data);
		}
		
		return data;
	}
	
	public ArrayList getSyncPacket(TransporterStack stack, boolean kill)
	{
		ArrayList data = new ArrayList();
		
		data.add(1);
		data.add(kill);
		data.add(transit.indexOf(stack));
		
		if(!kill)
		{
			stack.write(this, data);
		}
		
		return data;
	}
	
	@Override
    public void load(NBTTagCompound nbtTags)
    {
        super.load(nbtTags);
        
        if(nbtTags.hasKey("color"))
        {
        	color = TransporterUtils.colors.get(nbtTags.getInteger("color"));
        }
        
    	if(nbtTags.hasKey("stacks"))
    	{
    		NBTTagList tagList = nbtTags.getTagList("stacks");
    		
    		for(int i = 0; i < tagList.tagCount(); i++)
    		{
    			TransporterStack stack = TransporterStack.readFromNBT((NBTTagCompound)tagList.tagAt(i));
    			
    			transit.add(stack);
    			TransporterManager.add(stack);
    		}
    	}
    }

	@Override
    public void save(NBTTagCompound nbtTags)
    {
        super.save(nbtTags);
        
        if(color != null)
        {
        	nbtTags.setInteger("color", TransporterUtils.colors.indexOf(color));
        }
        
        NBTTagList stacks = new NBTTagList();
        
        for(TransporterStack stack : transit)
        {
        	NBTTagCompound tagCompound = new NBTTagCompound();
        	stack.write(tagCompound);
        	stacks.appendTag(tagCompound);
        }
        
        if(stacks.tagCount() != 0)
        {
        	nbtTags.setTag("stacks", stacks);
        }
    }

	@Override
	public boolean isSolidOnSide(ForgeDirection side)
	{
		return true;
	}

	@Override
	public int fill(ForgeDirection from, FluidStack resource, boolean doFill) 
	{
		return 0;
	}

	@Override
	public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain) 
	{
		return null;
	}

	@Override
	public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain) 
	{
		return null;
	}

	@Override
	public boolean canFill(ForgeDirection from, Fluid fluid) 
	{
		return false;
	}

	@Override
	public boolean canDrain(ForgeDirection from, Fluid fluid) 
	{
		return false;
	}

	@Override
	public FluidTankInfo[] getTankInfo(ForgeDirection from) 
	{
		return PipeUtils.EMPTY;
	}

	@Override
	public IPipe getPipe() 
	{
		return null;
	}

	@Override
	public PipeType getPipeType() 
	{
		return PipeType.ITEM;
	}

	@Override
	public int injectItem(ItemStack stack, boolean doAdd, ForgeDirection from) 
	{
		if(doAdd)
		{
			TileEntity tile = Coord4D.get(tile()).getFromSide(from).getTileEntity(world());
			
			ItemStack rejects = TransporterUtils.insert(tile, this, stack, null, true, 0);
			return TransporterManager.getToUse(stack, rejects).stackSize;
		}
		
		return 0;
	}

	@Override
	public boolean isPipeConnected(ForgeDirection with) 
	{
		return true;
	}

	@Override
	protected boolean onConfigure(EntityPlayer player, int part, int side)
	{
		TransporterUtils.incrementColor(this);
		PacketHandler.sendPacket(Transmission.CLIENTS_RANGE, new PacketTileEntity().setParams(Coord4D.get(tile()), getNetworkedData(new ArrayList())), Coord4D.get(tile()), 50D);
		player.sendChatToPlayer(ChatMessageComponent.createFromText(EnumColor.DARK_BLUE + "[Mekanism]" + EnumColor.GREY + " " + MekanismUtils.localize("tooltip.configurator.toggleColor") + ": " + (color != null ? color.getName() : EnumColor.BLACK + MekanismUtils.localize("gui.none"))));
		
		return true;
	}
	
	@Override
	public EnumColor getColor()
	{
		return color;
	}
	
	@Override
	public void setColor(EnumColor c)
	{
		color = c;
	}
	
	@Override
	public TileEntity getTile()
	{
		return tile();
	}
	
	@Override
	public EnumColor getRenderColor()
	{
		return color;
	}
}
