package mekanism.tools.item;

import java.util.HashSet;
import java.util.List;
import javax.annotation.Nonnull;
import mekanism.common.Mekanism;
import mekanism.common.util.LangUtils;
import mekanism.common.util.StackUtils;
import mekanism.tools.common.ToolUtils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTool;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemMekanismPaxel extends ItemTool {

    public ItemMekanismPaxel(ToolMaterial material) {
        super(4, -2.4F, material, new HashSet<>());
        setCreativeTab(Mekanism.tabMekanism);
        //setHarvestLevel("pickaxe", );
        //setHarvestLevel("axe", );
        //setHarvestLevel("shovel", );
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

    @Override
    public float getDestroySpeed(@Nonnull ItemStack stack, IBlockState blockState) {
        return blockState.getBlock() != Blocks.BEDROCK ? efficiency : 1.0F;
    }

    @Override
    public boolean canHarvestBlock(@Nonnull IBlockState state, ItemStack stack) {
        return ToolUtils.canShovelHarvest(state.getBlock()) || ToolUtils.canPickaxeHarvest(state, toolMaterial);
    }
}