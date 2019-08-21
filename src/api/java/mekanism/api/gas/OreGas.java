package mekanism.api.gas;

public class OreGas extends Gas {

    private String oreLangKey;
    private OreGas cleanGas;

    public OreGas(String s, String oreLangKey, int tint, boolean isClean) {
        super(s, "mekanism:block/liquid/liquid" + (isClean ? "_clean" : "") + "_ore");
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

    public String getOreTranslationKey() {
        return oreLangKey;
    }
}