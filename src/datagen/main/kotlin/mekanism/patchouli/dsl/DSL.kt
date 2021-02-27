@file:Suppress("MemberVisibilityCanBePrivate", "unused")//its an api

package mekanism.patchouli.dsl

import com.google.common.collect.BiMap
import com.google.common.collect.HashBiMap
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName
import mekanism.api.providers.IBlockProvider
import mekanism.api.providers.IItemProvider
import mekanism.common.block.attribute.Attribute
import mekanism.common.block.attribute.AttributeStateFacing
import mekanism.common.registration.impl.BlockRegistryObject
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.client.settings.KeyBinding
import net.minecraft.item.Item
import net.minecraft.item.ItemGroup
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundNBT
import net.minecraft.state.StateHolder
import net.minecraft.tags.ITag
import net.minecraft.util.Direction
import net.minecraft.util.ResourceLocation
import org.apache.logging.log4j.LogManager
import java.util.*

fun JsonObject.addProperty(name: String, res: ResourceLocation) {
    addProperty(name, res.toString())
}

fun JsonObject.addProperty(name: String, item: ItemStack) {
    addProperty(name, ItemStackUtils.serializeStack(item))
}

fun jsonObject(receiver: JsonObject.() -> Unit): JsonObject {
    return JsonObject().also(receiver)
}

fun jsonArray(receiver: JsonArray.() -> Unit): JsonArray {
    return JsonArray().apply(receiver)
}

fun Array<String>.toJsonArray():JsonArray = JsonArray().also { array->
    this.forEach {
        array.add(it)
    }
}

fun Array<Number>.toJsonArray():JsonArray = JsonArray().also { array->
    this.forEach {
        array.add(it)
    }
}

val IItemProvider.bookId: String get() {
    val type = if (this is IBlockProvider) "block" else "item"
    return type + "/" + this.registryName.path
}

fun link(item: IItemProvider, text: String): String {
    return "$(l:${item.bookId})${text}$(/l)"
}

fun link(guideEntry: IGuideEntry, text: String): String {
    return "$(l:${guideEntry.entryId})${text}$(/l)"
}

operator fun KeyBinding.invoke(): String {
    return "$(k:${translationKey})"
}

val LOGGER = LogManager.getLogger("PatchouliDSL")!!

interface IGuideCategory {
    val id: String
    val translationKeyName: String
    val translationKeyDescription: String
}

interface IGuideEntry {
    val entryId: String

    companion object {
        fun generate(folder: String?, name: String): String = (if (folder != null) "$folder/" else "") + name.toLowerCase(Locale.ROOT)
    }
}

@DslMarker
annotation class PatchouliDSL

@PatchouliDSL
class PatchouliBook(val id: ResourceLocation) {
    /** Mandatory. The name of the book that will be displayed in the book item and the GUI. For modders, this can be a localization key. */
    lateinit var name: String

    /** Mandatory.The text that will be displayed in the landing page of your book. This text can be formatted. For modders, this can be a localization key.*/
    @SerializedName("landing_text")
    lateinit var landingText: String

    /**
     * The texture for the background of the book GUI. You can use any resource location for this, but it is highly recommended
     * you use one of the built in ones so that if new elements get added, you have them right away.
     */
    @SerializedName("book_texture")
    var bookTexture: ResourceLocation? = null

    /**
     * The texture for the page filler (the cube thing that shows up on entries with an odd number of pages). Define if you want something else than the cube to fill your empty pages.
     */
    @SerializedName("filler_texture")
    var fillerTexture: ResourceLocation? = null

    /**
     * The texture for the crafting entry elements. Define if you want custom backdrops for these. Not really worth defining in most cases but if you want to be cool and stylish, you can.
     */
    @SerializedName("crafting_texture")
    var craftingTexture: ResourceLocation? = null

    /**
     * The model for the book's item. This can be any standard item model you define. Patchouli provides a few you can use:
     *
     * patchouli:book_blue
     * patchouli:book_brown (default value)
     * patchouli:book_cyan
     * patchouli:book_gray
     * patchouli:book_green
     * patchouli:book_purple
     * patchouli:book_red
     *
     * FOR MODDERS: Do NOT use any of the above models.
     * In the sake of books being distinguishable, having multiple mods using the same base textures is a bad idea, make your own!
     * They're provided for modpack makers.
     *
     * Patchouli automatically takes care of registering any models you pass in to this, so you don't have to mess with any code for them yourself.
     *
     * A property called "completion" is provided for book models, where its value is equal to the fraction of entries unlocked in the book,
     * which allows book models to change their display as they're more completed.
     */
    @SerializedName("model")
    var model: ResourceLocation? = null

    /**
     * The color of regular text, in hex ("RRGGBB", # not necessary). Defaults to "000000".
     */
    @SerializedName("text_color")
    var textColor: String? = null

    /**
     * The color of header text, in hex ("RRGGBB", # not necessary). Defaults to "333333".
     */
    @SerializedName("header_color")
    var headerColor: String? = null

    /**
     * The color of the book nameplate in the landing page, in hex ("RRGGBB", # not necessary). Defaults to "FFDD00".
     */
    @SerializedName("nameplate_color")
    var nameplateColor: String? = null

    /**
     * The color of link text, in hex ("RRGGBB", # not necessary). Defaults to "0000EE".
     */
    @SerializedName("link_color")
    var linkColor: String? = null

    /**
     * The color of hovered link text, in hex ("RRGGBB", # not necessary). Defaults to "8800EE".
     */
    @SerializedName("link_hover_color")
    var linkHoverColor: String? = null

    /**
     * The color of advancement progress bar, in hex ("RRGGBB", # not necessary). Defaults to "FFFF55".
     */
    @SerializedName("progress_bar_color")
    var progressBarColor: String? = null

    /**
     * The color of advancement progress bar's background, in hex ("RRGGBB", # not necessary). Defaults to "DDDDDD".
     */
    @SerializedName("progress_bar_background")
    var progressBarBackground: String? = null

    /**
     * The sound effect played when opening this book. This is a resource location pointing to the sound (which, for mooders, needs to be properly registered).
     */
    @SerializedName("open_sound")
    var openSound: ResourceLocation? = null

    /**
     * The sound effect played when flipping through pages in this book. This is a resource location pointing to the sound (which, for mooders, needs to be properly registered).
     */
    @SerializedName("flip_sound")
    var flipSound: ResourceLocation? = null

    /**
     * The icon to display for the Book Index. This can either be an ItemStack String, if you want an item to be the icon, or a resource location pointing to a square texture. If you want to use a resource location, make sure to end it with .png. This is optional, and if you don't include it, it'll default to the book's icon (which is the recommended value).
     */
    @SerializedName("index_icon")
    var indexIcon: ResourceLocation? = null

    /**
     * Defaults to true. Set to false to disable the advancement progress bar, even if advancements are enabled.
     */
    @SerializedName("show_progress")
    var showProgress: Boolean? = null

    /**
     * The "edition" of the book. This defaults to "0", and setting this to any other numerical value (let's call it X)
     * will display "X Edition" in the book's tooltip and landing page (e.g. X being 3 would display "3rd Edition").
     * Setting this to "0" or not modifying it will instead display whatever you set the "subtitle" key as.
     * If the value is non-numerical, it'll display "Writer's Edition".
     *
     * For modders. This is a good place you can inflate with gradle.
     * You can use something like ${book_version} here and set it your build script. As non-numerical values are accepted, this won't cause any issues.
     */
    var version: String? = null

    /**
     * A subtitle for your book, which will display in the tooltip and below the book name in the landing page if "version" is set to "0" or not set.
     */
    var subtitle: String? = null

    /**
     * The creative tab to display your book in. This defaults to Miscellaneous, but you can move it to any tab you wish.
     *
     * Here are the names for the vanilla tabs:
     *      buildingBlocks
     *      decorations
     *      redstone
     *      transportation
     *      misc (default value)
     *      food
     *      tools
     *      combat
     *      brewing
     *
     * For modders, simply put in the same string you use when constructing your creative tab here, and the book will show up there.
     */
    @SerializedName("creative_tab")
    var creativeTab: ItemGroup? = null

    /**
     * The name of the advancements tab you want this book to be associated to. If defined, an Advancements button will show up in the landing page that will open that tab.
     */
    @SerializedName("advancements_tab")
    var advancementsTab: ResourceLocation? = null

    /**
     * Defaults to false. Set this to true if you don't want Patchouli to make a book item for your book. Use only if you're a modder and you really need a custom Item class for whatever reason.
     */
    @SerializedName("dont_generate_book")
    var dontGenerateBook: Boolean? = null

    /**
     * Following from the previous key, if you do have a custom book, set it here. This is an ItemStack String.
     */
    @SerializedName("custom_book_item")
    var customBookItem: ItemStack? = null

    /**
     * Defaults to true. Set it to false if you don't want your book to show toast notifications when new entries are available.
     */
    @SerializedName("show_toasts")
    var showToasts: Boolean? = null

    /**
     * Defaults to false. Set it to true to use the vanilla blocky font rather than the slim font. If you have a font mod, it'll use whichever font that mod is providing instead.
     */
    @SerializedName("use_blocky_font")
    var useBlockyFont: Boolean? = null

    /**
     * Default false. If set to true, attempts to look up category, entry, and page titles as well as any page text in the lang files before rendering.
     */
    var i18n: Boolean? = null

    /**
     * Default true. When set true, opening any GUI from this book will pause the game in singleplayer.
     */
    @SerializedName("pause_game")
    var pauseGame: Boolean? = null

    /**
     * Define this to set this book as an extension of another. Extension books do not create a book item and don't really "exist".
     * All they serve to do is to add more content to another book that already exists.
     * This is mainly here for addon mods that want to add stuff to the book they're extending.
     *
     * The value you put here is simply the ID of the book you want to extend.
     * In the form of "modid:path", where modid is the ID of the mod that owns the book, and path the folder where it is.
     * For example, should you want to extend the book owned by "patchouli" that's in "/data/patchouli/patchouli_books/coolbook/book.json",
     * the value you'd put here would be "patchouli:coolbook".
     *
     * If this value is set, every single other value in the file is ignored.
     * This book will inherit any entries, categories, templates, and macros from the original one, so feel free to use them at will.
     */
    var extend: ResourceLocation? = null

    fun extend(modId: String, bookId: String) {
        this.extend = ResourceLocation(modId, bookId)
    }

    /**
     * Defaults to true. Set it to false if you want to not play nice and lock your book from being extended by other books.
     */
    @SerializedName("allow_extensions")
    var allowExtensions: Boolean? = null

    val macros: MutableMap<String, String> = mutableMapOf()
    
    var locale: String = "en_us"

    fun toJson(): JsonObject {
        val json = JsonObject()
        if (extend != null) {
            json.addProperty("extend", extend!!)
        } else {
            json.addProperty("name", name)
            json.addProperty("landing_text", landingText)
            bookTexture?.let { json.addProperty("book_texture", it) }
            fillerTexture?.let { json.addProperty("filler_texture", it) }
            craftingTexture?.let { json.addProperty("crafting_texture", it) }
            model?.let { json.addProperty("model", it) }
            textColor?.let { json.addProperty("text_color", it) }
            headerColor?.let { json.addProperty("header_color", it) }
            nameplateColor?.let { json.addProperty("nameplate_color", it) }
            linkColor?.let { json.addProperty("link_color", it) }
            linkHoverColor?.let { json.addProperty("link_hover_color", it) }
            progressBarColor?.let { json.addProperty("progress_bar_color", it) }
            progressBarBackground?.let { json.addProperty("progress_bar_background", it) }
            openSound?.let { json.addProperty("open_sound", it) }
            flipSound?.let { json.addProperty("flip_sound", it) }
            indexIcon?.let { json.addProperty("index_icon", it) }
            showProgress?.let { json.addProperty("show_progress", it) }
            version?.let { json.addProperty("version", it) }
            subtitle?.let { json.addProperty("subtitle", it) }
            creativeTab?.let { json.addProperty("creative_tab", it.path) }
            advancementsTab?.let { json.addProperty("advancements_tab", it) }
            dontGenerateBook?.let { json.addProperty("dont_generate_book", it) }
            customBookItem?.let { json.addProperty("custom_book_item", it) }
            showToasts?.let { json.addProperty("show_toasts", it) }
            useBlockyFont?.let { json.addProperty("use_blocky_font", it) }
            i18n?.let { json.addProperty("i18n", it) }
            pauseGame?.let { json.addProperty("pause_game", it) }
            allowExtensions?.let { json.addProperty("allow_extensions", it) }
        }
        if (macros.isNotEmpty()) {
            json.add("macros", jsonObject {
                macros.forEach { (key, value) -> addProperty(key, value) }
            })
        }
        return json
    }

    internal val categories: MutableList<Category> = mutableListOf()
    internal val entries: MutableList<Entry> = mutableListOf()

    @PatchouliDSL
    fun category(id: String, init: Category.() -> Unit): Category {
        return Category(this, id).also {
            categories.add(it)
            it.init()
        }
    }
    @PatchouliDSL
    fun category(guideCat: IGuideCategory, init: Category.() -> Unit) {
        category(guideCat.id, init)
    }

    @PatchouliDSL
    operator fun IGuideCategory.invoke(init: Category.() -> Unit) {
        category(this, init)
    }

    @PatchouliDSL
    fun existingCategory(guideCat: IGuideCategory, init: DelegateCategory.() -> Unit): ICategory {
        return DelegateCategory(this, guideCat.id).also {
            it.init()
        }
    }

    companion object {
        val BOOK_BLUE = ResourceLocation("patchouli", "textures/gui/book_blue.png")
        val BOOK_BROWN = ResourceLocation("patchouli", "textures/gui/book_brown.png")// (default value)
        val BOOK_CYAN = ResourceLocation("patchouli", "textures/gui/book_cyan.png")
        val BOOK_GRAY = ResourceLocation("patchouli", "textures/gui/book_gray.png")
        val BOOK_GREEN = ResourceLocation("patchouli", "textures/gui/book_green.png")
        val BOOK_PURPLE = ResourceLocation("patchouli", "textures/gui/book_purple.png")
        val BOOK_RED = ResourceLocation("patchouli", "textures/gui/book_red.png")
    }
}

@PatchouliDSL
fun patchouliBook(modId: String, bookId: String, init: PatchouliBook.() -> Unit): PatchouliBook {
    return PatchouliBook(ResourceLocation(modId, bookId)).also(init)
}

interface ICategory {
    val id: String
    val book: PatchouliBook

    @PatchouliDSL
    fun category(id: String, init: Category.() -> Unit): Category {
        val theParent = this
        return this.book.category(id) {
            parent = theParent
            init()
        }
    }

    @PatchouliDSL
    fun category(guideCat: IGuideCategory, init: Category.() -> Unit) {
        category(guideCat.id, init)
    }

    @PatchouliDSL
    operator fun IGuideCategory.invoke(init: Category.() -> Unit) {
        category(this, init)
    }

    @PatchouliDSL
    fun entry(id: String, init: Entry.() -> Unit): Entry {
        return Entry(id, this).also {
            this.book.entries.add(it)
            it.init()
        }
    }

    @PatchouliDSL
    operator fun IItemProvider.invoke(spotlightText: String? = null, init: (Entry.() -> Unit)? = null) {
        entry(this.bookId) {
            name = translationKey
            icon = this@invoke
            readByDefault = true//no one wants to have to go through EVERY item in the index...
            spotlight(this@invoke, spotlightText)
            init?.invoke(this)
        }
    }

    @PatchouliDSL
    operator fun IGuideEntry.invoke(init: Entry.() -> Unit) {
        entry(this.entryId) {
            init()
        }
    }
}

@PatchouliDSL
class Category(override val book: PatchouliBook, override val id: String): ICategory {

    /** mandatory. The name of this category. */
    lateinit var name: String

    /** mandatory. The description for this category. This displays in the category's main page, and can be formatted. */
    lateinit var description: String

    /** mandatory. The icon for this category. This can either be an ItemStack String, if you want an item to be the icon, or a resource location pointing to a square texture. If you want to use a resource location, make sure to end it with .png */
    @SerializedName("icon")
    lateinit var iconStr: String

    /**
     * not directly written to json. Sets the iconStr from the item provider
     */
    var icon: IItemProvider
        get() {
            throw UnsupportedOperationException()
        }
        set(value) {
            iconStr = ItemStackUtils.serializeStack(value.itemStack)
        }

    /** The parent category to this one. If this is a sub-category, simply put the name of the category this is a child to here. If not, don't define it. Use fully-qualified names including both a namespace and a path. */
    var parent: ICategory? = null

    /** A config flag expression that determines whether this category should exist or not. See Using Config Flags for more info on config flags. */
    var flag: String? = null


    /** The sorting number for this category. Defaults to 0. Categories are sorted in the main page from lowest sorting number to highest, so if you define this in every category you make, you can set what order they display in. */
    var sortNum: Int? = null


    /** Defaults to false. Set this to true to make this category a secret category. Secret categories don't display a locked icon when locked, and instead will not display at all until unlocked. */
    var secret: Boolean? = null

    fun toJson(): JsonObject {
        val json = JsonObject()
        json.addProperty("name", name)
        json.addProperty("description", description)
        json.addProperty("icon", iconStr)
        parent?.let {  json.addProperty("parent", it.id) }
        flag?.let {  json.addProperty("flag", it) }
        sortNum?.let {  json.addProperty("sortnum", it) }
        secret?.let {  json.addProperty("secret", it) }
        return json
    }
}

/** Category that doesn't make its own JSON file */
@PatchouliDSL
class DelegateCategory(override val book: PatchouliBook, override val id: String): ICategory

@PatchouliDSL
class Entry(
        val id: String,
        /** mandatory. The category this entry belongs to. This must be set to one of your categories' ID. For best results, use a fully-qualified ID that includes your book namespace yourbooknamespace:categoryname. In the future this will be enforced. */
        val category: ICategory
) {
    /** mandatory. The name of this entry. */
    lateinit var name: String

    /** mandatory. The icon for this entry. This can either be an ItemStack String, if you want an item to be the icon, or a resource location pointing to a square texture. If you want to use a resource location, make sure to end it with .png */
    @SerializedName("icon")
    lateinit var iconStr: String

    @PatchouliDSL
    fun icon(stack: ItemStack){
        iconStr = ItemStackUtils.serializeStack(stack)
    }

    /**
     * Not directly written to json. Sets the iconStr from the item provider
     * Gettable so we can see which items have an entry and which don't
     */
    var icon: IItemProvider? = null
        set(value) {
            field = value!!
            iconStr = ItemStackUtils.serializeStack(value.itemStack)
        }

    /** mandatory. The array of pages for this entry. */
    var pages: MutableList<EntryPage> = mutableListOf()

    /** The name of the advancement you want this entry to be locked behind. See Locking Content with Advancements for more info on locking content. */
    var advancement: String? = null

    /** A config flag expression that determines whether this entry should exist or not. See Using Config Flags for more info on config flags. */
    var flag: String? = null

    /** Defaults to false. If set to true, the entry will show up with an italicized name, and will always show up at the top of the category. Use for really important entries you want to show up at the top. */
    var priority: Boolean? = null

    /** Defaults to false. Set this to true to make this entry a secret entry. Secret entries don't display as "Locked" when locked, and instead will not display at all until unlocked. Secret entries do not count for % completion of the book, and when unlocked will instead show as an additional line in the tooltip. */
    var secret: Boolean? = null

    /** Defaults to false. Set this to true if you want to entry to not show the unread ("(!!)") indicator if it hasn't been opened yet. */
    @SerializedName("read_by_default")
    var readByDefault: Boolean? = null

    /**
     * The sorting number for this entry. Defaults to 0. Entries with the same sorting number are sorted alphabetically,
     * whereas entries with different sorting numbers are sorted from lowest to highest. Priority entries always show up first.
     * It's recommended you do NOT use this, as breaking the alphabetical sorting order can make things confusing, but it's left as an option.
     */
    var sortnum: Int? = null

    /** The ID of an advancement the player needs to do to "complete" this entry. The entry will show up at the top of the list with a (?) icon next to it until this advancement is complete. This can be used as a quest system or simply to help guide players along a starting path. */
    var turnin: String? = null

    /**
     * Additional list of items this page teaches the crafting process for,
     * for use with the in-world right click and quick lookup feature.
     * Keys are ItemStack strings, values are 0-indexed page numbers.
     */
    @SerializedName("extra_recipe_mappings")
    var extraRecipeMappings: MutableMap<String, Int> = mutableMapOf()

    @PatchouliDSL
    fun extraMapping(item: ItemStack, page: Int) {
        extraRecipeMappings[ItemStackUtils.serializeStack(item)] = page
    }

    @PatchouliDSL
    fun text(init: TextPage.() -> Unit) {
        this.pages.add(TextPage().also(init))
    }

    @PatchouliDSL
    fun text(title: String, init: TextPage.() -> Unit) {
        this.pages.add(TextPage().also {
            it.title = title
            it.init()
        })
    }

    @PatchouliDSL
    operator fun String.unaryPlus(){
        text { text = this@unaryPlus }
    }

    @PatchouliDSL
    fun image(init: ImagePage.() -> Unit) {
        this.pages.add(ImagePage().also(init))
    }

    @PatchouliDSL
    fun crafting(recipe: ResourceLocation, init: CraftingPage.() -> Unit) {
        this.pages.add(CraftingPage(recipe).also(init))
    }

    @PatchouliDSL
    fun smelting(recipe: ResourceLocation, init: SmeltingPage.() -> Unit) {
        this.pages.add(SmeltingPage(recipe).also(init))
    }

    @PatchouliDSL
    fun multiblock(init: MultiblockPage.() -> Unit) {
        this.pages.add(MultiblockPage().also(init))
    }

    @PatchouliDSL
    fun entity(init: EntityPage.() -> Unit) {
        this.pages.add(EntityPage().also(init))
    }

    @PatchouliDSL
    fun spotlight(init: SpotlightPage.() -> Unit) {
        this.pages.add(SpotlightPage().also(init))
    }

    @PatchouliDSL
    fun spotlight(item: IItemProvider, spotlightText: String? = null) {
        spotlight {
            this.item = item.itemStack
            linkRecipe = true
            text = spotlightText
        }
    }

    @PatchouliDSL
    fun link(init: LinkPage.() -> Unit) {
        this.pages.add(LinkPage().also(init))
    }

    @PatchouliDSL
    fun relations(init: RelationsPage.() -> Unit) {
        this.pages.add(RelationsPage().also(init))
    }

    @PatchouliDSL
    fun quest(init: QuestPage.() -> Unit) {
        this.pages.add(QuestPage().also(init))
    }

    @PatchouliDSL
    fun empty(init: EmptyPage.() -> Unit) {
        this.pages.add(EmptyPage().also(init))
    }

    fun empty() {
        this.pages.add(EmptyPage())
    }

    fun toJson(): JsonObject {
        val json = JsonObject()
        json.addProperty("name", name)
        json.addProperty("category", category.id)
        if (!this::iconStr.isInitialized){
            throw RuntimeException("$id doesnt have an icon set")
        }
        json.addProperty("icon", iconStr)
        val pages = JsonArray()
        for (page in this.pages) {
            pages.add(page.toJson())
        }
        json.add("pages", pages)
        advancement?.let {  json.addProperty("advancement", it) }
        flag?.let {  json.addProperty("flag", it) }
        priority?.let {  json.addProperty("priority", it) }
        secret?.let {  json.addProperty("secret", it) }
        readByDefault?.let {  json.addProperty("read_by_default", it) }
        sortnum?.let {  json.addProperty("sortnum", it) }
        turnin?.let {  json.addProperty("turnin", it) }
        if (extraRecipeMappings.isNotEmpty()) {
            val mappings = JsonObject()
            for ((key, value) in extraRecipeMappings) {
                mappings.addProperty(key, value)
            }
            json.add("extra_recipe_mappings", mappings)
        }
        return json
    }
}

@PatchouliDSL
abstract class EntryPage(val type: String) {
    /** A resource location to point at, to make a page appear when that advancement is completed. See Locking Content with Advancements for more info on locking content. Excluding this attribute or leaving it empty will leave the page unlocked from the start. Providing a nonexistent advancement will permanently lock this entry unless the advancement at the resource location starts existing. */
    var advancement: String? = null

    /** A config flag expression that determines whether this page should exist or not. See Using Config Flags for more info on config flags. */
    var flag: String? = null

    /** An anchor can be used elsewhere to refer to this specific page in an internal link. See Text Formatting 101 for more details about internal links. */
    var anchor: String? = null

    open fun toJson(): JsonObject {
        val json = JsonObject()
        json.addProperty("type", type)
        json.addProperty("advancement", advancement)
        json.addProperty("flag", flag)
        json.addProperty("anchor", anchor)
        return json
    }
}

open class TextPage: EntryPage("text"){
    /** Mandatory. The text to display on this page. This text can be formatted. */
    lateinit var text :String

    /** An optional title to display at the top of the page. If you set this, the rest of the text will be shifted down a bit. You can't use "title" in the first page of an entry. */
    var title :String? = null

    override fun toJson(): JsonObject {
        return super.toJson().also { json->
            json.addProperty("text", text)
            title?.let {  json.addProperty("title", it) }
        }
    }
}

class ImagePage: EntryPage("image") {
    /**
     * An array with images to display. Images should be in resource location format.
     * For example, the value botania:textures/gui/entries/banners.png will point to /assets/botania/textures/gui/entries/banners.png
     * in the resource pack.
     * Images used here should ideally be dimensioned as 256x256, and use only a 200x200 canvas centered in the top-left corner for contents,
     * which are rendered at a 0.5x scale compared to the rest of the book in pixel size.
     *
     * If there's more than one image in this array, arrow buttons are shown like in the picture,
     * allowing the viewer to switch between images. If there's only one image, they're not.
     */
    var images: MutableList<ResourceLocation> = mutableListOf()

    fun image(img: ResourceLocation) {
        images.add(img)
    }

    operator fun ResourceLocation.unaryPlus(){
        images.add(this)
    }

    /**
     * The title of the page, shown above the image.
     */
    var title: String? = null

    /**
     * Defaults to false. Set to true if you want the image to be bordered, like in the picture. It's suggested that border is set to true for images that use the entire canvas, whereas images that don't touch the corners shouldn't have it.
     */
    var border: Boolean? = null

    /**
     * The text to display on this page, under the image. This text can be formatted.
     */
    var text: String? = null

    operator fun String.unaryPlus(){
        text = this
    }

    override fun toJson(): JsonObject {
        return super.toJson().also { json->
            val images = JsonArray()
            for (image in this.images) {
                images.add(image.toString())
            }
            json.add("images", images)
            title?.let {  json.addProperty("title", it) }
            border?.let {  json.addProperty("border", it) }
            text?.let {  json.addProperty("text", it) }
        }
    }
}

abstract class BaseCraftingPage(
        /** The ID of the first recipe you want to show. */
        val recipe: ResourceLocation,
        type: String
) : EntryPage(type) {

    /** The ID of the second recipe you want to show. Displaying two recipes is optional. */
    var recipe2: String? = null

    /** The title of the page, to be displayed above both recipes. This is optional, but if you include it, only this title will be displayed, rather than the names of both recipe output items. */
    var title: String? = null

    /** The text to display on this page, under the recipes. This text can be formatted. */
    var text: String? = null

    override fun toJson(): JsonObject {
        return super.toJson().also { json->
            json.addProperty("recipe", recipe)
            recipe2?.let {  json.addProperty("recipe2", it) }
            title?.let {  json.addProperty("title", it) }
            text?.let {  json.addProperty("text", it) }
        }
    }
}

class CraftingPage(recipe: ResourceLocation): BaseCraftingPage(recipe, "crafting")

class SmeltingPage(recipe: ResourceLocation): BaseCraftingPage(recipe, "smelting")

class MultiblockPage: EntryPage("multiblock"){
    /** The name of the multiblock you're displaying. Shows as a header above the multiblock display. */
    lateinit var name: String

    /**
     * For modders only. The ID of the multiblock you want to display. See API Usage for how to create and register Multiblocks in code.
     * Note: Either this or "multiblock" need to be set for this page type to work.
     */
    @SerializedName("multiblock_id")
    var multiblockId: ResourceLocation? = null

    private var multiblock: MultiblockInfo? = null

    /**
     * The multiblock object to display. See Using Multiblocks for how to create this object.
     * Note: Either this or "multiblock_id" need to be set for this page type to work.
     */
    @PatchouliDSL
    fun definition(init: MultiblockInfo.() -> Unit) {
        multiblock = MultiblockInfo().also(init)
    }

    /** Defaults to true. Set this to false to disable the "Visualize" button. */
    @SerializedName("enable_visualize")
    var enableVisualize: Boolean? = null

    /** The text to display on this page, under the multiblock. This text can be formatted. */
    var text: String? = null

    override fun toJson(): JsonObject {
        return super.toJson().also { json ->
            if ((multiblock == null && multiblockId == null) || (multiblock != null && multiblockId != null)) {
                throw IllegalStateException("One of either multiblock or multiblockId needs to be supplied")
            }
            json.addProperty("name", name)
            multiblockId?.let { json.addProperty("multiblock_id", it) }
            multiblock?.let { json.add("multiblock", it.toJson()) }
            enableVisualize?.let { json.addProperty("enable_visualize", it)}
            text?.let {  json.addProperty("text", it) }
        }
    }
}

class MultiblockInfo {
    private val pattern: MutableList<MultiblockLayer> = mutableListOf()

    private val patternToValue: BiMap<String, String> = HashBiMap.create()
    private val valueToPattern = patternToValue.inverse()
    private var zeroValue: String? = null

    //I pity the fool who needs more than 26 pattern keys...
    private val availablePatternItem: Queue<String> = ArrayDeque("ABCDEFGHIJKLMNOPQRSTUVWXYZ@#$%^&*123456789".map{it.toString()}.toList())
    private fun getMapping(value: String): String {
        if (valueToPattern.containsKey(value)) {
            return valueToPattern[value]!!
        }
        val pattern = availablePatternItem.remove()!!
        patternToValue[pattern] = value
        return pattern
    }

    var symmetrical: Boolean? = null
    private var offset: Array<Int>? = null
    fun offset(x: Int, y: Int, z: Int) {
        this.offset = arrayOf(x,y,z)
    }

    @PatchouliDSL
    fun layer(block: MultiblockLayer.()->Unit) {
        this.pattern.add(MultiblockLayer().apply(block))
    }

    fun toJson(): JsonObject {
        val json = JsonObject()
        json.add("pattern", jsonArray {
            pattern.forEach { layer ->
                this@jsonArray.add(layer.toJson())
            }
        })
        json.add("mapping", jsonObject {
            patternToValue.forEach { (key, value) -> addProperty(key, value) }
            zeroValue?.let { addProperty("0", it) }
        })

        symmetrical?.let { json.addProperty("symmetrical", it) }
        offset?.let { json.add("offset", jsonArray {
            add(it[0])
            add(it[1])
            add(it[2])
        }) }
        return json
    }

    inner class MultiblockLayer {
        private val layerRows: MutableList<MultiblockRow> = mutableListOf()

        @PatchouliDSL
        fun row(block: MultiblockRow.()->Unit) {
            layerRows.add(MultiblockRow().apply(block))
        }

        fun toJson(): JsonArray {
            val layerJson = JsonArray()
            layerRows.forEach {
                layerJson.add(it.toJson())
            }
            return layerJson
        }
    }

    inner class MultiblockRow {
        private val column: MutableList<String> = mutableListOf()

        @PatchouliDSL
        operator fun BlockState.unaryPlus() {
            column.add(this.toString())
        }

        private fun BlockRegistryObject<out Block, out Item>.toPattern(): String {
            return block.block.registryName!!.toString()
        }

        @PatchouliDSL
        operator fun BlockRegistryObject<out Block, out Item>.unaryPlus() {
            column.add(toPattern())
        }

        @PatchouliDSL
        infix fun BlockRegistryObject<out Block, out Item>.facing(direction: Direction) {
            column.add(blockStatePattern(this){
                Attribute.get(block, AttributeStateFacing::class.java).setDirection(it, direction)
            })
        }

        private fun BlockState.toPattern(): String {
            return if (values.isNotEmpty()) {
                this.block.registryName!!.toString()+values.entries.joinToString(separator = ",", prefix = "[", postfix = "]", transform = StateHolder.field_235890_a_::apply)//+"["+(this.toString().substringAfter("["))
            } else {
                this.block.registryName!!.toString()
            }
        }

        private fun <BLOCK: Block> blockStatePattern(registryObject: BlockRegistryObject<BLOCK, out Item>, stateProvider: BLOCK.(defaultState: BlockState)->BlockState): String {
            return stateProvider(registryObject.block, registryObject.block.defaultState).toPattern()
        }

        @PatchouliDSL
        operator fun <BLOCK: Block> BlockRegistryObject<BLOCK, out Item>.invoke(stateProvider: BLOCK.(defaultState: BlockState)->BlockState) {
            column.add(blockStatePattern(this, stateProvider))
        }

        @PatchouliDSL
        operator fun ITag.INamedTag<Block>.unaryPlus() {
            column.add(toPattern())
        }

        private fun ITag.INamedTag<Block>.toPattern() = "#$name"

        @PatchouliDSL
        fun space() {
            column.add(" ")
        }

        @PatchouliDSL
        fun center() {
            column.add("0")
        }

        @PatchouliDSL
        fun <BLOCK: Block> center(blockRegistryObject: BlockRegistryObject<BLOCK, Item>, stateProvider: BLOCK.(defaultState: BlockState)->BlockState) {
            center()
            zeroValue = blockStatePattern(blockRegistryObject, stateProvider)
        }

        @PatchouliDSL
        fun center(block: BlockRegistryObject<out Block, out Item>) {
            center()
            zeroValue = block.toPattern()
        }

        @PatchouliDSL
        fun center(tag: ITag.INamedTag<Block>) {
            center()
            zeroValue = tag.toPattern()
        }

        fun toJson(): String {
            return column.joinToString(separator = "") { item ->
                when (item) {
                    " ", "0" -> item
                    else -> getMapping(item)
                }
            }
        }
    }
}

class EntityPage: EntryPage("entity") {
    /**
     * The ID of the entity you want to display. To display a chicken you'd use "minecraft:chicken". You can also add NBT data to the entity, in the same way you would in an ItemStack String.
     */
    lateinit var entity: ResourceLocation

    var nbt: CompoundNBT? = null

    /**
     * The scale to display the entity at. Defaults to 1.0. Values lower than 1.0 will have the entity be smaller than usual, while higher than 1.0 will have it be larger. Negative values will flip it upside down.
     */
    var scale: Float? = null

    /**
     * An amount to offset the entity display. Some mod entities have weird renders and won't fit in the box properly, you can change this to move them up and down.
     */
    var offset: Float? = null

    /**
     * Defaults to true. Set this to false to make the entity not rotate.
     */
    var rotate: Boolean? = null

    /**
     * The rotation at which this entity should be rendered. This value is only used if "rotate" is false. The default is -45.
     */
    @SerializedName("default_rotation")
    var defaultRotation: Float? = null

    /**
     * The name to display on top of the frame. If this is empty or not defined, it'll grab the name of the entity and use that instead.
     */
    var name: String? = null

    /**
     * The text to display on this page, under the entity. This text can be formatted.
     */
    var text: String? = null

    override fun toJson(): JsonObject {
        return super.toJson().also { json->
            json.addProperty("entity", entity.toString() + if (nbt != null) nbt.toString() else "")
            scale?.let {  json.addProperty("scale", it) }
            offset?.let {  json.addProperty("offset", it) }
            rotate?.let {  json.addProperty("rotate", it) }
            defaultRotation?.let {  json.addProperty("default_rotation", it) }
            name?.let {  json.addProperty("name", it) }
            text?.let {  json.addProperty("text", it) }
        }
    }
}

class SpotlightPage: EntryPage("spotlight") {
    /**
     * An ItemStack String representing the item to be spotlighted.
     */
    lateinit var item: ItemStack

    /**
     * A custom title to show instead on top of the item. If this is empty or not defined, it'll use the item's name instead.
     */
    var title: String? = null

    /**
     * Defaults to false. Set this to true to mark this spotlight page as the "recipe page" for the item being spotlighted.
     * If you do so, when looking at pages that display the item, you can shift-click the item to be taken to this page.
     * Highly recommended if the spotlight page has instructions on how to create an item by non-conventional means.
     */
    @SerializedName("link_recipe")
    var linkRecipe: Boolean? = null

    /**
     * The text to display on this page, under the item. This text can be formatted.
     */
    var text: String? = null

    override fun toJson(): JsonObject {
        return super.toJson().also { json->
            json.addProperty("item", item)
            title?.let {  json.addProperty("title", it) }
            linkRecipe?.let {  json.addProperty("link_recipe", it) }
            text?.let {  json.addProperty("text", it) }
        }
    }
}

class LinkPage: TextPage() {
    /**
     * The URL to open when clicking the button. In theory everything is supported, but please stick to HTTP/HTTPS addresses.
     */
    lateinit var url: String

    /**
     * The text to display on the link button.
     */
    @SerializedName("link_text")
    lateinit var linkText: String

    override fun toJson(): JsonObject {
        return super.toJson().also { json->
            json.addProperty("url", url)
            json.addProperty("link_text", linkText)
        }
    }
}

class RelationsPage: EntryPage("relations") {
    /**
     * An array of the entries that should be linked in this page. These are the IDs of the entries you want to link to in the same way you'd link an entry to a category's ID.
     */
    var entries: MutableList<String> = mutableListOf()

    /**
     * The title of this page, to display above the links. If this is missing or empty, it'll show "Related Chapters" instead.
     */
    var title: String? = null

    /**
     * The text to display on this page, under the links. This text can be formatted.
     */
    var text: String? = null

    override fun toJson(): JsonObject {
        return super.toJson().also { json->
            json.add("entries", jsonArray {
                entries.forEach(this@jsonArray::add)
            })
            title?.let {  json.addProperty("title", it) }
            text?.let {  json.addProperty("text", it) }
        }
    }
}

class QuestPage: EntryPage("quest") {
    /**
     * The advancement that should be completed to clear this quest. You may leave this empty should you want the quest to be completed manually. The image shows a quest with "trigger" set on the left and one with it unset on the right.
     */
    var trigger: ResourceLocation? = null

    /**
     * The title of this page, to display above the links. If this is missing or empty, it'll show "Objective" instead.
     */
    var title: String? = null

    /**
     * The text to display on this page, under the links. This text can be formatted.
     */
    var text: String? = null

    override fun toJson(): JsonObject {
        return super.toJson().also { json ->
            trigger?.let { json.addProperty("trigger", it) }
            title?.let { json.addProperty("title", it) }
            text?.let { json.addProperty("text", it) }
        }
    }
}

class EmptyPage: EntryPage("empty") {
    /**
     * Defaults to true. Set to false to draw a completely empty page, without the page filler... for whatever reason.
     */
    @SerializedName("draw_filler")
    var drawFiller: Boolean? = null

    override fun toJson(): JsonObject {
        return super.toJson().also { json->
            drawFiller?.let { json.addProperty("draw_filler", it) }
        }
    }
}