package lostpotatofoundation.hentaigallerydownloader;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class GalleryDownloadThread extends Thread {
    private static volatile int downloaderID = 0;

    private String title = "";
    private int pageNumber = 0, imageID = 1, pages = 0, imagesCompressed = 0;
    private boolean done;
    private File imageFile;

    private final String pageURLString;

    public GalleryDownloadThread(String pageURLString) {
        super("Gallery downloader #" + downloaderID++);
        this.pageURLString = pageURLString;
        this.start();
    }

    @Override
    public void run() {
        System.out.println(pageURLString);
        long nano = System.nanoTime();

        parsePage(pageURLString);
        imageFile = null;

        if (Configuration.compress) {
            if (Configuration.compressionType.equalsIgnoreCase("cb7") || Configuration.compressionType.equalsIgnoreCase("7z")) compressGallery_cb7();
            else if (Configuration.compressionType.equalsIgnoreCase("cbz") || Configuration.compressionType.equalsIgnoreCase("zip")) compressGallery_cbz();
            if (Configuration.deleteLoseFiles) deleteLoseArchive();
        }

        nano = System.nanoTime() - nano;
        System.out.println((double) nano / 1000000000.0);

        done = true;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void deleteLoseArchive() {
        File looseFileDirectory = new File(GalleryDownloader.downloadDir, title);
        File[] innerFiles = looseFileDirectory.listFiles();
        if (innerFiles != null) for (File file : innerFiles) file.delete();
        looseFileDirectory.delete();
    }

    private void compressGallery_cb7() {
        if (!new File(Configuration.program7zPath).exists()) throw new RuntimeException("7z.exe path is invalid.");

        try {
            String[] command = new String[]{
                    Configuration.program7zPath,
                    "a",
                    "-t7z",
                    "\"" + GalleryDownloader.downloadDir.getPath().replace("\\", "/") + "/" + title + ".cb7\"",
                    "\"" + GalleryDownloader.downloadDir.getPath().replace("\\", "/") + "/" + title + "/*\""
            };

            Runtime runtime = Runtime.getRuntime();

            final Process p = runtime.exec(command);

            if (Configuration.debug) new Thread(() -> {
                BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
                String line;

                try {
                    while ((line = input.readLine()) != null)
                        System.out.println(line);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

            p.waitFor();
            imagesCompressed = pages;
        } catch (Exception e) {
            if (Configuration.debug) e.printStackTrace();
        }
    }

    private void compressGallery_cbz() {
        try {
            File galleryFolder = new File(GalleryDownloader.downloadDir, title);

            File[] images = galleryFolder.listFiles();
            if (images == null) return;

            ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(new File(GalleryDownloader.downloadDir, title + ".cbz")));
            for (File image : images) {
                imagesCompressed++;
                ZipEntry zipEntry = new ZipEntry(image.getName());

                zipOutputStream.putNextEntry(zipEntry);
                zipOutputStream.write(Files.readAllBytes(image.toPath()));
                zipOutputStream.closeEntry();
            }
            zipOutputStream.close();
        } catch (Exception e) {
            if (Configuration.debug) e.printStackTrace();
        }
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
                Matcher slideMatcher = Pattern.compile(GalleryDownloader.SLIDE_PATTERN).matcher(line), rowMatcher = Pattern.compile(GalleryDownloader.ROWS_PATTERN).matcher(line), pageMatcher = Pattern.compile(GalleryDownloader.PAGES_PATTERN).matcher(line), titleMatcher = Pattern.compile(GalleryDownloader.TITLE_PATTERN).matcher(line);
                boolean pageNumberFound = pageMatcher.find(), rowNumberFound = rowMatcher.find();
                pages = pageNumberFound ? Integer.parseInt(pageMatcher.group().split(" ")[0]) : pages;
                int rows = rowNumberFound ? Integer.parseInt(rowMatcher.group().split(" ")[0]) : 0;
                title = title.isEmpty() && titleMatcher.find() ? titleMatcher.group().split(">")[1].replaceAll(GalleryDownloader.TITLE_PARSE_PATTERN, " ").trim() : title;
                if (title.isEmpty()) continue;

                while (slideMatcher.find()) {
                    parseSlide(new File(GalleryDownloader.downloadDir, title), slideMatcher.group());
                    imageID++;
                }

                if (!pageNumberFound || !rowNumberFound) continue;

                if (imageID > rows * 10 * (pageNumber + 1) && imageID <= pages) {
                    parsePage(pageURLString.replaceAll("\\?p=\\d+", "") + "?p=" + ++pageNumber);
                }
            }
        } catch (Exception e) {
            if (Configuration.debug) e.printStackTrace();
        }
    }

    private void parseSlide(File galleryDir, String urlString) {
        if (Configuration.debug) System.out.println(urlString);
        try {
            if (!galleryDir.exists() && !galleryDir.mkdirs()) throw new RuntimeException("Couldn't create download directory.");

            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.addRequestProperty("Cookie", Configuration.getCookies());

            InputStream inputStream = connection.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "utf-8"));

            LinkedList<String> lineList = new LinkedList<>();
            Stream<String> lines = reader.lines();
            lineList.addAll(lines.collect(Collectors.toList()));

            for (String line : lineList) {
                Matcher matcher = Pattern.compile(GalleryDownloader.IMAGE_PATTERN).matcher(line);

                while (matcher.find()) {
                    downloadImage(galleryDir, matcher.group());
                }
            }

            reader.close();
            inputStream.close();

        } catch (Exception e) {
            if (Configuration.debug) e.printStackTrace();
        }
    }

    private void downloadImage(File galleryDir, String urlString) throws Exception {
        //System.out.println(urlString);
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.addRequestProperty("Cookie", Configuration.getCookies());

        imageFile = new File(galleryDir, imageID +".png");
        if (!imageFile.createNewFile()) return;

        InputStream inputStream = connection.getInputStream();
        FileOutputStream outputStream = new FileOutputStream(imageFile);

        byte[] b = new byte[16384];
        int length;
        while ((length = inputStream.read(b)) != -1) {
            outputStream.write(b, 0, length);
        }
        inputStream.close();
        outputStream.close();
    }

    public synchronized boolean isDone() {
        return done;
    }

    public synchronized double getDownloadProgress() {
        if (pages != 0) return (double) Math.min(imageID, pages) / (double) pages;
        return 0.0D;
    }

    public synchronized double getCompressionProgress() {
        if (pages != 0) return (double) Math.min(imagesCompressed, pages) / (double) pages;
        return 0.0D;
    }

    public synchronized String getTitle() {
        return title;
    }

    public synchronized File getImageFile() {
        return imageFile;
    }
}
