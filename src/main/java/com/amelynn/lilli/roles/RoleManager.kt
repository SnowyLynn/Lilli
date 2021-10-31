package com.amelynn.lilli.roles

import net.fabricmc.loader.api.FabricLoader
import java.io.File
import java.io.IOException

class RoleManager {
    var instance:RoleManager = RoleManager()

    @Throws (IOException::class)
    fun load() {
        val roleFile = File(FabricLoader.getInstance().gameDir.toString(), "lilliRoles.bin")
    }
    fun save() {
        TODO("Not yet implemented")
    }
}