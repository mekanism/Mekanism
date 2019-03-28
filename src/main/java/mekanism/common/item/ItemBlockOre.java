package mekanism.common.item;

import java.util.List;
import mekanism.api.EnumColor;
import mekanism.client.MekKeyHandler;
import mekanism.client.MekanismKeyHandler;
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.block.Block;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Item class for handling multiple ore block IDs. 0: Osmium Ore 1: Copper Ore 2: Tin Ore
 *
 * @author AidanBrady
 */
public class ItemBlockOre extends ItemBlock {

    public Block metaBlock;

    public ItemBlockOre(Block block) {
        super(block);
        metaBlock = block;
        setHasSubtypes(true);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack itemstack, World world, List<String> list, ITooltipFlag flag) {
        if (!MekKeyHandler.getIsKeyPressed(MekanismKeyHandler.sneakKey)) {
            list.add(
                  "Hold " + EnumColor.AQUA + GameSettings.getKeyDisplayString(MekanismKeyHandler.sneakKey.getKeyCode())
                        + EnumColor.GREY + " " + LangUtils.localize("tooltip.forDetails") + ".");
        } else {
            list.addAll(MekanismUtils.splitTooltip(
                  LangUtils.localize("tooltip." + getUnlocalizedName(itemstack).replace("tile.OreBlock.", "")),
                  itemstack));
        }
    }

    @Override
    public int getMetadata(int i) {
        return i;
    }

    @Override
    public String getUnlocalizedName(ItemStack itemstack) {
        String name;

        switch (itemstack.getItemDamage()) {
            case 0:
                name = "OsmiumOre";
                break;
            case 1:
                name = "CopperOre";
                break;
            case 2:
                name = "TinOre";
                break;
            default:
                name = "Unknown";
                break;
        }

        return getUnlocalizedName() + "." + name;
    }
}
