package net.anarcho.pinsmodaddon.registry;

import com.tterrag.registrate.util.entry.MenuEntry;
import net.anarcho.pinsmodaddon.content.menus.PunchcardProgrammerMenu;
import net.anarcho.pinsmodaddon.content.screens.PunchcardProgrammerScreen;
import static net.anarcho.pinsmodaddon.registry.Registration.REGISTRATE;

public class ModMenuTypes {
    public static final MenuEntry<PunchcardProgrammerMenu> PUNCHCARD_PROGRAMMER =
            REGISTRATE.menu("punchcard_programmer",
                            PunchcardProgrammerMenu::new,
                            () -> PunchcardProgrammerScreen::new)
                    .register();

    public static void register() {
        // This method is called to trigger the static initializers
    }
}
