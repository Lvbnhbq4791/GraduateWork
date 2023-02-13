public class PageEntry implements Comparable<PageEntry> {
    private final String pdfName;//имя
    private final int page;//страница
    private final int count;//количество считаных слов на странице

    public PageEntry(String pdfName, int page, int count) {
        this.pdfName = pdfName;
        this.page = page;
        this.count = count;

    }

    public int getCount() {
        return count;
    }

    @Override
    public int compareTo(PageEntry o) {
        return o.getCount() - this.count;
    }

    @Override
    public String toString() {
        return "PageEntry{" +
                "pdfName='" + pdfName + '\'' +
                ", page=" + page +
                ", count=" + count +
                '}';
    }
}
