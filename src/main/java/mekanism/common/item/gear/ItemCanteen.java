package mekanism.common.item.gear;

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
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodConstants;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jetbrains.annotations.NotNull;

public class ItemCanteen extends Item implements ICustomCreativeTabContents {

    public ItemCanteen(Properties properties) {
        super(properties.rarity(Rarity.UNCOMMON).stacksTo(1).setNoCombineRepair());
    }

    @Override
    @Deprecated
    public void appendHoverText(@NotNull ItemStack stack, @NotNull Item.TooltipContext context, @NotNull TooltipDisplay tooltipDisplay, @NotNull Consumer<Component> tooltipAdder, @NotNull TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltipDisplay, tooltipAdder, flag);
        StorageUtils.addStoredFluid(ItemAccess.forStack(stack), tooltipAdder, MekanismLang.EMPTY);
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
        return FluidUtils.getRGBDurabilityForDisplay(ItemAccess.forStack(stack));
    }

    @Override
    public void addItems(Holder<Item> item, Consumer<ItemStack> tabOutput) {
        tabOutput.accept(FluidUtils.getFilledVariant(item, MekanismFluids.NUTRITIONAL_PASTE));
    }

    @NotNull
    @Override
    public ItemStack finishUsingItem(@NotNull ItemStack stack, @NotNull Level world, @NotNull LivingEntity entityLiving) {
        if (!world.isClientSide() && entityLiving instanceof Player player) {
            ResourceHandler<FluidResource> fluidHandler = Capabilities.FLUID.getCapability(ItemAccess.forStack(stack));
            if (fluidHandler != null) {
                FluidResource paste = MekanismFluids.NUTRITIONAL_PASTE.asResource();
                int missingFood = FoodConstants.MAX_FOOD - player.getFoodData().getFoodLevel();
                int pastePerFood = MekanismConfig.general.nutritionalPasteMBPerFood.get();
                int foodToFill;
                try (Transaction simulation = Transaction.openRoot()) {
                    //TODO - 26.1: Is there a worry of this multiplication overflowing?
                    foodToFill = fluidHandler.extract(paste, missingFood * pastePerFood, simulation) / pastePerFood;
                }
                if (foodToFill > 0) {
                    int pasteToUse = foodToFill * pastePerFood;
                    try (Transaction transaction = Transaction.openRoot()) {
                        int extracted = fluidHandler.extract(paste, pasteToUse, transaction);
                        if (extracted == pasteToUse) {
                            //Note: This if statement should always be true given we already simulated that we could extract at least this much,
                            // but we validate it just in case before actually committing any changes
                            player.getFoodData().eat(foodToFill, MekanismConfig.general.nutritionalPasteSaturation.get());
                            entityLiving.gameEvent(GameEvent.DRINK);
                            transaction.commit();
                        }
                    }
                }
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
    public ItemUseAnimation getUseAnimation(@NotNull ItemStack stack) {
        return ItemUseAnimation.DRINK;
    }

    @NotNull
    @Override
    public InteractionResult use(@NotNull Level worldIn, Player player, @NotNull InteractionHand hand) {
        if (!MekanismUtils.isPlayingMode(player)) {
            return InteractionResult.PASS;
        }
        if (player.canEat(false)) {
            ResourceHandler<FluidResource> fluidHandler = Capabilities.FLUID.getCapability(ItemAccess.forPlayerInteraction(player, hand));
            if (fluidHandler != null) {
                try (Transaction simulation = Transaction.openRoot()) {
                    int pastePerFood = MekanismConfig.general.nutritionalPasteMBPerFood.get();
                    int extracted = fluidHandler.extract(MekanismFluids.NUTRITIONAL_PASTE.asResource(), pastePerFood, simulation);
                    if (extracted == pastePerFood) {
                        //Only allow to start drinking if we have at least enough paste to provide a single point of food
                        player.startUsingItem(hand);
                        return InteractionResult.CONSUME;
                    }
                }
            }
        }
        return InteractionResult.FAIL;
    }
}
