package mekanism.api.recipes.inputs;

import java.util.Collections;
import java.util.List;
import net.minecraft.tags.ITag;

/**
 * @apiNote Only use this from within mekanism
 */
public class TagResolverHelper {

    private TagResolverHelper() {
    }

    public static <TYPE> List<TYPE> getRepresentations(ITag<TYPE> tag) {
        try {
            return tag.getAllElements();
        } catch (IllegalStateException e) {
            //Why do tags have to be such an annoyance in 1.16
            // This is needed so that we can ensure we give JEI an empty list of representations
            // instead of crashing on the first run, as recipes get "initialized" before tags are
            // done initializing, and we don't want to spam the log with errors. JEI and things
            // still work fine regardless of this
            return Collections.emptyList();
        }
    }
}