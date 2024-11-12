package com.xbaimiao.fastfiller

import com.xbaimiao.easylib.EasyPlugin
import com.xbaimiao.easylib.chat.BuiltInConfiguration
import com.xbaimiao.easylib.util.ShortUUID
import com.xbaimiao.easylib.util.plugin
import com.xbaimiao.fastfiller.api.PlayerFillerAPI
import com.xbaimiao.fastfiller.api.impl.DefaultPlayerFillerAPI
import com.xbaimiao.fastfiller.core.Hook
import com.xbaimiao.fastfiller.core.data.DataContainer
import com.xbaimiao.fastfiller.core.data.MemoryDataContainer
import com.xbaimiao.fastfiller.core.workload.WorkLoadPool
import com.xbaimiao.fastfiller.ui.FillerUIManager

@Suppress("unused")
class FastFiller : EasyPlugin() {

    companion object {
        val inst get() = plugin as FastFiller

        val conf get() = inst.config


        @JvmStatic
        lateinit var workLoadPool: WorkLoadPool

        @JvmStatic
        lateinit var dataContainer: DataContainer

        @JvmStatic
        lateinit var api: PlayerFillerAPI

    }

    val itemI18n by enable { BuiltInConfiguration("itemi18n.yml") }

    override fun enable() {
        saveDefaultConfig()
        FillerUIManager.init()
        Hook.init()
        workLoadPool = WorkLoadPool(6)
        dataContainer = MemoryDataContainer()
        api = DefaultPlayerFillerAPI()
        Commands.init()
        logger.info("${description.name} 插件启动成功 ${ShortUUID.randomShortUUID()}")
    }

}
