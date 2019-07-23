package mekanism.tools.item;

import java.util.List;
import javax.annotation.Nonnull;
import mekanism.common.Mekanism;
import mekanism.common.util.LangUtils;
import mekanism.common.util.StackUtils;
import mekanism.tools.common.ToolUtils;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemMekanismSword extends ItemSword {

    private ToolMaterial toolMaterial;

    public ItemMekanismSword(ToolMaterial tool) {
        super(tool);
        setCreativeTab(Mekanism.tabMekanism);
        //TODO: Access transformer to make it so we don't have to duplicate storing this? As ItemSword keeps track of it
        toolMaterial = tool;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack itemstack, World world, List<String> list, ITooltipFlag flag) {
        list.add(LangUtils.localize("tooltip.hp") + ": " + (itemstack.getMaxDamage() - itemstack.getItemDamage()));
    }

    @Override
    public boolean getIsRepairable(ItemStack toRepair, @Nonnull ItemStack repair) {
        return StackUtils.equalsWildcard(ToolUtils.getRepairStack(toolMaterial), repair) || super.getIsRepairable(toRepair, repair);
    }
}