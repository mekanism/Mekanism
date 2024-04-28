package mekanism.common.attachments.containers.chemical.infuse;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.infuse.IInfusionHandler.IMekanismInfusionHandler;
import mekanism.api.chemical.infuse.IInfusionTank;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.common.attachments.containers.ContainerType;
import mekanism.common.attachments.containers.chemical.ComponentBackedChemicalHandler;
import net.minecraft.world.item.ItemStack;

@NothingNullByDefault
public class ComponentBackedInfusionHandler extends ComponentBackedChemicalHandler<InfuseType, InfusionStack, IInfusionTank, AttachedInfuseTypes> implements IMekanismInfusionHandler {

    public ComponentBackedInfusionHandler(ItemStack attachedTo) {
        super(attachedTo);
    }

    @Override
    protected ContainerType<IInfusionTank, AttachedInfuseTypes, ?> containerType() {
        return ContainerType.INFUSION;
    }
}