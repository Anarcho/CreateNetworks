package net.anarcho.createnetworks.content.screens;

import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.create.foundation.gui.AbstractSimiScreen;
import com.simibubi.create.foundation.gui.element.GuiGameElement;
import net.anarcho.createnetworks.CreateNetworks;
import net.anarcho.createnetworks.content.menus.PunchcardProgrammerMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class PunchcardProgrammerScreen extends AbstractSimiScreen implements MenuAccess<PunchcardProgrammerMenu> {

    private static final ResourceLocation TEXTURE = new ResourceLocation(CreateNetworks.MOD_ID, "textures/gui/punchcard_programmer.png");
    private final PunchcardProgrammerMenu menu;
    private static final int GRID_SIZE = 5;
    private static final int GRID_START_X = 44;
    private static final int GRID_START_Y = 17;
    private static final int CELL_SIZE = 18;

    private Button programButton;
    private Button clearButton;
    private Button savePatternButton;
    private Button loadPatternButton;

    public PunchcardProgrammerScreen(PunchcardProgrammerMenu menu, Inventory inventory, Component title) {
        super(title);
        this.menu = menu;
    }

    @Override
    protected void init() {
        super.init();
        setWindowSize(176, 166);

        int x = (width - windowWidth) / 2;
        int y = (height - windowHeight) / 2;

        programButton = this.addRenderableWidget(Button.builder(Component.literal("Program"), (button) -> program())
                .bounds(x + 10, y + 120, 60, 20).build());
        clearButton = this.addRenderableWidget(Button.builder(Component.literal("Clear"), (button) -> clear())
                .bounds(x + 75, y + 120, 60, 20).build());
        savePatternButton = this.addRenderableWidget(Button.builder(Component.literal("Save"), (button) -> savePattern())
                .bounds(x + 10, y + 145, 60, 20).build());
        loadPatternButton = this.addRenderableWidget(Button.builder(Component.literal("Load"), (button) -> loadPattern())
                .bounds(x + 75, y + 145, 60, 20).build());
    }

    @Override
    protected void renderWindow(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        RenderSystem.setShaderTexture(0, TEXTURE);
        int x = (width - windowWidth) / 2;
        int y = (height - windowHeight) / 2;
        graphics.blit(TEXTURE, x, y, 0, 0, windowWidth, windowHeight);

        renderProgrammingGrid(graphics, x, y, mouseX, mouseY);
        renderEnergyBar(graphics, x, y);
        renderItemSlots(graphics, x, y);

        renderTooltips(graphics, mouseX, mouseY);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltips(graphics, mouseX, mouseY);
    }

    private void renderProgrammingGrid(GuiGraphics graphics, int x, int y, int mouseX, int mouseY) {
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                int cellX = x + GRID_START_X + col * CELL_SIZE;
                int cellY = y + GRID_START_Y + row * CELL_SIZE;
                graphics.blit(TEXTURE, cellX, cellY, 176, 0, CELL_SIZE, CELL_SIZE);

                String cellValue = menu.getProgramGridCell(row, col);
                if (!cellValue.isEmpty()) {
                    graphics.drawString(font, cellValue, cellX + 2, cellY + 5, 0xFFFFFF, false);
                }

                if (mouseX >= cellX && mouseX < cellX + CELL_SIZE && mouseY >= cellY && mouseY < cellY + CELL_SIZE) {
                    graphics.fill(cellX, cellY, cellX + CELL_SIZE, cellY + CELL_SIZE, 0x80FFFFFF);
                }
            }
        }
    }

    private void renderEnergyBar(GuiGraphics graphics, int x, int y) {
        menu.contentHolder.getCapability(ForgeCapabilities.ENERGY).ifPresent(energy -> {
            int energyStored = energy.getEnergyStored();
            int maxEnergy = energy.getMaxEnergyStored();
            int energyHeight = energyStored * 50 / maxEnergy;
            graphics.blit(TEXTURE, x + 156, y + 17 + (50 - energyHeight), 176, 18, 12, energyHeight);
        });
    }

    private void renderItemSlots(GuiGraphics graphics, int x, int y) {
        GuiGameElement.of(menu.contentHolder.getInventory().getStackInSlot(0))
                .at(x + 27, y + 47)
                .render(graphics);
        GuiGameElement.of(menu.contentHolder.getInventory().getStackInSlot(1))
                .at(x + 134, y + 47)
                .render(graphics);
    }

    private void renderTooltips(GuiGraphics graphics, int mouseX, int mouseY) {
        int x = (width - windowWidth) / 2;
        int y = (height - windowHeight) / 2;

        // Energy bar tooltip
        if (mouseX >= x + 156 && mouseX < x + 168 && mouseY >= y + 17 && mouseY < y + 67) {
            List<Component> tooltip = new ArrayList<>();
            tooltip.add(Component.literal("Energy: " + menu.getEnergyStored() + " FE"));
            graphics.renderComponentTooltip(font, tooltip, mouseX, mouseY);
        }

        // Grid cell tooltips
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                int cellX = x + GRID_START_X + col * CELL_SIZE;
                int cellY = y + GRID_START_Y + row * CELL_SIZE;
                if (mouseX >= cellX && mouseX < cellX + CELL_SIZE && mouseY >= cellY && mouseY < cellY + CELL_SIZE) {
                    List<Component> tooltip = new ArrayList<>();
                    tooltip.add(Component.literal("Row: " + (row + 1) + ", Column: " + (col + 1)));
                    tooltip.add(Component.literal("Value: " + menu.getProgramGridCell(row, col)));
                    graphics.renderComponentTooltip(font, tooltip, mouseX, mouseY);
                }
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int x = (width - windowWidth) / 2;
        int y = (height - windowHeight) / 2;

        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                int cellX = x + GRID_START_X + col * CELL_SIZE;
                int cellY = y + GRID_START_Y + row * CELL_SIZE;
                if (mouseX >= cellX && mouseX < cellX + CELL_SIZE && mouseY >= cellY && mouseY < cellY + CELL_SIZE) {
                    handleCellClick(row, col);
                    return true;
                }
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void handleCellClick(int row, int col) {
        String currentValue = menu.getProgramGridCell(row, col);
        String newValue = currentValue.isEmpty() ? "X" : "";
        menu.setProgramGridCell(row, col, newValue);
    }

    private void program() {
        menu.programPunchcard();
    }

    private void clear() {
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                menu.setProgramGridCell(row, col, "");
            }
        }
    }

    private void savePattern() {
        // Implement save pattern logic
    }

    private void loadPattern() {
        // Implement load pattern logic
    }

    @Override
    public @NotNull PunchcardProgrammerMenu getMenu() {
        return this.menu;
    }
}
