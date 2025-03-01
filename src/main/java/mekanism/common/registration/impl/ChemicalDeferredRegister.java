package mekanism.common.registration.impl;

import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import mekanism.api.MekanismAPI;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalBuilder;
import mekanism.common.base.IChemicalConstant;
import mekanism.common.registration.MekanismDeferredRegister;
import mekanism.common.resource.PrimaryResource;
import mekanism.common.util.ChemicalUtil;
import net.minecraft.resources.ResourceLocation;

@NothingNullByDefault//TODO - 1.20.2: Do we want to expose a basic form of this to the API
public class ChemicalDeferredRegister extends MekanismDeferredRegister<Chemical> {

    public ChemicalDeferredRegister(String modid) {
        super(MekanismAPI.CHEMICAL_REGISTRY_NAME, modid, DeferredChemical::new);
    }

    public DeferredChemical<Chemical> register(IChemicalConstant constants) {
        return register(constants.getName(), constants.getColor());
    }

    public DeferredChemical<Chemical> register(String name, int color) {
        return register(name, () -> new Chemical(ChemicalBuilder.builder().tint(color)));
    }

    public DeferredChemical<Chemical> registerPigment(String name, int color) {
        return register(name, () -> new Chemical(ChemicalBuilder.pigment().tint(color)));
    }

    public DeferredChemical<Chemical> registerInfuse(String name, int tint) {
        return register(name, () -> new Chemical(ChemicalBuilder.infuseType().tint(tint)));
    }

    public DeferredChemical<Chemical> register(String name, ResourceLocation texture, int barColor) {
        return register(name, () -> ChemicalUtil.chemical(ChemicalBuilder.builder(texture), barColor));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <CHEM extends Chemical> DeferredChemical<CHEM> register(String name, Supplier<? extends CHEM> sup) {
        return (DeferredChemical<CHEM>) super.register(name, sup);
    }

    public SlurryRegistryObject<Chemical, Chemical> registerSlurry(PrimaryResource resource) {
        return registerSlurry(resource.getRegistrySuffix(), builder -> builder.tint(resource.getTint()));
    }

    public SlurryRegistryObject<Chemical, Chemical> registerSlurry(String baseName, UnaryOperator<ChemicalBuilder> builderModifier) {
        return new SlurryRegistryObject<>(register("dirty_" + baseName, () -> new Chemical(builderModifier.apply(ChemicalBuilder.dirtySlurry()))),
              register("clean_" + baseName, () -> new Chemical(builderModifier.apply(ChemicalBuilder.cleanSlurry()))));
    }
}
