package mekanism.common.attachments.containers.chemical;

import java.util.List;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.ChemicalUtils;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.chemical.IMekanismChemicalHandler;
import mekanism.common.attachments.containers.ComponentBackedHandler;
import mekanism.common.attachments.containers.IAttachedContainers;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public abstract class ComponentBackedChemicalHandler<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>,
      TANK extends IChemicalTank<CHEMICAL, STACK>, ATTACHED extends IAttachedContainers<STACK, ATTACHED>>
      extends ComponentBackedHandler<STACK, TANK, ATTACHED> implements IMekanismChemicalHandler<CHEMICAL, STACK, TANK> {

    public ComponentBackedChemicalHandler(ItemStack attachedTo) {
        super(attachedTo);
    }

    @Override
    public List<TANK> getChemicalTanks(@Nullable Direction side) {
        return getContainers();
    }

    @Nullable
    @Override
    public TANK getChemicalTank(int tank, @Nullable Direction side) {
        return getContainer(tank);
    }

    @Override
    public int getTanks(@Nullable Direction side) {
        return containerCount();
    }

    @Override
    public STACK getChemicalInTank(int tank, @Nullable Direction side) {
        ATTACHED attachedChemicals = getAttached();
        return attachedChemicals == null ? getEmptyStack() : attachedChemicals.get(tank);
    }

    @Override
    public STACK insertChemical(STACK stack, @Nullable Direction side, Action action) {
        //TODO - 1.20.5: Can we optimize this any further? Maybe by somehow only initializing the chemical tanks as necessary/we actually get to iterating against them?
        return ChemicalUtils.insert(stack, side, this::getChemicalTanks, action, AutomationType.handler(side), getEmptyStack());
    }

    @Override
    public STACK extractChemical(long amount, @Nullable Direction side, Action action) {
        //TODO - 1.20.5: Can we optimize this any further? Maybe by somehow only initializing the chemical tanks as necessary/we actually get to iterating against them?
        return ChemicalUtils.extract(amount, side, this::getChemicalTanks, action, AutomationType.handler(side), getEmptyStack());
    }

    @Override
    public STACK extractChemical(STACK stack, @Nullable Direction side, Action action) {
        //TODO - 1.20.5: Can we optimize this any further? Maybe by somehow only initializing the chemical tanks as necessary/we actually get to iterating against them?
        return ChemicalUtils.extract(stack, side, this::getChemicalTanks, action, AutomationType.handler(side), getEmptyStack());
    }
}