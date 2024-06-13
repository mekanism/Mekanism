package mekanism.common.item;

import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.ICapabilityAware;
import mekanism.common.config.MekanismConfig;
import mekanism.common.util.MekanismUtils;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.capability.wrappers.FluidBucketWrapper;
import org.jetbrains.annotations.NotNull;

public class ItemNutritionalPasteBucket extends BucketItem implements ICapabilityAware {

    public ItemNutritionalPasteBucket(Fluid fluid, Properties builder) {
        super(fluid, builder);
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

    @NotNull
    @Override
    public InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
        if (MekanismUtils.isPlayingMode(player)) {
            int needed = Math.min(20 - player.getFoodData().getFoodLevel(), FluidType.BUCKET_VOLUME / MekanismConfig.general.nutritionalPasteMBPerFood.get());
            if (needed > 0) {
                return ItemUtils.startUsingInstantly(level, player, hand);
            }
        }
        return super.use(level, player, hand);
    }

    @NotNull
    @Override
    public ItemStack finishUsingItem(@NotNull ItemStack stack, @NotNull Level level, @NotNull LivingEntity entity) {
        if (entity instanceof Player player && MekanismUtils.isPlayingMode(player)) {
            int needed = Math.min(20 - player.getFoodData().getFoodLevel(), FluidType.BUCKET_VOLUME / MekanismConfig.general.nutritionalPasteMBPerFood.get());
            if (needed > 0) {
                if (entity instanceof ServerPlayer serverPlayer) {
                    CriteriaTriggers.CONSUME_ITEM.trigger(serverPlayer, stack);
                    serverPlayer.awardStat(Stats.ITEM_USED.get(this));
                }
                if (!level.isClientSide) {
                    player.getFoodData().eat(needed, MekanismConfig.general.nutritionalPasteSaturation.get());
                }
                stack.shrink(1);
                return stack.isEmpty() ? new ItemStack(Items.BUCKET) : stack;
            }
        }
        return super.finishUsingItem(stack, level, entity);
    }

    @Override
    public void attachCapabilities(RegisterCapabilitiesEvent event) {
        event.registerItem(Capabilities.FLUID.item(), (stack, ctx) -> new FluidBucketWrapper(stack), this);
    }
}
