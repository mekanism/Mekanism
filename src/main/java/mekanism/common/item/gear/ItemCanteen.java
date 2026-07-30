package mekanism.common.item.gear;

import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.proxy.AutomatedResourceHandler;
import mekanism.common.component.containers.type.ContainerType;
import mekanism.common.config.MekanismConfig;
import mekanism.common.item.interfaces.IFluidItem;
import mekanism.common.lib.transaction.TransactionHelper;
import mekanism.common.registries.MekanismFluids;
import mekanism.common.util.ItemAccessUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.StorageUtils;
import net.minecraft.SharedConstants;
import net.minecraft.core.Holder;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodConstants;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

public class ItemCanteen extends Item implements IFluidItem {

    public ItemCanteen(Properties properties) {
        super(properties.rarity(Rarity.UNCOMMON).stacksTo(1).setNoCombineRepair());
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return StorageUtils.isBarVisible(stack);
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return StorageUtils.getBarWidth(stack);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return ContainerType.FLUID.getRGBDurabilityForDisplay(stack);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level world, LivingEntity entityLiving) {
        if (!world.isClientSide() && entityLiving instanceof Player player) {
            ResourceHandler<FluidResource> fluidHandler = AutomatedResourceHandler.manual(Capabilities.FLUID.getCapability(ItemAccess.forStack(stack)));
            if (fluidHandler != null) {
                FluidResource paste = MekanismFluids.NUTRITIONAL_PASTE.asResource();
                int missingFood = FoodConstants.MAX_FOOD - player.getFoodData().getFoodLevel();
                int pastePerFood = MekanismConfig.general.nutritionalPasteMBPerFood.get();
                int foodToFill;
                //Protect against any mods that might be doing transactional logic, such as if an auto clicker validates it has enough energy before calling this method
                try (Transaction simulation = TransactionHelper.openTransactionSafe()) {
                    foodToFill = fluidHandler.extract(paste, missingFood * pastePerFood, simulation) / pastePerFood;
                }
                if (foodToFill > 0) {
                    int pasteToUse = foodToFill * pastePerFood;
                    //Protect against any mods that might be doing transactional logic, such as if an auto clicker validates it has enough energy before calling this method
                    try (Transaction transaction = TransactionHelper.openTransactionSafe()) {
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
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        //Based off of Consumable#consumeTicks
        return (int) (SharedConstants.TICKS_PER_SECOND * Consumable.DEFAULT_CONSUME_SECONDS);
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.DRINK;
    }

    @Override
    public InteractionResult use(Level worldIn, Player player, InteractionHand hand) {
        if (!MekanismUtils.isPlayingMode(player)) {
            return InteractionResult.PASS;
        } else if (player.canEat(false)) {
            ResourceHandler<FluidResource> fluidHandler = AutomatedResourceHandler.manual(Capabilities.FLUID.getCapability(ItemAccessUtils.playerHandAccess(player, hand)));
            if (fluidHandler != null) {
                //Protect against any mods that might be doing transactional logic, such as if an auto clicker validates it has enough energy before calling this method
                try (Transaction simulation = TransactionHelper.openTransactionSafe()) {
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

    @Override
    public Holder<Fluid> getFluidType() {
        return MekanismFluids.NUTRITIONAL_PASTE;
    }
}
