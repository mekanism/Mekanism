package mekanism.generators.common;

import java.util.function.Function;
import mekanism.common.block.PortalHelper;
import mekanism.common.item.IItemMekanism;
import mekanism.common.item.ItemBlockMekanism;
import mekanism.generators.common.block.generator.BlockAdvancedSolarGenerator;
import mekanism.generators.common.block.generator.BlockBioGenerator;
import mekanism.generators.common.block.generator.BlockGasBurningGenerator;
import mekanism.generators.common.block.generator.BlockHeatGenerator;
import mekanism.generators.common.block.generator.BlockSolarGenerator;
import mekanism.generators.common.block.generator.BlockWindGenerator;
import mekanism.generators.common.item.ItemBlockGenerator;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraftforge.registries.IForgeRegistry;

public enum GeneratorsBlock {
    HEAT_GENERATOR(new BlockHeatGenerator(), ItemBlockGenerator::new),
    SOLAR_GENERATOR(new BlockSolarGenerator(), ItemBlockGenerator::new),
    GAS_BURNING_GENERATOR(new BlockGasBurningGenerator(), ItemBlockGenerator::new),
    BIO_GENERATOR(new BlockBioGenerator(), ItemBlockGenerator::new),
    ADVANCED_SOLAR_GENERATOR(new BlockAdvancedSolarGenerator(), ItemBlockGenerator::new),
    WIND_GENERATOR(new BlockWindGenerator(), ItemBlockGenerator::new),
    TURBINE_ROTOR(),
    ROTATIONAL_COMPLEX(),
    ELECTROMAGNETIC_COIL(),
    TURBINE_CASING(),
    TURBINE_VALVE(),
    TURBINE_VENT(),
    SATURATING_CONDENSER(),
    REACTOR_CONTROLLER(),
    REACTOR_FRAME(),
    REACTOR_PORT(),
    REACTOR_LOGIC_ADAPTER(),
    REACTOR_GLASS(),
    LASER_FOCUS_MATRIX();

    private final ItemBlock item;
    private final Block block;

    GeneratorsBlock(Block block) {
        this(block, ItemBlockMekanism::new);
    }

    <ITEM extends ItemBlock & IItemMekanism, BLOCK extends Block> GeneratorsBlock(BLOCK block, Function<BLOCK, ITEM> itemCreator) {
        this.block = block;
        this.item = itemCreator.apply(block);
        //TODO: Fix all translation keys so that they have mekanism in them
    }

    public Block getBlock() {
        return block;
    }

    public ItemBlock getItem() {
        return item;
    }

    public ItemStack getItemStack() {
        return getItemStack(1);
    }

    public ItemStack getItemStack(int size) {
        return new ItemStack(getItem(), size);
    }

    public static void registerBlocks(IForgeRegistry<Block> registry) {
        for (GeneratorsBlock generatorsBlock : values()) {
            registry.register(generatorsBlock.getBlock());
        }
        registry.register(PortalHelper.BlockPortalOverride.instance);
    }

    public static void registerItemBlocks(IForgeRegistry<Item> registry) {
        for (GeneratorsBlock generatorsBlock : values()) {
            Item item = generatorsBlock.getItem();
            registry.register(item);
            if (item instanceof IItemMekanism) {
                ((IItemMekanism) item).registerOreDict();
            }
        }
    }
}