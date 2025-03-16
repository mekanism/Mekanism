package mekanism.common.item;

import mekanism.api.IAlloyInteraction;
import mekanism.api.tier.IAlloyTier;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.config.MekanismConfig;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class ItemAlloy extends Item {

    private final IAlloyTier tier;

    public ItemAlloy(IAlloyTier tier, Properties properties) {
        super(properties);
        this.tier = tier;
    }

    @NotNull
    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player != null && MekanismConfig.general.transmitterAlloyUpgrade.get()) {
            Level world = context.getLevel();
            BlockPos pos = context.getClickedPos();
            IAlloyInteraction alloyInteraction = WorldUtils.getCapability(world, Capabilities.ALLOY_INTERACTION, pos, context.getClickedFace());
            if (alloyInteraction != null) {
                if (!world.isClientSide) {
                    alloyInteraction.onAlloyInteraction(player, context.getItemInHand(), tier);
                }
                return InteractionResult.sidedSuccess(world.isClientSide);
            }
        }
        return InteractionResult.PASS;
    }

    public IAlloyTier getTier() {
        return tier;
    }
}