package mekanism.common;

import java.util.Locale;
import mekanism.common.resource.INamedResource;

public enum Resource implements INamedResource {
    IRON("iron", 0xaf8e77),
    GOLD("gold", 0xf2cd67),
    OSMIUM("osmium", 0x1e79c3),
    COPPER("copper", 0xaa4b19),
    TIN("tin", 0xccccd9),
    SILVER("silver", 0xbfc9cd),
    LEAD("lead", 0x3d3d41);

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

    @Override
    public String getRegistrySuffix() {
        return name;
    }
}