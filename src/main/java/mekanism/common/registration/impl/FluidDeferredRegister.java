package mekanism.common.registration.impl;

import net.minecraft.block.Block;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.block.material.Material;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.BucketItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.fluids.ForgeFlowingFluid.Flowing;
import net.minecraftforge.fluids.ForgeFlowingFluid.Source;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class FluidDeferredRegister {

    private final String modid;

    private final DeferredRegister<Fluid> fluidRegister;
    private final DeferredRegister<Block> blockRegister;
    private final DeferredRegister<Item> itemRegister;

    public FluidDeferredRegister(String modid) {
        this.modid = modid;
        blockRegister = new DeferredRegister<>(ForgeRegistries.BLOCKS, modid);
        fluidRegister = new DeferredRegister<>(ForgeRegistries.FLUIDS, modid);
        itemRegister = new DeferredRegister<>(ForgeRegistries.ITEMS, modid);
    }

    public FluidRegistryObject<Source, Flowing, FlowingFluidBlock, BucketItem> register(String name, FluidAttributes.Builder builder) {
        String flowingName = "flowing_" + name;
        String bucketName = name + "_bucket";
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
        //TODO: Allow setting custom block properties?
        //Note: The block properties used here is a copy of the ones for water
        fluidRegistryObject.updateBlock(blockRegister.register(name, () -> new FlowingFluidBlock(fluidRegistryObject::getStillFluid,
              Block.Properties.create(Material.WATER).doesNotBlockMovement().hardnessAndResistance(100.0F).noDrops())));
        return fluidRegistryObject;
    }

    public void register(IEventBus bus) {
        blockRegister.register(bus);
        fluidRegister.register(bus);
        itemRegister.register(bus);
    }
}