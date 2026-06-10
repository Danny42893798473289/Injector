package dev.kazhi.stressutil;

import java.util.ArrayList;
import java.util.List;

public final class StressBookPages {
    private StressBookPages() {}

    public static List<String> build(int pageCount, int charsPerPage, boolean jsonPages) {
        String content = "A".repeat(charsPerPage);
        String page = jsonPages
            ? "{\"text\":\"" + content + "\"}"
            : content;

        List<String> pages = new ArrayList<>(pageCount);
        for (int i = 0; i < pageCount; i++) {
            pages.add(page);
        }
        return pages;
    }
}
