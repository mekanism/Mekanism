package mekanism.common.inventory.container.tile;

import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.inventory.slot.IInventorySlot;
import mekanism.common.inventory.container.IEmptyContainer;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.MekanismUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

public abstract class MekanismTileContainer<TILE extends TileEntityMekanism> extends MekanismContainer {

    //TODO: Annotate this
    protected final TILE tile;

    protected MekanismTileContainer(@Nullable ContainerType<?> type, int id, @Nullable PlayerInventory inv, TILE tile) {
        super(type, id, inv);
        this.tile = tile;
        addSlotsAndOpen();
    }

    public TILE getTileEntity() {
        return tile;
    }

    @Override
    protected void openInventory(@Nonnull PlayerInventory inv) {
        if (tile != null) {
            tile.open(inv.player);
            //TODO: Some tiles had their update packet get sent (at the very least to the player opening it)
            /*if (!tile.isRemote()) {
                Mekanism.packetHandler.sendUpdatePacket(tile);
            }*/
        }
    }

    @Override
    protected void closeInventory(PlayerEntity player) {
        if (tile != null) {
            tile.close(player);
            //TODO: Do we need any specific closing code?
            //tile.closeInventory(player);
        }
    }

    @Override
    public boolean canInteractWith(@Nonnull PlayerEntity player) {
        if (tile == null) {
            return true;
        }
        //TODO: Double check this, it used to check to see if it had an inventory.
        if (tile.hasGui() && !tile.isRemoved()) {
            //prevent Containers from remaining valid after the chunk has unloaded;
            World world = tile.getWorld();
            if (world == null) {
                return false;
            }
            return world.isBlockLoaded(tile.getPos());
        }
        return false;
    }

    @Override
    protected void addSlots() {
        super.addSlots();
        if (this instanceof IEmptyContainer) {
            //Don't include the inventory slots
            return;
        }
        //TODO: Overwrite transferStackInSlot with the logic in the IInventorySlots??
        // NOTE: When implementing the generic transferStackInSlot stuff, we *should* probably ALWAYS allow manual extraction
        if (tile.hasInventory()) {
            //Get all the inventory slots the tile has
            List<IInventorySlot> inventorySlots = tile.getInventorySlots(null);
            for (IInventorySlot inventorySlot : inventorySlots) {
                Slot containerSlot = inventorySlot.createContainerSlot();
                if (containerSlot != null) {
                    addSlot(containerSlot);
                }
            }
        }
    }

    public static <TILE extends TileEntity> TILE getTileFromBuf(PacketBuffer buf, Class<TILE> type) {
        if (buf == null) {
            return null;
        }
        return DistExecutor.callWhenOn(Dist.CLIENT, () -> () -> MekanismUtils.getTileEntity(type, Minecraft.getInstance().world, buf.readBlockPos()));
    }
}