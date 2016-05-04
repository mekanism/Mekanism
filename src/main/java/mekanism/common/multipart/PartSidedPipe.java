package mekanism.common.multipart;

import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import mekanism.api.Coord4D;
import mekanism.api.EnumColor;
import mekanism.api.IConfigurable;
import mekanism.api.MekanismConfig.client;
import mekanism.api.transmitters.IBlockableConnection;
import mekanism.api.transmitters.ITransmitter;
import mekanism.api.transmitters.TransmissionType;
import mekanism.common.MekanismItems;
import mekanism.common.Tier;
import mekanism.common.base.ITileNetwork;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.multipart.TransmitterType.Size;
import mekanism.common.util.MekanismUtils;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumFacing;
//import net.minecraft.util.IIcon;
import net.minecraft.util.ITickable;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
/*import codechicken.lib.data.MCDataInput;
import codechicken.lib.data.MCDataOutput;
import codechicken.lib.raytracer.ExtendedMOP;
import codechicken.lib.raytracer.AxisAlignedBB;
import codechicken.lib.raytracer.RayTracer;
import codechicken.lib.render.CCModel;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Vector3;
import codechicken.microblock.ISidedHollowConnect;
import codechicken.multipart.INeighborTileChange;
import codechicken.multipart.IconHitEffects;
import codechicken.multipart.JIconHitEffects;
import codechicken.multipart.JNormalOcclusion;
import codechicken.multipart.NormalOcclusionTest;
import codechicken.multipart.PartMap;
import codechicken.multipart.IMultipart;
import codechicken.multipart.TSlottedPart;*/
import mcmultipart.block.TileMultipart;
import mcmultipart.multipart.IMultipart;
import mcmultipart.multipart.IOccludingPart;
import mcmultipart.multipart.Multipart;
import mcmultipart.raytrace.PartMOP;
import mcmultipart.raytrace.RayTraceUtils;
import mcmultipart.raytrace.RayTraceUtils.RayTraceResultPart;

public abstract class PartSidedPipe extends Multipart implements IOccludingPart, /*ISlotOccludingPart, ISidedHollowConnect, JIconHitEffects, INeighborTileChange,*/ ITileNetwork, IBlockableConnection, IConfigurable, ITransmitter, ITickable
{
	public static AxisAlignedBB[] smallSides = new AxisAlignedBB[7];
	public static AxisAlignedBB[] largeSides = new AxisAlignedBB[7];

	public int delayTicks;

	public EnumFacing testingSide = null;

	public byte currentAcceptorConnections = 0x00;
	public byte currentTransmitterConnections = 0x00;

	public boolean sendDesc = false;
	public boolean redstonePowered = false;

	public boolean redstoneReactive = true;
	
	public boolean forceUpdate = false;

	public ConnectionType[] connectionTypes = {ConnectionType.NORMAL, ConnectionType.NORMAL, ConnectionType.NORMAL, ConnectionType.NORMAL, ConnectionType.NORMAL, ConnectionType.NORMAL};
	public TileEntity[] cachedAcceptors = new TileEntity[6];

	static
	{
		smallSides[0] = new AxisAlignedBB(0.3, 0.0, 0.3, 0.7, 0.3, 0.7);
		smallSides[1] = new AxisAlignedBB(0.3, 0.7, 0.3, 0.7, 1.0, 0.7);
		smallSides[2] = new AxisAlignedBB(0.3, 0.3, 0.0, 0.7, 0.7, 0.3);
		smallSides[3] = new AxisAlignedBB(0.3, 0.3, 0.7, 0.7, 0.7, 1.0);
		smallSides[4] = new AxisAlignedBB(0.0, 0.3, 0.3, 0.3, 0.7, 0.7);
		smallSides[5] = new AxisAlignedBB(0.7, 0.3, 0.3, 1.0, 0.7, 0.7);
		smallSides[6] = new AxisAlignedBB(0.3, 0.3, 0.3, 0.7, 0.7, 0.7);

		largeSides[0] = new AxisAlignedBB(0.25, 0.0, 0.25, 0.75, 0.25, 0.75);
		largeSides[1] = new AxisAlignedBB(0.25, 0.75, 0.25, 0.75, 1.0, 0.75);
		largeSides[2] = new AxisAlignedBB(0.25, 0.25, 0.0, 0.75, 0.75, 0.25);
		largeSides[3] = new AxisAlignedBB(0.25, 0.25, 0.75, 0.75, 0.75, 1.0);
		largeSides[4] = new AxisAlignedBB(0.0, 0.25, 0.25, 0.25, 0.75, 0.75);
		largeSides[5] = new AxisAlignedBB(0.75, 0.25, 0.25, 1.0, 0.75, 0.75);
		largeSides[6] = new AxisAlignedBB(0.25, 0.25, 0.25, 0.75, 0.75, 0.75);
	}

	public static IMultipart getPartType(TransmitterType type)
	{
		switch(type)
		{
			case UNIVERSAL_CABLE_BASIC:
				return new PartUniversalCable(Tier.CableTier.BASIC);
			case UNIVERSAL_CABLE_ADVANCED:
				return new PartUniversalCable(Tier.CableTier.ADVANCED);
			case UNIVERSAL_CABLE_ELITE:
				return new PartUniversalCable(Tier.CableTier.ELITE);
			case UNIVERSAL_CABLE_ULTIMATE:
				return new PartUniversalCable(Tier.CableTier.ULTIMATE);
			case MECHANICAL_PIPE_BASIC:
				return new PartMechanicalPipe(Tier.PipeTier.BASIC);
			case MECHANICAL_PIPE_ADVANCED:
				return new PartMechanicalPipe(Tier.PipeTier.ADVANCED);
			case MECHANICAL_PIPE_ELITE:
				return new PartMechanicalPipe(Tier.PipeTier.ELITE);
			case MECHANICAL_PIPE_ULTIMATE:
				return new PartMechanicalPipe(Tier.PipeTier.ULTIMATE);
			case PRESSURIZED_TUBE_BASIC:
				return new PartPressurizedTube(Tier.TubeTier.BASIC);
			case PRESSURIZED_TUBE_ADVANCED:
				return new PartPressurizedTube(Tier.TubeTier.ADVANCED);
			case PRESSURIZED_TUBE_ELITE:
				return new PartPressurizedTube(Tier.TubeTier.ELITE);
			case PRESSURIZED_TUBE_ULTIMATE:
				return new PartPressurizedTube(Tier.TubeTier.ULTIMATE);
			case LOGISTICAL_TRANSPORTER_BASIC:
				return new PartLogisticalTransporter(Tier.TransporterTier.BASIC);
			case LOGISTICAL_TRANSPORTER_ADVANCED:
				return new PartLogisticalTransporter(Tier.TransporterTier.ADVANCED);
			case LOGISTICAL_TRANSPORTER_ELITE:
				return new PartLogisticalTransporter(Tier.TransporterTier.ELITE);
			case LOGISTICAL_TRANSPORTER_ULTIMATE:
				return new PartLogisticalTransporter(Tier.TransporterTier.ULTIMATE);
			case RESTRICTIVE_TRANSPORTER:
				return new PartRestrictiveTransporter();
			case DIVERSION_TRANSPORTER:
				return new PartDiversionTransporter();
			case THERMODYNAMIC_CONDUCTOR_BASIC:
				return new PartThermodynamicConductor(Tier.ConductorTier.BASIC);
			case THERMODYNAMIC_CONDUCTOR_ADVANCED:
				return new PartThermodynamicConductor(Tier.ConductorTier.ADVANCED);
			case THERMODYNAMIC_CONDUCTOR_ELITE:
				return new PartThermodynamicConductor(Tier.ConductorTier.ELITE);
			case THERMODYNAMIC_CONDUCTOR_ULTIMATE:
				return new PartThermodynamicConductor(Tier.ConductorTier.ULTIMATE);
			default:
				return null;
		}
	}

	public static boolean connectionMapContainsSide(byte connections, EnumFacing side)
	{
		byte tester = (byte)(1 << side.ordinal());
		return (connections & tester) > 0;
	}

	public static byte setConnectionBit(byte connections, boolean toSet, EnumFacing side)
	{
		return (byte)((connections & ~(byte)(1 << side.ordinal())) | (byte)((toSet?1:0) << side.ordinal()));
	}

	public abstract TextureAtlasSprite getCenterIcon(boolean opaque);

	public abstract TextureAtlasSprite getSideIcon(boolean opaque);

	public abstract TextureAtlasSprite getSideIconRotated(boolean opaque);

	@Override
	public void update()
	{
		if(getWorld().isRemote)
		{
			if(delayTicks == 5)
			{
				delayTicks = 6; /* don't refresh again */
				refreshConnections();
			}
			else if(delayTicks < 5)
			{
				delayTicks++;
			}
		}

		if(!getWorld().isRemote)
		{
			if(forceUpdate)
			{
				refreshConnections();
				forceUpdate = false;
			}
			
			if(sendDesc)
			{
				sendUpdatePacket();
				sendDesc = false;
			}
		}
	}
	
	public boolean handlesRedstone()
	{
		return true;
	}
	
	public boolean renderCenter()
	{
		return false;
	}
	
	public boolean transparencyRender()
	{
		return false;
	}

	public TextureAtlasSprite getIconForSide(EnumFacing side, boolean opaque)
	{
		ConnectionType type = getConnectionType(side);

		if(type == ConnectionType.NONE)
		{
			if(client.oldTransmitterRender || renderCenter())
			{
				return getCenterIcon(opaque);
			}
			else if(getAllCurrentConnections() == 3 && side != EnumFacing.DOWN && side != EnumFacing.UP)
			{
				return getSideIcon(opaque);
			}
			else if(getAllCurrentConnections() == 12 && (side == EnumFacing.DOWN || side == EnumFacing.UP))
			{
				return getSideIcon(opaque);
			}
			else if(getAllCurrentConnections() == 12 && (side == EnumFacing.EAST || side == EnumFacing.WEST))
			{
				return getSideIconRotated(opaque);
			}
			else if(getAllCurrentConnections() == 48 && side != EnumFacing.EAST && side != EnumFacing.WEST)
			{
				return getSideIconRotated(opaque);
			}

			return getCenterIcon(opaque);
		}
		else {
			return getSideIcon(opaque);
		}
	}

	public byte getPossibleTransmitterConnections()
	{
		byte connections = 0x00;

		if(handlesRedstone() && redstoneReactive && redstonePowered)
		{
			return connections;
		}

		for(EnumFacing side : EnumFacing.values())
		{
			if(canConnectMutual(side))
			{
				TileEntity tileEntity = getWorld().getTileEntity(getPos().offset(side));

				if(tileEntity != null && tileEntity.hasCapability(Capabilities.GRID_TRANSMITTER_CAPABILITY, side.getOpposite())
						&& TransmissionType.checkTransmissionType(tileEntity.getCapability(Capabilities.GRID_TRANSMITTER_CAPABILITY, side.getOpposite()), getTransmitterType().getTransmission())
						&& isValidTransmitter(tileEntity))
				{
					connections |= 1 << side.ordinal();
				}
			}
		}

		return connections;
	}

	public boolean getPossibleAcceptorConnection(EnumFacing side)
	{
		if(handlesRedstone() && redstoneReactive && redstonePowered)
		{
			return false;
		}

		if(canConnectMutual(side))
		{
			TileEntity tileEntity = getWorld().getTileEntity(getPos().offset(side));

			if(isValidAcceptor(tileEntity, side))
			{
				if(cachedAcceptors[side.ordinal()] != tileEntity)
				{
					cachedAcceptors[side.ordinal()] = tileEntity;
					markDirtyAcceptor(side);
				}
				
				return true;
			}
		}
		if(cachedAcceptors[side.ordinal()] != null)
		{
			cachedAcceptors[side.ordinal()] = null;
			markDirtyAcceptor(side);
		}

		return false;
	}

	public boolean getPossibleTransmitterConnection(EnumFacing side)
	{
		if(handlesRedstone() && redstoneReactive && MekanismUtils.isGettingPowered(getWorld(), new Coord4D(getPos())))
		{
			return false;
		}

		if(canConnectMutual(side))
		{
			TileEntity tileEntity = getWorld().getTileEntity(getPos().offset(side));

			if(tileEntity.hasCapability(Capabilities.GRID_TRANSMITTER_CAPABILITY, side.getOpposite())
					&& TransmissionType.checkTransmissionType(tileEntity.getCapability(Capabilities.GRID_TRANSMITTER_CAPABILITY, side.getOpposite()), getTransmitterType().getTransmission())
					&& isValidTransmitter(tileEntity))
			{
				return true;
			}
		}

		return false;
	}

	public byte getPossibleAcceptorConnections()
	{
		byte connections = 0x00;

		if(handlesRedstone() && redstoneReactive && redstonePowered)
		{
			return connections;
		}

		for(EnumFacing side : EnumFacing.values())
		{
			if(canConnectMutual(side))
			{
				Coord4D coord = new Coord4D(getPos()).offset(side);
				
				if(!getWorld().isRemote && !coord.exists(getWorld()))
				{
					forceUpdate = true;
					continue;
				}
				
				TileEntity tileEntity = coord.getTileEntity(getWorld());

				if(isValidAcceptor(tileEntity, side))
				{
					if(cachedAcceptors[side.ordinal()] != tileEntity)
					{
						cachedAcceptors[side.ordinal()] = tileEntity;
						markDirtyAcceptor(side);
					}
					
					connections |= 1 << side.ordinal();
					continue;
				}
			}
			
			if(cachedAcceptors[side.ordinal()] != null)
			{
				cachedAcceptors[side.ordinal()] = null;
				markDirtyAcceptor(side);
			}
		}

		return connections;
	}

	public byte getAllCurrentConnections()
	{
		return (byte)(currentTransmitterConnections | currentAcceptorConnections);
	}
	
	protected boolean isValidTransmitter(TileEntity tileEntity)
	{
		return true;
	}

/*
	@Override
	public boolean occlusionTest(IMultipart other)
	{
		return NormalOcclusionTest.apply(this, other);
	}
*/

	@Override
	public void addSelectionBoxes(List<AxisAlignedBB> list)
	{
		if(getContainer() != null)
		{
			for(EnumFacing side : EnumFacing.values())
			{
				int ord = side.ordinal();
				byte connections = getAllCurrentConnections();

				if(connectionMapContainsSide(connections, side) || side == testingSide)
				{
					list.add(getTransmitterType().getSize() == Size.SMALL ? smallSides[ord] : largeSides[ord]);
				}
			}
		}

		list.add(getTransmitterType().getSize() == Size.SMALL ? smallSides[6] : largeSides[6]);
	}

	public abstract TransmitterType getTransmitterType();

	@Override
	public void addCollisionBoxes(AxisAlignedBB mask, List<AxisAlignedBB> list, Entity collidingEntity)
	{
		addSelectionBoxes(list);
	}

	@Override
	public void addOcclusionBoxes(List<AxisAlignedBB> list)
	{
		addSelectionBoxes(list);
	}
/*
	@Override
	public EnumSet<PartSlot> getSlotMask()
	{
		return EnumSet.of(PartSlot.CENTER);
	}

	@Override
	public EnumSet<PartSlot> getOccludedSlots()
	{
		return EnumSet.of(PartSlot.CENTER); //TODO implement properly
	}


	@Override
	public TextureAtlasSprite getBreakingIcon(Object subPart, EnumFacing side)
	{
		return getCenterIcon(true);
	}

	@Override
	public TextureAtlasSprite getBrokenIcon(EnumFacing side)
	{
		return getCenterIcon(true);
	}

	@Override
	public Cuboid6 getBounds()
	{
		return getTransmitterType().getSize() == Size.SMALL ? smallSides[6] : largeSides[6];
	}

	@Override
	public int getHollowSize(EnumFacing side)
	{
		EnumFacing direction = EnumFacing.getOrientation(side);

		if(connectionMapContainsSide(getAllCurrentConnections(), direction) || direction == testingSide)
		{
			return getTransmitterType().getSize().centerSize+1;
		}
		
		return 0;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean renderStatic(Vector3 pos, int pass)
	{
		if(pass == 0)
		{
			RenderPartTransmitter.getInstance().renderStatic(this, pass);
			return true;
		}
		else if(pass == 1 && transparencyRender())
		{
			RenderPartTransmitter.getInstance().renderStatic(this, pass);
			return true;
		}
		
		return false;
	}

	@Override
	public void addHitEffects(MovingObjectPosition hit, EffectRenderer effectRenderer)
	{
		IconHitEffects.addHitEffects(this, hit, effectRenderer);
	}

	@Override
	public void addDestroyEffects(MovingObjectPosition mop, EffectRenderer effectRenderer)
	{
		IconHitEffects.addDestroyEffects(this, effectRenderer, false);
	}
*/

	public abstract boolean isValidAcceptor(TileEntity tile, EnumFacing side);

	@Override
	public boolean canConnectMutual(EnumFacing side)
	{
		if(!canConnect(side)) return false;

		TileEntity tile = getWorld().getTileEntity(getPos().offset(side));
		return (!(tile instanceof IBlockableConnection) || ((IBlockableConnection)tile).canConnect(side.getOpposite()));
	}

	@Override
	public boolean canConnect(EnumFacing side)
	{
		if(handlesRedstone() && redstoneReactive && redstonePowered)
		{
			return false;
		}

		testingSide = side;
		boolean unblocked = true;//getContainer().canReplacePart(this, this);
		testingSide = null;
		
		return unblocked;
	}

	@Override
	public void readUpdatePacket(PacketBuffer packet)
	{
		currentTransmitterConnections = packet.readByte();
		currentAcceptorConnections = packet.readByte();

		for(int i = 0; i < 6; i++)
		{
			connectionTypes[i] = ConnectionType.values()[packet.readInt()];
		}

		notifyPartUpdate();
		markRenderUpdate();
	}

	@Override
	public void writeUpdatePacket(PacketBuffer packet)
	{
		packet.writeByte(currentTransmitterConnections);
		packet.writeByte(currentAcceptorConnections);

		for(int i = 0; i < 6; i++)
		{
			packet.writeInt(connectionTypes[i].ordinal());
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound nbtTags)
	{
		super.readFromNBT(nbtTags);

		redstoneReactive = nbtTags.getBoolean("redstoneReactive");

		for(int i = 0; i < 6; i++)
		{
			connectionTypes[i] = ConnectionType.values()[nbtTags.getInteger("connection" + i)];
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound nbtTags)
	{
		super.writeToNBT(nbtTags);

		nbtTags.setBoolean("redstoneReactive", redstoneReactive);

		for(int i = 0; i < 6; i++)
		{
			nbtTags.setInteger("connection" + i, connectionTypes[i].ordinal());
		}
	}

	@Override
	public boolean onActivated(EntityPlayer player, ItemStack stack, PartMOP hit)
	{
		if(stack == null)
		{
			return false;
		}

		if(MekanismUtils.hasUsableWrench(player, getPos()) && player.isSneaking())
		{
			if(!getWorld().isRemote)
			{
				//TODO tile().dropItems(getDrops());
				getContainer().removePart(this);
			}

			return true;
		}

		return false;
	}

	@Override
	public List<ItemStack> getDrops()
	{
		return Collections.singletonList(getPickBlock(null, null));
	}

	@Override
	public ItemStack getPickBlock(EntityPlayer player, PartMOP hit)
	{
		return new ItemStack(MekanismItems.PartTransmitter, 1, getTransmitterType().ordinal());
	}
	
	protected void onRefresh() {}

	public void refreshConnections()
	{
		if(redstoneReactive)
		{
			redstonePowered = MekanismUtils.isGettingPowered(getWorld(), new Coord4D(getPos()));
		}
		else {
			redstonePowered = false;
		}

		if(!getWorld().isRemote)
		{
			byte possibleTransmitters = getPossibleTransmitterConnections();
			byte possibleAcceptors = getPossibleAcceptorConnections();
			
			if((possibleTransmitters | possibleAcceptors) != getAllCurrentConnections())
			{
				sendDesc = true;
			}

			currentTransmitterConnections = possibleTransmitters;
			currentAcceptorConnections = possibleAcceptors;
		}
	}

	public void refreshConnections(EnumFacing side)
	{
		if(redstoneReactive)
		{
			redstonePowered = MekanismUtils.isGettingPowered(getWorld(), new Coord4D(getPos()));
		}
		else {
			redstonePowered = false;
		}

		boolean possibleTransmitter = getPossibleTransmitterConnection(side);
		boolean possibleAcceptor = getPossibleAcceptorConnection(side);

		if(!getWorld().isRemote)
		{
			if((possibleTransmitter || possibleAcceptor) != connectionMapContainsSide(getAllCurrentConnections(), side))
			{
				sendDesc = true;
			}
			
			currentTransmitterConnections = setConnectionBit(currentTransmitterConnections, possibleTransmitter, side);
			currentAcceptorConnections = setConnectionBit(currentAcceptorConnections, possibleAcceptor, side);
		}
	}

	protected void onModeChange(EnumFacing side)
	{
		markDirtyAcceptor(side);
	}

	protected void markDirtyTransmitters()
	{
		notifyTileChange();
	}

	protected void markDirtyAcceptor(EnumFacing side) {}

	@Override
	public void onAdded()
	{
		super.onAdded();
		
		refreshConnections();
	}

	@Override
	public void onLoaded()
	{
		super.onLoaded();
		
		//refreshConnections(); TODO causes StackOverflow. Why?
		//notifyTileChange();
	}
	
	@Override
	public void onNeighborTileChange(EnumFacing side) 
	{
		refreshConnections(side);
	}

	@Override
	public void onNeighborBlockChange(Block block)
	{
		if(handlesRedstone())
		{
			boolean prevPowered = redstonePowered;
			refreshConnections();
			
			if(prevPowered != redstonePowered)
			{
				markDirtyTransmitters();
			}
		}
	}

	@Override
	public void onPartChanged(IMultipart part)
	{
		super.onPartChanged(part);
		
		byte transmittersBefore = currentTransmitterConnections;
		refreshConnections();
		
		if(transmittersBefore != currentTransmitterConnections)
		{
			markDirtyTransmitters();
		}
	}

	@Override
	public void handlePacketData(ByteBuf dataStream) throws Exception {}

	@Override
	public ArrayList<Object> getNetworkedData(ArrayList<Object> data)
	{
		return data;
	}

	public ConnectionType getConnectionType(EnumFacing side)
	{
		if(!connectionMapContainsSide(getAllCurrentConnections(), side))
		{
			return ConnectionType.NONE;
		}
		else if(connectionMapContainsSide(currentTransmitterConnections, side))
		{
			return ConnectionType.NORMAL;
		}

		return connectionTypes[side.ordinal()];
	}

	public List<EnumFacing> getConnections(ConnectionType type)
	{
		List<EnumFacing> sides = new ArrayList<EnumFacing>();

		for(EnumFacing side : EnumFacing.values())
		{
			if(getConnectionType(side) == type)
			{
				sides.add(side);
			}
		}

		return sides;
	}

/*
	public String getModelForSide(EnumFacing side, boolean internal)
	{
		String sideName = side.name().toLowerCase();
		String typeName = getConnectionType(side).name().toUpperCase();
		String name = sideName + typeName;

		if(internal)
		{
			return RenderPartTransmitter.contents_models.get(name);
		}
		else {
			if(getTransmitterType().getSize() == Size.LARGE)
			{
				return RenderPartTransmitter.large_models.get(name);
			}
			else {
				return RenderPartTransmitter.small_models.get(name);
			}
		}
	}
*/

	@Override
	public boolean onSneakRightClick(EntityPlayer player, EnumFacing side)
	{
		if(!getWorld().isRemote)
		{
			PartMOP hit = reTrace(getWorld(), getPos(), player);
	
			if(hit == null)
			{
				return false;
			}
			else if(hit.subHit < 6)
			{
				connectionTypes[hit.subHit] = connectionTypes[hit.subHit].next();
				sendDesc = true;
	
				onModeChange(EnumFacing.getFront(hit.subHit));
				player.addChatMessage(new ChatComponentText("Connection type changed to " + connectionTypes[hit.subHit].toString()));
	
				return true;
			}
			else {
				return onConfigure(player, hit.subHit, side);
			}
		}
		
		return true;
	}

	private PartMOP reTrace(World world, BlockPos pos, EntityPlayer player) {

		Vec3 start = RayTraceUtils.getStart(player);
		Vec3 end = RayTraceUtils.getEnd(player);
		RayTraceResultPart result = ((TileMultipart)world.getTileEntity(pos)).getPartContainer().collisionRayTrace(start, end);
		return result == null ? null : result.hit;
	}


	protected boolean onConfigure(EntityPlayer player, int part, EnumFacing side)
	{
		return false;
	}

	public EnumColor getRenderColor(boolean opaque)
	{
		return null;
	}

	@Override
	public boolean onRightClick(EntityPlayer player, EnumFacing side)
	{
		if(!getWorld().isRemote && handlesRedstone())
		{
			redstoneReactive ^= true;
			refreshConnections();
			notifyTileChange();

			player.addChatMessage(new ChatComponentText(EnumColor.DARK_BLUE + "[Mekanism]" + EnumColor.GREY + " Redstone sensitivity turned " + EnumColor.INDIGO + (redstoneReactive ? "on." : "off.")));
		}
		
		return true;
	}

	public static enum ConnectionType
	{
		NORMAL,
		PUSH,
		PULL,
		NONE;

		public ConnectionType next()
		{
			if(ordinal() == values().length-1)
			{
				return NORMAL;
			}

			return values()[ordinal()+1];
		}
	}

	public void notifyTileChange()
	{
		MekanismUtils.notifyLoadedNeighborsOfTileChange(getWorld(), new Coord4D(getPos()));
	}
}
