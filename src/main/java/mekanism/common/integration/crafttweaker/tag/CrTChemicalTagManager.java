package mekanism.common.integration.crafttweaker.tag;

import com.blamejared.crafttweaker.api.CraftTweakerAPI;
import com.blamejared.crafttweaker.api.action.tag.ActionTagAdd;
import com.blamejared.crafttweaker.api.action.tag.ActionTagRemove;
import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.tag.MCTag;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalTags;
import mekanism.api.providers.IChemicalProvider;
import mekanism.common.integration.crafttweaker.CrTConstants;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.Tag;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name(CrTConstants.CLASS_CHEMICAL_TAG_MANAGER)//TODO - 1.18: Hook back up
public abstract class CrTChemicalTagManager<CHEMICAL extends Chemical<CHEMICAL>> {// implements ITagManager<CHEMICAL> {

    private final ChemicalTags<CHEMICAL> chemicalTags;

    protected CrTChemicalTagManager(ChemicalTags<CHEMICAL> chemicalTags) {
        this.chemicalTags = chemicalTags;
    }

    //@Override//TODO - 1.18: Hook back up
    public void addElements(MCTag<CHEMICAL> to, List<CHEMICAL> toAdd) {
        Tag<CHEMICAL> internal = getInternal(to);
        List<CHEMICAL> itemsFromDefinitions = getChemicals(toAdd);
        if (internal == null) {
            //TODO - 1.18: Hook back up
            //SetTag<CHEMICAL> tagFromContents = SetTag.create(Sets.newHashSet(itemsFromDefinitions));
            //CraftTweakerAPI.apply(new ActionTagCreate<>(getTagCollection(), tagFromContents, to));
        } else {
            CraftTweakerAPI.apply(new ActionTagAdd<>(internal, itemsFromDefinitions, to));
        }
    }

    //@Override//TODO - 1.18: Hook back up
    public void removeElements(MCTag<CHEMICAL> from, List<CHEMICAL> toRemove) {
        Tag<CHEMICAL> internal = getInternal(from);
        List<CHEMICAL> chemicals = getChemicals(toRemove);
        CraftTweakerAPI.apply(new ActionTagRemove<>(internal, chemicals, from));
    }

    private List<CHEMICAL> getChemicals(List<CHEMICAL> toConvert) {
        return toConvert.stream().map(IChemicalProvider::getChemical).toList();
    }

    //@Override//TODO - 1.18: Hook back up
    public List<CHEMICAL> getElementsInTag(MCTag<CHEMICAL> theTag) {
        Tag<CHEMICAL> internal = getInternal(theTag);
        if (internal == null) {
            return Collections.emptyList();
        }
        return internal.getValues();
    }

    //TODO - 1.18: Hook back up
    /*@Override
    public TagCollection<CHEMICAL> getTagCollection() {
        return chemicalTags.getCollection();
    }*/

    @Nullable
    //@Override//TODO - 1.18: Hook back up/adjust as expected
    public Tag<CHEMICAL> getInternal(MCTag<CHEMICAL> theTag) {
        //TODO - 1.18: Hook back up after it changes
        //return getTagCollection().getTag(theTag.id());
        throw new UnsupportedOperationException("Not updated to 1.18.2 yet");
    }

    //TODO - 1.18: Remove the below it is copied from CrT's ITagManager to reduce errors
    @Nonnull
    public abstract Class<CHEMICAL> getElementClass();

    public MCTag<CHEMICAL> getTag(String name) {
        return this.getTag(new ResourceLocation(name));
    }

    public MCTag<CHEMICAL> getTag(ResourceLocation location) {
        throw new UnsupportedOperationException("Not updated to 1.18.2 yet");
    }

    public abstract String getTagFolder();
}