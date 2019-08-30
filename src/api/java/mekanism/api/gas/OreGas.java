package mekanism.api.gas;

import mekanism.api.MekanismAPI;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;

public class OreGas extends Gas {

    private String oreLangKey;
    private OreGas cleanGas;

    public OreGas(ResourceLocation registryName, int tint, boolean isClean) {
        super(registryName, new ResourceLocation(MekanismAPI.MEKANISM_MODID, "block/liquid/liquid" + (isClean ? "_clean" : "") + "_ore"));
        oreLangKey = Util.makeTranslationKey("oregas", getRegistryName());
        setTint(tint);
        setVisible(false);
    }

    public OreGas(ResourceLocation registryName, int tint) {
        this(registryName, tint, true);
    }

    public OreGas(ResourceLocation registryName, int tint, OreGas clean) {
        this(registryName, tint, false);
        setCleanGas(clean);
    }

    public boolean isClean() {
        return getCleanGas() == null;
    }

    public OreGas getCleanGas() {
        return cleanGas;
    }

    public OreGas setCleanGas(OreGas gas) {
        cleanGas = gas;
        return this;
    }

    public String getOreTranslationKey() {
        return oreLangKey;
    }
}