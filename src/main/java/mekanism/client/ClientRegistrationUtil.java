package mekanism.client;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.List;
import mekanism.client.gui.machine.GuiAdvancedElectricMachine;
import mekanism.client.gui.machine.GuiElectricMachine;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.RenderPropertiesProvider;
import mekanism.common.block.BlockMekanism;
import mekanism.common.block.interfaces.IColoredBlock;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.registration.impl.BlockDeferredRegister;
import mekanism.common.registration.impl.BlockRegistryObject;
import mekanism.common.registration.impl.ContainerTypeRegistryObject;
import mekanism.common.registration.impl.FluidDeferredRegister;
import mekanism.common.registration.impl.FluidDeferredRegister.MekanismFluidType;
import mekanism.common.registration.impl.FluidRegistryObject;
import mekanism.common.registration.impl.TileEntityTypeRegistryObject;
import mekanism.common.tile.prefab.TileEntityAdvancedElectricMachine;
import mekanism.common.tile.prefab.TileEntityElectricMachine;
import net.minecraft.client.Camera;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.gui.screens.MenuScreens.ScreenConstructor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.environment.FogEnvironment;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterFluidModelsEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.client.fluid.FluidTintSources;
import net.neoforged.neoforge.fluids.FluidType;
import org.joml.Vector4f;
import org.jspecify.annotations.Nullable;

public class ClientRegistrationUtil {

    private static final List<BlockTintSource> COLORED_BLOCK_COLOR = Collections.singletonList(state -> {
        Block block = state.getBlock();
        if (block instanceof IColoredBlock coloredBlock) {
            return coloredBlock.getColor().getPackedColor();
        }
        return -1;
    });
    //TODO - 26.2 item models
    /*
    private static final ItemColor COLORED_ITEM_COLOR = (stack, tintIndex) -> {
        Item item = stack.getItem();
        if (tintIndex == 1 && item instanceof IColoredItem) {
            EnumColor color = stack.get(MekanismDataComponents.COLOR);
            if (color == null) {
                return 0xFF555555;
            }
            int[] rgbCode = color.getRgbCode();
            return ARGB.color(0xFF, rgbCode[0], rgbCode[1], rgbCode[2]);
        }
        return -1;
    };*/
    //private static final ItemColor BUCKET_ITEM_COLOR = new DynamicFluidContainerModel.Colors();

    private ClientRegistrationUtil() {
    }

    @SafeVarargs
    public static <T extends BlockEntity, S extends BlockEntityRenderState> void bindTileEntityRenderer(EntityRenderersEvent.RegisterRenderers event,
          BlockEntityRendererProvider<T, S> rendererProvider, TileEntityTypeRegistryObject<? extends T>... tileEntityTypeROs) {
        if (tileEntityTypeROs.length == 0) {
            throw new IllegalArgumentException("No renderers provided.");
        } else if (tileEntityTypeROs.length == 1) {
            event.registerBlockEntityRenderer(tileEntityTypeROs[0].get(), rendererProvider);
        } else {
            BlockEntityRendererProvider<T, S> provider = new BlockEntityRendererProvider<>() {
                @Nullable
                private WeakReference<Context> cachedContext;
                @Nullable
                private WeakReference<BlockEntityRenderer<T, S>> cachedRenderer;

                @Override
                public BlockEntityRenderer<T, S> create(Context context) {
                    //If there is a cached context and renderer make use of it, otherwise create one and cache it
                    // this allows us to reduce the number of renderer classes we create
                    BlockEntityRenderer<T, S> renderer = cachedRenderer == null ? null : cachedRenderer.get();
                    if (cachedContext == null || cachedContext.get() != context || renderer == null) {
                        renderer = rendererProvider.create(context);
                        cachedContext = new WeakReference<>(context);
                        cachedRenderer = new WeakReference<>(renderer);
                    }
                    return renderer;
                }
            };
            for (TileEntityTypeRegistryObject<? extends T> tileTypeRO : tileEntityTypeROs) {
                event.registerBlockEntityRenderer(tileTypeRO.get(), provider);
            }
        }
    }

    public static <C extends AbstractContainerMenu, U extends Screen & MenuAccess<C>> void registerScreen(RegisterMenuScreensEvent event,
          ContainerTypeRegistryObject<C> type, ScreenConstructor<C, U> factory) {
        event.register(type.get(), factory);
    }

    //Helper method to register GuiElectricMachine due to generics not being able to be resolved through registerScreen
    @SuppressWarnings("RedundantTypeArguments")
    public static <TILE extends TileEntityElectricMachine, C extends MekanismTileContainer<TILE>> void registerElectricScreen(RegisterMenuScreensEvent event,
          ContainerTypeRegistryObject<C> type) {
        ClientRegistrationUtil.<C, GuiElectricMachine<TILE, C>>registerScreen(event, type, GuiElectricMachine::new);
    }

    //Helper method to register GuiAdvancedElectricMachine due to generics not being able to be resolved through registerScreen
    @SuppressWarnings("RedundantTypeArguments")
    public static <TILE extends TileEntityAdvancedElectricMachine, C extends MekanismTileContainer<TILE>> void registerAdvancedElectricScreen(RegisterMenuScreensEvent event,
          ContainerTypeRegistryObject<C> type) {
        ClientRegistrationUtil.<C, GuiAdvancedElectricMachine<TILE, C>>registerScreen(event, type, GuiAdvancedElectricMachine::new);
    }

    public static void registerKeyBindings(RegisterKeyMappingsEvent event, KeyMapping... keys) {
        for (KeyMapping key : keys) {
            event.register(key);
        }
    }


    @SafeVarargs
    public static void registerBlockColorHandler(RegisterColorHandlersEvent.BlockTintSources event, List<BlockTintSource> tintSources, Holder<Block>... blocks) {
        for (Holder<Block> blockProvider : blocks) {
            event.register(tintSources, blockProvider.value());
        }
    }

    @SafeVarargs
    public static void registerBlockColorHandler(RegisterColorHandlersEvent.BlockTintSources event, BlockTintSource tintSource, Holder<Block>... blocks) {
        registerBlockColorHandler(event, Collections.singletonList(tintSource), blocks);
    }

    public static void registerIColoredBlockHandler(RegisterColorHandlersEvent event, BlockRegistryObject<?, ?>... blocks) {
        //TODO - 26.2: do this with the models themselves?
        if (event instanceof RegisterColorHandlersEvent.BlockTintSources blockEvent) {
            registerBlockColorHandler(blockEvent, COLORED_BLOCK_COLOR, blocks);
        }//TODO - 26.2 item colours
        /* else if (event instanceof RegisterColorHandlersEvent.ItemTintSources itemEvent) {
            registerItemColorHandler(itemEvent, COLORED_BLOCK_ITEM_COLOR, blocks);
        }*/
    }

    public static void registerItemExtensions(RegisterClientExtensionsEvent event, IClientItemExtensions extension, ItemLike... items) {
        for (ItemLike item : items) {
            event.registerItem(extension, item.asItem());
        }
    }

    public static void registerBlockExtensions(RegisterClientExtensionsEvent event, BlockDeferredRegister allBlocks) {
        for (Holder<Block> primaryEntry : allBlocks.getPrimaryEntries()) {
            if (primaryEntry.value() instanceof BlockMekanism) {
                event.registerBlock(RenderPropertiesProvider.PARTICLE_HANDLER, primaryEntry);
            }
        }
    }

    public static void registerFluidExtensions(RegisterClientExtensionsEvent event, FluidDeferredRegister allFluids) {
        for (Holder<FluidType> fluidTypeEntry : allFluids.getFluidTypeEntries()) {
            if (fluidTypeEntry.value() instanceof MekanismFluidType fluidType) {
                int fluidTint = fluidType.color;
                event.registerFluidType(new IClientFluidTypeExtensions() {

                    @Override
                    public Identifier getRenderOverlayTexture(Minecraft mc) {
                        return fluidType.renderOverlayTexture;
                    }

                    @Override
                    public void modifyFogColor(Camera camera, float partialTick, ClientLevel level, int renderDistance, float darkenWorldAmount, Vector4f fluidFogColor) {
                        //TODO - 26.2: is alpha needed?
                        fluidFogColor.set(ARGB.redFloat(fluidTint), ARGB.greenFloat(fluidTint), ARGB.blueFloat(fluidTint));
                    }

                    @Override
                    public void modifyFogRender(Camera camera, @Nullable FogEnvironment environment, float renderDistance, float partialTick, FogData fog) {
                        //Copy of logic for water except always treating it as if it was a player who has no water vision
                        // and does not take the biome's closer water fog into account
                        float partialTicks = MekanismRenderer.getPartialTick();
                        fog.environmentalStart = camera.attributeProbe().getValue(EnvironmentAttributes.WATER_FOG_START_DISTANCE, partialTicks);
                        fog.environmentalEnd = camera.attributeProbe().getValue(EnvironmentAttributes.WATER_FOG_END_DISTANCE, partialTicks);

                        fog.skyEnd = fog.environmentalEnd;
                        fog.cloudEnd = fog.environmentalEnd;
                    }
                }, fluidType);
            }
        }
    }

    public static void registerFluidModels(RegisterFluidModelsEvent event, FluidDeferredRegister fluidDeferredRegister) {
        for (FluidRegistryObject<MekanismFluidType, ?, ?, ?, ?> registryObject : fluidDeferredRegister.getEntries()) {
            MekanismFluidType fluidType = registryObject.getFluidType();
            event.register(new FluidModel.Unbaked(
                  new Material(fluidType.stillTexture),
                  new Material(fluidType.flowingTexture),
                  new Material(fluidType.overlayTexture),
                  FluidTintSources.constant(fluidType.color)
            ), registryObject, registryObject.getFlowingFluid());
        }
    }
}