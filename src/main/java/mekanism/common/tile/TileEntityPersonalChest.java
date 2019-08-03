package mekanism.common.tile;

import javax.annotation.Nonnull;
import mekanism.common.security.ISecurityTile;
import mekanism.common.tile.base.TileEntityContainer;
import mekanism.common.tile.component.TileComponentSecurity;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.SecurityUtils;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.items.CapabilityItemHandler;

public class TileEntityPersonalChest extends TileEntityContainer implements ISecurityTile {

    public static int[] INV;

    public float lidAngle;

    public float prevLidAngle;

    public TileComponentSecurity securityComponent;

    public TileEntityPersonalChest() {
        inventory = NonNullList.withSize(54, ItemStack.EMPTY);
        securityComponent = new TileComponentSecurity(this);
    }

    @Override
    public void onUpdate() {
        prevLidAngle = lidAngle;
        float increment = 0.1F;
        if ((playersUsing.size() > 0) && (lidAngle == 0.0F)) {
            world.playSound(null, getPos().getX() + 0.5F, getPos().getY() + 0.5D, getPos().getZ() + 0.5F,
                  SoundEvents.BLOCK_CHEST_OPEN, SoundCategory.BLOCKS, 0.5F, (world.rand.nextFloat() * 0.1F) + 0.9F);
        }

        if ((playersUsing.size() == 0 && lidAngle > 0.0F) || (playersUsing.size() > 0 && lidAngle < 1.0F)) {
            float angle = lidAngle;
            if (playersUsing.size() > 0) {
                lidAngle += increment;
            } else {
                lidAngle -= increment;
            }
            if (lidAngle > 1.0F) {
                lidAngle = 1.0F;
            }
            float split = 0.5F;
            if (lidAngle < split && angle >= split) {
                world.playSound(null, getPos().getX() + 0.5D, getPos().getY() + 0.5D, getPos().getZ() + 0.5D,
                      SoundEvents.BLOCK_CHEST_CLOSE, SoundCategory.BLOCKS, 0.5F, (world.rand.nextFloat() * 0.1F) + 0.9F);
            }
            if (lidAngle < 0.0F) {
                lidAngle = 0.0F;
            }
        }
    }

    @Override
    public boolean isItemValidForSlot(int slotID, @Nonnull ItemStack itemstack) {
        return true;
    }

    @Nonnull
    @Override
    public int[] getSlotsForFace(@Nonnull EnumFacing side) {
        if (side == EnumFacing.DOWN || SecurityUtils.getSecurity(this, Side.SERVER) != SecurityMode.PUBLIC) {
            return InventoryUtils.EMPTY;
        } else if (INV == null) {
            INV = new int[54];
            for (int i = 0; i < INV.length; i++) {
                INV[i] = i;
            }
        }
        return INV;
    }

    @Override
    public boolean isCapabilityDisabled(@Nonnull Capability<?> capability, EnumFacing side) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            //Still allow for the capability if it is not public. It just won't
            // return any slots for the face. It doesn't properly sync when the state
            // changes so the pipes stay connected/disconnected and have to be replaced.
            // Leaving the slotsForFace to determine the ability to insert/extract in
            // those cases fixes that issue.
            return side == EnumFacing.DOWN;
        }
        return super.isCapabilityDisabled(capability, side);
    }

    @Override
    public boolean canExtractItem(int slotID, @Nonnull ItemStack itemstack, @Nonnull EnumFacing side) {
        return true;
    }

    @Override
    public boolean canSetFacing(@Nonnull EnumFacing facing) {
        return facing != EnumFacing.DOWN && facing != EnumFacing.UP;
    }

    @Override
    public TileComponentSecurity getSecurity() {
        return securityComponent;
    }
}