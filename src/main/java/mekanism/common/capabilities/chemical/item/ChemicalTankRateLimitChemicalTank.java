package mekanism.common.capabilities.chemical.item;

import java.util.function.BiPredicate;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.Action;
import mekanism.api.IContentsListener;
import mekanism.api.annotations.NonNull;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.attribute.ChemicalAttributeValidator;
import mekanism.api.chemical.gas.BasicGasTank;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasHandler;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.api.chemical.infuse.BasicInfusionTank;
import mekanism.api.chemical.infuse.IInfusionHandler;
import mekanism.api.chemical.infuse.IInfusionTank;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.chemical.pigment.BasicPigmentTank;
import mekanism.api.chemical.pigment.IPigmentHandler;
import mekanism.api.chemical.pigment.IPigmentTank;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.chemical.slurry.BasicSlurryTank;
import mekanism.api.chemical.slurry.ISlurryHandler;
import mekanism.api.chemical.slurry.ISlurryTank;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.api.inventory.AutomationType;
import mekanism.common.capabilities.chemical.variable.RateLimitChemicalTank;
import mekanism.common.tier.ChemicalTankTier;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class ChemicalTankRateLimitChemicalTank<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> extends RateLimitChemicalTank<CHEMICAL, STACK> {

    private final boolean isCreative;

    private ChemicalTankRateLimitChemicalTank(ChemicalTankTier tier, BiPredicate<@NonNull CHEMICAL, @NonNull AutomationType> canExtract,
          BiPredicate<@NonNull CHEMICAL, @NonNull AutomationType> canInsert, Predicate<@NonNull CHEMICAL> validator, @Nullable IContentsListener listener) {
        super(tier::getOutput, tier::getStorage, canExtract, canInsert, validator, tier == ChemicalTankTier.CREATIVE ? ChemicalAttributeValidator.ALWAYS_ALLOW : null,
              listener);
        isCreative = tier == ChemicalTankTier.CREATIVE;
    }

    @Override
    public STACK insert(STACK stack, Action action, AutomationType automationType) {
        return super.insert(stack, action.combine(!isCreative), automationType);
    }

    @Override
    public STACK extract(long amount, Action action, AutomationType automationType) {
        return super.extract(amount, action.combine(!isCreative), automationType);
    }

    /**
     * {@inheritDoc}
     *
     * Note: We are only patching {@link #setStackSize(long, Action)}, as both {@link #growStack(long, Action)} and {@link #shrinkStack(long, Action)} are wrapped
     * through this method.
     */
    @Override
    public long setStackSize(long amount, Action action) {
        return super.setStackSize(amount, action.combine(!isCreative));
    }

    public static class GasTankRateLimitChemicalTank extends ChemicalTankRateLimitChemicalTank<Gas, GasStack> implements IGasHandler, IGasTank {

        public GasTankRateLimitChemicalTank(ChemicalTankTier tier, @Nullable IContentsListener listener) {
            super(tier, BasicGasTank.alwaysTrueBi, BasicGasTank.alwaysTrueBi, BasicGasTank.alwaysTrue, listener);
        }
    }

    public static class InfusionTankRateLimitChemicalTank extends ChemicalTankRateLimitChemicalTank<InfuseType, InfusionStack> implements IInfusionHandler, IInfusionTank {

        public InfusionTankRateLimitChemicalTank(ChemicalTankTier tier, @Nullable IContentsListener listener) {
            super(tier, BasicInfusionTank.alwaysTrueBi, BasicInfusionTank.alwaysTrueBi, BasicInfusionTank.alwaysTrue, listener);
        }
    }

    public static class PigmentTankRateLimitChemicalTank extends ChemicalTankRateLimitChemicalTank<Pigment, PigmentStack> implements IPigmentHandler, IPigmentTank {

        public PigmentTankRateLimitChemicalTank(ChemicalTankTier tier, @Nullable IContentsListener listener) {
            super(tier, BasicPigmentTank.alwaysTrueBi, BasicPigmentTank.alwaysTrueBi, BasicPigmentTank.alwaysTrue, listener);
        }
    }

    public static class SlurryTankRateLimitChemicalTank extends ChemicalTankRateLimitChemicalTank<Slurry, SlurryStack> implements ISlurryHandler, ISlurryTank {

        public SlurryTankRateLimitChemicalTank(ChemicalTankTier tier, @Nullable IContentsListener listener) {
            super(tier, BasicSlurryTank.alwaysTrueBi, BasicSlurryTank.alwaysTrueBi, BasicSlurryTank.alwaysTrue, listener);
        }
    }
}