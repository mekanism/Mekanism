package mekanism.generators.common;

import static mekanism.generators.common.block.states.BlockStateGenerator.GeneratorBlock.GENERATOR_BLOCK_1;
import static mekanism.generators.common.block.states.BlockStateReactor.ReactorBlock.REACTOR_BLOCK;
import static mekanism.generators.common.block.states.BlockStateReactor.ReactorBlock.REACTOR_GLASS;

import mekanism.generators.common.block.BlockGenerator;
import mekanism.generators.common.block.BlockReactor;
import mekanism.generators.common.item.ItemBlockGenerator;
import mekanism.generators.common.item.ItemBlockReactor;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder;
import net.minecraftforge.registries.IForgeRegistry;

@ObjectHolder(MekanismGenerators.MODID)
public class GeneratorsBlocks {

    public static final Block Generator = BlockGenerator.getGeneratorBlock(GENERATOR_BLOCK_1);
    public static final Block Reactor = BlockReactor.getReactorBlock(REACTOR_BLOCK);
    public static final Block ReactorGlass = BlockReactor.getReactorBlock(REACTOR_GLASS);

    public static void registerBlocks(IForgeRegistry<Block> registry) {
        registry.register(init(Generator, "Generator"));
        registry.register(init(Reactor, "Reactor"));
        registry.register(init(ReactorGlass, "ReactorGlass"));
    }

    public static void registerItemBlocks(IForgeRegistry<Item> registry) {
        registry.register(GeneratorsItems.init(new ItemBlockGenerator(Generator), "Generator"));
        registry.register(GeneratorsItems.init(new ItemBlockReactor(Reactor), "Reactor"));
        registry.register(GeneratorsItems.init(new ItemBlockReactor(ReactorGlass), "ReactorGlass"));
    }

    public static Block init(Block block, String name) {
        return block.setTranslationKey(name).setRegistryName(new ResourceLocation(MekanismGenerators.MODID, name));
    }
}
