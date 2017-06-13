package lostpotatofoundation.hentaigallerydownloader.gui;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import lostpotatofoundation.hentaigallerydownloader.Configuration;
import lostpotatofoundation.hentaigallerydownloader.GalleryDownloadThread;

import javax.imageio.ImageIO;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainController {
    public TextField titlePart;
    public Button parseLinkFile;
    public Button updateFavs;
    public Button changeTheme;
    private boolean isRunning;
    private GalleryDownloadThread downloader;

    public TextField linkText;
    public Button startButton;
    public ProgressBar progressBar;
    public ImageView image;
    public Pane pane;

    private Stack<String> linkStack = new Stack<>();
    private short style = 0;
    public void startDownload() {
        if (!linkText.getText().isEmpty())
            startDownload(linkText.getText());
    }

    private void startDownload(String link) {
        if (isRunning) return;

        isRunning = true;

        progressBar.setProgress(0.0D);
        downloader = new GalleryDownloadThread(link);

        Thread main = new Thread(() -> {
            boolean titleSet = false;
            while (!downloader.isDone()) {
                if (progressBar == null) continue;
                progressBar.setProgress((downloader.getDownloadProgress() + downloader.getCompressionProgress()) / 2.0D);
                if (!downloader.getTitle().isEmpty() && !titleSet) {
                    titleSet = true;
                    String t = downloader.getTitle();
                    if (Configuration.attemptEnglish && t.contains("|")) t = t.split("\\|")[1];
                    titlePart.setText(t);
                }

                try {
                    if (downloader.getImageFile() == null) image.setImage(null);
                    else image.setImage(SwingFXUtils.toFXImage(ImageIO.read(downloader.getImageFile()), null));
                } catch (Exception e) {
                    //e.printStackTrace();
                }
            }

            progressBar.setProgress(0.0D);
            titlePart.setText("");
            isRunning = false;

            if (!linkStack.empty())
                startDownload(linkStack.pop());
        });

        main.setDaemon(true);
        main.start();
        downloader.start();
    }

    synchronized boolean isRunning() {
        return isRunning && !downloader.isDone();
    }

    public void parseLinksFromFile() {
        List<String> links = new ArrayList<>();
        File linksFile = new File(Configuration.watchedLinksFilePath);
        final String[] temp = {""};
        try {
            Files.lines(linksFile.toPath(), StandardCharsets.UTF_8).forEach(a -> temp[0] = temp[0].concat(a + " "));
        } catch (Exception i) {
            System.out.println(i.getMessage());
            if (Configuration.debug) i.printStackTrace();
        }
        Matcher m = Pattern.compile("http[a-zA-Z:/.=?0-9]+").matcher(temp[0]);
        while (m.find()) {
            links.add(m.group());
        }
        //TODO
        linkStack.addAll(links);
        if (!isRunning()) {
            startDownload(linkStack.pop());
        }
    }

    public void updateFavourites() {
        //TODO Move from being its own launch to ondemand use + Downloaded from Favs history list
    }

    public void changeTheme() {
        style += 1;
        if (style >= Configuration.cssSheets.size())
            style = 0;
        File f = new File(Configuration.cssSheets.get(style));
        this.pane.getScene().getStylesheets().clear();
        this.pane.getScene().getStylesheets().add("file:///" + f.getAbsolutePath().replace("\\", "/"));
    }
    // TODO
    // Custom gear Icon button Config Screen?
    // Settings for torrent shite, including preferences...
    // Torrent Favs only, all, or batch runs only options
    // Original Image download option if available
    // Move to using 7zip java plugin instead of external lib
    // Download said 7z jar if 7z/cb7 is selected
    // Log File enable option
    // pester karel for more ideas... :)
}
