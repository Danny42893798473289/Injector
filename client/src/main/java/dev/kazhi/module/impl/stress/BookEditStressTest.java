package dev.kazhi.module.impl.stress;

import dev.kazhi.stressutil.StressBookPages;
import net.minecraft.network.protocol.game.ServerboundEditBookPacket;
import net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;
import java.util.Optional;

public class BookEditStressTest extends StressModule {
    public int packetsPerSecond = 50;
    public boolean giveBook = true;
    public boolean openBook = false;
    public boolean signBooks = true;
    public int pages = 100;
    public int charsPerPage = 1024;
    public boolean jsonPages = true;

    private List<String> cachedPages;
    private int pagesSignature;
    private int signToggle;

    public BookEditStressTest() {
        super("Book Edit Stress", "Spams book edit/sign packets with large page payloads.");
    }

    @Override
    protected void onEnable() {
        if (player() == null) {
            return;
        }
        if (!requireConnection()) {
            return;
        }

        invalidatePages();
        signToggle = 0;

        if (giveBook && player().isCreative()) {
            giveWritableBook();
        }

        if (!hasWritableBook()) {
            fail("Hold a writable book in your selected hotbar slot.");
            return;
        }

        if (openBook) {
            MC.gameMode.useItem(player(), InteractionHand.MAIN_HAND);
        }

        info("Book edit stress running (~" + packetsPerSecond + " packets/s).");
    }

    @Override
    public void onTick() {
        if (player() == null || MC.level == null || connection() == null || !hasWritableBook()) {
            return;
        }

        int slot = player().getInventory().getSelectedSlot();
        List<String> pageList = getPages();

        for (int i = 0; i < packetsPerTick(packetsPerSecond); i++) {
            Optional<String> title = Optional.empty();
            if (signBooks && signToggle++ % 2 == 1) {
                title = Optional.of("Stress Test");
            }

            connection().send(new ServerboundEditBookPacket(slot, pageList, title));
        }
    }

    private List<String> getPages() {
        int signature = pages * 31 + charsPerPage * 17 + (jsonPages ? 1 : 0);
        if (cachedPages == null || pagesSignature != signature) {
            cachedPages = StressBookPages.build(pages, charsPerPage, jsonPages);
            pagesSignature = signature;
        }
        return cachedPages;
    }

    private void invalidatePages() {
        cachedPages = null;
        pagesSignature = 0;
    }

    private boolean hasWritableBook() {
        ItemStack stack = player().getMainHandItem();
        return stack.is(Items.WRITABLE_BOOK) || stack.is(Items.WRITTEN_BOOK);
    }

    private void giveWritableBook() {
        int slotId = 36 + player().getInventory().getSelectedSlot();
        connection().send(new ServerboundSetCreativeModeSlotPacket(slotId, new ItemStack(Items.WRITABLE_BOOK)));
    }
}
