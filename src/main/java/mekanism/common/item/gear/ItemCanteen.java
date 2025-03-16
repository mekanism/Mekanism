package mekanism.common.item.gear;

import java.util.List;
import java.util.function.Consumer;
import mekanism.common.MekanismLang;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.config.MekanismConfig;
import mekanism.common.registration.impl.CreativeTabDeferredRegister.ICustomCreativeTabContents;
import mekanism.common.registries.MekanismFluids;
import mekanism.common.util.FluidUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.StorageUtils;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import org.jetbrains.annotations.NotNull;

public class ItemCanteen extends Item implements ICustomCreativeTabContents {

    public ItemCanteen(Properties properties) {
        super(properties.rarity(Rarity.UNCOMMON).stacksTo(1).setNoRepair());
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull Item.TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        StorageUtils.addStoredFluid(stack, tooltip, MekanismLang.EMPTY);
    }

    @Override
    public boolean isBarVisible(@NotNull ItemStack stack) {
        return true;
    }

    @Override
    public int getBarWidth(@NotNull ItemStack stack) {
        return StorageUtils.getBarWidth(stack);
    }

    @Override
    public int getBarColor(@NotNull ItemStack stack) {
        return FluidUtils.getRGBDurabilityForDisplay(stack);
    }

    @Override
    public void addItems(Holder<Item> item, Consumer<ItemStack> tabOutput) {
        tabOutput.accept(FluidUtils.getFilledVariant(item, MekanismFluids.NUTRITIONAL_PASTE));
    }

    @NotNull
    @Override
    public ItemStack finishUsingItem(@NotNull ItemStack stack, @NotNull Level world, @NotNull LivingEntity entityLiving) {
        if (!world.isClientSide && entityLiving instanceof Player player) {
            int needed = Math.min(20 - player.getFoodData().getFoodLevel(), getFluid(stack).getAmount() / MekanismConfig.general.nutritionalPasteMBPerFood.get());
            if (needed > 0) {
                player.getFoodData().eat(needed, MekanismConfig.general.nutritionalPasteSaturation.get());
                IFluidHandlerItem handler = Capabilities.FLUID.getCapability(stack);
                if (handler != null) {
                    handler.drain(needed * MekanismConfig.general.nutritionalPasteMBPerFood.get(), FluidAction.EXECUTE);
                }
                entityLiving.gameEvent(GameEvent.DRINK);
            }
        }
        return stack;
    }

    @Override
    public int getUseDuration(@NotNull ItemStack stack, @NotNull LivingEntity entity) {
        return 32;
    }

    @NotNull
    @Override
    public UseAnim getUseAnimation(@NotNull ItemStack stack) {
        return UseAnim.DRINK;
    }

    private FluidStack getFluid(ItemStack stack) {
        IFluidHandlerItem fluidHandlerItem = Capabilities.FLUID.getCapability(stack);
        if (fluidHandlerItem != null) {
            return StorageUtils.getContainedFluid(fluidHandlerItem, MekanismFluids.NUTRITIONAL_PASTE.asStack(1));
        }
        return FluidStack.EMPTY;
    }

    @NotNull
    @Override
    public InteractionResultHolder<ItemStack> use(@NotNull Level worldIn, Player player, @NotNull InteractionHand hand) {
        ItemStack item = player.getItemInHand(hand);
        if (!MekanismUtils.isPlayingMode(player)) {
            return InteractionResultHolder.pass(item);
        }
        if (player.canEat(false) && getFluid(item).getAmount() >= 50) {
            player.startUsingItem(hand);
            return InteractionResultHolder.consume(item);
        }
        return InteractionResultHolder.fail(item);
    }
}
