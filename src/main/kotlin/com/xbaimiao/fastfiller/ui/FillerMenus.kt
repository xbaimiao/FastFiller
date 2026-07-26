package com.xbaimiao.fastfiller.ui

import com.xbaimiao.easylib.util.plugin
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File

/**
 * gui 目录下的菜单配置管理
 */
object FillerMenus {

    lateinit var main: MainMenu
        private set

    lateinit var confirm: ConfirmMenu
        private set

    lateinit var container: ContainerMenu
        private set

    fun load() {
        main = MainMenu(loadSection("main.yml"))
        confirm = ConfirmMenu(loadSection("confirm.yml"))
        container = ContainerMenu(loadSection("container.yml"))
    }

    private fun loadSection(fileName: String): ConfigurationSection {
        val file = File(plugin.dataFolder, "gui${File.separator}$fileName")
        if (!file.exists()) {
            plugin.saveResource("gui/$fileName", false)
        }
        return YamlConfiguration.loadConfiguration(file)
    }

}
