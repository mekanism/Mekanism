package mekanism.api.gas;

import mekanism.api.MekanismAPI;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;

//TODO: Eventually make this be a "chemical" instead of a "gas"
public class Slurry extends Gas {

    private Slurry cleanGas;
    private String oreLangKey;

    public Slurry(ResourceLocation registryName, int tint, boolean isClean) {
        //TODO: Rename texture path
        super(registryName, new ResourceLocation(MekanismAPI.MEKANISM_MODID, "block/liquid/liquid" + (isClean ? "_clean" : "") + "_ore"));
        //TODO: The oreLangKey is really for the in parenthesis of what ore type it is
        oreLangKey = Util.makeTranslationKey("oregas", getRegistryName());
        setTint(tint);
        setVisible(false);
    }

    public Slurry(ResourceLocation registryName, int tint) {
        this(registryName, tint, true);
    }

    public Slurry(ResourceLocation registryName, int tint, Slurry clean) {
        this(registryName, tint, false);
        cleanGas = clean;
    }

    public boolean isDirty() {
        //TODO: Store the dirty slurry tag somewhere instead of getting it every isDirty call
        // Also use the tag instead of checking our cleanGas variable
        return cleanGas != null;//isIn(new GasTags.Wrapper(new ResourceLocation(MekanismAPI.MEKANISM_MODID, "dirty_slurry")));
    }

    public String getOreTranslationKey() {
        return oreLangKey;
    }

    public Slurry getCleanSlurry() {
        return cleanGas;
    }
}