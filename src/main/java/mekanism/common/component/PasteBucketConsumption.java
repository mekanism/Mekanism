package mekanism.common.component;

import io.netty.buffer.ByteBuf;
import mekanism.common.config.MekanismConfig;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.ConsumableListener;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidType;
import org.jspecify.annotations.NonNull;

public class PasteBucketConsumption implements ConsumableListener {

    public static final PasteBucketConsumption INSTANCE = new PasteBucketConsumption();
    public static final StreamCodec<ByteBuf, PasteBucketConsumption> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    private PasteBucketConsumption() {
    }

    @Override
    public void onConsume(@NonNull Level level, @NonNull LivingEntity user, @NonNull ItemStack stack, @NonNull Consumable consumable) {
        //Based off of FoodProperties implementation of onConsume
        RandomSource random = user.getRandom();
        level.playSound(null, user.getX(), user.getY(), user.getZ(), consumable.sound().value(), SoundSource.NEUTRAL, 1.0F, random.triangle(1.0F, 0.4F));
        if (user instanceof Player player) {
            player.getFoodData().eat(FluidType.BUCKET_VOLUME / MekanismConfig.general.nutritionalPasteMBPerFood.get(), MekanismConfig.general.nutritionalPasteSaturation.get());
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_BURP, SoundSource.PLAYERS, 0.5F, Mth.randomBetween(random, 0.9F, 1.0F));
        }
    }

    //Note: These are required to be implemented due to validation that neo does for components, given we are a unit component though we can just use the default impl
    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}