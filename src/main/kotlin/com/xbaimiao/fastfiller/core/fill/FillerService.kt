package com.xbaimiao.fastfiller.core.fill

import com.xbaimiao.easylib.chat.Lang.sendLang
import com.xbaimiao.fastfiller.FastFiller
import com.xbaimiao.fastfiller.core.Permissions
import com.xbaimiao.fastfiller.core.config.BlockNames
import com.xbaimiao.fastfiller.core.config.FillerConfig
import com.xbaimiao.fastfiller.core.hook.Hooks
import com.xbaimiao.fastfiller.core.hook.MagicBlockHook
import com.xbaimiao.fastfiller.core.session.SessionManager
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

/**
 * 填充 / 清理操作的统一入口
 *
 * 所有前置校验(选点/世界/领地/范围/冷却)都集中在这里,
 * 菜单只负责触发
 */
internal object FillerService {

    /**
     * 用创世斧容器里的方块填充选点区域
     */
    fun startFill(player: Player) {
        if (!checkReady(player)) {
            return
        }
        val fillerItem = FastFiller.api.fillerItemInMainHand(player)
        if (fillerItem == null) {
            player.sendLang("filler-noItem")
            return
        }
        val stored = fillerItem.storage.get()
        if (stored.isEmpty) {
            player.sendLang("fill-noItem")
            player.closeInventory()
            return
        }
        val region = resolveRegion(player) ?: return
        player.closeInventory()
        player.sendLang("filler", BlockNames.of(stored.material))
        start(
            player,
            FillBlocksJob(region, fillerItem.storage, stored.material, FillerConfig.replaceableBlocks.toSet()) {
                finish(player)
            }
        )
    }

    /**
     * 清理选点区域内的指定方块
     */
    fun startClear(player: Player, clearable: List<Material>) {
        if (!checkReady(player)) {
            return
        }
        val region = resolveRegion(player) ?: return
        player.closeInventory()
        player.sendLang("fill-clear")
        start(player, ClearBlocksJob(region, clearable.toSet()) { finish(player) })
    }

    /**
     * 用副手的魔术方块填充选点区域
     */
    fun startMagicFill(player: Player, magicItem: ItemStack) {
        if (!checkReady(player)) {
            return
        }
        if (!player.hasPermission(Permissions.MAGIC)) {
            player.sendLang("filler-magic-no-permission")
            return
        }
        if (MagicBlockHook.isLimitedMagicItem(magicItem)) {
            player.sendLang("filler-magic-no-amount")
            return
        }
        val region = resolveRegion(player) ?: return
        player.closeInventory()
        player.sendLang("filler-magicblock")
        // 魔术方块填充额外覆盖岩浆
        val replaceable = (FillerConfig.replaceableBlocks + Material.LAVA).toSet()
        start(player, MagicFillJob(region, magicItem.type, replaceable) { finish(player) })
    }

    /**
     * 冷却与并发检查
     */
    private fun checkReady(player: Player): Boolean {
        if (SessionManager.isFilling(player)) {
            player.sendLang("filler-infill")
            return false
        }
        val remaining = SessionManager.remainingCooldown(player)
        if (remaining > 0) {
            player.sendLang("filler-cooldown", remaining)
            return false
        }
        return true
    }

    /**
     * 校验选点并构建区域, 校验失败时给玩家发提示并返回 null
     */
    private fun resolveRegion(player: Player): Region? {
        val session = SessionManager.of(player)
        val points = session.points()
        if (points == null) {
            player.sendLang("select-noSelect")
            return null
        }
        val region = Region.of(points.first, points.second)
        if (region == null) {
            player.sendLang("select-not-same-world")
            return null
        }
        if (region.world.name !in FillerConfig.enableWorlds) {
            player.sendLang("select-not-enable-world")
            return null
        }
        // 领地可能在选点之后发生变化, 执行前再校验一次
        for (point in listOf(points.first, points.second)) {
            val denyLangKey = Hooks.checkBuild(player, point)
            if (denyLangKey != null) {
                player.sendLang(denyLangKey)
                return null
            }
        }
        if (!Hooks.inSameRegion(points.first, points.second)) {
            player.sendLang("select-res-dissimilarity")
            return null
        }
        if (!player.hasPermission(Permissions.VIP)) {
            if (region.sizeX > FillerConfig.maxRangeX || region.sizeZ > FillerConfig.maxRangeZ) {
                player.sendLang("filler-tooLarge")
                return null
            }
        }
        return region
    }

    private fun start(player: Player, job: FillJob) {
        SessionManager.setFilling(player, true)
        SessionManager.markUse(player)
        FillScheduler.submitJob(job)
    }

    private fun finish(player: Player) {
        SessionManager.setFilling(player, false)
        if (player.isOnline) {
            player.sendLang("filler-finish")
        }
    }

}
