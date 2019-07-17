package mekanism.common;

import java.util.Locale;
import mekanism.common.resource.INamedResource;

public enum Resource implements INamedResource {
    IRON("Iron", 0xaf8e77),
    GOLD("Gold", 0xf2cd67),
    OSMIUM("Osmium", 0x1e79c3),
    COPPER("Copper", 0xaa4b19),
    TIN("Tin", 0xccccd9),
    SILVER("Silver", 0xbfc9cd),
    LEAD("Lead", 0x3d3d41);

    public final int tint;
    private final String name;

    Resource(String name, int tint) {
        this.name = name;
        this.tint = tint;
    }

    public static Resource getFromName(String s) {
        s = s.toLowerCase(Locale.ROOT);
        for (Resource r : values()) {
            if (r.getRegistrySuffix().equals(s)) {
                return r;
            }
        }
        return null;
    }

    public String getName() {
        return name;
    }

    @Override
    public String getRegistrySuffix() {
        return name.toLowerCase(Locale.ROOT);
    }

    @Override
    public String getOreSuffix() {
        return name;
    }
}