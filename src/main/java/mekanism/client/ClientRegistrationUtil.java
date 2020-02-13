package mekanism.client;

import java.util.function.Function;
import java.util.function.Predicate;
import mekanism.api.providers.IBlockProvider;
import mekanism.api.providers.IItemProvider;
import mekanism.api.providers.ITileEntityTypeProvider;
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
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
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

    public static synchronized <T extends TileEntity> void bindTileEntityRenderer(TileEntityTypeRegistryObject<T> tileTypeRO,
          Function<TileEntityRendererDispatcher, TileEntityRenderer<? super T>> renderFactory) {
        ClientRegistry.bindTileEntityRenderer(tileTypeRO.getTileEntityType(), renderFactory);
    }

    @SafeVarargs
    public static synchronized <T extends TileEntity> void bindTileEntityRenderer(Function<TileEntityRendererDispatcher, TileEntityRenderer<T>> rendererFactory,
          ITileEntityTypeProvider<? extends T>... tileEntityTypeProviders) {
        TileEntityRenderer<T> renderer = rendererFactory.apply(TileEntityRendererDispatcher.instance);
        for (ITileEntityTypeProvider<? extends T> tileEntityTypeProvider : tileEntityTypeProviders) {
            ClientRegistry.bindTileEntityRenderer(tileEntityTypeProvider.getTileEntityType(), constant -> renderer);
        }
    }

    public static <C extends Container, U extends Screen & IHasContainer<C>> void registerScreen(ContainerTypeRegistryObject<C> type, IScreenFactory<C, U> factory) {
        ScreenManager.registerFactory(type.getContainerType(), factory);
    }

    public static ModelResourceLocation getInventoryMRL(Function<String, ResourceLocation> rlCreator, String type) {
        return new ModelResourceLocation(rlCreator.apply(type), "inventory");
    }

    public static void registerItemColorHandler(ItemColors colors, IItemColor itemColor, IItemProvider... items) {
        for (IItemProvider itemProvider : items) {
            colors.register(itemColor, itemProvider.getItem());
        }
    }

    public static void registerBlockColorHandler(BlockColors blockColors, IBlockColor blockColor, IBlockProvider... blocks) {
        for (IBlockProvider blockProvider : blocks) {
            blockColors.register(blockColor, blockProvider.getBlock());
        }
    }

    public static void registerBlockColorHandler(BlockColors blockColors, ItemColors itemColors, IBlockColor blockColor, IItemColor itemColor, IBlockProvider... blocks) {
        for (IBlockProvider blockProvider : blocks) {
            blockColors.register(blockColor, blockProvider.getBlock());
            itemColors.register(itemColor, blockProvider.getItem());
        }
    }

    public static void setRenderLayer(RenderType type, IBlockProvider... blockProviders) {
        for (IBlockProvider blockProvider : blockProviders) {
            RenderTypeLookup.setRenderLayer(blockProvider.getBlock(), type);
        }
    }

    public static synchronized void setRenderLayer(Predicate<RenderType> predicate, IBlockProvider... blockProviders) {
        for (IBlockProvider blockProvider : blockProviders) {
            RenderTypeLookup.setRenderLayer(blockProvider.getBlock(), predicate);
        }
    }

    @SafeVarargs
    public static void setRenderLayer(RenderType type, FluidRegistryObject<Source, Flowing, FlowingFluidBlock, BucketItem>... fluidROs) {
        for (FluidRegistryObject<Source, Flowing, FlowingFluidBlock, BucketItem> fluidRO : fluidROs) {
            RenderTypeLookup.setRenderLayer(fluidRO.getStillFluid(), type);
            RenderTypeLookup.setRenderLayer(fluidRO.getFlowingFluid(), type);
            //TODO: Do we need to set the block as well?
        }
    }

    @SafeVarargs
    public static synchronized void setRenderLayer(Predicate<RenderType> predicate, FluidRegistryObject<Source, Flowing, FlowingFluidBlock, BucketItem>... fluidROs) {
        for (FluidRegistryObject<Source, Flowing, FlowingFluidBlock, BucketItem> fluidRO : fluidROs) {
            RenderTypeLookup.setRenderLayer(fluidRO.getStillFluid(), predicate);
            RenderTypeLookup.setRenderLayer(fluidRO.getFlowingFluid(), predicate);
            //TODO: Do we need to set the block as well?
        }
    }
}