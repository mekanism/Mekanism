package mekanism.common.registration.impl;

import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.providers.IChemicalProvider;
import mekanism.api.providers.IGasProvider;
import mekanism.api.providers.IInfuseTypeProvider;
import mekanism.api.providers.IPigmentProvider;
import mekanism.api.providers.ISlurryProvider;
import mekanism.common.registration.MekanismDeferredHolder;
import net.minecraft.resources.ResourceKey;
import org.jetbrains.annotations.NotNull;

public abstract class DeferredChemical<CHEMICAL extends Chemical<CHEMICAL>, TYPE extends CHEMICAL> extends MekanismDeferredHolder<CHEMICAL, TYPE>
      implements IChemicalProvider<CHEMICAL> {

    protected DeferredChemical(ResourceKey<CHEMICAL> key) {
        super(key);
    }

    @NotNull
    @Override
    public CHEMICAL getChemical() {
        return value();
    }

    public static class DeferredGas<GAS extends Gas> extends DeferredChemical<Gas, GAS> implements IGasProvider {

        public DeferredGas(ResourceKey<Gas> key) {
            super(key);
        }
    }

    public static class DeferredInfuseType<INFUSE_TYPE extends InfuseType> extends DeferredChemical<InfuseType, INFUSE_TYPE> implements IInfuseTypeProvider {

        public DeferredInfuseType(ResourceKey<InfuseType> key) {
            super(key);
        }
    }

    public static class DeferredPigment<PIGMENT extends Pigment> extends DeferredChemical<Pigment, PIGMENT> implements IPigmentProvider {

        public DeferredPigment(ResourceKey<Pigment> key) {
            super(key);
        }
    }

    public static class DeferredSlurry<SLURRY extends Slurry> extends DeferredChemical<Slurry, SLURRY> implements ISlurryProvider {

        public DeferredSlurry(ResourceKey<Slurry> key) {
            super(key);
        }
    }
}