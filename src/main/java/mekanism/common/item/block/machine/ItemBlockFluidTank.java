package mekanism.common.item.block.machine;

import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Coord4D;
import mekanism.api.text.EnumColor;
import mekanism.client.render.item.block.RenderFluidTankItem;
import mekanism.common.base.FluidItemWrapper;
import mekanism.common.base.IFluidItemWrapper;
import mekanism.common.base.IItemNetwork;
import mekanism.common.block.machine.BlockFluidTank;
import mekanism.common.capabilities.ItemCapabilityWrapper;
import mekanism.common.item.IItemSustainedInventory;
import mekanism.common.item.IItemSustainedTank;
import mekanism.common.item.ITieredItem;
import mekanism.common.item.block.ItemBlockAdvancedTooltip;
import mekanism.common.security.ISecurityItem;
import mekanism.common.tier.BaseTier;
import mekanism.common.tier.FluidTankTier;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.PipeUtils;
import mekanism.common.util.SecurityUtils;
import mekanism.common.util.text.BooleanStateDisplay.YesNo;
import mekanism.common.util.text.OwnerDisplay;
import mekanism.common.util.text.TextComponentUtil;
import mekanism.common.util.text.Translation;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext.FluidMode;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;

public class ItemBlockFluidTank extends ItemBlockAdvancedTooltip<BlockFluidTank> implements IItemSustainedInventory, IItemSustainedTank, IFluidItemWrapper, ISecurityItem,
      IItemNetwork, ITieredItem<FluidTankTier> {

    public ItemBlockFluidTank(BlockFluidTank block) {
        super(block, new Item.Properties().maxStackSize(1).setTEISR(() -> RenderFluidTankItem::new));
    }

    @Nullable
    @Override
    public FluidTankTier getTier(@Nonnull ItemStack stack) {
        Item item = stack.getItem();
        if (item instanceof ItemBlockFluidTank) {
            return ((ItemBlockFluidTank) item).getBlock().getTier();
        }
        return null;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addStats(@Nonnull ItemStack itemstack, World world, @Nonnull List<ITextComponent> tooltip, @Nonnull ITooltipFlag flag) {
        FluidStack fluidStack = getFluidStack(itemstack);
        if (!fluidStack.isEmpty()) {
            int amount = fluidStack.getAmount();
            if (amount == Integer.MAX_VALUE) {
                tooltip.add(TextComponentUtil.build(EnumColor.PINK, fluidStack, ": ", EnumColor.GRAY, amount + "mB"));
            } else {
                tooltip.add(TextComponentUtil.build(EnumColor.PINK, fluidStack, ": ", EnumColor.GRAY, Translation.of("gui.mekanism.infinite")));
            }
        } else {
            tooltip.add(TextComponentUtil.build(EnumColor.DARK_RED, Translation.of("gui.mekanism.empty"), "."));
        }
        FluidTankTier tier = getTier(itemstack);
        if (tier != null) {
            int cap = tier.getStorage();
            if (cap == Integer.MAX_VALUE) {
                tooltip.add(TextComponentUtil.build(EnumColor.INDIGO, Translation.of("tooltip.mekanism.capacity"), ": ", EnumColor.GRAY,
                      Translation.of("gui.mekanism.infinite")));
            } else {
                tooltip.add(TextComponentUtil.build(EnumColor.INDIGO, Translation.of("tooltip.mekanism.capacity"), ": ", EnumColor.GRAY, cap + " mB"));
            }
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addDetails(@Nonnull ItemStack itemstack, World world, @Nonnull List<ITextComponent> tooltip, @Nonnull ITooltipFlag flag) {
        tooltip.add(OwnerDisplay.of(Minecraft.getInstance().player, getOwnerUUID(itemstack)).getTextComponent());
        tooltip.add(TextComponentUtil.build(EnumColor.GRAY, Translation.of("gui.mekanism.security"), ": ", SecurityUtils.getSecurity(itemstack, Dist.CLIENT)));
        if (SecurityUtils.isOverridden(itemstack, Dist.CLIENT)) {
            tooltip.add(TextComponentUtil.build(EnumColor.RED, "(", Translation.of("gui.mekanism.overridden"), ")"));
        }
        tooltip.add(TextComponentUtil.build(EnumColor.INDIGO, Translation.of("tooltip.mekanism.portableTank.bucketMode", YesNo.of(getBucketMode(itemstack)))));
        ListNBT inventory = getInventory(itemstack);
        tooltip.add(TextComponentUtil.build(EnumColor.AQUA, Translation.of("tooltip.mekanism.inventory"), ": ", EnumColor.GRAY,
              YesNo.of(inventory != null && !inventory.isEmpty())));
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

    public boolean tryPlaceContainedLiquid(World world, ItemStack itemstack, BlockPos pos) {
        FluidStack fluidStack = getFluidStack(itemstack);
        if (fluidStack.isEmpty() || !fluidStack.getFluid().getAttributes().canBePlacedInWorld(world, pos, fluidStack)) {
            return false;
        }
        Material material = world.getBlockState(pos).getMaterial();
        boolean flag = !material.isSolid();
        if (!world.isAirBlock(pos) && !flag) {
            return false;
        }
        if (world.getDimension().doesWaterVaporize() && fluidStack.getFluid() == Fluids.WATER) {
            world.playSound(null, pos.getX() + 0.5F, pos.getY() + 0.5F, pos.getZ() + 0.5F, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 0.5F,
                  2.6F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.8F);
            for (int l = 0; l < 8; l++) {
                world.addParticle(ParticleTypes.LARGE_SMOKE, pos.getX() + Math.random(),
                      pos.getY() + Math.random(), pos.getZ() + Math.random(), 0.0D, 0.0D, 0.0D);
            }
        } else {
            if (!world.isRemote && flag && !material.isLiquid()) {
                world.destroyBlock(pos, true);
            }
            world.setBlockState(pos, MekanismUtils.getFlowingBlockState(fluidStack));
        }
        return true;
    }

    @Nonnull
    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity entityplayer, @Nonnull Hand hand) {
        ItemStack itemstack = entityplayer.getHeldItem(hand);
        if (getBucketMode(itemstack)) {
            if (SecurityUtils.canAccess(entityplayer, itemstack)) {
                //TODO: Handle picking up and putting down, for now just setting it as source only but this is almost certainly wrong
                RayTraceResult rayTraceResult = rayTrace(world, entityplayer, FluidMode.SOURCE_ONLY);
                //It can be null if there is nothing in range
                if (rayTraceResult != null && rayTraceResult instanceof BlockRayTraceResult) {
                    BlockRayTraceResult pos = (BlockRayTraceResult) rayTraceResult;
                    Coord4D coord = new Coord4D(pos.getPos(), world);
                    if (!world.getDimension().canMineBlock(entityplayer, coord.getPos())) {
                        return new ActionResult<>(ActionResultType.FAIL, itemstack);
                    }
                    if (!entityplayer.isSneaking()) {
                        if (!entityplayer.canPlayerEdit(coord.getPos(), pos.getFace(), itemstack)) {
                            return new ActionResult<>(ActionResultType.FAIL, itemstack);
                        }
                        FluidStack fluid = MekanismUtils.getFluid(world, coord, false);
                        if (!fluid.isEmpty()) {
                            FluidStack stored = getFluidStack(itemstack);
                            if (stored.isEmpty() || getFluidStack(itemstack).isFluidEqual(fluid)) {
                                int needed = getCapacity(itemstack) - stored.getAmount();
                                if (fluid.getAmount() > needed) {
                                    return new ActionResult<>(ActionResultType.FAIL, itemstack);
                                }
                                if (stored.isEmpty()) {
                                    setFluidStack(fluid, itemstack);
                                } else {
                                    FluidStack newStack = getFluidStack(itemstack);
                                    newStack.setAmount(newStack.getAmount() + fluid.getAmount());
                                    setFluidStack(newStack, itemstack);
                                }
                                world.removeBlock(coord.getPos(), false);
                            }
                        }
                    } else {
                        FluidStack stored = getFluidStack(itemstack);
                        if (stored.getAmount() < FluidAttributes.BUCKET_VOLUME) {
                            return new ActionResult<>(ActionResultType.FAIL, itemstack);
                        }
                        Coord4D trans = coord.offset(pos.getFace());
                        if (!entityplayer.canPlayerEdit(trans.getPos(), pos.getFace(), itemstack)) {
                            return new ActionResult<>(ActionResultType.FAIL, itemstack);
                        }
                        if (tryPlaceContainedLiquid(world, itemstack, trans.getPos())
                            && !entityplayer.isCreative()) {
                            FluidStack newStack = stored.copy();
                            newStack.setAmount(newStack.getAmount() - FluidAttributes.BUCKET_VOLUME);
                            setFluidStack(newStack.getAmount() > 0 ? newStack : FluidStack.EMPTY, itemstack);
                        }
                    }
                }
                return new ActionResult<>(ActionResultType.SUCCESS, itemstack);
            } else {
                SecurityUtils.displayNoAccess(entityplayer);
            }
        }
        return new ActionResult<>(ActionResultType.PASS, itemstack);
    }

    public void setBucketMode(ItemStack itemStack, boolean bucketMode) {
        ItemDataUtils.setBoolean(itemStack, "bucketMode", bucketMode);
    }

    public boolean getBucketMode(ItemStack itemStack) {
        return ItemDataUtils.getBoolean(itemStack, "bucketMode");
    }

    @Nonnull
    @Override
    public FluidStack getFluid(ItemStack container) {
        return getFluidStack(container);
    }

    @Override
    public int getCapacity(ItemStack container) {
        FluidTankTier tier = getTier(container);
        return tier == null ? 0 : tier.getStorage();
    }

    @Override
    public int fill(ItemStack container, @Nonnull FluidStack resource, FluidAction fluidAction) {
        if (resource.isEmpty()) {
            return 0;
        }
        if (getBaseTier(container) == BaseTier.CREATIVE) {
            setFluidStack(PipeUtils.copy(resource, Integer.MAX_VALUE), container);
            return resource.getAmount();
        }
        FluidStack stored = getFluidStack(container);
        int toFill;
        if (!stored.isEmpty() && stored.getFluid() != resource.getFluid()) {
            return 0;
        }
        if (stored.isEmpty()) {
            toFill = Math.min(resource.getAmount(), getCapacity(container));
        } else {
            toFill = Math.min(resource.getAmount(), getCapacity(container) - stored.getAmount());
        }
        if (fluidAction.execute()) {
            int fillAmount = toFill + stored.getAmount();
            setFluidStack(PipeUtils.copy(resource, fillAmount), container);
        }
        return toFill;
    }

    @Nonnull
    @Override
    public FluidStack drain(ItemStack container, int maxDrain, FluidAction fluidAction) {
        FluidStack stored = getFluidStack(container);
        if (stored.isEmpty()) {
            return FluidStack.EMPTY;
        }
        FluidStack toDrain = PipeUtils.copy(stored, Math.min(stored.getAmount(), maxDrain));
        if (fluidAction.execute() && getBaseTier(container) != BaseTier.CREATIVE) {
            stored.setAmount(stored.getAmount() - toDrain.getAmount());
            setFluidStack(stored.getAmount() > 0 ? stored : FluidStack.EMPTY, container);
        }
        return toDrain;
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, CompoundNBT nbt) {
        return new ItemCapabilityWrapper(stack, new FluidItemWrapper());
    }

    @Override
    public void handlePacketData(IWorld world, ItemStack stack, PacketBuffer dataStream) {
        if (!world.isRemote()) {
            setBucketMode(stack, dataStream.readBoolean());
        }
    }
}