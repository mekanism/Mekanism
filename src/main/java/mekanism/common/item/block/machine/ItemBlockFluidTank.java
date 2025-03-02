package mekanism.common.item.block.machine;

import java.util.List;
import java.util.Optional;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.fluid.IMekanismFluidHandler;
import mekanism.api.security.IItemSecurityUtils;
import mekanism.api.text.EnumColor;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.basic.BlockFluidTank;
import mekanism.common.block.prefab.BlockTile;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.item.block.ItemBlockTooltip;
import mekanism.common.item.interfaces.IModeItem.IAttachmentBasedModeItem;
import mekanism.common.lib.security.ItemSecurityUtils;
import mekanism.common.registries.MekanismDataComponents;
import mekanism.common.tier.FluidTankTier;
import mekanism.common.tile.interfaces.IFluidContainerManager.ContainerEditMode;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.StorageUtils;
import mekanism.common.util.WorldUtils;
import mekanism.common.util.text.BooleanStateDisplay.OnOff;
import mekanism.common.util.text.BooleanStateDisplay.YesNo;
import mekanism.common.util.text.TextUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.animal.goat.Goat;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.common.SoundActions;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ItemBlockFluidTank extends ItemBlockTooltip<BlockTile<?, ?>> implements IAttachmentBasedModeItem<Boolean> {

    public ItemBlockFluidTank(BlockFluidTank block, Item.Properties properties) {
        super(block, true, properties.component(MekanismDataComponents.BUCKET_MODE, false)
              .component(MekanismDataComponents.EDIT_MODE, ContainerEditMode.BOTH)
        );
    }

    @NotNull
    @Override
    public FluidTankTier getTier() {
        return Attribute.getTier(getBlock(), FluidTankTier.class);
    }

    @Override
    protected void addStats(@NotNull ItemStack stack, @NotNull Item.TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        FluidTankTier tier = getTier();
        FluidStack fluidStack = StorageUtils.getStoredFluidFromAttachment(stack);
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
    protected void addTypeDetails(@NotNull ItemStack stack, @NotNull Item.TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        tooltip.add(MekanismLang.BUCKET_MODE.translateColored(EnumColor.INDIGO, YesNo.of(getMode(stack), true)));
        super.addTypeDetails(stack, context, tooltip, flag);
    }

    @NotNull
    @Override
    public InteractionResult interactLivingEntity(@NotNull ItemStack stack, @NotNull Player player, @NotNull LivingEntity entity, @NotNull InteractionHand hand) {
        if (getMode(stack) && !entity.isBaby()) {
            Level level = player.level();
            if (ItemSecurityUtils.get().tryClaimItem(level, player, stack)) {
                return InteractionResult.sidedSuccess(level.isClientSide);
            } else if (!IItemSecurityUtils.INSTANCE.canAccessOrDisplayError(player, stack)) {
                return InteractionResult.FAIL;
            } else if (stack.getCount() > 1) {
                //Skip if the item is stacked
                return InteractionResult.PASS;
            }
            if (entity instanceof Cow || entity instanceof Goat) {
                IExtendedFluidTank fluidTank = getExtendedFluidTank(stack);
                //Get the fluid tank for the stack
                if (fluidTank == null) {
                    //If there isn't one then there is something wrong with the stack, treat it as a normal stack and skip
                    return InteractionResult.PASS;
                }
                FluidStack milk = new FluidStack(NeoForgeMod.MILK.get(), FluidType.BUCKET_VOLUME);
                //Try to insert the fluid
                if (fluidTank.insert(milk, Action.EXECUTE, AutomationType.MANUAL).getAmount() < FluidType.BUCKET_VOLUME) {
                    player.playSound(entity instanceof Cow ? SoundEvents.COW_MILK : SoundEvents.GOAT_MILK, 1.0F, 1.0F);
                    return InteractionResult.sidedSuccess(level.isClientSide);
                }
                //Fail if we can't insert any
                return InteractionResult.FAIL;
            }
        }
        return InteractionResult.PASS;
    }

    @NotNull
    @Override
    public InteractionResult useOn(UseOnContext context) {
        //Note: We don't need to check the stack size here, as we only want to allow placing it if it isn't in bucket mode
        return context.getPlayer() == null || getMode(context.getItemInHand()) ? InteractionResult.PASS : super.useOn(context);
    }

    @NotNull
    @Override
    public InteractionResultHolder<ItemStack> use(@NotNull Level world, Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (getMode(stack)) {
            if (ItemSecurityUtils.get().tryClaimItem(world, player, stack)) {
                return InteractionResultHolder.sidedSuccess(stack, world.isClientSide);
            } else if (!IItemSecurityUtils.INSTANCE.canAccessOrDisplayError(player, stack)) {
                return InteractionResultHolder.fail(stack);
            } else if (stack.getCount() > 1) {
                //Skip if the item is stacked
                return InteractionResultHolder.pass(stack);
            }
            //TODO: At some point maybe try to reduce the duplicate code between this and the dispense behavior
            BlockHitResult result = getPlayerPOVHitResult(world, player, player.isShiftKeyDown() ? ClipContext.Fluid.NONE : ClipContext.Fluid.SOURCE_ONLY);
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
                        FluidStack fluidStack = new FluidStack(fluid, FluidType.BUCKET_VOLUME);
                        if (blockState.getBlock() instanceof BucketPickup bucketPickup && validFluid(fluidTank, fluidStack)) {
                            //If it can be picked up by a bucket, and we actually want to pick it up, do so to update the fluid type we are doing
                            // otherwise we assume the type from the fluid state is correct
                            ItemStack pickedUpStack = bucketPickup.pickupBlock(player, world, pos, blockState);
                            if (pickedUpStack.isEmpty()) {
                                //If the fluid can't be picked up, pass on doing anything
                                return InteractionResultHolder.pass(stack);
                            } else if (pickedUpStack.getItem() instanceof BucketItem bucket) {
                                //This isn't the best validation check given it may not return a bucket, but it is good enough for now
                                fluid = bucket.content;
                                //Update the fluid stack in case something somehow changed about the type
                                // making sure that we replace to heavy water if we got heavy water
                                fluidStack = new FluidStack(fluid, FluidType.BUCKET_VOLUME);
                                if (!validFluid(fluidTank, fluidStack)) {
                                    Mekanism.logger.warn("Fluid removed without successfully picking up. Fluid {} at {} in {} was valid, but after picking up was {}.",
                                          fluidState.getType(), pos, world.dimension().location(), fluid);
                                    return InteractionResultHolder.fail(stack);
                                }
                            }
                            sound = bucketPickup.getPickupSound(blockState);
                        }
                        if (validFluid(fluidTank, fluidStack)) {
                            uncheckedGrow(fluidTank, fluidStack);
                            //Play the bucket fill sound
                            WorldUtils.playFillSound(player, world, pos, fluidStack, sound.orElse(null));
                            world.gameEvent(player, GameEvent.FLUID_PICKUP, pos);
                            return InteractionResultHolder.success(stack);
                        }
                        return InteractionResultHolder.fail(stack);
                    }
                } else {
                    if (fluidTank.extract(FluidType.BUCKET_VOLUME, Action.SIMULATE, AutomationType.MANUAL).getAmount() < FluidType.BUCKET_VOLUME
                        || !player.mayUseItemAt(pos.relative(result.getDirection()), result.getDirection(), stack)) {
                        return InteractionResultHolder.fail(stack);
                    }
                    if (WorldUtils.tryPlaceContainedLiquid(player, world, pos, fluidTank.getFluid(), result.getDirection())) {
                        if (!player.isCreative()) {
                            //Manually shrink in case bucket volume is greater than tank input/output rate limit
                            MekanismUtils.logMismatchedStackSize(fluidTank.shrinkStack(FluidType.BUCKET_VOLUME, Action.EXECUTE), FluidType.BUCKET_VOLUME);
                        }
                        world.gameEvent(player, GameEvent.FLUID_PLACE, pos);
                        return InteractionResultHolder.success(stack);
                    }
                }
            }
        }
        return InteractionResultHolder.pass(stack);
    }

    //Used after simulation to insert the stack rather than just using the insert method to properly handle cases
    // where the stack for a single bucket may be above the tank's configured rate limit
    private void uncheckedGrow(IExtendedFluidTank fluidTank, FluidStack fluidStack) {
        if (getTier() != FluidTankTier.CREATIVE) {
            //No-OP creative handling as that is how insert would be handled for items
            if (fluidTank.isEmpty()) {
                fluidTank.setStack(fluidStack);
            } else {
                //Grow the stack
                MekanismUtils.logMismatchedStackSize(fluidTank.growStack(fluidStack.getAmount(), Action.EXECUTE), fluidStack.getAmount());
            }
        }
    }

    private static boolean validFluid(@NotNull IExtendedFluidTank fluidTank, @NotNull FluidStack fluidStack) {
        return !fluidStack.isEmpty() && fluidTank.insert(fluidStack, Action.SIMULATE, AutomationType.MANUAL).isEmpty();
    }

    private static IExtendedFluidTank getExtendedFluidTank(@NotNull ItemStack stack) {
        IFluidHandlerItem fluidHandlerItem = Capabilities.FLUID.getCapability(stack);
        if (fluidHandlerItem instanceof IMekanismFluidHandler fluidHandler) {
            return fluidHandler.getFluidTank(0, null);
        }
        return null;
    }

    @Override
    public DataComponentType<Boolean> getModeDataType() {
        return MekanismDataComponents.BUCKET_MODE.get();
    }

    @Override
    public Boolean getDefaultMode() {
        return Boolean.FALSE;
    }

    @Override
    public void changeMode(@NotNull Player player, @NotNull ItemStack stack, int shift, DisplayChange displayChange) {
        if (Math.abs(shift) % 2 == 1) {
            //We are changing by an odd amount, so toggle the mode
            boolean newState = !getMode(stack);
            setMode(stack, player, newState);
            displayChange.sendMessage(player, newState, s -> MekanismLang.BUCKET_MODE.translate(OnOff.of(s, true)));
        }
    }

    @NotNull
    @Override
    public Component getScrollTextComponent(@NotNull ItemStack stack) {
        return MekanismLang.BUCKET_MODE.translateColored(EnumColor.GRAY, OnOff.of(getMode(stack), true));
    }

    public static class FluidTankItemDispenseBehavior extends DefaultDispenseItemBehavior {

        public static final FluidTankItemDispenseBehavior INSTANCE = new FluidTankItemDispenseBehavior();

        private FluidTankItemDispenseBehavior() {
        }

        @NotNull
        @Override
        public ItemStack execute(@NotNull BlockSource source, @NotNull ItemStack stack) {
            if (stack.getCount() == 1 && stack.getItem() instanceof ItemBlockFluidTank tank && tank.getMode(stack)) {
                //If the fluid tank is in bucket mode allow for it to act as a bucket
                //Note: We don't use DispenseFluidContainer as we have more specific logic for determining if we want it to
                // act as a bucket that is emptying its contents or one that is picking up contents
                IExtendedFluidTank fluidTank = getExtendedFluidTank(stack);
                //Get the fluid tank for the stack
                if (fluidTank == null) {
                    //If there isn't one then there is something wrong with the stack, treat it as a normal stack and just eject it
                    return super.execute(source, stack);
                }
                Level world = source.level();
                BlockPos pos = source.pos().relative(source.state().getValue(DispenserBlock.FACING));
                //Note: we get the block state from the world so that we can get the proper block in case it is fluid logged
                BlockState blockState = world.getBlockState(pos);
                FluidState fluidState = blockState.getFluidState();
                Optional<SoundEvent> sound = Optional.empty();
                //If the fluid state in the world isn't empty and is a source try to pick it up otherwise try to dispense the stored fluid
                if (!fluidState.isEmpty() && fluidState.isSource()) {
                    //Just in case someone does weird things and has a fluid state that is empty and a source
                    // only allow collecting from non-empty sources
                    Fluid fluid = fluidState.getType();
                    FluidStack fluidStack = new FluidStack(fluid, FluidType.BUCKET_VOLUME);
                    if (blockState.getBlock() instanceof BucketPickup bucketPickup && validFluid(fluidTank, fluidStack)) {
                        //If it can be picked up by a bucket, and we actually want to pick it up, do so to update the fluid type we are doing
                        // otherwise we assume the type from the fluid state is correct
                        ItemStack pickedUpStack = bucketPickup.pickupBlock(null, world, pos, blockState);
                        if (pickedUpStack.isEmpty()) {
                            //If the fluid cannot be picked up, then eject the stack similar to how vanilla does for buckets
                            return super.execute(source, stack);
                        } else if (pickedUpStack.getItem() instanceof BucketItem bucket) {
                            //This isn't the best validation check given it may not return a bucket, but it is good enough for now
                            fluid = bucket.content;
                            //Update the fluid stack in case something somehow changed about the type
                            // making sure that we replace to heavy water if we got heavy water
                            fluidStack = new FluidStack(fluid, FluidType.BUCKET_VOLUME);
                            if (!validFluid(fluidTank, fluidStack)) {
                                Mekanism.logger.warn("Fluid removed without successfully picking up. Fluid {} at {} in {} was valid, but after picking up was {}.",
                                      fluidState.getType(), pos, world.dimension().location(), fluid);
                                //If we can't insert or extract it, then eject the stack similar to how vanilla does for buckets
                                return super.execute(source, stack);
                            }
                        }
                        sound = bucketPickup.getPickupSound(blockState);
                    }
                    if (validFluid(fluidTank, fluidStack)) {
                        tank.uncheckedGrow(fluidTank, fluidStack);
                        //Play the bucket fill sound
                        WorldUtils.playFillSound(null, world, pos, fluidStack, sound.orElse(null));
                        world.gameEvent(null, GameEvent.FLUID_PICKUP, pos);
                        //Success, don't dispense anything just return our resulting stack
                        return stack;
                    }
                } else if (fluidTank.extract(FluidType.BUCKET_VOLUME, Action.SIMULATE, AutomationType.MANUAL).getAmount() >= FluidType.BUCKET_VOLUME) {
                    if (WorldUtils.tryPlaceContainedLiquid(null, world, pos, fluidTank.getFluid(), null)) {
                        //Manually shrink in case bucket volume is greater than tank input/output rate limit
                        MekanismUtils.logMismatchedStackSize(fluidTank.shrinkStack(FluidType.BUCKET_VOLUME, Action.EXECUTE), FluidType.BUCKET_VOLUME);
                        world.gameEvent(null, GameEvent.FLUID_PLACE, pos);
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

    public abstract static class BasicCauldronInteraction implements CauldronInteraction {

        public static final BasicCauldronInteraction EMPTY = new BasicCauldronInteraction() {
            @Nullable
            private BlockState getState(FluidStack current) {
                if (current.is(Fluids.WATER)) {
                    return Blocks.WATER_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, 3);
                } else if (current.is(Fluids.LAVA)) {
                    return Blocks.LAVA_CAULDRON.defaultBlockState();
                }
                return null;
            }

            @NotNull
            @Override
            protected ItemInteractionResult interact(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player,
                  @NotNull InteractionHand hand, @NotNull ItemStack stack, @NotNull IExtendedFluidTank fluidTank) {
                FluidStack fluidStack = fluidTank.getFluid();
                BlockState endState = getState(fluidStack);
                if (endState != null && fluidTank.extract(FluidType.BUCKET_VOLUME, Action.SIMULATE, AutomationType.MANUAL).getAmount() >= FluidType.BUCKET_VOLUME) {
                    if (!level.isClientSide) {
                        if (!player.isCreative()) {
                            //Manually shrink in case bucket volume is greater than tank input/output rate limit
                            MekanismUtils.logMismatchedStackSize(fluidTank.shrinkStack(FluidType.BUCKET_VOLUME, Action.EXECUTE), FluidType.BUCKET_VOLUME);
                        }
                        player.awardStat(Stats.FILL_CAULDRON);
                        player.awardStat(Stats.ITEM_USED.get(stack.getItem()));
                        level.setBlockAndUpdate(pos, endState);
                        SoundEvent emptySound = fluidStack.getFluidType().getSound(player, level, pos, SoundActions.BUCKET_EMPTY);
                        if (emptySound != null) {
                            level.playSound(null, pos, emptySound, SoundSource.BLOCKS, 1.0F, 1.0F);
                        }
                        level.gameEvent(null, GameEvent.FLUID_PLACE, pos);
                    }
                    return ItemInteractionResult.sidedSuccess(level.isClientSide);
                }
                return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
            }
        };

        @NotNull
        @Override
        public final ItemInteractionResult interact(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player,
              @NotNull InteractionHand hand, @NotNull ItemStack stack) {
            if (stack.getCount() == 1 && stack.getItem() instanceof ItemBlockFluidTank tank && tank.getMode(stack)) {
                //If the fluid tank is in bucket mode allow for it to act as a bucket
                IExtendedFluidTank fluidTank = getExtendedFluidTank(stack);
                //Get the fluid tank for the stack
                if (fluidTank == null) {
                    //If there isn't one then there is something wrong with the stack, treat it as a normal stack and skip
                    return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
                }
                return interact(state, level, pos, player, hand, stack, fluidTank);
            }
            //Otherwise skip
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        @NotNull
        protected abstract ItemInteractionResult interact(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player,
              @NotNull InteractionHand hand, @NotNull ItemStack stack, @NotNull IExtendedFluidTank fluidTank);
    }

    public static class BasicDrainCauldronInteraction extends BasicCauldronInteraction {

        public static final BasicDrainCauldronInteraction WATER = new BasicDrainCauldronInteraction(Fluids.WATER) {
            @NotNull
            @Override
            protected ItemInteractionResult interact(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player,
                  @NotNull InteractionHand hand, @NotNull ItemStack stack, @NotNull IExtendedFluidTank fluidTank) {
                if (state.getValue(LayeredCauldronBlock.LEVEL) == 3) {
                    //When emptying a water cauldron make sure it is full and just ignore handling of partial transfers
                    // as while we can handle them, they come with the added complication of deciding what value to give bottles
                    return super.interact(state, level, pos, player, hand, stack, fluidTank);
                }
                return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
            }
        };
        public static final BasicDrainCauldronInteraction LAVA = new BasicDrainCauldronInteraction(Fluids.LAVA);

        private final Fluid type;

        private BasicDrainCauldronInteraction(Fluid type) {
            this.type = type;
        }

        @NotNull
        @Override
        protected ItemInteractionResult interact(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player,
              @NotNull InteractionHand hand, @NotNull ItemStack stack, @NotNull IExtendedFluidTank fluidTank) {
            FluidStack fluidStack = new FluidStack(type, FluidType.BUCKET_VOLUME);
            FluidStack remainder = fluidTank.insert(fluidStack, Action.SIMULATE, AutomationType.MANUAL);
            if (remainder.isEmpty()) {
                //We can fit all the fluid we would be removing
                if (!level.isClientSide) {
                    if (!player.isCreative()) {
                        ((ItemBlockFluidTank) stack.getItem()).uncheckedGrow(fluidTank, fluidStack);
                    }
                    player.awardStat(Stats.USE_CAULDRON);
                    player.awardStat(Stats.ITEM_USED.get(stack.getItem()));
                    level.setBlockAndUpdate(pos, Blocks.CAULDRON.defaultBlockState());
                    SoundEvent fillSound = fluidStack.getFluidType().getSound(null, level, pos, SoundActions.BUCKET_FILL);
                    if (fillSound != null) {
                        level.playSound(null, pos, fillSound, SoundSource.BLOCKS, 1.0F, 1.0F);
                    }
                    level.gameEvent(null, GameEvent.FLUID_PICKUP, pos);
                }
                return ItemInteractionResult.sidedSuccess(level.isClientSide);
            }
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
    }
}