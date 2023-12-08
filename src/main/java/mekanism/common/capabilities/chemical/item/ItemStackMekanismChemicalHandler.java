package mekanism.common.capabilities.chemical.item;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import mekanism.api.IContentsListener;
import mekanism.api.annotations.ParametersAreNotNullByDefault;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.chemical.IMekanismChemicalHandler;
import mekanism.common.util.ItemDataUtils;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * Helper class for implementing chemical handlers for items
 */
@ParametersAreNotNullByDefault
@MethodsReturnNonnullByDefault
public abstract class ItemStackMekanismChemicalHandler<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>,
      TANK extends IChemicalTank<CHEMICAL, STACK>> implements IMekanismChemicalHandler<CHEMICAL, STACK, TANK> {

    protected final ItemStack stack;
    protected final List<TANK> tanks;

    @SafeVarargs
    protected ItemStackMekanismChemicalHandler(ItemStack stack, Function<IContentsListener, TANK>... tankProviders) {
        this.stack = stack;
        this.tanks = Arrays.stream(tankProviders)
              .map(provider -> provider.apply(this))
              .toList();
        ItemDataUtils.readContainers(this.stack, getNbtKey(), this.tanks);
    }

    @Override
    public void onContentsChanged() {
        ItemDataUtils.writeContainers(stack, getNbtKey(), getChemicalTanks(null));
    }

    @Override
    public List<TANK> getChemicalTanks(@Nullable Direction side) {
        return tanks;
    }

    protected abstract String getNbtKey();
}