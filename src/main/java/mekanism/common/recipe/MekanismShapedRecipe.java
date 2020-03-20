package mekanism.common.recipe;

import java.util.EnumMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.Action;
import mekanism.api.NBTConstants;
import mekanism.api.Upgrade;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasHandler;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.attribute.AttributeUpgradeSupport;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.item.block.ItemBlockBin;
import mekanism.common.registries.MekanismRecipeSerializers;
import mekanism.common.security.ISecurityItem;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.NBTUtils;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ICraftingRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapedRecipe;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.crafting.IShapedRecipe;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.items.ItemHandlerHelper;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class MekanismShapedRecipe implements ICraftingRecipe, IShapedRecipe<CraftingInventory> {

    private final ShapedRecipe internal;

    public MekanismShapedRecipe(ShapedRecipe internal) {
        this.internal = internal;
    }

    public ShapedRecipe getInternal() {
        return internal;
    }

    @Override
    public IRecipeSerializer<?> getSerializer() {
        return MekanismRecipeSerializers.MEK_DATA.getRecipeSerializer();
    }

    @Override
    public boolean matches(CraftingInventory inv, World world) {
        //Note: We do not override the matches method as we ignore checking NBT for purposes of if the recipe matches
        // we only take NBT into account for figuring out what data we need to transfer to the output item
        //TODO: Do we need to actually override matching so that we can validate the fact that internal components have valid data
        return internal.matches(inv, world);
    }

    @Override
    public ItemStack getCraftingResult(CraftingInventory inv) {
        if (getRecipeOutput().isEmpty()) {
            return ItemStack.EMPTY;
        }
        //TODO: Rewrite this again, to bypass any and all limits, and support things that don't actually expose capabilities for the stored data
        //TODO: Make sure the output doesn't have extra NBT for no reason, for example it seems that factories end up with NBT in the output?
        ItemStack toReturn = getRecipeOutput().copy();
        Item item = toReturn.getItem();
        int invLength = inv.getSizeInventory();
        //Transfer energy
        /*if (item instanceof IEnergizedItem) {
            IEnergizedItem energizedItem = (IEnergizedItem) item;
            double maxEnergy = energizedItem.getMaxEnergy(toReturn);
            if (maxEnergy > 0) {
                double energyFound = 0;
                for (int i = 0; i < invLength; i++) {
                    ItemStack stack = inv.getStackInSlot(i);
                    if (!stack.isEmpty() && stack.getItem() instanceof IEnergizedItem) {
                        energyFound += ((IEnergizedItem) stack.getItem()).getEnergy(stack);
                    }
                }
                double energyToSet = Math.min(maxEnergy, energyFound);
                if (energyToSet > 0) {
                    energizedItem.setEnergy(toReturn, energyToSet);
                }
            }
        }*/
        //Transfer gas
        Optional<IGasHandler> resultCapability = MekanismUtils.toOptional(toReturn.getCapability(Capabilities.GAS_HANDLER_CAPABILITY));
        if (resultCapability.isPresent()) {
            IGasHandler gasHandlerResult = resultCapability.get();
            for (int i = 0; i < invLength; i++) {
                ItemStack stack = inv.getStackInSlot(i);
                if (!stack.isEmpty()) {
                    Optional<IGasHandler> capability = MekanismUtils.toOptional(stack.getCapability(Capabilities.GAS_HANDLER_CAPABILITY));
                    if (capability.isPresent()) {
                        IGasHandler gasHandlerItem = capability.get();
                        for (int tank = 0; tank < gasHandlerItem.getGasTankCount(); tank++) {
                            GasStack gasInItem = gasHandlerItem.getGasInTank(tank);
                            if (!gasInItem.isEmpty()) {
                                //Simulate inserting gas from each tank in the item into our tank
                                GasStack simulatedRemainder = gasHandlerResult.insertGas(gasInItem, Action.SIMULATE);
                                int gasInItemAmount = gasInItem.getAmount();
                                int remainder = simulatedRemainder.getAmount();
                                if (remainder < gasInItemAmount) {
                                    //If we were simulated that we could actually insert any, then
                                    // extract up to as much gas as we were able to accept from the item
                                    GasStack extractedGas = gasHandlerItem.extractGas(tank, gasInItemAmount - remainder, Action.SIMULATE);
                                    if (!extractedGas.isEmpty()) {
                                        //If we were able to actually extract it from the item, then insert it into our gas tank
                                        if (!gasHandlerResult.insertGas(extractedGas, Action.EXECUTE).isEmpty()) {
                                            //TODO: Print warning/error
                                        }
                                    }
                                } else {
                                    //If the gas is not valid for the recipe so just return empty
                                    return ItemStack.EMPTY;
                                }
                            }
                        }
                    }
                }
            }
        }
        //Transfer security settings
        if (item instanceof ISecurityItem) {
            ISecurityItem securityItem = (ISecurityItem) item;
            for (int i = 0; i < invLength; i++) {
                ItemStack stack = inv.getStackInSlot(i);
                if (!stack.isEmpty() && stack.getItem() instanceof ISecurityItem) {
                    ISecurityItem stackSecurityItem = (ISecurityItem) stack.getItem();
                    UUID ownerUUID = stackSecurityItem.getOwnerUUID(stack);
                    if (ownerUUID != null) {
                        //Only set the security if the component currently has security
                        securityItem.setOwnerUUID(toReturn, ownerUUID);
                        securityItem.setSecurity(toReturn, stackSecurityItem.getSecurity(stack));
                        break;
                    }
                }
            }
        }
        //Transfer contained fluid
        Optional<IFluidHandlerItem> cap = MekanismUtils.toOptional(FluidUtil.getFluidHandler(toReturn));
        if (cap.isPresent()) {
            IFluidHandlerItem fluidContainerItem = cap.get();
            FluidStack fluidFound = FluidStack.EMPTY;
            for (int i = 0; i < invLength; i++) {
                ItemStack stack = inv.getStackInSlot(i);
                if (!stack.isEmpty()) {
                    Optional<IFluidHandlerItem> capability = MekanismUtils.toOptional(FluidUtil.getFluidHandler(stack));
                    if (capability.isPresent()) {
                        IFluidHandlerItem fluidHandlerItem = capability.get();
                        for (int tank = 0; tank < fluidHandlerItem.getTanks(); tank++) {
                            FluidStack stored = fluidHandlerItem.getFluidInTank(tank);
                            if (!stored.isEmpty()) {
                                if (fluidContainerItem.fill(stored, FluidAction.SIMULATE) == 0) {
                                    //If the fluid is not valid for the recipe so just return empty
                                    return ItemStack.EMPTY;
                                }
                                if (fluidFound.isEmpty()) {
                                    fluidFound = stored;
                                } else if (fluidFound.isFluidEqual(stored)) {
                                    fluidFound.grow(stored.getAmount());
                                } else {
                                    //If there are multiple types of fluids just return empty
                                    //TODO: Fix the case of if our new item can support multiple fluids (none of our items currently do so)
                                    return ItemStack.EMPTY;
                                }
                            }
                        }
                    }
                }
            }
            if (!fluidFound.isEmpty()) {
                fluidContainerItem.fill(fluidFound, FluidAction.EXECUTE);
            }
        }
        //TODO: Fix other things like factories not transferring inventory contents
        // While doing so we can maybe improve how the bin is handled as an item
        //Transfer bin contents
        if (item instanceof ItemBlockBin) {
            //TODO: Improve how the bin inventory is handled as an item, so that we need less manual NBT modifications
            ItemStack foundType = ItemStack.EMPTY;
            for (int i = 0; i < invLength; i++) {
                ItemStack stack = inv.getStackInSlot(i);
                if (!stack.isEmpty() && stack.getItem() instanceof ItemBlockBin) {
                    ListNBT items = ItemDataUtils.getList(stack, NBTConstants.ITEMS);
                    if (!items.isEmpty()) {
                        CompoundNBT compound = items.getCompound(0);
                        if (compound.contains(NBTConstants.ITEM, NBT.TAG_COMPOUND)) {
                            ItemStack stored = ItemStack.read(compound.getCompound(NBTConstants.ITEM));
                            NBTUtils.setIntIfPresent(compound, NBTConstants.SIZE_OVERRIDE, stack::setCount);
                            if (foundType.isEmpty()) {
                                foundType = stored;
                            } else if (ItemHandlerHelper.canItemStacksStack(foundType, stored)) {
                                foundType.grow(stored.getCount());
                            } else {
                                //If there are multiple types of items something is wrong with the inputs for the recipe so just return empty
                                return ItemStack.EMPTY;
                            }
                        }
                    }
                }
            }
            if (!foundType.isEmpty()) {
                CompoundNBT nbt = new CompoundNBT();
                CompoundNBT compound = new CompoundNBT();
                foundType.write(compound);
                nbt.put(NBTConstants.ITEM, compound);
                if (foundType.getCount() > foundType.getMaxStackSize()) {
                    nbt.putInt(NBTConstants.SIZE_OVERRIDE, foundType.getCount());
                }
                ListNBT items = new ListNBT();
                items.add(nbt);
                ItemDataUtils.setList(toReturn, NBTConstants.ITEMS, items);
            }
        }
        //Transfer stored upgrades
        if (item instanceof BlockItem && Attribute.has(((BlockItem) item).getBlock(), AttributeUpgradeSupport.class)) {
            //TODO: Fix this not transferring upgrade items that are in the upgrade slot itself and haven't finished being installed
            Map<Upgrade, Integer> upgrades = new EnumMap<>(Upgrade.class);
            for (int i = 0; i < invLength; i++) {
                ItemStack stack = inv.getStackInSlot(i);
                if (!stack.isEmpty() && stack.getItem() instanceof BlockItem && Attribute.has(((BlockItem) stack.getItem()).getBlock(), AttributeUpgradeSupport.class)) {
                    CompoundNBT componentUpgrade = ItemDataUtils.getCompound(stack, NBTConstants.COMPONENT_UPGRADE);
                    if (!componentUpgrade.isEmpty()) {
                        Map<Upgrade, Integer> stackMap = Upgrade.buildMap(componentUpgrade);
                        for (Entry<Upgrade, Integer> entry : stackMap.entrySet()) {
                            if (entry != null && entry.getKey() != null && entry.getValue() != null) {
                                Integer val = upgrades.get(entry.getKey());
                                upgrades.put(entry.getKey(), Math.min(entry.getKey().getMax(), (val != null ? val : 0) + entry.getValue()));
                            }
                        }
                    }
                }
            }
            if (!upgrades.isEmpty()) {
                //Only transfer upgrades if we were able to find any
                CompoundNBT nbt = new CompoundNBT();
                Upgrade.saveMap(upgrades, nbt);
                ItemDataUtils.setCompound(toReturn, NBTConstants.COMPONENT_UPGRADE, nbt);
            }
        }
        return toReturn;
    }

    @Override
    public boolean canFit(int width, int height) {
        return internal.canFit(width, height);
    }

    @Override
    public ItemStack getRecipeOutput() {
        return internal.getRecipeOutput();
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInventory inv) {
        return internal.getRemainingItems(inv);
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return internal.getIngredients();
    }

    @Override
    public boolean isDynamic() {
        return internal.isDynamic();
    }

    @Override
    public String getGroup() {
        return internal.getGroup();
    }

    @Override
    public ItemStack getIcon() {
        return internal.getIcon();
    }

    @Override
    public ResourceLocation getId() {
        return internal.getId();
    }

    @Override
    public int getRecipeWidth() {
        return internal.getRecipeWidth();
    }

    @Override
    public int getRecipeHeight() {
        return internal.getRecipeHeight();
    }
}