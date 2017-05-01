package lostpotatofoundation.hentaigallerydownloader;

import hxckdms.hxcconfig.HxCConfig;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;

public class GalleryDownloader {
    static final String GALLERY_PATTERN = "(?:https?://(ex|g\\.e-)hentai\\.org/g/)([^\"<>]+)";
    static final String SLIDE_PATTERN = "(?:https?://(ex|g\\.e-)hentai\\.org/s/)([^\"<>]+)";
    static final String PAGES_PATTERN = "(?:\\d+) pages";
    static final String ROWS_PATTERN = "(?:\\d+) rows";
    static final String TITLE_PATTERN = "(?:<h1 id=\"gn\")([^<]+)";
    static final String PARODY_PATTERN = "(?:ta_parody:)(?:[a-z,A-Z,0-9,_,-]+[^\"])";

    static final String IMAGE_PATTERN = "(?:http://\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}(:\\d{1,5})?/)[^\"]+";

    static final String TITLE_PARSE_PATTERN = "(\\[.*?\\]|\\{.*?\\}|\\(.*?\\))|(=.*=|~.*~)|([^a-z,A-Z,\\s,\\-,\\~,\\d,\\_])|(\\s{2,}|\\s+\\.)";

    public static File downloadDir = new File(System.getProperty("user.dir"), "DirtyDownloads");

    private static ArrayList<String> galleryLinkList = new ArrayList<>();

    public static void main(String[] args) throws IOException, URISyntaxException, InterruptedException {
        Collections.addAll(galleryLinkList, args);

        HxCConfig mainConfig = new HxCConfig(Configuration.class, "configuration", new File(System.getProperty("user.dir")), "cfg", "galleryDownloader");
        mainConfig.initConfiguration();
        Configuration.initCookies();

        if (!downloadDir.exists()) if (!downloadDir.mkdirs()) throw new RuntimeException("Couldn't create directories.");

        for (String galleryLink : galleryLinkList) {
            new GalleryDownloadThread(galleryLink).start();
        }
    }
}