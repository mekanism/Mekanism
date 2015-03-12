package mekanism.common.tile;

import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import mekanism.api.Coord4D;
import mekanism.api.EnumColor;
import mekanism.common.Mekanism;
import mekanism.common.MekanismBlocks;
import mekanism.common.PacketHandler;
import mekanism.common.block.BlockMachine.MachineType;
import mekanism.common.frequency.Frequency;
import mekanism.common.frequency.FrequencyManager;
import mekanism.common.network.PacketTileEntity.TileEntityMessage;
import mekanism.common.util.ChargeUtils;
import mekanism.common.util.MekanismUtils;
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
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Optional.Interface;
import cpw.mods.fml.common.Optional.Method;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;

@Interface(iface = "dan200.computercraft.api.peripheral.IPeripheral", modid = "ComputerCraft")
public class TileEntityTeleporter extends TileEntityElectricBlock implements IPeripheral
{
	private MinecraftServer server = MinecraftServer.getServer();

	public AxisAlignedBB teleportBounds = null;

	public Set<Entity> didTeleport = new HashSet<Entity>();

	public int teleDelay = 0;

	public boolean shouldRender;

	public boolean prevShouldRender;
	
	public String owner;
	
	public Frequency frequency;
	
	public List<Frequency> publicCache = new ArrayList<Frequency>();
	public List<Frequency> privateCache = new ArrayList<Frequency>();

	/** This teleporter's current status. */
	public byte status = 0;

	public TileEntityTeleporter()
	{
		super("Teleporter", MachineType.TELEPORTER.baseEnergy);
		inventory = new ItemStack[1];
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
			FrequencyManager manager = getManager(frequency);
			
			if(manager != null)
			{
				if(frequency != null && !frequency.valid)
				{
					frequency = manager.validateFrequency(owner, Coord4D.get(this), frequency);
				}
				
				frequency = manager.update(owner, Coord4D.get(this), frequency);
			}
			else {
				frequency = null;
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
	
	public void setFrequency(String name, boolean publicFreq)
	{
		if(name.equals(frequency))
		{
			return;
		}
		
		FrequencyManager manager = getManager(new Frequency(name, null).setPublic(publicFreq));
		
		for(Frequency freq : manager.getFrequencies())
		{
			if(freq.name.equals(name))
			{
				frequency = freq;
				frequency.activeCoords.add(Coord4D.get(this));
				return;
			}
		}
		
		Frequency freq = new Frequency(name, owner).setPublic(publicFreq);
		freq.activeCoords.add(Coord4D.get(this));
		manager.addFrequency(freq);
		frequency = freq;
		
		MekanismUtils.saveChunk(this);
	}
	
	public FrequencyManager getManager(Frequency freq)
	{
		if(owner == null || freq == null)
		{
			return null;
		}
		
		if(freq.isPublic())
		{
			return Mekanism.publicTeleporters;
		}
		else {
			if(!Mekanism.privateTeleporters.containsKey(owner))
			{
				FrequencyManager manager = new FrequencyManager(Frequency.class, owner);
				Mekanism.privateTeleporters.put(owner, manager);
				manager.createOrLoad(worldObj);
			}
			
			return Mekanism.privateTeleporters.get(owner);
		}
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
				return EnumColor.DARK_RED + MekanismUtils.localize("gui.teleporter.needsEnergy");
		}
		
		return EnumColor.DARK_RED + MekanismUtils.localize("gui.teleporter.noLink");
	}
	
	@Override
	public void onChunkUnload()
	{
		super.onChunkUnload();
		
		if(!worldObj.isRemote && frequency != null)
		{
			FrequencyManager manager = getManager(frequency);
			
			if(manager != null)
			{
				manager.deactivate(Coord4D.get(this));
			}
		}
	}
	
	@Override
	public void invalidate()
	{
		super.invalidate();
		
		if(!worldObj.isRemote && frequency != null)
		{
			FrequencyManager manager = getManager(frequency);
			
			if(manager != null)
			{
				manager.deactivate(Coord4D.get(this));
			}
		}
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
	public int[] getAccessibleSlotsFromSide(int side)
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
		teleportBounds = AxisAlignedBB.getBoundingBox(xCoord, yCoord, zCoord, xCoord+1, yCoord+3, zCoord+1);
	}

	/**
	 * 1: yes
	 * 2: no frame
	 * 3: no link found
	 * 4: not enough electricity
	 * @return
	 */
	public byte canTeleport()
	{
		if(!hasFrame())
		{
			return 2;
		}
		
		//TODO if no link return 3

		if(true/*TODO if has link*/)
		{
			List<Entity> entitiesInPortal = getToTeleport();

			Coord4D closestCoords = null;

			/*for(Coord4D coords : Mekanism.teleporters.get(code))
			{
				if(!coords.equals(Coord4D.get(this)))
				{
					closestCoords = coords;
					break;
				}
			}*/

			int electricityNeeded = 0;

			for(Entity entity : entitiesInPortal)
			{
				electricityNeeded += calculateEnergyCost(entity, closestCoords);
			}

			if(getEnergy() < electricityNeeded)
			{
				return 4;
			}

			return 1;
		}

		return 1;
	}

	public void teleport()
	{
		if(worldObj.isRemote) return;

		List<Entity> entitiesInPortal = getToTeleport();

		Coord4D closestCoords = null;

		/*for(Coord4D coords : Mekanism.teleporters.get(code))
		{
			if(!coords.equals(Coord4D.get(this)))
			{
				closestCoords = coords;
				break;
			}
		}*/

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

				/*for(Coord4D coords : Mekanism.teleporters.get(code))
				{
					Mekanism.packetHandler.sendToAllAround(new PortalFXMessage(coords), coords.getTargetPoint(40D));
				}*/

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
			player.playerNetServerHandler.sendPacket(new S07PacketRespawn(player.dimension, player.worldObj.difficultySetting, newWorld.getWorldInfo().getTerrainType(), player.theItemInWorldManager.getGameType()));
			oldWorld.removePlayerEntityDangerously(player);
			player.isDead = false;

			if(player.isEntityAlive())
			{
				newWorld.spawnEntityInWorld(player);
				player.setLocationAndAngles(coord.xCoord+0.5, coord.yCoord+1, coord.zCoord+0.5, player.rotationYaw, player.rotationPitch);
				newWorld.updateEntityWithOptionalForce(player, false);
				player.setWorld(newWorld);
			}

			server.getConfigurationManager().func_72375_a(player, oldWorld);
			player.playerNetServerHandler.setPlayerLocation(coord.xCoord+0.5, coord.yCoord+1, coord.zCoord+0.5, player.rotationYaw, player.rotationPitch);
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
			player.playerNetServerHandler.setPlayerLocation(coord.xCoord+0.5, coord.yCoord+1, coord.zCoord+0.5, player.rotationYaw, player.rotationPitch);
		}
	}

	public void teleportEntityTo(Entity entity, Coord4D coord, TileEntityTeleporter teleporter)
	{
		WorldServer world = server.worldServerForDimension(coord.dimensionId);

		if(entity.worldObj.provider.dimensionId != coord.dimensionId)
		{
			entity.worldObj.removeEntity(entity);
			entity.isDead = false;

			world.spawnEntityInWorld(entity);
			entity.setLocationAndAngles(coord.xCoord+0.5, coord.yCoord+1, coord.zCoord+0.5, entity.rotationYaw, entity.rotationPitch);
			world.updateEntityWithOptionalForce(entity, false);
			entity.setWorld(world);
			world.resetUpdateEntityTick();

			Entity e = EntityList.createEntityByName(EntityList.getEntityString(entity), world);

			if(e != null)
			{
				e.copyDataFrom(entity, true);
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

	public int calculateEnergyCost(Entity entity, Coord4D coords)
	{
		int energyCost = 1000;

		if(entity.worldObj.provider.dimensionId != coords.dimensionId)
		{
			energyCost+=10000;
		}

		int distance = (int)entity.getDistance(coords.xCoord, coords.yCoord, coords.zCoord);
		energyCost+=(distance*10);

		return energyCost;
	}

	public boolean hasFrame()
	{
		if(isFrame(xCoord-1, yCoord, zCoord) && isFrame(xCoord+1, yCoord, zCoord)
				&& isFrame(xCoord-1, yCoord+1, zCoord) && isFrame(xCoord+1, yCoord+1, zCoord)
				&& isFrame(xCoord-1, yCoord+2, zCoord) && isFrame(xCoord+1, yCoord+2, zCoord)
				&& isFrame(xCoord-1, yCoord+3, zCoord) && isFrame(xCoord+1, yCoord+3, zCoord)
				&& isFrame(xCoord, yCoord+3, zCoord)) {return true;}
		if(isFrame(xCoord, yCoord, zCoord-1) && isFrame(xCoord, yCoord, zCoord+1)
				&& isFrame(xCoord, yCoord+1, zCoord-1) && isFrame(xCoord, yCoord+1, zCoord+1)
				&& isFrame(xCoord, yCoord+2, zCoord-1) && isFrame(xCoord, yCoord+2, zCoord+1)
				&& isFrame(xCoord, yCoord+3, zCoord-1) && isFrame(xCoord, yCoord+3, zCoord+1)
				&& isFrame(xCoord, yCoord+3, zCoord)) {return true;}
		return false;
	}

	public boolean isFrame(int x, int y, int z)
	{
		return worldObj.getBlock(x, y, z) == MekanismBlocks.BasicBlock && worldObj.getBlockMetadata(x, y, z) == 7;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbtTags)
	{
		super.readFromNBT(nbtTags);
		
		if(nbtTags.hasKey("owner"))
		{
			owner = nbtTags.getString("owner");
		}
		
		if(nbtTags.hasKey("frequency"))
		{
			frequency = new Frequency(nbtTags.getCompoundTag("frequency"));
			frequency.valid = false;
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound nbtTags)
	{
		super.writeToNBT(nbtTags);
		
		if(owner != null)
		{
			nbtTags.setString("owner", owner);
		}
		
		if(frequency != null)
		{
			NBTTagCompound frequencyTag = new NBTTagCompound();
			frequency.write(frequencyTag);
			nbtTags.setTag("frequency", frequencyTag);
		}
	}

	@Override
	public void handlePacketData(ByteBuf dataStream)
	{
		if(!worldObj.isRemote)
		{
			int type = dataStream.readInt();
			
			if(type == 0)
			{
				String name = PacketHandler.readString(dataStream);
				boolean isPublic = dataStream.readBoolean();
				
				setFrequency(name, isPublic);
			}
			else if(type == 1)
			{
				String freq = PacketHandler.readString(dataStream);
				boolean isPublic = dataStream.readBoolean();
				
				FrequencyManager manager = getManager(new Frequency(freq, null).setPublic(isPublic));
				
				if(manager != null)
				{
					for(Iterator<Frequency> iter = manager.getFrequencies().iterator(); iter.hasNext();)
					{
						Frequency iterFreq = iter.next();
						
						if(iterFreq.name.equals(freq) && iterFreq.owner.equals(owner))
						{
							iter.remove();
						}
					}
				}
			}
			
			return;
		}

		super.handlePacketData(dataStream);
		
		if(dataStream.readBoolean())
		{
			owner = PacketHandler.readString(dataStream);
		}
		else {
			owner = null;
		}
		
		if(dataStream.readBoolean())
		{
			frequency = new Frequency(dataStream);
		}
		else {
			frequency = null;
		}

		status = dataStream.readByte();
		shouldRender = dataStream.readBoolean();
		
		publicCache.clear();
		privateCache.clear();
		
		int amount = dataStream.readInt();
		
		for(int i = 0; i < amount; i++)
		{
			publicCache.add(new Frequency(dataStream));
		}
		
		amount = dataStream.readInt();
		
		for(int i = 0; i < amount; i++)
		{
			privateCache.add(new Frequency(dataStream));
		}
	}

	@Override
	public ArrayList getNetworkedData(ArrayList data)
	{
		super.getNetworkedData(data);
		
		if(owner != null)
		{
			data.add(true);
			data.add(owner);
		}
		else {
			data.add(false);
		}
		
		if(frequency != null)
		{
			data.add(true);
			frequency.write(data);
		}
		else {
			data.add(false);
		}

		data.add(status);
		data.add(shouldRender);
		
		data.add(Mekanism.publicTeleporters.getFrequencies().size());
		
		for(Frequency freq : Mekanism.publicTeleporters.getFrequencies())
		{
			freq.write(data);
		}
		
		FrequencyManager manager = getManager(new Frequency(null, null).setPublic(false));
		
		data.add(manager.getFrequencies().size());
		
		for(Frequency freq : manager.getFrequencies())
		{
			freq.write(data);
		}

		return data;
	}

	@Override
	public boolean canExtractItem(int slotID, ItemStack itemstack, int side)
	{
		return ChargeUtils.canBeOutputted(itemstack, false);
	}

	@Override
	@Method(modid = "ComputerCraft")
	public String getType()
	{
		return getInventoryName();
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
				if(!(arguments[0] instanceof String) || !(arguments[1] instanceof Boolean))
				{
					return new Object[] {"Invalid parameters."};
				}
				
				String freq = ((String)arguments[0]).trim();
				boolean isPublic = (Boolean)arguments[1];
				
				setFrequency(freq, isPublic);
				
				return new Object[] {"Frequency set."};
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
