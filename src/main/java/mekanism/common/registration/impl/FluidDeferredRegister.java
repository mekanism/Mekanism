package mekanism.common.registration.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import javax.annotation.Nonnull;
import mekanism.common.Mekanism;
import mekanism.common.base.IChemicalConstant;
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
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidAttributes.Builder;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.fluids.ForgeFlowingFluid.Flowing;
import net.minecraftforge.fluids.ForgeFlowingFluid.Source;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class FluidDeferredRegister {

    private static final ResourceLocation OVERLAY = new ResourceLocation("minecraft", "block/water_overlay");
    private static final ResourceLocation LIQUID = Mekanism.rl("liquid/liquid");
    private static final ResourceLocation LIQUID_FLOW = Mekanism.rl("liquid/liquid_flow");
    //Copy of/based off of vanilla's lava/water bucket dispense behavior
    private static final DispenseItemBehavior BUCKET_DISPENSE_BEHAVIOR = new DefaultDispenseItemBehavior() {
        @Nonnull
        @Override
        public ItemStack execute(@Nonnull BlockSource source, @Nonnull ItemStack stack) {
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

    public static FluidAttributes.Builder getMekBaseBuilder() {
        return getMekBaseBuilder(LIQUID, LIQUID_FLOW);
    }

    public static FluidAttributes.Builder getMekBaseBuilder(ResourceLocation still, ResourceLocation flowing) {
        //For now all our fluids use the same "overlay" for being against glass as vanilla water.
        return FluidAttributes.builder(still, flowing)
              .sound(SoundEvents.BUCKET_FILL, SoundEvents.BUCKET_EMPTY)
              .overlay(OVERLAY);
    }

    private final List<FluidRegistryObject<?, ?, ?, ?>> allFluids = new ArrayList<>();

    private final DeferredRegister<Fluid> fluidRegister;
    private final DeferredRegister<Block> blockRegister;
    private final DeferredRegister<Item> itemRegister;

    public FluidDeferredRegister(String modid) {
        blockRegister = DeferredRegister.create(ForgeRegistries.BLOCKS, modid);
        fluidRegister = DeferredRegister.create(ForgeRegistries.FLUIDS, modid);
        itemRegister = DeferredRegister.create(ForgeRegistries.ITEMS, modid);
    }

    public FluidRegistryObject<Source, Flowing, LiquidBlock, BucketItem> registerLiquidChemical(IChemicalConstant constants) {
        int density = Math.round(constants.getDensity());
        return register(constants.getName(), fluidAttributes -> fluidAttributes
              .color(constants.getColor())
              .temperature(Math.round(constants.getTemperature()))
              .density(density)
              .viscosity(density)
              .luminosity(constants.getLuminosity()));
    }

    public FluidRegistryObject<Source, Flowing, LiquidBlock, BucketItem> register(String name, UnaryOperator<Builder> fluidAttributes) {
        return register(name, BucketItem::new, fluidAttributes);
    }

    public <BUCKET extends BucketItem> FluidRegistryObject<Source, Flowing, LiquidBlock, BUCKET> register(String name, BucketCreator<BUCKET> bucketCreator,
          UnaryOperator<Builder> fluidAttributes) {
        return register(name, fluidAttributes.apply(getMekBaseBuilder()), bucketCreator);
    }

    public FluidRegistryObject<Source, Flowing, LiquidBlock, BucketItem> register(String name, FluidAttributes.Builder builder) {
        return register(name, builder, BucketItem::new);
    }

    public <BUCKET extends BucketItem> FluidRegistryObject<Source, Flowing, LiquidBlock, BUCKET> register(String name, FluidAttributes.Builder builder,
          BucketCreator<BUCKET> bucketCreator) {
        String flowingName = "flowing_" + name;
        String bucketName = name + "_bucket";
        //Create the registry object and let the values init to null as before we actually call get on them, we will update the backing values
        FluidRegistryObject<Source, Flowing, LiquidBlock, BUCKET> fluidRegistryObject = new FluidRegistryObject<>();
        //Pass in suppliers that are wrapped instead of direct references to the registry objects, so that when we update the registry object to
        // point to a new object it gets updated properly.
        ForgeFlowingFluid.Properties properties = new ForgeFlowingFluid.Properties(fluidRegistryObject::getStillFluid,
              fluidRegistryObject::getFlowingFluid, builder).bucket(fluidRegistryObject::getBucket).block(fluidRegistryObject::getBlock);
        //Update the references to objects that are retrieved from the deferred registers
        fluidRegistryObject.updateStill(fluidRegister.register(name, () -> new Source(properties)));
        fluidRegistryObject.updateFlowing(fluidRegister.register(flowingName, () -> new Flowing(properties)));
        fluidRegistryObject.updateBucket(itemRegister.register(bucketName, () -> bucketCreator.create(fluidRegistryObject::getStillFluid,
              ItemDeferredRegister.getMekBaseProperties().stacksTo(1).craftRemainder(Items.BUCKET))));
        //Note: The block properties used here is a copy of the ones for water
        fluidRegistryObject.updateBlock(blockRegister.register(name, () -> new LiquidBlock(fluidRegistryObject::getStillFluid,
              BlockBehaviour.Properties.of(Material.WATER).noCollission().strength(100.0F).noDrops())));
        allFluids.add(fluidRegistryObject);
        return fluidRegistryObject;
    }

    public void register(IEventBus bus) {
        blockRegister.register(bus);
        fluidRegister.register(bus);
        itemRegister.register(bus);
    }

    public List<FluidRegistryObject<?, ?, ?, ?>> getAllFluids() {
        return Collections.unmodifiableList(allFluids);
    }

    public void registerBucketDispenserBehavior() {
        for (FluidRegistryObject<?, ?, ?, ?> fluidRO : getAllFluids()) {
            DispenserBlock.registerBehavior(fluidRO.getBucket(), BUCKET_DISPENSE_BEHAVIOR);
        }
    }

    @FunctionalInterface
    public interface BucketCreator<BUCKET extends BucketItem> {

        BUCKET create(Supplier<? extends Fluid> supplier, Properties builder);
    }
}