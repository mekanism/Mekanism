package mekanism.common.tile;

import java.util.function.Predicate;
import javax.annotation.Nonnull;
import mekanism.api.RelativeSide;
import mekanism.api.annotations.NonNull;
import mekanism.common.inventory.slot.BasicInventorySlot;
import mekanism.common.inventory.slot.holder.IInventorySlotHolder;
import mekanism.common.inventory.slot.holder.InventorySlotHelper;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.SecurityUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraftforge.api.distmarker.Dist;

public class TileEntityPersonalChest extends TileEntityMekanism {

    public static int[] INV;

    public float lidAngle;

    public float prevLidAngle;

    public TileEntityPersonalChest() {
        super(MekanismBlocks.PERSONAL_CHEST);
    }

    @Nonnull
    @Override
    protected IInventorySlotHolder getInitialInventory() {
        InventorySlotHelper builder = InventorySlotHelper.forSide(this::getDirection);
        Predicate<@NonNull ItemStack> canExtract = item -> SecurityUtils.getSecurity(this, Dist.DEDICATED_SERVER) != SecurityMode.PUBLIC;
        RelativeSide[] sides = new RelativeSide[]{RelativeSide.TOP, RelativeSide.FRONT, RelativeSide.LEFT, RelativeSide.RIGHT, RelativeSide.BACK};
        for (int slotY = 0; slotY < 6; slotY++) {
            for (int slotX = 0; slotX < 9; slotX++) {
                builder.addSlot(BasicInventorySlot.at(canExtract, item -> true, this, 8 + slotX * 18, 26 + slotY * 18), sides);
            }
        }
        //TODO: Update this comment it is from isCapabilityDisabled. We reimplemented HOW it acted above but it maybe should be done somewhat differently
        //Still allow for the capability if it is not public. It just won't
        // return any slots for the face. It doesn't properly sync when the state
        // changes so the pipes stay connected/disconnected and have to be replaced.
        // Leaving the slotsForFace to determine the ability to insert/extract in
        // those cases fixes that issue.
        return builder.build();
    }

    @Override
    public void onUpdate() {
        prevLidAngle = lidAngle;
        float increment = 0.1F;
        if (!playersUsing.isEmpty() && lidAngle == 0.0F) {
            world.playSound(null, getPos().getX() + 0.5F, getPos().getY() + 0.5D, getPos().getZ() + 0.5F,
                  SoundEvents.BLOCK_CHEST_OPEN, SoundCategory.BLOCKS, 0.5F, (world.rand.nextFloat() * 0.1F) + 0.9F);
        }

        if ((playersUsing.isEmpty() && lidAngle > 0.0F) || (!playersUsing.isEmpty() && lidAngle < 1.0F)) {
            float angle = lidAngle;
            if (playersUsing.isEmpty()) {
                lidAngle -= increment;
            } else {
                lidAngle += increment;
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
}