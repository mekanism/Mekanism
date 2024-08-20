package mekanism.common.attachments.containers.chemical;

import java.util.List;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.ChemicalUtils;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.chemical.IMekanismChemicalHandler;
import mekanism.common.attachments.containers.ComponentBackedHandler;
import mekanism.common.attachments.containers.ContainerType;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class ComponentBackedChemicalHandler extends ComponentBackedHandler<ChemicalStack, IChemicalTank, AttachedChemicals> implements IMekanismChemicalHandler {

    public ComponentBackedChemicalHandler(ItemStack attachedTo, int totalTanks) {
        super(attachedTo, totalTanks);
    }

    @Override
    protected ContainerType<IChemicalTank, AttachedChemicals, ?> containerType() {
        return ContainerType.CHEMICAL;
    }

    @Override
    public List<IChemicalTank> getChemicalTanks(@Nullable Direction side) {
        return getContainers();
    }

    @Override
    public IChemicalTank getChemicalTank(int tank, @Nullable Direction side) {
        return getContainer(tank);
    }

    @Override
    public int getCountChemicalTanks(@Nullable Direction side) {
        return size();
    }

    @Override
    public ChemicalStack getChemicalInTank(int tank, @Nullable Direction side) {
        return getContents(tank);
    }

    @Override
    public ChemicalStack insertChemical(ChemicalStack stack, @Nullable Direction side, Action action) {
        return ChemicalUtils.insert(stack, action, AutomationType.handler(side), size(), this);
    }

    @Override
    public ChemicalStack extractChemical(long amount, @Nullable Direction side, Action action) {
        return ChemicalUtils.extract(amount, action, AutomationType.handler(side), size(), this);
    }

    @Override
    public ChemicalStack extractChemical(ChemicalStack stack, @Nullable Direction side, Action action) {
        return ChemicalUtils.extract(stack, action, AutomationType.handler(side), size(), this);
    }
}