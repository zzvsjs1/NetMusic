package com.github.tartaricacid.netmusic.client.gui

import com.github.tartaricacid.netmusic.NetMusic
import com.github.tartaricacid.netmusic.client.config.MusicListManage
import com.github.tartaricacid.netmusic.inventory.CDBurnerMenu
import com.github.tartaricacid.netmusic.item.ItemMusicCD.Companion.getSongInfo
import com.github.tartaricacid.netmusic.network.NetworkHandler
import com.github.tartaricacid.netmusic.network.message.SetMusicIDMessage
import com.mojang.blaze3d.platform.InputConstants
import net.minecraft.ChatFormatting
import net.minecraft.Util
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.components.Checkbox
import net.minecraft.client.gui.components.EditBox
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.player.Inventory
import org.anti_ad.mc.ipn.api.IPNIgnore
import java.util.regex.Pattern

@IPNIgnore
class CDBurnerMenuScreen(
    screenContainer: CDBurnerMenu,
    inv: Inventory,
    titleIn: Component
) : AbstractContainerScreen<CDBurnerMenu?>(screenContainer, inv, titleIn) {

    private var textField: EditBox? = null

    private var readOnlyButton: Checkbox? = null

    private var tips: Component = Component.empty()

    init {
        this.imageHeight = 176
    }

    override fun init() {
        super.init()

        var perText = ""
        var focus = false
        if (textField != null) {
            perText = textField!!.value
            focus = textField!!.isFocused
        }

        textField = object : EditBox(
            getMinecraft().font,
            leftPos + 12,
            topPos + 18,
            132,
            16,
            Component.literal("Music ID Box")
        ) {
            override fun insertText(text: String) {
                val matcher1 = URL_1_REG.matcher(text)
                if (matcher1.find()) {
                    val group = matcher1.group(1)
                    super.insertText(group)
                    return
                }

                val matcher2 = URL_2_REG.matcher(text)
                if (matcher2.find()) {
                    val group = matcher2.group(1)
                    super.insertText(group)
                    return
                }

                super.insertText(text)
            }
        }

        (textField as EditBox).value = perText
        (textField as EditBox).setBordered(false)
        (textField as EditBox).setMaxLength(19)
        (textField as EditBox).setTextColor(0xF3EFE0)
        (textField as EditBox).isFocused = focus
        (textField as EditBox).moveCursorToEnd()
        this.addWidget(this.textField)

        this.readOnlyButton = Checkbox(
            leftPos + 66,
            topPos + 34,
            80,
            20,
            Component.translatable("gui.netmusic.cd_burner.read_only"),
            false
        )

        this.addRenderableWidget(this.readOnlyButton)
        this.addRenderableWidget(
            Button.builder(
                Component.translatable("gui.netmusic.cd_burner.craft")
            ) { b: Button? ->
                handleCraftButton()
            }.pos(leftPos + 7, topPos + 35).size(55, 18).build()
        )
    }

    private fun handleCraftButton() {
        val cd = menu!!.input.getStackInSlot(0)
        if (cd.isEmpty) {
            this.tips = Component.translatable("gui.netmusic.cd_burner.cd_is_empty")
            return
        }

        val songInfo = getSongInfo(cd)
        if (songInfo != null && songInfo.readOnly) {
            this.tips = Component.translatable("gui.netmusic.cd_burner.cd_read_only")
            return
        }

        if (Util.isBlank(textField!!.value)) {
            this.tips = Component.translatable("gui.netmusic.cd_burner.no_music_id")
            return
        }

        if (ID_REG.matcher(textField!!.value).matches()) {
            val id = textField!!.value.toLong()
            try {
                val song = MusicListManage.get163Song(id)
                song.readOnly = readOnlyButton!!.selected()
                NetworkHandler.CHANNEL.sendToServer(SetMusicIDMessage(song))
            } catch (e: Exception) {
                this.tips = Component.translatable("gui.netmusic.cd_burner.get_info_error")
                NetMusic.LOGGER.error("Error", e)
            }

            return
        }

        this.tips = Component.translatable("gui.netmusic.cd_burner.music_id_error")
    }

    override fun renderLabels(graphics: GuiGraphics, x: Int, y: Int) {}

    override fun renderBg(graphics: GuiGraphics, partialTicks: Float, x: Int, y: Int) {
        renderBackground(graphics)
        val posX = this.leftPos
        val posY = (this.height - this.imageHeight) / 2
        graphics.blit(
            BG, posX, posY, 0, 0,
            this.imageWidth,
            this.imageHeight
        )
    }

    override fun render(graphics: GuiGraphics, x: Int, y: Int, partialTicks: Float) {
        super.render(graphics, x, y, partialTicks)
        textField!!.render(graphics, x, y, partialTicks)
        if (Util.isBlank(textField!!.value) && !textField!!.isFocused) {
            graphics.drawString(
                font,
                Component.translatable("gui.netmusic.cd_burner.id.tips").withStyle(ChatFormatting.ITALIC),
                this.leftPos + 12,
                this.topPos + 18,
                ChatFormatting.GRAY.color!!,
                false
            )
        }

        graphics.drawWordWrap(font, tips, this.leftPos + 8, this.topPos + 57, 135, 0xCF0000)
        renderTooltip(graphics, x, y)
    }

    override fun resize(minecraft: Minecraft, width: Int, height: Int) {
        val value = textField!!.value
        super.resize(minecraft, width, height)
        textField!!.value = value
    }

    override fun containerTick() {
        textField!!.tick()
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (textField!!.mouseClicked(mouseX, mouseY, button)) {
            this.focused = this.textField
            return true
        }
        return super.mouseClicked(mouseX, mouseY, button)
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        val mouseKey = InputConstants.getKey(keyCode, scanCode)
        // 防止 E 键关闭界面
        if (getMinecraft().options.keyInventory.isActiveAndMatches(mouseKey) && textField!!.isFocused) {
            return true
        }

        return super.keyPressed(keyCode, scanCode, modifiers)
    }

    override fun insertText(text: String, overwrite: Boolean) {
        if (overwrite) {
            textField!!.value = text
            return
        }

        textField!!.insertText(text)
    }

    companion object {
        private val BG = ResourceLocation(NetMusic.MOD_ID, "textures/gui/cd_burner.png")
        private val ID_REG: Pattern = Pattern.compile("^\\d{4,}$")
        private val URL_1_REG: Pattern = Pattern.compile("^https://music\\.163\\.com/song\\?id=(\\d+).*$")
        private val URL_2_REG: Pattern = Pattern.compile("^https://music\\.163\\.com/#/song\\?id=(\\d+).*$")
    }
}