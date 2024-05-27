package mekanism.common.attachments.containers.chemical.slurry;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.slurry.ISlurryHandler.IMekanismSlurryHandler;
import mekanism.api.chemical.slurry.ISlurryTank;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.common.attachments.containers.ContainerType;
import mekanism.common.attachments.containers.chemical.ComponentBackedChemicalHandler;
import net.minecraft.world.item.ItemStack;

@NothingNullByDefault
public class ComponentBackedSlurryHandler extends ComponentBackedChemicalHandler<Slurry, SlurryStack, ISlurryTank, AttachedSlurries> implements IMekanismSlurryHandler {

    public ComponentBackedSlurryHandler(ItemStack attachedTo, int totalTanks) {
        super(attachedTo, totalTanks);
    }

    @Override
    protected ContainerType<ISlurryTank, AttachedSlurries, ?> containerType() {
        return ContainerType.SLURRY;
    }
}