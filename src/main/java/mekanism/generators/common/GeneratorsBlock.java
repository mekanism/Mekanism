package mekanism.generators.common;

import java.util.function.Function;
import javax.annotation.Nonnull;
import mekanism.common.Mekanism;
import mekanism.common.base.IBlockProvider;
import mekanism.common.item.IItemMekanism;
import mekanism.common.item.block.ItemBlockTooltip;
import mekanism.generators.common.block.BlockAdvancedSolarGenerator;
import mekanism.generators.common.block.BlockBioGenerator;
import mekanism.generators.common.block.turbine.BlockElectromagneticCoil;
import mekanism.generators.common.block.BlockGasBurningGenerator;
import mekanism.generators.common.block.BlockHeatGenerator;
import mekanism.generators.common.block.turbine.BlockRotationalComplex;
import mekanism.generators.common.block.turbine.BlockSaturatingCondenser;
import mekanism.generators.common.block.BlockSolarGenerator;
import mekanism.generators.common.block.turbine.BlockTurbineCasing;
import mekanism.generators.common.block.turbine.BlockTurbineRotor;
import mekanism.generators.common.block.turbine.BlockTurbineValve;
import mekanism.generators.common.block.turbine.BlockTurbineVent;
import mekanism.generators.common.block.BlockWindGenerator;
import mekanism.generators.common.block.reactor.BlockLaserFocusMatrix;
import mekanism.generators.common.block.reactor.BlockReactorController;
import mekanism.generators.common.block.reactor.BlockReactorFrame;
import mekanism.generators.common.block.reactor.BlockReactorGlass;
import mekanism.generators.common.block.reactor.BlockReactorLogicAdapter;
import mekanism.generators.common.block.reactor.BlockReactorPort;
import mekanism.generators.common.item.generator.ItemBlockAdvancedSolarGenerator;
import mekanism.generators.common.item.generator.ItemBlockBioGenerator;
import mekanism.generators.common.item.generator.ItemBlockGasBurningGenerator;
import mekanism.generators.common.item.generator.ItemBlockHeatGenerator;
import mekanism.generators.common.item.generator.ItemBlockSolarGenerator;
import mekanism.generators.common.item.generator.ItemBlockTurbineCasing;
import mekanism.generators.common.item.generator.ItemBlockTurbineValve;
import mekanism.generators.common.item.generator.ItemBlockTurbineVent;
import mekanism.generators.common.item.generator.ItemBlockWindGenerator;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.registries.IForgeRegistry;

public enum GeneratorsBlock implements IBlockProvider {
    HEAT_GENERATOR(new BlockHeatGenerator(), ItemBlockHeatGenerator::new),
    SOLAR_GENERATOR(new BlockSolarGenerator(), ItemBlockSolarGenerator::new),
    GAS_BURNING_GENERATOR(new BlockGasBurningGenerator(), ItemBlockGasBurningGenerator::new),
    BIO_GENERATOR(new BlockBioGenerator(), ItemBlockBioGenerator::new),
    ADVANCED_SOLAR_GENERATOR(new BlockAdvancedSolarGenerator(), ItemBlockAdvancedSolarGenerator::new),
    WIND_GENERATOR(new BlockWindGenerator(), ItemBlockWindGenerator::new),
    TURBINE_ROTOR(new BlockTurbineRotor(), ItemBlockTooltip::new),
    ROTATIONAL_COMPLEX(new BlockRotationalComplex(), ItemBlockTooltip::new),
    ELECTROMAGNETIC_COIL(new BlockElectromagneticCoil(), ItemBlockTooltip::new),
    TURBINE_CASING(new BlockTurbineCasing(), ItemBlockTurbineCasing::new),
    TURBINE_VALVE(new BlockTurbineValve(), ItemBlockTurbineValve::new),
    TURBINE_VENT(new BlockTurbineVent(), ItemBlockTurbineVent::new),
    SATURATING_CONDENSER(new BlockSaturatingCondenser(), ItemBlockTooltip::new),
    REACTOR_CONTROLLER(new BlockReactorController(), ItemBlockTooltip::new),
    REACTOR_FRAME(new BlockReactorFrame(), ItemBlockTooltip::new),
    REACTOR_PORT(new BlockReactorPort(), ItemBlockTooltip::new),
    REACTOR_LOGIC_ADAPTER(new BlockReactorLogicAdapter(), ItemBlockTooltip::new),
    REACTOR_GLASS(new BlockReactorGlass(), ItemBlockTooltip::new),
    LASER_FOCUS_MATRIX(new BlockLaserFocusMatrix(), ItemBlockTooltip::new);

    private final ItemBlock item;
    private final Block block;

    <ITEM extends ItemBlock & IItemMekanism, BLOCK extends Block> GeneratorsBlock(BLOCK block, Function<BLOCK, ITEM> itemCreator) {
        this.block = block;
        this.item = itemCreator.apply(block);
    }

    @Nonnull
    @Override
    public Block getBlock() {
        return block;
    }

    @Nonnull
    @Override
    public ItemBlock getItem() {
        return item;
    }

    public static void registerBlocks(IForgeRegistry<Block> registry) {
        for (GeneratorsBlock generatorsBlock : values()) {
            Block block = generatorsBlock.getBlock();
            block.setCreativeTab(Mekanism.tabMekanism);
            block.setTranslationKey("mekanism." + generatorsBlock.getName());
            registry.register(block);
        }
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