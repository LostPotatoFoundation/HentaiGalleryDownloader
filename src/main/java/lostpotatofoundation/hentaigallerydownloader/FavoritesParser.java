package lostpotatofoundation.hentaigallerydownloader;

import hxckdms.hxcconfig.HxCConfig;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FavoritesParser {
    private static LinkedHashMap<String, String> favoriteGalleries = new LinkedHashMap<>();

    public static void main(String[] args) throws IOException {
        HxCConfig mainConfig = new HxCConfig(Configuration.class, "configuration", new File(System.getProperty("user.dir")), "cfg", "galleryDownloader");
        mainConfig.initConfiguration();
        Configuration.initCookies();

        URL url = new URL("https://exhentai.org/favorites.php");

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.addRequestProperty("Cookie", Configuration.getCookies());

        InputStream inputStream = connection.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "utf-8"));


        LinkedList<String> lineList = new LinkedList<>();
        Stream<String> lines = reader.lines();
        lineList.addAll(lines.collect(Collectors.toList()));
        lines.close();
        reader.close();
        inputStream.close();
        connection.disconnect();

        String pages = "";
        int galleriesPerPage = -1;
        int totalGalleries = -1;

        for (String line : lineList) {
            Matcher galleryMatcher = Pattern.compile("(https?:\\/\\/exhentai\\.org\\/g\\/[^<]+\\/[^<]+)").matcher(line);
            Matcher test = Pattern.compile("(?!Showing\\s)(?:\\d+\\-\\d+)\\sof\\s(?:\\d+)").matcher(line);

            if (test.find() && pages.isEmpty()) pages = test.group();

            boolean keep = false;
            while (galleryMatcher.find()) {
                String gallery = galleryMatcher.group();
                if (keep = !keep) favoriteGalleries.put(gallery.split("\">")[0], gallery.split("\">")[1]);
            }

            if (pages.isEmpty()) continue;
            galleriesPerPage = Integer.parseInt(pages.split(" ")[0].split("-")[1]);
            totalGalleries = Integer.parseInt(pages.split(" ")[2]);
        }

        if (totalGalleries > galleriesPerPage) {
            int page = 1;
            do {
                URL url2 = new URL("https://exhentai.org/favorites.php?page=" + page);

                HttpURLConnection connection2 = (HttpURLConnection) url2.openConnection();
                connection2.addRequestProperty("Cookie", Configuration.getCookies());

                InputStream inputStream2 = connection2.getInputStream();
                BufferedReader reader2 = new BufferedReader(new InputStreamReader(inputStream2, "utf-8"));

                lineList.clear();
                Stream<String> lines2 = reader2.lines();
                lineList.addAll(lines2.collect(Collectors.toList()));
                lines2.close();
                reader2.close();
                inputStream2.close();
                connection2.disconnect();

                for (String line : lineList) {
                    Matcher galleryMatcher = Pattern.compile("(https?:\\/\\/exhentai\\.org\\/g\\/[^<]+\\/[^<]+)").matcher(line);

                    boolean keep = false;
                    while (galleryMatcher.find()) {
                        String gallery = galleryMatcher.group();


                        if (keep = !keep) favoriteGalleries.put(gallery.split("\">")[0], gallery.split("\">")[1]);
                    }
                }
            } while (totalGalleries > ++page * galleriesPerPage);
        }

        favoriteGalleries = favoriteGalleries.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

        System.out.println(galleriesPerPage);
        System.out.println(totalGalleries);
        favoriteGalleries.forEach((k, v) -> System.out.println(k + " = " + v));
        System.out.println(favoriteGalleries.size());

        for (String key : favoriteGalleries.keySet()) {
            processGalleryTorrent(getGalleryTorrentLink(new URL(key)));
        }

        //processGalleryTorrent(new URL("https://exhentai.org/gallerytorrents.php?gid=1054332&t=3ca70bfcc0"));

        //GalleryDownloadThread[] test = new GalleryDownloadThread[4];

        //for (String link : favoriteGalleries.keySet()) {
        //    A: while (true) {
        //        for (int i = 0; i < test.length; i++) {
        //            if (test[i] == null || test[i].isDone()) {
        //                test[i] = new GalleryDownloadThread(link);
        //                test[i].start();
        //                break A;
        //            }
        //        }
        //    }
        //}
    }

    private static URL getGalleryTorrentLink(URL url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.addRequestProperty("Cookie", Configuration.getCookies());

        InputStream inputStream = connection.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "utf-8"));


        LinkedList<String> lineList = new LinkedList<>();
        Stream<String> lines = reader.lines();
        lineList.addAll(lines.collect(Collectors.toList()));
        reader.close();
        inputStream.close();
        connection.disconnect();

        for (String line : lineList) {
            Matcher torrentLocationMatcher = Pattern.compile("(?!popUp\\(\\')(?:h[^']+gallerytorrents[^']+)").matcher(line);

            if (torrentLocationMatcher.find()) return new URL(torrentLocationMatcher.group().replace("&amp;", "&"));
        }

        return null;
    }

    private static void processGalleryTorrent(URL torrentLink) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) torrentLink.openConnection();
        connection.addRequestProperty("Cookie", Configuration.getCookies());

        InputStream inputStream = connection.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "utf-8"));


        LinkedList<String> lineList = new LinkedList<>();
        Stream<String> lines = reader.lines();
        lineList.addAll(lines.collect(Collectors.toList()));
        reader.close();
        inputStream.close();
        connection.disconnect();

        String site = String.join("", lineList);

        String[] splitSite = site.split("<form method=\"post\"");

        if (splitSite.length - 1 < 2) {
            System.out.println("non");
            return;
        }

        System.out.println(torrentLink);

        String mostRecentTorrent = "";
        Date mostRecent = new Date(0);

        String largestTorrent = "";
        double largestSize = -1D;

        String mostDownloadedTorrent = "";
        int mostDownloads = -1;

        String mostSeededTorrent = "";
        int mostSeeds = -1;

        for (int i = 1; i < splitSite.length - 1; i++) {
            Matcher date = Pattern.compile("(\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2})").matcher(splitSite[i]);
            Matcher size = Pattern.compile("(\\d{1,3}\\.\\d{1,2} ([KMG])B)").matcher(splitSite[i]);
            Matcher seeds = Pattern.compile("(Seeds:</span> \\d+)").matcher(splitSite[i]);
            Matcher downloads = Pattern.compile("(Downloads:</span> \\d+)").matcher(splitSite[i]);
            Matcher torrent = Pattern.compile("(?:https?://ehtracker[^\"]+)").matcher(splitSite[i]);

            if (!torrent.find()) continue;
            String currentTorrent = torrent.group();

            if (date.find()) {
                SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd hh:mm");
                try {
                    Date parsedDate = parser.parse(date.group());
                    if (parsedDate.after(mostRecent)) {
                        mostRecent = parsedDate;
                        mostRecentTorrent = currentTorrent;
                    }
                } catch (ParseException ignored) {}
            }

            if (size.find()) {
                double currentSize = Double.parseDouble(size.group().replace(" MB", ""));

                if (currentSize > largestSize) {
                    largestSize = currentSize;
                    largestTorrent = currentTorrent;
                }
            }

            if (seeds.find()) {
                int currentSeeds = Integer.parseInt(seeds.group().split("</span> ")[1]);

                if (currentSeeds > mostSeeds) {
                    mostSeeds = currentSeeds;
                    mostSeededTorrent = currentTorrent;
                }
            }

            if (downloads.find()) {
                int currentDownloads = Integer.parseInt(downloads.group().split("</span> ")[1]);

                if (currentDownloads > mostDownloads) {
                    mostDownloads = currentDownloads;
                    mostDownloadedTorrent = currentTorrent;
                }
            }
        }
        System.out.println("Most recent: " + mostRecent);
        System.out.println(mostRecentTorrent);

        System.out.println("Largest size: " + largestSize);
        System.out.println(largestTorrent);

        System.out.println("Most seeds: " + mostSeeds);
        System.out.println(mostSeededTorrent);

        System.out.println("Most Downloads: " + mostDownloads);
        System.out.println(mostDownloadedTorrent);

        openTorrent(largestTorrent);
    }

    private static void openTorrent(String torrentDownload) {
        try {
            Runtime runtime = Runtime.getRuntime();

            final Process p = runtime.exec(
                    "C:\\Users\\Sietse\\AppData\\Roaming\\uTorrent\\uTorrent.exe /DIRECTORY " +
                    "\"D:\\Development\\IdeaProjects\\HentaiGalleryDownloader\\DirtyDownloads\\\" " +
                    "\"" + torrentDownload + "\"");

            p.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
