package mekanism.client;

import mekanism.client.render.lib.ColorAtlas;
import mekanism.client.render.lib.ColorAtlas.ColorRegistryObject;

public class SpecialColors {

    private SpecialColors() {
    }

    public static final ColorAtlas GUI_OBJECTS = new ColorAtlas("gui_objects");
    public static final ColorAtlas GUI_TEXT = new ColorAtlas("gui_text");

    public static final ColorRegistryObject TAB_ENERGY_CONFIG = GUI_OBJECTS.register(0xFF59C15F);
    public static final ColorRegistryObject TAB_FLUID_CONFIG = GUI_OBJECTS.register(0xFF366BD0);
    public static final ColorRegistryObject TAB_CHEMICAL_CONFIG = GUI_OBJECTS.register(0xFFFFDD4F);
    public static final ColorRegistryObject TAB_ITEM_CONFIG = GUI_OBJECTS.register(0xFFCFCFCF);
    public static final ColorRegistryObject TAB_HEAT_CONFIG = GUI_OBJECTS.register(0xFFFFA160);

    public static final ColorRegistryObject TAB_REDSTONE_CONTROL = GUI_OBJECTS.register(0xFFC9071F);
    public static final ColorRegistryObject TAB_SECURITY = GUI_OBJECTS.register(0xFFFFA160);
    public static final ColorRegistryObject TAB_CONTAINER_EDIT_MODE = GUI_OBJECTS.register(0xFF366BD0);
    public static final ColorRegistryObject TAB_UPGRADE = GUI_OBJECTS.register();
    public static final ColorRegistryObject TAB_CONFIGURATION = GUI_OBJECTS.register();
    public static final ColorRegistryObject TAB_TRANSPORTER = GUI_OBJECTS.register();
    public static final ColorRegistryObject TAB_VISUALS = GUI_OBJECTS.register();
    public static final ColorRegistryObject TAB_ROBIT_MENU = GUI_OBJECTS.register();
    public static final ColorRegistryObject TAB_FACTORY_SORT = GUI_OBJECTS.register();
    public static final ColorRegistryObject TAB_QIO_FREQUENCY = GUI_OBJECTS.register();
    public static final ColorRegistryObject TAB_RESIZE_CONTROLS = GUI_OBJECTS.register();
    public static final ColorRegistryObject TAB_LASER_AMPLIFIER = GUI_OBJECTS.register();
    public static final ColorRegistryObject TAB_CHEMICAL_WASHER = GUI_OBJECTS.register();
    public static final ColorRegistryObject TAB_MULTIBLOCK_MAIN = GUI_OBJECTS.register();
    public static final ColorRegistryObject TAB_MULTIBLOCK_STATS = GUI_OBJECTS.register();
    public static final ColorRegistryObject TAB_CRAFTING_WINDOW = GUI_OBJECTS.register();
    public static final ColorRegistryObject TAB_ARMOR_SLOTS = GUI_OBJECTS.register();
    public static final ColorRegistryObject TAB_TARGET_DIRECTION = GUI_OBJECTS.register();
    public static final ColorRegistryObject TAB_JEI_REJECTS_TARGET = GUI_OBJECTS.register();

    public static final ColorRegistryObject TEXT_TITLE = GUI_TEXT.register(0xFF404040);
    public static final ColorRegistryObject TEXT_HEADING = GUI_TEXT.register(0xFF202020);
    public static final ColorRegistryObject TEXT_SUBHEADING = GUI_TEXT.register(0xFF787878);
    public static final ColorRegistryObject TEXT_SCREEN = GUI_TEXT.register(0xFF3CFE9A);
    public static final ColorRegistryObject TEXT_ACTIVE_BUTTON = GUI_TEXT.register(0xFFFFFFFF);
    public static final ColorRegistryObject TEXT_INACTIVE_BUTTON = GUI_TEXT.register(0xFFA0A0A0);
}