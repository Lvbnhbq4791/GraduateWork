import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class BooleanSearchEngine implements SearchEngine {
    File pdfsDir;
    String[] pdfFileName;
    Map<String, Integer> fileNamePages;
    Map<String, Map<String, Integer>> fileNamePage_wordFreqs;
    List<PageEntry> pageEntries;

    public BooleanSearchEngine(File pdfsDir) throws IOException {
        this.pdfsDir = pdfsDir;
        pdfFileName = pdfsDir.list();
        fileNamePages = new HashMap<>();
        fileNamePage_wordFreqs = new HashMap<>();
        pageEntries = new ArrayList<>();
        filePdfProcessing();
    }

    private void filePdfProcessing() throws IOException {
        for (int i = 0; i < Objects.requireNonNull(pdfFileName).length; i++) {
            var doc = new PdfDocument(new PdfReader(pdfsDir + "/" + pdfFileName[i]));
            fileNamePages.put(pdfFileName[i], doc.getNumberOfPages());
            for (int a = 1; a <= doc.getNumberOfPages(); a++) {
                var text = PdfTextExtractor.getTextFromPage(doc.getPage(a));
                var words = text.split("\\P{IsAlphabetic}+");
                Map<String, Integer> freqs = new HashMap<>(); // мапа, где ключом будет слово, а значением - частота
                for (var word : words) { // перебираем слова
                    if (word.isEmpty()) {
                        continue;
                    }
                    word = word.toLowerCase();
                    freqs.put(word, freqs.getOrDefault(word, 0) + 1);
                }
                fileNamePage_wordFreqs.put(pdfFileName[i] + a, freqs);
            }
            doc.close();
        }
    }

    @Override
    public List<PageEntry> search(String word) {
        String searchWord = word.toLowerCase();
        for (String name : pdfFileName) {
            int pages = fileNamePages.get(name);
            for (int i = 1; i <= pages; i++) {
                String keyName = name + i;
                if (fileNamePage_wordFreqs.get(keyName).get(searchWord) != null) {
                    int count = fileNamePage_wordFreqs.get(keyName).get(searchWord);
                    PageEntry pageEntry = new PageEntry(name, i, count);
                    pageEntries.add(pageEntry);
                }
            }
        }
        Collections.sort(pageEntries);
        return pageEntries;
    }
}
