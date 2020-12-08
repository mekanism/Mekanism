package mekanism.client;

import mekanism.client.render.lib.ColorAtlas;
import mekanism.client.render.lib.ColorAtlas.ColorRegistryObject;

public class SpecialColors {

    private SpecialColors() {
    }

    public static final ColorAtlas GUI_OBJECTS = new ColorAtlas("gui_objects");
    public static final ColorAtlas GUI_TEXT = new ColorAtlas("gui_text");

    public static final ColorRegistryObject TAB_ENERGY_CONFIG = GUI_OBJECTS.register();
    public static final ColorRegistryObject TAB_FLUID_CONFIG = GUI_OBJECTS.register();
    public static final ColorRegistryObject TAB_GAS_CONFIG = GUI_OBJECTS.register();
    public static final ColorRegistryObject TAB_INFUSION_CONFIG = GUI_OBJECTS.register();
    public static final ColorRegistryObject TAB_PIGMENT_CONFIG = GUI_OBJECTS.register();
    public static final ColorRegistryObject TAB_SLURRY_CONFIG = GUI_OBJECTS.register();
    public static final ColorRegistryObject TAB_ITEM_CONFIG = GUI_OBJECTS.register();
    public static final ColorRegistryObject TAB_HEAT_CONFIG = GUI_OBJECTS.register();

    public static final ColorRegistryObject TAB_REDSTONE_CONTROL = GUI_OBJECTS.register();
    public static final ColorRegistryObject TAB_SECURITY = GUI_OBJECTS.register();
    public static final ColorRegistryObject TAB_CONTAINER_EDIT_MODE = GUI_OBJECTS.register();
    public static final ColorRegistryObject TAB_UPGRADE = GUI_OBJECTS.register();
    public static final ColorRegistryObject TAB_CONFIGURATION = GUI_OBJECTS.register();
    public static final ColorRegistryObject TAB_TRANSPORTER = GUI_OBJECTS.register();
    public static final ColorRegistryObject TAB_DIGITAL_MINER_VISUAL = GUI_OBJECTS.register();
    public static final ColorRegistryObject TAB_ROBIT_MENU = GUI_OBJECTS.register();
    public static final ColorRegistryObject TAB_FACTORY_SORT = GUI_OBJECTS.register();
    public static final ColorRegistryObject TAB_QIO_FREQUENCY = GUI_OBJECTS.register();
    public static final ColorRegistryObject TAB_RESIZE_CONTROLS = GUI_OBJECTS.register();
    public static final ColorRegistryObject TAB_LASER_AMPLIFIER = GUI_OBJECTS.register();
    public static final ColorRegistryObject TAB_CHEMICAL_WASHER = GUI_OBJECTS.register();
    public static final ColorRegistryObject TAB_MULTIBLOCK_MAIN = GUI_OBJECTS.register();
    public static final ColorRegistryObject TAB_MULTIBLOCK_STATS = GUI_OBJECTS.register();
    public static final ColorRegistryObject TAB_CRAFTING_WINDOW = GUI_OBJECTS.register();

    public static final ColorRegistryObject TEXT_TITLE = GUI_TEXT.register();
    public static final ColorRegistryObject TEXT_HEADING = GUI_TEXT.register();
    public static final ColorRegistryObject TEXT_SUBHEADING = GUI_TEXT.register();
    public static final ColorRegistryObject TEXT_SCREEN = GUI_TEXT.register();
}
