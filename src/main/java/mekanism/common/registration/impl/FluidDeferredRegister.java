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
import net.minecraft.core.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
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
import net.minecraft.world.level.material.Material;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.common.SoundActions;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.fluids.ForgeFlowingFluid.Flowing;
import net.minecraftforge.fluids.ForgeFlowingFluid.Source;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FluidDeferredRegister {

    private static final ResourceLocation OVERLAY = new ResourceLocation("block/water_overlay");
    private static final ResourceLocation RENDER_OVERLAY = new ResourceLocation("textures/misc/underwater.png");
    private static final ResourceLocation LIQUID = Mekanism.rl("liquid/liquid");
    private static final ResourceLocation LIQUID_FLOW = Mekanism.rl("liquid/liquid_flow");
    //Copy of/based off of vanilla's lava/water bucket dispense behavior
    private static final DispenseItemBehavior BUCKET_DISPENSE_BEHAVIOR = new DefaultDispenseItemBehavior() {
        @NotNull
        @Override
        public ItemStack execute(@NotNull BlockSource source, @NotNull ItemStack stack) {
            Level world = source.getLevel();
            DispensibleContainerItem bucket = (DispensibleContainerItem) stack.getItem();
            BlockPos pos = source.getPos().relative(source.getBlockState().getValue(DispenserBlock.FACING));
            if (bucket.emptyContents(null, world, pos, null)) {
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
    private final String modid;

    public FluidDeferredRegister(String modid) {
        this.modid = modid;
        blockRegister = DeferredRegister.create(ForgeRegistries.BLOCKS, modid);
        fluidRegister = DeferredRegister.create(ForgeRegistries.FLUIDS, modid);
        fluidTypeRegister = DeferredRegister.create(ForgeRegistries.Keys.FLUID_TYPES, modid);
        itemRegister = DeferredRegister.create(ForgeRegistries.ITEMS, modid);
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

    public <TYPE extends MekanismFluidType, BUCKET extends BucketItem> FluidRegistryObject<TYPE, Source, Flowing, LiquidBlock, BUCKET> register(String name,
          FluidType.Properties properties, FluidTypeRenderProperties renderProperties, BucketCreator<BUCKET> bucketCreator,
          BiFunction<FluidType.Properties, FluidTypeRenderProperties, TYPE> fluidTypeCreator) {
        String flowingName = "flowing_" + name;
        String bucketName = name + "_bucket";
        //Set the translation string to the same as the block
        properties.descriptionId(Util.makeDescriptionId("block", new ResourceLocation(modid, name)));
        //Create the registry object and let the values init to null as before we actually call get on them, we will update the backing values
        FluidRegistryObject<TYPE, Source, Flowing, LiquidBlock, BUCKET> fluidRegistryObject = new FluidRegistryObject<>();
        //Pass in suppliers that are wrapped instead of direct references to the registry objects, so that when we update the registry object to
        // point to a new object it gets updated properly.
        ForgeFlowingFluid.Properties fluidProperties = new ForgeFlowingFluid.Properties(fluidRegistryObject::getFluidType, fluidRegistryObject::getStillFluid,
              fluidRegistryObject::getFlowingFluid).bucket(fluidRegistryObject::getBucket).block(fluidRegistryObject::getBlock);
        //Update the references to objects that are retrieved from the deferred registers
        fluidRegistryObject.updateFluidType(fluidTypeRegister.register(name, () -> fluidTypeCreator.apply(properties, renderProperties)));
        fluidRegistryObject.updateStill(fluidRegister.register(name, () -> new Source(fluidProperties)));
        fluidRegistryObject.updateFlowing(fluidRegister.register(flowingName, () -> new Flowing(fluidProperties)));
        fluidRegistryObject.updateBucket(itemRegister.register(bucketName, () -> bucketCreator.create(fluidRegistryObject::getStillFluid,
              ItemDeferredRegister.getMekBaseProperties().stacksTo(1).craftRemainder(Items.BUCKET))));
        //Note: The block properties used here is a copy of the ones for water
        fluidRegistryObject.updateBlock(blockRegister.register(name, () -> new LiquidBlock(fluidRegistryObject::getStillFluid,
              BlockBehaviour.Properties.of(Material.WATER).noCollission().strength(100.0F).noLootTable())));
        allFluids.add(fluidRegistryObject);
        return fluidRegistryObject;
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

        private final ResourceLocation stillTexture;
        private final ResourceLocation flowingTexture;
        private final ResourceLocation overlayTexture;
        private final ResourceLocation renderOverlayTexture;
        private final int color;

        public MekanismFluidType(FluidType.Properties properties, FluidTypeRenderProperties renderProperties) {
            super(properties);
            this.stillTexture = renderProperties.stillTexture;
            this.flowingTexture = renderProperties.flowingTexture;
            this.overlayTexture = renderProperties.overlayTexture;
            this.renderOverlayTexture = renderProperties.renderOverlayTexture;
            this.color = renderProperties.color;
        }

        //For use in datagen
        public ResourceLocation getStillTexture() {
            return stillTexture;
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