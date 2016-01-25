package buildcraft.api;

import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;

/* Note that neither this class (Or any of its subtypes) overrides equals or hash code, despite it being used as a key
 * in a hash table. This is to speed up comparisons as no two ObjectDefinitions instances should be created with the
 * same values. */
public class ObjectDefinition {
    /** A globally unique tag for the pipe */
    public final String globalUniqueTag;
    /** A mod unique tag for the pipe. WARNING: this should only be used to register other mod-unique things, such as
     * the pipe or gate item. For general use, use {@link #globalUniqueTag} */
    public final String modUniqueTag;

    protected static String getCurrentMod() {
        ModContainer container = Loader.instance().activeModContainer();
        if (container == null) {
            throw new IllegalStateException("Pipes MUST be registered inside a mod");
        }
        return container.getModId();
    }

    private ObjectDefinition(String mod, String modUniqueTag) {
        this.globalUniqueTag = mod + ":" + modUniqueTag;
        this.modUniqueTag = modUniqueTag;
    }

    public ObjectDefinition(String tag) {
        this(getCurrentMod(), tag);
    }
}
