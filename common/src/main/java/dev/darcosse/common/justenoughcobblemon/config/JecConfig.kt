package dev.darcosse.common.justenoughcobblemon.config

import java.nio.file.Path

/**
 * Entry point of the mod's configuration: `config/justenoughcobblemon/client.json` and
 * `config/justenoughcobblemon/server.json`.
 *
 * The loaders call [init] at startup with their config directory, since only they know where it
 * is. Until then, and for the client file on a dedicated server, the defaults are served.
 *
 * This object must not reference any optional mod (Jade, JEI): it is loaded by the loader entry
 * points, which run for every player.
 *
 * @author Darcosse
 * @version 1.0
 * @since 2026
 */
object JecConfig {

    /** Sub-folder of the game's config directory holding both files. */
    private const val FOLDER = "justenoughcobblemon"

    private val clientFile = ConfigFile("client.json", ClientConfig::class.java) { ClientConfig() }
    private val serverFile = ConfigFile("server.json", ServerConfig::class.java) { ServerConfig() }

    /**
     * Loads both files, creating them with their defaults when missing.
     *
     * @param configRoot the loader's config directory
     * @param isClient whether this is a physical client; a dedicated server has no use for
     *        client.json, so the file is not created there
     */
    @JvmStatic
    fun init(configRoot: Path, isClient: Boolean) {
        val directory = configRoot.resolve(FOLDER)
        if (isClient) {
            clientFile.bind(directory)
        }
        serverFile.bind(directory)
    }

    /** Current client settings, reloaded when the file changes. */
    @JvmStatic
    fun client(): ClientConfig = clientFile.get()

    /** Current server settings, reloaded when the file changes. */
    @JvmStatic
    fun server(): ServerConfig = serverFile.get()
}
