package mekanism.common.item;

import java.util.List;
import java.util.OptionalInt;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.ChemicalTankBuilder;
import mekanism.api.chemical.IChemicalHandler;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.api.fluid.IExtendedFluidHandler;
import mekanism.common.attachments.containers.chemical.gas.ComponentBackedGasTank;
import mekanism.common.attachments.containers.chemical.infuse.ComponentBackedInfusionTank;
import mekanism.common.attachments.containers.chemical.merged.MergedTankCreator;
import mekanism.common.attachments.containers.chemical.pigment.ComponentBackedPigmentTank;
import mekanism.common.attachments.containers.chemical.slurry.ComponentBackedSlurryTank;
import mekanism.common.attachments.containers.fluid.ComponentBackedFluidTank;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.fluid.BasicFluidTank;
import mekanism.common.config.MekanismConfig;
import mekanism.common.util.ChemicalUtil;
import mekanism.common.util.FluidUtils;
import mekanism.common.util.StorageUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import org.jetbrains.annotations.NotNull;

public class ItemGaugeDropper extends Item {

    public static final MergedTankCreator MERGED_TANK_CREATOR = new MergedTankCreator(
          (type, attachedTo, containerIndex) -> new ComponentBackedGasTank(attachedTo, containerIndex, ChemicalTankBuilder.GAS.alwaysTrueBi, ChemicalTankBuilder.GAS.alwaysTrueBi,
                ChemicalTankBuilder.GAS.alwaysTrue, MekanismConfig.gear.gaugeDroppedTransferRate, MekanismConfig.gear.gaugeDropperCapacity, null),
          (type, attachedTo, containerIndex) -> new ComponentBackedInfusionTank(attachedTo, containerIndex, ChemicalTankBuilder.INFUSION.alwaysTrueBi, ChemicalTankBuilder.INFUSION.alwaysTrueBi,
                ChemicalTankBuilder.INFUSION.alwaysTrue, MekanismConfig.gear.gaugeDroppedTransferRate, MekanismConfig.gear.gaugeDropperCapacity, null),
          (type, attachedTo, containerIndex) -> new ComponentBackedPigmentTank(attachedTo, containerIndex, ChemicalTankBuilder.PIGMENT.alwaysTrueBi, ChemicalTankBuilder.PIGMENT.alwaysTrueBi,
                ChemicalTankBuilder.PIGMENT.alwaysTrue, MekanismConfig.gear.gaugeDroppedTransferRate, MekanismConfig.gear.gaugeDropperCapacity, null),
          (type, attachedTo, containerIndex) -> new ComponentBackedSlurryTank(attachedTo, containerIndex, ChemicalTankBuilder.SLURRY.alwaysTrueBi, ChemicalTankBuilder.SLURRY.alwaysTrueBi,
                ChemicalTankBuilder.SLURRY.alwaysTrue, MekanismConfig.gear.gaugeDroppedTransferRate, MekanismConfig.gear.gaugeDropperCapacity, null),
          (type, attachedTo, containerIndex) -> new ComponentBackedFluidTank(attachedTo, containerIndex, BasicFluidTank.alwaysTrueBi, BasicFluidTank.alwaysTrueBi,
                BasicFluidTank.alwaysTrue, MekanismConfig.gear.gaugeDroppedTransferRate, MekanismConfig.gear.gaugeDropperCapacity)
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
        OptionalInt color = FluidUtils.getRGBDurabilityForDisplay(stack);
        if (color.isPresent()) {
            return color.getAsInt();
        }
        return ChemicalUtil.getRGBDurabilityForDisplay(stack);
    }

    @NotNull
    @Override
    public InteractionResultHolder<ItemStack> use(@NotNull Level world, Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isShiftKeyDown()) {
            if (!world.isClientSide) {
                IFluidHandlerItem fluidHandler = Capabilities.FLUID.getCapability(stack);
                if (fluidHandler instanceof IExtendedFluidHandler fluidHandlerItem) {
                    for (int tank = 0, tanks = fluidHandlerItem.getTanks(); tank < tanks; tank++) {
                        fluidHandlerItem.setFluidInTank(tank, FluidStack.EMPTY);
                    }
                }
                clearChemicalTanks(stack, GasStack.EMPTY);
                clearChemicalTanks(stack, InfusionStack.EMPTY);
                clearChemicalTanks(stack, PigmentStack.EMPTY);
                clearChemicalTanks(stack, SlurryStack.EMPTY);
            }
            return InteractionResultHolder.sidedSuccess(stack, world.isClientSide);
        }
        return InteractionResultHolder.pass(stack);
    }

    private static <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> void clearChemicalTanks(ItemStack stack, STACK empty) {
        IChemicalHandler<CHEMICAL, STACK> handler = ChemicalUtil.getCapabilityForChemical(empty).getCapability(stack);
        if (handler != null) {
            for (int tank = 0; tank < handler.getTanks(); tank++) {
                handler.setChemicalInTank(tank, empty);
            }
        }
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull Item.TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        StorageUtils.addStoredSubstance(stack, tooltip, false);
    }
}