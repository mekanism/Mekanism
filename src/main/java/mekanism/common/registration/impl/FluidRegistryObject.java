package mekanism.common.registration.impl;

import mekanism.api.text.IHasTextComponent;
import mekanism.api.text.IHasTranslationKey;
import mekanism.common.registration.MekanismDeferredHolder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidStackTemplate;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.transfer.fluid.FluidResource;

public class FluidRegistryObject<TYPE extends FluidType, STILL extends Fluid, FLOWING extends Fluid, BLOCK extends LiquidBlock, BUCKET extends BucketItem>
    extends MekanismDeferredHolder<Fluid, STILL> implements IHasTextComponent, IHasTranslationKey {

    private final DeferredHolder<FluidType, TYPE> fluidType;
    private final DeferredHolder<Fluid, FLOWING> flowing;
    private final ItemRegistryObject<BUCKET> bucket;
    private final DeferredHolder<Block, BLOCK> block;

    FluidRegistryObject(DeferredHolder<FluidType, TYPE> fluidType, DeferredHolder<Fluid, STILL> still, DeferredHolder<Fluid, FLOWING> flowing,
          ItemRegistryObject<BUCKET> bucket, DeferredHolder<Block, BLOCK> block) {
        //Default our fluid to being the still variant
        super(still.getKey());
        this.fluidType = fluidType;
        this.flowing = flowing;
        this.bucket = bucket;
        this.block = block;
    }

    public TYPE getFluidType() {
        return fluidType.get();
    }

    public DeferredHolder<Fluid, FLOWING> getFlowingFluid() {
        return flowing;
    }

    public BLOCK getBlock() {
        return block.get();
    }

    public ItemRegistryObject<BUCKET> getBucket() {
        return bucket;
    }

    public FluidResource asResource() {
        return FluidResource.of(this);
    }

    public FluidStack asStack(int amount) {
        return new FluidStack(get(), amount);
    }

    public FluidStackTemplate asTemplate(int amount) {
        //TODO - 26.1: Should we pass get() or this to the template constructor?
        return new FluidStackTemplate(get(), amount);
    }

    @Override
    public Component getTextComponent() {
        return getFluidType().getDescription(asStack(1));
    }

    @Override
    public String getTranslationKey() {
        return getFluidType().getDescriptionId();
    }
}