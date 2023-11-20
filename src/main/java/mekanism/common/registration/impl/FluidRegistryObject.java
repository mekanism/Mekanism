package mekanism.common.registration.impl;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.providers.IFluidProvider;
import mekanism.common.registration.MekanismDeferredHolder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

@NothingNullByDefault
public class FluidRegistryObject<TYPE extends FluidType, STILL extends Fluid, FLOWING extends Fluid, BLOCK extends LiquidBlock, BUCKET extends BucketItem>
      implements IFluidProvider {

    final MekanismDeferredHolder<FluidType, TYPE> fluidTypeRO;
    final MekanismDeferredHolder<Fluid, STILL> stillRO;
    final MekanismDeferredHolder<Fluid, FLOWING> flowingRO;
    final MekanismDeferredHolder<Block, BLOCK> blockRO;
    final MekanismDeferredHolder<Item, BUCKET> bucketRO;

    FluidRegistryObject(ResourceLocation key) {
        this.fluidTypeRO = new MekanismDeferredHolder<>(NeoForgeRegistries.Keys.FLUID_TYPES, key);
        this.stillRO = new MekanismDeferredHolder<>(Registries.FLUID, key);
        this.flowingRO = new MekanismDeferredHolder<>(Registries.FLUID, key.withPrefix("flowing_"));
        this.blockRO = new MekanismDeferredHolder<>(Registries.BLOCK, key);
        this.bucketRO = new MekanismDeferredHolder<>(Registries.ITEM, key.withSuffix("_bucket"));
    }

    public TYPE getFluidType() {
        return fluidTypeRO.get();
    }

    public STILL getStillFluid() {
        return stillRO.get();
    }

    public FLOWING getFlowingFluid() {
        return flowingRO.get();
    }

    public BLOCK getBlock() {
        return blockRO.get();
    }

    public BUCKET getBucket() {
        return bucketRO.get();
    }

    @Override
    public STILL getFluid() {
        //Default our fluid to being the still variant
        return getStillFluid();
    }
}