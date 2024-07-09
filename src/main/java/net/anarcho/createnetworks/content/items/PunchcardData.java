package net.anarcho.createnetworks.content.items;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class PunchcardData {
    private static final int GRID_SIZE = 5;
    private static final String NBT_INSTRUCTIONS = "Instructions";

    private final String[][] grid;

    public PunchcardData() {
        this.grid = new String[GRID_SIZE][GRID_SIZE];
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                grid[i][j] = "";
            }
        }
    }

    public void setInstruction(int row, int col, String value) {
        if (isValidPosition(row, col)) {
            grid[row][col] = value;
        }
    }

    public String getInstruction(int row, int col) {
        return isValidPosition(row, col) ? grid[row][col] : "";
    }

    public boolean isValidPosition(int row, int col) {
        return row >= 0 && row < GRID_SIZE && col >= 0 && col < GRID_SIZE;
    }

    public void clear() {
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                grid[i][j] = "";
            }
        }
    }

    public boolean isEmpty() {
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                if (!grid[i][j].isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }

    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        ListTag listTag = new ListTag();

        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                if (!grid[i][j].isEmpty()) {
                    CompoundTag cellTag = new CompoundTag();
                    cellTag.putInt("Row", i);
                    cellTag.putInt("Col", j);
                    cellTag.putString("Value", grid[i][j]);
                    listTag.add(cellTag);
                }
            }
        }

        tag.put(NBT_INSTRUCTIONS, listTag);
        return tag;
    }

    public void deserializeNBT(CompoundTag tag) {
        clear();
        ListTag listTag = tag.getList(NBT_INSTRUCTIONS, 10); // 10 is the ID for CompoundTag

        for (int i = 0; i < listTag.size(); i++) {
            CompoundTag cellTag = listTag.getCompound(i);
            int row = cellTag.getInt("Row");
            int col = cellTag.getInt("Col");
            String value = cellTag.getString("Value");
            setInstruction(row, col, value);
        }
    }

    public boolean isValid() {
        // Implement validation logic here
        // For now, we'll consider it valid if it's not empty
        return !isEmpty();
    }

    public List<Component> getTooltip() {
        List<Component> tooltip = new ArrayList<>();
        tooltip.add(Component.literal("Punchcard Instructions:"));

        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                if (!grid[i][j].isEmpty()) {
                    tooltip.add(Component.literal(String.format("  [%d,%d]: %s", i, j, grid[i][j])));
                }
            }
        }

        return tooltip;
    }
}