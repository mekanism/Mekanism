package buildcraft.api.recipes;

import java.io.IOException;
import java.util.Iterator;

import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.NonNullList;

import net.minecraftforge.common.util.Constants;

public final class IntegrationRecipe {
    public final long requiredMicroJoules;
    public final @Nonnull ItemStack target;
    public final NonNullList<ItemStack> toIntegrate;
    public final ItemStack output;

    public IntegrationRecipe(long requiredMicroJoules, @Nonnull ItemStack target, NonNullList<ItemStack> toIntegrate, ItemStack output) {
        this.requiredMicroJoules = requiredMicroJoules;
        this.target = target;
        this.toIntegrate = toIntegrate;
        this.output = output;
    }

    public IntegrationRecipe(NBTTagCompound nbt) {
        requiredMicroJoules = nbt.getLong("required_micro_joules");
        target = new ItemStack(nbt.getCompoundTag("target"));
        NBTTagList toIntegrateTag = nbt.getTagList("to_integrate", Constants.NBT.TAG_COMPOUND);
        toIntegrate = NonNullList.withSize(toIntegrateTag.tagCount(), ItemStack.EMPTY);
        for (int i = 0; i < toIntegrateTag.tagCount(); i++) {
            toIntegrate.set(i, new ItemStack(toIntegrateTag.getCompoundTagAt(i)));
        }
        output = new ItemStack(nbt.getCompoundTag("output"));
    }

    public IntegrationRecipe(PacketBuffer buffer) throws IOException {
        requiredMicroJoules = buffer.readLong();
        ItemStack stack = buffer.readItemStack();
        if (stack == null) throw new NullPointerException("stack");// should never happen
        target = stack;
        int count = buffer.readInt();
        toIntegrate = NonNullList.withSize(count, ItemStack.EMPTY);
        for (int i = 0; i < count; i++) {
            toIntegrate.set(i, buffer.readItemStack());
        }
        output = buffer.readItemStack();
    }

    public NBTTagCompound writeToNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setLong("required_micro_joules", requiredMicroJoules);
        nbt.setTag("target", target.serializeNBT());
        NBTTagList toIntegrateTag = new NBTTagList();
        for (ItemStack toIntegrateElement : toIntegrate) {
            toIntegrateTag.appendTag(toIntegrateElement.serializeNBT());
        }
        nbt.setTag("to_integrate", toIntegrateTag);
        nbt.setTag("output", output.serializeNBT());
        return nbt;
    }

    public void writeToBuffer(PacketBuffer buffer) {
        buffer.writeLong(requiredMicroJoules);
        buffer.writeItemStack(target);
        buffer.writeInt(toIntegrate.size());
        for (ItemStack stack : toIntegrate) {
            buffer.writeItemStack(stack);
        }
        buffer.writeItemStack(output);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        IntegrationRecipe that = (IntegrationRecipe) o;

        if (requiredMicroJoules != that.requiredMicroJoules) {
            return false;
        }
        if (!ItemStack.areItemStacksEqual(target, that.target)) {
            return false;
        }
        if (toIntegrate != null && that.toIntegrate != null) {
            Iterator<ItemStack> iterator1 = toIntegrate.iterator();
            Iterator<ItemStack> iterator2 = that.toIntegrate.iterator();
            while (iterator1.hasNext()) {
                if (!iterator2.hasNext()) {
                    return false;
                }
                ItemStack o1 = iterator1.next();
                ItemStack o2 = iterator2.next();
                if (!ItemStack.areItemStacksEqual(o1, o2)) {
                    return false;
                }
            }
            return !iterator2.hasNext() && output != null ? ItemStack.areItemStacksEqual(output, that.output) : that.output == null;
        } else {
            return toIntegrate == null && that.toIntegrate == null && output != null ? ItemStack.areItemStacksEqual(output, that.output) : that.output == null;
        }
    }

    @Override
    public int hashCode() {
        int result = (int) (requiredMicroJoules ^ (requiredMicroJoules >>> 32));
        result = 31 * result + (target != null ? target.hashCode() : 0);
        result = 31 * result + (toIntegrate != null ? toIntegrate.hashCode() : 0);
        result = 31 * result + (output != null ? output.hashCode() : 0);
        return result;
    }
}
