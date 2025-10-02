package net.firetastesgood.ageofminecraft.fusion;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.ChatFormatting;
import java.util.List;
import java.util.Optional;

public class FusionCrafterScreen extends AbstractContainerScreen<FusionCrafterMenu> {

    private static final ResourceLocation TEX =
            new ResourceLocation("ageofminecraft", "textures/gui/fusion_crafter.png");

    private static final int TEX_W = 256, TEX_H = 256;

    private static final int BG_U = 0, BG_V = 0, BG_W = 176, BG_H = 166;

    private static final int PROG_SRC_U = 176, PROG_SRC_V = 41, PROG_W = 4, PROG_H = 25;
    private static final int MANA_SRC_U = 177, MANA_SRC_V = 1,  MANA_W = 3, MANA_H = 38;
    private static final int ENTR_SRC_U = 182, ENTR_SRC_V = 20, ENTR_W = 3, ENTR_H = 19;

    private static final int PROG_X = 84, PROG_Y = 34;
    private static final int MANA_X = 36, MANA_Y = 17;
    private static final int ENTR_X = 45, ENTR_Y = 36;

    private static final int MAX_MANA = 2_000_000;
    private static final int MAX_ENTROPY = 20_000;

    public FusionCrafterScreen(FusionCrafterMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = BG_W;
        this.imageHeight = BG_H;
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
        this.titleLabelY = 6;
        this.inventoryLabelY = this.imageHeight - 96 + 2;
    }

    @Override
    protected void renderBg(GuiGraphics g, float partial, int mouseX, int mouseY) {
        RenderSystem.setShaderTexture(0, TEX);

        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        g.blit(TEX, x, y, BG_U, BG_V, BG_W, BG_H, TEX_W, TEX_H);

        int prog  = menu.data().get(0);
        int total = Math.max(1, menu.data().get(1));
        int progH = (int)(PROG_H * (prog / (float) total));
        if (progH > 0) {
            g.blit(TEX, x + PROG_X, y + PROG_Y,
                    PROG_SRC_U, PROG_SRC_V,
                    PROG_W, progH, TEX_W, TEX_H);
        }

        float manaStep = MAX_MANA / (float) MANA_H;
        int mana = menu.data().get(2);
        int manaFill = mana <= 0 ? 0 : Math.min(MANA_H, (int)Math.ceil(mana / manaStep));

        if (manaFill > 0) {
            int dv = MANA_H - manaFill;
            g.blit(TEX,
                    x + MANA_X, y + MANA_Y + dv,
                    MANA_SRC_U, MANA_SRC_V + dv,
                    MANA_W, manaFill, TEX_W, TEX_H);
        }

        float entrStep = MAX_ENTROPY / (float) ENTR_H;
        int entr = menu.data().get(3);
        int entrFill = entr <= 0 ? 0 : Math.min(ENTR_H, (int)Math.ceil(entr / entrStep));

        if (entrFill > 0) {
            int dv = ENTR_H - entrFill;
            g.blit(TEX,
                    x + ENTR_X, y + ENTR_Y + dv,
                    ENTR_SRC_U, ENTR_SRC_V + dv,
                    ENTR_W, entrFill, TEX_W, TEX_H);
        }
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partial) {
        renderBackground(g);
        super.render(g, mouseX, mouseY, partial);
        renderTooltip(g, mouseX, mouseY);
        renderBarTooltips(g, mouseX, mouseY);
    }

    private void renderBarTooltips(GuiGraphics g, int mouseX, int mouseY) {
        final Font f = this.font;

        int mana = menu.data().get(2);
        int entr = menu.data().get(3);

        if (isHovering(MANA_X, MANA_Y, MANA_W, MANA_H, mouseX, mouseY)) {
            List<Component> tip = List.of(
                    Component.literal("Mana: " + mana).withStyle(ChatFormatting.AQUA)
            );
            g.renderTooltip(f, tip, Optional.empty(), mouseX, mouseY);
        }
        else if (isHovering(ENTR_X, ENTR_Y, ENTR_W, ENTR_H, mouseX, mouseY)) {
            List<Component> tip = List.of(
                    Component.literal("Entropy: " + entr).withStyle(ChatFormatting.DARK_RED)
            );
            g.renderTooltip(f, tip, Optional.empty(), mouseX, mouseY);
        }
    }
}