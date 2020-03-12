package mekanism.common.tile;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Coord4D;
import mekanism.api.NBTConstants;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.providers.IBlockProvider;
import mekanism.common.Mekanism;
import mekanism.common.multiblock.IMultiblock;
import mekanism.common.multiblock.IStructuralMultiblock;
import mekanism.common.multiblock.MultiblockCache;
import mekanism.common.multiblock.MultiblockManager;
import mekanism.common.multiblock.SynchronizedData;
import mekanism.common.multiblock.UpdateProtocol;
import mekanism.common.network.PacketDataRequest;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.NBTUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants.NBT;

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
    private boolean prevStructure;

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
    public void validate() {
        super.validate();
        //TODO: Remove this, mainly used right now to request the structure gets sent
        if (isRemote()) {
            Mekanism.packetHandler.sendToServer(new PacketDataRequest(Coord4D.get(this)));
        }
    }

    @Override
    protected void onUpdateClient() {
        super.onUpdateClient();
        if (!clientHasStructure && !playersUsing.isEmpty()) {
            for (PlayerEntity player : new ObjectOpenHashSet<>(playersUsing)) {
                player.closeScreen();
            }
        }
    }

    @Override
    protected void onUpdateServer() {
        super.onUpdateServer();
        if (structure == null) {
            if (!playersUsing.isEmpty()) {
                for (PlayerEntity player : new ObjectOpenHashSet<>(playersUsing)) {
                    player.closeScreen();
                }
            }
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
                if (structure == null || (!structure.locations.contains(obj) && !structure.internalLocations.contains(obj))) {
                    BlockPos pos = obj.getPos();
                    TileEntity tile = MekanismUtils.getTileEntity(world, pos);
                    if (!world.isAirBlock(pos) && (tile == null || tile.getClass() != getClass()) && !(tile instanceof IStructuralMultiblock || tile instanceof IMultiblock)) {
                        MekanismUtils.notifyNeighborofChange(world, pos, getPos());
                    }
                }
            }
            sendUpdatePacket();
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

    @Override
    public void doUpdate() {
        if (!isRemote() && (structure == null || !structure.didTick)) {
            getProtocol().doUpdate();
            if (structure != null) {
                structure.didTick = true;
            }
        }
    }

    @Nonnull
    protected abstract T getNewStructure();

    public abstract MultiblockCache<T> getNewCache();

    protected abstract UpdateProtocol<T> getProtocol();

    public abstract MultiblockManager<T> getManager();

    @Nonnull
    @Override
    public CompoundNBT getUpdateTag() {
        CompoundNBT updateTag = super.getUpdateTag();
        updateTag.putBoolean(NBTConstants.RENDERING, isRendering);
        updateTag.putBoolean(NBTConstants.HAS_STRUCTURE, structure != null);
        if (structure != null && isRendering && sendStructure) {
            updateTag.putInt(NBTConstants.HEIGHT, structure.volHeight);
            updateTag.putInt(NBTConstants.WIDTH, structure.volWidth);
            updateTag.putInt(NBTConstants.LENGTH, structure.volLength);
            if (structure.renderLocation != null) {
                updateTag.put(NBTConstants.RENDER_LOCATION, structure.renderLocation.write(new CompoundNBT()));
            }
            if (structure.inventoryID != null) {
                updateTag.putString(NBTConstants.INVENTORY_ID, structure.inventoryID);
            }
        }
        if (sendStructure) {
            sendStructure = false;
        }
        return updateTag;
    }

    @Override
    public void handleUpdateTag(@Nonnull CompoundNBT tag) {
        super.handleUpdateTag(tag);
        if (structure == null) {
            structure = getNewStructure();
        }
        NBTUtils.setBooleanIfPresent(tag, NBTConstants.RENDERING, value -> isRendering = value);
        NBTUtils.setBooleanIfPresent(tag, NBTConstants.HAS_STRUCTURE, value -> clientHasStructure = value);
        if (isRendering) {
            if (clientHasStructure) {
                NBTUtils.setIntIfPresent(tag, NBTConstants.HEIGHT, value -> structure.volHeight = value);
                NBTUtils.setIntIfPresent(tag, NBTConstants.WIDTH, value -> structure.volWidth = value);
                NBTUtils.setIntIfPresent(tag, NBTConstants.LENGTH, value -> structure.volLength = value);
                NBTUtils.setCoord4DIfPresent(tag, NBTConstants.RENDER_LOCATION, value -> structure.renderLocation = value);
                if (tag.contains(NBTConstants.INVENTORY_ID, NBT.TAG_STRING)) {
                    structure.inventoryID = tag.getString(NBTConstants.INVENTORY_ID);
                } else {
                    structure.inventoryID = null;
                }
                //TODO: Test sparkle
                if (structure.renderLocation != null && !prevStructure) {
                    Mekanism.proxy.doMultiblockSparkle(this, structure.renderLocation.getPos(), structure.volLength, structure.volWidth, structure.volHeight,
                          tile -> MultiblockManager.areEqual(this, tile));
                }
            }
            prevStructure = clientHasStructure;
        }
    }

    @Override
    public void read(CompoundNBT nbtTags) {
        super.read(nbtTags);
        if (structure == null) {
            if (nbtTags.contains(NBTConstants.INVENTORY_ID, NBT.TAG_STRING)) {
                cachedID = nbtTags.getString(NBTConstants.INVENTORY_ID);
                cachedData.load(nbtTags);
            }
        }
    }

    @Nonnull
    @Override
    public CompoundNBT write(CompoundNBT nbtTags) {
        super.write(nbtTags);
        if (cachedID != null) {
            nbtTags.putString(NBTConstants.INVENTORY_ID, cachedID);
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
    public boolean persistInventory() {
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
        return structure.getInventorySlots(side);
    }
}