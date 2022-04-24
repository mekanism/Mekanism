package mekanism.common.util;

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
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public final class TransporterUtils {

    private TransporterUtils() {
    }

    public static final List<EnumColor> colors = List.of(EnumColor.DARK_BLUE, EnumColor.DARK_GREEN, EnumColor.DARK_AQUA, EnumColor.DARK_RED, EnumColor.PURPLE,
          EnumColor.INDIGO, EnumColor.BRIGHT_GREEN, EnumColor.AQUA, EnumColor.RED, EnumColor.PINK, EnumColor.YELLOW, EnumColor.BLACK);

    @Nullable
    public static EnumColor readColor(int inputColor) {
        return inputColor == -1 ? null : TransporterUtils.colors.get(inputColor);
    }

    public static int getColorIndex(@Nullable EnumColor color) {
        return color == null ? -1 : TransporterUtils.colors.indexOf(color);
    }

    public static boolean isValidAcceptorOnSide(BlockEntity tile, Direction side) {
        if (tile instanceof TileEntityTransmitter transmitter && TransmissionType.ITEM.checkTransmissionType(transmitter)) {
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
            blockPos = blockPos.offset(pos[0], pos[1], pos[2]);
        }
        TransporterManager.remove(transporter.getTileWorld(), stack);
        Block.popResource(transporter.getTileWorld(), blockPos, stack.itemStack);
    }

    public static float[] getStackPosition(LogisticalTransporterBase transporter, TransporterStack stack, float partial) {
        Direction side = stack.getSide(transporter);
        float progress = ((stack.progress + partial) / 100F) - 0.5F;
        return new float[]{0.5F + side.getStepX() * progress, 0.25F + side.getStepY() * progress, 0.5F + side.getStepZ() * progress};
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

    public static boolean canInsert(BlockEntity tile, EnumColor color, ItemStack itemStack, Direction side, boolean force) {
        if (force && tile instanceof TileEntityLogisticalSorter sorter) {
            return sorter.canSendHome(itemStack);
        }
        if (!force && tile instanceof ISideConfiguration config && config.getEjector().hasStrictInput()) {
            Direction tileSide = config.getDirection();
            EnumColor configColor = config.getEjector().getInputColor(RelativeSide.fromDirections(tileSide, side.getOpposite()));
            if (configColor != null && configColor != color) {
                return false;
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