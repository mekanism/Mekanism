package mekanism.api.infuse;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.MekanismAPI;
import mekanism.api.sustained.ISustainedData;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;

//TODO: Rewrite this to be more like GsaTank
public class InfusionTank implements ISustainedData {

    @Nonnull
    private InfuseType type = MekanismAPI.EMPTY_INFUSE_TYPE;
    private int amount;

    public InfusionTank() {
    }

    public InfusionTank(@Nonnull InfuseType infuseType, int infuseAmount) {
        type = infuseType;
        amount = infuseAmount;
    }

    /**
     * Replace this instance's properties with that from another
     *
     * @param other the instance to copy from
     */
    public void copyFrom(@Nonnull InfusionTank other) {
        this.type = other.getType();
        this.amount = other.getAmount();
    }

    public boolean contains(InfusionTank storage) {
        return type == storage.type && amount >= storage.amount;
    }

    public void subtract(InfusionTank storage) {
        if (contains(storage)) {
            amount -= storage.amount;
        } else if (type == storage.type) {
            amount = 0;
        }
        if (amount <= 0) {
            type = MekanismAPI.EMPTY_INFUSE_TYPE;
            amount = 0;
        }
    }

    public void subtract(@Nonnull InfusionStack input) {
        if (type == input.getType() && amount >= input.getAmount()) {
            amount -= input.getAmount();
        } else if (type == input.getType()) {
            amount = 0;
        }
        if (amount <= 0) {
            type = MekanismAPI.EMPTY_INFUSE_TYPE;
            amount = 0;
        }
    }

    public void increase(InfusionTank input) {
        if (type == MekanismAPI.EMPTY_INFUSE_TYPE) {
            type = input.type;
            amount = input.amount;
        } else if (type == input.type) {
            amount += input.amount;
        } else {
            MekanismAPI.logger.error("Tried to increase infusion storage with an incompatible type", new Exception());
        }
    }

    public void increase(@Nonnull InfusionStack input) {
        if (type == MekanismAPI.EMPTY_INFUSE_TYPE) {
            type = input.getType();
            amount = input.getAmount();
        } else if (type == input.getType()) {
            amount += input.getAmount();
        } else {
            MekanismAPI.logger.error("Tried to increase infusion storage with an incompatible type", new Exception());
        }
    }

    @Override
    public void writeSustainedData(@Nonnull ItemStack itemStack) {
        if (type != MekanismAPI.EMPTY_INFUSE_TYPE && amount > 0) {
            write(itemStack.getOrCreateTag());
        }
    }

    @Override
    public void readSustainedData(@Nonnull ItemStack itemStack) {
        if (itemStack.hasTag()) {
            read(itemStack.getTag());
        } else {
            type = MekanismAPI.EMPTY_INFUSE_TYPE;
            amount = 0;
        }
    }

    @Nullable
    public static InfusionTank readFromNBT(CompoundNBT nbtTags) {
        if (nbtTags == null || nbtTags.isEmpty()) {
            return null;
        }

        InfuseType type = InfuseType.readFromNBT(nbtTags);
        int amount = nbtTags.getInt("amount");

        if (type == MekanismAPI.EMPTY_INFUSE_TYPE || amount <= 0) {
            return null;
        }
        return new InfusionTank(type, amount);
    }

    public void read(CompoundNBT nbtTags) {
        type = InfuseType.readFromNBT(nbtTags);
        amount = nbtTags.getInt("amount");
    }

    public CompoundNBT write(CompoundNBT nbtTags) {
        type.write(nbtTags);
        nbtTags.putInt("amount", amount);
        return nbtTags;
    }

    @Nonnull
    public InfuseType getType() {
        return amount == 0 ? MekanismAPI.EMPTY_INFUSE_TYPE : type;
    }

    public InfusionTank setType(@Nonnull InfuseType type) {
        this.type = type;
        return this;
    }

    public int getAmount() {
        return type == MekanismAPI.EMPTY_INFUSE_TYPE ? 0 : amount;
    }

    public boolean isEmpty() {
        return type == MekanismAPI.EMPTY_INFUSE_TYPE || amount <= 0;
    }

    public InfusionTank setAmount(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        this.amount = amount;
        return this;
    }

    public void setEmpty() {
        this.amount = 0;
        this.type = MekanismAPI.EMPTY_INFUSE_TYPE;
    }

    @Nonnull
    public InfusionStack getStack() {
        //TODO: does this need a separate empty check, also make this just return our stored stack
        return new InfusionStack(type, amount);
    }
}