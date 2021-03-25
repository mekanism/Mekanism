package mekanism.common.inventory.container.tile;

import java.util.List;
import javax.annotation.Nonnull;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.inventory.container.IEmptyContainer;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.lib.security.ISecurityObject;
import mekanism.common.registration.impl.ContainerTypeRegistryObject;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.WorldUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

public class MekanismTileContainer<TILE extends TileEntityMekanism> extends MekanismContainer {

    @Nonnull
    protected final TILE tile;

    public MekanismTileContainer(ContainerTypeRegistryObject<?> type, int id, PlayerInventory inv, @Nonnull TILE tile) {
        super(type, id, inv);
        this.tile = tile;
        addContainerTrackers();
        addSlotsAndOpen();
    }

    protected void addContainerTrackers() {
        tile.addContainerTrackers(this);
    }

    public TILE getTileEntity() {
        return tile;
    }

    @Override
    public ISecurityObject getSecurityObject() {
        return getTileEntity();
    }

    @Override
    protected void openInventory(@Nonnull PlayerInventory inv) {
        tile.open(inv.player);
    }

    @Override
    protected void closeInventory(@Nonnull PlayerEntity player) {
        tile.close(player);
    }

    @Override
    public boolean stillValid(@Nonnull PlayerEntity player) {
        //prevent Containers from remaining valid after the chunk has unloaded;
        return tile.hasGui() && !tile.isRemoved() && WorldUtils.isBlockLoaded(tile.getLevel(), tile.getBlockPos());
    }

    @Override
    protected void addSlots() {
        super.addSlots();
        if (this instanceof IEmptyContainer) {
            //Don't include the inventory slots
            return;
        }
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

    @Nonnull
    public static <TILE extends TileEntity> TILE getTileFromBuf(PacketBuffer buf, Class<TILE> type) {
        if (buf == null) {
            throw new IllegalArgumentException("Null packet buffer");
        }
        return DistExecutor.unsafeCallWhenOn(Dist.CLIENT, () -> () -> {
            BlockPos pos = buf.readBlockPos();
            TILE tile = WorldUtils.getTileEntity(type, Minecraft.getInstance().level, pos);
            if (tile == null) {
                throw new IllegalStateException("Client could not locate tile at " + pos + " for tile container. "
                                                + "This is likely caused by a mod breaking client side tile lookup");
            }
            return tile;
        });
    }
}