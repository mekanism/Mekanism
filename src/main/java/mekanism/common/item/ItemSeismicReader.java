package mekanism.common.item;

import mekanism.api.text.EnumColor;
import mekanism.common.MekanismLang;
import mekanism.common.advancements.MekanismCriteriaTriggers;
import mekanism.common.registries.MekanismContainerTypes;
import mekanism.common.registries.MekanismDataComponents;
import mekanism.common.util.ItemAccessUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.transfer.access.ItemAccess;

public class ItemSeismicReader extends Item {

    public ItemSeismicReader(Properties properties) {
        super(properties.rarity(Rarity.UNCOMMON).stacksTo(1)
              .component(MekanismDataComponents.DESCRIPTION, MekanismLang.DESCRIPTION_SEISMIC_READER.translate())
        );
    }

    @Override
    public InteractionResult use(Level world, Player player, InteractionHand hand) {
        if (world.isClientSide()) {
            return InteractionResult.SUCCESS_SERVER;
        } else if (!WorldUtils.isChunkVibrated(ChunkPos.containing(player.blockPosition()), player.level())) {
            player.sendSystemMessage(MekanismUtils.logFormat(EnumColor.RED, MekanismLang.NO_VIBRATIONS));
            return InteractionResult.SUCCESS_SERVER;
        }
        ItemAccess itemAccess = ItemAccessUtils.playerHandAccess(player, hand);
        MekanismCriteriaTriggers.VIEW_VIBRATIONS.value().trigger((ServerPlayer) player);
        MekanismContainerTypes.SEISMIC_READER.tryOpenGui(player, hand, itemAccess);
        return InteractionResult.SUCCESS_SERVER.heldItemTransformedTo(ItemAccessUtils.asStack(itemAccess));
    }
}