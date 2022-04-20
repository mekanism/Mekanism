package mekanism.common.item.gear;

import java.util.List;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.fluid.IMekanismFluidHandler;
import mekanism.common.MekanismLang;
import mekanism.common.capabilities.ItemCapabilityWrapper.ItemCapability;
import mekanism.common.capabilities.fluid.BasicFluidTank;
import mekanism.common.capabilities.fluid.item.RateLimitFluidHandler;
import mekanism.common.config.MekanismConfig;
import mekanism.common.item.CapabilityItem;
import mekanism.common.registries.MekanismFluids;
import mekanism.common.util.FluidUtils;
import mekanism.common.util.StorageUtils;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

public class ItemCanteen extends CapabilityItem {

    public ItemCanteen(Properties properties) {
        super(properties.rarity(Rarity.UNCOMMON).stacksTo(1).setNoRepair());
    }

    @Override
    public void appendHoverText(@Nonnull ItemStack stack, @Nullable Level world, @Nonnull List<Component> tooltip, @Nonnull TooltipFlag flag) {
        StorageUtils.addStoredFluid(stack, tooltip, true, MekanismLang.EMPTY);
    }

    @Override
    public boolean isBarVisible(@Nonnull ItemStack stack) {
        return true;
    }

    @Override
    public int getBarWidth(@Nonnull ItemStack stack) {
        return StorageUtils.getBarWidth(stack);
    }

    @Override
    public int getBarColor(@Nonnull ItemStack stack) {
        return FluidUtils.getRGBDurabilityForDisplay(stack).orElse(0);
    }

    @Override
    public void fillItemCategory(@Nonnull CreativeModeTab group, @Nonnull NonNullList<ItemStack> items) {
        super.fillItemCategory(group, items);
        if (allowdedIn(group)) {
            items.add(FluidUtils.getFilledVariant(new ItemStack(this), MekanismConfig.gear.canteenMaxStorage.get(), MekanismFluids.NUTRITIONAL_PASTE));
        }
    }

    @Nonnull
    @Override
    public ItemStack finishUsingItem(@Nonnull ItemStack stack, @Nonnull Level world, @Nonnull LivingEntity entityLiving) {
        if (!world.isClientSide && entityLiving instanceof Player player) {
            int needed = Math.min(20 - player.getFoodData().getFoodLevel(), getFluid(stack).getAmount() / MekanismConfig.general.nutritionalPasteMBPerFood.get());
            if (needed > 0) {
                player.getFoodData().eat(needed, needed * MekanismConfig.general.nutritionalPasteSaturation.get());
                FluidUtil.getFluidHandler(stack).ifPresent(handler -> handler.drain(needed * MekanismConfig.general.nutritionalPasteMBPerFood.get(),
                      FluidAction.EXECUTE));
                world.gameEvent(entityLiving, GameEvent.DRINKING_FINISH, entityLiving.eyeBlockPosition());
            }
        }
        return stack;
    }

    @Override
    public int getUseDuration(@Nonnull ItemStack stack) {
        return 32;
    }

    @Nonnull
    @Override
    public UseAnim getUseAnimation(@Nonnull ItemStack stack) {
        return UseAnim.DRINK;
    }

    @Override
    protected void gatherCapabilities(List<ItemCapability> capabilities, ItemStack stack, CompoundTag nbt) {
        super.gatherCapabilities(capabilities, stack, nbt);
        capabilities.add(RateLimitFluidHandler.create(MekanismConfig.gear.canteenTransferRate, MekanismConfig.gear.canteenMaxStorage,
              BasicFluidTank.alwaysTrueBi, BasicFluidTank.alwaysTrueBi, fluid -> fluid.getFluid() == MekanismFluids.NUTRITIONAL_PASTE.getFluid()));
    }

    private FluidStack getFluid(ItemStack stack) {
        Optional<IFluidHandlerItem> capability = FluidUtil.getFluidHandler(stack).resolve();
        if (capability.isPresent()) {
            IFluidHandlerItem fluidHandlerItem = capability.get();
            if (fluidHandlerItem instanceof IMekanismFluidHandler fluidHandler) {
                IExtendedFluidTank fluidTank = fluidHandler.getFluidTank(0, null);
                if (fluidTank != null) {
                    return fluidTank.getFluid();
                }
            }
            return fluidHandlerItem.getFluidInTank(0);
        }
        return FluidStack.EMPTY;
    }

    @Nonnull
    @Override
    public InteractionResultHolder<ItemStack> use(@Nonnull Level worldIn, Player playerIn, @Nonnull InteractionHand handIn) {
        if (!playerIn.isCreative() && playerIn.canEat(false) && getFluid(playerIn.getItemInHand(handIn)).getAmount() >= 50) {
            playerIn.startUsingItem(handIn);
        }
        return InteractionResultHolder.success(playerIn.getItemInHand(handIn));
    }
}
