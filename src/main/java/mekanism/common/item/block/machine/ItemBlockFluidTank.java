package mekanism.common.item.block.machine;

import java.util.function.Consumer;
import mekanism.api.resource.LargeResourceStack;
import mekanism.api.security.IItemSecurityUtils;
import mekanism.api.text.EnumColor;
import mekanism.common.MekanismLang;
import mekanism.common.attachments.containers.type.ContainerType;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.basic.BlockFluidTank;
import mekanism.common.block.prefab.BlockTile;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.proxy.AutomatedResourceHandler;
import mekanism.common.item.block.ItemBlockTooltip;
import mekanism.common.item.interfaces.IModeItem.IAttachmentBasedModeItem;
import mekanism.common.lib.security.ItemSecurityUtils;
import mekanism.common.registries.MekanismDataComponents;
import mekanism.common.tier.FluidTankTier;
import mekanism.common.tile.interfaces.IFluidContainerManager.ContainerEditMode;
import mekanism.common.util.ItemAccessUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.text.BooleanStateDisplay.OnOff;
import mekanism.common.util.text.BooleanStateDisplay.YesNo;
import mekanism.common.util.text.TextUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.common.SoundActions;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.fluid.FluidUtil;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemUtil;
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
    protected void addStats(@NotNull ItemStack stack, @NotNull ItemAccess itemAccess, @NotNull Item.TooltipContext context, @NotNull TooltipDisplay tooltipDisplay,
          @NotNull Consumer<Component> tooltipAdder, @NotNull TooltipFlag flag) {
        FluidTankTier tier = getTier();
        LargeResourceStack<FluidResource> fluidStack = ContainerType.FLUID.getStoredContentsFromAttachment(itemAccess);
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
    protected void addTypeDetails(@NotNull ItemStack stack, @NotNull ItemAccess itemAccess, @NotNull Item.TooltipContext context, @NotNull TooltipDisplay tooltipDisplay,
          @NotNull Consumer<Component> tooltipAdder, @NotNull TooltipFlag flag) {
        tooltipAdder.accept(MekanismLang.BUCKET_MODE.translateColored(EnumColor.INDIGO, YesNo.of(getMode(itemAccess), true)));
        super.addTypeDetails(stack, itemAccess, context, tooltipDisplay, tooltipAdder, flag);
    }

    @NotNull
    @Override
    public InteractionResult interactLivingEntity(@NotNull ItemStack stack, @NotNull Player player, @NotNull LivingEntity entity, @NotNull InteractionHand hand) {
        if (getMode(stack) && !entity.isBaby()) {
            Level level = player.level();
            //Protect against any mods that might be doing transactional logic, such as if an auto clicker validates it has enough energy before calling this method
            try (Transaction transaction = MekanismUtils.openTransactionSafe()) {
                //TODO: Should this use the stack or the player's held item?
                ItemAccess itemAccess = ItemAccess.forStack(stack);
                if (ItemSecurityUtils.get().tryClaimItem(level, player, itemAccess, transaction)) {
                    transaction.commit();
                    return InteractionResult.SUCCESS.heldItemTransformedTo(ItemAccessUtils.asStack(itemAccess));
                } else if (!IItemSecurityUtils.INSTANCE.canAccessOrDisplayError(player, itemAccess)) {
                    return InteractionResult.FAIL;
                }
                SoundEvent milkSound = getMilkSound(entity);
                if (milkSound != null) {
                    //Update the item access to take whether the player is in creative into account, and to allow it to put any overflow into other slots in the player's inventory
                    itemAccess = ItemAccess.forPlayerInteraction(player, hand);
                    ResourceHandler<FluidResource> fluidHandler = getOneByOneFluidHandler(itemAccess);
                    //If there isn't a fluid handler then there is something wrong with the stack, treat it as a normal stack and skip
                    if (fluidHandler != null) {
                        //Try to insert the fluid
                        if (fluidHandler.insert(FluidResource.of(NeoForgeMod.MILK), FluidType.BUCKET_VOLUME, transaction) == 0) {
                            //Fail if we can't insert any, we allow partial bucket amounts for milking though
                            return InteractionResult.FAIL;
                        }
                        player.playSound(milkSound, 1.0F, 1.0F);
                        transaction.commit();
                        return InteractionResult.SUCCESS.heldItemTransformedTo(ItemAccessUtils.asStack(itemAccess));
                    }
                }
            }
        }
        return InteractionResult.PASS;
    }

    @Nullable
    private SoundEvent getMilkSound(LivingEntity entity) {
        if (entity instanceof Goat goat) {
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
        if (context.getPlayer() == null) {
            return InteractionResult.PASS;
        } else if (getMode(context.getItemInHand())) {
            return InteractionResult.PASS;
        }
        return super.useOn(context);
    }

    @NotNull
    @Override
    public InteractionResult use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
        ItemAccess itemAccess = ItemAccessUtils.playerHandAccess(player, hand);
        if (!getMode(itemAccess.getResource())) {
            return InteractionResult.PASS;
        }
        //Protect against any mods that might be doing transactional logic, such as if an auto clicker validates it has enough energy before calling this method
        try (Transaction transaction = MekanismUtils.openTransactionSafe()) {
            if (ItemSecurityUtils.get().tryClaimItem(level, player, itemAccess, transaction)) {
                transaction.commit();
                //TODO - 26.1: Re-evaluate SUCCESS vs SUCCESS_SERVER for our use impls
                return InteractionResult.SUCCESS.heldItemTransformedTo(ItemAccessUtils.asStack(itemAccess));
            } else if (!IItemSecurityUtils.INSTANCE.canAccessOrDisplayError(player, itemAccess)) {
                return InteractionResult.FAIL;
            }
            BlockHitResult result = getPlayerPOVHitResult(level, player, player.isShiftKeyDown() ? ClipContext.Fluid.NONE : ClipContext.Fluid.SOURCE_ONLY);
            //It can be null if there is nothing in range
            if (result.getType() != Type.BLOCK) {
                return InteractionResult.PASS;
            }
            BlockPos pos = result.getBlockPos();
            Direction direction = result.getDirection();
            BlockPos directionOffsetPos = pos.relative(direction);
            if (!level.mayInteract(player, pos) || !player.mayUseItemAt(directionOffsetPos, direction, ItemAccessUtils.asStack(itemAccess))) {
                //Check that mirrors BucketItem#use where it validates if the player may use the item at the given position
                return InteractionResult.FAIL;
            }
            if (player.isCreative()) {
                //Update the item access to take whether the player is in creative into account
                itemAccess = ItemAccess.forInfiniteMaterials(player, ItemAccessUtils.asStack(itemAccess));
            }
            ResourceHandler<FluidResource> fluidHandler = getOneByOneFluidHandler(itemAccess);
            if (fluidHandler == null) {
                //If something went wrong, and we don't have a fluid handler fail
                return InteractionResult.FAIL;
            } else if (player.isShiftKeyDown()) {
                //Note: Unlike buckets we try to be smarter and not actually perform the usage if nothing changed because of the state already being fluid logged
                if (FluidUtil.tryPlaceFluid(fluidHandler, player, level, pos, true, transaction).isEmpty() &&
                    FluidUtil.tryPlaceFluid(fluidHandler, player, level, directionOffsetPos, true, transaction).isEmpty()) {
                    return InteractionResult.FAIL;
                }
            } else if (FluidUtil.tryPickupFluid(fluidHandler, player, level, pos, transaction).isEmpty()) {
                return InteractionResult.FAIL;
            }
            transaction.commit();
            return InteractionResult.SUCCESS.heldItemTransformedTo(ItemAccessUtils.asStack(itemAccess));
        }
    }

    @Nullable
    private static ResourceHandler<FluidResource> getOneByOneFluidHandler(ItemAccess itemAccess) {
        //Note: We wrap the fluid handler to force it interacting with the manual automation type so that it can bypass the rate limit
        // and still work as a bucket when the tank's rate limit is less than a bucket
        return AutomatedResourceHandler.manual(Capabilities.FLUID.getCapability(itemAccess.oneByOne()));
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
            //If the fluid tank is in bucket mode allow for it to act as a bucket
            if (stack.getItem() instanceof ItemBlockFluidTank tank && tank.getMode(stack)) {
                //Note: We don't use DispenseFluidContainer as we have more specific logic for determining if we want it to act as a bucket that is emptying its contents
                // or one that is picking up contents. We do however create the item access in the same way as DispenseFluidContainer does:
                // Create an item access; for now a simple one with 1 overflow slots.
                ItemStacksResourceHandler containingHandler = new ItemStacksResourceHandler(2);
                containingHandler.set(0, ItemResource.of(stack), stack.getCount());
                ResourceHandler<FluidResource> resourceHandler = getOneByOneFluidHandler(ItemAccess.forHandlerIndex(containingHandler, 0));
                if (resourceHandler == null) {
                    //If something went wrong, and we don't have a fluid handler fail, treat it as a normal stack and just eject it
                    return super.execute(source, stack);
                }
                Level level = source.level();
                BlockPos pos = source.pos().relative(source.state().getValue(DispenserBlock.FACING));
                FluidState fluidState = level.getFluidState(pos);
                //Protect against any mods that might be doing transactional logic, such as if a custom dispenser validates it has enough energy before calling this method
                try (Transaction transaction = MekanismUtils.openTransactionSafe()) {
                    FluidStack result;
                    //If the fluid state in the world isn't empty and is a source try to pick it up otherwise try to dispense the stored fluid
                    if (fluidState.isEmpty() || !fluidState.isSource()) {
                        //Note: Unlike buckets we try to be smarter and not actually perform the usage if nothing changed because of the state already being fluid logged
                        result = FluidUtil.tryPlaceFluid(resourceHandler, null, level, pos, true, transaction);
                    } else {
                        result = FluidUtil.tryPickupFluid(resourceHandler, null, level, pos, transaction);
                    }
                    if (!result.isEmpty()) {
                        //Commit the transaction as successful, and return the proper stack
                        transaction.commit();
                        //Mirror DispenseFluidContainer again in how it handles if the fluid handler had to get split due to being stacked
                        ItemStack stack0 = ItemUtil.getStack(containingHandler, 0);
                        ItemStack stack1 = ItemUtil.getStack(containingHandler, 1);
                        // Grow by 1 to match the shrink in consumeWithRemainder
                        stack0.grow(1);
                        return consumeWithRemainder(source, stack0, stack1);
                    }
                }
                //If we can't insert or extract it, then eject the stack similar to how vanilla does for buckets
            }
            //Otherwise, eject it as a normal item
            return super.execute(source, stack);
        }
    }

    public static class FluidTankCauldronInteraction implements CauldronInteraction {

        //Note: Theoretically for vanilla cauldrons this could be simplified slightly by relying on the fact they only allow inserting/extracting the full amount
        // at once, but in case we manage to find a way to add this to other cauldron interaction maps (or if an addon wants to use this against their custom cauldrons)
        // then we want this to be able to easily support those cases
        public static final FluidTankCauldronInteraction INSTANCE = new FluidTankCauldronInteraction();

        private FluidTankCauldronInteraction() {
        }

        @NotNull
        @Override
        public final InteractionResult interact(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player,
              @NotNull InteractionHand hand, @NotNull ItemStack stack) {
            if (stack.getItem() instanceof ItemBlockFluidTank tank && tank.getMode(stack)) {
                //If the fluid tank is in bucket mode allow for it to act as a bucket
                //Note: To behave similar to buckets, we specifically use a side effect free item access for when the player is in creative
                ItemAccess itemAccess = ItemAccessUtils.playerHandAccess(player, hand, true);
                ResourceHandler<FluidResource> fluidHandler = getOneByOneFluidHandler(itemAccess);
                if (fluidHandler == null || fluidHandler.size() == 0) {
                    //If there isn't a handler then there is something wrong with the stack, treat it as a normal stack and skip
                    return InteractionResult.TRY_WITH_EMPTY_HAND;
                }
                //TODO: Theoretically we could look up the direction using getPlayerPOVHitResult, but at least for builtin cauldrons
                // the side doesn't matter when getting the capability so we pretend we are interacting with it from the top
                ResourceHandler<FluidResource> cauldronHandler = Capabilities.FLUID.getCapabilityIfLoaded(level, pos, state, null, Direction.UP);
                if (cauldronHandler == null) {
                    return InteractionResult.TRY_WITH_EMPTY_HAND;
                }
                Item usedItem = stack.getItem();
                //Protect against any mods that might be doing transactional logic, such as if an auto clicker validates it has enough energy before calling this method
                try (Transaction transaction = MekanismUtils.openTransactionSafe()) {
                    FluidResource targetFluidType = FluidResource.EMPTY;
                    int amountToTransfer = 0;
                    //Note: The cauldron capability that Neo provides for vanilla cauldrons just have a single index supported
                    // We loop it though just in case we are being used for a modded cauldron that has multiple tanks
                    for (int i = 0, size = cauldronHandler.size(); i < size; i++) {
                        FluidResource resource = cauldronHandler.getResource(i);
                        if (!resource.isEmpty()) {
                            try (Transaction simulation = Transaction.open(transaction)) {
                                amountToTransfer = fluidHandler.insert(resource, cauldronHandler.getAmountAsInt(i), simulation);
                                if (amountToTransfer > 0) {
                                    //If we found a type that can go in the fluid tank, mark it as the target
                                    targetFluidType = resource;
                                    break;
                                }
                            }
                        }
                    }
                    if (targetFluidType.isEmpty()) {
                        //If there is nothing stored in the cauldron, see if there is anything stored in our tank that can be used to fill the cauldron
                        FluidResource resource = fluidHandler.getResource(0);
                        if (!resource.isEmpty()) {
                            try (Transaction simulation = Transaction.open(transaction)) {
                                //Calculate the amount we actually have available in case the tank's rate limit is less than the cauldron will be able to handle
                                amountToTransfer = fluidHandler.extract(resource, fluidHandler.getAmountAsInt(0), simulation);
                            }
                            if (amountToTransfer > 0) {
                                //Try to fill the cauldron with our tank's contents
                                return tryTransfer(level, pos, player, usedItem, resource, amountToTransfer, fluidHandler, cauldronHandler, true, transaction);
                            }
                        }
                    } else {
                        //Try to take the contents out of the cauldron
                        return tryTransfer(level, pos, player, usedItem, targetFluidType, amountToTransfer, fluidHandler, cauldronHandler, false, transaction);
                    }
                }
            }
            //Otherwise skip
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }

        private InteractionResult tryTransfer(Level level, BlockPos pos, Player player, Item usedItem, FluidResource fluid, int amountToTransfer,
              ResourceHandler<FluidResource> fluidHandler, ResourceHandler<FluidResource> cauldronHandler, boolean filledCauldron, Transaction transaction) {
            ResourceHandler<FluidResource> handlerToFill = filledCauldron ? cauldronHandler : fluidHandler;
            ResourceHandler<FluidResource> handlerToDrain = filledCauldron ? fluidHandler : cauldronHandler;
            int inserted = handlerToFill.insert(fluid, amountToTransfer, transaction);
            if (inserted > 0 && handlerToDrain.extract(fluid, inserted, transaction) == inserted) {
                if (!level.isClientSide()) {
                    player.awardStat(filledCauldron ? Stats.FILL_CAULDRON : Stats.USE_CAULDRON);
                    player.awardStat(Stats.ITEM_USED.get(usedItem));
                    SoundEvent sound = fluid.getFluidType().getSound(player, level, pos, filledCauldron ? SoundActions.BUCKET_EMPTY : SoundActions.BUCKET_FILL);
                    if (sound != null) {
                        level.playSound(null, pos, sound, SoundSource.BLOCKS, 1.0F, 1.0F);
                    }
                    level.gameEvent(null, filledCauldron ? GameEvent.FLUID_PLACE : GameEvent.FLUID_PICKUP, pos);
                    //Note: This will handle updating the cauldron to the correct state
                    transaction.commit();
                }
                return InteractionResult.SUCCESS;
            }
            //Otherwise skip
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
    }
}