package lostpotatofoundation.hentaigallerydownloader.gui;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import lostpotatofoundation.hentaigallerydownloader.GalleryDownloadThread;
import lostpotatofoundation.hentaigallerydownloader.SearchParseThread;

import javax.imageio.ImageIO;
import java.util.ArrayDeque;

public class MainController {
    private boolean isRunning;
    private GalleryDownloadThread downloader;

    public TextField linkText;
    public Button startButton;
    public ProgressBar progressBar;
    public ImageView image;
    public Pane pane;
    public TextField title;
    private ArrayDeque<String> links;
    private int ls = 0;

    public void startDownload() {
        if (isRunning) return;
        if (linkText.getText().isEmpty()) return;

        isRunning = true;

        progressBar.setProgress(0.0D);

        Thread main = new Thread(() -> {
            if (!linkText.getText().contains("?")) {
                downloader = new GalleryDownloadThread(linkText.getText());
            } else {
                SearchParseThread t = new SearchParseThread(linkText.getText());
                t.start();
                while (!t.isDone()) {
                    if (progressBar == null) continue;

                    progressBar.setProgress(progressBar.getProgress() + 0.00001D);
                    if (progressBar.getProgress() >= 1) progressBar.setProgress(0);
                }
                links = new ArrayDeque<>(t.getLinks());
                ls = links.size();
            }

            if (links != null && !links.isEmpty()) downloader = new GalleryDownloadThread(links.pop());
            while (links != null && !links.isEmpty()) {
                if (downloader == null) {
                    downloader = new GalleryDownloadThread(links.pop());
                }
                while (!downloader.isDone()) {
                    if (progressBar == null) continue;
                    if (!downloader.getTitle().isEmpty() && !title.getText().equals(downloader.getTitle()))
                        title.setText(downloader.getTitle());
                    double progress = (ls == 0) ?
                            (downloader.getDownloadProgress() + downloader.getCompressionProgress()) / 2.0D :
                            ((downloader.getDownloadProgress() + downloader.getCompressionProgress()) / 2.0D) * (links.size()-ls)/(double)ls;
                    progressBar.setProgress(-progress);

                    try {
                        if (downloader.getImageFile() == null) image.setImage(null);
                        else image.setImage(SwingFXUtils.toFXImage(ImageIO.read(downloader.getImageFile()), null));
                    } catch (Exception e) {
                        //e.printStackTrace();
                    }
                }
                downloader = null;
            }
            progressBar.setProgress(0.0D);
            isRunning = false;
        });

        main.setDaemon(true);
        main.start();
        if (downloader != null)
            downloader.start();
    }

    synchronized boolean isRunning() {
        return isRunning && !downloader.isDone();
    }
}
