package mekanism.common.component;

import com.mojang.serialization.Codec;
import java.util.function.Consumer;
import mekanism.api.text.EnumColor;
import mekanism.api.upgrade.Upgrade;
import mekanism.common.MekanismLang;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;

public record UpgradeType(Holder<Upgrade> type) implements TooltipProvider {

    public static final Codec<UpgradeType> CODEC = Upgrade.CODEC.xmap(UpgradeType::new, UpgradeType::type);
    public static final StreamCodec<RegistryFriendlyByteBuf, UpgradeType> STREAM_CODEC = Upgrade.STREAM_CODEC.map(UpgradeType::new, UpgradeType::type);

    @Override
    public void addToTooltip(TooltipContext context, Consumer<Component> consumer, TooltipFlag flag, DataComponentGetter components) {
        Upgrade upgrade = type.value();
        consumer.accept(MekanismLang.TOOLTIP_UPGRADE_TYPE.translateColored(EnumColor.PURPLE, upgrade.color(), upgrade));
        consumer.accept(MekanismLang.TOOLTIP_UPGRADE_MAX_INSTALLED.translateColored(EnumColor.GRAY, EnumColor.AQUA, upgrade.max()));
        consumer.accept(upgrade.description());
    }
}