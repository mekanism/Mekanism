package mekanism.common.item.interfaces;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import mekanism.api.IIncrementalEnum;
import mekanism.api.gear.config.IHasModeIcon;
import mekanism.api.text.EnumColor;
import mekanism.api.text.IHasTextComponent.IHasEnumNameTextComponent;
import mekanism.api.text.ILangEntry;
import mekanism.common.CommonPlayerTickHandler;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.integration.curios.ICuriosHelper;
import mekanism.common.util.ItemAccessUtils;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.TypedInstance;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.Nullable;

public interface IJetpackItem {

    boolean canUseJetpack(ItemAccess itemAccess);

    <ITEM extends TypedInstance<Item> & DataComponentGetter> JetpackMode getJetpackMode(ITEM instance);

    ///@return thrust that fuel was consumed for
    <ITEM extends TypedInstance<Item> & DataComponentGetter> double useJetpackFuel(RegistryAccess registryAccess, ItemAccess itemAccess, ITEM primaryInstance, TransactionContext transaction);

    enum JetpackMode implements IIncrementalEnum<JetpackMode>, IHasModeIcon, StringRepresentable, IHasEnumNameTextComponent, TooltipProvider {
        NORMAL(MekanismLang.JETPACK_NORMAL, EnumColor.DARK_GREEN, "jetpack_normal"),
        HOVER(MekanismLang.JETPACK_HOVER, EnumColor.DARK_AQUA, "jetpack_hover"),
        VECTOR(MekanismLang.JETPACK_VECTOR, EnumColor.ORANGE, "jetpack_vector"),
        DISABLED(MekanismLang.JETPACK_DISABLED, EnumColor.DARK_RED, "jetpack_off");

        public static final Codec<JetpackMode> CODEC = StringRepresentable.fromEnum(JetpackMode::values);
        public static final IntFunction<JetpackMode> BY_ID = ByIdMap.continuous(JetpackMode::ordinal, values(), ByIdMap.OutOfBoundsStrategy.WRAP);
        public static final StreamCodec<ByteBuf, JetpackMode> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, JetpackMode::ordinal);

        private final String serializedName;
        private final ILangEntry langEntry;
        private final EnumColor color;
        private final Identifier hudIcon;
        private final Identifier modeIcon;

        JetpackMode(ILangEntry langEntry, EnumColor color, String icon) {
            this.serializedName = name().toLowerCase(Locale.ROOT);
            this.langEntry = langEntry;
            this.color = color;
            this.hudIcon = Mekanism.rl("hud/" + icon);
            this.modeIcon = Mekanism.rl("mode/" + icon);
        }

        @Override
        public Component getTextComponent() {
            return langEntry.translateColored(color);
        }

        @Override
        public JetpackMode byIndex(int index) {
            return BY_ID.apply(index);
        }

        public Identifier getHUDIcon() {
            return hudIcon;
        }

        @Override
        public Identifier getModeIcon() {
            return modeIcon;
        }

        @Override
        public String getSerializedName() {
            return serializedName;
        }

        @Override
        public void addToTooltip(TooltipContext context, Consumer<Component> builder, TooltipFlag flag, DataComponentGetter components) {
            builder.accept(MekanismLang.MODE.translateColored(EnumColor.GRAY, this));
        }
    }

    /// Gets the first found active jetpack from an entity, if one is worn.
    ///
    /// If Curios is loaded, the curio slots will be checked as well.
    ///
    /// @param entity the entity on which to look for the jetpack
    ///
    /// @return the jetpack stack if present, otherwise an empty stack
    @Nullable
    static ItemAccess getActiveJetpack(LivingEntity entity) {
        if (entity.isPassenger()) {
            return null;
        }
        ItemAccess jetpack = getJetpack(entity, itemAccess -> itemAccess.getResource().getItem() instanceof IJetpackItem jetpackItem && jetpackItem.canUseJetpack(itemAccess));
        if (jetpack != null && entity instanceof Player player && player.getCooldowns().isOnCooldown(ItemAccessUtils.asStack(jetpack))) {
            return null;
        }
        return jetpack;
    }

    /// Gets the first found jetpack from an entity, if one is worn. Purpose of this is to get the correct jetpack mode to use.
    ///
    /// If Curios is loaded, the curio slots will be checked as well.
    ///
    /// @param entity the entity on which to look for the jetpack
    ///
    /// @return the jetpack stack if present, otherwise an empty stack
    static ItemResource getPrimaryJetpack(LivingEntity entity) {
        ItemAccess jetpack = getJetpack(entity, itemAccess -> itemAccess.getResource().getItem() instanceof IJetpackItem);
        return jetpack == null ? ItemResource.EMPTY : jetpack.getResource();
    }

    @Nullable
    private static ItemAccess getJetpack(LivingEntity entity, Predicate<ItemAccess> matcher) {
        ItemAccess chest = ItemAccessUtils.forEntitySlot(entity, EquipmentSlot.CHEST);
        if (matcher.test(chest)) {
            return chest;
        } else if (Mekanism.hooks.curios.isLoaded() && ICuriosHelper.INSTANCE != null) {
            return ICuriosHelper.INSTANCE.findFirstCurio(entity, matcher);
        }
        return null;
    }

    /// @return If fall distance should get reset or not
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
