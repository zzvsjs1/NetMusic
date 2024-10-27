package com.github.tartaricacid.netmusic.client.gui

import com.github.tartaricacid.netmusic.NetMusic
import com.github.tartaricacid.netmusic.inventory.ComputerMenu
import com.github.tartaricacid.netmusic.item.ItemMusicCD.Companion.getSongInfo
import com.github.tartaricacid.netmusic.item.ItemMusicCD.SongInfo
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
import java.net.MalformedURLException
import java.nio.file.Paths
import java.util.regex.Pattern

@IPNIgnore
class ComputerMenuScreen(
    screenContainer: ComputerMenu,
    inv: Inventory,
    titleIn: Component
) : AbstractContainerScreen<ComputerMenu?>(screenContainer, inv, titleIn) {

    private var urlTextField: EditBox? = null

    private var nameTextField: EditBox? = null

    private var timeTextField: EditBox? = null

    private var readOnlyButton: Checkbox? = null

    private var tips: Component = Component.empty()

    companion object {
        private val BG = ResourceLocation(NetMusic.MOD_ID, "textures/gui/computer.png")
        private val URL_HTTP_REG: Pattern =
            Pattern.compile("(http|ftp|https)://[\\w\\-_]+(\\.[\\w\\-_]+)+([\\w\\-.,@?^=%&:/~+#]*[\\w\\-@?^=%&/~+#])?")
        private val URL_FILE_REG: Pattern =
            Pattern.compile("^[a-zA-Z]:\\\\(?:[^\\\\/:*?\"<>|\\r\\n]+\\\\)*[^\\\\/:*?\"<>|\\r\\n]*$")
        private val TIME_REG: Pattern = Pattern.compile("^\\d+$")
    }

    init {
        this.imageHeight = 216
    }

    override fun init() {
        super.init()

        this.initUrlEditBox()
        this.initNameEditBox()
        this.initTimeEditBox()
        this.readOnlyButton = Checkbox(
            leftPos + 58, topPos + 55, 80, 20,
            Component.translatable("gui.netmusic.cd_burner.read_only"), false
        )

        this.addRenderableWidget(this.readOnlyButton)
        this.addRenderableWidget(
            Button.builder(
                Component.translatable("gui.netmusic.cd_burner.craft")
            ) { b: Button? -> handleCraftButton() }
                .pos(leftPos + 7, topPos + 78).size(135, 18).build())
    }

    private fun initUrlEditBox() {
        var perText = ""
        var focus = false
        if (urlTextField != null) {
            perText = urlTextField!!.value
            focus = urlTextField!!.isFocused
        }

        urlTextField =
            EditBox(getMinecraft().font, leftPos + 10, topPos + 18, 120, 16, Component.literal("Music URL Box"))
        urlTextField!!.value = perText
        urlTextField!!.setBordered(false)
        urlTextField!!.setMaxLength(32500)
        urlTextField!!.setTextColor(0xF3EFE0)
        urlTextField!!.isFocused = focus
        urlTextField!!.moveCursorToEnd()
        this.addWidget(this.urlTextField)
    }

    private fun initNameEditBox() {
        var perText = ""
        var focus = false
        if (nameTextField != null) {
            perText = nameTextField!!.value
            focus = nameTextField!!.isFocused
        }
        nameTextField =
            EditBox(getMinecraft().font, leftPos + 10, topPos + 39, 120, 16, Component.literal("Music Name Box"))
        nameTextField!!.value = perText
        nameTextField!!.setBordered(false)
        nameTextField!!.setMaxLength(256)
        nameTextField!!.setTextColor(0xF3EFE0)
        nameTextField!!.isFocused = focus
        nameTextField!!.moveCursorToEnd()
        this.addWidget(this.nameTextField)
    }

    private fun initTimeEditBox() {
        var perText = ""
        var focus = false
        if (timeTextField != null) {
            perText = timeTextField!!.value
            focus = timeTextField!!.isFocused
        }
        timeTextField =
            EditBox(getMinecraft().font, leftPos + 10, topPos + 61, 40, 16, Component.literal("Music Time Box"))
        timeTextField!!.value = perText
        timeTextField!!.setBordered(false)
        timeTextField!!.setMaxLength(5)
        timeTextField!!.setTextColor(0xF3EFE0)
        timeTextField!!.isFocused = focus
        timeTextField!!.moveCursorToEnd()
        this.addWidget(this.timeTextField)
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

        val urlText = urlTextField!!.value
        if (urlText.isNullOrBlank()) {
            this.tips = Component.translatable("gui.netmusic.computer.url.empty")
            return
        }

        val nameText = nameTextField!!.value
        if (nameText.isNullOrBlank()) {
            this.tips = Component.translatable("gui.netmusic.computer.name.empty")
            return
        }

        val timeText = timeTextField!!.value
        if (timeText.isNullOrBlank()) {
            this.tips = Component.translatable("gui.netmusic.computer.time.empty")
            return
        }

        if (timeText.any { !it.isDigit() }) {
            this.tips = Component.translatable("gui.netmusic.computer.time.not_number")
            return
        }

        val time: Int = timeText.toInt()

        if (URL_HTTP_REG.matcher(urlText).matches()) {
            val song = SongInfo(urlText, nameText, time, readOnlyButton!!.selected())
            NetworkHandler.CHANNEL.sendToServer(SetMusicIDMessage(song))
            return
        }

        if (URL_FILE_REG.matcher(urlText).matches()) {
            val file = Paths.get(urlText).toFile()
            if (!file.isFile) {
                this.tips = Component.translatable("gui.netmusic.computer.url.local_file_error")
                return
            }

            try {
                val url = file.toURI().toURL()
                val song = SongInfo(url.toString(), nameText, time, readOnlyButton!!.selected())
                NetworkHandler.CHANNEL.sendToServer(SetMusicIDMessage(song))
                return
            } catch (e: MalformedURLException) {
                NetMusic.LOGGER.error("Error", e)
            }
        }

        this.tips = Component.translatable("gui.netmusic.computer.url.error")
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
        urlTextField!!.render(graphics, x, y, partialTicks)
        nameTextField!!.render(graphics, x, y, partialTicks)
        timeTextField!!.render(graphics, x, y, partialTicks)

        if (Util.isBlank(urlTextField!!.value) && !urlTextField!!.isFocused) {
            graphics.drawString(
                font,
                Component.translatable("gui.netmusic.computer.url.tips").withStyle(ChatFormatting.ITALIC),
                this.leftPos + 12,
                this.topPos + 18,
                ChatFormatting.GRAY.color!!, false
            )
        }

        if (Util.isBlank(nameTextField!!.value) && !nameTextField!!.isFocused) {
            graphics.drawString(
                font, Component.translatable("gui.netmusic.computer.name.tips").withStyle(ChatFormatting.ITALIC),
                this.leftPos + 12,
                this.topPos + 39,
                ChatFormatting.GRAY.color!!, false
            )
        }

        if (Util.isBlank(timeTextField!!.value) && !timeTextField!!.isFocused) {
            graphics.drawString(
                font, Component.translatable("gui.netmusic.computer.time.tips").withStyle(ChatFormatting.ITALIC),
                this.leftPos + 11,
                this.topPos + 61,
                ChatFormatting.GRAY.color!!, false
            )
        }

        graphics.drawWordWrap(font, tips, this.leftPos + 8, this.topPos + 100, 162, 0xCF0000)
        renderTooltip(graphics, x, y)
    }

    override fun resize(minecraft: Minecraft, width: Int, height: Int) {
        val urlValue = urlTextField!!.value
        val nameValue = nameTextField!!.value
        val timeValue = timeTextField!!.value
        super.resize(minecraft, width, height)
        urlTextField!!.value = urlValue
        nameTextField!!.value = nameValue
        timeTextField!!.value = timeValue
    }

    override fun containerTick() {
        urlTextField!!.tick()
        nameTextField!!.tick()
        timeTextField!!.tick()
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (urlTextField!!.mouseClicked(mouseX, mouseY, button)) {
            this.focused = this.urlTextField
            return true
        }

        if (nameTextField!!.mouseClicked(mouseX, mouseY, button)) {
            this.focused = this.nameTextField
            return true
        }

        if (timeTextField!!.mouseClicked(mouseX, mouseY, button)) {
            this.focused = this.timeTextField
            return true
        }

        return super.mouseClicked(mouseX, mouseY, button)
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        val mouseKey = InputConstants.getKey(keyCode, scanCode)

        // Prevent E to close this menu.
        if (minecraft!!.options.keyInventory.isActiveAndMatches(mouseKey)) {
            if (urlTextField!!.isFocused || nameTextField!!.isFocused || timeTextField!!.isFocused) {
                return true
            }
        }

        return super.keyPressed(keyCode, scanCode, modifiers)
    }

    override fun insertText(text: String, overwrite: Boolean) {
        if (overwrite) {
            urlTextField!!.value = text
            nameTextField!!.value = text
            timeTextField!!.value = text
            return
        }

        urlTextField!!.insertText(text)
        nameTextField!!.insertText(text)
        timeTextField!!.insertText(text)
    }

}