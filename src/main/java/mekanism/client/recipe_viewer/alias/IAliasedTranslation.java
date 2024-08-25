package mekanism.client.recipe_viewer.alias;

import mekanism.api.text.IHasTranslationKey;

public interface IAliasedTranslation extends IHasTranslationKey {

    String getAlias();
}