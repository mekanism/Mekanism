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

abstract class BasePatchouliProvider(protected val generator: DataGenerator, protected val modid: String, bookId: String) : DataProvider {

    val book: PatchouliBook = PatchouliBook(ResourceLocation(modid, bookId))

    /**
     * Build the book in this function
     */
    abstract fun PatchouliBook.buildBook()

    final override fun run(output: CachedOutput){
        book.buildBook()
        saveBook(output, book.toJson(), book.id)
        for (category in book.categories) {
            saveCategory(output, category, book)
        }
        for (entry in book.entries) {
            saveEntry(output, entry, book)
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