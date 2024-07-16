package mekanism.common.attachments.containers.energy;

import java.util.function.LongSupplier;
import java.util.function.Predicate;
import mekanism.api.AutomationType;
import mekanism.api.annotations.NothingNullByDefault;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

@NothingNullByDefault
public class ComponentBackedNoClampEnergyContainer extends ComponentBackedEnergyContainer {

    public ComponentBackedNoClampEnergyContainer(ItemStack attachedTo, int containerIndex, Predicate<@NotNull AutomationType> canExtract,
          Predicate<@NotNull AutomationType> canInsert, LongSupplier rate, LongSupplier maxEnergy) {
        super(attachedTo, containerIndex, canExtract, canInsert, rate, maxEnergy);
    }

    @Override
    protected long clampEnergy(long energy) {
        //Don't clamp the energy
        return energy;
    }
}