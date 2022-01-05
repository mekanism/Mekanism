package mekanism.common.registration.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;
import javax.annotation.Nonnull;
import mekanism.common.Mekanism;
import mekanism.common.base.IChemicalConstant;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.material.Material;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.BlockSource;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
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
    //Copy of/based off of vanilla's lava/water bucket dispense behavior
    private static final DispenseItemBehavior BUCKET_DISPENSE_BEHAVIOR = new DefaultDispenseItemBehavior() {
        @Nonnull
        @Override
        public ItemStack execute(@Nonnull BlockSource source, @Nonnull ItemStack stack) {
            Level world = source.getLevel();
            BucketItem bucket = (BucketItem) stack.getItem();
            BlockPos pos = source.getPos().relative(source.getBlockState().getValue(DispenserBlock.FACING));
            if (bucket.emptyContents(null, world, pos, null)) {
                bucket.checkExtraContent(null, world, stack, pos);
                return new ItemStack(Items.BUCKET);
            }
            return super.execute(source, stack);
        }
    };

    private final List<FluidRegistryObject<?, ?, ?, ?>> allFluids = new ArrayList<>();

    private final String modid;

    private final DeferredRegister<Fluid> fluidRegister;
    private final DeferredRegister<Block> blockRegister;
    private final DeferredRegister<Item> itemRegister;

    public FluidDeferredRegister(String modid) {
        this.modid = modid;
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
        return register(name, fluidAttributes.apply(FluidAttributes.builder(Mekanism.rl("liquid/liquid"), Mekanism.rl("liquid/liquid_flow"))));
    }

    public FluidRegistryObject<Source, Flowing, LiquidBlock, BucketItem> register(String name, FluidAttributes.Builder builder) {
        //TODO - 1.18: Set fill and empty sounds on the attributes .sound(SoundEvents.BUCKET_FILL, SoundEvents.BUCKET_EMPTY)
        // need to decide where we want to assign it in case we ever decide to add custom sounds for some fluids, we won't want
        // them overwritten
        String flowingName = "flowing_" + name;
        String bucketName = name + "_bucket";
        //For now all our fluids use the same "overlay" for being against glass as vanilla water.
        builder.overlay(OVERLAY);
        //Create the registry object with dummy entries that we can use as part of the supplier but that works as use in suppliers
        FluidRegistryObject<Source, Flowing, LiquidBlock, BucketItem> fluidRegistryObject = new FluidRegistryObject<>(modid, name);
        //Pass in suppliers that are wrapped instead of direct references to the registry objects, so that when we update the registry object to
        // point to a new object it gets updated properly.
        ForgeFlowingFluid.Properties properties = new ForgeFlowingFluid.Properties(fluidRegistryObject::getStillFluid,
              fluidRegistryObject::getFlowingFluid, builder).bucket(fluidRegistryObject::getBucket).block(fluidRegistryObject::getBlock);
        //Update the references to objects that are retrieved from the deferred registers
        fluidRegistryObject.updateStill(fluidRegister.register(name, () -> new Source(properties)));
        fluidRegistryObject.updateFlowing(fluidRegister.register(flowingName, () -> new Flowing(properties)));
        fluidRegistryObject.updateBucket(itemRegister.register(bucketName, () -> new BucketItem(fluidRegistryObject::getStillFluid,
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
        return allFluids;
    }

    public void registerBucketDispenserBehavior() {
        for (FluidRegistryObject<?, ?, ?, ?> fluidRO : getAllFluids()) {
            DispenserBlock.registerBehavior(fluidRO.getBucket(), BUCKET_DISPENSE_BEHAVIOR);
        }
    }
}