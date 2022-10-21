package mekanism.common.item;

import java.util.function.Supplier;
import mekanism.common.config.MekanismConfig;
import mekanism.common.util.MekanismUtils;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.nbt.CompoundTag;
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
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.capability.wrappers.FluidBucketWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ItemNutritionalPasteBucket extends BucketItem {

    public ItemNutritionalPasteBucket(Supplier<? extends Fluid> supplier, Properties builder) {
        super(supplier, builder);
    }

    @Override
    public int getUseDuration(@NotNull ItemStack stack) {
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
                    player.getFoodData().eat(needed, needed * MekanismConfig.general.nutritionalPasteSaturation.get());
                }
                stack.shrink(1);
                return stack.isEmpty() ? new ItemStack(Items.BUCKET) : stack;
            }
        }
        return super.finishUsingItem(stack, level, entity);
    }

    @Override
    public ICapabilityProvider initCapabilities(@NotNull ItemStack stack, @Nullable CompoundTag nbt) {
        return new FluidBucketWrapper(stack);
    }
}