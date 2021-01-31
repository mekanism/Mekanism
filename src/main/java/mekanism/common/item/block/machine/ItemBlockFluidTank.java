package mekanism.common.item.block.machine;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nonnull;
import mekanism.api.Action;
import mekanism.api.DataHandlerUtils;
import mekanism.api.NBTConstants;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.fluid.IMekanismFluidHandler;
import mekanism.api.inventory.AutomationType;
import mekanism.api.text.EnumColor;
import mekanism.client.render.item.ISTERProvider;
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
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.IBucketPickupHandler;
import net.minecraft.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext.FluidMode;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
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
        super(block, true, ItemDeferredRegister.getMekBaseProperties().maxStackSize(1).setISTER(ISTERProvider::fluidTank));
    }

    @Nonnull
    @Override
    public FluidTankTier getTier() {
        return Attribute.getTier(getBlock(), FluidTankTier.class);
    }

    @Override
    public void addStats(@Nonnull ItemStack stack, World world, @Nonnull List<ITextComponent> tooltip, boolean advanced) {
        FluidTankTier tier = getTier();
        FluidStack fluidStack = StorageUtils.getStoredFluidFromNBT(stack);
        if (fluidStack.isEmpty()) {
            tooltip.add(MekanismLang.EMPTY.translateColored(EnumColor.DARK_RED));
        } else if (tier == FluidTankTier.CREATIVE) {
            tooltip.add(MekanismLang.GENERIC_STORED.translateColored(EnumColor.PINK, fluidStack, EnumColor.GRAY, MekanismLang.INFINITE));
        } else {
            tooltip.add(MekanismLang.GENERIC_STORED_MB.translateColored(EnumColor.PINK, fluidStack, EnumColor.GRAY, fluidStack.getAmount()));
        }
        if (tier == FluidTankTier.CREATIVE) {
            tooltip.add(MekanismLang.CAPACITY.translateColored(EnumColor.INDIGO, EnumColor.GRAY, MekanismLang.INFINITE));
        } else {
            tooltip.add(MekanismLang.CAPACITY_MB.translateColored(EnumColor.INDIGO, EnumColor.GRAY, tier.getStorage()));
        }
    }

    @Override
    public void addDetails(@Nonnull ItemStack stack, World world, @Nonnull List<ITextComponent> tooltip, boolean advanced) {
        SecurityUtils.addSecurityTooltip(stack, tooltip);
        tooltip.add(MekanismLang.BUCKET_MODE.translateColored(EnumColor.INDIGO, YesNo.of(getBucketMode(stack))));
        tooltip.add(MekanismLang.HAS_INVENTORY.translateColored(EnumColor.AQUA, EnumColor.GRAY, YesNo.of(hasInventory(stack))));
    }

    @Override
    public void fillItemGroup(@Nonnull ItemGroup group, @Nonnull NonNullList<ItemStack> items) {
        super.fillItemGroup(group, items);
        if (isInGroup(group)) {
            FluidTankTier tier = Attribute.getTier(getBlock(), FluidTankTier.class);
            if (tier == FluidTankTier.CREATIVE && MekanismConfig.general.prefilledFluidTanks.get()) {
                int capacity = tier.getStorage();
                for (Fluid fluid : ForgeRegistries.FLUIDS.getValues()) {
                    //Only add sources
                    if (fluid.isSource(fluid.getDefaultState())) {
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
    public ActionResultType onItemUse(ItemUseContext context) {
        PlayerEntity player = context.getPlayer();
        if (player == null) {
            return ActionResultType.PASS;
        }
        ItemStack stack = context.getItem();
        if (getBucketMode(stack)) {
            return ActionResultType.PASS;
        }
        return super.onItemUse(context);
    }

    @Nonnull
    @Override
    public ActionResult<ItemStack> onItemRightClick(@Nonnull World world, PlayerEntity player, @Nonnull Hand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (getBucketMode(stack)) {
            if (getOwnerUUID(stack) == null) {
                if (!world.isRemote) {
                    SecurityUtils.claimItem(player, stack);
                }
                return new ActionResult<>(ActionResultType.SUCCESS, stack);
            } else if (SecurityUtils.canAccess(player, stack)) {
                //TODO: At some point maybe try to reduce the duplicate code between this and the dispense behavior
                BlockRayTraceResult result = rayTrace(world, player, !player.isSneaking() ? FluidMode.SOURCE_ONLY : FluidMode.NONE);
                //It can be null if there is nothing in range
                if (result.getType() == Type.BLOCK) {
                    BlockPos pos = result.getPos();
                    if (!world.isBlockModifiable(player, pos)) {
                        return new ActionResult<>(ActionResultType.FAIL, stack);
                    }
                    IExtendedFluidTank fluidTank = getExtendedFluidTank(stack);
                    if (fluidTank == null) {
                        //If something went wrong and we don't have a fluid tank fail
                        return new ActionResult<>(ActionResultType.FAIL, stack);
                    }
                    if (!player.isSneaking()) {
                        if (!player.canPlayerEdit(pos, result.getFace(), stack)) {
                            return new ActionResult<>(ActionResultType.FAIL, stack);
                        }
                        //Note: we get the block state from the world so that we can get the proper block in case it is fluid logged
                        BlockState blockState = world.getBlockState(pos);
                        FluidState fluidState = blockState.getFluidState();
                        if (!fluidState.isEmpty() && fluidState.isSource()) {
                            //Just in case someone does weird things and has a fluid state that is empty and a source
                            // only allow collecting from non empty sources
                            Fluid fluid = fluidState.getFluid();
                            FluidStack fluidStack = new FluidStack(fluid, FluidAttributes.BUCKET_VOLUME);
                            Block block = blockState.getBlock();
                            if (block instanceof IFluidBlock) {
                                fluidStack = ((IFluidBlock) block).drain(world, pos, FluidAction.SIMULATE);
                                if (!validFluid(fluidTank, fluidStack)) {
                                    //If the fluid is not valid, pass on doing anything
                                    return new ActionResult<>(ActionResultType.PASS, stack);
                                }
                                //Actually drain it
                                fluidStack = ((IFluidBlock) block).drain(world, pos, FluidAction.EXECUTE);
                            } else if (block instanceof IBucketPickupHandler && validFluid(fluidTank, fluidStack)) {
                                //If it can be picked up by a bucket and we actually want to pick it up, do so to update the fluid type we are doing
                                // otherwise we assume the type from the fluid state is correct
                                fluid = ((IBucketPickupHandler) block).pickupFluid(world, pos, blockState);
                                //Update the fluid stack in case something somehow changed about the type
                                // making sure that we replace to heavy water if we got heavy water
                                fluidStack = new FluidStack(fluid, FluidAttributes.BUCKET_VOLUME);
                                if (!validFluid(fluidTank, fluidStack)) {
                                    Mekanism.logger.warn("Fluid removed without successfully picking up. Fluid {} at {} in {} was valid, but after picking up was {}.",
                                          fluidState.getFluid().getRegistryName(), pos, world.getDimensionKey().getLocation(), fluid.getRegistryName());
                                    return new ActionResult<>(ActionResultType.FAIL, stack);
                                }
                            }
                            if (validFluid(fluidTank, fluidStack)) {
                                if (fluidTank.isEmpty()) {
                                    fluidTank.setStack(fluidStack);
                                } else {
                                    //Grow the stack
                                    MekanismUtils.logMismatchedStackSize(fluidTank.growStack(fluidStack.getAmount(), Action.EXECUTE), fluidStack.getAmount());
                                }
                                //Play the bucket fill sound
                                WorldUtils.playFillSound(player, world, pos, fluidStack);
                                return new ActionResult<>(ActionResultType.SUCCESS, stack);
                            }
                            return new ActionResult<>(ActionResultType.FAIL, stack);
                        }
                    } else {
                        if (fluidTank.extract(FluidAttributes.BUCKET_VOLUME, Action.SIMULATE, AutomationType.MANUAL).getAmount() < FluidAttributes.BUCKET_VOLUME
                            || !player.canPlayerEdit(pos.offset(result.getFace()), result.getFace(), stack)) {
                            return new ActionResult<>(ActionResultType.FAIL, stack);
                        }
                        if (WorldUtils.tryPlaceContainedLiquid(player, world, pos, fluidTank.getFluid(), result.getFace())) {
                            if (!player.isCreative()) {
                                MekanismUtils.logMismatchedStackSize(fluidTank.shrinkStack(FluidAttributes.BUCKET_VOLUME, Action.EXECUTE), FluidAttributes.BUCKET_VOLUME);
                            }
                            return new ActionResult<>(ActionResultType.SUCCESS, stack);
                        }
                    }
                }
            } else {
                if (!world.isRemote) {
                    SecurityUtils.displayNoAccess(player);
                }
                return new ActionResult<>(ActionResultType.FAIL, stack);
            }
        }
        return new ActionResult<>(ActionResultType.PASS, stack);
    }

    private static boolean validFluid(@Nonnull IExtendedFluidTank fluidTank, @Nonnull FluidStack fluidStack) {
        return !fluidStack.isEmpty() && fluidTank.insert(fluidStack, Action.SIMULATE, AutomationType.MANUAL).isEmpty();
    }

    private static IExtendedFluidTank getExtendedFluidTank(@Nonnull ItemStack stack) {
        Optional<IFluidHandlerItem> capability = FluidUtil.getFluidHandler(stack).resolve();
        if (capability.isPresent()) {
            IFluidHandlerItem fluidHandlerItem = capability.get();
            if (fluidHandlerItem instanceof IMekanismFluidHandler) {
                return ((IMekanismFluidHandler) fluidHandlerItem).getFluidTank(0, null);
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
    public ICapabilityProvider initCapabilities(ItemStack stack, CompoundNBT nbt) {
        return new ItemCapabilityWrapper(stack, RateLimitFluidHandler.create(getTier()));
    }

    @Override
    public void changeMode(@Nonnull PlayerEntity player, @Nonnull ItemStack stack, int shift, boolean displayChangeMessage) {
        if (Math.abs(shift) % 2 == 1) {
            //We are changing by an odd amount, so toggle the mode
            boolean newState = !getBucketMode(stack);
            setBucketMode(stack, newState);
            if (displayChangeMessage) {
                player.sendMessage(MekanismLang.LOG_FORMAT.translateColored(EnumColor.DARK_BLUE, MekanismLang.MEKANISM,
                      MekanismLang.BUCKET_MODE.translateColored(EnumColor.GRAY, OnOff.of(newState, true))), Util.DUMMY_UUID);
            }
        }
    }

    @Nonnull
    @Override
    public ITextComponent getScrollTextComponent(@Nonnull ItemStack stack) {
        return MekanismLang.BUCKET_MODE.translateColored(EnumColor.GRAY, OnOff.of(getBucketMode(stack), true));
    }

    public static class FluidTankItemDispenseBehavior extends DefaultDispenseItemBehavior {

        public static final FluidTankItemDispenseBehavior INSTANCE = new FluidTankItemDispenseBehavior();

        private FluidTankItemDispenseBehavior() {
        }

        @Nonnull
        @Override
        public ItemStack dispenseStack(@Nonnull IBlockSource source, @Nonnull ItemStack stack) {
            if (stack.getItem() instanceof ItemBlockFluidTank && ((ItemBlockFluidTank) stack.getItem()).getBucketMode(stack)) {
                //If the fluid tank is in bucket mode allow for it to act as a bucket
                //Note: We don't use DispenseFluidContainer as we have more specific logic for determining if we want it to
                // act as a bucket that is emptying its contents or one that is picking up contents
                IExtendedFluidTank fluidTank = getExtendedFluidTank(stack);
                //Get the fluid tank for the stack
                if (fluidTank == null) {
                    //If there isn't one then there is something wrong with the stack, treat it as a normal stack and just eject it
                    return super.dispenseStack(source, stack);
                }
                World world = source.getWorld();
                BlockPos pos = source.getBlockPos().offset(source.getBlockState().get(DispenserBlock.FACING));
                //Note: we get the block state from the world so that we can get the proper block in case it is fluid logged
                BlockState blockState = world.getBlockState(pos);
                FluidState fluidState = blockState.getFluidState();
                //If the fluid state in the world isn't empty and is a source try to pick it up otherwise try to dispense the stored fluid
                if (!fluidState.isEmpty() && fluidState.isSource()) {
                    //Just in case someone does weird things and has a fluid state that is empty and a source
                    // only allow collecting from non empty sources
                    Fluid fluid = fluidState.getFluid();
                    FluidStack fluidStack = new FluidStack(fluid, FluidAttributes.BUCKET_VOLUME);
                    Block block = blockState.getBlock();
                    if (block instanceof IFluidBlock) {
                        fluidStack = ((IFluidBlock) block).drain(world, pos, FluidAction.SIMULATE);
                        if (!validFluid(fluidTank, fluidStack)) {
                            //If the fluid is not valid, then eject the stack similar to how vanilla does for buckets
                            return super.dispenseStack(source, stack);
                        }
                        //Actually drain it
                        fluidStack = ((IFluidBlock) block).drain(world, pos, FluidAction.EXECUTE);
                    } else if (block instanceof IBucketPickupHandler && validFluid(fluidTank, fluidStack)) {
                        //If it can be picked up by a bucket and we actually want to pick it up, do so to update the fluid type we are doing
                        // otherwise we assume the type from the fluid state is correct
                        fluid = ((IBucketPickupHandler) block).pickupFluid(world, pos, blockState);
                        //Update the fluid stack in case something somehow changed about the type
                        // making sure that we replace to heavy water if we got heavy water
                        fluidStack = new FluidStack(fluid, FluidAttributes.BUCKET_VOLUME);
                        if (!validFluid(fluidTank, fluidStack)) {
                            Mekanism.logger.warn("Fluid removed without successfully picking up. Fluid {} at {} in {} was valid, but after picking up was {}.",
                                  fluidState.getFluid().getRegistryName(), pos, world.getDimensionKey().getLocation(), fluid.getRegistryName());
                            //If we can't insert or extract it, then eject the stack similar to how vanilla does for buckets
                            return super.dispenseStack(source, stack);
                        }
                    }
                    if (validFluid(fluidTank, fluidStack)) {
                        if (fluidTank.isEmpty()) {
                            fluidTank.setStack(fluidStack);
                        } else {
                            //Grow the stack
                            MekanismUtils.logMismatchedStackSize(fluidTank.growStack(fluidStack.getAmount(), Action.EXECUTE), fluidStack.getAmount());
                        }
                        //Play the bucket fill sound
                        WorldUtils.playFillSound(null, world, pos, fluidStack);
                        //Success, don't dispense anything just return our resulting stack
                        return stack;
                    }
                } else if (fluidTank.extract(FluidAttributes.BUCKET_VOLUME, Action.SIMULATE, AutomationType.MANUAL).getAmount() >= FluidAttributes.BUCKET_VOLUME) {
                    if (WorldUtils.tryPlaceContainedLiquid(null, world, pos, fluidTank.getFluid(), null)) {
                        MekanismUtils.logMismatchedStackSize(fluidTank.shrinkStack(FluidAttributes.BUCKET_VOLUME, Action.EXECUTE), FluidAttributes.BUCKET_VOLUME);
                        //Success, don't dispense anything just return our resulting stack
                        return stack;
                    }
                }
                //If we can't insert or extract it, then eject the stack similar to how vanilla does for buckets
            }
            //Otherwise eject it as a normal item
            return super.dispenseStack(source, stack);
        }
    }
}