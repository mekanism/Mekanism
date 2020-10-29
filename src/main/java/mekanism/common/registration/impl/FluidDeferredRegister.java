package mekanism.common.registration.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;
import javax.annotation.Nonnull;
import mekanism.common.Mekanism;
import mekanism.common.base.IChemicalConstant;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.block.material.Material;
import net.minecraft.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.dispenser.IDispenseItemBehavior;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.BucketItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
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
    private static final IDispenseItemBehavior BUCKET_DISPENSE_BEHAVIOR = new DefaultDispenseItemBehavior() {
        @Nonnull
        @Override
        public ItemStack dispenseStack(@Nonnull IBlockSource source, @Nonnull ItemStack stack) {
            World world = source.getWorld();
            BucketItem bucket = (BucketItem) stack.getItem();
            BlockPos pos = source.getBlockPos().offset(source.getBlockState().get(DispenserBlock.FACING));
            if (bucket.tryPlaceContainedLiquid(null, world, pos, null)) {
                bucket.onLiquidPlaced(world, stack, pos);
                return new ItemStack(Items.BUCKET);
            }
            return super.dispenseStack(source, stack);
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

    public FluidRegistryObject<Source, Flowing, FlowingFluidBlock, BucketItem> registerLiquidChemical(IChemicalConstant constants) {
        int density = Math.round(constants.getDensity());
        return register(constants.getName(), fluidAttributes -> fluidAttributes
              .color(constants.getColor())
              .temperature(Math.round(constants.getTemperature()))
              .density(density)
              .viscosity(density)
              .luminosity(constants.getLuminosity()));
    }

    public FluidRegistryObject<Source, Flowing, FlowingFluidBlock, BucketItem> register(String name, UnaryOperator<Builder> fluidAttributes) {
        return register(name, fluidAttributes.apply(FluidAttributes.builder(Mekanism.rl("liquid/liquid"), Mekanism.rl("liquid/liquid_flow"))));
    }

    public FluidRegistryObject<Source, Flowing, FlowingFluidBlock, BucketItem> register(String name, FluidAttributes.Builder builder) {
        String flowingName = "flowing_" + name;
        String bucketName = name + "_bucket";
        //For now all our fluids use the same "overlay" for being against glass as vanilla water.
        builder.overlay(OVERLAY);
        //Create the registry object with dummy entries that we can use as part of the supplier but that works as use in suppliers
        FluidRegistryObject<Source, Flowing, FlowingFluidBlock, BucketItem> fluidRegistryObject = new FluidRegistryObject<>(modid, name);
        //Pass in suppliers that are wrapped instead of direct references to the registry objects, so that when we update the registry object to
        // point to a new object it gets updated properly.
        ForgeFlowingFluid.Properties properties = new ForgeFlowingFluid.Properties(fluidRegistryObject::getStillFluid,
              fluidRegistryObject::getFlowingFluid, builder).bucket(fluidRegistryObject::getBucket).block(fluidRegistryObject::getBlock);
        //Update the references to objects that are retrieved from the deferred registers
        fluidRegistryObject.updateStill(fluidRegister.register(name, () -> new Source(properties)));
        fluidRegistryObject.updateFlowing(fluidRegister.register(flowingName, () -> new Flowing(properties)));
        fluidRegistryObject.updateBucket(itemRegister.register(bucketName, () -> new BucketItem(fluidRegistryObject::getStillFluid,
              ItemDeferredRegister.getMekBaseProperties().maxStackSize(1).containerItem(Items.BUCKET))));
        //Note: The block properties used here is a copy of the ones for water
        fluidRegistryObject.updateBlock(blockRegister.register(name, () -> new FlowingFluidBlock(fluidRegistryObject::getStillFluid,
              AbstractBlock.Properties.create(Material.WATER).doesNotBlockMovement().hardnessAndResistance(100.0F).noDrops())));
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
            DispenserBlock.registerDispenseBehavior(fluidRO.getBucket(), BUCKET_DISPENSE_BEHAVIOR);
        }
    }
}