import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class BooleanSearchEngine implements SearchEngine {
    protected File pdfsDir;
    protected Map<String, List<PageEntry>> wordPageEntry;

    public BooleanSearchEngine(File pdfsDir) throws IOException {
        this.pdfsDir = pdfsDir;
        wordPageEntry = new HashMap<>();
        filePdfProcessing();
    }

    public void setPdfsDir(File pdfsDir) {
        this.pdfsDir = pdfsDir;
    }

    public void setWordPageEntry(Map<String, List<PageEntry>> wordPageEntry) {
        this.wordPageEntry = wordPageEntry;
    }

    public File getPdfsDir() {
        return pdfsDir;
    }

    public Map<String, List<PageEntry>> getWordPageEntry() {
        return wordPageEntry;
    }

    private void filePdfProcessing() throws IOException {
        String[] pdfFileName = pdfsDir.list();
        for (int i = 0; i < Objects.requireNonNull(pdfFileName).length; i++) {
            var doc = new PdfDocument(new PdfReader(pdfsDir + "/" + pdfFileName[i]));
            for (int pageNum = 1; pageNum <= doc.getNumberOfPages(); pageNum++) {
                var text = PdfTextExtractor.getTextFromPage(doc.getPage(pageNum));
                var words = text.split("\\P{IsAlphabetic}+");
                Map<String, Integer> freqs = new HashMap<>(); // мапа, где ключом будет слово, а значением - частота
                for (var word : words) { // перебираем слова
                    if (word.isEmpty()) {
                        continue;
                    }
                    word = word.toLowerCase();
                    freqs.put(word, freqs.getOrDefault(word, 0) + 1);
                }
                mapPageEntry(pdfFileName[i], pageNum, freqs);
            }
            doc.close();
        }
    }

    public void mapPageEntry(String fileName, int pageNum, Map<String, Integer> freqs) {
        String[] words = freqs.keySet().toArray(new String[0]);
        for (String word : words) {
            if (wordPageEntry.containsKey(word)) {
                List<PageEntry> pageEntry = wordPageEntry.get(word);
                pageEntry.add(new PageEntry(fileName, pageNum, freqs.get(word)));
                wordPageEntry.put(word, pageEntry);
            } else {
                List<PageEntry> pageEntry = new ArrayList<>();
                pageEntry.add(new PageEntry(fileName, pageNum, freqs.get(word)));
                wordPageEntry.put(word, pageEntry);
            }
        }
    }

    @Override
    public List<PageEntry> search(String word) {
        try {
            word = word.toLowerCase();
            List<PageEntry> pageEntries;
            pageEntries = wordPageEntry.get(word);
            Collections.sort(pageEntries);
            return pageEntries;
        } catch (RuntimeException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }
}
