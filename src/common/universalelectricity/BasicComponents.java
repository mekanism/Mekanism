package universalelectricity;

import net.minecraft.src.Block;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import universalelectricity.ore.OreGenBase;
import universalelectricity.prefab.ItemElectric;

/**
 * The main class for managing Basic Component items and blocks. Reference objects from this class to add them to your
 * recipes and such.
 * @author Calclavia
 */

public class BasicComponents
{
    public static final String FILE_PATH = "/basiccomponents/textures/";
    public static final String BLOCK_TEXTURE_FILE = FILE_PATH + "blocks.png";
    public static final String ITEM_TEXTURE_FILE = FILE_PATH + "items.png";
    
    /**
     * Try not reference to these variable in pre-initialization as they might be null!
    */
    public static int BLOCK_ID_PREFIX = 3970;
    
    //Metadata ore block that contains copper
    public static Block blockBasicOre;
    public static Block blockCopperWire;
    public static Block oilMoving;
    public static Block oilStill;
    public static Block blockMachine;
    
    public static final int ITEM_ID_PREFIX = 13970;
    public static ItemElectric itemBattery;
    public static Item itemWrench;
    public static Item itemCopperIngot;
    public static Item itemTinIngot;
    public static Item itemSteelIngot;
    public static Item itemSteelDust;
    public static Item itemCircuit;
    public static Item itemBronzeIngot;
    public static Item itemBronzeDust;
    public static Item itemSteelPlate;
    public static Item itemBronzePlate;
    public static Item itemMotor;
    public static Item itemOilBucket;
    public static Item itemCopperPlate;
    public static Item itemTinPlate;
    
    /**
     * Some neat references to help you out.
     */
    public static ItemStack coalGenerator;
    public static ItemStack batteryBox;
    public static ItemStack electricFurnace;
    
    
    public static OreGenBase copperOreGeneration;
    public static OreGenBase tinOreGeneration;
}
