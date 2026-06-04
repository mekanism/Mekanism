package mekanism.common.item;

import java.util.function.Consumer;
import mekanism.api.text.EnumColor;
import mekanism.client.key.MekKeyHandler;
import mekanism.client.key.MekanismKeyHandler;
import mekanism.common.MekanismLang;
import mekanism.common.advancements.MekanismCriteriaTriggers;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.config.MekanismConfig;
import mekanism.common.registries.MekanismContainerTypes;
import mekanism.common.util.EnergyUtils;
import mekanism.common.util.ItemAccessUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jetbrains.annotations.NotNull;

public class ItemSeismicReader extends ItemEnergized {

    public ItemSeismicReader(Properties properties) {
        super(properties.rarity(Rarity.UNCOMMON).stacksTo(1));
    }

    @Override
    @Deprecated
    public void appendHoverText(@NotNull ItemStack stack, @NotNull Item.TooltipContext context, @NotNull TooltipDisplay tooltipDisplay, @NotNull Consumer<Component> tooltipAdder, @NotNull TooltipFlag flag) {
        if (MekKeyHandler.isKeyPressed(MekanismKeyHandler.descriptionKey)) {
            tooltipAdder.accept(MekanismLang.DESCRIPTION_SEISMIC_READER.translate());
        } else if (MekKeyHandler.isKeyPressed(MekanismKeyHandler.detailsKey)) {
            super.appendHoverText(stack, context, tooltipDisplay, tooltipAdder, flag);
        } else {
            tooltipAdder.accept(MekanismLang.HOLD_FOR_DETAILS.translateColored(EnumColor.GRAY, EnumColor.INDIGO, MekanismKeyHandler.detailsKey.getTranslatedKeyMessage()));
            tooltipAdder.accept(MekanismLang.HOLD_FOR_DESCRIPTION.translateColored(EnumColor.GRAY, EnumColor.AQUA, MekanismKeyHandler.descriptionKey.getTranslatedKeyMessage()));
        }
    }

    @NotNull
    @Override
    public InteractionResult use(Level world, Player player, @NotNull InteractionHand hand) {
        if (world.isClientSide()) {
            return InteractionResult.SUCCESS_SERVER;
        } else if (!WorldUtils.isChunkVibrated(ChunkPos.containing(player.blockPosition()), player.level())) {
            player.sendSystemMessage(MekanismUtils.logFormat(EnumColor.RED, MekanismLang.NO_VIBRATIONS));
            return InteractionResult.SUCCESS_SERVER;
        }
        ItemAccess itemAccess = ItemAccessUtils.playerHandAccess(player, hand);
        if (!player.isCreative()) {
            EnergyHandler energyHandler = Capabilities.ENERGY.getCapability(itemAccess);
            if (energyHandler == null) {
                return needsEnergy(player);
            }
            try (Transaction transaction = Transaction.openRoot()) {
                int energyUsage = MekanismConfig.gear.seismicReaderEnergyUsage.get();
                if (EnergyUtils.extractManual(energyHandler, energyUsage, transaction) < energyUsage) {
                    return needsEnergy(player);
                }
                transaction.commit();
            }
        }
        MekanismCriteriaTriggers.VIEW_VIBRATIONS.value().trigger((ServerPlayer) player);
        MekanismContainerTypes.SEISMIC_READER.tryOpenGui(player, hand, itemAccess);
        return InteractionResult.SUCCESS_SERVER.heldItemTransformedTo(ItemAccessUtils.asStack(itemAccess));
    }

    private InteractionResult needsEnergy(Player player) {
        player.sendSystemMessage(MekanismUtils.logFormat(EnumColor.RED, MekanismLang.NEEDS_ENERGY));
        return InteractionResult.FAIL;
    }
}