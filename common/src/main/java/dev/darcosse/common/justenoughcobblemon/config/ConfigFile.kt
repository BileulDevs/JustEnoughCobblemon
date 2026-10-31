package dev.darcosse.common.justenoughcobblemon.config

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.lang.reflect.Modifier
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path

/**
 * One JSON config file, loaded into an instance of [type] and reloaded automatically when the
 * file changes on disk, so edits apply without restarting the game.
 *
 * Loading is forgiving on purpose: missing keys take their default value, unknown enum values
 * fall back to the default of that field, and the file is rewritten with every key present so the
 * player always sees the full list of options. A file that is not valid JSON is never
 * overwritten: the previous values are kept and a warning is logged, so a typo cannot wipe the
 * player's settings.
 *
 * @author Darcosse
 * @version 1.0
 * @since 2026
 */
class ConfigFile<T : Any>(
    private val fileName: String,
    private val type: Class<T>,
    private val defaults: () -> T
) {

    private companion object {
        val LOGGER: Logger = LoggerFactory.getLogger("JustEnoughCobblemon/Config")

        val GSON: Gson = GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create()

        /**
         * Minimum delay between two checks of the file's modification time. Config values are read
         * every tick while a tooltip is shown; checking the disk that often would be wasteful.
         * Lower means faster pickup of edits, higher means fewer filesystem calls.
         */
        const val RELOAD_CHECK_INTERVAL_MS = 1000L
    }

    @Volatile
    private var current: T = defaults()

    /** Null until [bind] is called; the defaults are served until then. */
    private var path: Path? = null

    /** Modification time of the file as of the last load or write, to detect external edits. */
    private var lastModified: Long = Long.MIN_VALUE

    /** Earliest time, in ms, at which [get] checks the file again. */
    private var nextCheckAt: Long = 0L

    /**
     * Attaches this config to its file in [directory] and loads it, creating the directory and a
     * default file if needed.
     */
    @Synchronized
    fun bind(directory: Path) {
        path = directory.resolve(fileName)
        reload()
    }

    /**
     * Current values. Cheap enough to call on every tooltip refresh: the disk is checked at most
     * once per [RELOAD_CHECK_INTERVAL_MS].
     */
    @Synchronized
    fun get(): T {
        val now = System.currentTimeMillis()
        if (now >= nextCheckAt) {
            nextCheckAt = now + RELOAD_CHECK_INTERVAL_MS
            val file = path
            if (file != null && modificationTime(file) != lastModified) {
                reload()
            }
        }
        return current
    }

    /** Reads the file, repairs it, and writes it back if repairing changed anything. */
    private fun reload() {
        val file = path ?: return
        try {
            Files.createDirectories(file.parent)
            if (!Files.exists(file)) {
                current = defaults()
                write(file, GSON.toJson(current))
                return
            }

            val text = Files.readString(file, StandardCharsets.UTF_8)
            val loaded = GSON.fromJson(text, type) ?: defaults()
            fillNulls(loaded, defaults())
            current = loaded

            val normalized = GSON.toJson(loaded)
            if (normalized.trim() != text.trim()) {
                write(file, normalized)
            } else {
                lastModified = modificationTime(file)
            }
        } catch (e: Exception) {
            // Keep the previous values and leave the file alone, so the player can fix it.
            lastModified = modificationTime(file)
            LOGGER.warn("Could not read {}, keeping the previous values: {}", file, e.message)
        }
    }

    private fun write(file: Path, json: String) {
        Files.writeString(file, json + System.lineSeparator(), StandardCharsets.UTF_8)
        lastModified = modificationTime(file)
    }

    private fun modificationTime(file: Path): Long =
        try {
            Files.getLastModifiedTime(file).toMillis()
        } catch (e: Exception) {
            Long.MIN_VALUE
        }

    /**
     * Replaces every null field of [target] by the same field of [fallback], recursing into
     * nested config sections.
     *
     * Gson writes null into a field when the JSON holds an unknown enum value or an explicit null,
     * even for Kotlin non-null types; left as is, that null would crash the first tooltip reading
     * it. Only this mod's config classes are recursed into.
     */
    private fun fillNulls(target: Any, fallback: Any) {
        var clazz: Class<*>? = target.javaClass
        while (clazz != null && clazz != Any::class.java) {
            for (field in clazz.declaredFields) {
                if (Modifier.isStatic(field.modifiers)) continue
                field.isAccessible = true
                val value = field.get(target)
                val defaultValue = field.get(fallback)
                if (value == null) {
                    field.set(target, defaultValue)
                } else if (defaultValue != null && isConfigSection(value)) {
                    fillNulls(value, defaultValue)
                }
            }
            clazz = clazz.superclass
        }
    }

    /** Config sections are the classes of this package; everything else is a leaf value. */
    private fun isConfigSection(value: Any): Boolean =
        value.javaClass.packageName == ConfigFile::class.java.packageName && !value.javaClass.isEnum
}
