package mekanism.common.item;

import java.util.function.Consumer;
import mekanism.api.functions.ConstantPredicates;
import mekanism.common.attachments.containers.chemical.ComponentBackedChemicalTank;
import mekanism.common.attachments.containers.creator.IBasicContainerCreator;
import mekanism.common.attachments.containers.fluid.ComponentBackedFluidTank;
import mekanism.common.attachments.containers.type.ContainerType;
import mekanism.common.capabilities.merged.MergedTank;
import mekanism.common.config.MekanismConfig;
import mekanism.common.util.ChemicalUtils;
import mekanism.common.util.FluidUtils;
import mekanism.common.util.ItemAccessUtils;
import mekanism.common.util.StorageUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import org.jetbrains.annotations.NotNull;

public class ItemGaugeDropper extends Item {

    public static final IBasicContainerCreator<MergedTank> MERGED_TANK_CREATOR = (attachedAccess, containerIndex) -> MergedTank.create(
          new ComponentBackedFluidTank(attachedAccess, containerIndex,
                ConstantPredicates.alwaysTrueBi(), ConstantPredicates.alwaysTrueBi(), ConstantPredicates.alwaysTrue(),
                MekanismConfig.gear.gaugeDroppedTransferRate, MekanismConfig.gear.gaugeDropperCapacity
          ),
          new ComponentBackedChemicalTank(attachedAccess, containerIndex,
                ConstantPredicates.alwaysTrueBi(), ConstantPredicates.alwaysTrueBi(), ConstantPredicates.alwaysTrue(),
                MekanismConfig.gear.gaugeDroppedTransferRate, MekanismConfig.gear.gaugeDropperCapacity, null
          )
    );

    public ItemGaugeDropper(Properties properties) {
        super(properties.stacksTo(1).rarity(Rarity.UNCOMMON));
    }

    @Override
    public boolean isBarVisible(@NotNull ItemStack stack) {
        return StorageUtils.isBarVisible(stack);
    }

    @Override
    public int getBarWidth(@NotNull ItemStack stack) {
        return StorageUtils.getBarWidth(stack);
    }

    @Override
    public int getBarColor(@NotNull ItemStack stack) {
        ItemAccess itemAccess = ItemAccess.forStack(stack);
        FluidResource fluid = ContainerType.FLUID.getFirstResourceFromAttachment(itemAccess);
        if (!fluid.isEmpty()) {
            return FluidUtils.getRGBDurabilityForDisplay(fluid);
        }
        return ChemicalUtils.getRGBDurabilityForDisplay(itemAccess);
    }

    @NotNull
    @Override
    public InteractionResult use(@NotNull Level level, Player player, @NotNull InteractionHand hand) {
        if (player.isShiftKeyDown()) {
            if (level.isClientSide()) {
                return InteractionResult.SUCCESS_SERVER;
            }
            BlockPos pos = player.blockPosition();
            ItemAccess itemAccess = ItemAccessUtils.playerHandAccess(player, hand);
            ContainerType.FLUID.tryDumpContents(level, pos, itemAccess, null);
            ContainerType.CHEMICAL.tryDumpContents(level, pos, itemAccess, null);
            return InteractionResult.SUCCESS_SERVER.heldItemTransformedTo(itemAccess.getResource().toStack(itemAccess.getAmount()));
        }
        return InteractionResult.PASS;
    }

    @Override
    @Deprecated
    public void appendHoverText(@NotNull ItemStack stack, @NotNull Item.TooltipContext context, @NotNull TooltipDisplay tooltipDisplay, @NotNull Consumer<Component> tooltipAdder, @NotNull TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltipDisplay, tooltipAdder, flag);
        StorageUtils.addStoredSubstance(ItemAccess.forStack(stack), tooltipAdder, false);
    }
}