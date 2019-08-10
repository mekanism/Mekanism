package mekanism.common.item.block.transmitter;

import java.util.List;
import javax.annotation.Nonnull;
import mcmultipart.api.multipart.IMultipart;
import mekanism.api.EnumColor;
import mekanism.client.MekKeyHandler;
import mekanism.client.MekanismKeyHandler;
import mekanism.common.block.transmitter.BlockDiversionTransporter;
import mekanism.common.integration.MekanismHooks;
import mekanism.common.integration.multipart.MultipartMekanism;
import mekanism.common.item.block.ItemBlockMultipartAble;
import mekanism.common.util.LangUtils;
import net.minecraft.client.GameSettings;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.common.Optional;

public class ItemBlockDiversionTransporter extends ItemBlockMultipartAble<BlockDiversionTransporter> {

    public ItemBlockDiversionTransporter(BlockDiversionTransporter block) {
        super(block);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(@Nonnull ItemStack itemstack, World world, @Nonnull List<ITextComponent> tooltip, @Nonnull ITooltipFlag flag) {
        if (!MekKeyHandler.getIsKeyPressed(MekanismKeyHandler.sneakKey)) {
            tooltip.add(LangUtils.localize("tooltip.hold") + " " + EnumColor.AQUA + GameSettings.getKeyDisplayString(MekanismKeyHandler.sneakKey.getKeyCode()) +
                     EnumColor.GREY + " " + LangUtils.localize("tooltip.forDetails"));
        } else {
            tooltip.add(EnumColor.DARK_GREY + LangUtils.localize("tooltip.capableTrans") + ":");
            tooltip.add("- " + EnumColor.PURPLE + LangUtils.localize("tooltip.items") + " (" + LangUtils.localize("tooltip.universal") + ")");
            tooltip.add("- " + EnumColor.PURPLE + LangUtils.localize("tooltip.blocks") + " (" + LangUtils.localize("tooltip.universal") + ")");
            tooltip.add("- " + EnumColor.DARK_RED + LangUtils.localize("tooltip.diversionDesc"));
        }
    }

    @Override
    @Optional.Method(modid = MekanismHooks.MCMULTIPART_MOD_ID)
    protected IMultipart getMultiPart() {
        //TODO
        return MultipartMekanism.TRANSMITTER_MP;
    }
}