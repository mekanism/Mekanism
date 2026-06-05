package mekanism.common.util;

import mekanism.api.RelativeSide;
import mekanism.api.text.EnumColor;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.content.network.transmitter.LogisticalTransporterBase;
import mekanism.common.content.transporter.TransporterStack;
import mekanism.common.lib.inventory.IAdvancedTransportEjector;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.tile.interfaces.ISideConfiguration;
import mekanism.common.tile.transmitter.TileEntityTransmitter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

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

    public static Vector3f getStackPosition(LogisticalTransporterBase transporter, TransporterStack stack, float partial) {
        return stack.getSide(transporter)
              .step()//Note: Direction#step returns a new Vector3f
              .mul(((stack.progress + partial) / 100F) - 0.5F)
              .add(0.5F, 0.25F, 0.5F);
    }

    public static boolean canInsert(Level level, BlockPos pos, EnumColor color, ItemResource itemType, int itemAmount, Direction side, boolean force, @Nullable TransactionContext transaction) {
        return canInsert(level, pos, WorldUtils.getTileEntity(level, pos), color, itemType, itemAmount, side, force, transaction);
    }

    public static boolean canInsert(Level level, BlockPos pos, @Nullable BlockEntity tile, EnumColor color, ItemResource itemType, int itemAmount, Direction side,
          boolean force, @Nullable TransactionContext transaction) {
        TransferPreconditions.checkNonEmptyNonNegative(itemType, itemAmount);
        if (itemAmount == 0) {
            //Note: Theoretically this should never be zero when passed, but if it is, just return that it can be inserted as there is nothing to insert
            return true;
        } else if (force && tile instanceof IAdvancedTransportEjector sorter) {
            return sorter.canSendHome(itemType, itemAmount, transaction);
        }
        if (!force && tile instanceof ISideConfiguration config && config.getEjector().hasStrictInput()) {
            Direction tileSide = config.getDirection();
            EnumColor configColor = config.getEjector().getInputColor(RelativeSide.fromDirections(tileSide, side.getOpposite()));
            if (configColor != null && configColor != color) {
                return false;
            }
        }
        ResourceHandler<ItemResource> inventory = Capabilities.ITEM.getCapabilityIfLoaded(level, pos, null, tile, side.getOpposite());
        if (inventory == null) {
            return false;
        }
        try (Transaction simulation = Transaction.open(transaction)) {
            //Simulate insert, this will handle validating the item is valid for the inventory, and that at least some of it can be accepted
            return inventory.insert(itemType, itemAmount, simulation) > 0;
        }
    }
}