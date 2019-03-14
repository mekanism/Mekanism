package mekanism.api.gas;

import net.minecraft.util.text.translation.I18n;

public class OreGas extends Gas {

    private String oreLangKey;
    private OreGas cleanGas;

    public OreGas(String s, String oreLangKey) {
        super(s, "mekanism:blocks/liquid/Liquid" + (s.contains("clean") ? "Clean" : "") + "Ore");
        this.oreLangKey = oreLangKey;
        setTint(0xf2cd67);//default old tint
    }

    public OreGas(String s, String oreLangKey, int tint, boolean isClean) {
        super(s, "mekanism:blocks/liquid/Liquid" + (isClean ? "Clean" : "") + "Ore");
        this.oreLangKey = oreLangKey;
        setTint(tint);
        setVisible(false);
    }

    public OreGas(String s, String oreLangKey, int tint) {
        this(s, oreLangKey, tint, true);
    }

    public OreGas(String s, String oreLangKey, int tint, OreGas clean) {
        this(s, oreLangKey, tint, false);
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

    public String getOreName() {
        return I18n.translateToLocal(oreLangKey);
    }
}