package mekanism.common.item;

import java.util.List;
import javax.annotation.Nonnull;
import mekanism.common.content.gear.IModuleItem;
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

    public ItemModule(Properties properties, ModuleData<?> moduleData) {
        super(properties.maxStackSize(moduleData.getMaxStackSize()));
        moduleData.setStack(this);
        this.moduleData = moduleData;
    }

    @Override
    public ModuleData<?> getModuleData() {
        return moduleData;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(@Nonnull ItemStack stack, World world, @Nonnull List<ITextComponent> tooltip, @Nonnull ITooltipFlag flag) {
        tooltip.add(moduleData.getDescription());
    }

    @Override
    public String getTranslationKey() {
        return moduleData.getTranslationKey();
    }
}
