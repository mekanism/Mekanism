package mekanism.tools.item;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;
import mekanism.common.Mekanism;
import mekanism.common.util.LangUtils;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemSpade;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTool;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemMekanismPaxel extends ItemTool {

    private static final Set<Block> EFFECTIVE_ON = new HashSet<>();

    static {
        EFFECTIVE_ON.addAll(ItemPickaxe.EFFECTIVE_ON);
        EFFECTIVE_ON.addAll(ItemSpade.EFFECTIVE_ON);
        EFFECTIVE_ON.addAll(ItemAxe.EFFECTIVE_ON);
    }

    private final ItemPickaxe pickaxe;
    private final ItemSpade shovel;
    private final ItemAxe axe;

    public ItemMekanismPaxel(ToolMaterial material, ItemPickaxe pickaxe, ItemSpade shovel, ItemAxe axe) {
        super(4, -2.4F, material, EFFECTIVE_ON);
        setCreativeTab(Mekanism.tabMekanism);
        setHarvestLevel("pickaxe", material.getHarvestLevel());
        setHarvestLevel("shovel", material.getHarvestLevel());
        setHarvestLevel("axe", material.getHarvestLevel());
        //Store the normal tool variants of this material for query purposes
        this.pickaxe = pickaxe;
        this.shovel = shovel;
        this.axe = axe;
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
    public boolean canHarvestBlock(@Nonnull IBlockState state, @Nonnull ItemStack stack) {
        return pickaxe.canHarvestBlock(state, stack) || shovel.canHarvestBlock(state, stack) || axe.canHarvestBlock(state, stack);
    }
}