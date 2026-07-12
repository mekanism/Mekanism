package mekanism.common.integration.lookingat;

import mekanism.common.Mekanism;
import net.minecraft.resources.Identifier;

public class LookingAtConstants {

    private LookingAtConstants() {
    }

    public static class Jade {

        private Jade() {
        }

        public static final Identifier REMOVE_BUILTIN = Mekanism.rl("remove_builtin");
        public static final Identifier BLOCK_DATA = Mekanism.rl("data_provider");
        public static final Identifier ENTITY_DATA = Mekanism.rl("entity_data_provider");
        public static final Identifier TOOLTIP_RENDERER = Mekanism.rl("tooltip_renderer");
    }
}