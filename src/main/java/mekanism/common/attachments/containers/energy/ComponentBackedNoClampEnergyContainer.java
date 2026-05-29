package mekanism.common.attachments.containers.energy;

import java.util.function.IntSupplier;
import java.util.function.LongSupplier;
import java.util.function.Predicate;
import mekanism.api.AutomationType;
import mekanism.api.annotations.NothingNullByDefault;
import net.neoforged.neoforge.transfer.access.ItemAccess;

@NothingNullByDefault
public class ComponentBackedNoClampEnergyContainer extends ComponentBackedEnergyContainer {

    public ComponentBackedNoClampEnergyContainer(ItemAccess attachedAccess, Predicate<AutomationType> canExtract, Predicate<AutomationType> canInsert, IntSupplier rate,
          LongSupplier maxEnergy) {
        super(attachedAccess, canExtract, canInsert, rate, maxEnergy);
    }

    @Override
    protected long clampEnergy(long energy) {
        //Don't clamp the energy
        return energy;
    }
}