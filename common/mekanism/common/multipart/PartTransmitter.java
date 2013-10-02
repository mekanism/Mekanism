package mekanism.common.multipart;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import buildcraft.api.tools.IToolWrench;
import net.minecraft.client.particle.EffectRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.common.ForgeDirection;
import mekanism.api.Object3D;
import mekanism.api.transmitters.DynamicNetwork;
import mekanism.api.transmitters.ITransmitter;
import mekanism.api.transmitters.TransmissionType;
import mekanism.client.render.RenderPartTransmitter;
import mekanism.common.Mekanism;
import mekanism.common.item.ItemConfigurator;
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
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;


public abstract class PartTransmitter<N extends DynamicNetwork<?,N, D>, D> extends TMultiPart implements TSlottedPart, JNormalOcclusion, IHollowConnect, JIconHitEffects, ITransmitter<N, D>
{
	public N theNetwork;
	public D transmitting;
	public static IndexedCuboid6[] sides = new IndexedCuboid6[7];
	public ForgeDirection testingSide = null;
	public byte currentAcceptorConnections = 0x00;
	public byte currentTransmitterConnections = 0x00;
	public boolean isActive = false;
	
	static
	{
		sides[0] = new IndexedCuboid6(0, new Cuboid6(0.3, 0.0, 0.3, 0.7, 0.3, 0.7));
		sides[1] = new IndexedCuboid6(1, new Cuboid6(0.3, 0.7, 0.3, 0.7, 1.0, 0.7));
		sides[2] = new IndexedCuboid6(2, new Cuboid6(0.3, 0.3, 0.0, 0.7, 0.7, 0.3));
		sides[3] = new IndexedCuboid6(3, new Cuboid6(0.3, 0.3, 0.7, 0.7, 0.7, 1.0));
		sides[4] = new IndexedCuboid6(4, new Cuboid6(0.0, 0.3, 0.3, 0.3, 0.7, 0.7));
		sides[5] = new IndexedCuboid6(5, new Cuboid6(0.7, 0.3, 0.3, 1.0, 0.7, 0.7));
		sides[6] = new IndexedCuboid6(6, new Cuboid6(0.3, 0.3, 0.3, 0.7, 0.7, 0.7));
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
			case ITEM:
				return new PartLogisticalTransporter();
			default:
				return null;
		}
	}
	
	@Override
	public void bind(TileMultipart t)
	{
		if (tile() != null && theNetwork != null)
		{
			this.getTransmitterNetwork().transmitters.remove(tile());
			super.bind(t);
			this.getTransmitterNetwork().transmitters.add((ITransmitter<N, D>) tile());
		}
		else
		{
			super.bind(t);
		}
	}

	public static boolean connectionMapContainsSide(byte connections, ForgeDirection side)
	{
		byte tester = (byte) (1 << side.ordinal());
		return ((connections & tester) > 0);
	}
	
	public byte getPossibleTransmitterConnections()
	{
		byte connections = 0x00;
		if(world().isBlockIndirectlyGettingPowered(x(), y(), z()))
			return connections;
		
		for(ForgeDirection side : ForgeDirection.VALID_DIRECTIONS)
		{
			if (canConnectMutual(side))
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
				return connections;
			
		for(ForgeDirection side : ForgeDirection.VALID_DIRECTIONS)
		{
			if (canConnectMutual(side))
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
		if(!world().isRemote)
		{
			byte possibleTransmitters = getPossibleTransmitterConnections();
			byte possibleAcceptors = getPossibleAcceptorConnections();
			
			if(possibleTransmitters != currentTransmitterConnections)
			{
				byte or = (byte)(possibleTransmitters | currentTransmitterConnections);
				if(or != possibleTransmitters)
				{
					((DynamicNetwork<?,N, D>)getTransmitterNetwork()).split((ITransmitter<N, D>)tile());
					setTransmitterNetwork(null);
				}
				
				for(ForgeDirection side : ForgeDirection.VALID_DIRECTIONS)
				{
					if(connectionMapContainsSide(possibleTransmitters, side))
					{
						TileEntity tileEntity = Object3D.get(tile()).getFromSide(side).getTileEntity(world());
						
						if(TransmissionType.checkTransmissionType(tileEntity, getTransmissionType()))
							((DynamicNetwork<?,N, D>)getTransmitterNetwork()).merge(((ITransmitter<N, D>)tileEntity).getTransmitterNetwork());
					}
				}
			}
			currentTransmitterConnections = possibleTransmitters;
			currentAcceptorConnections = possibleAcceptors;

			((DynamicNetwork<?,N, D>)getTransmitterNetwork()).refresh();
			
			sendDescUpdate();
		}
	}
	
	public byte getAllCurrentConnections()
	{
		return (byte) (currentTransmitterConnections | currentAcceptorConnections);
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
				if(connectionMapContainsSide(connections, side) || side == this.testingSide) subParts.add(sides[ord]);
			}
		}
		subParts.add(sides[6]);
		return subParts;
	}
	
	@Override
	public Iterable<Cuboid6> getCollisionBoxes()
	{
		Set<Cuboid6> collisionBoxes = new HashSet<Cuboid6>();
		collisionBoxes.addAll((Collection<? extends Cuboid6>) getSubParts());
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
		return RenderPartTransmitter.getInstance().getIconForPart(this);
	}
	
	@Override
	public Icon getBrokenIcon(int side)
	{
		return RenderPartTransmitter.getInstance().getIconForPart(this);
	}
	
	@Override
	public Cuboid6 getBounds()
	{
		return sides[6];
	}
	
	@Override
	public int getHollowSize()
	{
		return 7;
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
					if(TransmissionType.checkTransmissionType(cable, TransmissionType.ENERGY) && ((ITransmitter<N, D>)cable).getTransmitterNetwork(false) != null)
					{
						connectedNets.add(((ITransmitter<N, D>)cable).getTransmitterNetwork());
					}
				}
			}
			
			if(connectedNets.size() == 0 || world().isRemote)
			{
				theNetwork = createNetworkFromSingleTransmitter((ITransmitter<N, D>)tile());
			}
			else if(connectedNets.size() == 1)
			{
				theNetwork = connectedNets.iterator().next();
				theNetwork.transmitters.add((ITransmitter<N, D>)tile());
			}
			else {
				theNetwork = createNetworkByMergingSet(connectedNets);
				theNetwork.transmitters.add((ITransmitter<N, D>)tile());
			}
		}
		
		return theNetwork;
	}
	
	@Override
	public void removeFromTransmitterNetwork()
	{
		if(theNetwork != null)
		{
			theNetwork.removeTransmitter((ITransmitter<N, D>) tile());
		}
	}

	@Override
	public void fixTransmitterNetwork()
	{
		getTransmitterNetwork().fixMessedUpNetwork((ITransmitter<N, D>) tile());
	}
	
	public abstract boolean isValidAcceptor(TileEntity tile, ForgeDirection side);
	
	public abstract N createNetworkFromSingleTransmitter(ITransmitter<N, D> transmitter);
	
	public abstract N createNetworkByMergingSet(Set<N> networks);
	
	@Override
	public boolean canConnectMutual(ForgeDirection side)
	{
		if(!canConnect(side)) return false;
		
		TileEntity tile = Object3D.get(tile()).getFromSide(side).getTileEntity(world());
		return (!(tile instanceof ITransmitter) || ((ITransmitter<?, ?>)tile).canConnect(side.getOpposite()));
	}
	
	@Override
	public boolean canConnect(ForgeDirection side)
	{
		if(world().isBlockIndirectlyGettingPowered(x(), y(), z()))
			return false;
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
	
	public void writeDesc(MCDataOutput packet)
	{
		packet.writeByte(currentTransmitterConnections);
		packet.writeByte(currentAcceptorConnections);
		packet.writeBoolean(isActive);
	}
	
	@Override
	public boolean activate(EntityPlayer player, MovingObjectPosition part, ItemStack item)
	{
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
		return Collections.singletonList(new ItemStack(Mekanism.PartTransmitter, 1, getTransmissionType().ordinal()));
	}
	
	@Override
	public void clientUpdate(D data)
	{
		transmitting = data;	
	}
	
	@Override
	public void preRemove()
	{
		if (!this.world().isRemote && tile() instanceof ITransmitter)
		{
			this.getTransmitterNetwork().split((ITransmitter<N, D>)tile());
		}

		super.preRemove();
	}

	@Override
	public boolean doesTick()
	{
		return true;
	}

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
}
