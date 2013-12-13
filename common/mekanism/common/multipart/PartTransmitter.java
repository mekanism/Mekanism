package mekanism.common.multipart;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import mekanism.api.Object3D;
import mekanism.api.transmitters.DynamicNetwork;
import mekanism.api.transmitters.ITransmitter;
import mekanism.api.transmitters.TransmissionType;
import mekanism.api.transmitters.TransmissionType.Size;
import mekanism.api.transmitters.TransmitterNetworkRegistry;
import mekanism.client.render.RenderPartTransmitter;
import mekanism.common.IConfigurable;
import mekanism.common.ITileNetwork;
import mekanism.common.Mekanism;
import mekanism.common.item.ItemConfigurator;
import net.minecraft.client.particle.EffectRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.common.ForgeDirection;
import buildcraft.api.tools.IToolWrench;
import codechicken.lib.data.MCDataInput;
import codechicken.lib.data.MCDataOutput;
import codechicken.lib.lighting.LazyLightMatrix;
import codechicken.lib.raytracer.IndexedCuboid6;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Vector3;
import codechicken.microblock.IHollowConnect;
import codechicken.multipart.IconHitEffects;
import codechicken.multipart.JIconHitEffects;
import codechicken.multipart.JNormalOcclusion;
import codechicken.multipart.NormalOcclusionTest;
import codechicken.multipart.PartMap;
import codechicken.multipart.TMultiPart;
import codechicken.multipart.TSlottedPart;
import codechicken.multipart.TileMultipart;

import com.google.common.io.ByteArrayDataInput;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public abstract class PartTransmitter<N extends DynamicNetwork<?, N>> extends TMultiPart implements TSlottedPart, JNormalOcclusion, IHollowConnect, JIconHitEffects, ITransmitter<N>, ITileNetwork, IConfigurable
{
	public int delayTicks;
	
	public N theNetwork;
	
	public static IndexedCuboid6[] smallSides = new IndexedCuboid6[7];
	public static IndexedCuboid6[] largeSides = new IndexedCuboid6[7];
	
	public ForgeDirection testingSide = null;
	
	public byte currentAcceptorConnections = 0x00;
	public byte currentTransmitterConnections = 0x00;
	
	public boolean isActive = false;
	public boolean sendDesc;
	
	static
	{
		smallSides[0] = new IndexedCuboid6(0, new Cuboid6(0.3, 0.0, 0.3, 0.7, 0.3, 0.7));
		smallSides[1] = new IndexedCuboid6(1, new Cuboid6(0.3, 0.7, 0.3, 0.7, 1.0, 0.7));
		smallSides[2] = new IndexedCuboid6(2, new Cuboid6(0.3, 0.3, 0.0, 0.7, 0.7, 0.3));
		smallSides[3] = new IndexedCuboid6(3, new Cuboid6(0.3, 0.3, 0.7, 0.7, 0.7, 1.0));
		smallSides[4] = new IndexedCuboid6(4, new Cuboid6(0.0, 0.3, 0.3, 0.3, 0.7, 0.7));
		smallSides[5] = new IndexedCuboid6(5, new Cuboid6(0.7, 0.3, 0.3, 1.0, 0.7, 0.7));
		smallSides[6] = new IndexedCuboid6(6, new Cuboid6(0.3, 0.3, 0.3, 0.7, 0.7, 0.7));
		
		largeSides[0] = new IndexedCuboid6(0, new Cuboid6(0.25, 0.0, 0.25, 0.75, 0.25, 0.75));
		largeSides[1] = new IndexedCuboid6(1, new Cuboid6(0.25, 0.75, 0.25, 0.75, 1.0, 0.75));
		largeSides[2] = new IndexedCuboid6(2, new Cuboid6(0.25, 0.25, 0.0, 0.75, 0.75, 0.25));
		largeSides[3] = new IndexedCuboid6(3, new Cuboid6(0.25, 0.25, 0.75, 0.75, 0.75, 1.0));
		largeSides[4] = new IndexedCuboid6(4, new Cuboid6(0.0, 0.25, 0.25, 0.25, 0.75, 0.75));
		largeSides[5] = new IndexedCuboid6(5, new Cuboid6(0.75, 0.25, 0.25, 1.0, 0.75, 0.75));
		largeSides[6] = new IndexedCuboid6(6, new Cuboid6(0.25, 0.25, 0.25, 0.75, 0.75, 0.75));
	}

	public static TMultiPart getPartType(TransmissionType type)
	{
		switch(type)
		{
			case ENERGY:
				return new PartUniversalCable();
			case FLUID:
				return new PartMechanicalPipe();
			case GAS:
				return new PartPressurizedTube();
			default:
				return null;
		}
	}

    public Size getTransmitterSize()
    {
        return getTransmissionType().transmitterSize;
    }

    public abstract Icon getCenterIcon();

    public abstract Icon getSideIcon();

	
	@Override
	public void bind(TileMultipart t)
	{
		if(tile() != null && theNetwork != null)
		{
			getTransmitterNetwork().transmitters.remove(tile());
			super.bind(t);
			getTransmitterNetwork().transmitters.add((ITransmitter<N>) tile());
		}
		else {
			super.bind(t);
		}
	}
	
	@Override
	public void update()
	{
        if(world().isRemote)
        {
            if(delayTicks == 3)
            {
                delayTicks++;
                refreshTransmitterNetwork();
            }
            else if(delayTicks < 3)
            {
                delayTicks++;
            }
        }

        if(sendDesc)
		{
			sendDescUpdate();
			sendDesc = false;
		}
	}

	public static boolean connectionMapContainsSide(byte connections, ForgeDirection side)
	{
		byte tester = (byte) (1 << side.ordinal());
		return (connections & tester) > 0;
	}
	
	public byte getPossibleTransmitterConnections()
	{
		byte connections = 0x00;
		
		if(world().isBlockIndirectlyGettingPowered(x(), y(), z()))
		{
			return connections;
		}
		
		for(ForgeDirection side : ForgeDirection.VALID_DIRECTIONS)
		{
			if(canConnectMutual(side))
			{
				TileEntity tileEntity = Object3D.get(tile()).getFromSide(side).getTileEntity(world());
				
				if(TransmissionType.checkTransmissionType(tileEntity, getTransmissionType()))
				{
					connections |= 1 << side.ordinal();
				}
			}
		}
		
		return connections;
	}
	
	public byte getPossibleAcceptorConnections()
	{
		byte connections = 0x00;
		
		if(world().isBlockIndirectlyGettingPowered(x(), y(), z()))
		{
				return connections;
		}
			
		for(ForgeDirection side : ForgeDirection.VALID_DIRECTIONS)
		{
			if(canConnectMutual(side))
			{
				TileEntity tileEntity = Object3D.get(tile()).getFromSide(side).getTileEntity(world());
				
				if(isValidAcceptor(tileEntity, side))
				{
					connections |= 1 << side.ordinal();
				}
				
			}
		}
		
		return connections;		
	}
	
	@Override
	public void refreshTransmitterNetwork()
	{
		byte possibleTransmitters = getPossibleTransmitterConnections();
		byte possibleAcceptors = getPossibleAcceptorConnections();
		
		if(possibleTransmitters != currentTransmitterConnections)
		{
			byte or = (byte)(possibleTransmitters | currentTransmitterConnections);
			
			if(or != possibleTransmitters)
			{
				((DynamicNetwork<?, N>)getTransmitterNetwork()).split((ITransmitter<N>)tile());
				setTransmitterNetwork(null);
			}
			
			for(ForgeDirection side : ForgeDirection.VALID_DIRECTIONS)
			{
				if(connectionMapContainsSide(possibleTransmitters, side))
				{
					TileEntity tileEntity = Object3D.get(tile()).getFromSide(side).getTileEntity(world());
					
					if(TransmissionType.checkTransmissionType(tileEntity, getTransmissionType()))
					{
						((DynamicNetwork<?,N>)getTransmitterNetwork()).merge(((ITransmitter<N>)tileEntity).getTransmitterNetwork());
					}
				}
			}
		}

		((DynamicNetwork<?,N>)getTransmitterNetwork()).refresh();
		
		if(!world().isRemote)
		{
			currentTransmitterConnections = possibleTransmitters;
			currentAcceptorConnections = possibleAcceptors;
			
			sendDesc = true;
		}
	}
	
	public byte getAllCurrentConnections()
	{
		return (byte)(currentTransmitterConnections | currentAcceptorConnections);
	}
	
	@Override
	public boolean occlusionTest(TMultiPart other)
	{
		return NormalOcclusionTest.apply(this, other);
	}

	@Override
	public Iterable<IndexedCuboid6> getSubParts()
	{
		Set<IndexedCuboid6> subParts = new HashSet<IndexedCuboid6>();
		
		if(tile() != null)
		{
			for(ForgeDirection side : ForgeDirection.VALID_DIRECTIONS)
			{
				int ord = side.ordinal();
				byte connections = getAllCurrentConnections();
				
				if(connectionMapContainsSide(connections, side) || side == testingSide) 
				{
					subParts.add(getTransmissionType().transmitterSize == Size.SMALL ? smallSides[ord] : largeSides[ord]);
				}
			}
		}
		
		subParts.add(getTransmissionType().transmitterSize == Size.SMALL ? smallSides[6] : largeSides[6]);
		
		return subParts;
	}
	
	@Override
	public Iterable<Cuboid6> getCollisionBoxes()
	{
		Set<Cuboid6> collisionBoxes = new HashSet<Cuboid6>();
		collisionBoxes.addAll((Collection<? extends Cuboid6>)getSubParts());
		return collisionBoxes;
	}

	@Override
	public Iterable<Cuboid6> getOcclusionBoxes()
	{
		return getCollisionBoxes();
	}
	
	@Override
	public int getSlotMask()
	{
		return PartMap.CENTER.mask;
	}
	
	@Override
	public Icon getBreakingIcon(Object subPart, int side)
	{
        return getCenterIcon();
	}
	
	@Override
	public Icon getBrokenIcon(int side)
	{
		return getCenterIcon();
	}
	
	@Override
	public Cuboid6 getBounds()
	{
		return smallSides[6];
	}
	
	@Override
	public int getHollowSize()
	{
		return getTransmissionType().transmitterSize == Size.SMALL ? 7 : 8;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void renderStatic(Vector3 pos, LazyLightMatrix olm, int pass)
	{
		RenderPartTransmitter.getInstance().renderStatic(this);
	}
	
    @Override
    public void addHitEffects(MovingObjectPosition hit, EffectRenderer effectRenderer)
    {
        IconHitEffects.addHitEffects(this, hit, effectRenderer);
    }
    
    @Override
    public void addDestroyEffects(EffectRenderer effectRenderer)
    {
        IconHitEffects.addDestroyEffects(this, effectRenderer, false);
    }
    
	@Override
	public void setTransmitterNetwork(N network)
	{
		if(network != theNetwork)
		{
			removeFromTransmitterNetwork();
			theNetwork = network;
		}
	}
	
	@Override
	public boolean areTransmitterNetworksEqual(TileEntity tileEntity)
	{
		return tileEntity instanceof ITransmitter && getTransmissionType() == ((ITransmitter)tileEntity).getTransmissionType();
	}
	
	@Override
	public N getTransmitterNetwork()
	{
		return getTransmitterNetwork(true);
	}
	
	@Override
	public N getTransmitterNetwork(boolean createIfNull)
	{
		if(theNetwork == null && createIfNull)
		{
			byte possibleTransmitters = getPossibleTransmitterConnections();
			HashSet<N> connectedNets = new HashSet<N>();
			
			for(ForgeDirection side : ForgeDirection.VALID_DIRECTIONS)
			{
				if(connectionMapContainsSide(possibleTransmitters, side))
				{
					TileEntity cable = Object3D.get(tile()).getFromSide(side).getTileEntity(world());
					if(TransmissionType.checkTransmissionType(cable, TransmissionType.ENERGY) && ((ITransmitter<N>)cable).getTransmitterNetwork(false) != null)
					{
						connectedNets.add(((ITransmitter<N>)cable).getTransmitterNetwork());
					}
				}
			}
			
			if(connectedNets.size() == 0)
			{
				theNetwork = createNetworkFromSingleTransmitter((ITransmitter<N>)tile());
			}
			else if(connectedNets.size() == 1)
			{
				theNetwork = connectedNets.iterator().next();
				theNetwork.transmitters.add((ITransmitter<N>)tile());
			}
			else {
				theNetwork = createNetworkByMergingSet(connectedNets);
				theNetwork.transmitters.add((ITransmitter<N>)tile());
			}
		}
		
		return theNetwork;
	}
	
	@Override
	public void removeFromTransmitterNetwork()
	{
		if(theNetwork != null)
		{
			theNetwork.removeTransmitter((ITransmitter<N>) tile());
		}
	}

	@Override
	public void fixTransmitterNetwork()
	{
		getTransmitterNetwork().fixMessedUpNetwork((ITransmitter<N>) tile());
	}
	
	public abstract boolean isValidAcceptor(TileEntity tile, ForgeDirection side);
	
	public abstract N createNetworkFromSingleTransmitter(ITransmitter<N> transmitter);
	
	public abstract N createNetworkByMergingSet(Set<N> networks);
	
	@Override
	public boolean canConnectMutual(ForgeDirection side)
	{
		if(!canConnect(side)) return false;
		
		TileEntity tile = Object3D.get(tile()).getFromSide(side).getTileEntity(world());
		return (!(tile instanceof ITransmitter) || ((ITransmitter<?>)tile).canConnect(side.getOpposite()));
	}
	
	@Override
	public boolean canConnect(ForgeDirection side)
	{
		if(world().isBlockIndirectlyGettingPowered(x(), y(), z()))
		{
			return false;
		}
		
		testingSide = side;
		boolean unblocked = tile().canReplacePart(this, this);
		testingSide = null;
		return unblocked;
	}
	
	@Override
	public void readDesc(MCDataInput packet)
	{
		currentTransmitterConnections = packet.readByte();
		currentAcceptorConnections = packet.readByte();
		isActive = packet.readBoolean();
	}
	
	@Override
	public void onChunkUnload()
	{
		super.onChunkUnload();
		
		getTransmitterNetwork().split(this);
		
		if(!world().isRemote)
		{
			TransmitterNetworkRegistry.getInstance().pruneEmptyNetworks();
		}
	}
	
	@Override
	public void writeDesc(MCDataOutput packet)
	{
		packet.writeByte(currentTransmitterConnections);
		packet.writeByte(currentAcceptorConnections);
		packet.writeBoolean(isActive);
	}
	
	@Override
	public boolean activate(EntityPlayer player, MovingObjectPosition part, ItemStack item)
	{
		if(item == null)
			return false;
		if(item.getItem() instanceof ItemConfigurator && player.isSneaking())
		{
			isActive ^= true;
			tile().markRender();
			return true;
		}
		if(item.getItem() instanceof IToolWrench && player.isSneaking())
		{
			if(!world().isRemote)
			{
				tile().dropItems(getDrops());
				tile().remPart(this);
			}
			
			return true;
		}
			
		return false;
	}

	@Override
	public Iterable<ItemStack> getDrops()
	{
		return Collections.singletonList(pickItem(null));
	}
	
	@Override
	public ItemStack pickItem(MovingObjectPosition hit)
	{
		return new ItemStack(Mekanism.PartTransmitter, 1, getTransmissionType().ordinal());
	}
	
	@Override
	public void preRemove()
	{
		if(tile() instanceof ITransmitter)
		{
			getTransmitterNetwork().split((ITransmitter<N>)tile());
			
			if(!world().isRemote)
			{
				TransmitterNetworkRegistry.getInstance().pruneEmptyNetworks();
			}
		}

		super.preRemove();
	}
	
	@Override
	public boolean doesTick()
	{
		return true;
	}
	
	@Override
	public void chunkLoad() {}

	@Override
	public void onAdded()
	{
		super.onAdded();
		refreshTransmitterNetwork();
	}

	@Override
	public void onChunkLoad()
	{
		super.onChunkLoad();
		refreshTransmitterNetwork();
	}
	
	@Override
	public void onNeighborChanged()
	{
		super.onNeighborChanged();
		refreshTransmitterNetwork();
	}
	
	@Override
	public void onPartChanged(TMultiPart part)
	{
		super.onPartChanged(part);
		refreshTransmitterNetwork();
	}
	
	@Override
	public void handlePacketData(ByteArrayDataInput dataStream) throws Exception {}

	@Override
	public ArrayList getNetworkedData(ArrayList data)
	{
		return data;
	}
	
	@Override
	public boolean onSneakRightClick(EntityPlayer player, int side)
	{
		return false;
	}

	@Override
	public boolean onRightClick(EntityPlayer player, int side)
	{
		fixTransmitterNetwork();
		return true;
	}
}
