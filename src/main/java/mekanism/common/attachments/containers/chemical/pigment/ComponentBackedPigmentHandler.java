package mekanism.common.attachments.containers.chemical.pigment;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.pigment.IPigmentHandler.IMekanismPigmentHandler;
import mekanism.api.chemical.pigment.IPigmentTank;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.common.attachments.containers.ContainerType;
import mekanism.common.attachments.containers.chemical.ComponentBackedChemicalHandler;
import net.minecraft.world.item.ItemStack;

@NothingNullByDefault
public class ComponentBackedPigmentHandler extends ComponentBackedChemicalHandler<Pigment, PigmentStack, IPigmentTank, AttachedPigments> implements IMekanismPigmentHandler {

    public ComponentBackedPigmentHandler(ItemStack attachedTo) {
        super(attachedTo);
    }

    @Override
    protected ContainerType<IPigmentTank, AttachedPigments, ?> containerType() {
        return ContainerType.PIGMENT;
    }
}