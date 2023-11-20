package mekanism.common.registration.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import mekanism.common.Mekanism;
import mekanism.common.base.IChemicalConstant;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FastColor;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.DispensibleContainerItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.common.SoundActions;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.BaseFlowingFluid.Flowing;
import net.neoforged.neoforge.fluids.BaseFlowingFluid.Source;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FluidDeferredRegister {

    private static final ResourceLocation OVERLAY = new ResourceLocation("block/water_overlay");
    private static final ResourceLocation RENDER_OVERLAY = new ResourceLocation("misc/underwater");
    private static final ResourceLocation LIQUID = Mekanism.rl("liquid/liquid");
    private static final ResourceLocation LIQUID_FLOW = Mekanism.rl("liquid/liquid_flow");
    //Copy of/based off of vanilla's lava/water bucket dispense behavior
    private static final DispenseItemBehavior BUCKET_DISPENSE_BEHAVIOR = new DefaultDispenseItemBehavior() {
        @NotNull
        @Override
        public ItemStack execute(@NotNull BlockSource source, @NotNull ItemStack stack) {
            Level world = source.level();
            DispensibleContainerItem bucket = (DispensibleContainerItem) stack.getItem();
            BlockPos pos = source.pos().relative(source.state().getValue(DispenserBlock.FACING));
            if (bucket.emptyContents(null, world, pos, null, stack)) {
                bucket.checkExtraContent(null, world, stack, pos);
                return new ItemStack(Items.BUCKET);
            }
            return super.execute(source, stack);
        }
    };

    public static FluidType.Properties getMekBaseBuilder() {
        return FluidType.Properties.create()
              .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL)
              .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY);
    }

    private final List<FluidRegistryObject<? extends MekanismFluidType, ?, ?, ?, ?>> allFluids = new ArrayList<>();

    private final DeferredRegister<FluidType> fluidTypeRegister;
    private final DeferredRegister<Fluid> fluidRegister;
    private final DeferredRegister<Block> blockRegister;
    private final DeferredRegister<Item> itemRegister;

    public FluidDeferredRegister(String modid) {
        blockRegister = DeferredRegister.create(Registries.BLOCK, modid);
        fluidRegister = DeferredRegister.create(Registries.FLUID, modid);
        fluidTypeRegister = DeferredRegister.create(NeoForgeRegistries.Keys.FLUID_TYPES, modid);
        itemRegister = DeferredRegister.create(Registries.ITEM, modid);
    }

    public FluidRegistryObject<MekanismFluidType, Source, Flowing, LiquidBlock, BucketItem> registerLiquidChemical(IChemicalConstant constants) {
        int density = Math.round(constants.getDensity());
        return register(constants.getName(), properties -> properties
              .temperature(Math.round(constants.getTemperature()))
              .density(density)
              .viscosity(density)
              .lightLevel(constants.getLightLevel()), renderProperties -> renderProperties
              .tint(constants.getColor())
        );
    }

    public FluidRegistryObject<MekanismFluidType, Source, Flowing, LiquidBlock, BucketItem> register(String name, UnaryOperator<FluidTypeRenderProperties> renderProperties) {
        return register(name, UnaryOperator.identity(), renderProperties);
    }

    public FluidRegistryObject<MekanismFluidType, Source, Flowing, LiquidBlock, BucketItem> register(String name, UnaryOperator<FluidType.Properties> properties,
          UnaryOperator<FluidTypeRenderProperties> renderProperties) {
        return register(name, BucketItem::new, properties, renderProperties);
    }

    public <BUCKET extends BucketItem> FluidRegistryObject<MekanismFluidType, Source, Flowing, LiquidBlock, BUCKET> register(String name, BucketCreator<BUCKET> bucketCreator,
          UnaryOperator<FluidType.Properties> fluidProperties, UnaryOperator<FluidTypeRenderProperties> renderProperties) {
        return register(name, fluidProperties.apply(getMekBaseBuilder()), renderProperties.apply(FluidTypeRenderProperties.builder()), bucketCreator,
              MekanismFluidType::new);
    }

    public <BUCKET extends BucketItem> FluidRegistryObject<MekanismFluidType, Source, Flowing, LiquidBlock, BUCKET> register(String name,
          FluidType.Properties properties, FluidTypeRenderProperties renderProperties, BucketCreator<BUCKET> bucketCreator,
          BiFunction<FluidType.Properties, FluidTypeRenderProperties, MekanismFluidType> fluidTypeCreator) {
        //Create a wrapper to hold the fluid object that we then will populate information using
        FluidRegistryObject<MekanismFluidType, Source, Flowing, LiquidBlock, BUCKET> result =
              new FluidRegistryObject<>(new ResourceLocation(fluidRegister.getNamespace(), name));
        BaseFlowingFluid.Properties fluidProperties = new BaseFlowingFluid.Properties(result.fluidTypeRO, result.stillRO, result.flowingRO)
              .bucket(result.bucketRO)
              .block(result.blockRO);
        //Note: We ignore the returned values as we already make our own holders in FluidRegistryObject's constructor
        fluidTypeRegister.register(result.fluidTypeRO.getName(), rl -> {
            //Set the translation string to the same as the block (we rely on the implementation detail that we make our fluid type's name is the same as the block's)
            properties.descriptionId(Util.makeDescriptionId("block", rl));
            return fluidTypeCreator.apply(properties, renderProperties);
        });
        fluidRegister.register(result.stillRO.getName(), () -> new Source(fluidProperties));
        fluidRegister.register(result.flowingRO.getName(), () -> new Flowing(fluidProperties));
        itemRegister.register(result.bucketRO.getName(), () -> bucketCreator.create(result.stillRO, new Item.Properties().stacksTo(1).craftRemainder(Items.BUCKET)));
        MapColor color = getClosestColor(renderProperties.color);
        //Note: The block properties used here is a copy of the ones for water
        blockRegister.register(result.blockRO.getName(), () -> new LiquidBlock(result.stillRO, BlockBehaviour.Properties.of()
              .noCollission().strength(100.0F).noLootTable().replaceable().pushReaction(PushReaction.DESTROY).liquid().mapColor(color)));
        allFluids.add(result);
        return result;
    }

    private static MapColor getClosestColor(int tint) {
        if (tint == 0xFFFFFFFF) {
            return MapColor.NONE;
        }
        int red = FastColor.ARGB32.red(tint);
        int green = FastColor.ARGB32.green(tint);
        int blue = FastColor.ARGB32.blue(tint);
        MapColor color = MapColor.NONE;
        double minDistance = Double.MAX_VALUE;
        for (MapColor toTest : MapColor.MATERIAL_COLORS) {
            if (toTest != null && toTest != MapColor.NONE) {
                int testRed = FastColor.ARGB32.red(toTest.col);
                int testGreen = FastColor.ARGB32.green(toTest.col);
                int testBlue = FastColor.ARGB32.blue(toTest.col);
                double distanceSquare = perceptualColorDistanceSquared(red, green, blue, testRed, testGreen, testBlue);
                if (distanceSquare < minDistance) {
                    minDistance = distanceSquare;
                    color = toTest;
                }
            }
        }
        return color;
    }

    /**
     * <a href="http://www.compuphase.com/cmetric.htm">Color Metric</a>
     * <a href="http://stackoverflow.com/a/6334454">Stack Overflow</a>
     * Returns 0 for equal colors, nonzero for colors that look different. The return value is farther from 0 the more different the colors look.
     */
    private static double perceptualColorDistanceSquared(int red1, int green1, int blue1, int red2, int green2, int blue2) {
        int redMean = (red1 + red2) >> 1;
        int r = red1 - red2;
        int g = green1 - green2;
        int b = blue1 - blue2;
        return (((512 + redMean) * r * r) >> 8) + 4 * g * g + (((767 - redMean) * b * b) >> 8);
    }

    public void register(IEventBus bus) {
        blockRegister.register(bus);
        fluidRegister.register(bus);
        fluidTypeRegister.register(bus);
        itemRegister.register(bus);
    }

    public List<FluidRegistryObject<? extends MekanismFluidType, ?, ?, ?, ?>> getAllFluids() {
        return Collections.unmodifiableList(allFluids);
    }

    public void registerBucketDispenserBehavior() {
        for (FluidRegistryObject<?, ?, ?, ?, ?> fluidRO : getAllFluids()) {
            DispenserBlock.registerBehavior(fluidRO.getBucket(), BUCKET_DISPENSE_BEHAVIOR);
        }
    }

    @FunctionalInterface
    public interface BucketCreator<BUCKET extends BucketItem> {

        BUCKET create(Supplier<? extends Fluid> supplier, Properties builder);
    }

    public static class FluidTypeRenderProperties {

        private ResourceLocation stillTexture = LIQUID;
        private ResourceLocation flowingTexture = LIQUID_FLOW;
        //For now all our fluids use the same "overlay" for being against glass as vanilla water.
        private ResourceLocation overlayTexture = OVERLAY;
        private ResourceLocation renderOverlayTexture = RENDER_OVERLAY;
        private int color = 0xFFFFFFFF;

        private FluidTypeRenderProperties() {
        }

        public static FluidTypeRenderProperties builder() {
            return new FluidTypeRenderProperties();
        }

        public FluidTypeRenderProperties texture(ResourceLocation still, ResourceLocation flowing) {
            this.stillTexture = still;
            this.flowingTexture = flowing;
            return this;
        }

        public FluidTypeRenderProperties texture(ResourceLocation still, ResourceLocation flowing, ResourceLocation overlay) {
            this.stillTexture = still;
            this.flowingTexture = flowing;
            this.overlayTexture = overlay;
            return this;
        }

        public FluidTypeRenderProperties renderOverlay(ResourceLocation renderOverlay) {
            this.renderOverlayTexture = renderOverlay;
            return this;
        }

        public FluidTypeRenderProperties tint(int color) {
            this.color = color;
            return this;
        }
    }

    public static class MekanismFluidType extends FluidType {

        public final ResourceLocation stillTexture;
        public final ResourceLocation flowingTexture;
        public final ResourceLocation overlayTexture;
        public final ResourceLocation renderOverlayTexture;
        private final int color;

        public MekanismFluidType(FluidType.Properties properties, FluidTypeRenderProperties renderProperties) {
            super(properties);
            this.stillTexture = renderProperties.stillTexture;
            this.flowingTexture = renderProperties.flowingTexture;
            this.overlayTexture = renderProperties.overlayTexture;
            this.renderOverlayTexture = renderProperties.renderOverlayTexture;
            this.color = renderProperties.color;
        }

        @Override
        public boolean isVaporizedOnPlacement(Level level, BlockPos pos, FluidStack stack) {
            //TODO - 1.19: Decide on this for our fluids for now default to not vaporizing
            return false;
        }

        @Override
        public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
            consumer.accept(new IClientFluidTypeExtensions() {
                @Override
                public ResourceLocation getStillTexture() {
                    return stillTexture;
                }

                @Override
                public ResourceLocation getFlowingTexture() {
                    return flowingTexture;
                }

                @Override
                public ResourceLocation getOverlayTexture() {
                    return overlayTexture;
                }

                @Nullable
                @Override
                public ResourceLocation getRenderOverlayTexture(Minecraft mc) {
                    return renderOverlayTexture;
                }

                @Override
                public int getTintColor() {
                    return color;
                }
            });
        }
    }
}