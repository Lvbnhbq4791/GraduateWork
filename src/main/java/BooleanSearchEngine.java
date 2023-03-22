import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class BooleanSearchEngine implements SearchEngine {
    protected File pdfsDir;
    protected File stopWords;
    protected Map<String, List<PageEntry>> wordPageEntry;
    protected List<String> listOfstopWords;
    protected Set<String> keyWordPageEntry;

    public BooleanSearchEngine(File pdfsDir, File stopWords) throws IOException {
        this.pdfsDir = pdfsDir;
        this.stopWords = stopWords;
        wordPageEntry = new HashMap<>();
        listOfstopWords = new ArrayList<>();
        keyWordPageEntry = new TreeSet<>();
        filePdfProcessing();
    }

    public void setPdfsDir(File pdfsDir) {
        this.pdfsDir = pdfsDir;
    }

    public void setWordPageEntry(Map<String, List<PageEntry>> wordPageEntry) {
        this.wordPageEntry = wordPageEntry;
    }

    public void setStopWords(File stopWords) {
        this.stopWords = stopWords;
    }

    public void setListOfstopWords(List<String> listOfstopWords) {
        this.listOfstopWords = listOfstopWords;
    }

    public File getPdfsDir() {
        return pdfsDir;
    }

    public Map<String, List<PageEntry>> getWordPageEntry() {
        return wordPageEntry;
    }

    public File getStopWords() {
        return stopWords;
    }

    public List<String> getListOfstopWords() {
        return listOfstopWords;
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
        sortPageEntry();
    }

    public void mapPageEntry(String fileName, int pageNum, Map<String, Integer> freqs) {
        String[] listOfKeys = freqs.keySet().toArray(new String[0]);
        List<String> words = (stopWordsDelete(listOfKeys));
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

    public List<String> stopWordsDelete(String[] listOfKeys) {
        List<String> listOfKeysNew = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(stopWords))) {
            String stopWord;
            while ((stopWord = reader.readLine()) != null) {
                listOfstopWords.add(stopWord.trim());
            }
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
        for (String checkedWord : listOfKeys) {
            checkedWord = checkedWord.toLowerCase();
            if (!listOfstopWords.contains(checkedWord)) {
                listOfKeysNew.add(checkedWord);
            }
        }
        return listOfKeysNew;
    }

    public void sortPageEntry() {
        keyWordPageEntry = wordPageEntry.keySet();
        for (String key : keyWordPageEntry) {
            Collections.sort(wordPageEntry.get(key));
        }
    }

    @Override
    public List<PageEntry> search(String line) {
        List<String> words = (queryWordsProcessing(line));
        if (words.size() == 1) {
            return wordPageEntry.get(words.get(0));
        }
        if (words.size() > 1) {
            return (sumEntry(words));
        }
        return Collections.emptyList();
    }

    public List<String> queryWordsProcessing(String line) {
        line = line.toLowerCase();
        String[] queryWords = line.split("\\P{IsAlphabetic}+");
        Set<String> queryWordsSet = new HashSet<>(Arrays.asList(queryWords));
        List<String> words = new ArrayList<>(queryWordsSet);
        List<String> wordsClone = new ArrayList<>(words);
        for (String word : wordsClone) {
            if (!keyWordPageEntry.contains(word)) {
                words.remove(word);
            }
        }
        return words;
    }

    public List<PageEntry> sumEntry(List<String> words) {
        List<PageEntry> commonList = new ArrayList<>();
        List<PageEntry> wordPageSumEntry = new ArrayList<>();
        for (String word : words) {
            commonList.addAll(wordPageEntry.get(word));
        }
        while (!commonList.isEmpty()) {
            List<PageEntry> intermediateList = commonList.stream()
                    .filter(x -> x.getPdfName().equals(commonList.get(0).getPdfName()))
                    .filter(x -> x.getPage().equals(commonList.get(0).getPage()))
                    .collect(Collectors.toList());
            if (intermediateList.size() > 1) {
                String pdfName = intermediateList.get(0).getPdfName();
                int page = intermediateList.get(0).getPage();
                int sumCount = 0;
                for (PageEntry pageEntry : intermediateList) {
                    sumCount = sumCount + pageEntry.getCount();
                }
                wordPageSumEntry.add(new PageEntry(pdfName, page, sumCount));
            }
            if (intermediateList.size() == 1) {
                wordPageSumEntry.addAll(intermediateList);
            }
            commonList.removeAll(intermediateList);
        }
        Collections.sort(wordPageSumEntry);
        return wordPageSumEntry;
    }
}
