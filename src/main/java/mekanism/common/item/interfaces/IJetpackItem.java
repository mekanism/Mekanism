package mekanism.common.item.interfaces;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.Locale;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import mekanism.api.IIncrementalEnum;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.gear.config.IHasModeIcon;
import mekanism.api.text.EnumColor;
import mekanism.api.text.IHasTextComponent.IHasEnumNameTextComponent;
import mekanism.api.text.ILangEntry;
import mekanism.common.CommonPlayerTickHandler;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.integration.curios.CuriosIntegration;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public interface IJetpackItem {

    boolean canUseJetpack(ItemStack stack);

    JetpackMode getJetpackMode(ItemStack stack);

    double getJetpackThrust(ItemStack stack);

    void useJetpackFuel(ItemStack stack);

    @NothingNullByDefault
    enum JetpackMode implements IIncrementalEnum<JetpackMode>, IHasModeIcon, StringRepresentable, IHasEnumNameTextComponent {
        NORMAL(MekanismLang.JETPACK_NORMAL, EnumColor.DARK_GREEN, "jetpack_normal.png"),
        HOVER(MekanismLang.JETPACK_HOVER, EnumColor.DARK_AQUA, "jetpack_hover.png"),
        VECTOR(MekanismLang.JETPACK_VECTOR, EnumColor.ORANGE, "jetpack_vector.png"),
        DISABLED(MekanismLang.JETPACK_DISABLED, EnumColor.DARK_RED, "jetpack_off.png");

        public static final Codec<JetpackMode> CODEC = StringRepresentable.fromEnum(JetpackMode::values);
        public static final IntFunction<JetpackMode> BY_ID = ByIdMap.continuous(JetpackMode::ordinal, values(), ByIdMap.OutOfBoundsStrategy.WRAP);
        public static final StreamCodec<ByteBuf, JetpackMode> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, JetpackMode::ordinal);

        private final String serializedName;
        private final ILangEntry langEntry;
        private final EnumColor color;
        private final ResourceLocation hudIcon;
        private final ResourceLocation modeIcon;

        JetpackMode(ILangEntry langEntry, EnumColor color, String icon) {
            this.serializedName = name().toLowerCase(Locale.ROOT);
            this.langEntry = langEntry;
            this.color = color;
            this.hudIcon = MekanismUtils.getResource(ResourceType.GUI_HUD, icon);
            this.modeIcon = MekanismUtils.getResource(ResourceType.GUI_MODE, icon);
        }

        @Override
        public Component getTextComponent() {
            return langEntry.translateColored(color);
        }

        @Override
        public JetpackMode byIndex(int index) {
            return BY_ID.apply(index);
        }

        public ResourceLocation getHUDIcon() {
            return hudIcon;
        }

        @Override
        public ResourceLocation getModeIcon() {
            return modeIcon;
        }

        @Override
        public String getSerializedName() {
            return serializedName;
        }
    }

    /**
     * Gets the first found active jetpack from an entity, if one is worn.
     * <br>
     * If Curios is loaded, the curio slots will be checked as well.
     *
     * @param entity the entity on which to look for the jetpack
     *
     * @return the jetpack stack if present, otherwise an empty stack
     */
    @NotNull
    static ItemStack getActiveJetpack(LivingEntity entity) {
        if (entity.isPassenger()) {
            return ItemStack.EMPTY;
        }
        ItemStack jetpack = getJetpack(entity, stack -> stack.getItem() instanceof IJetpackItem jetpackItem && jetpackItem.canUseJetpack(stack));
        if (entity instanceof Player player && player.getCooldowns().isOnCooldown(jetpack.getItem())) {
            return ItemStack.EMPTY;
        }
        return jetpack;
    }

    /**
     * Gets the first found jetpack from an entity, if one is worn. Purpose of this is to get the correct jetpack mode to use.
     * <br>
     * If Curios is loaded, the curio slots will be checked as well.
     *
     * @param entity the entity on which to look for the jetpack
     *
     * @return the jetpack stack if present, otherwise an empty stack
     */
    @NotNull
    static ItemStack getPrimaryJetpack(LivingEntity entity) {
        return getJetpack(entity, stack -> stack.getItem() instanceof IJetpackItem);
    }

    private static ItemStack getJetpack(LivingEntity entity, Predicate<ItemStack> matcher) {
        ItemStack chest = entity.getItemBySlot(EquipmentSlot.CHEST);
        if (matcher.test(chest)) {
            return chest;
        } else if (Mekanism.hooks.curios.isLoaded()) {
            return CuriosIntegration.findFirstCurio(entity, matcher);
        }
        return ItemStack.EMPTY;
    }

    /**
     * @return If fall distance should get reset or not
     */
    static <PLAYER extends Player> boolean handleJetpackMotion(PLAYER player, JetpackMode mode, double thrust, Predicate<PLAYER> ascendingCheck) {
        Vec3 motion = player.getDeltaMovement();
        if (mode == JetpackMode.VECTOR && player.isShiftKeyDown()) {
            //TODO: Do we want to expand holding shift to some sort of secondary behavior
            mode = JetpackMode.NORMAL;
        }
        if ((mode == JetpackMode.NORMAL || mode == JetpackMode.VECTOR) && player.isFallFlying()) {
            Vec3 forward = player.getLookAngle();
            Vec3 drag = forward.scale(1.5).subtract(motion).scale(0.5);
            Vec3 delta = forward.scale(thrust).add(drag);
            player.addDeltaMovement(delta);
            return false;
        } else if (mode == JetpackMode.NORMAL) {
            Vec3 delta = new Vec3(0.08 * motion.x, thrust * getVerticalCoefficient(motion.y()), 0.08 * motion.z);
            player.addDeltaMovement(delta);
        } else if (mode == JetpackMode.VECTOR) {
            Vec3 thrustVec = player.getUpVector(1F).scale(thrust);
            Vec3 delta = new Vec3(thrustVec.x, thrustVec.y * getVerticalCoefficient(motion.y()), thrustVec.z);
            player.addDeltaMovement(delta);
        } else if (mode == JetpackMode.HOVER) {
            boolean ascending = ascendingCheck.test(player);
            boolean descending = player.isDescending();
            if (ascending == descending) {
                if (motion.y() > 0) {
                    player.setDeltaMovement(motion.x(), Math.max(motion.y() - thrust, 0), motion.z());
                } else if (motion.y() < 0) {
                    if (!CommonPlayerTickHandler.isOnGroundOrSleeping(player)) {
                        player.setDeltaMovement(motion.x(), Math.min(motion.y() + thrust, 0), motion.z());
                    }
                }
            } else if (ascending) {
                player.setDeltaMovement(motion.x(), Math.min(motion.y() + thrust, 2 * thrust), motion.z());
            } else if (!CommonPlayerTickHandler.isOnGroundOrSleeping(player)) {
                player.setDeltaMovement(motion.x(), Math.max(motion.y() - thrust, -2 * thrust), motion.z());
            }
        }
        return true;
    }

    private static double getVerticalCoefficient(double currentYVelocity) {
        return Math.min(1, Math.exp(-currentYVelocity));
    }

    static <PLAYER extends Player> JetpackMode getPlayerJetpackMode(PLAYER player, JetpackMode mode, Predicate<PLAYER> ascendingCheck) {
        if (!player.isSpectator()) {
            if (mode != JetpackMode.DISABLED) {
                boolean ascending = ascendingCheck.test(player);
                if (mode == JetpackMode.HOVER) {
                    if (ascending && !player.isDescending() || !CommonPlayerTickHandler.isOnGroundOrSleeping(player)) {
                        return mode;
                    }
                } else if (ascending) {
                    return mode;
                }
            }
        }
        return JetpackMode.DISABLED;
    }
}
