package mekanism.client;

import java.util.function.Function;
import java.util.function.Predicate;
import javax.annotation.Nonnull;
import mekanism.api.providers.IBlockProvider;
import mekanism.api.providers.IItemProvider;
import mekanism.client.gui.machine.GuiAdvancedElectricMachine;
import mekanism.client.gui.machine.GuiElectricMachine;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.block.interfaces.IColoredBlock;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.registration.impl.ContainerTypeRegistryObject;
import mekanism.common.registration.impl.EntityTypeRegistryObject;
import mekanism.common.registration.impl.FluidRegistryObject;
import mekanism.common.registration.impl.ParticleTypeRegistryObject;
import mekanism.common.registration.impl.TileEntityTypeRegistryObject;
import mekanism.common.tile.prefab.TileEntityAdvancedElectricMachine;
import mekanism.common.tile.prefab.TileEntityElectricMachine;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.IHasContainer;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.gui.ScreenManager.IScreenFactory;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.BlockItem;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.Item;
import net.minecraft.item.ItemModelsProperties;
import net.minecraft.particles.IParticleData;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.client.registry.RenderingRegistry;

public class ClientRegistrationUtil {

    private ClientRegistrationUtil() {
    }

    public static <T extends Entity> void registerEntityRenderingHandler(EntityTypeRegistryObject<T> entityTypeRO, IRenderFactory<? super T> renderFactory) {
        RenderingRegistry.registerEntityRenderingHandler(entityTypeRO.getEntityType(), renderFactory);
    }

    public static synchronized <T extends TileEntity> void bindTileEntityRenderer(TileEntityTypeRegistryObject<T> tileTypeRO,
          Function<TileEntityRendererDispatcher, TileEntityRenderer<? super T>> renderFactory) {
        ClientRegistry.bindTileEntityRenderer(tileTypeRO.getTileEntityType(), renderFactory);
    }

    @SafeVarargs
    public static synchronized <T extends TileEntity> void bindTileEntityRenderer(Function<TileEntityRendererDispatcher, TileEntityRenderer<T>> rendererFactory,
          TileEntityTypeRegistryObject<? extends T>... tileEntityTypeROs) {
        TileEntityRenderer<T> renderer = rendererFactory.apply(TileEntityRendererDispatcher.instance);
        for (TileEntityTypeRegistryObject<? extends T> tileTypeRO : tileEntityTypeROs) {
            ClientRegistry.bindTileEntityRenderer(tileTypeRO.getTileEntityType(), constant -> renderer);
        }
    }

    public static <T extends IParticleData> void registerParticleFactory(ParticleTypeRegistryObject<T, ?> particleTypeRO, ParticleManager.IParticleMetaFactory<T> factory) {
        Minecraft.getInstance().particles.registerFactory(particleTypeRO.getParticleType(), factory);
    }

    public static <C extends Container, U extends Screen & IHasContainer<C>> void registerScreen(ContainerTypeRegistryObject<C> type, IScreenFactory<C, U> factory) {
        ScreenManager.registerFactory(type.getContainerType(), factory);
    }

    //Helper method to register GuiElectricMachine due to generics not being able to be resolved through registerScreen
    public static <TILE extends TileEntityElectricMachine, C extends MekanismTileContainer<TILE>> void registerElectricScreen(ContainerTypeRegistryObject<C> type) {
        registerScreen(type, new IScreenFactory<C, GuiElectricMachine<TILE, C>>() {
            @Nonnull
            @Override
            public GuiElectricMachine<TILE, C> create(@Nonnull C container, @Nonnull PlayerInventory inv, @Nonnull ITextComponent title) {
                return new GuiElectricMachine<>(container, inv, title);
            }
        });
    }

    //Helper method to register GuiAdvancedElectricMachine due to generics not being able to be resolved through registerScreen
    public static <TILE extends TileEntityAdvancedElectricMachine, C extends MekanismTileContainer<TILE>> void registerAdvancedElectricScreen(ContainerTypeRegistryObject<C> type) {
        registerScreen(type, new IScreenFactory<C, GuiAdvancedElectricMachine<TILE, C>>() {
            @Nonnull
            @Override
            public GuiAdvancedElectricMachine<TILE, C> create(@Nonnull C container, @Nonnull PlayerInventory inv, @Nonnull ITextComponent title) {
                return new GuiAdvancedElectricMachine<>(container, inv, title);
            }
        });
    }

    public static void setPropertyOverride(IItemProvider itemProvider, ResourceLocation override, IItemPropertyGetter propertyGetter) {
        ItemModelsProperties.registerProperty(itemProvider.getItem(), override, propertyGetter);
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

    public static void registerIColoredBlockHandler(BlockColors blockColors, ItemColors itemColors, IBlockProvider... blocks) {
        ClientRegistrationUtil.registerBlockColorHandler(blockColors, itemColors, (state, world, pos, tintIndex) -> {
            Block block = state.getBlock();
            if (block instanceof IColoredBlock) {
                return MekanismRenderer.getColorARGB(((IColoredBlock) block).getColor(), 1);
            }
            return -1;
        }, (stack, tintIndex) -> {
            Item item = stack.getItem();
            if (item instanceof BlockItem) {
                Block block = ((BlockItem) item).getBlock();
                if (block instanceof IColoredBlock) {
                    return MekanismRenderer.getColorARGB(((IColoredBlock) block).getColor(), 1);
                }
            }
            return -1;
        }, blocks);
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

    public static void setRenderLayer(RenderType type, FluidRegistryObject<?, ?, ?, ?>... fluidROs) {
        for (FluidRegistryObject<?, ?, ?, ?> fluidRO : fluidROs) {
            RenderTypeLookup.setRenderLayer(fluidRO.getStillFluid(), type);
            RenderTypeLookup.setRenderLayer(fluidRO.getFlowingFluid(), type);
        }
    }

    public static synchronized void setRenderLayer(Predicate<RenderType> predicate, FluidRegistryObject<?, ?, ?, ?>... fluidROs) {
        for (FluidRegistryObject<?, ?, ?, ?> fluidRO : fluidROs) {
            RenderTypeLookup.setRenderLayer(fluidRO.getStillFluid(), predicate);
            RenderTypeLookup.setRenderLayer(fluidRO.getFlowingFluid(), predicate);
        }
    }
}