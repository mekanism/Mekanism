package mekanism.common.util;

import java.util.Arrays;
import java.util.List;
import mekanism.api.Coord4D;
import mekanism.api.EnumColor;
import mekanism.common.base.ILogisticalTransporter;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.content.transporter.TransitRequest;
import mekanism.common.content.transporter.TransitRequest.TransitResponse;
import mekanism.common.content.transporter.TransporterManager;
import mekanism.common.content.transporter.TransporterStack;
import mekanism.common.tile.TileEntityBin;
import mekanism.common.tile.TileEntityLogisticalSorter;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

public final class TransporterUtils {

    public static List<EnumColor> colors = Arrays
          .asList(EnumColor.DARK_BLUE, EnumColor.DARK_GREEN, EnumColor.DARK_AQUA, EnumColor.DARK_RED, EnumColor.PURPLE,
                EnumColor.INDIGO, EnumColor.BRIGHT_GREEN, EnumColor.AQUA, EnumColor.RED, EnumColor.PINK,
                EnumColor.YELLOW, EnumColor.BLACK);

    public static boolean isValidAcceptorOnSide(TileEntity tile, EnumFacing side) {
        if (CapabilityUtils.hasCapability(tile, Capabilities.GRID_TRANSMITTER_CAPABILITY, side.getOpposite())) {
            return false;
        }

        //Don't let the bin accept from all sides
        if (!(tile instanceof TileEntityBin) && InventoryUtils.isItemHandler(tile, side.getOpposite())) {
            return true;
        } else if (tile instanceof IInventory) {
            IInventory inventory = (IInventory) tile;

            if (inventory.getSizeInventory() > 0) {
                if (!(inventory instanceof ISidedInventory)) {
                    return true;
                }

                int[] slots = ((ISidedInventory) inventory).getSlotsForFace(side.getOpposite());

                return slots.length > 0;
            }
        }

        return false;
    }

    public static TransitResponse insert(TileEntity outputter, ILogisticalTransporter transporter,
          TransitRequest request, EnumColor color, boolean doEmit, int min) {
        return transporter.insert(Coord4D.get(outputter), request, color, doEmit, min);
    }

    public static TransitResponse insertRR(TileEntityLogisticalSorter outputter, ILogisticalTransporter transporter,
          TransitRequest request, EnumColor color, boolean doEmit, int min) {
        return transporter.insertRR(outputter, request, color, doEmit, min);
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

    public static void drop(ILogisticalTransporter tileEntity, TransporterStack stack) {
        float[] pos;

        if (stack.hasPath()) {
            pos = TransporterUtils.getStackPosition(tileEntity, stack, 0);
        } else {
            pos = new float[]{0, 0, 0};
        }

        TransporterManager.remove(stack);

        EntityItem entityItem = new EntityItem(tileEntity.world(), tileEntity.coord().x + pos[0],
              tileEntity.coord().y + pos[1], tileEntity.coord().z + pos[2], stack.itemStack);

        entityItem.motionX = 0;
        entityItem.motionY = 0;
        entityItem.motionZ = 0;

        tileEntity.world().spawnEntity(entityItem);
    }

    public static float[] getStackPosition(ILogisticalTransporter tileEntity, TransporterStack stack, float partial) {
        Coord4D offset = new Coord4D(0, 0, 0, tileEntity.world().provider.getDimension())
              .offset(stack.getSide(tileEntity));
        float progress = (((float) stack.progress + partial) / 100F) - 0.5F;

        return new float[]{0.5F + offset.x * progress, 0.25F + offset.y * progress, 0.5F + offset.z * progress};
    }

    public static void incrementColor(ILogisticalTransporter tileEntity) {
        if (tileEntity.getColor() == null) {
            tileEntity.setColor(colors.get(0));
            return;
        } else if (colors.indexOf(tileEntity.getColor()) == colors.size() - 1) {
            tileEntity.setColor(null);
            return;
        }

        int index = colors.indexOf(tileEntity.getColor());
        tileEntity.setColor(colors.get(index + 1));
    }
}
