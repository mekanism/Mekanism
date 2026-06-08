package mekanism.common.content.qio;

import java.util.List;
import mekanism.common.inventory.slot.QIODriveSlot;

public interface IQIODriveHolder extends IQIOFrequencyHolder {

    List<QIODriveSlot> getDriveSlots();
}
