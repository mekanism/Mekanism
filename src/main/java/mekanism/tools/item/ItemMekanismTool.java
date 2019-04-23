package mekanism.tools.item;

import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;
import mekanism.common.Mekanism;
import mekanism.common.MekanismItems;
import mekanism.common.util.LangUtils;
import mekanism.common.util.StackUtils;
import mekanism.tools.common.MekanismTools;
import net.minecraft.block.Block;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTool;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemMekanismTool extends ItemTool {

    public ItemMekanismTool(float attack, float speed, ToolMaterial toolMaterial, Set<Block> effectiveBlocksIn) {
        super(attack, speed, toolMaterial, effectiveBlocksIn);
        setCreativeTab(Mekanism.tabMekanism);
    }

    public static ItemStack getRepairStack(ToolMaterial material) {
        if (material == MekanismTools.toolOBSIDIAN || material == MekanismTools.toolOBSIDIAN2) {
            return new ItemStack(MekanismItems.Ingot, 1, 0);
        } else if (material == MekanismTools.toolLAZULI || material == MekanismTools.toolLAZULI2) {
            return new ItemStack(Items.DYE, 1, 4);
        } else if (material == MekanismTools.toolOSMIUM || material == MekanismTools.toolOSMIUM2) {
            return new ItemStack(MekanismItems.Ingot, 1, 1);
        } else if (material == MekanismTools.toolBRONZE || material == MekanismTools.toolBRONZE2) {
            return new ItemStack(MekanismItems.Ingot, 1, 2);
        } else if (material == MekanismTools.toolGLOWSTONE || material == MekanismTools.toolGLOWSTONE2) {
            return new ItemStack(MekanismItems.Ingot, 1, 3);
        } else if (material == MekanismTools.toolSTEEL || material == MekanismTools.toolSTEEL2) {
            return new ItemStack(MekanismItems.Ingot, 1, 4);
        }

        return material.getRepairItemStack();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack itemstack, World world, List<String> list, ITooltipFlag flag) {
        list.add(LangUtils.localize("tooltip.hp") + ": " + (itemstack.getMaxDamage() - itemstack.getItemDamage()));
    }

    @Override
    public boolean getIsRepairable(ItemStack stack1, @Nonnull ItemStack stack2) {
        return StackUtils.equalsWildcard(getRepairStack(), stack2) || super.getIsRepairable(stack1, stack2);
    }

    private ItemStack getRepairStack() {
        return getRepairStack(toolMaterial);
    }
}
