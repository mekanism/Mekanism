package mekanism.common.registration.impl;

import java.util.function.Supplier;
import mekanism.api.MekanismAPI;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfuseTypeBuilder;
import mekanism.common.registration.MekanismDeferredRegister;
import mekanism.common.registration.impl.DeferredChemical.DeferredInfuseType;
import mekanism.common.util.ChemicalUtil;
import net.minecraft.resources.ResourceLocation;

@NothingNullByDefault
public class InfuseTypeDeferredRegister extends MekanismDeferredRegister<InfuseType> {

    public InfuseTypeDeferredRegister(String modid) {
        super(MekanismAPI.INFUSE_TYPE_REGISTRY_NAME, modid, DeferredInfuseType::new);
    }

    public DeferredInfuseType<InfuseType> register(String name, int tint) {
        return register(name, () -> new InfuseType(InfuseTypeBuilder.builder().tint(tint)));
    }

    public DeferredInfuseType<InfuseType> register(String name, ResourceLocation texture, int barColor) {
        return register(name, () -> ChemicalUtil.infuseType(InfuseTypeBuilder.builder(texture), barColor));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <INFUSE_TYPE extends InfuseType> DeferredInfuseType<INFUSE_TYPE> register(String name, Supplier<? extends INFUSE_TYPE> sup) {
        return (DeferredInfuseType<INFUSE_TYPE>) super.register(name, sup);
    }
}