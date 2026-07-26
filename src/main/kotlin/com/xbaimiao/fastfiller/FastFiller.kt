package com.xbaimiao.fastfiller

import com.xbaimiao.easylib.EasyPlugin
import com.xbaimiao.easylib.util.plugin
import com.xbaimiao.fastfiller.api.FillerApi
import com.xbaimiao.fastfiller.core.config.BlockNames
import com.xbaimiao.fastfiller.core.config.FillerConfig
import com.xbaimiao.fastfiller.core.fill.FillScheduler
import com.xbaimiao.fastfiller.core.hook.Hooks
import com.xbaimiao.fastfiller.core.item.FillerApiImpl
import com.xbaimiao.fastfiller.core.session.SessionManager
import com.xbaimiao.fastfiller.ui.FillerMenus
import org.bukkit.configuration.file.FileConfiguration

@Suppress("unused")
class FastFiller : EasyPlugin() {

    override fun enable() {
        saveDefaultConfig()
        FillerConfig.load()
        BlockNames.load()
        api = FillerApiImpl()
        Hooks.init()
        FillerMenus.load()
        FillScheduler.start()
        FillerCommands.register()
        logger.info("${description.name} v${description.version} 启动完成")
    }

    override fun disable() {
        FillScheduler.stop()
        SessionManager.clear()
    }

    companion object {

        /** 插件实例 **/
        val inst: FastFiller get() = plugin as FastFiller

        /** config.yml **/
        val conf: FileConfiguration get() = inst.config

        /** 对外 API **/
        @JvmStatic
        lateinit var api: FillerApi
            private set

    }

}
