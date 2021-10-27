package mekanism.common.registries;

import java.util.EnumMap;
import java.util.Map;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.text.EnumColor;
import mekanism.common.Mekanism;
import mekanism.common.registration.impl.PigmentDeferredRegister;
import mekanism.common.registration.impl.PigmentRegistryObject;
import mekanism.common.util.EnumUtils;

public class MekanismPigments {

    private MekanismPigments() {
    }

    public static final PigmentDeferredRegister PIGMENTS = new PigmentDeferredRegister(Mekanism.MODID);

    public static Map<EnumColor, PigmentRegistryObject<Pigment>> PIGMENT_COLOR_LOOKUP = new EnumMap<>(EnumColor.class);

    static {
        for (EnumColor color : EnumUtils.COLORS) {
            PIGMENT_COLOR_LOOKUP.put(color, register(color));
        }
    }

    private static PigmentRegistryObject<Pigment> register(EnumColor color) {
        int[] rgb = color.getRgbCode();
        int tint = rgb[0] << 16;
        tint |= rgb[1] << 8;
        tint |= rgb[2];
        return PIGMENTS.register(color.getRegistryPrefix(), tint);
    }
}