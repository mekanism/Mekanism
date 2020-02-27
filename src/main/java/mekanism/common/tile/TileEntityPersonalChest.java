package mekanism.common.tile;

import java.util.function.Predicate;
import javax.annotation.Nonnull;
import mekanism.api.annotations.NonNull;
import mekanism.common.inventory.slot.BasicInventorySlot;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
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
        Predicate<@NonNull ItemStack> isPublic = item -> SecurityUtils.getSecurity(this, Dist.DEDICATED_SERVER) == SecurityMode.PUBLIC;
        for (int slotY = 0; slotY < 6; slotY++) {
            for (int slotX = 0; slotX < 9; slotX++) {
                //Note: we allow access to the slots from all sides as long as it is public, unlike in 1.12 where we always denied the bottom face
                // We did that to ensure that things like hoppers that could check IInventory did not bypass any restrictions
                builder.addSlot(BasicInventorySlot.at(isPublic, isPublic, this, 8 + slotX * 18, 26 + slotY * 18));
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