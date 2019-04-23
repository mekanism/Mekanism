package mekanism.common;

import java.util.Locale;

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

    public static Resource getFromName(String s) {
        for (Resource r : values()) {
            if (r.name.toLowerCase(Locale.ROOT).equals(s.toLowerCase(Locale.ROOT))) {
                return r;
            }
        }

        return null;
    }

    public String getName() {
        return name;
    }
}
