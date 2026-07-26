package com.xbaimiao.fastfiller.listener

import com.xbaimiao.easylib.util.EListener
import com.xbaimiao.fastfiller.core.session.SessionManager
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent

/**
 * 玩家退出时清理会话数据
 */
@EListener
object QuitListener : Listener {

    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        SessionManager.remove(event.player)
    }

}
