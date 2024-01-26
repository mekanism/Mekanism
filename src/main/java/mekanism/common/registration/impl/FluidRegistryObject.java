package mekanism.common.registration.impl;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.providers.IFluidProvider;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredHolder;

@NothingNullByDefault
public class FluidRegistryObject<TYPE extends FluidType, STILL extends Fluid, FLOWING extends Fluid, BLOCK extends LiquidBlock, BUCKET extends BucketItem>
      implements IFluidProvider {

    private final DeferredHolder<FluidType, TYPE> fluidType;
    private final DeferredHolder<Fluid, STILL> still;
    private final DeferredHolder<Fluid, FLOWING> flowing;
    private final DeferredHolder<Item, BUCKET> bucket;
    private final DeferredHolder<Block, BLOCK> block;

    FluidRegistryObject(DeferredHolder<FluidType, TYPE> fluidType, DeferredHolder<Fluid, STILL> still, DeferredHolder<Fluid, FLOWING> flowing,
          DeferredHolder<Item, BUCKET> bucket, DeferredHolder<Block, BLOCK> block) {
        this.fluidType = fluidType;
        this.still = still;
        this.flowing = flowing;
        this.bucket = bucket;
        this.block = block;
    }

    public TYPE getFluidType() {
        return fluidType.get();
    }

    public STILL getStillFluid() {
        return still.get();
    }

    public FLOWING getFlowingFluid() {
        return flowing.get();
    }

    public BLOCK getBlock() {
        return block.get();
    }

    public BUCKET getBucket() {
        return bucket.get();
    }

    @Override
    public STILL getFluid() {
        //Default our fluid to being the still variant
        return getStillFluid();
    }
}