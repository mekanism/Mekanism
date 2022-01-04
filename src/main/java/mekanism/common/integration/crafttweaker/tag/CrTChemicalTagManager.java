package mekanism.common.integration.crafttweaker.tag;

import com.blamejared.crafttweaker.api.CraftTweakerAPI;
import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker.impl.actions.tags.ActionTagAdd;
import com.blamejared.crafttweaker.impl.actions.tags.ActionTagCreate;
import com.blamejared.crafttweaker.impl.actions.tags.ActionTagRemove;
import com.blamejared.crafttweaker.impl.tag.MCTag;
import com.blamejared.crafttweaker.impl.tag.manager.TagManager;
import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalTags;
import mekanism.api.providers.IChemicalProvider;
import mekanism.common.integration.crafttweaker.CrTConstants;
import net.minecraft.tags.Tag;
import net.minecraft.tags.TagCollection;
import net.minecraft.tags.SetTag;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name(CrTConstants.CLASS_CHEMICAL_TAG_MANAGER)
public abstract class CrTChemicalTagManager<CHEMICAL extends Chemical<CHEMICAL>> implements TagManager<CHEMICAL> {

    private final ChemicalTags<CHEMICAL> chemicalTags;

    protected CrTChemicalTagManager(ChemicalTags<CHEMICAL> chemicalTags) {
        this.chemicalTags = chemicalTags;
    }

    @Override
    public void addElements(MCTag<CHEMICAL> to, List<CHEMICAL> toAdd) {
        Tag<CHEMICAL> internal = getInternal(to);
        List<CHEMICAL> itemsFromDefinitions = getChemicals(toAdd);
        if (internal == null) {
            SetTag<CHEMICAL> tagFromContents = SetTag.create(Sets.newHashSet(itemsFromDefinitions));
            CraftTweakerAPI.apply(new ActionTagCreate<>(getTagCollection(), tagFromContents, to));
        } else {
            CraftTweakerAPI.apply(new ActionTagAdd<>(internal, itemsFromDefinitions, to));
        }
    }

    @Override
    public void removeElements(MCTag<CHEMICAL> from, List<CHEMICAL> toRemove) {
        Tag<CHEMICAL> internal = getInternal(from);
        List<CHEMICAL> chemicals = getChemicals(toRemove);
        CraftTweakerAPI.apply(new ActionTagRemove<>(internal, chemicals, from));
    }

    private List<CHEMICAL> getChemicals(List<CHEMICAL> toConvert) {
        return toConvert.stream().map(IChemicalProvider::getChemical).collect(Collectors.toList());
    }

    @Override
    public List<CHEMICAL> getElementsInTag(MCTag<CHEMICAL> theTag) {
        Tag<CHEMICAL> internal = getInternal(theTag);
        if (internal == null) {
            return Collections.emptyList();
        }
        return internal.getValues();
    }

    @Override
    public TagCollection<CHEMICAL> getTagCollection() {
        return chemicalTags.getCollection();
    }

    @Nullable
    @Override
    public Tag<CHEMICAL> getInternal(MCTag<CHEMICAL> theTag) {
        return getTagCollection().getTag(theTag.getIdInternal());
    }
}