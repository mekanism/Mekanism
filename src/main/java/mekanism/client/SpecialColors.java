package mekanism.client;

import mekanism.client.render.lib.ColorAtlas;
import mekanism.client.render.lib.ColorAtlas.ColorRegistryObject;

public class SpecialColors {

    public static final ColorAtlas GUI_OBJECTS = new ColorAtlas("gui_objects");
    public static final ColorAtlas GUI_TEXT = new ColorAtlas("gui_text");

    public static ColorRegistryObject TAB_ENERGY_CONFIG = GUI_OBJECTS.register();
    public static ColorRegistryObject TAB_FLUID_CONFIG = GUI_OBJECTS.register();
    public static ColorRegistryObject TAB_GAS_CONFIG = GUI_OBJECTS.register();
    public static ColorRegistryObject TAB_INFUSION_CONFIG = GUI_OBJECTS.register();
    public static ColorRegistryObject TAB_PIGMENT_CONFIG = GUI_OBJECTS.register();
    public static ColorRegistryObject TAB_SLURRY_CONFIG = GUI_OBJECTS.register();
    public static ColorRegistryObject TAB_ITEM_CONFIG = GUI_OBJECTS.register();
    public static ColorRegistryObject TAB_HEAT_CONFIG = GUI_OBJECTS.register();

    public static ColorRegistryObject TAB_REDSTONE_CONTROL = GUI_OBJECTS.register();
    public static ColorRegistryObject TAB_SECURITY = GUI_OBJECTS.register();
    public static ColorRegistryObject TAB_CONTAINER_EDIT_MODE = GUI_OBJECTS.register();
    public static ColorRegistryObject TAB_UPGRADE = GUI_OBJECTS.register();
    public static ColorRegistryObject TAB_CONFIGURATION = GUI_OBJECTS.register();
    public static ColorRegistryObject TAB_TRANSPORTER = GUI_OBJECTS.register();
    public static ColorRegistryObject TAB_DIGITAL_MINER_VISUAL = GUI_OBJECTS.register();
    public static ColorRegistryObject TAB_ROBIT_MENU = GUI_OBJECTS.register();
    public static ColorRegistryObject TAB_FACTORY_SORT = GUI_OBJECTS.register();
    public static ColorRegistryObject TAB_QIO_FREQUENCY = GUI_OBJECTS.register();
    public static ColorRegistryObject TAB_RESIZE_CONTROLS = GUI_OBJECTS.register();

    public static ColorRegistryObject TEXT_TITLE = GUI_TEXT.register();
    public static ColorRegistryObject TEXT_HEADING = GUI_TEXT.register();
    public static ColorRegistryObject TEXT_SUBHEADING = GUI_TEXT.register();
    public static ColorRegistryObject TEXT_SCREEN = GUI_TEXT.register();
}
