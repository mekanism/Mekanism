package mekanism.common.item.gear;

import java.util.List;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Action;
import mekanism.api.chemical.gas.BasicGasTank;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasHandler;
import mekanism.api.chemical.gas.IGasHandler.IMekanismGasHandler;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.api.inventory.AutomationType;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.ItemCapabilityWrapper;
import mekanism.common.capabilities.chemical.item.RateLimitGasHandler;
import mekanism.common.config.MekanismConfig;
import mekanism.common.registries.MekanismGases;
import mekanism.common.util.GasUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.StorageUtils;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Rarity;
import net.minecraft.item.UseAction;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

public class ItemCanteen extends Item {

    private static final long TRANSFER_RATE = 128;

    public ItemCanteen(Properties properties) {
        super(properties.rarity(Rarity.UNCOMMON).maxStackSize(1).setNoRepair());
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
        StorageUtils.addStoredGas(stack, tooltip, true, false);
    }

    @Override
    public boolean showDurabilityBar(ItemStack stack) {
        return true;
    }

    @Override
    public double getDurabilityForDisplay(ItemStack stack) {
        return StorageUtils.getDurabilityForDisplay(stack);
    }

    @Override
    public int getRGBDurabilityForDisplay(ItemStack stack) {
        GasStack stored = StorageUtils.getStoredGasFromNBT(stack);
        return stored.isEmpty() ? 0 : stored.getType().getTint();
    }

    @Override
    public void fillItemGroup(@Nonnull ItemGroup group, @Nonnull NonNullList<ItemStack> items) {
        super.fillItemGroup(group, items);
        if (isInGroup(group)) {
            items.add(GasUtils.getFilledVariant(new ItemStack(this), MekanismConfig.gear.canteenMaxStorage.get(), MekanismGases.NUTRITIONAL_PASTE));
        }
    }

    @Override
    public ItemStack onItemUseFinish(ItemStack stack, World worldIn, LivingEntity entityLiving) {
        if (entityLiving instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity) entityLiving;
            long needed = Math.min(20 - player.getFoodStats().getFoodLevel(), getGas(stack).getAmount() / MekanismConfig.general.nutritionalPasteMBPerFood.get());
            player.getFoodStats().addStats((int) needed, MekanismConfig.general.nutritionalPasteSaturation.get());
            useGas(stack, needed * MekanismConfig.general.nutritionalPasteMBPerFood.get());
        }
        return stack;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 32;
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.DRINK;
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, CompoundNBT nbt) {
        return new ItemCapabilityWrapper(stack, RateLimitGasHandler.create(() -> TRANSFER_RATE, () -> MekanismConfig.gear.canteenMaxStorage.get(),
              (item, automationType) -> automationType != AutomationType.EXTERNAL, BasicGasTank.alwaysTrueBi, gas -> gas == MekanismGases.NUTRITIONAL_PASTE.getGas()));
    }

    @Nonnull
    private GasStack useGas(ItemStack stack, long amount) {
        Optional<IGasHandler> capability = MekanismUtils.toOptional(stack.getCapability(Capabilities.GAS_HANDLER_CAPABILITY));
        if (capability.isPresent()) {
            IGasHandler gasHandlerItem = capability.get();
            if (gasHandlerItem instanceof IMekanismGasHandler) {
                IGasTank gasTank = ((IMekanismGasHandler) gasHandlerItem).getChemicalTank(0, null);
                if (gasTank != null) {
                    return gasTank.extract(amount, Action.EXECUTE, AutomationType.MANUAL);
                }
            }
            return gasHandlerItem.extractChemical(amount, Action.EXECUTE);
        }
        return GasStack.EMPTY;
    }

    private GasStack getGas(ItemStack stack) {
        Optional<IGasHandler> capability = MekanismUtils.toOptional(stack.getCapability(Capabilities.GAS_HANDLER_CAPABILITY));
        if (capability.isPresent()) {
            IGasHandler gasHandlerItem = capability.get();
            if (gasHandlerItem instanceof IMekanismGasHandler) {
                IGasTank gasTank = ((IMekanismGasHandler) gasHandlerItem).getChemicalTank(0, null);
                if (gasTank != null) {
                    return gasTank.getStack();
                }
            }
            return gasHandlerItem.getChemicalInTank(0);
        }
        return GasStack.EMPTY;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
        if (!playerIn.isCreative() && playerIn.canEat(false) && getGas(playerIn.getHeldItem(handIn)).getAmount() >= 50) {
            playerIn.setActiveHand(handIn);
        }
        return ActionResult.resultSuccess(playerIn.getHeldItem(handIn));
    }
}
