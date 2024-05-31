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

    public ComponentBackedChemicalHandler(ItemStack attachedTo, int totalTanks) {
        super(attachedTo, totalTanks);
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
        return size();
    }

    @Override
    public STACK getChemicalInTank(int tank, @Nullable Direction side) {
        return getContents(tank);
    }

    @Override
    public STACK insertChemical(STACK stack, @Nullable Direction side, Action action) {
        return ChemicalUtils.insert(stack, action, AutomationType.handler(side), getEmptyStack(), size(), this);
    }

    @Override
    public STACK extractChemical(long amount, @Nullable Direction side, Action action) {
        return ChemicalUtils.extract(amount, action, AutomationType.handler(side), getEmptyStack(), size(), this);
    }

    @Override
    public STACK extractChemical(STACK stack, @Nullable Direction side, Action action) {
        return ChemicalUtils.extract(stack, action, AutomationType.handler(side), getEmptyStack(), size(), this);
    }
}