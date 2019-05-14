package mekanism.common;

import java.util.Locale;
import javax.annotation.Nullable;

public enum Resource {
    IRON("Iron", 0xaf8e77),
    GOLD("Gold", 0xf2cd67),
    OSMIUM("Osmium", 0x1e79c3),
    COPPER("Copper", 0xaa4b19),
    TIN("Tin", 0xccccd9),
    SILVER("Silver", 0xbfc9cd),
    LEAD("Lead", 0x3d3d41);

    public final int tint;
    private String name;

    Resource(String s, int t) {
        name = s;
        tint = t;
    }

    @Nullable
    public static Resource get(int index) {
        if (index < 0 || index >= values().length) {
            return null;
        }
        return values()[index];
    }

    public static Resource getFromName(String s) {
        s = s.toLowerCase(Locale.ROOT);
        for (Resource r : values()) {
            if (r.name.toLowerCase(Locale.ROOT).equals(s)) {
                return r;
            }
        }
        return null;
    }

    public String getName() {
        return name;
    }
}