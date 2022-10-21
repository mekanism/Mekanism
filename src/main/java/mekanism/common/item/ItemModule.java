package mekanism.common.item;

import java.util.List;
import java.util.Set;
import mekanism.api.MekanismAPI;
import mekanism.api.gear.IModuleHelper;
import mekanism.api.gear.ModuleData;
import mekanism.api.providers.IModuleDataProvider;
import mekanism.api.text.EnumColor;
import mekanism.api.text.TextComponentUtil;
import mekanism.client.key.MekKeyHandler;
import mekanism.client.key.MekanismKeyHandler;
import mekanism.common.MekanismLang;
import mekanism.common.content.gear.IModuleItem;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class ItemModule extends Item implements IModuleItem {

    private final IModuleDataProvider<?> moduleData;

    public ItemModule(IModuleDataProvider<?> moduleData, Properties properties) {
        super(properties);
        this.moduleData = moduleData;
    }

    @Override
    public int getMaxStackSize(ItemStack stack) {
        return getModuleData().getMaxStackSize();
    }

    @Override
    public ModuleData<?> getModuleData() {
        return moduleData.getModuleData();
    }

    @NotNull
    @Override
    public Rarity getRarity(@NotNull ItemStack stack) {
        return getModuleData().getRarity();
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, Level world, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        if (MekKeyHandler.isKeyPressed(MekanismKeyHandler.detailsKey)) {
            tooltip.add(MekanismLang.MODULE_SUPPORTED.translateColored(EnumColor.BRIGHT_GREEN));
            IModuleHelper moduleHelper = MekanismAPI.getModuleHelper();
            for (Item item : moduleHelper.getSupported(getModuleData())) {
                tooltip.add(MekanismLang.GENERIC_LIST.translate(item.getName(new ItemStack(item))));
            }
            Set<ModuleData<?>> conflicting = moduleHelper.getConflicting(getModuleData());
            if (!conflicting.isEmpty()) {
                tooltip.add(MekanismLang.MODULE_CONFLICTING.translateColored(EnumColor.RED));
                for (ModuleData<?> module : conflicting) {
                    tooltip.add(MekanismLang.GENERIC_LIST.translate(module));
                }
            }
        } else {
            ModuleData<?> moduleData = getModuleData();
            tooltip.add(TextComponentUtil.translate(moduleData.getDescriptionTranslationKey()));
            tooltip.add(MekanismLang.MODULE_STACKABLE.translateColored(EnumColor.GRAY, EnumColor.AQUA, moduleData.getMaxStackSize()));
            tooltip.add(MekanismLang.HOLD_FOR_SUPPORTED_ITEMS.translateColored(EnumColor.GRAY, EnumColor.INDIGO, MekanismKeyHandler.detailsKey.getTranslatedKeyMessage()));
        }
    }

    @NotNull
    @Override
    public String getDescriptionId() {
        return getModuleData().getTranslationKey();
    }
}
