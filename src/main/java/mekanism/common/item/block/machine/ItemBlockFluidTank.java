package mekanism.common.item.block.machine;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import mekanism.api.resource.LargeResourceStack;
import mekanism.api.security.IItemSecurityUtils;
import mekanism.api.text.EnumColor;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.attachments.containers.type.ContainerType;
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
import mekanism.common.util.ItemAccessUtils;
import mekanism.common.util.ResourceUtils;
import mekanism.common.util.WorldUtils;
import mekanism.common.util.text.BooleanStateDisplay.OnOff;
import mekanism.common.util.text.BooleanStateDisplay.YesNo;
import mekanism.common.util.text.TextUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.TypedInstance;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.core.component.DataComponentGetter;
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
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.cow.Cow;
import net.minecraft.world.entity.animal.cow.MushroomCow;
import net.minecraft.world.entity.animal.goat.Goat;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
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
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
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
    protected void addStats(@NotNull ItemStack stack, @NotNull Item.TooltipContext context, @NotNull TooltipDisplay tooltipDisplay, @NotNull Consumer<Component> tooltipAdder, @NotNull TooltipFlag flag) {
        FluidTankTier tier = getTier();
        LargeResourceStack<FluidResource> fluidStack = ContainerType.FLUID.getStoredContentsFromAttachment(ItemAccess.forStack(stack));
        if (fluidStack.isEmpty()) {
            tooltipAdder.accept(MekanismLang.EMPTY.translateColored(EnumColor.DARK_RED));
        } else if (tier == FluidTankTier.CREATIVE) {
            tooltipAdder.accept(MekanismLang.GENERIC_STORED.translateColored(EnumColor.PINK, fluidStack.resource(), EnumColor.GRAY, MekanismLang.INFINITE));
        } else {
            tooltipAdder.accept(MekanismLang.GENERIC_STORED_MB.translateColored(EnumColor.PINK, fluidStack.resource(), EnumColor.GRAY, TextUtils.format(fluidStack.amount())));
        }
        if (tier == FluidTankTier.CREATIVE) {
            tooltipAdder.accept(MekanismLang.CAPACITY.translateColored(EnumColor.INDIGO, EnumColor.GRAY, MekanismLang.INFINITE));
        } else {
            tooltipAdder.accept(MekanismLang.CAPACITY_MB.translateColored(EnumColor.INDIGO, EnumColor.GRAY, TextUtils.format(tier.getCapacity())));
        }
    }

    @Override
    protected void addTypeDetails(@NotNull ItemStack stack, @NotNull Item.TooltipContext context, @NotNull TooltipDisplay tooltipDisplay, @NotNull Consumer<Component> tooltipAdder, @NotNull TooltipFlag flag) {
        tooltipAdder.accept(MekanismLang.BUCKET_MODE.translateColored(EnumColor.INDIGO, YesNo.of(getMode(stack), true)));
        super.addTypeDetails(stack, context, tooltipDisplay, tooltipAdder, flag);
    }

    @NotNull
    @Override
    public InteractionResult interactLivingEntity(@NotNull ItemStack stack, @NotNull Player player, @NotNull LivingEntity entity, @NotNull InteractionHand hand) {
        if (getMode(stack) && !entity.isBaby()) {
            Level level = player.level();
            //TODO - 26.1: Should this use this or forPlayerInteraction
            ItemAccess itemAccess = ItemAccess.forStack(stack);
            if (ItemSecurityUtils.get().tryClaimItem(level, player, itemAccess, null)) {
                return InteractionResult.SUCCESS;
            } else if (!IItemSecurityUtils.INSTANCE.canAccessOrDisplayError(player, itemAccess)) {
                return InteractionResult.FAIL;
            } else if (stack.count() > 1) {
                //Skip if the item is stacked
                return InteractionResult.PASS;
            }
            SoundEvent milkSound = getMilkSound(entity);
            if (milkSound != null) {
                ResourceHandler<FluidResource> fluidHandler = Capabilities.FLUID.getCapability(itemAccess);
                if (fluidHandler == null) {
                    //If there isn't a fluid handler then there is something wrong with the stack, treat it as a normal stack and skip
                    return InteractionResult.PASS;
                }
                FluidResource milk = FluidResource.of(NeoForgeMod.MILK);
                //Try to insert the fluid
                try (Transaction transaction = Transaction.openRoot()) {
                    if (ResourceUtils.insertManual(fluidHandler, milk, FluidType.BUCKET_VOLUME, transaction) < FluidType.BUCKET_VOLUME) {
                        //TODO - 26.1: Given it is milking the animal, should we allow getting less than a full bucket, and only fail if inserted was zero?
                        //Fail if we can't insert any
                        return InteractionResult.FAIL;
                    }
                    transaction.commit();
                    player.playSound(milkSound, 1.0F, 1.0F);
                    //TODO - 26.1: Should this call heldItemTransformedTo?
                    return InteractionResult.SUCCESS;
                }
            }
        }
        return InteractionResult.PASS;
    }

    @Nullable
    private SoundEvent getMilkSound(LivingEntity entity) {
        if (entity instanceof Goat goat) {
            //TODO - 26.1: Do we want to AT this?
            //return goat.getMilkingSound();
            return goat.isScreamingGoat() ? SoundEvents.GOAT_SCREAMING_MILK : SoundEvents.GOAT_MILK;
        } else if (entity instanceof MushroomCow) {
            return SoundEvents.MOOSHROOM_MILK;
        } else if (entity instanceof Cow) {
            return SoundEvents.COW_MILK;
        }
        return null;
    }

    @NotNull
    @Override
    public InteractionResult useOn(UseOnContext context) {
        //Note: We don't need to check the stack size here, as we only want to allow placing it if it isn't in bucket mode
        return context.getPlayer() == null || getMode(context.getItemInHand()) ? InteractionResult.PASS : super.useOn(context);
    }

    @NotNull
    @Override
    public InteractionResult use(@NotNull Level world, Player player, @NotNull InteractionHand hand) {
        //TODO - 26.1: Re-evaluate this item access (and more accurately the usages of stack)
        ItemAccess itemAccess = ItemAccessUtils.playerHandAccess(player, hand);
        if (!getMode(itemAccess.getResource())) {
            return InteractionResult.PASS;
        } else if (ItemSecurityUtils.get().tryClaimItem(world, player, itemAccess, null)) {
            //TODO - 26.1: Re-evaluate SUCCESS vs SUCCESS_SERVER for our use impls
            return InteractionResult.SUCCESS.heldItemTransformedTo(ItemAccessUtils.asStack(itemAccess));
        } else if (!IItemSecurityUtils.INSTANCE.canAccessOrDisplayError(player, itemAccess)) {
            return InteractionResult.FAIL;
        } else if (itemAccess.getAmount() > 1) {
            //Skip if the item is stacked
            return InteractionResult.PASS;
        }
        //TODO: At some point maybe try to reduce the duplicate code between this and the dispense behavior
        BlockHitResult result = getPlayerPOVHitResult(world, player, player.isShiftKeyDown() ? ClipContext.Fluid.NONE : ClipContext.Fluid.SOURCE_ONLY);
        //It can be null if there is nothing in range
        if (result.getType() != Type.BLOCK) {
            return InteractionResult.PASS;
        }
        BlockPos pos = result.getBlockPos();
        if (!world.mayInteract(player, pos)) {
            return InteractionResult.FAIL;
        }
        //TODO - 26.1: Evaluate FluidUtil#tryPickupFluid for this and for pumps
        ResourceHandler<FluidResource> fluidHandler = Capabilities.FLUID.getCapability(itemAccess);
        if (fluidHandler == null) {
            //If something went wrong, and we don't have a fluid handler fail
            return InteractionResult.FAIL;
        }
        if (!player.isShiftKeyDown()) {
            if (!player.mayUseItemAt(pos, result.getDirection(), ItemAccessUtils.asStack(itemAccess))) {
                return InteractionResult.FAIL;
            }
            //Note: we get the block state from the world so that we can get the proper block in case it is fluid logged
            BlockState blockState = world.getBlockState(pos);
            FluidState fluidState = blockState.getFluidState();
            if (fluidState.isEmpty() || !fluidState.isSource() || !(blockState.getBlock() instanceof BucketPickup bucketPickup)) {
                return InteractionResult.PASS;
            }
            //Just in case someone does weird things and has a fluid state that is empty and a source
            // only allow collecting from non-empty sources
            FluidResource fluidType = FluidResource.of(fluidState.getType());
            Optional<SoundEvent> sound = bucketPickup.getPickupSound(blockState);
            try (Transaction transaction = Transaction.openRoot()) {
                if (fluidType.isEmpty() || ResourceUtils.insertManual(fluidHandler, fluidType, FluidType.BUCKET_VOLUME, transaction) < FluidType.BUCKET_VOLUME) {
                    //There is nothing to insert, or we couldn't insert a full bucket worth, fail
                    return InteractionResult.FAIL;
                }
                //If it can be picked up by a bucket, and we actually want to pick it up, do so to update the fluid type we are doing
                // otherwise we assume the type from the fluid state is correct
                ItemStack pickedUpStack = bucketPickup.pickupBlock(player, world, pos, blockState);
                if (pickedUpStack.isEmpty()) {
                    //If the fluid can't be picked up, pass on doing anything
                    return InteractionResult.PASS;
                } else if (pickedUpStack.getItem() instanceof BucketItem bucket) {
                    //This isn't the best validation check given it may not return a bucket, but it is good enough for now
                    if (fluidType.is(bucket.content)) {
                        //If the fluid that got picked up is the type we were expecting it to be, apply the insertion
                        // and play the fill sound and fire relevant game events
                        transaction.commit();
                        WorldUtils.playFillSound(player, world, pos, fluidType, sound.orElse(null));
                        world.gameEvent(player, GameEvent.FLUID_PICKUP, pos);
                        return InteractionResult.SUCCESS.heldItemTransformedTo(ItemAccessUtils.asStack(itemAccess));
                    }
                    fluidType = FluidResource.of(bucket.content);
                }//TODO - 26.1: Else do we want to just assume we got the type, and commit the transaction?
            }
            try (Transaction transaction = Transaction.openRoot()) {
                if (!fluidType.isEmpty() && ResourceUtils.insertManual(fluidHandler, fluidType, FluidType.BUCKET_VOLUME, transaction) >= FluidType.BUCKET_VOLUME) {
                    //If the fluid that got picked up is the type we were expecting it to be, apply the insertion
                    // and play the fill sound and fire relevant game events
                    transaction.commit();
                    WorldUtils.playFillSound(player, world, pos, fluidType, sound.orElse(null));
                    world.gameEvent(player, GameEvent.FLUID_PICKUP, pos);
                    return InteractionResult.SUCCESS.heldItemTransformedTo(ItemAccessUtils.asStack(itemAccess));
                }
            }
            Mekanism.logger.warn("Fluid removed without successfully picking up. Fluid {} at {} in {} was valid, but after picking up was {}.",
                  fluidState.getType(), pos, world.dimension().identifier(), fluidType);
        } else if (player.mayUseItemAt(pos.relative(result.getDirection()), result.getDirection(), ItemAccessUtils.asStack(itemAccess))) {
            for (int tank = 0, size = fluidHandler.size(); tank < size; tank++) {
                FluidResource resource = fluidHandler.getResource(tank);
                if (!resource.isEmpty()) {
                    try (Transaction transaction = Transaction.openRoot()) {
                        int extracted = ResourceUtils.extractManual(fluidHandler, resource, FluidType.BUCKET_VOLUME, transaction);
                        if (extracted == FluidType.BUCKET_VOLUME && WorldUtils.tryPlaceContainedLiquid(player, world, pos, resource.toStack(extracted), result.getDirection())) {
                            //If we extracted it, and are able to place it into the world
                            if (!player.isCreative()) {//TODO - 26.1: How does item access interact with the player being creative
                                //Manually shrink in case bucket volume is greater than tank input/output rate limit
                                //TODO - 26.1: Re-evaluate this comment as it seems like it was wrong, as we validated that we can extract that amount
                                transaction.commit();
                            }
                            world.gameEvent(player, GameEvent.FLUID_PLACE, pos);
                            return InteractionResult.SUCCESS.heldItemTransformedTo(ItemAccessUtils.asStack(itemAccess));
                        }
                    }
                }
            }
        }
        return InteractionResult.FAIL;
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
    public void changeMode(@NotNull Player player, @NotNull ItemAccess itemAccess, int shift, DisplayChange displayChange, TransactionContext transaction) {
        if (Math.abs(shift) % 2 == 1) {
            //We are changing by an odd amount, so toggle the mode
            boolean newState = !getMode(itemAccess);
            if (setMode(itemAccess, player, newState, transaction)) {
                displayChange.sendMessage(player, newState, s -> MekanismLang.BUCKET_MODE.translate(OnOff.of(s, true)));
            }
        }
    }

    @NotNull
    @Override
    public <ITEM extends TypedInstance<Item> & DataComponentGetter> Component getScrollTextComponent(@NotNull ITEM instance) {
        return MekanismLang.BUCKET_MODE.translateColored(EnumColor.GRAY, OnOff.of(getMode(instance), true));
    }

    public static class FluidTankItemDispenseBehavior extends DefaultDispenseItemBehavior {

        public static final FluidTankItemDispenseBehavior INSTANCE = new FluidTankItemDispenseBehavior();

        private FluidTankItemDispenseBehavior() {
        }

        @NotNull
        @Override
        public ItemStack execute(@NotNull BlockSource source, @NotNull ItemStack stack) {
            //TODO - 26.1: Can we come up with a way to allow deduplicating some of the logic between this and the item use method?
            if (stack.count() == 1 && stack.getItem() instanceof ItemBlockFluidTank tank && tank.getMode(stack)) {
                //If the fluid tank is in bucket mode allow for it to act as a bucket
                //Note: We don't use DispenseFluidContainer as we have more specific logic for determining if we want it to
                // act as a bucket that is emptying its contents or one that is picking up contents
                ResourceHandler<FluidResource> fluidHandler = Capabilities.FLUID.getCapability(ItemAccess.forStack(stack));
                if (fluidHandler == null) {
                    //If something went wrong, and we don't have a fluid handler fail, treat it as a normal stack and just eject it
                    return super.execute(source, stack);
                }
                Level world = source.level();
                BlockPos pos = source.pos().relative(source.state().getValue(DispenserBlock.FACING));
                //Note: we get the block state from the world so that we can get the proper block in case it is fluid logged
                BlockState blockState = world.getBlockState(pos);
                FluidState fluidState = blockState.getFluidState();
                //If the fluid state in the world isn't empty and is a source try to pick it up otherwise try to dispense the stored fluid
                if (!fluidState.isEmpty() && fluidState.isSource()) {
                    //Just in case someone does weird things and has a fluid state that is empty and a source
                    // only allow collecting from non-empty sources
                    //TODO - 26.1: Do we want to merge this with the above if statement? Would slightly change what cases it might try emptying the tank
                    if (blockState.getBlock() instanceof BucketPickup bucketPickup) {
                        FluidResource fluidType = FluidResource.of(fluidState.getType());
                        Optional<SoundEvent> sound = bucketPickup.getPickupSound(blockState);
                        try (Transaction transaction = Transaction.openRoot()) {
                            if (fluidType.isEmpty() || ResourceUtils.insertManual(fluidHandler, fluidType, FluidType.BUCKET_VOLUME, transaction) < FluidType.BUCKET_VOLUME) {
                                //There is nothing to insert, or we couldn't insert a full bucket worth, fail
                                return super.execute(source, stack);
                            }
                            //If it can be picked up by a bucket, and we actually want to pick it up, do so to update the fluid type we are doing
                            // otherwise we assume the type from the fluid state is correct
                            ItemStack pickedUpStack = bucketPickup.pickupBlock(null, world, pos, blockState);
                            if (pickedUpStack.isEmpty()) {
                                //If the fluid cannot be picked up, then eject the stack similar to how vanilla does for buckets
                                return super.execute(source, stack);
                            } else if (pickedUpStack.getItem() instanceof BucketItem bucket) {
                                //This isn't the best validation check given it may not return a bucket, but it is good enough for now
                                if (fluidType.is(bucket.content)) {
                                    //If the fluid that got picked up is the type we were expecting it to be, apply the insertion
                                    // and play the fill sound and fire relevant game events
                                    transaction.commit();
                                    WorldUtils.playFillSound(null, world, pos, fluidType, sound.orElse(null));
                                    world.gameEvent(null, GameEvent.FLUID_PICKUP, pos);
                                    //Success, don't dispense anything just return our resulting stack
                                    return stack;
                                }
                                fluidType = FluidResource.of(bucket.content);
                            }//TODO - 26.1: Else do we want to just assume we got the type, and commit the transaction?
                        }
                        try (Transaction transaction = Transaction.openRoot()) {
                            if (!fluidType.isEmpty() && ResourceUtils.insertManual(fluidHandler, fluidType, FluidType.BUCKET_VOLUME, transaction) >= FluidType.BUCKET_VOLUME) {
                                //If the fluid that got picked up is the type we were expecting it to be, apply the insertion
                                // and play the fill sound and fire relevant game events
                                transaction.commit();
                                WorldUtils.playFillSound(null, world, pos, fluidType, sound.orElse(null));
                                world.gameEvent(null, GameEvent.FLUID_PICKUP, pos);
                                //Success, don't dispense anything just return our resulting stack
                                return stack;
                            }
                        }
                        Mekanism.logger.warn("Fluid removed without successfully picking up. Fluid {} at {} in {} was valid, but after picking up was {}.",
                              fluidState.getType(), pos, world.dimension().identifier(), fluidType);
                        //If we can't insert or extract it, then eject the stack similar to how vanilla does for buckets
                    }
                } else {
                    for (int index = 0, size = fluidHandler.size(); index < size; index++) {
                        FluidResource resource = fluidHandler.getResource(index);
                        if (!resource.isEmpty()) {
                            try (Transaction transaction = Transaction.openRoot()) {
                                int extracted = ResourceUtils.extractManual(fluidHandler, resource, FluidType.BUCKET_VOLUME, transaction);
                                if (extracted == FluidType.BUCKET_VOLUME && WorldUtils.tryPlaceContainedLiquid(null, world, pos, resource.toStack(extracted), null)) {
                                    //If we extracted it, and are able to place it into the world
                                    transaction.commit();
                                    world.gameEvent(null, GameEvent.FLUID_PLACE, pos);
                                    //Success, don't dispense anything just return our resulting stack
                                    return stack;
                                }
                            }
                        }
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
            @NotNull
            @Override
            protected InteractionResult interact(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player,
                  @NotNull InteractionHand hand, @NotNull ItemStack stack, @NotNull ResourceHandler<FluidResource> fluidHandler) {
                try (Transaction transaction = Transaction.openRoot()) {
                    FluidResource extractedFluid = FluidResource.EMPTY;
                    for (FluidResource supportedFluid : List.of(FluidResource.of(Fluids.WATER), FluidResource.of(Fluids.LAVA))) {
                        try (Transaction subTransaction = Transaction.open(transaction)) {
                            int extracted = ResourceUtils.extractManual(fluidHandler, supportedFluid, FluidType.BUCKET_VOLUME, subTransaction);
                            if (extracted == FluidType.BUCKET_VOLUME) {
                                //Commit the fluid extraction to the sub transaction, and mark what type we have pending for overall commit with extraction
                                subTransaction.commit();
                                extractedFluid = supportedFluid;
                                break;
                            }
                        }
                    }
                    BlockState endState;
                    if (extractedFluid.is(Fluids.WATER)) {
                        endState = Blocks.WATER_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, LayeredCauldronBlock.MAX_FILL_LEVEL);
                    } else if (extractedFluid.is(Fluids.LAVA)) {
                        endState = Blocks.LAVA_CAULDRON.defaultBlockState();
                    } else {
                        return InteractionResult.TRY_WITH_EMPTY_HAND;
                    }
                    if (!level.isClientSide()) {
                        if (!player.isCreative()) {
                            //Manually shrink in case bucket volume is greater than tank input/output rate limit
                            //TODO - 26.1: Re-evaluate this comment as it seems like it was wrong, as we validated that we can extract that amount
                            transaction.commit();
                        }
                        player.awardStat(Stats.FILL_CAULDRON);
                        player.awardStat(Stats.ITEM_USED.get(stack.getItem()));
                        level.setBlockAndUpdate(pos, endState);
                        SoundEvent emptySound = extractedFluid.getFluidType().getSound(player, level, pos, SoundActions.BUCKET_EMPTY);
                        if (emptySound != null) {
                            level.playSound(null, pos, emptySound, SoundSource.BLOCKS, 1.0F, 1.0F);
                        }
                        level.gameEvent(null, GameEvent.FLUID_PLACE, pos);
                    }
                    return InteractionResult.SUCCESS;
                }
            }
        };

        @NotNull
        @Override
        public final InteractionResult interact(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player,
              @NotNull InteractionHand hand, @NotNull ItemStack stack) {
            if (stack.count() == 1 && stack.getItem() instanceof ItemBlockFluidTank tank && tank.getMode(stack)) {
                //If the fluid tank is in bucket mode allow for it to act as a bucket
                //TODO - 26.1: Should this be getting the item access from the player's interaction hand or the passed in stack?
                ResourceHandler<FluidResource> fluidHandler = Capabilities.FLUID.getCapability(ItemAccess.forStack(stack));
                if (fluidHandler == null) {
                    //If there isn't a handler then there is something wrong with the stack, treat it as a normal stack and skip
                    return InteractionResult.TRY_WITH_EMPTY_HAND;
                }
                return interact(state, level, pos, player, hand, stack, fluidHandler);
            }
            //Otherwise skip
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }

        @NotNull
        protected abstract InteractionResult interact(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player,
              @NotNull InteractionHand hand, @NotNull ItemStack stack, @NotNull ResourceHandler<FluidResource> fluidHandler);
    }

    public static class BasicDrainCauldronInteraction extends BasicCauldronInteraction {

        public static final BasicDrainCauldronInteraction WATER = new BasicDrainCauldronInteraction(Fluids.WATER) {
            @NotNull
            @Override
            protected InteractionResult interact(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player,
                  @NotNull InteractionHand hand, @NotNull ItemStack stack, @NotNull ResourceHandler<FluidResource> fluidHandler) {
                if (state.getValue(LayeredCauldronBlock.LEVEL) == LayeredCauldronBlock.MAX_FILL_LEVEL) {
                    //When emptying a water cauldron make sure it is full and just ignore handling of partial transfers
                    // as while we can handle them, they come with the added complication of deciding what value to give bottles
                    return super.interact(state, level, pos, player, hand, stack, fluidHandler);
                }
                return InteractionResult.TRY_WITH_EMPTY_HAND;
            }
        };
        public static final BasicDrainCauldronInteraction LAVA = new BasicDrainCauldronInteraction(Fluids.LAVA);

        private final Fluid type;

        private BasicDrainCauldronInteraction(Fluid type) {
            this.type = type;
        }

        @NotNull
        @Override
        protected InteractionResult interact(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player,
              @NotNull InteractionHand hand, @NotNull ItemStack stack, @NotNull ResourceHandler<FluidResource> fluidHandler) {
            try (Transaction transaction = Transaction.openRoot()) {
                int inserted = ResourceUtils.insertManual(fluidHandler, FluidResource.of(type), FluidType.BUCKET_VOLUME, transaction);
                if (inserted == FluidType.BUCKET_VOLUME) {
                    //We can fit all the fluid we would be removing
                    if (!level.isClientSide()) {
                        if (!player.isCreative()) {
                            transaction.commit();
                        }
                        player.awardStat(Stats.USE_CAULDRON);
                        player.awardStat(Stats.ITEM_USED.get(stack.getItem()));
                        level.setBlockAndUpdate(pos, Blocks.CAULDRON.defaultBlockState());
                        SoundEvent fillSound = type.getFluidType().getSound(null, level, pos, SoundActions.BUCKET_FILL);
                        if (fillSound != null) {
                            level.playSound(null, pos, fillSound, SoundSource.BLOCKS, 1.0F, 1.0F);
                        }
                        level.gameEvent(null, GameEvent.FLUID_PICKUP, pos);
                    }
                    return InteractionResult.SUCCESS;
                }
            }
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
    }
}