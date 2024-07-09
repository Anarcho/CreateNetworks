package net.anarcho.createnetworks.content.items;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class PunchcardItem extends Item {

    private static final String TAG_INSTRUCTIONS = "Instructions";
    private static final String TAG_CHAINED_CARD = "ChainedCard";
    private static final int GRID_SIZE = 5;

    public PunchcardItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltipComponents, @NotNull TooltipFlag isAdvanced) {
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);

        CompoundTag nbt = stack.getTag();
        if (nbt != null && nbt.contains(TAG_INSTRUCTIONS)) {
            tooltipComponents.add(Component.literal("Programmed Punchcard"));
            if (nbt.contains(TAG_CHAINED_CARD)) {
                tooltipComponents.add(Component.literal("Chained to another card"));
            }
        } else {
            tooltipComponents.add(Component.literal("Blank Punchcard"));
        }
    }

    public static void setInstruction(ItemStack stack, int row, int col, String value) {
        PunchcardData data = getPunchcardData(stack);
        data.setInstruction(row, col, value);
        setPunchcardData(stack, data);
    }

    public static String getInstruction(ItemStack stack, int row, int col) {
        PunchcardData data = getPunchcardData(stack);
        return data.getInstruction(row, col);
    }

    public static PunchcardData getPunchcardData(ItemStack stack) {
        PunchcardData data = new PunchcardData();
        CompoundTag nbt = stack.getTag();
        if (nbt != null && nbt.contains("PunchcardData")) {
            data.deserializeNBT(nbt.getCompound("PunchcardData"));
        }
        return data;
    }

    public static void setPunchcardData(ItemStack stack, PunchcardData data) {
        CompoundTag nbt = stack.getOrCreateTag();
        nbt.put("PunchcardData", data.serializeNBT());
    }

    public static void chainCard(ItemStack stack, ItemStack chainedCard) {
        CompoundTag nbt = stack.getOrCreateTag();
        CompoundTag chainedCardNBT = new CompoundTag();
        chainedCard.save(chainedCardNBT);
        nbt.put(TAG_CHAINED_CARD, chainedCardNBT);
    }

    public static ItemStack getChainedCard(ItemStack stack) {
        CompoundTag nbt = stack.getTag();
        if (nbt != null && nbt.contains(TAG_CHAINED_CARD)) {
            CompoundTag chainedCardNBT = nbt.getCompound(TAG_CHAINED_CARD);
            return ItemStack.of(chainedCardNBT);
        }
        return ItemStack.EMPTY;
    }

    public static ItemStack copyPunchcard(ItemStack original) {
        ItemStack copy = new ItemStack(original.getItem());
        CompoundTag originalNBT = original.getTag();
        if (originalNBT != null) {
            copy.setTag(originalNBT.copy());
        }
        return copy;
    }

    public static boolean isProgrammed(ItemStack stack) {
        CompoundTag nbt = stack.getTag();
        return nbt != null && nbt.contains(TAG_INSTRUCTIONS);
    }

    public static void clearInstructions(ItemStack stack) {
        CompoundTag nbt = stack.getTag();
        if (nbt != null) {
            nbt.remove(TAG_INSTRUCTIONS);
            nbt.remove(TAG_CHAINED_CARD);
        }
    }
}