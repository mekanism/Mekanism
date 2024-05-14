package mekanism.client.integration;

import mekanism.api.text.IHasTranslationKey;

public interface IAliasedTranslation extends IHasTranslationKey {

    String getAlias();
}