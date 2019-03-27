package mekanism.tools.item;

import java.util.List;
import mekanism.common.Mekanism;
import mekanism.common.util.LangUtils;
import mekanism.common.util.StackUtils;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemMekanismSword extends ItemSword {

    private ToolMaterial toolMaterial;

    public ItemMekanismSword(ToolMaterial enumtoolmaterial) {
        super(enumtoolmaterial);
        toolMaterial = enumtoolmaterial;
        setCreativeTab(Mekanism.tabMekanism);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack itemstack, World world, List<String> list, ITooltipFlag flag) {
        list.add(LangUtils.localize("tooltip.hp") + ": " + (itemstack.getMaxDamage() - itemstack.getItemDamage()));
    }

    @Override
    public boolean getIsRepairable(ItemStack stack1, ItemStack stack2) {
        return StackUtils.equalsWildcard(ItemMekanismTool.getRepairStack(toolMaterial), stack2) ? true
              : super.getIsRepairable(stack1, stack2);
    }
}
