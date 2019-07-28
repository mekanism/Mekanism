package mekanism.generators.common.item;

import java.util.List;
import javax.annotation.Nonnull;
import mekanism.api.EnumColor;
import mekanism.common.block.interfaces.IBlockDescriptive;
import mekanism.common.item.ItemBlockMekanism;
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.block.Block;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

public class ItemBlockReactor extends ItemBlockMekanism {

    public ItemBlockReactor(Block block) {
        super(block);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(@Nonnull ItemStack itemstack, World world, @Nonnull List<String> list, @Nonnull ITooltipFlag flag) {
        if (!Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
            list.add(LangUtils.localize("tooltip.hold") + " " + EnumColor.INDIGO + "shift" + EnumColor.GREY + " " + LangUtils.localize("tooltip.forDetails") + ".");
        } else {
            list.addAll(MekanismUtils.splitTooltip(((IBlockDescriptive) block).getDescription(), itemstack));
        }
    }
}