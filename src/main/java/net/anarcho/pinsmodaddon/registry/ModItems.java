package net.anarcho.pinsmodaddon.registry;

import com.tterrag.registrate.util.entry.ItemEntry;
import net.anarcho.pinsmodaddon.content.items.PunchcardItem;

import static net.anarcho.pinsmodaddon.registry.Registration.REGISTRATE;

public class ModItems {
    public static final ItemEntry<PunchcardItem> PUNCHCARD =
            REGISTRATE.item("punchcard", PunchcardItem::new)
                    .properties(p -> p.stacksTo(1))
                    .register();

    public static void register() {
        // This method is called to trigger the static initializers
    }
}