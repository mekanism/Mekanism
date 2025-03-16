package mekanism.common.content.qio;

import java.util.List;
import mekanism.common.Mekanism;
import mekanism.common.attachments.qio.DriveContents;
import mekanism.common.inventory.slot.QIODriveSlot;
import mekanism.common.registries.MekanismDataComponents;
import net.minecraft.world.item.ItemStack;

public interface IQIODriveHolder extends IQIOFrequencyHolder {

    List<QIODriveSlot> getDriveSlots();

    void onDataUpdate();

    default void save(int slot, QIODriveData data) {
        ItemStack stack = getDriveSlots().get(slot).getStack();
        if (stack.getItem() instanceof IQIODriveItem) {
            stack.set(MekanismDataComponents.DRIVE_CONTENTS, DriveContents.create(data));
        } else {
            Mekanism.logger.error("Tried to save data map to an invalid item ({}). Something has gone very wrong!", stack.getItem());
        }
    }
}
