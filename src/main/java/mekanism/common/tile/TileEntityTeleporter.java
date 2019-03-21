package mekanism.common.tile;

import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nonnull;
import mekanism.api.Chunk3D;
import mekanism.api.Coord4D;
import mekanism.api.Range4D;
import mekanism.common.Mekanism;
import mekanism.common.MekanismBlocks;
import mekanism.common.PacketHandler;
import mekanism.common.Upgrade;
import mekanism.common.base.IRedstoneControl;
import mekanism.common.base.IUpgradeTile;
import mekanism.api.TileNetworkList;
import mekanism.common.block.states.BlockStateMachine;
import mekanism.common.chunkloading.IChunkLoader;
import mekanism.common.config.MekanismConfig;
import mekanism.common.frequency.Frequency;
import mekanism.common.frequency.FrequencyManager;
import mekanism.common.frequency.IFrequencyHandler;
import mekanism.common.integration.computer.IComputerIntegration;
import mekanism.common.network.PacketEntityMove.EntityMoveMessage;
import mekanism.common.network.PacketPortalFX.PortalFXMessage;
import mekanism.common.network.PacketTileEntity.TileEntityMessage;
import mekanism.common.security.ISecurityTile;
import mekanism.common.tile.component.TileComponentChunkLoader;
import mekanism.common.tile.component.TileComponentSecurity;
import mekanism.common.tile.component.TileComponentUpgrade;
import mekanism.common.tile.prefab.TileEntityElectricBlock;
import mekanism.common.util.ChargeUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileEntityTeleporter extends TileEntityElectricBlock implements IComputerIntegration, IChunkLoader,
      IFrequencyHandler, IRedstoneControl, ISecurityTile, IUpgradeTile {

    private static final String[] methods = new String[]{"getEnergy", "canTeleport", "getMaxEnergy", "teleport",
          "setFrequency"};
    public AxisAlignedBB teleportBounds = null;

    public Set<UUID> didTeleport = new HashSet<>();

    public int teleDelay = 0;

    public boolean shouldRender;

    public boolean prevShouldRender;

    public Frequency frequency;

    public List<Frequency> publicCache = new ArrayList<>();
    public List<Frequency> privateCache = new ArrayList<>();

    /**
     * This teleporter's current status.
     */
    public byte status = 0;

    public RedstoneControl controlType = RedstoneControl.DISABLED;

    public TileComponentSecurity securityComponent;
    public TileComponentChunkLoader chunkLoaderComponent;
    public TileComponentUpgrade upgradeComponent;

    public TileEntityTeleporter() {
        super("Teleporter", BlockStateMachine.MachineType.TELEPORTER.baseEnergy);
        inventory = NonNullList.withSize(2, ItemStack.EMPTY);

        securityComponent = new TileComponentSecurity(this);
        chunkLoaderComponent = new TileComponentChunkLoader(this);
        upgradeComponent = new TileComponentUpgrade(this, 1);
        upgradeComponent.clearSupportedTypes();
        upgradeComponent.setSupported(Upgrade.ANCHOR);
    }

    public static void teleportPlayerTo(EntityPlayerMP player, Coord4D coord, TileEntityTeleporter teleporter) {
        if (player.dimension != coord.dimensionId) {
            player.changeDimension(coord.dimensionId,
                  (world, entity, yaw) -> entity.setPositionAndUpdate(coord.x + 0.5, coord.y + 1, coord.z + 0.5));
        } else {
            player.setPositionAndUpdate(coord.x + 0.5, coord.y + 1, coord.z + 0.5);
        }
        player.world.updateEntityWithOptionalForce(player, true);
    }

    public static void alignPlayer(EntityPlayerMP player, Coord4D coord) {
        Coord4D upperCoord = coord.offset(EnumFacing.UP);
        EnumFacing side = null;
        float yaw = player.rotationYaw;

        for (EnumFacing iterSide : MekanismUtils.SIDE_DIRS) {
            if (upperCoord.offset(iterSide).isAirBlock(player.world)) {
                side = iterSide;
                break;
            }
        }

        if (side != null) {
            switch (side) {
                case NORTH:
                    yaw = 180;
                    break;
                case SOUTH:
                    yaw = 0;
                    break;
                case WEST:
                    yaw = 90;
                    break;
                case EAST:
                    yaw = 270;
                    break;
                default:
                    break;
            }
        }

        player.connection.setPlayerLocation(player.posX, player.posY, player.posZ, yaw, player.rotationPitch);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();

        if (teleportBounds == null) {
            resetBounds();
        }

        if (!world.isRemote) {
            FrequencyManager manager = getManager(frequency);

            if (manager != null) {
                if (frequency != null && !frequency.valid) {
                    frequency = manager.validateFrequency(getSecurity().getOwnerUUID(), Coord4D.get(this), frequency);
                }

                if (frequency != null) {
                    frequency = manager.update(Coord4D.get(this), frequency);
                }
            } else {
                frequency = null;
            }

            status = canTeleport();

            if (MekanismUtils.canFunction(this) && status == 1 && teleDelay == 0) {
                teleport();
            }

            if (teleDelay == 0 && didTeleport.size() > 0) {
                cleanTeleportCache();
            }

            shouldRender = status == 1 || status > 4;

            if (shouldRender != prevShouldRender) {
                Mekanism.packetHandler.sendToAllAround(
                      new TileEntityMessage(Coord4D.get(this), getNetworkedData(new TileNetworkList())),
                      Coord4D.get(this).getTargetPoint(40D));
            }

            prevShouldRender = shouldRender;

            teleDelay = Math.max(0, teleDelay - 1);
        }

        ChargeUtils.discharge(0, this);
    }

    @Override
    public Frequency getFrequency(FrequencyManager manager) {
        if (manager == Mekanism.securityFrequencies) {
            return getSecurity().getFrequency();
        }

        return frequency;
    }

    public Coord4D getClosest() {
        if (frequency != null) {
            return frequency.getClosestCoords(Coord4D.get(this));
        }

        return null;
    }

    public void setFrequency(String name, boolean publicFreq) {
        FrequencyManager manager = getManager(new Frequency(name, null).setPublic(publicFreq));
        manager.deactivate(Coord4D.get(this));

        for (Frequency freq : manager.getFrequencies()) {
            if (freq.name.equals(name)) {
                frequency = freq;
                frequency.activeCoords.add(Coord4D.get(this));

                return;
            }
        }

        Frequency freq = new Frequency(name, getSecurity().getOwnerUUID()).setPublic(publicFreq);
        freq.activeCoords.add(Coord4D.get(this));
        manager.addFrequency(freq);
        frequency = freq;

        MekanismUtils.saveChunk(this);
    }

    public FrequencyManager getManager(Frequency freq) {
        if (getSecurity().getOwnerUUID() == null || freq == null) {
            return null;
        }

        if (freq.isPublic()) {
            return Mekanism.publicTeleporters;
        } else {
            if (!Mekanism.privateTeleporters.containsKey(getSecurity().getOwnerUUID())) {
                FrequencyManager manager = new FrequencyManager(Frequency.class, Frequency.TELEPORTER,
                      getSecurity().getOwnerUUID());
                Mekanism.privateTeleporters.put(getSecurity().getOwnerUUID(), manager);
                manager.createOrLoad(world);
            }

            return Mekanism.privateTeleporters.get(getSecurity().getOwnerUUID());
        }
    }

    @Override
    public void onChunkUnload() {
        super.onChunkUnload();

        if (!world.isRemote && frequency != null) {
            FrequencyManager manager = getManager(frequency);

            if (manager != null) {
                manager.deactivate(Coord4D.get(this));
            }
        }
    }

    @Override
    public void invalidate() {
        super.invalidate();

        if (!world.isRemote) {
            if (frequency != null) {
                FrequencyManager manager = getManager(frequency);

                if (manager != null) {
                    manager.deactivate(Coord4D.get(this));
                }
            }
        }
    }

    public void cleanTeleportCache() {
        List<UUID> list = new ArrayList<>();

        for (Entity e : world.getEntitiesWithinAABB(Entity.class, teleportBounds)) {
            list.add(e.getPersistentID());
        }

        Set<UUID> teleportCopy = new HashSet<>(didTeleport);

        for (UUID id : teleportCopy) {
            if (!list.contains(id)) {
                didTeleport.remove(id);
            }
        }
    }

    @Nonnull
    @Override
    public int[] getSlotsForFace(@Nonnull EnumFacing side) {
        return new int[]{0};
    }

    @Override
    public boolean isItemValidForSlot(int slotID, @Nonnull ItemStack itemstack) {
        if (slotID == 0) {
            return ChargeUtils.canBeDischarged(itemstack);
        }

        return true;
    }

    public void resetBounds() {
        teleportBounds = new AxisAlignedBB(getPos(), getPos().add(1, 3, 1));
    }

    /**
     * @return 1: yes, 2: no frame, 3: no link found, 4: not enough electricity
     */
    public byte canTeleport() {
        if (!hasFrame()) {
            return 2;
        }

        if (getClosest() == null) {
            return 3;
        }

        List<Entity> entitiesInPortal = getToTeleport();
        Coord4D closestCoords = getClosest();

        int electricityNeeded = 0;

        for (Entity entity : entitiesInPortal) {
            electricityNeeded += calculateEnergyCost(entity, closestCoords);
        }

        if (getEnergy() < electricityNeeded) {
            return 4;
        }

        return 1;
    }

    public void teleport() {
        if (world.isRemote) {
            return;
        }

        List<Entity> entitiesInPortal = getToTeleport();

        Coord4D closestCoords = getClosest();

        if (closestCoords == null) {
            return;
        }

        for (Entity entity : entitiesInPortal) {
            World teleWorld = FMLCommonHandler.instance().getMinecraftServerInstance()
                  .getWorld(closestCoords.dimensionId);
            TileEntityTeleporter teleporter = (TileEntityTeleporter) closestCoords.getTileEntity(teleWorld);

            if (teleporter != null) {
                teleporter.didTeleport.add(entity.getPersistentID());
                teleporter.teleDelay = 5;

                if (entity instanceof EntityPlayerMP) {
                    teleportPlayerTo((EntityPlayerMP) entity, closestCoords, teleporter);
                    alignPlayer((EntityPlayerMP) entity, closestCoords);
                } else {
                    teleportEntityTo(entity, closestCoords, teleporter);
                }

                for (Coord4D coords : frequency.activeCoords) {
                    Mekanism.packetHandler.sendToAllAround(new PortalFXMessage(coords), coords.getTargetPoint(40D));
                }

                setEnergy(getEnergy() - calculateEnergyCost(entity, closestCoords));

                world.playSound(entity.posX, entity.posY, entity.posZ, SoundEvents.ENTITY_ENDERMEN_TELEPORT,
                      entity.getSoundCategory(), 1.0F, 1.0F, false);
            }
        }
    }

    public void teleportEntityTo(Entity entity, Coord4D coord, TileEntityTeleporter teleporter) {
        if (entity.world.provider.getDimension() != coord.dimensionId) {
            entity.changeDimension(coord.dimensionId,
                  (world, entity2, yaw) -> entity2.setPositionAndUpdate(coord.x + 0.5, coord.y + 1, coord.z + 0.5));
        } else {
            entity.setPositionAndUpdate(coord.x + 0.5, coord.y + 1, coord.z + 0.5);
            Mekanism.packetHandler.sendToReceivers(new EntityMoveMessage(entity), new Range4D(new Coord4D(entity)));
        }
    }

    public List<Entity> getToTeleport() {
        List<Entity> entities = world.getEntitiesWithinAABB(Entity.class, teleportBounds);
        List<Entity> ret = new ArrayList<>();

        for (Entity entity : entities) {
            if (!didTeleport.contains(entity.getPersistentID())) {
                ret.add(entity);
            }
        }

        return ret;
    }

    public int calculateEnergyCost(Entity entity, Coord4D coords) {
        int energyCost = MekanismConfig.usage.teleporterBaseUsage;

        if (entity.world.provider.getDimension() != coords.dimensionId) {
            energyCost += MekanismConfig.usage.teleporterDimensionPenalty;
        }

        int distance = (int) entity.getDistance(coords.x, coords.y, coords.z);
        energyCost += (distance * MekanismConfig.usage.teleporterDistanceUsage);

        return energyCost;
    }

    public boolean hasFrame() {
        if (isFrame(getPos().getX() - 1, getPos().getY(), getPos().getZ()) && isFrame(getPos().getX() + 1,
              getPos().getY(), getPos().getZ())
              && isFrame(getPos().getX() - 1, getPos().getY() + 1, getPos().getZ()) && isFrame(getPos().getX() + 1,
              getPos().getY() + 1, getPos().getZ())
              && isFrame(getPos().getX() - 1, getPos().getY() + 2, getPos().getZ()) && isFrame(getPos().getX() + 1,
              getPos().getY() + 2, getPos().getZ())
              && isFrame(getPos().getX() - 1, getPos().getY() + 3, getPos().getZ()) && isFrame(getPos().getX() + 1,
              getPos().getY() + 3, getPos().getZ())
              && isFrame(getPos().getX(), getPos().getY() + 3, getPos().getZ())) {
            return true;
        }
        return isFrame(getPos().getX(), getPos().getY(), getPos().getZ() - 1) && isFrame(getPos().getX(),
              getPos().getY(),
              getPos().getZ() + 1)
              && isFrame(getPos().getX(), getPos().getY() + 1, getPos().getZ() - 1) && isFrame(getPos().getX(),
              getPos().getY() + 1, getPos().getZ() + 1)
              && isFrame(getPos().getX(), getPos().getY() + 2, getPos().getZ() - 1) && isFrame(getPos().getX(),
              getPos().getY() + 2, getPos().getZ() + 1)
              && isFrame(getPos().getX(), getPos().getY() + 3, getPos().getZ() - 1) && isFrame(getPos().getX(),
              getPos().getY() + 3, getPos().getZ() + 1)
              && isFrame(getPos().getX(), getPos().getY() + 3, getPos().getZ());
    }

    public boolean isFrame(int x, int y, int z) {
        IBlockState state = world.getBlockState(new BlockPos(x, y, z));
        return state.getBlock() == MekanismBlocks.BasicBlock && state.getBlock().getMetaFromState(state) == 7;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbtTags) {
        super.readFromNBT(nbtTags);

        controlType = RedstoneControl.values()[nbtTags.getInteger("controlType")];

        if (nbtTags.hasKey("frequency")) {
            frequency = new Frequency(nbtTags.getCompoundTag("frequency"));
            frequency.valid = false;
        }
    }

    @Nonnull
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbtTags) {
        super.writeToNBT(nbtTags);

        nbtTags.setInteger("controlType", controlType.ordinal());

        if (frequency != null) {
            NBTTagCompound frequencyTag = new NBTTagCompound();
            frequency.write(frequencyTag);
            nbtTags.setTag("frequency", frequencyTag);
        }

        return nbtTags;
    }

    @Override
    public void handlePacketData(ByteBuf dataStream) {
        if (FMLCommonHandler.instance().getEffectiveSide().isServer()) {
            int type = dataStream.readInt();

            if (type == 0) {
                String name = PacketHandler.readString(dataStream);
                boolean isPublic = dataStream.readBoolean();

                setFrequency(name, isPublic);
            } else if (type == 1) {
                String freq = PacketHandler.readString(dataStream);
                boolean isPublic = dataStream.readBoolean();

                FrequencyManager manager = getManager(new Frequency(freq, null).setPublic(isPublic));

                if (manager != null) {
                    manager.remove(freq, getSecurity().getOwnerUUID());
                }
            }

            return;
        }

        super.handlePacketData(dataStream);

        if (FMLCommonHandler.instance().getEffectiveSide().isClient()) {
            if (dataStream.readBoolean()) {
                frequency = new Frequency(dataStream);
            } else {
                frequency = null;
            }

            status = dataStream.readByte();
            shouldRender = dataStream.readBoolean();
            controlType = RedstoneControl.values()[dataStream.readInt()];

            publicCache.clear();
            privateCache.clear();

            int amount = dataStream.readInt();

            for (int i = 0; i < amount; i++) {
                publicCache.add(new Frequency(dataStream));
            }

            amount = dataStream.readInt();

            for (int i = 0; i < amount; i++) {
                privateCache.add(new Frequency(dataStream));
            }
        }
    }

    @Override
    public TileNetworkList getNetworkedData(TileNetworkList data) {
        super.getNetworkedData(data);

        if (frequency != null) {
            data.add(true);
            frequency.write(data);
        } else {
            data.add(false);
        }

        data.add(status);
        data.add(shouldRender);
        data.add(controlType.ordinal());

        data.add(Mekanism.publicTeleporters.getFrequencies().size());

        for (Frequency freq : Mekanism.publicTeleporters.getFrequencies()) {
            freq.write(data);
        }

        FrequencyManager manager = getManager(new Frequency(null, null).setPublic(false));

        if (manager != null) {
            data.add(manager.getFrequencies().size());

            for (Frequency freq : manager.getFrequencies()) {
                freq.write(data);
            }
        } else {
            data.add(0);
        }

        return data;
    }

    @Override
    public boolean canExtractItem(int slotID, @Nonnull ItemStack itemstack, @Nonnull EnumFacing side) {
        return ChargeUtils.canBeOutputted(itemstack, false);
    }

    @Override
    public String[] getMethods() {
        return methods;
    }

    @Override
    public Object[] invoke(int method, Object[] arguments) throws Exception {
        switch (method) {
            case 0:
                return new Object[]{getEnergy()};
            case 1:
                return new Object[]{canTeleport()};
            case 2:
                return new Object[]{getMaxEnergy()};
            case 3:
                teleport();
                return new Object[]{"Attempted to teleport."};
            case 4:
                if (!(arguments[0] instanceof String) || !(arguments[1] instanceof Boolean)) {
                    return new Object[]{"Invalid parameters."};
                }

                String freq = ((String) arguments[0]).trim();
                boolean isPublic = (Boolean) arguments[1];

                setFrequency(freq, isPublic);

                return new Object[]{"Frequency set."};
            default:
                throw new NoSuchMethodException();
        }
    }

    @Nonnull
    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox() {
        return INFINITE_EXTENT_AABB;
    }

    @Override
    public RedstoneControl getControlType() {
        return controlType;
    }

    @Override
    public void setControlType(RedstoneControl type) {
        controlType = type;
    }

    @Override
    public boolean canPulse() {
        return false;
    }

    @Override
    public TileComponentSecurity getSecurity() {
        return securityComponent;
    }

    @Override
    public TileComponentChunkLoader getChunkLoader() {
        return chunkLoaderComponent;
    }

    @Override
    public Set<ChunkPos> getChunkSet() {
        Set<ChunkPos> ret = new HashSet<>();

        ret.add(new Chunk3D(Coord4D.get(this)).getPos());

        return ret;
    }

    @Override
    public TileComponentUpgrade getComponent() {
        return upgradeComponent;
    }
}
