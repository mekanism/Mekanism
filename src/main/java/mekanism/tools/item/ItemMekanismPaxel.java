package mekanism.tools.item;

import java.util.HashSet;
import java.util.List;
import javax.annotation.Nonnull;
import mekanism.common.util.LangUtils;
import mekanism.tools.common.IHasRepairType;
import mekanism.tools.common.Materials;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTool;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemMekanismPaxel extends ItemTool implements IHasRepairType {

    public ItemMekanismPaxel(Materials material) {
        this(material.getPaxelMaterial());
    }

    public ItemMekanismPaxel(ToolMaterial material) {
        super(4, -2.4F, material, new HashSet<>());
        setHarvestLevel("pickaxe", material.getHarvestLevel());
        setHarvestLevel("shovel", material.getHarvestLevel());
        setHarvestLevel("axe", material.getHarvestLevel());
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack itemstack, World world, List<String> list, ITooltipFlag flag) {
        list.add(LangUtils.localize("tooltip.hp") + ": " + (itemstack.getMaxDamage() - itemstack.getItemDamage()));
    }

    @Override
    public float getDestroySpeed(@Nonnull ItemStack stack, IBlockState state) {
        Material material = state.getMaterial();
        //TODO: 1.14 Double check the various items to see if their getDestroySpeed short paths changed
        boolean pickaxeShortcut = material == Material.IRON || material == Material.ANVIL || material == Material.ROCK;
        boolean axeShortcut = material == Material.WOOD || material == Material.PLANTS || material == Material.VINE;
        return pickaxeShortcut || axeShortcut ? this.efficiency : super.getDestroySpeed(stack, state);
    }

    @Override
    public ItemStack getRepairStack() {
        return toolMaterial.getRepairItemStack();
    }
}