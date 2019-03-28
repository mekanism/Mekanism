package mekanism.common.tile;

import mekanism.common.security.ISecurityTile;
import mekanism.common.tile.component.TileComponentSecurity;
import mekanism.common.tile.prefab.TileEntityContainerBlock;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.SecurityUtils;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.fml.relauncher.Side;

public class TileEntityPersonalChest extends TileEntityContainerBlock implements ISecurityTile {

    public static int[] INV;

    public float lidAngle;

    public float prevLidAngle;

    public TileComponentSecurity securityComponent;

    public TileEntityPersonalChest() {
        super("PersonalChest");
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
                      SoundEvents.BLOCK_CHEST_CLOSE, SoundCategory.BLOCKS, 0.5F,
                      (world.rand.nextFloat() * 0.1F) + 0.9F);
            }

            if (lidAngle < 0.0F) {
                lidAngle = 0.0F;
            }
        }
    }

    @Override
    public boolean isItemValidForSlot(int slotID, ItemStack itemstack) {
        return true;
    }

    @Override
    public int[] getSlotsForFace(EnumFacing side) {
        if (side == EnumFacing.DOWN || SecurityUtils.getSecurity(this, Side.SERVER) != SecurityMode.PUBLIC) {
            return InventoryUtils.EMPTY;
        } else {
            if (INV == null) {
                INV = new int[54];

                for (int i = 0; i < INV.length; i++) {
                    INV[i] = i;
                }
            }

            return INV;
        }
    }

    @Override
    public boolean canExtractItem(int slotID, ItemStack itemstack, EnumFacing side) {
        return true;
    }

    @Override
    public boolean canSetFacing(int side) {
        return side != 0 && side != 1;
    }

    @Override
    public TileComponentSecurity getSecurity() {
        return securityComponent;
    }
}
