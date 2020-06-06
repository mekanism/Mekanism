package mekanism.common.util;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import mekanism.api.RelativeSide;
import mekanism.api.text.EnumColor;
import mekanism.common.content.transporter.TransporterManager;
import mekanism.common.content.transporter.TransporterStack;
import mekanism.common.lib.transmitter.IGridTransmitter;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.tile.TileEntityLogisticalSorter;
import mekanism.common.tile.interfaces.ILogisticalTransporter;
import mekanism.common.tile.interfaces.ISideConfiguration;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public final class TransporterUtils {

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
        if (tile instanceof IGridTransmitter && TransmissionType.ITEM.checkTransmissionType(((IGridTransmitter<?, ?, ?>) tile))) {
            return false;
        }
        return InventoryUtils.isItemHandler(tile, side.getOpposite());
    }

    public static EnumColor increment(EnumColor color) {
        if (color == null) {
            return colors.get(0);
        } else if (colors.indexOf(color) == colors.size() - 1) {
            return null;
        }
        return colors.get(colors.indexOf(color) + 1);
    }

    public static EnumColor decrement(EnumColor color) {
        if (color == null) {
            return colors.get(colors.size() - 1);
        } else if (colors.indexOf(color) == 0) {
            return null;
        }
        return colors.get(colors.indexOf(color) - 1);
    }

    public static void drop(ILogisticalTransporter tile, TransporterStack stack) {
        float[] pos;
        if (stack.hasPath()) {
            pos = TransporterUtils.getStackPosition(tile, stack, 0);
        } else {
            pos = new float[]{0, 0, 0};
        }
        TransporterManager.remove(stack);
        BlockPos blockPos = new BlockPos(tile.coord().x + pos[0], tile.coord().y + pos[1], tile.coord().z + pos[2]);
        Block.spawnAsEntity(tile.world(), blockPos, stack.itemStack);
    }

    public static float[] getStackPosition(ILogisticalTransporter tile, TransporterStack stack, float partial) {
        Direction side = stack.getSide(tile);
        float progress = ((stack.progress + partial) / 100F) - 0.5F;
        return new float[]{0.5F + side.getXOffset() * progress, 0.25F + side.getYOffset() * progress, 0.5F + side.getZOffset() * progress};
    }

    public static void incrementColor(ILogisticalTransporter tile) {
        if (tile.getColor() == null) {
            tile.setColor(colors.get(0));
        } else if (colors.indexOf(tile.getColor()) == colors.size() - 1) {
            tile.setColor(null);
        } else {
            int index = colors.indexOf(tile.getColor());
            tile.setColor(colors.get(index + 1));
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
        Optional<IItemHandler> capability = MekanismUtils.toOptional(CapabilityUtils.getCapability(tile, CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side.getOpposite()));
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