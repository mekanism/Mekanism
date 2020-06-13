package mekanism.common.inventory.container.tile;

import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.inventory.container.IEmptyContainer;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.registration.impl.ContainerTypeRegistryObject;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.MekanismUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

public class MekanismTileContainer<TILE extends TileEntityMekanism> extends MekanismContainer {

    //Note: We don't want our tile to be null but it technically can be if something went wrong
    // retrieving it, so we mark it as nullable to not instantly hard crash
    @Nullable
    protected final TILE tile;

    public MekanismTileContainer(ContainerTypeRegistryObject<?> type, int id, @Nullable PlayerInventory inv, @Nullable TILE tile) {
        super(type, id, inv);
        this.tile = tile;
        addContainerTrackers();
        addSlotsAndOpen();
    }

    protected void addContainerTrackers() {
        if (tile != null) {
            tile.addContainerTrackers(this);
        }
    }

    public TILE getTileEntity() {
        return tile;
    }

    @Override
    protected void openInventory(@Nonnull PlayerInventory inv) {
        if (tile != null) {
            tile.open(inv.player);
        }
    }

    @Override
    protected void closeInventory(PlayerEntity player) {
        if (tile != null) {
            tile.close(player);
        }
    }

    @Override
    public boolean canInteractWith(@Nonnull PlayerEntity player) {
        if (tile == null) {
            return true;
        }
        if (tile.hasGui() && !tile.isRemoved()) {
            //prevent Containers from remaining valid after the chunk has unloaded;
            World world = tile.getWorld();
            if (world == null) {
                return false;
            }
            return world.isBlockPresent(tile.getPos());
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
        if (tile != null && tile.hasInventory()) {
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
        return DistExecutor.unsafeCallWhenOn(Dist.CLIENT, () -> () -> MekanismUtils.getTileEntity(type, Minecraft.getInstance().world, buf.readBlockPos()));
    }
}