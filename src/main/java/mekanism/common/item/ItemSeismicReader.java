package mekanism.common.item;

import java.util.List;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.text.EnumColor;
import mekanism.client.key.MekKeyHandler;
import mekanism.client.key.MekanismKeyHandler;
import mekanism.common.MekanismLang;
import mekanism.common.advancements.MekanismCriteriaTriggers;
import mekanism.common.config.MekanismConfig;
import mekanism.common.registries.MekanismContainerTypes;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.StorageUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class ItemSeismicReader extends ItemEnergized {

    public ItemSeismicReader(Properties properties) {
        super(properties.rarity(Rarity.UNCOMMON).stacksTo(1));
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull Item.TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        if (MekKeyHandler.isKeyPressed(MekanismKeyHandler.descriptionKey)) {
            tooltip.add(MekanismLang.DESCRIPTION_SEISMIC_READER.translate());
        } else if (MekKeyHandler.isKeyPressed(MekanismKeyHandler.detailsKey)) {
            super.appendHoverText(stack, context, tooltip, flag);
        } else {
            tooltip.add(MekanismLang.HOLD_FOR_DETAILS.translateColored(EnumColor.GRAY, EnumColor.INDIGO, MekanismKeyHandler.detailsKey.getTranslatedKeyMessage()));
            tooltip.add(MekanismLang.HOLD_FOR_DESCRIPTION.translateColored(EnumColor.GRAY, EnumColor.AQUA, MekanismKeyHandler.descriptionKey.getTranslatedKeyMessage()));
        }
    }

    @NotNull
    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (world.isClientSide) {
            return InteractionResultHolder.success(stack);
        }
        if (!WorldUtils.isChunkVibrated(new ChunkPos(player.blockPosition()), player.level())) {
            player.sendSystemMessage(MekanismUtils.logFormat(EnumColor.RED, MekanismLang.NO_VIBRATIONS));
        } else {
            if (!player.isCreative()) {
                IEnergyContainer energyContainer = StorageUtils.getEnergyContainer(stack, 0);
                long energyUsage = MekanismConfig.gear.seismicReaderEnergyUsage.get();
                if (energyContainer == null || energyContainer.extract(energyUsage, Action.SIMULATE, AutomationType.MANUAL) < energyUsage) {
                    player.sendSystemMessage(MekanismUtils.logFormat(EnumColor.RED, MekanismLang.NEEDS_ENERGY));
                    return InteractionResultHolder.consume(stack);
                }
                energyContainer.extract(energyUsage, Action.EXECUTE, AutomationType.MANUAL);
            }
            ServerPlayer serverPlayer = (ServerPlayer) player;
            MekanismCriteriaTriggers.VIEW_VIBRATIONS.value().trigger(serverPlayer);
            MekanismContainerTypes.SEISMIC_READER.tryOpenGui(serverPlayer, hand, stack);
        }
        return InteractionResultHolder.consume(stack);
    }
}