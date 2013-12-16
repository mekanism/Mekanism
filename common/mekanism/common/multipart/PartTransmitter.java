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
import mekanism.client.ClientTickHandler;
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

public abstract class PartTransmitter<N extends DynamicNetwork<?, N>> extends PartSidedPipe implements ITransmitter<N>, IConfigurable
{
	public N theNetwork;
	
	@Override
	public void bind(TileMultipart t)
	{
		if(tile() != null && theNetwork != null)
		{
			getTransmitterNetwork().transmitters.remove(tile());
			super.bind(t);
			getTransmitterNetwork().transmitters.add((ITransmitter<N>)tile());
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
            if(delayTicks == 5)
            {
                delayTicks = 6; /* don't refresh again */
                refreshTransmitterNetwork();
            }
            else if(delayTicks < 5)
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
		byte tester = (byte)(1 << side.ordinal());
		return (connections & tester) > 0;
	}
	
	@Override
	public void refreshTransmitterNetwork()
	{
		byte possibleTransmitters = getPossibleTransmitterConnections();
		byte possibleAcceptors = getPossibleAcceptorConnections();
		
		if(possibleTransmitters != currentTransmitterConnections)
		{
			//TODO @unpairedbracket, I don't think this is necessary; I couldn't tell a difference without it,
			//and it results in many extra possible recursive calls on the network
			
			/*byte or = (byte)(possibleTransmitters | currentTransmitterConnections);
			
			if(or != possibleTransmitters)
			{
				((DynamicNetwork<?, N>)getTransmitterNetwork()).split((ITransmitter<N>)tile());
				setTransmitterNetwork(null);
			}*/
			
			for(ForgeDirection side : ForgeDirection.VALID_DIRECTIONS)
			{
				if(connectionMapContainsSide(possibleTransmitters, side))
				{
					TileEntity tileEntity = Object3D.get(tile()).getFromSide(side).getTileEntity(world());
					
					if(TransmissionType.checkTransmissionType(tileEntity, getTransmissionType()) && isConnectable(tileEntity))
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
					
					if(TransmissionType.checkTransmissionType(cable, getTransmissionType()) && ((ITransmitter<N>)cable).getTransmitterNetwork(false) != null)
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
			theNetwork.removeTransmitter((ITransmitter<N>)tile());
		}
	}

	@Override
	public void fixTransmitterNetwork()
	{
		getTransmitterNetwork().fixMessedUpNetwork((ITransmitter<N>) tile());
	}
	
	public abstract N createNetworkFromSingleTransmitter(ITransmitter<N> transmitter);
	
	public abstract N createNetworkByMergingSet(Set<N> networks);
	
	@Override
	public void onChunkUnload()
	{
		super.onChunkUnload();
		
		getTransmitterNetwork().split(this);
		
		if(!world().isRemote)
		{
			TransmitterNetworkRegistry.getInstance().pruneEmptyNetworks();
		}
		else {
			try {
				ClientTickHandler.killDeadNetworks();
			} catch(Exception e) {}
		}
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
			else {
				try {
					ClientTickHandler.killDeadNetworks();
				} catch(Exception e) {}
			}
		}

		super.preRemove();
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
	
	@Override
	public void chunkLoad() {}
}
