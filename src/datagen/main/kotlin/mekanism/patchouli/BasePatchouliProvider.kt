package mekanism.patchouli

import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import mekanism.patchouli.dsl.*
import net.minecraft.data.CachedOutput
import net.minecraft.data.DataGenerator
import net.minecraft.data.DataProvider
import net.minecraft.resources.ResourceLocation
import org.apache.logging.log4j.LogManager
import java.io.IOException

abstract class BasePatchouliProvider(protected val generator: DataGenerator, protected val modid: String) : DataProvider {

    /**
     * Adds books to the cache. Param is turned into an invokable to create books.
     * e.g.
     * <code>
     *      output("testbook") {
     *          locale = "en_us"
     *          name = "my fancy book"
     *      }
     * </code>
     *
     * @param output the cache
     */
    abstract override fun run(output: CachedOutput)

    @PatchouliDSL
    operator fun CachedOutput.invoke(bookId: String, receiver: PatchouliBook.()->Unit) {
        val book = patchouliBook(modid, bookId, receiver)
        saveBook(this, book.toJson(), book.id)
        for (category in book.categories) {
            saveCategory(this, category, book)
        }
        for (entry in book.entries) {
            saveEntry(this, entry, book)
        }
    }

    private fun saveEntry(cache: CachedOutput, entry: Entry, book: PatchouliBook) {
        val mainOutput = generator.outputFolder
        val pathSuffix = makeBookPath(book.id) + "/" + book.locale + "/entries/" + entry.id + ".json"
        val outputPath = mainOutput.resolve(pathSuffix)
        try {
            DataProvider.saveStable(cache, entry.toJson(), outputPath)
        } catch (e: IOException) {
            LOGGER.error("Couldn't save entry to {}", outputPath, e)
        }
    }

    private fun saveCategory(cache: CachedOutput, category: Category, book: PatchouliBook) {
        val mainOutput = generator.outputFolder
        val pathSuffix = makeBookPath(book.id) + "/" + book.locale + "/categories/" + category.id + ".json"
        val outputPath = mainOutput.resolve(pathSuffix)
        try {
            DataProvider.saveStable(cache, category.toJson(), outputPath)
        } catch (e: IOException) {
            LOGGER.error("Couldn't save category to {}", outputPath, e)
        }
    }

    private fun saveBook(cache: CachedOutput, json: JsonObject, bookId: ResourceLocation) {
        val mainOutput = generator.outputFolder
        val pathSuffix = makeBookPath(bookId) + "/book.json"
        val outputPath = mainOutput.resolve(pathSuffix)
        try {
            DataProvider.saveStable(cache, json, outputPath)
        } catch (e: IOException) {
            LOGGER.error("Couldn't save book to {}", outputPath, e)
        }
    }

    private fun makeBookPath(bookId: ResourceLocation): String {
        return "data/" + bookId.namespace + "/patchouli_books/" + bookId.path
    }

    /**
     * Gets a name for this provider, to use in logging.
     */
    override fun getName(): String {
        return "Patchouli Book Provider: $modid"
    }

    companion object {
        private val LOGGER = LogManager.getLogger()
        //private val GSON = GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create()
    }
}