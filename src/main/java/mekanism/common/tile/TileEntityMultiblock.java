package mekanism.common.tile;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
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
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.NBTUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

public abstract class TileEntityMultiblock<T extends SynchronizedData<T>> extends TileEntityMekanism implements IMultiblock<T> {

    /**
     * The multiblock data for this structure.
     */
    @Nullable
    public T structure;

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
    public MultiblockCache<T> cachedData = getManager().getNewCache();

    /**
     * This multiblock segment's cached inventory ID
     */
    @Nullable
    public UUID cachedID = null;

    public TileEntityMultiblock(IBlockProvider blockProvider) {
        super(blockProvider);
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
            if (cachedID != null) {
                getManager().updateCache(this, false);
            }
            if (ticker == 5) {
                doUpdate();
            }
            if (prevStructure) {
                structureChanged();
                prevStructure = false;
            }
            isRendering = false;
        } else {
            if (!prevStructure) {
                structureChanged();
                prevStructure = true;
            }
            structure.didTick = false;
            if (isRendering && structure.inventoryID != null) {
                cachedData.sync(structure);
                cachedID = structure.inventoryID;
                getManager().updateCache(this, false);
            }
        }
    }

    private void structureChanged() {
        if (structure != null && !structure.hasRenderer) {
            structure.hasRenderer = true;
            isRendering = true;
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

    @Override
    public void doUpdate() {
        if (!isRemote() && (structure == null || !structure.didTick)) {
            if (structure != null && structure.inventoryID != null) {
                // update the cache before we destroy the multiblock
                cachedData.sync(structure);
                cachedID = structure.inventoryID;
                getManager().updateCache(this, true);
            }
            getProtocol().doUpdate();
            if (structure != null) {
                structure.didTick = true;
            }
        }
    }

    @Override
    public ActionResultType onActivate(PlayerEntity player, Hand hand, ItemStack stack) {
        if (player.isShiftKeyDown() || structure == null) {
            return ActionResultType.PASS;
        }
        return openGui(player);
    }

    @Nonnull
    public abstract T getNewStructure();

    protected abstract UpdateProtocol<T> getProtocol();

    public abstract MultiblockManager<T> getManager();

    @Nonnull
    @Override
    public CompoundNBT getReducedUpdateTag() {
        CompoundNBT updateTag = super.getReducedUpdateTag();
        updateTag.putBoolean(NBTConstants.RENDERING, isRendering);
        updateTag.putBoolean(NBTConstants.HAS_STRUCTURE, structure != null);
        if (structure != null && isRendering) {
            updateTag.putInt(NBTConstants.HEIGHT, structure.volHeight);
            updateTag.putInt(NBTConstants.WIDTH, structure.volWidth);
            updateTag.putInt(NBTConstants.LENGTH, structure.volLength);
            if (structure.renderLocation != null) {
                updateTag.put(NBTConstants.RENDER_LOCATION, structure.renderLocation.write(new CompoundNBT()));
            }
            if (structure.inventoryID != null) {
                updateTag.putUniqueId(NBTConstants.INVENTORY_ID, structure.inventoryID);
            }
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
                if (tag.hasUniqueId(NBTConstants.INVENTORY_ID)) {
                    structure.inventoryID = tag.getUniqueId(NBTConstants.INVENTORY_ID);
                } else {
                    structure.inventoryID = null;
                }
                if (structure.renderLocation != null && !prevStructure) {
                    Mekanism.proxy.doMultiblockSparkle(this, structure.renderLocation.getPos(), structure.volLength, structure.volWidth, structure.volHeight,
                          tile -> MultiblockManager.areEqual(this, tile));
                }
            } else {
                // this will consecutively be set on the server
                isRendering = false;
            }
            prevStructure = clientHasStructure;
        }
    }

    @Override
    public void read(CompoundNBT nbtTags) {
        super.read(nbtTags);
        if (structure == null) {
            if (nbtTags.hasUniqueId(NBTConstants.INVENTORY_ID)) {
                cachedID = nbtTags.getUniqueId(NBTConstants.INVENTORY_ID);
                cachedData.load(nbtTags);
            }
        }
    }

    @Nonnull
    @Override
    public CompoundNBT write(CompoundNBT nbtTags) {
        super.write(nbtTags);
        if (cachedID != null) {
            nbtTags.putUniqueId(NBTConstants.INVENTORY_ID, cachedID);
            cachedData.save(nbtTags);
        }
        return nbtTags;
    }

    @Nonnull
    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        if (clientHasStructure && structure != null && isRendering && structure.renderLocation != null) {
            //TODO: Eventually we may want to look into caching this
            BlockPos corner1 = structure.renderLocation.getPos();
            //height - 2 up, but then we go up one further to take into account that block
            BlockPos corner2 = corner1.east(structure.volLength + 1).south(structure.volWidth + 1).up(structure.volHeight - 1);
            //Note: We do basically the full dimensions as it still is a lot smaller than always rendering it, and makes sure no matter
            // how the specific multiblock wants to render, that it is being viewed
            return new AxisAlignedBB(corner1, corner2);
        }
        return super.getRenderBoundingBox();
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
        if (!hasInventory() || structure == null) {
            //If we don't have a structure then return that we have no slots accessible
            return Collections.emptyList();
        }
        //Otherwise we get the inventory slots for our structure.
        // NOTE: Currently we have nothing that "cares" about facing/can give different output to different sides
        // so we are just returning the list directly instead of dealing with the side
        return structure.getInventorySlots(side);
    }
}