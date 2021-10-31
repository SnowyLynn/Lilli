package com.amelynn.lilli.data

import com.google.gson.GsonBuilder
import net.fabricmc.loader.api.FabricLoader
import java.io.*


class Config {
    var serverName = "My Lilli Server"
    var header = "⌛ %tps%"
    var footer = ""
    var motd = "Hallo %player%\nWilkommen auf %server%"
    @Throws(IOException::class)
    fun save() {
        val gson = GsonBuilder().setPrettyPrinting().create()
        val config = File(FabricLoader.getInstance().configDir.toString(), "lilli-config.json")
        FileWriter(config).use { file -> file.write(gson.toJson(this)) }
    }
    @Throws(IOException::class)
    fun load() {
        val config1 = File(FabricLoader.getInstance().configDir.toString(), "lilli-config.json")
        if (!config1.exists()) {
            save()
            return
        }
        if (config1.isDirectory) {
            if (config1.delete()) {
                save()
                return
            }
        }
        try {
            FileReader(config1).use { reader ->
                val gson = GsonBuilder().setPrettyPrinting().create()
                val config = gson.fromJson(reader, Config::class.java)

                serverName = config.serverName
                header = config.header
                footer = config.footer
                motd = config.motd
            }
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
    }

    companion object {
        var INSTANCE = Config()
    }
}
