package mekanism.common.capabilities.holder.chemical;

import java.util.function.BiPredicate;
import java.util.function.Supplier;
import mekanism.api.AutomationType;
import mekanism.api.RelativeSide;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalTank;
import mekanism.common.lib.radiation.RadiationManager;
import mekanism.common.tile.interfaces.ISideConfiguration;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.NotNull;

public class ChemicalTankHelper {

    private final IChemicalTankHolder slotHolder;
    private boolean built;

    private ChemicalTankHelper(IChemicalTankHolder slotHolder) {
        this.slotHolder = slotHolder;
    }

    public static BiPredicate<ChemicalStack, @NotNull AutomationType> radioactiveInputTankPredicate(Supplier<IChemicalTank> outputTank) {
        //Allow extracting out of the input gas tank if it isn't external OR the output tank is empty AND the input is radioactive
        //Note: This only is the case if radiation is enabled as otherwise things like gauge droppers can work as the way to remove radioactive contents
        return (type, automationType) -> automationType != AutomationType.EXTERNAL ||
                                         (outputTank.get().isEmpty() && type.isRadioactive() && RadiationManager.isGlobalRadiationEnabled());
    }

    public static ChemicalTankHelper forSide(Supplier<Direction> facingSupplier) {
        return new ChemicalTankHelper(new ChemicalTankHolder(facingSupplier));
    }

    public static ChemicalTankHelper forSideWithConfig(ISideConfiguration sideConfiguration) {
        return new ChemicalTankHelper(new ConfigChemicalTankHolder(sideConfiguration));
    }

    public IChemicalTank addTank(IChemicalTank tank) {
        if (built) {
            throw new IllegalStateException("Builder has already built.");
        }
        if (slotHolder instanceof ChemicalTankHolder tankHolder) {
            tankHolder.addTank(tank);
        } else if (slotHolder instanceof ConfigChemicalTankHolder tankHolder) {
            tankHolder.addTank(tank);
        } else {
            throw new IllegalArgumentException("Holder does not know how to add tanks");
        }
        return tank;
    }

    public IChemicalTank addTank(IChemicalTank tank, RelativeSide... sides) {
        if (built) {
            throw new IllegalStateException("Builder has already built.");
        }
        if (slotHolder instanceof ChemicalTankHolder tankHolder) {
            tankHolder.addTank(tank, sides);
        } else {
            throw new IllegalArgumentException("Holder does not know how to add tanks on specific sides");
        }
        return tank;
    }

    public IChemicalTankHolder build() {
        built = true;
        return slotHolder;
    }
}