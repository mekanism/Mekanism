package mekanism.common.attachments.containers.energy;

import java.util.function.Predicate;
import mekanism.api.AutomationType;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.math.FloatingLong;
import mekanism.api.math.FloatingLongSupplier;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

@NothingNullByDefault
public class ComponentBackedNoClampEnergyContainer extends ComponentBackedEnergyContainer {

    public ComponentBackedNoClampEnergyContainer(ItemStack attachedTo, int containerIndex, Predicate<@NotNull AutomationType> canExtract,
          Predicate<@NotNull AutomationType> canInsert, FloatingLongSupplier rate, FloatingLongSupplier maxEnergy) {
        super(attachedTo, containerIndex, canExtract, canInsert, rate, maxEnergy);
    }

    @Override
    protected FloatingLong clampEnergy(FloatingLong energy) {
        //Don't clamp the energy
        return energy;
    }
}