package mekanism.common.util;

import mekanism.api.RelativeSide;
import mekanism.api.text.EnumColor;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.content.network.transmitter.LogisticalTransporterBase;
import mekanism.common.content.transporter.TransporterManager;
import mekanism.common.content.transporter.TransporterStack;
import mekanism.common.lib.inventory.IAdvancedTransportEjector;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.tile.interfaces.ISideConfiguration;
import mekanism.common.tile.transmitter.TileEntityTransmitter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

public final class TransporterUtils {

    private TransporterUtils() {
    }

    @Nullable
    public static EnumColor readColor(int inputColor) {
        if (inputColor < 0) {
            return null;
        }
        return EnumColor.BY_ID.apply(inputColor);
    }

    public static int getColorIndex(@Nullable EnumColor color) {
        return color == null ? -1 : color.ordinal();
    }

    public static boolean isValidAcceptorOnSide(Level level, BlockPos pos, Direction side) {
        return isValidAcceptorOnSide(level, pos, WorldUtils.getTileEntity(level, pos), side);
    }

    public static boolean isValidAcceptorOnSide(Level level, BlockPos pos, @Nullable BlockEntity tile, Direction side) {
        if (tile instanceof TileEntityTransmitter transmitter && TransmissionType.ITEM.checkTransmissionType(transmitter)) {
            return false;
        }
        return Capabilities.ITEM.getCapabilityIfLoaded(level, pos, null, tile, side) != null;
    }

    public static EnumColor increment(@Nullable EnumColor color) {
        if (color == null) {
            return EnumUtils.COLORS[0];
        } else if (color.ordinal() == EnumUtils.COLORS.length - 1) {
            return null;
        }
        return color.getNext();
    }

    public static EnumColor decrement(@Nullable EnumColor color) {
        if (color == null) {
            return EnumUtils.COLORS[EnumUtils.COLORS.length - 1];
        }
        return color.ordinal() == 0 ? null : color.getPrevious();
    }

    public static void drop(LogisticalTransporterBase transporter, TransporterStack stack) {
        BlockPos blockPos;
        if (stack.hasPath()) {
            float[] pos = getStackPosition(transporter, stack, 0);
            blockPos = transporter.getBlockPos().offset(Mth.floor(pos[0]), Mth.floor(pos[1]), Mth.floor(pos[2]));
        } else {
            blockPos = transporter.getBlockPos();
        }
        TransporterManager.remove(transporter.getLevel(), stack);
        InventoryUtils.dropStack(transporter.getLevel(), blockPos, null, stack.itemStack, (level, pos, ignored, item) -> Block.popResource(level, pos, item));
    }

    public static float[] getStackPosition(LogisticalTransporterBase transporter, TransporterStack stack, float partial) {
        Direction side = stack.getSide(transporter);
        float progress = ((stack.progress + partial) / 100F) - 0.5F;
        return new float[]{0.5F + side.getStepX() * progress, 0.25F + side.getStepY() * progress, 0.5F + side.getStepZ() * progress};
    }

    public static boolean canInsert(Level level, BlockPos pos, EnumColor color, ItemStack itemStack, Direction side, boolean force) {
        return canInsert(level, pos, WorldUtils.getTileEntity(level, pos), color, itemStack, side, force);
    }

    public static boolean canInsert(Level level, BlockPos pos, @Nullable BlockEntity tile, EnumColor color, ItemStack itemStack, Direction side, boolean force) {
        if (force && tile instanceof IAdvancedTransportEjector sorter) {
            return sorter.canSendHome(itemStack);
        }
        if (!force && tile instanceof ISideConfiguration config && config.getEjector().hasStrictInput()) {
            Direction tileSide = config.getDirection();
            EnumColor configColor = config.getEjector().getInputColor(RelativeSide.fromDirections(tileSide, side.getOpposite()));
            if (configColor != null && configColor != color) {
                return false;
            }
        }
        IItemHandler inventory = Capabilities.ITEM.getCapabilityIfLoaded(level, pos, null, tile, side.getOpposite());
        if (inventory != null) {
            for (int i = 0, slots = inventory.getSlots(); i < slots; i++) {
                // Simulate insert, this will handle validating the item is valid for the inventory
                ItemStack rejects = inventory.insertItem(i, itemStack, true);
                if (TransporterManager.didEmit(itemStack, rejects)) {
                    return true;
                }
            }
        }
        return false;
    }
}