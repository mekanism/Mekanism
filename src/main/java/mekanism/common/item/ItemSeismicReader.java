package mekanism.common.item;

import java.util.List;
import javax.annotation.Nonnull;
import mekanism.api.Action;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.inventory.AutomationType;
import mekanism.api.math.FloatingLong;
import mekanism.api.text.EnumColor;
import mekanism.client.key.MekKeyHandler;
import mekanism.client.key.MekanismKeyHandler;
import mekanism.common.MekanismLang;
import mekanism.common.config.MekanismConfig;
import mekanism.common.registries.MekanismContainerTypes;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.StorageUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Rarity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.Util;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;

public class ItemSeismicReader extends ItemEnergized {

    public ItemSeismicReader(Properties properties) {
        super(MekanismConfig.gear.seismicReaderChargeRate, MekanismConfig.gear.seismicReaderMaxEnergy, properties.rarity(Rarity.UNCOMMON));
    }

    @Override
    public void appendHoverText(@Nonnull ItemStack stack, World world, @Nonnull List<ITextComponent> tooltip, @Nonnull ITooltipFlag flag) {
        if (MekKeyHandler.isKeyPressed(MekanismKeyHandler.descriptionKey)) {
            tooltip.add(MekanismLang.DESCRIPTION_SEISMIC_READER.translate());
        } else if (MekKeyHandler.isKeyPressed(MekanismKeyHandler.detailsKey)) {
            super.appendHoverText(stack, world, tooltip, flag);
        } else {
            tooltip.add(MekanismLang.HOLD_FOR_DETAILS.translateColored(EnumColor.GRAY, EnumColor.INDIGO, MekanismKeyHandler.detailsKey.getTranslatedKeyMessage()));
            tooltip.add(MekanismLang.HOLD_FOR_DESCRIPTION.translateColored(EnumColor.GRAY, EnumColor.AQUA, MekanismKeyHandler.descriptionKey.getTranslatedKeyMessage()));
        }
    }

    @Nonnull
    @Override
    public ActionResult<ItemStack> use(World world, PlayerEntity player, @Nonnull Hand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (world.isClientSide) {
            return new ActionResult<>(ActionResultType.SUCCESS, stack);
        }
        if (!WorldUtils.isChunkVibrated(new ChunkPos(player.blockPosition()), player.level)) {
            player.sendMessage(MekanismUtils.logFormat(EnumColor.RED, MekanismLang.NO_VIBRATIONS), Util.NIL_UUID);
        } else {
            if (!player.isCreative()) {
                IEnergyContainer energyContainer = StorageUtils.getEnergyContainer(stack, 0);
                FloatingLong energyUsage = MekanismConfig.gear.seismicReaderEnergyUsage.get();
                if (energyContainer == null || energyContainer.extract(energyUsage, Action.SIMULATE, AutomationType.MANUAL).smallerThan(energyUsage)) {
                    player.sendMessage(MekanismUtils.logFormat(EnumColor.RED, MekanismLang.NEEDS_ENERGY), Util.NIL_UUID);
                    return new ActionResult<>(ActionResultType.SUCCESS, stack);
                }
                energyContainer.extract(energyUsage, Action.EXECUTE, AutomationType.MANUAL);
            }
            MekanismContainerTypes.SEISMIC_READER.tryOpenGui((ServerPlayerEntity) player, hand, stack);
        }
        return new ActionResult<>(ActionResultType.SUCCESS, stack);
    }
}