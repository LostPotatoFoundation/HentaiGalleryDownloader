package lostpotatofoundation.hentaigallerydownloader;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SearchParseThread extends Thread {
    private volatile static int parseThreadID = 0;
    private boolean done;

    private final String pageURLString;
    private List<String> links = new ArrayList<>();

    public SearchParseThread(String pageURLString) {
        super("Gallery search parsing thread #" + parseThreadID++);
        this.pageURLString = pageURLString;
    }

    @Override
    public void run() {
        System.out.println(pageURLString);
        long nano = System.nanoTime();

        parsePage(pageURLString);

        nano = System.nanoTime() - nano;
        System.out.println((double) nano / 1000000000.0);
        done = true;
    }
    private void parsePage(String pageURLString) {
        if (Configuration.debug) System.out.println(pageURLString);
        try {
            URL url = new URL(pageURLString);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.addRequestProperty("Cookie", Configuration.getCookies());

            InputStream inputStream = connection.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "utf-8"));


            LinkedList<String> lineList = new LinkedList<>();
            Stream<String> lines = reader.lines();
            lineList.addAll(lines.collect(Collectors.toList()));
            reader.close();
            inputStream.close();


            for (String line : lineList) {
                Matcher galleryMatcher = Pattern.compile(GalleryDownloader.GALLERY_PATTERN).matcher(line);
                while (galleryMatcher.find()) {
                    links.add(galleryMatcher.group());
                }
            }
        } catch (Exception e) {
            if (Configuration.debug) e.printStackTrace();
        }
    }

    public synchronized boolean isDone() {
        return done;
    }

    public synchronized List<String> getLinks() {
        return links;
    }
}
