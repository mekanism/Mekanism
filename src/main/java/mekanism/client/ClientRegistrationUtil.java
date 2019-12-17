package mekanism.client;

import java.util.function.Predicate;
import mekanism.api.providers.IBlockProvider;
import mekanism.api.providers.IItemProvider;
import mekanism.common.registration.impl.ContainerTypeRegistryObject;
import mekanism.common.registration.impl.EntityTypeRegistryObject;
import mekanism.common.registration.impl.FluidRegistryObject;
import mekanism.common.registration.impl.TileEntityTypeRegistryObject;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.client.gui.IHasContainer;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.gui.ScreenManager.IScreenFactory;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.BucketItem;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.ForgeFlowingFluid.Flowing;
import net.minecraftforge.fluids.ForgeFlowingFluid.Source;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.client.registry.RenderingRegistry;

public class ClientRegistrationUtil {

    public static <T extends Entity> void registerEntityRenderingHandler(EntityTypeRegistryObject<T> entityTypeRO, IRenderFactory<? super T> renderFactory) {
        RenderingRegistry.registerEntityRenderingHandler(entityTypeRO.getEntityType(), renderFactory);
    }

    public static <T extends TileEntity> void bindTileEntityRenderer(TileEntityTypeRegistryObject<T> tileTypeRO, TileEntityRenderer<? super T> specialRenderer) {
        ClientRegistry.bindTileEntityRenderer(tileTypeRO.getTileEntityType(), specialRenderer);
    }

    public static <C extends Container, U extends Screen & IHasContainer<C>> void registerScreen(ContainerTypeRegistryObject<C> type, IScreenFactory<C, U> factory) {
        ScreenManager.registerFactory(type.getContainerType(), factory);
    }

    public static ModelResourceLocation getInventoryMRL(String modid, String type) {
        return new ModelResourceLocation(new ResourceLocation(modid, type), "inventory");
    }

    public static void registerItemColorHandler(ItemColors colors, IItemColor itemColor, IItemProvider... items) {
        for (IItemProvider itemProvider : items) {
            colors.register(itemColor, itemProvider.getItem());
        }
    }

    public static void registerBlockColorHandler(BlockColors blockColors, ItemColors itemColors, IBlockColor blockColor, IItemColor itemColor, IBlockProvider... blocks) {
        for (IBlockProvider additionsBlock : blocks) {
            blockColors.register(blockColor, additionsBlock.getBlock());
            itemColors.register(itemColor, additionsBlock.getItem());
        }
    }

    public static void setRenderLayer(IBlockProvider block, RenderType type) {
        RenderTypeLookup.setRenderLayer(block.getBlock(), type);
    }

    public static void setRenderLayer(IBlockProvider block, Predicate<RenderType> predicate) {
        RenderTypeLookup.setRenderLayer(block.getBlock(), predicate);
    }

    public static void setRenderLayer(FluidRegistryObject<Source, Flowing, FlowingFluidBlock, BucketItem> fluidRO, RenderType type) {
        RenderTypeLookup.setRenderLayer(fluidRO.getStillFluid(), type);
        RenderTypeLookup.setRenderLayer(fluidRO.getFlowingFluid(), type);
        //TODO: Do we need to set the block as well?
    }

    public static synchronized void setRenderLayer(FluidRegistryObject<Source, Flowing, FlowingFluidBlock, BucketItem> fluidRO, Predicate<RenderType> predicate) {
        RenderTypeLookup.setRenderLayer(fluidRO.getStillFluid(), predicate);
        RenderTypeLookup.setRenderLayer(fluidRO.getFlowingFluid(), predicate);
        //TODO: Do we need to set the block as well?
    }
}