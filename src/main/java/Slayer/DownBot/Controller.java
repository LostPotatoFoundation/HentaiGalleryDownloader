package Slayer.DownBot;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
/*
* This program was created by DrZed/KeldonSlayer/LostPotatoFoundation
* Created for lack of a good program to do what I wanted.
* feel free to modify for your own use..
*/
@SuppressWarnings({"ConstantConditions", "unused", "unchecked"})
public class Controller {
    public TextArea input;
    public CheckBox zip;
    public CheckBox cbz;
    public ProgressBar progress = new ProgressBar(0);
    public TextField amount;
    private File outputDir = new File(System.getProperty("user.dir") + "/FakkuDownloads/");

    private int nl = 0, nc = 0;

    public void updateProgress() {
        if (nl != 0)
            progress.setProgress((double)(nc/nl));
    }
    public void downloadImages(ActionEvent event) {
        //make sure the links provided match requirements
        if (input != null && input.getText() != null && !input.getText().isEmpty() && input.getText().contains("http") && input.getText().contains("fakku")) {
            if (!outputDir.exists()) {
                //make sure the output dir exists so no io exceptions
                outputDir.mkdirs();
            }
            //get all the links from input
            String[] links = input.getText().split(",");
            if (links.length == 0) links = new String[]{input.getText()};
            nl = links.length;
            for (String link : links) {
                link = link.replace("https", "http");
                //for each link grab the gallery
                try (final WebClient webClient = new WebClient(BrowserVersion.FIREFOX_38)) {
                    HtmlPage page = webClient.getPage(link);
                    String pageAsXML = page.asXml();

                    if (link.contains("/read")) {
                        getGallery(page);
                        updateProgress();
                    } else if (!link.contains("/read") && pageAsXML.contains("<div class=\"book\">")) {
                        String[] pageFragments = pageAsXML.split("<div class=\"book\">");
                        nl += pageFragments.length;
                        for (String fragment : pageFragments) {
                            if (fragment.contains("<div class=\"book-cover\">")) {
                                fragment = fragment.substring(fragment.indexOf("href=") + 6);
                                fragment = fragment.substring(0, fragment.indexOf("\""));
                                fragment = "https://www.fakku.net" + fragment;
                                getGallery(webClient.getPage(fragment + "/read"));
                                nc++;
                                updateProgress();
                            }
                        }
                    } else if (!link.contains("/read") && link.contains("tags") && pageAsXML.contains("<div class=\"images four columns\">")) {
                        String[] pageFragments = pageAsXML.split("<div class=\"content-row doujinshi row\">");
                        if (pageFragments.length == 0) pageFragments = pageAsXML.split("<div class=\"content-row manga row\">");
                        if (pageFragments.length == 0) break;
                        nl += pageFragments.length;
                        for (String fragment : pageFragments) {
                            if (fragment.contains("<div class=\"images four columns\">")) {
                                fragment = fragment.substring(fragment.indexOf("href=") + 6);
                                fragment = fragment.substring(0, fragment.indexOf("\""));
                                fragment = "https://www.fakku.net" + fragment;
                                getGallery(webClient.getPage(fragment + "/read"));
                                nc++;
                                updateProgress();
                            }
                        }
                    } else {
                        getGallery(webClient.getPage(link + "/read"));
                        updateProgress();
                    }
                    nc++;
                    updateProgress();
                    if (amount.getText().charAt(0) != '-' && Integer.parseInt(amount.getText()) != 0 && pageAsXML.contains("<div class=\"results\">")) {
                        String nlink;
                        for (int i = 0; i < Integer.parseInt(amount.getText()); i++) {
                            if (!link.contains("/page/")) {
                                nlink = link + "/page/" + i + 2;
                            } else {
                                nlink = link + link.substring(0, link.lastIndexOf("/")) + i + 2 + Integer.parseInt(link.substring(link.lastIndexOf("/")).replace("/", ""));
                            }
                            getGals(webClient, webClient.getPage(nlink));
                        }
                    }
                } catch(Exception e){
                    e.printStackTrace();
                }
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

    private Random rand = new Random();

    public void getGallery(HtmlPage page) {
        String pageAsXML = page.asXml();
        //fakku specifically has their gallery thumbnails in an area matching this..
        String[] pageFragment = pageAsXML.split("<a href=\"#page=");
        String galleryName = page.getTitleText().replaceAll("\"", "''");
        if (!galleryName.isEmpty() && galleryName.contains("Read")) {
            galleryName = galleryName.substring(galleryName.indexOf("Read") + 4, galleryName.indexOf(" - ")).trim();
        } else if (galleryName.isEmpty()) {
            galleryName = "ERRORED " + rand.nextInt(1000);
        }
        List<String> imagesToDL = new ArrayList<>();
        for (String fragment : pageFragment) {
            //make sure I'm only getting the areas containing an image link
            if (fragment.contains("\" title=\"(Page ")) {
                if (fragment.contains("</div>"))
                    fragment = fragment.substring(0, fragment.indexOf("</div>"));
                fragment = fragment.substring(fragment.indexOf("//t.fakku.net"), fragment.lastIndexOf(".jpg"));

                //add each link to be downloaded from
                imagesToDL.add(("http:" + fragment + ".jpg").replace(".thumb.jpg", ".jpg").replace("/thumbs/", "/images/"));
            }
        }
        File gallery = new File(outputDir, "/" + galleryName + "/");
        if (!gallery.exists()) {
            gallery.mkdirs();
        }

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
        updateProgress();
    }

    public void getGals(WebClient webClient, HtmlPage page) {
        String pageAsXML = page.asXml();
        //fakku specifically has their gallery thumbnails in an area matching this..
        String[] pageFragment = pageAsXML.split("<a href=\"#page=");
        String galleryName = page.getTitleText().replaceAll("\"", "''");
        if (!galleryName.isEmpty() && galleryName.contains("Read")) {
            galleryName = galleryName.substring(galleryName.indexOf("Read") + 4, galleryName.indexOf(" - ")).trim();
        } else if (galleryName.isEmpty()) {
            galleryName = "ERRORED " + rand.nextInt(1000);
        }
        String[] pageFragments = pageAsXML.split("<div class=\"content-row doujinshi row\">");
        if (pageFragments.length == 0) pageFragments = pageAsXML.split("<div class=\"content-row manga row\">");
        if (pageFragments.length == 0) return;
        nl += pageFragments.length;
        for (String fragment : pageFragments) {
            if (fragment.contains("<div class=\"images four columns\">")) {
                fragment = fragment.substring(fragment.indexOf("href=") + 6);
                fragment = fragment.substring(0, fragment.indexOf("\""));
                fragment = "https://www.fakku.net" + fragment;
                try {
                    getGallery(webClient.getPage(fragment + "/read"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                nc++;
                updateProgress();
            }
        }
    }
}
