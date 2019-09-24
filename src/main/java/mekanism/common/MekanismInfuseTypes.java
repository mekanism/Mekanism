package mekanism.common;

import javax.annotation.Nonnull;
import mekanism.api.infuse.InfuseType;
import mekanism.api.providers.IInfuseTypeProvider;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;

public enum MekanismInfuseTypes implements IInfuseTypeProvider {
    CARBON("carbon", 0x404040),
    REDSTONE("redstone", 0xB30505),
    DIAMOND("diamond", 0x6CEDD8),
    REFINED_OBSIDIAN("refined_obsidian", 0x7C00ED),
    TIN("tin", 0xCCCCD9),
    FUNGI("fungi", new ResourceLocation(Mekanism.MODID, "infuse_type/fungi")),
    BIO("bio", new ResourceLocation(Mekanism.MODID, "infuse_type/bio"));

    private final InfuseType infuseType;

    MekanismInfuseTypes(String name, int tint) {
        infuseType = new InfuseType(new ResourceLocation(Mekanism.MODID, name), tint);
    }

    MekanismInfuseTypes(String name, ResourceLocation texture) {
        infuseType = new InfuseType(new ResourceLocation(Mekanism.MODID, name), texture);
    }

    @Nonnull
    @Override
    public InfuseType getInfuseType() {
        return infuseType;
    }

    public static void register(IForgeRegistry<InfuseType> registry) {
        for (IInfuseTypeProvider gasProvider : values()) {
            registry.register(gasProvider.getInfuseType());
        }
    }
}