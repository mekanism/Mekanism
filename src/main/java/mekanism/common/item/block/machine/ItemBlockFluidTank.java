package mekanism.common.item.block.machine;

import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Coord4D;
import mekanism.api.text.EnumColor;
import mekanism.common.base.FluidItemWrapper;
import mekanism.common.base.IFluidItemWrapper;
import mekanism.common.base.IItemNetwork;
import mekanism.common.block.machine.BlockFluidTank;
import mekanism.common.capabilities.ItemCapabilityWrapper;
import mekanism.common.item.IItemRedirectedModel;
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
import mekanism.common.util.text.BooleanStateDisplay;
import mekanism.common.util.text.OwnerDisplay;
import mekanism.common.util.text.TextComponentUtil;
import mekanism.common.util.text.Translation;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
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
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

public class ItemBlockFluidTank extends ItemBlockAdvancedTooltip<BlockFluidTank> implements IItemSustainedInventory, IItemSustainedTank, IFluidItemWrapper, ISecurityItem,
      IItemNetwork, ITieredItem<FluidTankTier>, IItemRedirectedModel {

    public ItemBlockFluidTank(BlockFluidTank block) {
        super(block, new Item.Properties().maxStackSize(1));
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
        if (fluidStack != null) {
            int amount = fluidStack.amount;
            if (amount == Integer.MAX_VALUE) {
                tooltip.add(TextComponentUtil.build(EnumColor.PINK, fluidStack, ": ", EnumColor.GRAY, amount + "mB"));
            } else {
                tooltip.add(TextComponentUtil.build(EnumColor.PINK, fluidStack, ": ", EnumColor.GRAY, Translation.of("mekanism.gui.infinite")));
            }
        } else {
            tooltip.add(TextComponentUtil.build(EnumColor.DARK_RED, Translation.of("mekanism.gui.empty"), "."));
        }
        FluidTankTier tier = getTier(itemstack);
        if (tier != null) {
            int cap = tier.getStorage();
            if (cap == Integer.MAX_VALUE) {
                tooltip.add(TextComponentUtil.build(EnumColor.INDIGO, Translation.of("mekanism.tooltip.capacity"), ": ", EnumColor.GRAY,
                      Translation.of("mekanism.gui.infinite")));
            } else {
                tooltip.add(TextComponentUtil.build(EnumColor.INDIGO, Translation.of("mekanism.tooltip.capacity"), ": ", EnumColor.GRAY, cap + " mB"));
            }
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addDetails(@Nonnull ItemStack itemstack, World world, @Nonnull List<ITextComponent> tooltip, @Nonnull ITooltipFlag flag) {
        tooltip.add(TextComponentUtil.build(OwnerDisplay.of(Minecraft.getInstance().player, getOwnerUUID(itemstack))));
        tooltip.add(TextComponentUtil.build(EnumColor.GRAY, Translation.of("mekanism.gui.security"), ": ", SecurityUtils.getSecurity(itemstack, Dist.CLIENT)));
        if (SecurityUtils.isOverridden(itemstack, Dist.CLIENT)) {
            tooltip.add(TextComponentUtil.build(EnumColor.RED, "(", Translation.of("mekanism.gui.overridden"), ")"));
        }
        tooltip.add(TextComponentUtil.build(EnumColor.INDIGO, Translation.of("mekanism.tooltip.portableTank.bucketMode", BooleanStateDisplay.YesNo.of(getBucketMode(itemstack)))));
        ListNBT inventory = getInventory(itemstack);
        tooltip.add(TextComponentUtil.build(EnumColor.AQUA, Translation.of("mekanism.tooltip.inventory"), ": ", EnumColor.GRAY,
              BooleanStateDisplay.YesNo.of(inventory != null && !inventory.isEmpty())));
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
        if (getFluidStack(itemstack) == null || !getFluidStack(itemstack).getFluid().canBePlacedInWorld()) {
            return false;
        }
        Material material = world.getBlockState(pos).getMaterial();
        boolean flag = !material.isSolid();
        if (!world.isAirBlock(pos) && !flag) {
            return false;
        }
        if (world.getDimension().doesWaterVaporize() && getFluidStack(itemstack).getFluid() == FluidRegistry.WATER) {
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
            world.setBlockState(pos, MekanismUtils.getFlowingBlock(getFluidStack(itemstack).getFluid()).getDefaultState(), 3);
        }
        return true;
    }

    @Nonnull
    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity entityplayer, @Nonnull Hand hand) {
        ItemStack itemstack = entityplayer.getHeldItem(hand);
        if (getBucketMode(itemstack)) {
            if (SecurityUtils.canAccess(entityplayer, itemstack)) {
                RayTraceResult rayTraceResult = rayTrace(world, entityplayer, !entityplayer.isSneaking());
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
                        if (fluid != null && (getFluidStack(itemstack) == null || getFluidStack(itemstack).isFluidEqual(fluid))) {
                            int needed = getCapacity(itemstack) - (getFluidStack(itemstack) != null ? getFluidStack(itemstack).amount : 0);
                            if (fluid.amount > needed) {
                                return new ActionResult<>(ActionResultType.FAIL, itemstack);
                            }
                            if (getFluidStack(itemstack) == null) {
                                setFluidStack(fluid, itemstack);
                            } else {
                                FluidStack newStack = getFluidStack(itemstack);
                                newStack.amount += fluid.amount;
                                setFluidStack(newStack, itemstack);
                            }
                            world.removeBlock(coord.getPos(), false);
                        }
                    } else {
                        FluidStack stored = getFluidStack(itemstack);
                        if (stored == null || stored.amount < Fluid.BUCKET_VOLUME) {
                            return new ActionResult<>(ActionResultType.FAIL, itemstack);
                        }
                        Coord4D trans = coord.offset(pos.getFace());
                        if (!entityplayer.canPlayerEdit(trans.getPos(), pos.getFace(), itemstack)) {
                            return new ActionResult<>(ActionResultType.FAIL, itemstack);
                        }
                        if (tryPlaceContainedLiquid(world, itemstack, trans.getPos())
                            && !entityplayer.isCreative()) {
                            FluidStack newStack = stored.copy();
                            newStack.amount -= Fluid.BUCKET_VOLUME;
                            setFluidStack(newStack.amount > 0 ? newStack : null, itemstack);
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
    public int fill(ItemStack container, FluidStack resource, boolean doFill) {
        if (resource != null) {
            if (getBaseTier(container) == BaseTier.CREATIVE) {
                setFluidStack(PipeUtils.copy(resource, Integer.MAX_VALUE), container);
                return resource.amount;
            }
            FluidStack stored = getFluidStack(container);
            int toFill;
            if (stored != null && stored.getFluid() != resource.getFluid()) {
                return 0;
            }
            if (stored == null) {
                toFill = Math.min(resource.amount, getCapacity(container));
            } else {
                toFill = Math.min(resource.amount, getCapacity(container) - stored.amount);
            }
            if (doFill) {
                int fillAmount = toFill + (stored == null ? 0 : stored.amount);
                setFluidStack(PipeUtils.copy(resource, fillAmount), container);
            }
            return toFill;
        }
        return 0;
    }

    @Override
    public FluidStack drain(ItemStack container, int maxDrain, boolean doDrain) {
        FluidStack stored = getFluidStack(container);
        if (stored != null) {
            FluidStack toDrain = PipeUtils.copy(stored, Math.min(stored.amount, maxDrain));
            if (doDrain && getBaseTier(container) != BaseTier.CREATIVE) {
                stored.amount -= toDrain.amount;
                setFluidStack(stored.amount > 0 ? stored : null, container);
            }
            return toDrain;
        }
        return null;
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

    @Nonnull
    @Override
    public String getRedirectLocation() {
        return "fluid_tank";
    }
}