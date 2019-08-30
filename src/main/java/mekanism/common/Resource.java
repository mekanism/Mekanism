package mekanism.common;

import java.util.Locale;
import mekanism.common.resource.INamedResource;

public enum Resource implements INamedResource {
    IRON("iron", 0xAF8E77),
    GOLD("gold", 0xF2CD67),
    OSMIUM("osmium", 0x1E79C3),
    COPPER("copper", 0xAA4B19),
    TIN("tin", 0xCCCCD9),
    SILVER("silver", 0xBFC9CD),
    LEAD("lead", 0x3D3D41);

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