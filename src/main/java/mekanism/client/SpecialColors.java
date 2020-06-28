package mekanism.client;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import mekanism.client.render.lib.ColorAtlasLoader;
import mekanism.common.Mekanism;
import mekanism.common.lib.Color;
import net.minecraft.util.ResourceLocation;

public class SpecialColors {

    private static final List<ColorRegistryObject> colors = new ArrayList<>();

    public static Supplier<Color> ENERGY_CONFIG_TAB = register();
    public static Supplier<Color> FLUID_CONFIG_TAB = register();
    public static Supplier<Color> GAS_CONFIG_TAB = register();
    public static Supplier<Color> INFUSION_CONFIG_TAB = register();
    public static Supplier<Color> PIGMENT_CONFIG_TAB = register();
    public static Supplier<Color> SLURRY_CONFIG_TAB = register();
    public static Supplier<Color> ITEM_CONFIG_TAB = register();
    public static Supplier<Color> HEAT_CONFIG_TAB = register();

    public static Supplier<Color> REDSTONE_CONTROL_TAB = register();
    public static Supplier<Color> SECURITY_TAB = register();

    private static Supplier<Color> register() {
        ColorRegistryObject obj = new ColorRegistryObject();
        colors.add(obj);
        return obj;
    }

    public static void parse(ResourceLocation rl) {
        List<Color> parsed = ColorAtlasLoader.load(rl, colors.size());
        if (parsed.size() < colors.size()) {
            Mekanism.logger.error("Failed to parse special color atlas.");
            return;
        }
        for (int i = 0; i < parsed.size(); i++) {
            colors.get(i).setColor(parsed.get(i));
        }
    }

    private static class ColorRegistryObject implements Supplier<Color> {

        private Color color;

        private void setColor(Color color) {
            this.color = color;
        }

        @Override
        public Color get() {
            return color;
        }
    }
}
