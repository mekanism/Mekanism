package mekanism.common;

//@ObjectHolder(Mekanism.MODID)
public class MekanismBlocks {

    /*public static Block BasicBlock = BlockBasic.getBlockBasic(BASIC_BLOCK_1);
    public static Block BasicBlock2 = BlockBasic.getBlockBasic(BASIC_BLOCK_2);
    public static Block MachineBlock = BlockMachine.getBlockMachine(MACHINE_BLOCK_1);
    public static Block MachineBlock2 = BlockMachine.getBlockMachine(MACHINE_BLOCK_2);
    public static Block MachineBlock3 = BlockMachine.getBlockMachine(MACHINE_BLOCK_3);
    public static Block OreBlock = new BlockOre();
    public static Block ObsidianTNT = new BlockObsidianTNT();
    public static Block EnergyCube = new BlockEnergyCube();
    public static Block Transmitter = new BlockTransmitter();
    public static Block BoundingBlock = new BlockBounding();
    public static Block GasTank = new BlockGasTank();
    public static Block CardboardBox = new BlockCardboardBox();
    public static Block GlowPanel = new BlockGlowPanel();
    public static Block PlasticBlock = new BlockPlastic(PlasticBlockType.PLASTIC);
    public static Block SlickPlasticBlock = new BlockPlastic(PlasticBlockType.SLICK);
    public static Block GlowPlasticBlock = new BlockPlastic(PlasticBlockType.GLOW);
    public static Block ReinforcedPlasticBlock = new BlockPlastic(PlasticBlockType.REINFORCED);
    public static Block RoadPlasticBlock = new BlockPlastic(PlasticBlockType.ROAD);
    public static Block PlasticFence = new BlockPlasticFence();
    public static Block SaltBlock = new BlockSalt();*/

    /**
     * Adds and registers all blocks.
     *
     * @param registry IForgeRegistry for blocks.
     */
    /*public static void registerBlocks(IForgeRegistry<Block> registry) {
        registry.register(init(BasicBlock, "BasicBlock"));
        registry.register(init(BasicBlock2, "BasicBlock2"));
        registry.register(init(MachineBlock, "MachineBlock"));
        registry.register(init(MachineBlock2, "MachineBlock2"));
        registry.register(init(MachineBlock3, "MachineBlock3"));
        registry.register(init(OreBlock, "OreBlock"));
        registry.register(init(EnergyCube, "EnergyCube"));
        registry.register(init(Transmitter, "Transmitter"));
        registry.register(init(ObsidianTNT, "ObsidianTNT"));
        registry.register(init(BoundingBlock, "BoundingBlock"));
        registry.register(init(GasTank, "GasTank"));
        registry.register(init(CardboardBox, "CardboardBox"));
        registry.register(init(GlowPanel, "GlowPanel"));
        registry.register(init(PlasticBlock, "PlasticBlock"));
        registry.register(init(SlickPlasticBlock, "SlickPlasticBlock"));
        registry.register(init(GlowPlasticBlock, "GlowPlasticBlock"));
        registry.register(init(ReinforcedPlasticBlock, "ReinforcedPlasticBlock"));
        registry.register(init(RoadPlasticBlock, "RoadPlasticBlock"));
        registry.register(init(PlasticFence, "PlasticFence"));
        registry.register(init(SaltBlock, "SaltBlock"));
        registry.register(BlockBasic.BlockPortalOverride.instance);
    }*/

    /**
     * Adds and registers all itemBlocks.
     *
     * @param registry IForgeRegistry for items.
     */
    /*public static void registerItemBlocks(IForgeRegistry<Item> registry) {
        registry.register(MekanismItems.init(new ItemBlockBasic(BasicBlock), "BasicBlock"));
        registry.register(MekanismItems.init(new ItemBlockBasic(BasicBlock2), "BasicBlock2"));
        registry.register(MekanismItems.init(new ItemBlockMachine(MachineBlock), "MachineBlock"));
        registry.register(MekanismItems.init(new ItemBlockMachine(MachineBlock2), "MachineBlock2"));
        registry.register(MekanismItems.init(new ItemBlockMachine(MachineBlock3), "MachineBlock3"));
        registry.register(MekanismItems.init(new ItemBlockOre(OreBlock), "OreBlock"));
        registry.register(MekanismItems.init(new ItemBlockEnergyCube(EnergyCube), "EnergyCube"));
        registry.register(MekanismItems.init(new ItemBlockTransmitter(Transmitter), "Transmitter"));
        registry.register(MekanismItems.init(new ItemBlock(ObsidianTNT), "ObsidianTNT"));
        registry.register(MekanismItems.init(new ItemBlock(BoundingBlock), "BoundingBlock"));
        registry.register(MekanismItems.init(new ItemBlockGasTank(GasTank), "GasTank"));
        registry.register(MekanismItems.init(new ItemBlockCardboardBox(CardboardBox), "CardboardBox"));
        registry.register(MekanismItems.init(new ItemBlockGlowPanel(GlowPanel), "GlowPanel"));
        registry.register(MekanismItems.init(new ItemBlockPlastic(PlasticBlock), "PlasticBlock"));
        registry.register(MekanismItems.init(new ItemBlockPlastic(SlickPlasticBlock), "SlickPlasticBlock"));
        registry.register(MekanismItems.init(new ItemBlockPlastic(GlowPlasticBlock), "GlowPlasticBlock"));
        registry.register(MekanismItems.init(new ItemBlockPlastic(ReinforcedPlasticBlock), "ReinforcedPlasticBlock"));
        registry.register(MekanismItems.init(new ItemBlockPlastic(RoadPlasticBlock), "RoadPlasticBlock"));
        registry.register(MekanismItems.init(new ItemBlockPlastic(PlasticFence), "PlasticFence"));
        registry.register(MekanismItems.init(new ItemBlock(SaltBlock), "SaltBlock"));
    }

    public static Block init(Block block, String name) {
        return block.setTranslationKey(name).setRegistryName(new ResourceLocation(Mekanism.MODID, name));
    }*/
}