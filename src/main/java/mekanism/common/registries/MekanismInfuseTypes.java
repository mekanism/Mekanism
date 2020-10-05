package mekanism.common.registries;

import mekanism.api.chemical.infuse.InfuseType;
import mekanism.common.Mekanism;
import mekanism.common.registration.impl.InfuseTypeDeferredRegister;
import mekanism.common.registration.impl.InfuseTypeRegistryObject;

public class MekanismInfuseTypes {

    private MekanismInfuseTypes() {
    }

    public static final InfuseTypeDeferredRegister INFUSE_TYPES = new InfuseTypeDeferredRegister(Mekanism.MODID);

    public static final InfuseTypeRegistryObject<InfuseType> CARBON = INFUSE_TYPES.register("carbon", 0x404040);
    public static final InfuseTypeRegistryObject<InfuseType> REDSTONE = INFUSE_TYPES.register("redstone", 0xB30505);
    public static final InfuseTypeRegistryObject<InfuseType> DIAMOND = INFUSE_TYPES.register("diamond", 0x6CEDD8);
    public static final InfuseTypeRegistryObject<InfuseType> REFINED_OBSIDIAN = INFUSE_TYPES.register("refined_obsidian", 0x7C00ED);
    public static final InfuseTypeRegistryObject<InfuseType> GOLD = INFUSE_TYPES.register("gold", 0xF2CD67);
    public static final InfuseTypeRegistryObject<InfuseType> TIN = INFUSE_TYPES.register("tin", 0xCCCCD9);
    public static final InfuseTypeRegistryObject<InfuseType> FUNGI = INFUSE_TYPES.register("fungi", Mekanism.rl("infuse_type/fungi"));
    public static final InfuseTypeRegistryObject<InfuseType> BIO = INFUSE_TYPES.register("bio", Mekanism.rl("infuse_type/bio"));
}