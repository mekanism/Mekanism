package mekanism.common.attachments.containers.chemical;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.ChemicalResource;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.chemical.IMekanismChemicalHandler;
import mekanism.common.attachments.containers.ComponentBackedResourceHandler;
import mekanism.common.attachments.containers.AttachedResources;
import mekanism.common.attachments.containers.ContainerType;
import net.minecraft.world.item.ItemStack;

@NothingNullByDefault
public class ComponentBackedChemicalHandler extends ComponentBackedResourceHandler<ChemicalResource, IChemicalTank> implements IMekanismChemicalHandler {

    public ComponentBackedChemicalHandler(ItemStack attachedTo, int totalTanks) {
        super(attachedTo, totalTanks);
    }

    @Override
    protected ContainerType<IChemicalTank, AttachedResources<ChemicalResource>, ?> containerType() {
        return ContainerType.CHEMICAL;
    }
}