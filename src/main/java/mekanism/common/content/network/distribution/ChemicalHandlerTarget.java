package mekanism.common.content.network.distribution;

import mekanism.api.Action;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalHandler;
import mekanism.common.lib.distribution.SplitInfo;
import mekanism.common.lib.distribution.Target;
import org.jetbrains.annotations.NotNull;

public class ChemicalHandlerTarget<CHEMICAL extends Chemical, STACK extends ChemicalStack, HANDLER extends IChemicalHandler>
      extends Target<HANDLER, Long, @NotNull STACK> {

    public ChemicalHandlerTarget(@NotNull STACK type) {
        this.extra = type;
    }

    public ChemicalHandlerTarget(@NotNull STACK type, int expectedSize) {
        super(expectedSize);
        this.extra = type;
    }

    @Override
    protected void acceptAmount(HANDLER handler, SplitInfo<Long> splitInfo, Long amount) {
        splitInfo.send(amount - handler.insertChemical(extra.copyWithAmount(amount), Action.EXECUTE).getAmount());
    }

    @Override
    protected Long simulate(HANDLER handler, @NotNull STACK stack) {
        return stack.getAmount() - handler.insertChemical(stack, Action.SIMULATE).getAmount();
    }
}