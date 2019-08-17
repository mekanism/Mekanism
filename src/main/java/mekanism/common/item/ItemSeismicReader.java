package mekanism.common.item;

import java.util.List;
import javax.annotation.Nonnull;
import mekanism.api.Chunk3D;
import mekanism.api.text.EnumColor;
import mekanism.client.MekKeyHandler;
import mekanism.client.MekanismKeyHandler;
import mekanism.common.Mekanism;
import mekanism.common.inventory.container.ContainerProvider;
import mekanism.common.inventory.container.item.SeismicReaderContainer;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.text.TextComponentUtil;
import mekanism.common.util.text.Translation;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

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
                  EnumColor.GRAY, " ", Translation.of("mekanism.tooltip.for_details"), "."));
            tooltip.add(TextComponentUtil.build(Translation.of("mekanism.tooltip.hold"), " ", EnumColor.AQUA, MekanismKeyHandler.sneakKey.getKey(),
                  EnumColor.GRAY, " ", Translation.of("mekanism.tooltip.and"), " ", EnumColor.AQUA, MekanismKeyHandler.modeSwitchKey.getKey(), EnumColor.GRAY, " ",
                  Translation.of("tooltip.forDesc"), "."));
        } else if (!MekKeyHandler.getIsKeyPressed(MekanismKeyHandler.modeSwitchKey)) {
            super.addInformation(itemstack, world, tooltip, flag);
        } else {
            tooltip.add(TextComponentUtil.translate("tooltip.mekanism.seismic_reader"));
        }
    }

    @Nonnull
    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, @Nonnull Hand hand) {
        Chunk3D chunk = new Chunk3D(player);
        ItemStack stack = player.getHeldItem(hand);

        if (getEnergy(stack) < ENERGY_USAGE && !player.isCreative()) {
            if (!world.isRemote) {
                player.sendMessage(TextComponentUtil.build(EnumColor.DARK_BLUE, Mekanism.LOG_TAG + " ", EnumColor.RED,
                      Translation.of("mekanism.tooltip.seismicReader.needsEnergy")));
            }

            return new ActionResult<>(ActionResultType.SUCCESS, stack);
        } else if (!MekanismUtils.isChunkVibrated(chunk)) {
            if (!world.isRemote) {
                player.sendMessage(TextComponentUtil.build(EnumColor.DARK_BLUE, Mekanism.LOG_TAG + " ", EnumColor.RED,
                      Translation.of("mekanism.tooltip.seismicReader.noVibrations")));
            }
            return new ActionResult<>(ActionResultType.SUCCESS, stack);
        }
        if (!player.isCreative()) {
            setEnergy(stack, getEnergy(stack) - ENERGY_USAGE);
        }
        NetworkHooks.openGui((ServerPlayerEntity) player, new ContainerProvider(stack.getDisplayName(), (i, inv, p) -> new SeismicReaderContainer(i, inv, hand, stack)), buf -> {
            buf.writeEnumValue(hand);
            buf.writeItemStack(stack);
        });
        return new ActionResult<>(ActionResultType.PASS, stack);
    }
}