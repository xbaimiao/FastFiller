package com.xbaimiao.fastfiller.core.fill

import com.xbaimiao.fastfiller.api.FillerStorage
import com.xbaimiao.fastfiller.core.hook.MagicBlockHook
import org.bukkit.Material
import org.bukkit.block.Block

/**
 * 用创世斧容器里的方块填充区域
 *
 * 材质在任务创建时固定, 数量按批从容器中扣除,
 * 结束时把没用掉的部分退还给容器
 */
internal class FillBlocksJob(
    region: Region,
    private val storage: FillerStorage,
    private val material: Material,
    private val replaceable: Set<Material>,
    finishCallback: () -> Unit,
) : RegionJob(region, finishCallback) {

    /** 已经从容器扣除但还没用掉的数量 **/
    private var quota = 0

    /** 实际放下的方块数 **/
    var placed = 0
        private set

    override fun handle(block: Block): Boolean {
        if (block.type !in replaceable) {
            return true
        }
        if (quota <= 0) {
            // 容器里的方块类型被换掉时直接结束, 避免填错方块
            val stored = storage.get()
            if (stored.isEmpty || stored.material != material) {
                return false
            }
            quota = storage.take(BATCH_SIZE)
            if (quota <= 0) {
                return false
            }
        }
        block.type = material
        quota--
        placed++
        MagicBlockHook.unmarkMagicBlock(block)
        return true
    }

    override fun onFinish() {
        // 退还没用掉的部分
        if (quota > 0) {
            storage.add(material, quota)
            quota = 0
        }
        super.onFinish()
    }

    companion object {

        /** 每次从容器扣除的数量 **/
        private const val BATCH_SIZE = 512

    }

}
