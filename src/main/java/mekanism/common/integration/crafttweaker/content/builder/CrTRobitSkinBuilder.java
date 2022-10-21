package mekanism.common.integration.crafttweaker.content.builder;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import java.util.function.Predicate;
import mekanism.api.robit.RobitSkin;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.integration.crafttweaker.CrTUtils;
import mekanism.common.integration.crafttweaker.content.CrTContentUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister(loaders = CrTConstants.CONTENT_LOADER)
@ZenCodeType.Name(CrTConstants.CLASS_BUILDER_ROBIT_SKIN)
public class CrTRobitSkinBuilder {

    /**
     * Creates a builder for registering a custom {@link RobitSkin}. The textures should be located in the asset location: {@code
     * <namespace>/textures/entity/robit/<path>.png}
     * <br><br>
     * It is <strong>important</strong> that this list has at <strong>least ONE</strong> element in it.
     * <br><br>
     * Every three ticks of the robit being alive if it has moved, the selected texture of this skin is incremented to the next one in the list, and then it repeats from
     * the start. This allows skins to define "movement" changes such as how the Robit's treads appear to be moving in the base skin.
     *
     * @param textures Textures to use for the skin.
     *
     * @return A builder for creating a custom {@link RobitSkin}.
     */
    @ZenCodeType.Method
    public static CrTRobitSkinBuilder builder(ResourceLocation... textures) {
        if (textures.length == 0) {
            throw new IllegalArgumentException("Robit skins require at least one texture!");
        }
        return new CrTRobitSkinBuilder(textures);
    }

    private final ResourceLocation[] textures;
    @Nullable
    private Predicate<Player> unlockedPredicate;
    @Nullable
    private ResourceLocation model;

    private CrTRobitSkinBuilder(ResourceLocation... textures) {
        this.textures = textures;
    }

    /**
     * Sets the location of the custom json model for this skin. In general, it is probably a good idea to base it off the existing robit model's json except with any
     * small changes this skin requires. For an example of the syntax the default model's location would be {@code mekanism:item/robit}.
     *
     * @param model Custom model.
     *
     * @apiNote This is mostly untested currently so if you run into issues please report them.
     */
    @ZenCodeType.Method
    public CrTRobitSkinBuilder customModel(ResourceLocation model) {
        this.model = model;
        return this;
    }

    /**
     * Sets a predicate that can be used to check if a player has access to selecting this skin or not.
     *
     * @param unlockedPredicate Predicate that takes the player to check.
     */
    @ZenCodeType.Method
    public CrTRobitSkinBuilder unlockCheck(Predicate<Player> unlockedPredicate) {
        this.unlockedPredicate = unlockedPredicate;
        return this;
    }

    /**
     * Create a robit skin from this builder with the given name.
     *
     * @param name Registry name for the robit skin.
     */
    @ZenCodeType.Method
    public void build(String name) {
        RobitSkin skin;
        if (unlockedPredicate == null && model == null) {
            //If we have no overrides we can just use the base implementation
            skin = new RobitSkin(textures);
        } else {
            //Otherwise, we need to override the various methods
            skin = new RobitSkin(textures) {
                @Nullable
                @Override
                public ResourceLocation getCustomModel() {
                    return model;
                }

                @Override
                public boolean isUnlocked(@NotNull Player player) {
                    return unlockedPredicate == null ? super.isUnlocked(player) : unlockedPredicate.test(player);
                }
            };
        }
        CrTContentUtils.queueRobitSkinForRegistration(CrTUtils.rl(name), skin);
    }
}