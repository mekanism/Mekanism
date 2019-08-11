package mekanism.common.item;

import java.util.List;
import javax.annotation.Nonnull;
import mekanism.api.Chunk3D;
import mekanism.api.EnumColor;
import mekanism.client.MekKeyHandler;
import mekanism.client.MekanismKeyHandler;
import mekanism.common.Mekanism;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.TextComponentUtil;
import mekanism.common.util.TextComponentUtil.Translation;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;

public class ItemSeismicReader extends ItemEnergized {

    public static final double ENERGY_USAGE = 250;

    public ItemSeismicReader() {
        super("seismic_reader", 12000);
    }

    @Override
    public boolean canSend(ItemStack itemStack) {
        return false;
    }

    @Override
    public void addInformation(ItemStack itemstack, World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
        if (!MekKeyHandler.getIsKeyPressed(MekanismKeyHandler.sneakKey)) {
            tooltip.add(TextComponentUtil.build(Translation.of("mekanism.tooltip.hold"), " ", EnumColor.INDIGO, MekanismKeyHandler.sneakKey.getKey(),
                  EnumColor.GREY, " ", Translation.of("mekanism.tooltip.for_details"), "."));
            tooltip.add(TextComponentUtil.build(Translation.of("mekanism.tooltip.hold"), " ", EnumColor.AQUA, MekanismKeyHandler.sneakKey.getKey(),
                  EnumColor.GREY, " ", Translation.of("mekanism.tooltip.and"), " ", EnumColor.AQUA, MekanismKeyHandler.modeSwitchKey.getKey(), EnumColor.GREY, " ",
                  Translation.of("tooltip.forDesc"), "."));
        } else if (!MekKeyHandler.getIsKeyPressed(MekanismKeyHandler.modeSwitchKey)) {
            super.addInformation(itemstack, world, tooltip, flag);
        } else {
            tooltip.add(TextComponentUtil.build(Translation.of("tooltip.mekanism.seismic_reader")));
        }
    }

    @Nonnull
    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity entityplayer, @Nonnull Hand hand) {
        Chunk3D chunk = new Chunk3D(entityplayer);
        ItemStack itemstack = entityplayer.getHeldItem(hand);

        if (getEnergy(itemstack) < ENERGY_USAGE && !entityplayer.isCreative()) {
            if (!world.isRemote) {
                entityplayer.sendMessage(TextComponentUtil.build(EnumColor.DARK_BLUE, Mekanism.LOG_TAG + " ", EnumColor.RED,
                      Translation.of("mekanism.tooltip.seismicReader.needsEnergy")));
            }

            return new ActionResult<>(ActionResultType.SUCCESS, itemstack);
        } else if (!MekanismUtils.isChunkVibrated(chunk)) {
            if (!world.isRemote) {
                entityplayer.sendMessage(TextComponentUtil.build(EnumColor.DARK_BLUE, Mekanism.LOG_TAG + " ", EnumColor.RED,
                      Translation.of("mekanism.tooltip.seismicReader.noVibrations")));
            }
            return new ActionResult<>(ActionResultType.SUCCESS, itemstack);
        }
        if (!entityplayer.isCreative()) {
            setEnergy(itemstack, getEnergy(itemstack) - ENERGY_USAGE);
        }
        MekanismUtils.openItemGui(entityplayer, hand, 38);
        return new ActionResult<>(ActionResultType.PASS, itemstack);
    }
}