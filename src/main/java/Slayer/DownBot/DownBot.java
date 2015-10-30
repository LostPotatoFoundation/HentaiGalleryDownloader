package Slayer.DownBot;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.awt.*;
import java.awt.image.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class DownBot extends Application {
    static final File outputDir = new File(System.getProperty("user.dir") + "/Dloads/");
    static final WebClient webClient = new WebClient(BrowserVersion.FIREFOX_38);

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("botGUI.fxml"));
        primaryStage.setTitle("Hentai Comic Downloader");
        primaryStage.setScene(new Scene(root, 270, 375));
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }


    static BufferedImage imageToBufferedImage(final Image img) {
        if (img instanceof BufferedImage)
            return (BufferedImage) img;

        // Create a buffered image with transparency
        BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB_PRE);

        // Draw the image on to the buffered image
        Graphics2D bGr = bimage.createGraphics();
        bGr.drawImage(img, 0, 0, null);
        bGr.dispose();

        // Return the buffered image
        return bimage;
    }

    static File zipFiles(java.util.List<File> files, String filename, boolean cbz) {
        File zipfile = new File(outputDir, filename + (cbz ? ".cbz" : ".zip"));
        // Create a buffer for reading the files
        byte[] buf = new byte[1024];
        try {
            // create the ZIP file
            ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipfile));
            // compress the files
            for (File file : files) {
                FileInputStream in = new FileInputStream(file.getCanonicalPath());
                // add ZIP entry to output stream
                out.putNextEntry(new ZipEntry(file.getName()));
                // transfer bytes from the file to the ZIP file
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                // complete the entry
                out.closeEntry();
                in.close();
            }
            // complete the ZIP file
            out.close();
            return zipfile;
        } catch (Exception ignored) {}
        return null;
    }
}
