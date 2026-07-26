package com.xbaimiao.fastfiller.core.session

import com.xbaimiao.fastfiller.core.Permissions
import com.xbaimiao.fastfiller.core.config.FillerConfig
import org.bukkit.entity.Player
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * 玩家会话管理
 *
 * 老版本用玩家名做键且从不清理, 玩家改名或退出后数据会一直留在内存里,
 * 这里统一用 UUID 并在退出时清理
 */
object SessionManager {

    private val sessions = ConcurrentHashMap<UUID, FillerSession>()

    fun of(player: Player): FillerSession {
        return sessions.getOrPut(player.uniqueId) { FillerSession() }
    }

    fun remove(player: Player) {
        sessions.remove(player.uniqueId)
    }

    fun clear() {
        sessions.clear()
    }

    /**
     * 玩家是否有正在执行的任务
     */
    fun isFilling(player: Player): Boolean {
        return of(player).filling
    }

    fun setFilling(player: Player, filling: Boolean) {
        of(player).filling = filling
    }

    /**
     * 剩余冷却秒数, 0 代表可以使用
     */
    fun remainingCooldown(player: Player): Long {
        val cooldown = FillerConfig.cooldownSeconds
        if (cooldown <= 0 || player.hasPermission(Permissions.VIP)) {
            return 0
        }
        val lastUseTime = of(player).lastUseTime
        if (lastUseTime <= 0) {
            return 0
        }
        val passed = (System.currentTimeMillis() - lastUseTime) / 1000
        return (cooldown - passed).coerceAtLeast(0)
    }

    fun markUse(player: Player) {
        if (FillerConfig.cooldownSeconds <= 0) {
            return
        }
        of(player).lastUseTime = System.currentTimeMillis()
    }

}
