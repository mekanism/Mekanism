package mekanism.common.item;

import java.util.Set;
import java.util.function.Consumer;
import mekanism.api.gear.IModuleHelper;
import mekanism.api.gear.ModuleData;
import mekanism.api.text.EnumColor;
import mekanism.api.text.TextComponentUtil;
import mekanism.client.key.MekKeyHandler;
import mekanism.client.key.MekanismKeyHandler;
import mekanism.common.MekanismLang;
import mekanism.common.content.gear.IModuleItem;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import org.jetbrains.annotations.NotNull;

public class ItemModule extends Item implements IModuleItem {

    private final Holder<ModuleData<?>> moduleData;

    public ItemModule(Holder<ModuleData<?>> moduleData, Properties properties) {
        super(properties.overrideDescription(moduleData.value().getTranslationKey()));
        this.moduleData = moduleData;
    }

    @Override
    public Holder<ModuleData<?>> getModuleData() {
        return moduleData;
    }

    @Override
    @Deprecated
    public void appendHoverText(@NotNull ItemStack stack, @NotNull Item.TooltipContext context, @NotNull TooltipDisplay tooltipDisplay, @NotNull Consumer<Component> tooltipAdder, @NotNull TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltipDisplay, tooltipAdder, flag);
        if (MekKeyHandler.isKeyPressed(MekanismKeyHandler.detailsKey)) {
            tooltipAdder.accept(MekanismLang.MODULE_SUPPORTED.translateColored(EnumColor.BRIGHT_GREEN));
            for (Item item : IModuleHelper.INSTANCE.getSupportedItems(moduleData)) {
                tooltipAdder.accept(MekanismLang.GENERIC_LIST.translate(item.getName(new ItemStack(item))));
            }
            Set<ModuleData<?>> conflicting = IModuleHelper.INSTANCE.getConflicting(moduleData);
            if (!conflicting.isEmpty()) {
                tooltipAdder.accept(MekanismLang.MODULE_CONFLICTING.translateColored(EnumColor.RED));
                for (ModuleData<?> module : conflicting) {
                    tooltipAdder.accept(MekanismLang.GENERIC_LIST.translate(module));
                }
            }
        } else {
            ModuleData<?> data = moduleData.value();
            tooltipAdder.accept(TextComponentUtil.translate(data.getDescriptionTranslationKey()));
            tooltipAdder.accept(MekanismLang.MODULE_STACKABLE.translateColored(EnumColor.GRAY, EnumColor.AQUA, data.getMaxStackSize()));
            tooltipAdder.accept(MekanismLang.HOLD_FOR_SUPPORTED_ITEMS.translateColored(EnumColor.GRAY, EnumColor.INDIGO, MekanismKeyHandler.detailsKey.getTranslatedKeyMessage()));
        }
    }
}
