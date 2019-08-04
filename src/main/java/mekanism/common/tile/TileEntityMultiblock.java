package mekanism.common.tile;

import io.netty.buffer.ByteBuf;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Coord4D;
import mekanism.api.TileNetworkList;
import mekanism.common.Mekanism;
import mekanism.common.PacketHandler;
import mekanism.common.base.IBlockProvider;
import mekanism.common.multiblock.IMultiblock;
import mekanism.common.multiblock.IStructuralMultiblock;
import mekanism.common.multiblock.MultiblockCache;
import mekanism.common.multiblock.MultiblockManager;
import mekanism.common.multiblock.SynchronizedData;
import mekanism.common.multiblock.UpdateProtocol;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.MekanismUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

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
        if (world.isRemote) {
            if (structure == null) {
                structure = getNewStructure();
            }
            if (structure != null && structure.renderLocation != null && clientHasStructure && isRendering && !prevStructure) {
                Mekanism.proxy.doMultiblockSparkle(this, structure.renderLocation.getPos(), structure.volLength, structure.volWidth, structure.volHeight,
                      tile -> MultiblockManager.areEqual(this, tile));
            }
            prevStructure = clientHasStructure;
        }

        if (playersUsing.size() > 0 && ((world.isRemote && !clientHasStructure) || (!world.isRemote && structure == null))) {
            for (EntityPlayer player : playersUsing) {
                player.closeScreen();
            }
        }

        if (!world.isRemote) {
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
                for (EnumFacing side : EnumFacing.values()) {
                    Coord4D obj = thisCoord.offset(side);
                    if (structure != null && (structure.locations.contains(obj) || structure.internalLocations.contains(obj))) {
                        continue;
                    }
                    TileEntity tile = obj.getTileEntity(world);
                    if (!obj.isAirBlock(world) && (tile == null || tile.getClass() != getClass()) && !(tile instanceof IStructuralMultiblock || tile instanceof IMultiblock)) {
                        MekanismUtils.notifyNeighborofChange(world, obj, getPos());
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
        if (!world.isRemote && (structure == null || !structure.didTick)) {
            getProtocol().doUpdate();
            if (structure != null) {
                structure.didTick = true;
            }
        }
    }

    public void sendPacketToRenderer() {
        if (structure != null) {
            for (Coord4D obj : structure.locations) {
                TileEntityMultiblock<T> tileEntity = (TileEntityMultiblock<T>) obj.getTileEntity(world);
                if (tileEntity != null && tileEntity.isRendering) {
                    Mekanism.packetHandler.sendUpdatePacket(tileEntity);
                }
            }
        }
    }

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
    public void handlePacketData(ByteBuf dataStream) {
        super.handlePacketData(dataStream);
        if (FMLCommonHandler.instance().getEffectiveSide().isClient()) {
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
    public void readFromNBT(NBTTagCompound nbtTags) {
        super.readFromNBT(nbtTags);
        if (structure == null) {
            if (nbtTags.hasKey("cachedID")) {
                cachedID = nbtTags.getString("cachedID");
                cachedData.load(nbtTags);
            }
        }
    }

    @Nonnull
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbtTags) {
        super.writeToNBT(nbtTags);
        if (cachedID != null) {
            nbtTags.setString("cachedID", cachedID);
            cachedData.save(nbtTags);
        }
        return nbtTags;
    }

    @Nonnull
    @Override
    public NonNullList<ItemStack> getInventory() {
        return structure != null ? structure.getInventory() : null;
    }

    @Override
    public boolean onActivate(EntityPlayer player, EnumHand hand, ItemStack stack) {
        return false;
    }

    @Nonnull
    @Override
    @SideOnly(Side.CLIENT)
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
}