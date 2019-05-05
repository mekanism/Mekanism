package mekanism.common.fixers;

import javax.annotation.Nonnull;
import mekanism.common.fixers.MekanismDataFixers.MekFixers;
import mekanism.common.util.LangUtils;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.datafix.IFixableData;

/**
 * Attempt top remap old localised names to lang key based names
 */
public class KeybindingFixer implements IFixableData {

    @Override
    public int getFixVersion() {
        return MekFixers.KEYBINDINGS.getFixVersion();
    }

    @Nonnull
    @Override
    public NBTTagCompound fixTagCompound(@Nonnull NBTTagCompound compound) {
        remapKey(compound, "key_Mekanism Item Mode Switch", "mekanism.key.mode");
        remapKey(compound, "key_Mekanism Armor Mode Switch", "mekanism.key.armorMode");
        remapKey(compound, "key_Mekanism Feet Mode Switch", "mekanism.key.feetMode");
        remapKey(compound, "key_Mekanism Voice", "mekanism.key.voice");
        return compound;
    }

    private void remapKey(@Nonnull NBTTagCompound compound, String oldKey, String langKey) {
        if (compound.hasKey(oldKey)) {
            compound.setInteger(langKey, compound.getInteger(oldKey));
            compound.removeTag(oldKey);
        }
        //Not sure if translations loaded yet, but try anyway
        String translated = "key_Mekanism "+LangUtils.localize(langKey);
        if (compound.hasKey(translated)) {
            compound.setInteger(langKey, compound.getInteger(translated));
            compound.removeTag(translated);
        }
    }
}
