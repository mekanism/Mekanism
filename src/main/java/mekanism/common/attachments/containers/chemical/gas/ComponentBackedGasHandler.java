package mekanism.common.attachments.containers.chemical.gas;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasHandler.IMekanismGasHandler;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.common.attachments.containers.ContainerType;
import mekanism.common.attachments.containers.chemical.ComponentBackedChemicalHandler;
import net.minecraft.world.item.ItemStack;

@NothingNullByDefault
public class ComponentBackedGasHandler extends ComponentBackedChemicalHandler<Gas, GasStack, IGasTank, AttachedGases> implements IMekanismGasHandler {

    public ComponentBackedGasHandler(ItemStack attachedTo, int totalTanks) {
        super(attachedTo, totalTanks);
    }

    @Override
    protected ContainerType<IGasTank, AttachedGases, ?> containerType() {
        return ContainerType.GAS;
    }
}