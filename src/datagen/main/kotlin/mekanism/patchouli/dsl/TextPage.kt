package mekanism.patchouli.dsl

import com.google.gson.JsonObject
import net.minecraft.resources.ResourceLocation

open class TextPage: EntryPage(ResourceLocation("patchouli", "text")){
    /** Mandatory. The text to display on this page. This text can be formatted. */
    /** Mandatory. The text to display on this page. This text can be formatted. */
    lateinit var text :String

    /** An optional title to display at the top of the page. If you set this, the rest of the text will be shifted down a bit. You can't use "title" in the first page of an entry. */
    /** An optional title to display at the top of the page. If you set this, the rest of the text will be shifted down a bit. You can't use "title" in the first page of an entry. */
    var title :String? = null

    override fun toJson(): JsonObject {
        return super.toJson().also { json->
            json.addProperty("text", text)
            title?.let {  json.addProperty("title", it) }
        }
    }
}