package mekanism.common.item.interfaces;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import mekanism.api.IIncrementalEnum;
import mekanism.api.text.EnumColor;
import mekanism.api.text.IHasTextComponent.IHasEnumNameTextComponent;
import mekanism.api.text.ILangEntry;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.integration.curios.ICuriosHelper;
import mekanism.common.util.ItemAccessUtils;
import net.minecraft.core.TypedInstance;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jspecify.annotations.Nullable;

public interface IFreeRunnerItem {

    <ITEM extends TypedInstance<Item> & DataComponentGetter> FreeRunnerMode getFreeRunnerMode(ITEM instance);

    enum FreeRunnerMode implements IIncrementalEnum<FreeRunnerMode>, IHasEnumNameTextComponent, StringRepresentable, TooltipProvider {
        NORMAL(MekanismLang.FREE_RUNNER_NORMAL, EnumColor.DARK_GREEN, true, true),
        SAFETY(MekanismLang.FREE_RUNNER_SAFETY, EnumColor.ORANGE, true, false),
        DISABLED(MekanismLang.FREE_RUNNER_DISABLED, EnumColor.DARK_RED, false, false);

        public static final Codec<FreeRunnerMode> CODEC = StringRepresentable.fromEnum(FreeRunnerMode::values);
        public static final IntFunction<FreeRunnerMode> BY_ID = ByIdMap.continuous(FreeRunnerMode::ordinal, values(), ByIdMap.OutOfBoundsStrategy.WRAP);
        public static final StreamCodec<ByteBuf, FreeRunnerMode> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, FreeRunnerMode::ordinal);

        private final String serializedName;
        private final boolean preventsFallDamage;
        private final boolean providesStepBoost;
        private final ILangEntry langEntry;
        private final EnumColor color;

        FreeRunnerMode(ILangEntry langEntry, EnumColor color, boolean preventsFallDamage, boolean providesStepBoost) {
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
        public FreeRunnerMode byIndex(int index) {
            return BY_ID.apply(index);
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

    /// Gets the first found active pair (pair with energy) of free runners for an entity, if any are worn.
    ///
    /// If Curios is loaded, the curio slots will be checked as well.
    ///
    /// @param entity the entity on which to look for the free runners
    ///
    /// @return the free runners stack if present, otherwise an empty stack
    @Nullable
    static ItemAccess getActiveFreeRunners(LivingEntity entity) {
        return getFreeRunners(entity, itemAccess -> {
            if (itemAccess.getResource().getItem() instanceof IFreeRunnerItem) {
                EnergyHandler energyHandler = Capabilities.ENERGY.getCapability(itemAccess);
                return energyHandler != null && energyHandler.getAmountAsLong() > 0;
            }
            return false;
        });
    }

    /// Gets the first found free runners from an entity, if one is worn. Purpose of this is to get the correct free runner mode to use.
    ///
    /// If Curios is loaded, the curio slots will be checked as well.
    ///
    /// @param entity the entity on which to look for the free runners
    ///
    /// @return the free runners stack if present, otherwise an empty stack
    static ItemResource getPrimaryFreeRunners(LivingEntity entity) {
        ItemAccess freeRunners = getFreeRunners(entity, itemAccess -> itemAccess.getResource().getItem() instanceof IFreeRunnerItem);
        return freeRunners == null ? ItemResource.EMPTY : freeRunners.getResource();
    }

    @Nullable
    private static ItemAccess getFreeRunners(LivingEntity entity, Predicate<ItemAccess> matcher) {
        ItemAccess feet = ItemAccessUtils.forEntitySlot(entity, EquipmentSlot.FEET);
        if (matcher.test(feet)) {
            return feet;
        } else if (Mekanism.hooks.curios.isLoaded() && ICuriosHelper.INSTANCE != null) {
            return ICuriosHelper.INSTANCE.findFirstCurio(entity, matcher);
        }
        return null;
    }
}