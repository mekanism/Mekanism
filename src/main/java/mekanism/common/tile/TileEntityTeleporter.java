package mekanism.common.tile;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import mekanism.api.Coord4D;
import mekanism.api.EnumColor;
import mekanism.common.Mekanism;
import mekanism.common.MekanismBlocks;
import mekanism.common.Teleporter;
import mekanism.common.block.states.BlockStateBasic;
import mekanism.common.block.states.BlockStateBasic.BasicBlock;
import mekanism.common.block.states.BlockStateBasic.BasicBlockType;
import mekanism.common.block.states.BlockStateMachine.MachineBlockType;
import mekanism.common.network.PacketPortalFX.PortalFXMessage;
import mekanism.common.network.PacketTileEntity.TileEntityMessage;
import mekanism.common.util.ChargeUtils;
import mekanism.common.util.MekanismUtils;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.S07PacketRespawn;
import net.minecraft.network.play.server.S1DPacketEntityEffect;
import net.minecraft.potion.PotionEffect;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Optional.Interface;
import net.minecraftforge.fml.common.Optional.Method;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import io.netty.buffer.ByteBuf;

import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;

@Interface(iface = "dan200.computercraft.api.peripheral.IPeripheral", modid = "ComputerCraft")
public class TileEntityTeleporter extends TileEntityElectricBlock implements IPeripheral
{
	private MinecraftServer server = MinecraftServer.getServer();

	/** This teleporter's frequency. */
	public Teleporter.Code code;

	public AxisAlignedBB teleportBounds = null;

	public Set<Entity> didTeleport = new HashSet<Entity>();

	public int teleDelay = 0;

	public boolean shouldRender;

	public boolean prevShouldRender;

	/** This teleporter's current status. */
	public byte status = 0;

	public TileEntityTeleporter()
	{
		super("Teleporter", MachineBlockType.TELEPORTER.baseEnergy);
		inventory = new ItemStack[1];
		code = new Teleporter.Code(0, 0, 0, 0);
	}

	@Override
	public void onUpdate()
	{
		super.onUpdate();

		if(teleportBounds == null)
		{
			resetBounds();
		}

		if(!worldObj.isRemote)
		{
			if(Mekanism.teleporters.containsKey(code))
			{
				if(!Mekanism.teleporters.get(code).contains(Coord4D.get(this)) && hasFrame())
				{
					Mekanism.teleporters.get(code).add(Coord4D.get(this));
				}
				else if(Mekanism.teleporters.get(code).contains(Coord4D.get(this)) && !hasFrame())
				{
					Mekanism.teleporters.get(code).remove(Coord4D.get(this));
				}
			}
			else if(hasFrame())
			{
				ArrayList<Coord4D> newCoords = new ArrayList<Coord4D>();
				newCoords.add(Coord4D.get(this));
				Mekanism.teleporters.put(code, newCoords);
			}
			
			status = canTeleport();

			if(status == 1 && teleDelay == 0)
			{
				teleport();
			}

			if(teleDelay == 0 && didTeleport.size() > 0)
			{
				cleanTeleportCache();
			}

			shouldRender = status == 1 || status > 4;

			if(shouldRender != prevShouldRender)
			{
				Mekanism.packetHandler.sendToAllAround(new TileEntityMessage(Coord4D.get(this), getNetworkedData(new ArrayList())), Coord4D.get(this).getTargetPoint(40D));
			}

			prevShouldRender = shouldRender;

			teleDelay = Math.max(0, teleDelay-1);
		}

		ChargeUtils.discharge(0, this);
	}
	
	public String getStatusDisplay()
	{
		switch(status)
		{
			case 1:
				return EnumColor.DARK_GREEN + MekanismUtils.localize("gui.teleporter.ready");
			case 2:
				return EnumColor.DARK_RED + MekanismUtils.localize("gui.teleporter.noFrame");
			case 3:
				return EnumColor.DARK_RED + MekanismUtils.localize("gui.teleporter.noLink");
			case 4:
				return EnumColor.DARK_RED + MekanismUtils.localize("gui.teleporter.exceeds");
			case 5:
				return EnumColor.DARK_RED + MekanismUtils.localize("gui.teleporter.needsEnergy");
			case 6:
				return EnumColor.DARK_GREEN + MekanismUtils.localize("gui.idle");
		}
		
		return EnumColor.DARK_RED + MekanismUtils.localize("gui.teleporter.noLink");
	}

	public void cleanTeleportCache()
	{
		List<Entity> list = worldObj.getEntitiesWithinAABB(Entity.class, teleportBounds);
		Set<Entity> teleportCopy = (Set<Entity>)((HashSet<Entity>)didTeleport).clone();

		for(Entity entity : teleportCopy)
		{
			if(!list.contains(entity))
			{
				didTeleport.remove(entity);
			}
		}
	}

	@Override
	public int[] getSlotsForFace(EnumFacing side)
	{
		return new int[] {0};
	}

	@Override
	public boolean isItemValidForSlot(int slotID, ItemStack itemstack)
	{
		if(slotID == 0)
		{
			return ChargeUtils.canBeDischarged(itemstack);
		}

		return true;
	}

	public void resetBounds()
	{
		teleportBounds = new AxisAlignedBB(getPos(), getPos().add(1, 3, 1));
	}

	/**
	 * 1: yes
	 * 2: no frame
	 * 3: no link found
	 * 4: too many links
	 * 5: not enough electricity
	 * 6: nothing to teleport
	 * @return
	 */
	public byte canTeleport()
	{
		if(!hasFrame())
		{
			return 2;
		}

		if(!Mekanism.teleporters.containsKey(code) || Mekanism.teleporters.get(code).isEmpty())
		{
			return 3;
		}

		if(Mekanism.teleporters.get(code).size() > 2)
		{
			return 4;
		}

		if(Mekanism.teleporters.get(code).size() == 2)
		{
			List<Entity> entitiesInPortal = getToTeleport();

			Coord4D closestCoords = null;

			for(Coord4D coords : Mekanism.teleporters.get(code))
			{
				if(!coords.equals(Coord4D.get(this)))
				{
					closestCoords = coords;
					break;
				}
			}

			int electricityNeeded = 0;

			for(Entity entity : entitiesInPortal)
			{
				electricityNeeded += calculateEnergyCost(entity, closestCoords);
			}

			if(entitiesInPortal.size() == 0)
			{
				return 6;
			}

			if(getEnergy() < electricityNeeded)
			{
				return 5;
			}

			return 1;
		}

		return 3;
	}

	public void teleport()
	{
		if(worldObj.isRemote) return;

		List<Entity> entitiesInPortal = getToTeleport();

		Coord4D closestCoords = null;

		for(Coord4D coords : Mekanism.teleporters.get(code))
		{
			if(!coords.equals(Coord4D.get(this)))
			{
				closestCoords = coords;
				break;
			}
		}

		for(Entity entity : entitiesInPortal)
		{
			World teleWorld = FMLCommonHandler.instance().getMinecraftServerInstance().worldServerForDimension(closestCoords.dimensionId);
			TileEntityTeleporter teleporter = (TileEntityTeleporter)closestCoords.getTileEntity(teleWorld);

			if(teleporter != null)
			{
				teleporter.didTeleport.add(entity);
				teleporter.teleDelay = 5;

				if(entity instanceof EntityPlayerMP)
				{
					teleportPlayerTo((EntityPlayerMP)entity, closestCoords, teleporter);
				}
				else {
					teleportEntityTo(entity, closestCoords, teleporter);
				}

				for(Coord4D coords : Mekanism.teleporters.get(code))
				{
					Mekanism.packetHandler.sendToAllAround(new PortalFXMessage(coords), coords.getTargetPoint(40D));
				}

				setEnergy(getEnergy() - calculateEnergyCost(entity, closestCoords));

				worldObj.playSoundAtEntity(entity, "mob.endermen.portal", 1.0F, 1.0F);
			}
		}
	}

	public void teleportPlayerTo(EntityPlayerMP player, Coord4D coord, TileEntityTeleporter teleporter)
	{
		if(player.dimension != coord.dimensionId)
		{
			int id = player.dimension;
			WorldServer oldWorld = server.worldServerForDimension(player.dimension);
			player.dimension = coord.dimensionId;
			WorldServer newWorld = server.worldServerForDimension(player.dimension);
			player.playerNetServerHandler.sendPacket(new S07PacketRespawn(player.dimension, player.worldObj.getDifficulty(), newWorld.getWorldInfo().getTerrainType(), player.theItemInWorldManager.getGameType()));
			oldWorld.removePlayerEntityDangerously(player);
			player.isDead = false;

			if(player.isEntityAlive())
			{
				newWorld.spawnEntityInWorld(player);
				player.setLocationAndAngles(coord.getX()+0.5, coord.getY()+1, coord.getZ()+0.5, player.rotationYaw, player.rotationPitch);
				newWorld.updateEntityWithOptionalForce(player, false);
				player.setWorld(newWorld);
			}

			server.getConfigurationManager().func_72375_a(player, oldWorld);
			player.playerNetServerHandler.setPlayerLocation(coord.getX()+0.5, coord.getY()+1, coord.getZ()+0.5, player.rotationYaw, player.rotationPitch);
			player.theItemInWorldManager.setWorld(newWorld);
			server.getConfigurationManager().updateTimeAndWeatherForPlayer(player, newWorld);
			server.getConfigurationManager().syncPlayerInventory(player);
			Iterator iterator = player.getActivePotionEffects().iterator();

			while(iterator.hasNext())
			{
				PotionEffect potioneffect = (PotionEffect)iterator.next();
				player.playerNetServerHandler.sendPacket(new S1DPacketEntityEffect(player.getEntityId(), potioneffect));
			}

			FMLCommonHandler.instance().firePlayerChangedDimensionEvent(player, id, coord.dimensionId);
		}
		else {
			player.playerNetServerHandler.setPlayerLocation(coord.getX()+0.5, coord.getY()+1, coord.getZ()+0.5, player.rotationYaw, player.rotationPitch);
		}
	}

	public void teleportEntityTo(Entity entity, Coord4D coord, TileEntityTeleporter teleporter)
	{
		WorldServer world = server.worldServerForDimension(coord.dimensionId);

		if(entity.worldObj.provider.getDimensionId() != coord.dimensionId)
		{
			entity.worldObj.removeEntity(entity);
			entity.isDead = false;

			world.spawnEntityInWorld(entity);
			entity.setLocationAndAngles(coord.getX()+0.5, coord.getY()+1, coord.getZ()+0.5, entity.rotationYaw, entity.rotationPitch);
			world.updateEntityWithOptionalForce(entity, false);
			entity.setWorld(world);
			world.resetUpdateEntityTick();

			Entity e = EntityList.createEntityByName(EntityList.getEntityString(entity), world);

			if(e != null)
			{
				e.copyDataFromOld(entity);
				world.spawnEntityInWorld(e);
				teleporter.didTeleport.add(e);
			}

			entity.isDead = true;
		}
	}

	public List<Entity> getToTeleport()
	{
		List<Entity> entities = worldObj.getEntitiesWithinAABB(Entity.class, teleportBounds);
		List<Entity> ret = new ArrayList<Entity>();

		for(Entity entity : entities)
		{
			if(!didTeleport.contains(entity))
			{
				ret.add(entity);
			}
		}

		return ret;
	}

	@Override
	public void invalidate()
	{
		super.invalidate();

		if(!worldObj.isRemote)
		{
			if(Mekanism.teleporters.containsKey(code))
			{
				if(Mekanism.teleporters.get(code).contains(Coord4D.get(this)))
				{
					Mekanism.teleporters.get(code).remove(Coord4D.get(this));
				}

				if(Mekanism.teleporters.get(code).isEmpty())
				{
					Mekanism.teleporters.remove(code);
				}
			}
		}
	}

	public int calculateEnergyCost(Entity entity, Coord4D coords)
	{
		int energyCost = 1000;

		if(entity.worldObj.provider.getDimensionId() != coords.dimensionId)
		{
			energyCost+=10000;
		}

		int distance = (int)entity.getDistance(coords.getX(), coords.getY(), coords.getZ());
		energyCost+=(distance*10);

		return energyCost;
	}

	public boolean hasFrame()
	{
		if(        isFrame(getPos().add(1, 0, 0)) && isFrame(getPos().add(-1, 0, 0))
				&& isFrame(getPos().add(1, 1, 0)) && isFrame(getPos().add(-1, 1, 0))
				&& isFrame(getPos().add(1, 2, 0)) && isFrame(getPos().add(-1, 2, 0))
				&& isFrame(getPos().add(1, 3, 0)) && isFrame(getPos().add(-1, 3, 0))
				&& isFrame(getPos().add(0, 3, 0))) {return true;}
		if(        isFrame(getPos().add(0, 0, 1)) && isFrame(getPos().add(0, 0, -1))
				&& isFrame(getPos().add(0, 1, 1)) && isFrame(getPos().add(0, 1, -1))
				&& isFrame(getPos().add(0, 2, 1)) && isFrame(getPos().add(0, 2, -1))
				&& isFrame(getPos().add(0, 3, 1)) && isFrame(getPos().add(0, 3, -1))
				&& isFrame(getPos().add(0, 3, 0))) {return true;}
		return false;
	}

	public boolean isFrame(BlockPos pos)
	{
		IBlockState state = worldObj.getBlockState(pos);
		return state.getBlock() == MekanismBlocks.BasicBlock && state.getValue(BasicBlock.BASIC_BLOCK_1.predicatedProperty) == BasicBlockType.TELEPORTER_FRAME;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbtTags)
	{
		super.readFromNBT(nbtTags);

		code.digitOne = nbtTags.getInteger("digitOne");
		code.digitTwo = nbtTags.getInteger("digitTwo");
		code.digitThree = nbtTags.getInteger("digitThree");
		code.digitFour = nbtTags.getInteger("digitFour");
	}

	@Override
	public void writeToNBT(NBTTagCompound nbtTags)
	{
		super.writeToNBT(nbtTags);

		nbtTags.setInteger("digitOne", code.digitOne);
		nbtTags.setInteger("digitTwo", code.digitTwo);
		nbtTags.setInteger("digitThree", code.digitThree);
		nbtTags.setInteger("digitFour", code.digitFour);
	}

	@Override
	public void handlePacketData(ByteBuf dataStream)
	{
		if(!worldObj.isRemote)
		{
			if(Mekanism.teleporters.containsKey(code))
			{
				if(Mekanism.teleporters.get(code).contains(Coord4D.get(this)))
				{
					Mekanism.teleporters.get(code).remove(Coord4D.get(this));
				}

				if(Mekanism.teleporters.get(code).isEmpty()) Mekanism.teleporters.remove(code);
			}

			int type = dataStream.readInt();

			if(type == 0)
			{
				code.digitOne = dataStream.readInt();
			}
			else if(type == 1)
			{
				code.digitTwo = dataStream.readInt();
			}
			else if(type == 2)
			{
				code.digitThree = dataStream.readInt();
			}
			else if(type == 3)
			{
				code.digitFour = dataStream.readInt();
			}
			
			return;
		}

		super.handlePacketData(dataStream);

		status = dataStream.readByte();
		code.digitOne = dataStream.readInt();
		code.digitTwo = dataStream.readInt();
		code.digitThree = dataStream.readInt();
		code.digitFour = dataStream.readInt();
		shouldRender = dataStream.readBoolean();
	}

	@Override
	public ArrayList getNetworkedData(ArrayList data)
	{
		super.getNetworkedData(data);

		data.add(status);
		data.add(code.digitOne);
		data.add(code.digitTwo);
		data.add(code.digitThree);
		data.add(code.digitFour);
		data.add(shouldRender);

		return data;
	}

	@Override
	public boolean canExtractItem(int slotID, ItemStack itemstack, EnumFacing side)
	{
		return ChargeUtils.canBeOutputted(itemstack, false);
	}

	@Override
	@Method(modid = "ComputerCraft")
	public String getType()
	{
		return getName();
	}

	@Override
	@Method(modid = "ComputerCraft")
	public String[] getMethodNames()
	{
		return new String[] {"getStored", "canTeleport", "getMaxEnergy", "getEnergyNeeded", "teleport", "set"};
	}

	@Override
	@Method(modid = "ComputerCraft")
	public Object[] callMethod(IComputerAccess computer, ILuaContext context, int method, Object[] arguments) throws LuaException, InterruptedException
	{
		switch(method)
		{
			case 0:
				return new Object[] {getEnergy()};
			case 1:
				return new Object[] {canTeleport()};
			case 2:
				return new Object[] {getMaxEnergy()};
			case 3:
				return new Object[] {(getMaxEnergy()-getEnergy())};
			case 4:
				teleport();
				return new Object[] {"Attempted to teleport."};
			case 5:
				if(!(arguments[0] instanceof Double) || !(arguments[1] instanceof Double))
				{
					return new Object[] {"Invalid parameters."};
				}

				int digit = ((Double)arguments[0]).intValue();
				int newDigit = ((Double)arguments[1]).intValue();

				switch(digit)
				{
					case 0:
						code.digitOne = newDigit;
						break;
					case 1:
						code.digitTwo = newDigit;
						break;
					case 2:
						code.digitThree = newDigit;
						break;
					case 3:
						code.digitFour = newDigit;
						break;
					default:
						return new Object[] {"No digit found."};
				}
			default:
				Mekanism.logger.error("Attempted to call unknown method with computer ID " + computer.getID());
				return new Object[] {"Unknown command."};
		}
	}

	@Override
	@Method(modid = "ComputerCraft")
	public boolean equals(IPeripheral other)
	{
		return this == other;
	}

	@Override
	@Method(modid = "ComputerCraft")
	public void attach(IComputerAccess computer) {}

	@Override
	@Method(modid = "ComputerCraft")
	public void detach(IComputerAccess computer) {}

	@Override
	@SideOnly(Side.CLIENT)
	public AxisAlignedBB getRenderBoundingBox()
	{
		return INFINITE_EXTENT_AABB;
	}
}
