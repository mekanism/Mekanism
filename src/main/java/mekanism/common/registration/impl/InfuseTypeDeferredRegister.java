package mekanism.common.registration.impl;

import java.util.function.Supplier;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfuseTypeBuilder;
import mekanism.common.registration.WrappedDeferredRegister;
import mekanism.common.util.ChemicalUtil;
import net.minecraft.resources.ResourceLocation;

public class InfuseTypeDeferredRegister extends WrappedDeferredRegister<InfuseType> {

    public InfuseTypeDeferredRegister(String modid) {
        super(modid, MekanismAPI.INFUSE_TYPE_REGISTRY_NAME);
    }

    public InfuseTypeRegistryObject<InfuseType> register(String name, int tint) {
        return register(name, () -> new InfuseType(InfuseTypeBuilder.builder().tint(tint)));
    }

    public InfuseTypeRegistryObject<InfuseType> register(String name, ResourceLocation texture, int barColor) {
        return register(name, () -> ChemicalUtil.infuseType(InfuseTypeBuilder.builder(texture), barColor));
    }

    public <INFUSE_TYPE extends InfuseType> InfuseTypeRegistryObject<INFUSE_TYPE> register(String name, Supplier<? extends INFUSE_TYPE> sup) {
        return register(name, sup, InfuseTypeRegistryObject::new);
    }
}