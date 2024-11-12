package com.xbaimiao.fastfiller.ui

import com.xbaimiao.easylib.util.plugin
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File

object FillerUIManager {

    lateinit var main: FillerUI
    lateinit var confirm: ConfirmFillerUI
    lateinit var container: FillerUI

    fun init() {
        val mainFile = File(plugin.dataFolder, "gui${File.separator}main.yml")
        if (!mainFile.exists()) {
            plugin.saveResource("gui/main.yml", false)
        }
        main = MainFillerUI(YamlConfiguration.loadConfiguration(mainFile))


        val confirmFile = File(plugin.dataFolder, "gui${File.separator}confirm.yml")
        if (!confirmFile.exists()) {
            plugin.saveResource("gui/confirm.yml", false)
        }
        confirm = ConfirmFillerUI(YamlConfiguration.loadConfiguration(confirmFile))


        val containerFile = File(plugin.dataFolder, "gui${File.separator}container.yml")
        if (!containerFile.exists()) {
            plugin.saveResource("gui/container.yml", false)
        }
        container = ContainerFillerUI(YamlConfiguration.loadConfiguration(containerFile))
    }

}
