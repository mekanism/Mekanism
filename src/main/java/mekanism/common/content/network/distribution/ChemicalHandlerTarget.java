package mekanism.common.content.network.distribution;

import javax.annotation.Nonnull;
import mekanism.api.Action;
import mekanism.api.annotations.NonNull;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalHandler;
import mekanism.common.lib.distribution.SplitInfo;
import mekanism.common.lib.distribution.Target;
import mekanism.common.util.ChemicalUtil;

public class ChemicalHandlerTarget<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, HANDLER extends IChemicalHandler<CHEMICAL, STACK>>
      extends Target<HANDLER, Long, @NonNull STACK> {

    public ChemicalHandlerTarget(@Nonnull STACK type) {
        this.extra = type;
    }

    @Override
    protected void acceptAmount(HANDLER handler, SplitInfo<Long> splitInfo, Long amount) {
        splitInfo.send(amount - handler.insertChemical(ChemicalUtil.copyWithAmount(extra, amount), Action.EXECUTE).getAmount());
    }

    @Override
    protected Long simulate(HANDLER handler, @Nonnull STACK stack) {
        return stack.getAmount() - handler.insertChemical(stack, Action.SIMULATE).getAmount();
    }
}