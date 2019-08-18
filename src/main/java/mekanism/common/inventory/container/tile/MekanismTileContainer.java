package mekanism.common.inventory.container.tile;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
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
            tile.openInventory(inv.player);
        }
    }

    @Override
    protected void closeInventory(PlayerEntity player) {
        if (tile != null) {
            tile.close(player);
            tile.closeInventory(player);
        }
    }

    @Override
    public boolean canInteractWith(@Nonnull PlayerEntity player) {
        return tile == null || tile.isUsableByPlayer(player);
    }

    public static <TILE extends TileEntity> TILE getTileFromBuf(PacketBuffer buf, Class<TILE> type) {
        if (buf == null) {
            return null;
        }
        return DistExecutor.callWhenOn(Dist.CLIENT, () -> () -> {
            BlockPos pos = buf.readBlockPos();
            TileEntity tile = Minecraft.getInstance().world.getTileEntity(pos);
            if (type.isInstance(tile)) {
                return (TILE) tile;
            }
            return null;
        });
    }
}