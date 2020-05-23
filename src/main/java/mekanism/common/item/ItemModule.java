package mekanism.common.item;

import java.util.List;
import javax.annotation.Nonnull;
import mekanism.api.text.EnumColor;
import mekanism.client.MekKeyHandler;
import mekanism.client.MekanismKeyHandler;
import mekanism.common.MekanismLang;
import mekanism.common.content.gear.IModuleItem;
import mekanism.common.content.gear.Modules;
import mekanism.common.content.gear.Modules.ModuleData;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ItemModule extends Item implements IModuleItem {

    private ModuleData<?> moduleData;

    public ItemModule(ModuleData<?> moduleData, Properties properties) {
        super(properties.maxStackSize(moduleData.getMaxStackSize()));
        moduleData.setStack(this);
        this.moduleData = moduleData;
    }

    @Override
    public ModuleData<?> getModuleData() {
        return moduleData;
    }

    @Override
    public ITextComponent getDisplayName(ItemStack stack) {
        return super.getDisplayName(stack).applyTextStyle(EnumColor.GRAY.textFormatting);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(@Nonnull ItemStack stack, World world, @Nonnull List<ITextComponent> tooltip, @Nonnull ITooltipFlag flag) {
        if (MekKeyHandler.getIsKeyPressed(MekanismKeyHandler.detailsKey)) {
            for (Item item : Modules.getSupported(getModuleData())) {
                tooltip.add(item.getDisplayName(new ItemStack(item)));
            }
        } else {
            tooltip.add(moduleData.getDescription());
            tooltip.add(MekanismLang.MODULE_STACKABLE.translateColored(EnumColor.GRAY, EnumColor.AQUA, moduleData.getMaxStackSize()));
            tooltip.add(MekanismLang.HOLD_FOR_SUPPORTED_ITEMS.translateColored(EnumColor.GRAY, EnumColor.INDIGO, MekanismKeyHandler.detailsKey.getLocalizedName()));
        }
    }

    @Override
    public String getTranslationKey() {
        return moduleData.getTranslationKey();
    }
}
