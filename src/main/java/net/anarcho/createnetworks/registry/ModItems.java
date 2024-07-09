package net.anarcho.createnetworks.registry;

import com.tterrag.registrate.util.entry.ItemEntry;
import net.anarcho.createnetworks.content.items.PunchcardItem;

import static net.anarcho.createnetworks.registry.Registration.REGISTRATE;

public class ModItems {
    public static final ItemEntry<PunchcardItem> PUNCHCARD =
            REGISTRATE.item("punchcard", PunchcardItem::new)
                    .properties(p -> p.stacksTo(1))
                    .register();

    public static void register() {
        // This method is called to trigger the static initializers
    }
}