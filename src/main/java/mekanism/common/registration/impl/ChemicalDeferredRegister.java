package mekanism.common.registration.impl;

import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalBuilder;
import mekanism.api.chemical.CleanDirtySlurryId;
import mekanism.common.base.IChemicalConstant;
import mekanism.common.registration.MekanismDeferredRegister;
import mekanism.common.resource.PrimaryResource;
import mekanism.common.util.ChemicalUtils;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

//TODO - 1.20.2: Do we want to expose a basic form of this to the API
public class ChemicalDeferredRegister extends MekanismDeferredRegister<Chemical> {

    public ChemicalDeferredRegister(String modid) {
        super(MekanismAPI.CHEMICAL_REGISTRY_NAME, modid, DeferredChemical::new);
    }

    public ResourceKey<Chemical> register(IChemicalConstant constants) {
        return register(constants.getName(), constants.getColor());
    }

    public ResourceKey<Chemical> register(String name, int color) {
        return register(name, () -> new Chemical(ChemicalBuilder.builder().tint(color))).getKey();
    }

    public ResourceKey<Chemical> registerPigment(String name, int color) {
        return register(name, () -> new Chemical(ChemicalBuilder.pigment().tint(color))).getKey();
    }

    public ResourceKey<Chemical> registerInfuse(String name, int tint) {
        return register(name, () -> new Chemical(ChemicalBuilder.infuseType().tint(tint))).getKey();
    }

    public ResourceKey<Chemical> register(String name, Identifier texture, int barColor) {
        return register(name, () -> ChemicalUtils.chemical(ChemicalBuilder.builder(texture), barColor)).getKey();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <CHEM extends Chemical> DeferredChemical<CHEM> register(String name, Supplier<? extends CHEM> sup) {
        return (DeferredChemical<CHEM>) super.register(name, sup);
    }

    public CleanDirtySlurryId registerSlurry(PrimaryResource resource) {
        return registerSlurry(resource.getRegistrySuffix(), builder -> builder.tint(resource.getTint()));
    }

    public CleanDirtySlurryId registerSlurry(String baseName, UnaryOperator<ChemicalBuilder> builderModifier) {
        return new CleanDirtySlurryId(register("dirty_" + baseName, () -> new Chemical(builderModifier.apply(ChemicalBuilder.dirtySlurry()))).getKey(),
              register("clean_" + baseName, () -> new Chemical(builderModifier.apply(ChemicalBuilder.cleanSlurry()))).getKey());
    }
}
