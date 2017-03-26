/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license, which
 * should be located as "LICENSE.API" in the BuildCraft source code distribution. */
package buildcraft.api.statements;

import java.util.Objects;

import javax.annotation.Nonnull;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class StatementParameterItemStack implements IStatementParameter {
    // needed because ItemStack.EMPTY doesn't have @Nonnull applied to it :/
    @Nonnull
    private static final ItemStack EMPTY_STACK;

    static {
        ItemStack stack = ItemStack.EMPTY;
        if (stack == null) throw new Error("Somehow ItemStack.EMPTY was null!");
        EMPTY_STACK = stack;
    }

    @Nonnull
    protected ItemStack stack = EMPTY_STACK;

    @Override
    public TextureAtlasSprite getGuiSprite() {
        return null;
    }

    @Override
    public ItemStack getItemStack() {
        return stack;
    }

    @Override
    public boolean onClick(IStatementContainer source, IStatement stmt, ItemStack stack, StatementMouseClick mouse) {
        if (stack != null) {
            this.stack = stack.copy();
            this.stack.setCount(1);
        } else {
            this.stack = EMPTY_STACK;
        }
        return true;
    }

    @Override
    public void writeToNBT(NBTTagCompound compound) {
        if (!stack.isEmpty()) {
            NBTTagCompound tagCompound = new NBTTagCompound();
            stack.writeToNBT(tagCompound);
            compound.setTag("stack", tagCompound);
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        ItemStack read = new ItemStack(compound.getCompoundTag("stack"));
        if (read.isEmpty()) {
            stack = EMPTY_STACK;
        } else {
            stack = read;
        }
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof StatementParameterItemStack) {
            StatementParameterItemStack param = (StatementParameterItemStack) object;

            return ItemStack.areItemStacksEqual(stack, param.stack) && ItemStack.areItemStackTagsEqual(stack, param.stack);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(stack);
    }

    @Override
    public String getDescription() {
        if (stack.isEmpty()) {
            return "";
        } else {
            return stack.getDisplayName();
        }
    }

    @Override
    public String getUniqueTag() {
        return "buildcraft:stack";
    }

    @Override
    public IStatementParameter rotateLeft() {
        return this;
    }

    @Override
    public IStatementParameter[] getPossible(IStatementContainer source, IStatement stmt) {
        return null;
    }
}
