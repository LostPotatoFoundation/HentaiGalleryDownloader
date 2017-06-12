package lostpotatofoundation.hentaigallerydownloader.gui;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import lostpotatofoundation.hentaigallerydownloader.GalleryDownloadThread;

import javax.imageio.ImageIO;

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

    public void startDownload() {
        if (isRunning) return;
        if (linkText.getText().isEmpty()) return;

        isRunning = true;

        progressBar.setProgress(0.0D);
        downloader = new GalleryDownloadThread(linkText.getText());

        Thread main = new Thread(() -> {
            boolean titleSet = false;
            while (!downloader.isDone()) {
                if (progressBar == null) continue;
                progressBar.setProgress((downloader.getDownloadProgress() + downloader.getCompressionProgress()) / 2.0D);
                if (!downloader.getTitle().isEmpty() && !titleSet) {
                    titleSet = true;
                    titlePart.setText(downloader.getTitle());
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
        });

        main.setDaemon(true);
        main.start();
        downloader.start();
    }

    synchronized boolean isRunning() {
        return isRunning && !downloader.isDone();
    }

    public void parseLinksFromFile() {
        //TODO Read from configured file location (eg. ..downloads/gals.txt) regex http -> whitespace
    }

    public void updateFavourites() {
        //TODO Move from being its own launch to ondemand use + Downloaded from Favs history list
    }

    public void changeTheme() {
        //TODO: Dark Theme/ read from CSS
    }
}
