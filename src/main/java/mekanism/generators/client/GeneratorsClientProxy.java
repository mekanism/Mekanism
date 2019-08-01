package mekanism.generators.client;

import java.util.function.Function;
import mekanism.client.render.item.ItemLayerWrapper;
import mekanism.common.item.IItemRedirectedModel;
import mekanism.generators.client.gui.GuiBioGenerator;
import mekanism.generators.client.gui.GuiGasGenerator;
import mekanism.generators.client.gui.GuiHeatGenerator;
import mekanism.generators.client.gui.GuiIndustrialTurbine;
import mekanism.generators.client.gui.GuiReactorController;
import mekanism.generators.client.gui.GuiReactorFuel;
import mekanism.generators.client.gui.GuiReactorHeat;
import mekanism.generators.client.gui.GuiReactorLogicAdapter;
import mekanism.generators.client.gui.GuiReactorStats;
import mekanism.generators.client.gui.GuiSolarGenerator;
import mekanism.generators.client.gui.GuiTurbineStats;
import mekanism.generators.client.gui.GuiWindGenerator;
import mekanism.generators.client.render.RenderAdvancedSolarGenerator;
import mekanism.generators.client.render.RenderBioGenerator;
import mekanism.generators.client.render.RenderGasGenerator;
import mekanism.generators.client.render.RenderHeatGenerator;
import mekanism.generators.client.render.RenderIndustrialTurbine;
import mekanism.generators.client.render.RenderReactor;
import mekanism.generators.client.render.RenderSolarGenerator;
import mekanism.generators.client.render.RenderTurbineRotor;
import mekanism.generators.client.render.RenderWindGenerator;
import mekanism.generators.client.render.item.RenderAdvancedSolarGeneratorItem;
import mekanism.generators.client.render.item.RenderBioGeneratorItem;
import mekanism.generators.client.render.item.RenderGasGeneratorItem;
import mekanism.generators.client.render.item.RenderHeatGeneratorItem;
import mekanism.generators.client.render.item.RenderSolarGeneratorItem;
import mekanism.generators.client.render.item.RenderWindGeneratorItem;
import mekanism.generators.common.GeneratorsBlock;
import mekanism.generators.common.GeneratorsCommonProxy;
import mekanism.generators.common.GeneratorsItem;
import mekanism.generators.common.MekanismGenerators;
import mekanism.generators.common.tile.TileEntityAdvancedSolarGenerator;
import mekanism.generators.common.tile.TileEntityBioGenerator;
import mekanism.generators.common.tile.TileEntityGasGenerator;
import mekanism.generators.common.tile.TileEntityHeatGenerator;
import mekanism.generators.common.tile.TileEntitySolarGenerator;
import mekanism.generators.common.tile.TileEntityWindGenerator;
import mekanism.generators.common.tile.reactor.TileEntityReactorController;
import mekanism.generators.common.tile.reactor.TileEntityReactorLogicAdapter;
import mekanism.generators.common.tile.turbine.TileEntityTurbineCasing;
import mekanism.generators.common.tile.turbine.TileEntityTurbineRotor;
import mekanism.generators.common.tile.turbine.TileEntityTurbineValve;
import mekanism.generators.common.tile.turbine.TileEntityTurbineVent;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.IRegistry;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GeneratorsClientProxy extends GeneratorsCommonProxy {

    @Override
    public void registerTESRs() {
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityAdvancedSolarGenerator.class, new RenderAdvancedSolarGenerator());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityBioGenerator.class, new RenderBioGenerator());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityGasGenerator.class, new RenderGasGenerator());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityHeatGenerator.class, new RenderHeatGenerator());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityReactorController.class, new RenderReactor());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntitySolarGenerator.class, new RenderSolarGenerator());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityTurbineCasing.class, new RenderIndustrialTurbine());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityTurbineRotor.class, new RenderTurbineRotor());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityTurbineValve.class, new RenderIndustrialTurbine());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityTurbineVent.class, new RenderIndustrialTurbine());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityWindGenerator.class, new RenderWindGenerator());
    }

    @Override
    public void registerItemRenders() {
        registerItemRender(GeneratorsItem.SOLAR_PANEL.getItem());
        registerItemRender(GeneratorsItem.HOHLRAUM.getItem());
        registerItemRender(GeneratorsItem.TURBINE_BLADE.getItem());

        GeneratorsBlock.ADVANCED_SOLAR_GENERATOR.getItem().setTileEntityItemStackRenderer(new RenderAdvancedSolarGeneratorItem());
        GeneratorsBlock.BIO_GENERATOR.getItem().setTileEntityItemStackRenderer(new RenderBioGeneratorItem());
        GeneratorsBlock.GAS_BURNING_GENERATOR.getItem().setTileEntityItemStackRenderer(new RenderGasGeneratorItem());
        GeneratorsBlock.HEAT_GENERATOR.getItem().setTileEntityItemStackRenderer(new RenderHeatGeneratorItem());
        GeneratorsBlock.SOLAR_GENERATOR.getItem().setTileEntityItemStackRenderer(new RenderSolarGeneratorItem());
        GeneratorsBlock.WIND_GENERATOR.getItem().setTileEntityItemStackRenderer(new RenderWindGeneratorItem());

        //Register the item inventory model locations for the various blocks
        for (GeneratorsBlock generatorsBlock : GeneratorsBlock.values()) {
            ItemBlock item = generatorsBlock.getItem();
            if (item instanceof IItemRedirectedModel) {
                //TODO: Fix Glow panel item coloring
                ModelLoader.setCustomModelResourceLocation(item, 0, getInventoryMRL(((IItemRedirectedModel) item).getRedirectLocation()));
            } else {
                ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(item.getRegistryName(), "inventory"));
            }
        }
    }

    @Override
    public void registerBlockRenders() {
    }

    public void registerItemRender(Item item) {
        ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(item.getRegistryName(), "inventory"));
    }

    @SubscribeEvent
    public void onModelBake(ModelBakeEvent event) {
        IRegistry<ModelResourceLocation, IBakedModel> modelRegistry = event.getModelRegistry();
        registerItemStackModel(modelRegistry, "heat_generator", model -> RenderHeatGeneratorItem.model = model);
        registerItemStackModel(modelRegistry, "solar_generator", model -> RenderSolarGeneratorItem.model = model);
        registerItemStackModel(modelRegistry, "bio_generator", model -> RenderBioGeneratorItem.model = model);
        registerItemStackModel(modelRegistry, "wind_generator", model -> RenderWindGeneratorItem.model = model);
        registerItemStackModel(modelRegistry, "gas_burning_generator", model -> RenderGasGeneratorItem.model = model);
        registerItemStackModel(modelRegistry, "advanced_solar_generator", model -> RenderAdvancedSolarGeneratorItem.model = model);
    }

    private void registerItemStackModel(IRegistry<ModelResourceLocation, IBakedModel> modelRegistry, String type, Function<ItemLayerWrapper, IBakedModel> setModel) {
        ModelResourceLocation resourceLocation = getInventoryMRL(type);
        modelRegistry.putObject(resourceLocation, setModel.apply(new ItemLayerWrapper(modelRegistry.getObject(resourceLocation))));
    }

    private ModelResourceLocation getInventoryMRL(String type) {
        return new ModelResourceLocation(new ResourceLocation(MekanismGenerators.MODID, type), "inventory");
    }

    @Override
    public void preInit() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public GuiScreen getClientGui(int ID, EntityPlayer player, World world, BlockPos pos) {
        TileEntity tileEntity = world.getTileEntity(pos);

        switch (ID) {
            case 0:
                return new GuiHeatGenerator(player.inventory, (TileEntityHeatGenerator) tileEntity);
            case 1:
                return new GuiSolarGenerator(player.inventory, (TileEntitySolarGenerator) tileEntity);
            case 3:
                return new GuiGasGenerator(player.inventory, (TileEntityGasGenerator) tileEntity);
            case 4:
                return new GuiBioGenerator(player.inventory, (TileEntityBioGenerator) tileEntity);
            case 5:
                return new GuiWindGenerator(player.inventory, (TileEntityWindGenerator) tileEntity);
            case 6:
                return new GuiIndustrialTurbine(player.inventory, (TileEntityTurbineCasing) tileEntity);
            case 7:
                return new GuiTurbineStats(player.inventory, (TileEntityTurbineCasing) tileEntity);
            case 10:
                return new GuiReactorController(player.inventory, (TileEntityReactorController) tileEntity);
            case 11:
                return new GuiReactorHeat(player.inventory, (TileEntityReactorController) tileEntity);
            case 12:
                return new GuiReactorFuel(player.inventory, (TileEntityReactorController) tileEntity);
            case 13:
                return new GuiReactorStats(player.inventory, (TileEntityReactorController) tileEntity);
            case 15:
                return new GuiReactorLogicAdapter(player.inventory, (TileEntityReactorLogicAdapter) tileEntity);
        }

        return null;
    }

    @SubscribeEvent
    public void onStitch(TextureStitchEvent.Pre event) {
    }
}