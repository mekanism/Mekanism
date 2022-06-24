package mekanism.common.integration.crafttweaker.chemical;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.tag.type.KnownTag;
import com.blamejared.crafttweaker_annotations.annotations.NativeTypeRegistration;
import com.blamejared.crafttweaker_annotations.annotations.TaggableElement;
import java.util.List;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.integration.crafttweaker.CrTUtils;
import mekanism.common.integration.crafttweaker.chemical.CrTChemicalStack.CrTSlurryStack;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack.ICrTSlurryStack;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@TaggableElement("mekanism:slurry")
@NativeTypeRegistration(value = Slurry.class, zenCodeName = CrTConstants.CLASS_SLURRY)
public class CrTSlurry {

    private CrTSlurry() {
    }

    /**
     * Creates a new {@link ICrTSlurryStack} with the given amount of slurry.
     *
     * @param amount The size of the stack to create.
     *
     * @return a new (immutable) {@link ICrTSlurryStack}
     */
    @ZenCodeType.Method
    @ZenCodeType.Operator(ZenCodeType.OperatorType.MUL)
    public static ICrTSlurryStack makeStack(Slurry _this, long amount) {
        return new CrTSlurryStack(_this.getStack(amount));
    }

    /**
     * Gets the tags that this slurry is a part of.
     *
     * @return All the tags this slurry is a part of.
     */
    @ZenCodeType.Method
    @ZenCodeType.Getter("tags")
    public static List<KnownTag<Slurry>> getTags(Slurry _this) {
        return CrTUtils.slurryTags().getTagsFor(_this);
    }

    /**
     * Gets the item tag representing the ore for this slurry.
     *
     * @return The tag for the item the slurry goes with. May be null.
     */
    @ZenCodeType.Method
    @ZenCodeType.Nullable
    public static KnownTag<Item> getOreTag(Slurry _this) {
        TagKey<Item> oreTag = _this.getOreTag();
        if (oreTag == null) {
            return null;
        }
        return CrTUtils.itemTags().tag(oreTag.location());
    }
}