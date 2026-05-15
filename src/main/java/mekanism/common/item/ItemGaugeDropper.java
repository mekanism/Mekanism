package mekanism.common.item;

import java.util.function.Consumer;
import mekanism.api.resource.IMekanismResourceHandler;
import mekanism.api.resource.IResourceContainer;
import mekanism.api.functions.ConstantPredicates;
import mekanism.common.attachments.containers.chemical.ComponentBackedChemicalTank;
import mekanism.common.attachments.containers.chemical.merged.MergedTankCreator;
import mekanism.common.attachments.containers.fluid.ComponentBackedFluidTank;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.config.MekanismConfig;
import mekanism.common.util.ChemicalUtils;
import mekanism.common.util.FluidUtils;
import mekanism.common.util.StorageUtils;
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
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.resource.Resource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ItemGaugeDropper extends Item {

    public static final MergedTankCreator MERGED_TANK_CREATOR = new MergedTankCreator(
          (type, attachedTo, containerIndex) -> new ComponentBackedChemicalTank(attachedTo, containerIndex,
                ConstantPredicates.alwaysTrueBi(), ConstantPredicates.alwaysTrueBi(), ConstantPredicates.alwaysTrue(),
                MekanismConfig.gear.gaugeDroppedTransferRate, MekanismConfig.gear.gaugeDropperCapacity, null
          ),
          (type, attachedTo, containerIndex) -> new ComponentBackedFluidTank(attachedTo, containerIndex,
                ConstantPredicates.alwaysTrueBi(), ConstantPredicates.alwaysTrueBi(), ConstantPredicates.alwaysTrue(),
                MekanismConfig.gear.gaugeDroppedTransferRate, MekanismConfig.gear.gaugeDropperCapacity
          )
    );

    public ItemGaugeDropper(Properties properties) {
        super(properties.stacksTo(1).rarity(Rarity.UNCOMMON));
    }

    @Override
    public boolean isBarVisible(@NotNull ItemStack stack) {
        return true;
    }

    @Override
    public int getBarWidth(@NotNull ItemStack stack) {
        return StorageUtils.getBarWidth(stack);
    }

    @Override
    public int getBarColor(@NotNull ItemStack stack) {
        FluidStack fluid = StorageUtils.getFirstFluidFromAttachment(stack);
        if (!fluid.isEmpty()) {
            return FluidUtils.getRGBDurabilityForDisplay(stack);
        }
        return ChemicalUtils.getRGBDurabilityForDisplay(stack);
    }

    @NotNull
    @Override
    public InteractionResult use(@NotNull Level world, Player player, @NotNull InteractionHand hand) {
        if (player.isShiftKeyDown()) {
            if (world.isClientSide()) {
                return InteractionResult.SUCCESS_SERVER;
            }
            ItemAccess itemAccess = ItemAccess.forPlayerInteraction(player, hand);
            dumpHandler(Capabilities.FLUID.getCapability(itemAccess));
            dumpHandler(Capabilities.CHEMICAL.getCapability(itemAccess));
            //TODO - 26.1: Is this the correct way to transform the output?
            return InteractionResult.SUCCESS_SERVER.heldItemTransformedTo(itemAccess.getResource().toStack(itemAccess.getAmount()));
        }
        return InteractionResult.PASS;
    }

    private <RESOURCE extends Resource> void dumpHandler(@Nullable ResourceHandler<RESOURCE> handler) {
        if (handler instanceof IMekanismResourceHandler<RESOURCE, ?> handlerItem) {//TODO - 26.1: Test if this works
            for (IResourceContainer<RESOURCE> container : handlerItem.getContainers()) {
                container.setEmpty();
            }
        }
    }

    @Override
    @Deprecated
    public void appendHoverText(@NotNull ItemStack stack, @NotNull Item.TooltipContext context, @NotNull TooltipDisplay tooltipDisplay, @NotNull Consumer<Component> tooltipAdder, @NotNull TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltipDisplay, tooltipAdder, flag);
        StorageUtils.addStoredSubstance(stack, tooltipAdder, false);
    }
}