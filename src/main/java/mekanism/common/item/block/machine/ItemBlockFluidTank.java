package mekanism.common.item.block.machine;

import io.netty.buffer.ByteBuf;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Coord4D;
import mekanism.api.EnumColor;
import mekanism.client.MekanismClient;
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
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.PipeUtils;
import mekanism.common.util.SecurityUtils;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ItemBlockFluidTank extends ItemBlockAdvancedTooltip implements IItemSustainedInventory, IItemSustainedTank, IFluidItemWrapper, ISecurityItem, IItemNetwork,
      ITieredItem<FluidTankTier>, IItemRedirectedModel {

    public ItemBlockFluidTank(BlockFluidTank block) {
        super(block);
        setMaxStackSize(1);
    }

    @Nullable
    @Override
    public FluidTankTier getTier(@Nonnull ItemStack stack) {
        Item item = stack.getItem();
        if (item instanceof ItemBlockFluidTank) {
            return ((BlockFluidTank) ((ItemBlockFluidTank) item).block).getTier();
        }
        return null;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addStats(@Nonnull ItemStack itemstack, World world, @Nonnull List<String> list, @Nonnull ITooltipFlag flag) {
        FluidStack fluidStack = getFluidStack(itemstack);
        if (fluidStack != null) {
            int amount = getFluidStack(itemstack).amount;
            String amountStr = amount == Integer.MAX_VALUE ? LangUtils.localize("gui.infinite") : amount + "mB";
            list.add(EnumColor.AQUA + LangUtils.localizeFluidStack(fluidStack) + ": " + EnumColor.GREY + amountStr);
        } else {
            list.add(EnumColor.DARK_RED + LangUtils.localize("gui.empty") + ".");
        }
        FluidTankTier tier = getTier(itemstack);
        if (tier != null) {
            int cap = tier.getStorage();
            list.add(EnumColor.INDIGO + LangUtils.localize("tooltip.capacity") + ": " + EnumColor.GREY + (cap == Integer.MAX_VALUE ? LangUtils.localize("gui.infinite") : cap + " mB"));
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addDetails(@Nonnull ItemStack itemstack, World world, @Nonnull List<String> list, @Nonnull ITooltipFlag flag) {
        list.add(SecurityUtils.getOwnerDisplay(Minecraft.getMinecraft().player, MekanismClient.clientUUIDMap.get(getOwnerUUID(itemstack))));
        list.add(EnumColor.GREY + LangUtils.localize("gui.security") + ": " + SecurityUtils.getSecurityDisplay(itemstack, Side.CLIENT));
        if (SecurityUtils.isOverridden(itemstack, Side.CLIENT)) {
            list.add(EnumColor.RED + "(" + LangUtils.localize("gui.overridden") + ")");
        }
        list.add(EnumColor.INDIGO + LangUtils.localizeWithFormat("mekanism.tooltip.portableTank.bucketMode", LangUtils.transYesNo(getBucketMode(itemstack))));
        list.add(EnumColor.AQUA + LangUtils.localize("tooltip.inventory") + ": " + EnumColor.GREY +
                 LangUtils.transYesNo(getInventory(itemstack) != null && getInventory(itemstack).tagCount() != 0));
    }

    @Nonnull
    @Override
    public ActionResultType onItemUse(PlayerEntity player, World world, @Nonnull BlockPos pos, @Nonnull Hand hand, @Nonnull Direction side, float hitX, float hitY, float hitZ) {
        ItemStack stack = player.getHeldItem(hand);
        if (getBucketMode(stack)) {
            return ActionResultType.PASS;
        }
        return super.onItemUse(player, world, pos, hand, side, hitX, hitY, hitZ);
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
        if (world.provider.doesWaterVaporize() && getFluidStack(itemstack).getFluid() == FluidRegistry.WATER) {
            world.playSound(null, pos.getX() + 0.5F, pos.getY() + 0.5F, pos.getZ() + 0.5F, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 0.5F,
                  2.6F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.8F);
            for (int l = 0; l < 8; l++) {
                world.spawnParticle(EnumParticleTypes.SMOKE_LARGE, pos.getX() + Math.random(),
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
                RayTraceResult pos = rayTrace(world, entityplayer, !entityplayer.isSneaking());
                //It can be null if there is nothing in range
                if (pos != null && pos.typeOfHit == RayTraceResult.Type.BLOCK) {
                    Coord4D coord = new Coord4D(pos.getBlockPos(), world);
                    if (!world.provider.canMineBlock(entityplayer, coord.getPos())) {
                        return new ActionResult<>(ActionResultType.FAIL, itemstack);
                    }
                    if (!entityplayer.isSneaking()) {
                        if (!entityplayer.canPlayerEdit(coord.getPos(), pos.sideHit, itemstack)) {
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
                        Coord4D trans = coord.offset(pos.sideHit);
                        if (!entityplayer.canPlayerEdit(trans.getPos(), pos.sideHit, itemstack)) {
                            return new ActionResult<>(ActionResultType.FAIL, itemstack);
                        }
                        if (tryPlaceContainedLiquid(world, itemstack, trans.getPos())
                            && !entityplayer.capabilities.isCreativeMode) {
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
        return new ItemCapabilityWrapper(stack, new FluidItemWrapper()) {
            @Override
            public boolean hasCapability(@Nonnull Capability<?> capability, Direction facing) {
                return capability == CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY || super.hasCapability(capability, facing);
            }
        };
    }

    @Override
    public void handlePacketData(ItemStack stack, ByteBuf dataStream) {
        if (FMLCommonHandler.instance().getEffectiveSide().isServer()) {
            setBucketMode(stack, dataStream.readBoolean());
        }
    }

    @Nonnull
    @Override
    public String getRedirectLocation() {
        return "fluid_tank";
    }
}