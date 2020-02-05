package mekanism.common.tile;

import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Coord4D;
import mekanism.api.TileNetworkList;
import mekanism.api.inventory.slot.IInventorySlot;
import mekanism.api.providers.IBlockProvider;
import mekanism.common.Mekanism;
import mekanism.common.PacketHandler;
import mekanism.common.multiblock.IMultiblock;
import mekanism.common.multiblock.IStructuralMultiblock;
import mekanism.common.multiblock.MultiblockCache;
import mekanism.common.multiblock.MultiblockManager;
import mekanism.common.multiblock.SynchronizedData;
import mekanism.common.multiblock.UpdateProtocol;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

public abstract class TileEntityMultiblock<T extends SynchronizedData<T>> extends TileEntityMekanism implements IMultiblock<T> {

    /**
     * The multiblock data for this structure.
     */
    @Nullable
    public T structure;

    /**
     * Whether or not to send this multiblock's structure in the next update packet.
     */
    public boolean sendStructure;

    /**
     * This multiblock's previous "has structure" state.
     */
    public boolean prevStructure;

    /**
     * Whether or not this multiblock has it's structure, for the client side mechanics.
     */
    public boolean clientHasStructure;

    /**
     * Whether or not this multiblock segment is rendering the structure.
     */
    public boolean isRendering;

    /**
     * This multiblock segment's cached data
     */
    public MultiblockCache<T> cachedData = getNewCache();

    /**
     * This multiblock segment's cached inventory ID
     */
    @Nullable
    public String cachedID = null;

    public TileEntityMultiblock(IBlockProvider blockProvider) {
        super(blockProvider);
    }

    @Override
    public void onUpdate() {
        if (isRemote()) {
            if (structure == null) {
                structure = getNewStructure();
            }
            if (structure != null && structure.renderLocation != null && clientHasStructure && isRendering && !prevStructure) {
                Mekanism.proxy.doMultiblockSparkle(this, structure.renderLocation.getPos(), structure.volLength, structure.volWidth, structure.volHeight,
                      tile -> MultiblockManager.areEqual(this, tile));
            }
            prevStructure = clientHasStructure;
        }

        if (!playersUsing.isEmpty() && ((isRemote() && !clientHasStructure) || (!isRemote() && structure == null))) {
            for (PlayerEntity player : playersUsing) {
                player.closeScreen();
            }
        }

        if (!isRemote()) {
            if (structure == null) {
                isRendering = false;
                if (cachedID != null) {
                    getManager().updateCache(this);
                }
                if (ticker == 5) {
                    doUpdate();
                }
            }

            if (prevStructure == (structure == null)) {
                if (structure != null && !structure.hasRenderer) {
                    structure.hasRenderer = true;
                    isRendering = true;
                    sendStructure = true;
                }

                Coord4D thisCoord = Coord4D.get(this);
                for (Direction side : EnumUtils.DIRECTIONS) {
                    Coord4D obj = thisCoord.offset(side);
                    if (structure != null && (structure.locations.contains(obj) || structure.internalLocations.contains(obj))) {
                        continue;
                    }
                    BlockPos pos = obj.getPos();
                    TileEntity tile = MekanismUtils.getTileEntity(world, pos);
                    if (!world.isAirBlock(pos) && (tile == null || tile.getClass() != getClass()) && !(tile instanceof IStructuralMultiblock || tile instanceof IMultiblock)) {
                        MekanismUtils.notifyNeighborofChange(world, pos, getPos());
                    }
                }

                Mekanism.packetHandler.sendUpdatePacket(this);
            }

            prevStructure = structure != null;

            if (structure != null) {
                structure.didTick = false;
                if (structure.inventoryID != null) {
                    cachedData.sync(structure);
                    cachedID = structure.inventoryID;
                    getManager().updateCache(this);
                }
            }
        }
    }

    @Override
    public void doUpdate() {
        if (!isRemote() && (structure == null || !structure.didTick)) {
            getProtocol().doUpdate();
            if (structure != null) {
                structure.didTick = true;
            }
        }
    }

    public void sendPacketToRenderer() {
        if (structure != null) {
            for (Coord4D obj : structure.locations) {
                TileEntityMultiblock<T> tile = (TileEntityMultiblock<T>) MekanismUtils.getTileEntity(TileEntityMultiblock.class, getWorld(), obj.getPos());
                if (tile != null && tile.isRendering) {
                    Mekanism.packetHandler.sendUpdatePacket(tile);
                }
            }
        }
    }

    @Nonnull
    protected abstract T getNewStructure();

    public abstract MultiblockCache<T> getNewCache();

    protected abstract UpdateProtocol<T> getProtocol();

    public abstract MultiblockManager<T> getManager();

    @Override
    public TileNetworkList getNetworkedData(TileNetworkList data) {
        super.getNetworkedData(data);
        data.add(isRendering);
        data.add(structure != null);

        if (structure != null && isRendering) {
            if (sendStructure) {
                sendStructure = false;

                data.add(true);

                data.add(structure.volHeight);
                data.add(structure.volWidth);
                data.add(structure.volLength);

                structure.renderLocation.write(data);
                data.add(structure.inventoryID != null);//boolean for if has inv id
                if (structure.inventoryID != null) {
                    data.add(structure.inventoryID);
                }
            } else {
                data.add(false);
            }
        }
        return data;
    }

    @Override
    public void handlePacketData(PacketBuffer dataStream) {
        super.handlePacketData(dataStream);
        if (isRemote()) {
            if (structure == null) {
                structure = getNewStructure();
            }

            isRendering = dataStream.readBoolean();
            clientHasStructure = dataStream.readBoolean();
            if (clientHasStructure && isRendering) {
                if (dataStream.readBoolean()) {
                    structure.volHeight = dataStream.readInt();
                    structure.volWidth = dataStream.readInt();
                    structure.volLength = dataStream.readInt();
                    structure.renderLocation = Coord4D.read(dataStream);
                    if (dataStream.readBoolean()) {
                        structure.inventoryID = PacketHandler.readString(dataStream);
                    } else {
                        structure.inventoryID = null;
                    }
                }
            }
        }
    }

    @Override
    public void read(CompoundNBT nbtTags) {
        super.read(nbtTags);
        if (structure == null) {
            if (nbtTags.contains("cachedID")) {
                cachedID = nbtTags.getString("cachedID");
                cachedData.load(nbtTags);
            }
        }
    }

    @Nonnull
    @Override
    public CompoundNBT write(CompoundNBT nbtTags) {
        super.write(nbtTags);
        if (cachedID != null) {
            nbtTags.putString("cachedID", cachedID);
            cachedData.save(nbtTags);
        }
        return nbtTags;
    }

    @Nonnull
    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return INFINITE_EXTENT_AABB;
    }

    @Override
    public boolean handleInventory() {
        return false;
    }

    @Override
    public T getSynchronizedData() {
        return structure;
    }

    @Nonnull
    @Override
    public List<IInventorySlot> getInventorySlots(@Nullable Direction side) {
        //TODO: Check we may have to still be disabling the item handler cap for some blocks like the boundaries
        if (!hasInventory() || structure == null) {
            //TODO: Previously we had a check like !isRemote() ? structure == null : !clientHasStructure
            // Do we still need this if we ever actually needed it?
            //If we don't have a structure then return that we have no slots accessible
            return Collections.emptyList();
        }
        //Otherwise we get the inventory slots for our structure.
        // NOTE: Currently we have nothing that "cares" about facing/can give different output to different sides
        // so we are just returning the list directly instead of dealing with the side
        return structure.getInventorySlots();
    }
}