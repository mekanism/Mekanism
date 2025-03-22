package mekanism.common.item.interfaces;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import mekanism.api.IIncrementalEnum;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.text.EnumColor;
import mekanism.api.text.IHasTextComponent;
import mekanism.api.text.ILangEntry;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.integration.curios.CuriosIntegration;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.function.IntFunction;
import java.util.function.Predicate;

public interface IFreeRunnersItem {
    FreeRunnersMode getFreeRunnersMode(ItemStack stack);

    @NothingNullByDefault
    enum FreeRunnersMode implements IIncrementalEnum<FreeRunnersMode>, IHasTextComponent.IHasEnumNameTextComponent, StringRepresentable {
        NORMAL(MekanismLang.FREE_RUNNER_NORMAL, EnumColor.DARK_GREEN, true, true),
        SAFETY(MekanismLang.FREE_RUNNER_SAFETY, EnumColor.ORANGE, true, false),
        DISABLED(MekanismLang.FREE_RUNNER_DISABLED, EnumColor.DARK_RED, false, false);

        public static final Codec<FreeRunnersMode> CODEC = StringRepresentable.fromEnum(FreeRunnersMode::values);
        public static final IntFunction<FreeRunnersMode> BY_ID = ByIdMap.continuous(FreeRunnersMode::ordinal, values(), ByIdMap.OutOfBoundsStrategy.WRAP);
        public static final StreamCodec<ByteBuf, FreeRunnersMode> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, FreeRunnersMode::ordinal);

        private final String serializedName;
        private final boolean preventsFallDamage;
        private final boolean providesStepBoost;
        private final ILangEntry langEntry;
        private final EnumColor color;

        FreeRunnersMode(ILangEntry langEntry, EnumColor color, boolean preventsFallDamage, boolean providesStepBoost) {
            this.serializedName = name().toLowerCase(Locale.ROOT);
            this.preventsFallDamage = preventsFallDamage;
            this.providesStepBoost = providesStepBoost;
            this.langEntry = langEntry;
            this.color = color;
        }

        public boolean preventsFallDamage() {
            return preventsFallDamage;
        }

        public boolean providesStepBoost() {
            return providesStepBoost;
        }

        @Override
        public Component getTextComponent() {
            return langEntry.translateColored(color);
        }

        @Override
        public FreeRunnersMode byIndex(int index) {
            return BY_ID.apply(index);
        }

        @Override
        public String getSerializedName() {
            return serializedName;
        }
    }

    /**
     * Gets the first found free runners from an entity, if one is worn. Purpose of this is to get the correct free
     * runners mode to use.
     * <br>
     * If Curios is loaded, the curio slots will be checked as well.
     *
     * @param entity the entity on which to look for the free runners
     *
     * @return the free runners stack if present, otherwise an empty stack
     */
    @NotNull
    static ItemStack getFreeRunners(LivingEntity entity) {
        Predicate<ItemStack> matcher = stack -> stack.getItem() instanceof IFreeRunnersItem;

        ItemStack feet = entity.getItemBySlot(EquipmentSlot.FEET);
        if (matcher.test(feet)) {
            return feet;
        } else if (Mekanism.hooks.curios.isLoaded()) {
            return CuriosIntegration.findFirstCurio(entity, matcher);
        }

        return ItemStack.EMPTY;
    }
}
