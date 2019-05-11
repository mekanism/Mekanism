package mekanism.generators.common.item;

import java.util.List;
import javax.annotation.Nonnull;
import mekanism.api.EnumColor;
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.generators.common.block.states.BlockStateReactor.ReactorBlockType;
import net.minecraft.block.Block;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

public class ItemBlockReactor extends ItemBlock {

    public Block metaBlock;

    public ItemBlockReactor(Block block) {
        super(block);
        metaBlock = block;
        setHasSubtypes(true);
    }

    @Override
    public int getMetadata(int i) {
        return i;
    }

    @Nonnull
    @Override
    public String getTranslationKey(ItemStack itemstack) {
        return getTranslationKey() + "." + ReactorBlockType.get(itemstack).name;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(@Nonnull ItemStack itemstack, World world, @Nonnull List<String> list, @Nonnull ITooltipFlag flag) {
        ReactorBlockType type = ReactorBlockType.get(itemstack);

        if (!Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
            list.add(LangUtils.localize("tooltip.hold") + " " + EnumColor.INDIGO + "shift" + EnumColor.GREY + " " + LangUtils.localize("tooltip.forDetails") + ".");
        } else {
            list.addAll(MekanismUtils.splitTooltip(type.getDescription(), itemstack));
        }
    }
}