public class PageEntry implements Comparable<PageEntry> {
    private final String pdfName;//имя
    private final Integer page;//страница
    private final Integer count;//количество считаных слов на странице

    public PageEntry(String pdfName, int page, int count) {
        this.pdfName = pdfName;
        this.page = page;
        this.count = count;

    }

    public String getPdfName() {
        return pdfName;
    }

    public Integer getPage() {
        return page;
    }

    public Integer getCount() {
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
