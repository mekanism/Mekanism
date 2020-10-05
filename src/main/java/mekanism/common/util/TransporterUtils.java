package mekanism.common.util;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import mekanism.api.RelativeSide;
import mekanism.api.text.EnumColor;
import mekanism.common.content.network.transmitter.LogisticalTransporter;
import mekanism.common.content.network.transmitter.LogisticalTransporterBase;
import mekanism.common.content.transporter.TransporterManager;
import mekanism.common.content.transporter.TransporterStack;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.tile.TileEntityLogisticalSorter;
import mekanism.common.tile.interfaces.ISideConfiguration;
import mekanism.common.tile.transmitter.TileEntityTransmitter;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public final class TransporterUtils {

    private TransporterUtils() {
    }

    public static final List<EnumColor> colors = Arrays.asList(EnumColor.DARK_BLUE, EnumColor.DARK_GREEN, EnumColor.DARK_AQUA, EnumColor.DARK_RED, EnumColor.PURPLE,
          EnumColor.INDIGO, EnumColor.BRIGHT_GREEN, EnumColor.AQUA, EnumColor.RED, EnumColor.PINK, EnumColor.YELLOW, EnumColor.BLACK);

    @Nullable
    public static EnumColor readColor(int inputColor) {
        return inputColor == -1 ? null : TransporterUtils.colors.get(inputColor);
    }

    public static int getColorIndex(@Nullable EnumColor color) {
        return color == null ? -1 : TransporterUtils.colors.indexOf(color);
    }

    public static boolean isValidAcceptorOnSide(TileEntity tile, Direction side) {
        if (tile instanceof TileEntityTransmitter && TransmissionType.ITEM.checkTransmissionType((TileEntityTransmitter) tile)) {
            return false;
        }
        return InventoryUtils.isItemHandler(tile, side.getOpposite());
    }

    public static EnumColor increment(EnumColor color) {
        if (color == null) {
            return colors.get(0);
        }
        int index = colors.indexOf(color);
        return index == colors.size() - 1 ? null : colors.get(index + 1);
    }

    public static EnumColor decrement(EnumColor color) {
        if (color == null) {
            return colors.get(colors.size() - 1);
        }
        int index = colors.indexOf(color);
        return index == 0 ? null : colors.get(index - 1);
    }

    public static void drop(LogisticalTransporterBase transporter, TransporterStack stack) {
        BlockPos blockPos = transporter.getTilePos();
        if (stack.hasPath()) {
            float[] pos = TransporterUtils.getStackPosition(transporter, stack, 0);
            blockPos = blockPos.add(pos[0], pos[1], pos[2]);
        }
        TransporterManager.remove(transporter.getTileWorld(), stack);
        Block.spawnAsEntity(transporter.getTileWorld(), blockPos, stack.itemStack);
    }

    public static float[] getStackPosition(LogisticalTransporterBase transporter, TransporterStack stack, float partial) {
        Direction side = stack.getSide(transporter);
        float progress = ((stack.progress + partial) / 100F) - 0.5F;
        return new float[]{0.5F + side.getXOffset() * progress, 0.25F + side.getYOffset() * progress, 0.5F + side.getZOffset() * progress};
    }

    public static void incrementColor(LogisticalTransporter tile) {
        EnumColor color = tile.getColor();
        if (color == null) {
            tile.setColor(colors.get(0));
        } else {
            int index = colors.indexOf(color);
            if (index == colors.size() - 1) {
                tile.setColor(null);
            } else {
                tile.setColor(colors.get(index + 1));
            }
        }
    }

    public static boolean canInsert(TileEntity tile, EnumColor color, ItemStack itemStack, Direction side, boolean force) {
        if (force && tile instanceof TileEntityLogisticalSorter) {
            return ((TileEntityLogisticalSorter) tile).canSendHome(itemStack);
        }
        if (!force && tile instanceof ISideConfiguration) {
            ISideConfiguration config = (ISideConfiguration) tile;
            if (config.getEjector().hasStrictInput()) {
                Direction tileSide = config.getOrientation();
                EnumColor configColor = config.getEjector().getInputColor(RelativeSide.fromDirections(tileSide, side.getOpposite()));
                if (configColor != null && configColor != color) {
                    return false;
                }
            }
        }
        Optional<IItemHandler> capability = CapabilityUtils.getCapability(tile, CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side.getOpposite()).resolve();
        if (capability.isPresent()) {
            IItemHandler inventory = capability.get();
            for (int i = 0; i < inventory.getSlots(); i++) {
                // Check validation
                if (inventory.isItemValid(i, itemStack)) {
                    // Simulate insert
                    ItemStack rejects = inventory.insertItem(i, itemStack, true);
                    if (TransporterManager.didEmit(itemStack, rejects)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}