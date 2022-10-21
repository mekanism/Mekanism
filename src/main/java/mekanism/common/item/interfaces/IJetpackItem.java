package mekanism.common.item.interfaces;

import java.util.function.BooleanSupplier;
import java.util.function.Predicate;
import mekanism.api.IIncrementalEnum;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.math.MathUtils;
import mekanism.api.text.EnumColor;
import mekanism.api.text.IHasTextComponent;
import mekanism.api.text.ILangEntry;
import mekanism.common.CommonPlayerTickHandler;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.integration.curios.CuriosIntegration;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public interface IJetpackItem {

    boolean canUseJetpack(ItemStack stack);

    JetpackMode getJetpackMode(ItemStack stack);

    void useJetpackFuel(ItemStack stack);

    @NothingNullByDefault
    enum JetpackMode implements IIncrementalEnum<JetpackMode>, IHasTextComponent {
        NORMAL(MekanismLang.JETPACK_NORMAL, EnumColor.DARK_GREEN, MekanismUtils.getResource(ResourceType.GUI_HUD, "jetpack_normal.png")),
        HOVER(MekanismLang.JETPACK_HOVER, EnumColor.DARK_AQUA, MekanismUtils.getResource(ResourceType.GUI_HUD, "jetpack_hover.png")),
        DISABLED(MekanismLang.JETPACK_DISABLED, EnumColor.DARK_RED, MekanismUtils.getResource(ResourceType.GUI_HUD, "jetpack_off.png"));

        private static final JetpackMode[] MODES = values();
        private final ILangEntry langEntry;
        private final EnumColor color;
        private final ResourceLocation hudIcon;

        JetpackMode(ILangEntry langEntry, EnumColor color, ResourceLocation hudIcon) {
            this.langEntry = langEntry;
            this.color = color;
            this.hudIcon = hudIcon;
        }

        @Override
        public Component getTextComponent() {
            return langEntry.translateColored(color);
        }

        @Override
        public JetpackMode byIndex(int index) {
            return byIndexStatic(index);
        }

        public ResourceLocation getHUDIcon() {
            return hudIcon;
        }

        public static JetpackMode byIndexStatic(int index) {
            return MathUtils.getByIndexMod(MODES, index);
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
        return getJetpack(entity, stack -> stack.getItem() instanceof IJetpackItem jetpackItem && jetpackItem.canUseJetpack(stack));
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
        } else if (Mekanism.hooks.CuriosLoaded) {
            return CuriosIntegration.findFirstCurio(entity, matcher);
        }
        return ItemStack.EMPTY;
    }

    /**
     * @return If fall distance should get reset or not
     */
    static boolean handleJetpackMotion(Player player, JetpackMode mode, BooleanSupplier ascendingSupplier) {
        Vec3 motion = player.getDeltaMovement();
        if (mode == JetpackMode.NORMAL) {
            if (player.isFallFlying()) {
                Vec3 forward = player.getLookAngle();
                Vec3 delta = forward.multiply(forward.scale(0.15))
                      .add(forward.scale(1.5).subtract(motion).scale(0.5));
                player.setDeltaMovement(motion.add(delta));
                return false;
            } else {
                player.setDeltaMovement(motion.x(), Math.min(motion.y() + 0.15D, 0.5D), motion.z());
            }
        } else if (mode == JetpackMode.HOVER) {
            boolean ascending = ascendingSupplier.getAsBoolean();
            boolean descending = player.isDescending();
            if (ascending == descending) {
                if (motion.y() > 0) {
                    player.setDeltaMovement(motion.x(), Math.max(motion.y() - 0.15D, 0), motion.z());
                } else if (motion.y() < 0) {
                    if (!CommonPlayerTickHandler.isOnGroundOrSleeping(player)) {
                        player.setDeltaMovement(motion.x(), Math.min(motion.y() + 0.15D, 0), motion.z());
                    }
                }
            } else if (ascending) {
                player.setDeltaMovement(motion.x(), Math.min(motion.y() + 0.15D, 0.2D), motion.z());
            } else if (!CommonPlayerTickHandler.isOnGroundOrSleeping(player)) {
                player.setDeltaMovement(motion.x(), Math.max(motion.y() - 0.15D, -0.2D), motion.z());
            }
        }
        return true;
    }

    static JetpackMode getPlayerJetpackMode(Player player, JetpackMode mode, BooleanSupplier ascendingSupplier) {
        if (!player.isSpectator()) {
            if (mode != JetpackMode.DISABLED) {
                boolean ascending = ascendingSupplier.getAsBoolean();
                if (mode == JetpackMode.HOVER) {
                    if (ascending && !player.isDescending() || !CommonPlayerTickHandler.isOnGroundOrSleeping(player)) {
                        return mode;
                    }
                } else if (mode == JetpackMode.NORMAL && ascending) {
                    return mode;
                }
            }
        }
        return JetpackMode.DISABLED;
    }
}
