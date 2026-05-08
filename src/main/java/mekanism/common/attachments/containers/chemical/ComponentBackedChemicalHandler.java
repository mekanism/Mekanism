package mekanism.common.attachments.containers.chemical;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.chemical.IMekanismChemicalHandler;
import mekanism.common.attachments.containers.ComponentBackedHandler;
import mekanism.common.attachments.containers.ContainerType;
import net.minecraft.world.item.ItemStack;

@NothingNullByDefault
public class ComponentBackedChemicalHandler extends ComponentBackedHandler<ChemicalStack, IChemicalTank, AttachedChemicals> implements IMekanismChemicalHandler {

    public ComponentBackedChemicalHandler(ItemStack attachedTo, int totalTanks) {
        super(attachedTo, totalTanks);
    }

    @Override
    protected ContainerType<IChemicalTank, AttachedChemicals, ?> containerType() {
        return ContainerType.CHEMICAL;
    }
}