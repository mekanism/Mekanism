package mekanism.common.item.block.machine;

import java.util.List;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Action;
import mekanism.api.NBTConstants;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.fluid.IMekanismFluidHandler;
import mekanism.api.inventory.AutomationType;
import mekanism.api.text.EnumColor;
import mekanism.client.render.item.ISTERProvider;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.base.IItemNetwork;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.basic.BlockFluidTank;
import mekanism.common.capabilities.ItemCapabilityWrapper;
import mekanism.common.capabilities.fluid.item.RateLimitFluidHandler;
import mekanism.common.item.IItemSustainedInventory;
import mekanism.common.item.block.ItemBlockAdvancedTooltip;
import mekanism.common.registration.impl.ItemDeferredRegister;
import mekanism.common.security.ISecurityItem;
import mekanism.common.tier.FluidTankTier;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.SecurityUtils;
import mekanism.common.util.StorageUtils;
import mekanism.common.util.text.BooleanStateDisplay.YesNo;
import mekanism.common.util.text.OwnerDisplay;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.IBucketPickupHandler;
import net.minecraft.block.ILiquidContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FlowingFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.IFluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext.FluidMode;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.Constants.BlockFlags;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

public class ItemBlockFluidTank extends ItemBlockAdvancedTooltip<BlockFluidTank> implements IItemSustainedInventory, ISecurityItem, IItemNetwork {

    public ItemBlockFluidTank(BlockFluidTank block) {
        super(block, ItemDeferredRegister.getMekBaseProperties().maxStackSize(1).setISTER(ISTERProvider::fluidTank));
    }

    @Nonnull
    public FluidTankTier getTier() {
        return Attribute.getTier(getBlock(), FluidTankTier.class);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addStats(@Nonnull ItemStack stack, World world, @Nonnull List<ITextComponent> tooltip, @Nonnull ITooltipFlag flag) {
        FluidStack fluidStack = StorageUtils.getStoredFluidFromNBT(stack);
        if (fluidStack.isEmpty()) {
            tooltip.add(MekanismLang.EMPTY.translateColored(EnumColor.DARK_RED));
        } else if (fluidStack.getAmount() == Integer.MAX_VALUE) {
            tooltip.add(MekanismLang.GENERIC_STORED.translateColored(EnumColor.PINK, fluidStack, EnumColor.GRAY, MekanismLang.INFINITE));
        } else {
            tooltip.add(MekanismLang.GENERIC_STORED_MB.translateColored(EnumColor.PINK, fluidStack, EnumColor.GRAY, fluidStack.getAmount()));
        }
        int cap = getTier().getStorage();
        if (cap == Integer.MAX_VALUE) {
            tooltip.add(MekanismLang.CAPACITY.translateColored(EnumColor.INDIGO, EnumColor.GRAY, MekanismLang.INFINITE));
        } else {
            tooltip.add(MekanismLang.CAPACITY_MB.translateColored(EnumColor.INDIGO, EnumColor.GRAY, cap));
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addDetails(@Nonnull ItemStack stack, World world, @Nonnull List<ITextComponent> tooltip, @Nonnull ITooltipFlag flag) {
        tooltip.add(OwnerDisplay.of(Minecraft.getInstance().player, getOwnerUUID(stack)).getTextComponent());
        tooltip.add(MekanismLang.SECURITY.translateColored(EnumColor.GRAY, SecurityUtils.getSecurity(stack, Dist.CLIENT)));
        if (SecurityUtils.isOverridden(stack, Dist.CLIENT)) {
            tooltip.add(MekanismLang.SECURITY_OVERRIDDEN.translateColored(EnumColor.RED));
        }
        tooltip.add(MekanismLang.BUCKET_MODE.translateColored(EnumColor.INDIGO, YesNo.of(getBucketMode(stack))));
        tooltip.add(MekanismLang.HAS_INVENTORY.translateColored(EnumColor.AQUA, EnumColor.GRAY, YesNo.of(hasInventory(stack))));
    }

    @Nonnull
    @Override
    public ActionResultType onItemUse(ItemUseContext context) {
        PlayerEntity player = context.getPlayer();
        if (player == null) {
            return ActionResultType.PASS;
        }
        ItemStack stack = player.getHeldItem(context.getHand());
        if (getBucketMode(stack)) {
            return ActionResultType.PASS;
        }
        return super.onItemUse(context);
    }

    @Nonnull
    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, @Nonnull Hand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (getBucketMode(stack)) {
            if (SecurityUtils.canAccess(player, stack)) {
                RayTraceResult rayTraceResult = rayTrace(world, player, !player.isShiftKeyDown() ? FluidMode.SOURCE_ONLY : FluidMode.NONE);
                //It can be null if there is nothing in range
                if (rayTraceResult.getType() == Type.BLOCK) {
                    BlockRayTraceResult result = (BlockRayTraceResult) rayTraceResult;
                    BlockPos pos = result.getPos();
                    if (!world.isBlockModifiable(player, pos)) {
                        return new ActionResult<>(ActionResultType.FAIL, stack);
                    }
                    Optional<IFluidHandlerItem> capability = MekanismUtils.toOptional(FluidUtil.getFluidHandler(stack));
                    if (!capability.isPresent()) {
                        //If something went wrong and we don't have a fluid handler on our tank, then fail
                        return new ActionResult<>(ActionResultType.FAIL, stack);
                    }
                    IFluidHandlerItem fluidHandlerItem = capability.get();
                    if (!(fluidHandlerItem instanceof IMekanismFluidHandler)) {
                        //TODO: Decide if we want to support someone replacing our fluid handler with another?
                        //If it isn't one of our fluid handlers fail
                        return new ActionResult<>(ActionResultType.FAIL, stack);
                    }
                    IExtendedFluidTank fluidTank = ((IMekanismFluidHandler) fluidHandlerItem).getFluidTank(0, null);
                    if (fluidTank == null) {
                        //If something went wrong and we don't have a fluid tank fail
                        return new ActionResult<>(ActionResultType.FAIL, stack);
                    }
                    if (!player.isShiftKeyDown()) {
                        if (!player.canPlayerEdit(pos, result.getFace(), stack)) {
                            return new ActionResult<>(ActionResultType.FAIL, stack);
                        }
                        IFluidState fluidState = world.getFluidState(pos);
                        if (!fluidState.isEmpty() && fluidState.isSource()) {
                            //Just in case someone does weird things and has a fluid state that is empty and a source
                            // only allow collecting from non empty sources
                            //TODO: Move some of this back into a util method in MekanismUtils?
                            // This is semi similar to the code in TileEntityElectricPump
                            Fluid fluid = fluidState.getFluid();
                            FluidStack fluidStack = new FluidStack(fluid, FluidAttributes.BUCKET_VOLUME);
                            //Note: we get the block state from the world and not the fluid state
                            // so that we can get the proper block in case it is fluid logged
                            BlockState blockState = world.getBlockState(pos);
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
                                          fluidState.getFluid(), pos, world, fluid);
                                    return new ActionResult<>(ActionResultType.FAIL, stack);
                                }
                            }
                            if (validFluid(fluidTank, fluidStack)) {
                                if (fluidTank.isEmpty()) {
                                    fluidTank.setStack(fluidStack);
                                } else {
                                    //Grow the stack
                                    if (fluidTank.growStack(fluidStack.getAmount(), Action.EXECUTE) != fluidStack.getAmount()) {
                                        //TODO: Print warning/error
                                    }
                                }
                                return new ActionResult<>(ActionResultType.SUCCESS, stack);
                            } else {
                                return new ActionResult<>(ActionResultType.FAIL, stack);
                            }
                        }
                    } else {
                        if (fluidTank.extract(FluidAttributes.BUCKET_VOLUME, Action.SIMULATE, AutomationType.MANUAL).getAmount() < FluidAttributes.BUCKET_VOLUME
                            || !player.canPlayerEdit(pos.offset(result.getFace()), result.getFace(), stack)) {
                            return new ActionResult<>(ActionResultType.FAIL, stack);
                        }
                        if (tryPlaceContainedLiquid(player, world, pos, fluidHandlerItem.getFluidInTank(0), result.getFace())) {
                            if (!player.isCreative()) {
                                if (fluidTank.shrinkStack(FluidAttributes.BUCKET_VOLUME, Action.EXECUTE) != FluidAttributes.BUCKET_VOLUME) {
                                    //TODO: Print warning/error
                                }
                            }
                            return new ActionResult<>(ActionResultType.SUCCESS, stack);
                        }
                    }
                }
            } else {
                SecurityUtils.displayNoAccess(player);
            }
        }
        return new ActionResult<>(ActionResultType.PASS, stack);
    }

    private boolean tryPlaceContainedLiquid(@Nullable PlayerEntity player, World world, BlockPos pos, @Nonnull FluidStack fluidStack, @Nullable Direction side) {
        Fluid fluid = fluidStack.getFluid();
        if (!fluid.getAttributes().canBePlacedInWorld(world, pos, fluidStack)) {
            //If there is no fluid or it cannot be placed in the world just
            return false;
        }
        BlockState state = world.getBlockState(pos);
        boolean isReplaceable = state.isReplaceable(fluid);
        boolean canContainFluid = state.getBlock() instanceof ILiquidContainer && ((ILiquidContainer) state.getBlock()).canContainFluid(world, pos, state, fluid);
        if (world.isAirBlock(pos) || isReplaceable || canContainFluid) {
            if (world.getDimension().doesWaterVaporize() && fluid.isIn(FluidTags.WATER)) {
                world.playSound(player, pos, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 0.5F, 2.6F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.8F);
                for (int l = 0; l < 8; l++) {
                    world.addParticle(ParticleTypes.LARGE_SMOKE, pos.getX() + Math.random(), pos.getY() + Math.random(), pos.getZ() + Math.random(), 0, 0, 0);
                }
            } else if (canContainFluid) {
                if (((ILiquidContainer) state.getBlock()).receiveFluid(world, pos, state, ((FlowingFluid) fluid).getStillFluidState(false))) {
                    playEmptySound(player, world, pos, fluidStack);
                }
            } else {
                if (!world.isRemote && isReplaceable && !state.getMaterial().isLiquid()) {
                    world.destroyBlock(pos, true);
                }
                playEmptySound(player, world, pos, fluidStack);
                world.setBlockState(pos, fluid.getDefaultState().getBlockState(), BlockFlags.DEFAULT_AND_RERENDER);
            }
            return true;
        }
        return side != null && tryPlaceContainedLiquid(player, world, pos.offset(side), fluidStack, null);
    }

    private void playEmptySound(@Nullable PlayerEntity player, IWorld worldIn, BlockPos pos, @Nonnull FluidStack fluidStack) {
        SoundEvent soundevent = fluidStack.getFluid().getAttributes().getEmptySound();
        if (soundevent == null) {
            soundevent = fluidStack.getFluid().isIn(FluidTags.LAVA) ? SoundEvents.ITEM_BUCKET_EMPTY_LAVA : SoundEvents.ITEM_BUCKET_EMPTY;
        }
        worldIn.playSound(player, pos, soundevent, SoundCategory.BLOCKS, 1.0F, 1.0F);
    }

    private boolean validFluid(@Nonnull IExtendedFluidTank fluidTank, @Nonnull FluidStack fluidStack) {
        return !fluidStack.isEmpty() && fluidTank.insert(fluidStack, Action.SIMULATE, AutomationType.MANUAL).isEmpty();
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
    public void handlePacketData(IWorld world, ItemStack stack, PacketBuffer dataStream) {
        if (!world.isRemote()) {
            setBucketMode(stack, dataStream.readBoolean());
        }
    }
}