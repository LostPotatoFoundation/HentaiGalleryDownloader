package Slayer.DownBot;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import javafx.event.ActionEvent;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextArea;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
/*
* This program was created by DrZed/KeldonSlayer/LostPotatoFoundation
* Created for lack of a good program to do what I wanted.
* feel free to modify for your own use..
*/
@SuppressWarnings({"ConstantConditions", "unused"})
public class Controller {
    public TextArea input;
    public CheckBox zip;
    public CheckBox cbz;
    private File outputDir = new File(System.getProperty("user.dir") + "/FakkuDownloads/");

    public void downloadImages(ActionEvent actionEvent) {
        //make sure the links provided match requirements
        if (input != null && input.getText() != null && !input.getText().isEmpty() && input.getText().contains("http") && input.getText().contains("fakku")) {
            if (!outputDir.exists()) {
                //make sure the output dir exists so no io exceptions
                outputDir.mkdirs();
            }
            //get all the links from input
            String[] links = input.getText().split(",");
            if (links.length == 0) links = new String[]{input.getText()};
            for (String link : links) {
                link = link.replace("https", "http").trim();
                if (!link.contains("read")) {
                    link = link + "/read";
                }
                System.out.println(link);
                //for each link grab the gallery
                try (final WebClient webClient = new WebClient(BrowserVersion.FIREFOX_38)) {
                    HtmlPage page = webClient.getPage(link);
                    String galleryName = page.getTitleText();
                    galleryName = galleryName.substring(galleryName.indexOf("Read") + 4, galleryName.indexOf(" - ")).trim();
                    String pageAsXML = page.asXml();
                    //fakku specifically has their gallery thumbnails in an area matching this..
                    String[] pageFragment = pageAsXML.split("<a href=\"#page=");
                    List<String> imagesToDL = new ArrayList<>();
                    for (String fragment : pageFragment) {
                        //make sure I'm only getting the areas containing an image link
                        if (fragment.contains("\" title=\"(Page ")) {
                            if (fragment.contains("</div>")) fragment = fragment.substring(0, fragment.indexOf("</div>"));
                            fragment = fragment.substring(fragment.indexOf("//t.fakku.net"), fragment.lastIndexOf(".jpg"));

                            //add each link to be downloaded from
                            imagesToDL.add(("http:" + fragment + ".jpg").replace(".thumb.jpg", ".jpg").replace("/thumbs/", "/images/"));
                        }
                    }
                    System.out.println(galleryName);
                    File gallery = new File(outputDir, "/" + galleryName + "/");
                    if (!gallery.exists()) {
                        gallery.mkdirs();
                    }
                    System.out.println(gallery);

                    imagesToDL.forEach(img -> {
                        try {
                            URL url = new URL(img);
                            Image image = ImageIO.read(url);
                            File imag = new File(gallery, img.substring(img.lastIndexOf("/")));
                            ImageIO.write(imageToBufferedImage(image), "JPG", imag);
                        } catch (Exception ignored) {
                        }
                    });

                    if (zip.isSelected() && gallery.listFiles() != null && gallery.listFiles().length > 0) {
                        zipFiles(Arrays.asList(gallery.listFiles()), galleryName);
                    }
                } catch (Exception ignored) {}
            }
        }
    }

    private static BufferedImage imageToBufferedImage(final Image img) {
        if (img instanceof BufferedImage) {
            return (BufferedImage) img;
        }

        // Create a buffered image with transparency
        BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);

        // Draw the image on to the buffered image
        Graphics2D bGr = bimage.createGraphics();
        bGr.drawImage(img, 0, 0, null);
        bGr.dispose();

        // Return the buffered image
        return bimage;
    }

    public File zipFiles(List<File> files, String filename) {
        File zipfile = new File(outputDir, filename + (cbz.isSelected() ? ".cbz" : ".zip"));
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
