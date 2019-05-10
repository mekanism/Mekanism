package mekanism.common.fixers;

import javax.annotation.Nonnull;
import mekanism.common.util.LangUtils;
import net.minecraft.nbt.NBTTagCompound;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Attempt to remap old localised names to lang key based names
 */
@SuppressWarnings("unused")//from Coremod via reflection
public class KeybindingFixer {

    private static final Logger LOGGER = LogManager.getLogger("Mekanism KeybindingFixer");

    public static void runFix(NBTTagCompound compound) {
        remapKey(compound, "key_Mekanism Item Mode Switch", "mekanism.key.mode");
        remapKey(compound, "key_Mekanism Armor Mode Switch", "mekanism.key.armorMode");
        remapKey(compound, "key_Mekanism Feet Mode Switch", "mekanism.key.feetMode");
        remapKey(compound, "key_Mekanism Voice", "mekanism.key.voice");
    }

    private static void remapKey(@Nonnull NBTTagCompound compound, String oldKey, String langKey) {
        String newKey = "key_" + langKey;
        if (compound.hasKey(oldKey)) {
            LOGGER.info("Remapping {} to {}", oldKey, langKey);
            compound.setString(newKey, compound.getString(oldKey));
            compound.removeTag(oldKey);
        }
        //Not sure if translations loaded yet, but try anyway
        try {
            String translated = "key_Mekanism " + LangUtils.localize(langKey);
            if (compound.hasKey(translated)) {
                LOGGER.info("Remapping {} to {}", translated, langKey);
                compound.setString(newKey, compound.getString(translated));
                compound.removeTag(translated);
            }
        } catch (Exception e) {
            LOGGER.error("Remap error", e);
        }
    }
}
