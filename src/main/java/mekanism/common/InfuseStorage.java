package mekanism.common;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.infuse.InfuseObject;
import mekanism.api.infuse.InfuseType;
import mekanism.common.base.ISustainedData;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;

public class InfuseStorage implements ISustainedData {

    private InfuseType type;

    private int amount;

    public InfuseStorage() {
    }

    public InfuseStorage(InfuseType infuseType, int infuseAmount) {
        type = infuseType;
        amount = infuseAmount;
    }

    /**
     * Replace this instance's properties with that from another
     *
     * @param other the instance to copy from
     */
    public void copyFrom(@Nonnull InfuseStorage other) {
        this.type = other.getType();
        this.amount = other.getAmount();
    }

    public boolean contains(InfuseStorage storage) {
        return type == storage.type && amount >= storage.amount;
    }

    public void subtract(InfuseStorage storage) {
        if (contains(storage)) {
            amount -= storage.amount;
        } else if (type == storage.type) {
            amount = 0;
        }
        if (amount <= 0) {
            type = null;
            amount = 0;
        }
    }

    public void increase(InfuseStorage input) {
        if (type == null) {
            type = input.type;
            amount = input.amount;
        } else if (type == input.type) {
            amount += input.amount;
        } else {
            Mekanism.logger.error("Tried to increase infusion storage with an incompatible type", new Exception());
        }
    }

    public void increase(InfuseObject input) {
        if (type == null) {
            type = input.type;
            amount = input.stored;
        } else if (type == input.type) {
            amount += input.stored;
        } else {
            Mekanism.logger.error("Tried to increase infusion storage with an incompatible type", new Exception());
        }
    }

    @Override
    public void writeSustainedData(ItemStack itemStack) {
        if (type != null && amount > 0) {
            write(itemStack.getOrCreateTag());
        }
    }

    @Override
    public void readSustainedData(ItemStack itemStack) {
        if (itemStack.hasTag()) {
            read(itemStack.getTag());
        } else {
            type = null;
            amount = 0;
        }
    }

    @Nullable
    public static InfuseStorage readFromNBT(CompoundNBT nbtTags) {
        if (nbtTags == null || nbtTags.isEmpty()) {
            return null;
        }

        InfuseType type = InfuseType.readFromNBT(nbtTags);
        int amount = nbtTags.getInt("amount");

        if (type == null || amount <= 0) {
            return null;
        }
        return new InfuseStorage(type, amount);
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

    public InfuseType getType() {
        return amount == 0 ? null : type;
    }

    public InfuseStorage setType(InfuseType type) {
        this.type = type;
        return this;
    }

    public int getAmount() {
        return type == null ? 0 : amount;
    }

    public InfuseStorage setAmount(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        this.amount = amount;
        return this;
    }

    public void setEmpty() {
        this.amount = 0;
        this.type = null;
    }
}