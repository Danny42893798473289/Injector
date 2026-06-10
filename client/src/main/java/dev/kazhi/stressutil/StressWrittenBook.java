package dev.kazhi.stressutil;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.network.Filterable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.WrittenBookContent;

import java.util.ArrayList;
import java.util.List;

public final class StressWrittenBook {
    private StressWrittenBook() {}

    public static ItemStack build(int pageCount, int charsPerPage, boolean jsonPages) {
        List<String> rawPages = StressBookPages.build(pageCount, charsPerPage, jsonPages);
        List<Filterable<Component>> pages = new ArrayList<>(rawPages.size());

        for (String page : rawPages) {
            pages.add(Filterable.passThrough(Component.literal(page)));
        }

        WrittenBookContent content = new WrittenBookContent(
            Filterable.passThrough("Stress Test"),
            "StressTester",
            0,
            pages,
            true
        );

        ItemStack book = new ItemStack(Items.WRITTEN_BOOK);
        book.set(DataComponents.WRITTEN_BOOK_CONTENT, content);
        return book;
    }

    public static ItemStack buildDefault() {
        return build(100, 512, true);
    }
}
