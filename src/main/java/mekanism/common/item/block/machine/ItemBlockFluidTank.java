package mekanism.common.item.block.machine;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import javax.annotation.Nonnull;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.DataHandlerUtils;
import mekanism.api.NBTConstants;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.fluid.IMekanismFluidHandler;
import mekanism.api.text.EnumColor;
import mekanism.client.render.RenderPropertiesProvider;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.basic.BlockFluidTank;
import mekanism.common.capabilities.ItemCapabilityWrapper;
import mekanism.common.capabilities.fluid.BasicFluidTank;
import mekanism.common.capabilities.fluid.item.RateLimitFluidHandler;
import mekanism.common.config.MekanismConfig;
import mekanism.common.item.block.ItemBlockTooltip;
import mekanism.common.item.interfaces.IItemSustainedInventory;
import mekanism.common.item.interfaces.IModeItem;
import mekanism.common.lib.security.ISecurityItem;
import mekanism.common.registration.impl.ItemDeferredRegister;
import mekanism.common.tier.FluidTankTier;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.SecurityUtils;
import mekanism.common.util.StorageUtils;
import mekanism.common.util.WorldUtils;
import mekanism.common.util.text.BooleanStateDisplay.OnOff;
import mekanism.common.util.text.BooleanStateDisplay.YesNo;
import mekanism.common.util.text.TextUtils;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.core.NonNullList;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraftforge.client.IItemRenderProperties;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.registries.ForgeRegistries;

public class ItemBlockFluidTank extends ItemBlockTooltip<BlockFluidTank> implements IItemSustainedInventory, ISecurityItem, IModeItem {

    public ItemBlockFluidTank(BlockFluidTank block) {
        super(block, true, ItemDeferredRegister.getMekBaseProperties().stacksTo(1));
    }

    @Override
    public void initializeClient(@Nonnull Consumer<IItemRenderProperties> consumer) {
        consumer.accept(RenderPropertiesProvider.fluidTank());
    }

    @Nonnull
    @Override
    public FluidTankTier getTier() {
        return Attribute.getTier(getBlock(), FluidTankTier.class);
    }

    @Override
    protected void addStats(@Nonnull ItemStack stack, Level world, @Nonnull List<Component> tooltip, @Nonnull TooltipFlag flag) {
        FluidTankTier tier = getTier();
        FluidStack fluidStack = StorageUtils.getStoredFluidFromNBT(stack);
        if (fluidStack.isEmpty()) {
            tooltip.add(MekanismLang.EMPTY.translateColored(EnumColor.DARK_RED));
        } else if (tier == FluidTankTier.CREATIVE) {
            tooltip.add(MekanismLang.GENERIC_STORED.translateColored(EnumColor.PINK, fluidStack, EnumColor.GRAY, MekanismLang.INFINITE));
        } else {
            tooltip.add(MekanismLang.GENERIC_STORED_MB.translateColored(EnumColor.PINK, fluidStack, EnumColor.GRAY, TextUtils.format(fluidStack.getAmount())));
        }
        if (tier == FluidTankTier.CREATIVE) {
            tooltip.add(MekanismLang.CAPACITY.translateColored(EnumColor.INDIGO, EnumColor.GRAY, MekanismLang.INFINITE));
        } else {
            tooltip.add(MekanismLang.CAPACITY_MB.translateColored(EnumColor.INDIGO, EnumColor.GRAY, TextUtils.format(tier.getStorage())));
        }
    }

    @Override
    protected void addDetails(@Nonnull ItemStack stack, Level world, @Nonnull List<Component> tooltip, @Nonnull TooltipFlag flag) {
        SecurityUtils.addSecurityTooltip(stack, tooltip);
        tooltip.add(MekanismLang.BUCKET_MODE.translateColored(EnumColor.INDIGO, YesNo.of(getBucketMode(stack))));
        tooltip.add(MekanismLang.HAS_INVENTORY.translateColored(EnumColor.AQUA, EnumColor.GRAY, YesNo.of(hasInventory(stack))));
    }

    @Override
    public void fillItemCategory(@Nonnull CreativeModeTab group, @Nonnull NonNullList<ItemStack> items) {
        super.fillItemCategory(group, items);
        if (allowdedIn(group)) {
            FluidTankTier tier = Attribute.getTier(getBlock(), FluidTankTier.class);
            if (tier == FluidTankTier.CREATIVE && MekanismConfig.general.prefilledFluidTanks.get()) {
                int capacity = tier.getStorage();
                for (Fluid fluid : ForgeRegistries.FLUIDS.getValues()) {
                    //Only add sources
                    if (fluid.isSource(fluid.defaultFluidState())) {
                        IExtendedFluidTank dummyTank = BasicFluidTank.create(capacity, null);
                        //Manually handle filling it as capabilities are not necessarily loaded yet
                        dummyTank.setStack(new FluidStack(fluid, dummyTank.getCapacity()));
                        ItemStack stack = new ItemStack(this);
                        ItemDataUtils.setList(stack, NBTConstants.FLUID_TANKS, DataHandlerUtils.writeContainers(Collections.singletonList(dummyTank)));
                        items.add(stack);
                    }
                }
            }
        }
    }

    @Nonnull
    @Override
    public InteractionResult useOn(UseOnContext context) {
        return context.getPlayer() == null || getBucketMode(context.getItemInHand()) ? InteractionResult.PASS : super.useOn(context);
    }

    @Nonnull
    @Override
    public InteractionResultHolder<ItemStack> use(@Nonnull Level world, Player player, @Nonnull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (getBucketMode(stack)) {
            if (getOwnerUUID(stack) == null) {
                if (!world.isClientSide) {
                    SecurityUtils.claimItem(player, stack);
                }
                return InteractionResultHolder.sidedSuccess(stack, world.isClientSide);
            } else if (SecurityUtils.canAccess(player, stack)) {
                //TODO: At some point maybe try to reduce the duplicate code between this and the dispense behavior
                BlockHitResult result = getPlayerPOVHitResult(world, player, !player.isShiftKeyDown() ? ClipContext.Fluid.SOURCE_ONLY : ClipContext.Fluid.NONE);
                //It can be null if there is nothing in range
                if (result.getType() == Type.BLOCK) {
                    BlockPos pos = result.getBlockPos();
                    if (!world.mayInteract(player, pos)) {
                        return InteractionResultHolder.fail(stack);
                    }
                    IExtendedFluidTank fluidTank = getExtendedFluidTank(stack);
                    if (fluidTank == null) {
                        //If something went wrong, and we don't have a fluid tank fail
                        return InteractionResultHolder.fail(stack);
                    }
                    if (!player.isShiftKeyDown()) {
                        if (!player.mayUseItemAt(pos, result.getDirection(), stack)) {
                            return InteractionResultHolder.fail(stack);
                        }
                        //Note: we get the block state from the world so that we can get the proper block in case it is fluid logged
                        BlockState blockState = world.getBlockState(pos);
                        FluidState fluidState = blockState.getFluidState();
                        Optional<SoundEvent> sound = Optional.empty();
                        if (!fluidState.isEmpty() && fluidState.isSource()) {
                            //Just in case someone does weird things and has a fluid state that is empty and a source
                            // only allow collecting from non-empty sources
                            Fluid fluid = fluidState.getType();
                            FluidStack fluidStack = new FluidStack(fluid, FluidAttributes.BUCKET_VOLUME);
                            Block block = blockState.getBlock();
                            if (block instanceof IFluidBlock fluidBlock) {
                                fluidStack = fluidBlock.drain(world, pos, FluidAction.SIMULATE);
                                if (!validFluid(fluidTank, fluidStack)) {
                                    //If the fluid is not valid, pass on doing anything
                                    return InteractionResultHolder.pass(stack);
                                }
                                //Actually drain it
                                fluidStack = fluidBlock.drain(world, pos, FluidAction.EXECUTE);
                            } else if (block instanceof BucketPickup bucketPickup && validFluid(fluidTank, fluidStack)) {
                                //If it can be picked up by a bucket, and we actually want to pick it up, do so to update the fluid type we are doing
                                // otherwise we assume the type from the fluid state is correct
                                ItemStack pickedUpStack = bucketPickup.pickupBlock(world, pos, blockState);
                                if (pickedUpStack.isEmpty()) {
                                    //If the fluid can't be picked up, pass on doing anything
                                    return InteractionResultHolder.pass(stack);
                                } else if (pickedUpStack.getItem() instanceof BucketItem bucket) {
                                    //This isn't the best validation check given it may not return a bucket, but it is good enough for now
                                    fluid = bucket.getFluid();
                                    //Update the fluid stack in case something somehow changed about the type
                                    // making sure that we replace to heavy water if we got heavy water
                                    fluidStack = new FluidStack(fluid, FluidAttributes.BUCKET_VOLUME);
                                    if (!validFluid(fluidTank, fluidStack)) {
                                        Mekanism.logger.warn("Fluid removed without successfully picking up. Fluid {} at {} in {} was valid, but after picking up was {}.",
                                              fluidState.getType().getRegistryName(), pos, world.dimension().location(), fluid.getRegistryName());
                                        return InteractionResultHolder.fail(stack);
                                    }
                                }
                                sound = bucketPickup.getPickupSound(blockState);
                            }
                            if (validFluid(fluidTank, fluidStack)) {
                                if (fluidTank.isEmpty()) {
                                    fluidTank.setStack(fluidStack);
                                } else {
                                    //Grow the stack
                                    MekanismUtils.logMismatchedStackSize(fluidTank.growStack(fluidStack.getAmount(), Action.EXECUTE), fluidStack.getAmount());
                                }
                                //Play the bucket fill sound
                                WorldUtils.playFillSound(player, world, pos, fluidStack, sound.orElse(null));
                                world.gameEvent(player, GameEvent.FLUID_PICKUP, pos);
                                return InteractionResultHolder.success(stack);
                            }
                            return InteractionResultHolder.fail(stack);
                        }
                    } else {
                        if (fluidTank.extract(FluidAttributes.BUCKET_VOLUME, Action.SIMULATE, AutomationType.MANUAL).getAmount() < FluidAttributes.BUCKET_VOLUME
                            || !player.mayUseItemAt(pos.relative(result.getDirection()), result.getDirection(), stack)) {
                            return InteractionResultHolder.fail(stack);
                        }
                        if (WorldUtils.tryPlaceContainedLiquid(player, world, pos, fluidTank.getFluid(), result.getDirection())) {
                            if (!player.isCreative()) {
                                MekanismUtils.logMismatchedStackSize(fluidTank.shrinkStack(FluidAttributes.BUCKET_VOLUME, Action.EXECUTE), FluidAttributes.BUCKET_VOLUME);
                            }
                            world.gameEvent(player, GameEvent.FLUID_PLACE, pos);
                            return InteractionResultHolder.success(stack);
                        }
                    }
                }
            } else {
                if (!world.isClientSide) {
                    SecurityUtils.displayNoAccess(player);
                }
                return InteractionResultHolder.fail(stack);
            }
        }
        return InteractionResultHolder.pass(stack);
    }

    private static boolean validFluid(@Nonnull IExtendedFluidTank fluidTank, @Nonnull FluidStack fluidStack) {
        return !fluidStack.isEmpty() && fluidTank.insert(fluidStack, Action.SIMULATE, AutomationType.MANUAL).isEmpty();
    }

    private static IExtendedFluidTank getExtendedFluidTank(@Nonnull ItemStack stack) {
        Optional<IFluidHandlerItem> capability = FluidUtil.getFluidHandler(stack).resolve();
        if (capability.isPresent()) {
            IFluidHandlerItem fluidHandlerItem = capability.get();
            if (fluidHandlerItem instanceof IMekanismFluidHandler fluidHandler) {
                return fluidHandler.getFluidTank(0, null);
            }
        }
        return null;
    }

    public void setBucketMode(ItemStack itemStack, boolean bucketMode) {
        ItemDataUtils.setBoolean(itemStack, NBTConstants.BUCKET_MODE, bucketMode);
    }

    public boolean getBucketMode(ItemStack itemStack) {
        return ItemDataUtils.getBoolean(itemStack, NBTConstants.BUCKET_MODE);
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, CompoundTag nbt) {
        return new ItemCapabilityWrapper(stack, RateLimitFluidHandler.create(getTier()));
    }

    @Override
    public void changeMode(@Nonnull Player player, @Nonnull ItemStack stack, int shift, boolean displayChangeMessage) {
        if (Math.abs(shift) % 2 == 1) {
            //We are changing by an odd amount, so toggle the mode
            boolean newState = !getBucketMode(stack);
            setBucketMode(stack, newState);
            if (displayChangeMessage) {
                player.sendMessage(MekanismUtils.logFormat(MekanismLang.BUCKET_MODE.translate(OnOff.of(newState, true))), Util.NIL_UUID);
            }
        }
    }

    @Nonnull
    @Override
    public Component getScrollTextComponent(@Nonnull ItemStack stack) {
        return MekanismLang.BUCKET_MODE.translateColored(EnumColor.GRAY, OnOff.of(getBucketMode(stack), true));
    }

    public static class FluidTankItemDispenseBehavior extends DefaultDispenseItemBehavior {

        public static final FluidTankItemDispenseBehavior INSTANCE = new FluidTankItemDispenseBehavior();

        private FluidTankItemDispenseBehavior() {
        }

        @Nonnull
        @Override
        public ItemStack execute(@Nonnull BlockSource source, @Nonnull ItemStack stack) {
            if (stack.getItem() instanceof ItemBlockFluidTank tank && tank.getBucketMode(stack)) {
                //If the fluid tank is in bucket mode allow for it to act as a bucket
                //Note: We don't use DispenseFluidContainer as we have more specific logic for determining if we want it to
                // act as a bucket that is emptying its contents or one that is picking up contents
                IExtendedFluidTank fluidTank = getExtendedFluidTank(stack);
                //Get the fluid tank for the stack
                if (fluidTank == null) {
                    //If there isn't one then there is something wrong with the stack, treat it as a normal stack and just eject it
                    return super.execute(source, stack);
                }
                Level world = source.getLevel();
                BlockPos pos = source.getPos().relative(source.getBlockState().getValue(DispenserBlock.FACING));
                //Note: we get the block state from the world so that we can get the proper block in case it is fluid logged
                BlockState blockState = world.getBlockState(pos);
                FluidState fluidState = blockState.getFluidState();
                Optional<SoundEvent> sound = Optional.empty();
                //If the fluid state in the world isn't empty and is a source try to pick it up otherwise try to dispense the stored fluid
                if (!fluidState.isEmpty() && fluidState.isSource()) {
                    //Just in case someone does weird things and has a fluid state that is empty and a source
                    // only allow collecting from non-empty sources
                    Fluid fluid = fluidState.getType();
                    FluidStack fluidStack = new FluidStack(fluid, FluidAttributes.BUCKET_VOLUME);
                    Block block = blockState.getBlock();
                    if (block instanceof IFluidBlock fluidBlock) {
                        fluidStack = fluidBlock.drain(world, pos, FluidAction.SIMULATE);
                        if (!validFluid(fluidTank, fluidStack)) {
                            //If the fluid is not valid, then eject the stack similar to how vanilla does for buckets
                            return super.execute(source, stack);
                        }
                        //Actually drain it
                        fluidStack = fluidBlock.drain(world, pos, FluidAction.EXECUTE);
                    } else if (block instanceof BucketPickup bucketPickup && validFluid(fluidTank, fluidStack)) {
                        //If it can be picked up by a bucket, and we actually want to pick it up, do so to update the fluid type we are doing
                        // otherwise we assume the type from the fluid state is correct
                        ItemStack pickedUpStack = bucketPickup.pickupBlock(world, pos, blockState);
                        if (pickedUpStack.isEmpty()) {
                            //If the fluid cannot be picked up, then eject the stack similar to how vanilla does for buckets
                            return super.execute(source, stack);
                        } else if (pickedUpStack.getItem() instanceof BucketItem bucket) {
                            //This isn't the best validation check given it may not return a bucket, but it is good enough for now
                            fluid = bucket.getFluid();
                            //Update the fluid stack in case something somehow changed about the type
                            // making sure that we replace to heavy water if we got heavy water
                            fluidStack = new FluidStack(fluid, FluidAttributes.BUCKET_VOLUME);
                            if (!validFluid(fluidTank, fluidStack)) {
                                Mekanism.logger.warn("Fluid removed without successfully picking up. Fluid {} at {} in {} was valid, but after picking up was {}.",
                                      fluidState.getType().getRegistryName(), pos, world.dimension().location(), fluid.getRegistryName());
                                //If we can't insert or extract it, then eject the stack similar to how vanilla does for buckets
                                return super.execute(source, stack);
                            }
                        }
                        sound = bucketPickup.getPickupSound(blockState);
                    }
                    if (validFluid(fluidTank, fluidStack)) {
                        if (fluidTank.isEmpty()) {
                            fluidTank.setStack(fluidStack);
                        } else {
                            //Grow the stack
                            MekanismUtils.logMismatchedStackSize(fluidTank.growStack(fluidStack.getAmount(), Action.EXECUTE), fluidStack.getAmount());
                        }
                        //Play the bucket fill sound
                        WorldUtils.playFillSound(null, world, pos, fluidStack, sound.orElse(null));
                        world.gameEvent(GameEvent.FLUID_PICKUP, pos);
                        //Success, don't dispense anything just return our resulting stack
                        return stack;
                    }
                } else if (fluidTank.extract(FluidAttributes.BUCKET_VOLUME, Action.SIMULATE, AutomationType.MANUAL).getAmount() >= FluidAttributes.BUCKET_VOLUME) {
                    if (WorldUtils.tryPlaceContainedLiquid(null, world, pos, fluidTank.getFluid(), null)) {
                        MekanismUtils.logMismatchedStackSize(fluidTank.shrinkStack(FluidAttributes.BUCKET_VOLUME, Action.EXECUTE), FluidAttributes.BUCKET_VOLUME);
                        world.gameEvent(GameEvent.FLUID_PLACE, pos);
                        //Success, don't dispense anything just return our resulting stack
                        return stack;
                    }
                }
                //If we can't insert or extract it, then eject the stack similar to how vanilla does for buckets
            }
            //Otherwise, eject it as a normal item
            return super.execute(source, stack);
        }
    }
}