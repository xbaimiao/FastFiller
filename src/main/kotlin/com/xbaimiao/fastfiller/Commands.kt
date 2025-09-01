package com.xbaimiao.fastfiller

import com.xbaimiao.easylib.chat.Lang.sendLang
import com.xbaimiao.easylib.command.command
import com.xbaimiao.easylib.util.giveItem
import com.xbaimiao.fastfiller.Config
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

object Commands {

    fun init() {
        command<CommandSender>("playerfiller") {
            permission = "playerfiller.admin"
            subCommand<CommandSender>("give") {
                description = "给玩家一个填充工具"
                val playerArg = players()
                exec {
                    val player = playerArg.value() ?: return@exec error("玩家不在线")
                    player.giveItem(Config.defaultItem)
                    sender.sendLang("giveItem", player.name)
                }
            }
            subCommand<Player>("add") {
                description = "为你手上的工具增加物品储量"
                val amountArg = number()
                exec {
                    val fillerItem = FastFiller.api.toFillerItem(sender.inventory.itemInMainHand)
                    if (fillerItem == null) {
                        sender.sendMessage("§c你手上的不是PlayerFiller工具")
                        return@exec
                    }
                    val old = fillerItem.getContainer().getItem()
                    if (old == null) {
                        sender.sendMessage("§c里面没有东西 不知道添加什么类型的方块")
                        return@exec
                    }

                    val amount = amountArg.value()?.toInt() ?: return@exec error("数量错误")
                    fillerItem.getContainer().addItem(old.first, amount)
                    sender.sendMessage("§a添加成功: $amount")
                }
            }
        }.register()
    }

}