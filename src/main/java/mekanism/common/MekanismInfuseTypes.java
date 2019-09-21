package mekanism.common;

import javax.annotation.Nonnull;
import mekanism.api.infuse.InfuseType;
import mekanism.api.providers.IInfuseTypeProvider;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;

//TODO: Decide if this should instead be
public enum MekanismInfuseTypes implements IInfuseTypeProvider {
    CARBON("carbon", new ResourceLocation(Mekanism.MODID, "infuse_type/carbon")),
    REDSTONE("redstone", new ResourceLocation(Mekanism.MODID, "infuse_type/redstone")),
    DIAMOND("diamond", new ResourceLocation(Mekanism.MODID, "infuse_type/diamond")),
    REFINED_OBSIDIAN("refined_obsidian", new ResourceLocation(Mekanism.MODID, "infuse_type/refined_obsidian")),
    TIN("tin", new ResourceLocation(Mekanism.MODID, "infuse_type/tin")),
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