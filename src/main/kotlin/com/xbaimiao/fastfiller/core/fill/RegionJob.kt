package com.xbaimiao.fastfiller.core.fill

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.Container
import java.util.concurrent.ConcurrentHashMap

/**
 * 按区域逐格推进的任务基类
 */
internal abstract class RegionJob(
    protected val region: Region,
    private val finishCallback: () -> Unit,
) : FillJob {

    private val worldId = region.world.uid
    private val cursor = RegionCursor(region)
    private var finished = false

    override val isFinished: Boolean get() = finished

    override fun tick(deadline: Long, maxBlocks: Int) {
        if (finished) {
            return
        }
        val world = Bukkit.getWorld(worldId)
        if (world == null) {
            // 世界被卸载, 直接结束任务
            finished = true
            return
        }
        var processed = 0
        while (processed < maxBlocks && System.nanoTime() < deadline) {
            processed++
            if (!handle(world.getBlockAt(cursor.x, cursor.y, cursor.z)) || !cursor.next()) {
                finished = true
                return
            }
        }
    }

    /**
     * 处理单个方块
     *
     * @return false 代表提前结束整个任务
     */
    protected abstract fun handle(block: Block): Boolean

    override fun onFinish() {
        finishCallback()
    }

    companion object {

        private val containerCache = ConcurrentHashMap<Material, Boolean>()

        /**
         * 判断方块是不是容器(箱子/漏斗等)
         *
         * 是否为容器由材质决定, 因此按材质缓存结果,
         * 避免对每个方块都创建一次 BlockState 快照
         */
        fun isContainer(block: Block): Boolean {
            return containerCache.getOrPut(block.type) { block.state is Container }
        }

    }

}
