package mekanism.common.capabilities.resolver.manager;

import java.util.Collections;
import java.util.List;
import mekanism.api.IContentsListener;
import mekanism.api.annotations.ParametersAreNotNullByDefault;
import mekanism.api.chemical.ChemicalResource;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.chemical.IMekanismChemicalHandler;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.holder.chemical.IChemicalTankHolder;
import mekanism.common.capabilities.proxy.ProxyResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Helper class to make reading instead of having as messy generics
 */
@ParametersAreNotNullByDefault
public class ChemicalHandlerManager extends CapabilityHandlerManager<IChemicalTankHolder, IChemicalTank, ResourceHandler<ChemicalResource>> {

    public ChemicalHandlerManager(@Nullable IChemicalTankHolder holder, @Nullable IContentsListener changeListener) {
        super(holder, Capabilities.CHEMICAL.block(), IChemicalTankHolder::getTanks, (side, h) -> new ProxyResourceHandler<>(new IMekanismChemicalHandler() {
            @Override
            public void onContentsChanged() {
                if (changeListener != null) {
                    changeListener.onContentsChanged();
                }
            }

            @NotNull
            @Override
            public List<IChemicalTank> getContainers() {
                //Note: This instance of check should always pass, but we have it in case we are passed a null holder
                return h instanceof IChemicalTankHolder tankHolder ? tankHolder.getTanks(side) : Collections.emptyList();
            }
        }, side, h));
    }
}