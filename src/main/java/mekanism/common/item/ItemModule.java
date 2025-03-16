package mekanism.common.item;

import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
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
import org.jetbrains.annotations.NotNull;

public class ItemModule extends Item implements IModuleItem {

    private final Supplier<Holder<ModuleData<?>>> moduleDataSupplier;

    public ItemModule(Supplier<Holder<ModuleData<?>>> moduleDataSupplier, Properties properties) {
        super(properties);
        this.moduleDataSupplier = moduleDataSupplier;
    }

    @Override
    public Holder<ModuleData<?>> getModuleData() {
        return moduleDataSupplier.get();
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull Item.TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        Holder<ModuleData<?>> moduleData = getModuleData();
        if (MekKeyHandler.isKeyPressed(MekanismKeyHandler.detailsKey)) {
            tooltip.add(MekanismLang.MODULE_SUPPORTED.translateColored(EnumColor.BRIGHT_GREEN));
            for (Item item : IModuleHelper.INSTANCE.getSupportedItems(moduleData)) {
                tooltip.add(MekanismLang.GENERIC_LIST.translate(item.getName(new ItemStack(item))));
            }
            Set<ModuleData<?>> conflicting = IModuleHelper.INSTANCE.getConflicting(moduleData);
            if (!conflicting.isEmpty()) {
                tooltip.add(MekanismLang.MODULE_CONFLICTING.translateColored(EnumColor.RED));
                for (ModuleData<?> module : conflicting) {
                    tooltip.add(MekanismLang.GENERIC_LIST.translate(module));
                }
            }
        } else {
            ModuleData<?> data = moduleData.value();
            tooltip.add(TextComponentUtil.translate(data.getDescriptionTranslationKey()));
            tooltip.add(MekanismLang.MODULE_STACKABLE.translateColored(EnumColor.GRAY, EnumColor.AQUA, data.getMaxStackSize()));
            tooltip.add(MekanismLang.HOLD_FOR_SUPPORTED_ITEMS.translateColored(EnumColor.GRAY, EnumColor.INDIGO, MekanismKeyHandler.detailsKey.getTranslatedKeyMessage()));
        }
    }

    @NotNull
    @Override
    public String getDescriptionId() {
        return getModuleData().value().getTranslationKey();
    }
}
