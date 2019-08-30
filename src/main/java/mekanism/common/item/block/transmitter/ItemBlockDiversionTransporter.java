package mekanism.common.item.block.transmitter;

import java.util.List;
import javax.annotation.Nonnull;
import mekanism.api.text.EnumColor;
import mekanism.client.MekKeyHandler;
import mekanism.client.MekanismKeyHandler;
import mekanism.common.block.transmitter.BlockDiversionTransporter;
import mekanism.common.item.block.ItemBlockMultipartAble;
import mekanism.common.util.text.TextComponentUtil;
import mekanism.common.util.text.Translation;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ItemBlockDiversionTransporter extends ItemBlockMultipartAble<BlockDiversionTransporter> {

    public ItemBlockDiversionTransporter(BlockDiversionTransporter block) {
        super(block);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(@Nonnull ItemStack itemstack, World world, @Nonnull List<ITextComponent> tooltip, @Nonnull ITooltipFlag flag) {
        if (!MekKeyHandler.getIsKeyPressed(MekanismKeyHandler.sneakKey)) {
            tooltip.add(TextComponentUtil.build(Translation.of("tooltip.mekanism.hold"), " ", EnumColor.INDIGO, MekanismKeyHandler.sneakKey.getKey(),
                  EnumColor.GRAY, " ", Translation.of("tooltip.mekanism.for_details"), "."));
        } else {
            tooltip.add(TextComponentUtil.build(EnumColor.DARK_GRAY, Translation.of("tooltip.capableTrans"), ":"));
            tooltip.add(TextComponentUtil.build("- ", EnumColor.PURPLE, Translation.of("tooltip.items"), " (", Translation.of("tooltip.universal"), ")"));
            tooltip.add(TextComponentUtil.build("- ", EnumColor.PURPLE, Translation.of("tooltip.blocks"), " (", Translation.of("tooltip.universal"), ")"));
            tooltip.add(TextComponentUtil.build("- ", EnumColor.DARK_RED, Translation.of("tooltip.diversionDesc")));
        }
    }

    //TODO: Multipart
    /*@Override
    @Optional.Method(modid = MekanismHooks.MCMULTIPART_MOD_ID)
    protected IMultipart getMultiPart() {
        //TODO
        return MultipartMekanism.TRANSMITTER_MP;
    }*/
}