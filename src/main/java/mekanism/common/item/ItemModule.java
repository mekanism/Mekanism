package mekanism.common.item;

import java.util.Set;
import java.util.function.Consumer;
import mekanism.api.MekanismRegistries;
import mekanism.api.gear.IModuleHelper;
import mekanism.api.gear.ModuleData;
import mekanism.api.text.EnumColor;
import mekanism.api.text.TextComponentUtil;
import mekanism.client.key.MekKeyHandler;
import mekanism.client.key.MekanismKeyHandler;
import mekanism.common.MekanismLang;
import mekanism.common.registration.impl.CreativeTabDeferredRegister.ICustomCreativeTabContents;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab.ItemDisplayParameters;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

public class ItemModule extends Item implements ICustomCreativeTabContents {

    public ItemModule(Item.Properties properties) {
        super(properties);
    }

    @Override
    @Deprecated
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> tooltipAdder, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltipDisplay, tooltipAdder, flag);
        Holder<ModuleData<?>> moduleData = stack.get(IModuleHelper.INSTANCE.dataComponent());
        if (moduleData != null) {
            ModuleData<?> data = moduleData.value();
            //TODO - 26.2: Switch this to https://github.com/neoforged/NeoForge/pull/3132
            tooltipAdder.accept(MekanismLang.TOOLTIP_MODULE_TYPE.translateColored(EnumColor.PURPLE, EnumColor.DARK_AQUA, moduleData));
            tooltipAdder.accept(MekanismLang.MODULE_STACKABLE.translateColored(EnumColor.GRAY, EnumColor.AQUA, data.getMaxStackSize()));
            tooltipAdder.accept(TextComponentUtil.translate(data.getDescriptionTranslationKey()));

            if (MekKeyHandler.isKeyPressed(MekanismKeyHandler.detailsKey)) {
                tooltipAdder.accept(MekanismLang.MODULE_SUPPORTED.translateColored(EnumColor.BRIGHT_GREEN));
                for (Item item : IModuleHelper.INSTANCE.getSupportedItems(moduleData)) {
                    tooltipAdder.accept(MekanismLang.GENERIC_LIST.translate(item.getName(item.getDefaultInstance())));
                }
                Set<ModuleData<?>> conflicting = IModuleHelper.INSTANCE.getConflicting(moduleData);
                if (!conflicting.isEmpty()) {
                    tooltipAdder.accept(MekanismLang.MODULE_CONFLICTING.translateColored(EnumColor.RED));
                    for (ModuleData<?> module : conflicting) {
                        tooltipAdder.accept(MekanismLang.GENERIC_LIST.translate(module));
                    }
                }
            } else {
                tooltipAdder.accept(MekanismLang.HOLD_FOR_SUPPORTED_ITEMS.translateColored(EnumColor.GRAY, EnumColor.INDIGO, MekanismKeyHandler.detailsKey.getTranslatedKeyMessage()));
            }
        }
    }

    @Override
    public void addItems(ItemDisplayParameters displayParameters, Holder<Item> item, Consumer<ItemStack> addToTab) {
        displayParameters.holders().lookupOrThrow(MekanismRegistries.Keys.MODULES)
              .listElements()
              .map(IModuleHelper.INSTANCE::asStack)
              .forEach(addToTab);
    }

    @Override
    public boolean addDefault() {
        return false;
    }
}
