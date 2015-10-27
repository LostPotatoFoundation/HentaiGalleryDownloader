package Slayer.DownBot;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static Slayer.DownBot.DownBot.*;

public class ParsingUtils {
    static Random rand = new Random();

    static void getFakkuGallery(HtmlPage page, boolean zip, boolean cbz) {
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

        if (zip && gallery.listFiles() != null && gallery.listFiles().length > 0) {
            zipFiles(Arrays.asList(gallery.listFiles()), galleryName, cbz);
        }
    }

    static void getFakkuGalleries(WebClient webClient, HtmlPage page, boolean zip, boolean cbz) {
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
        for (String fragment : pageFragments) {
            if (fragment.contains("<div class=\"images four columns\">")) {
                fragment = fragment.substring(fragment.indexOf("href=") + 6);
                fragment = fragment.substring(0, fragment.indexOf("\""));
                fragment = "https://www.fakku.net" + fragment;
                try {
                    getFakkuGallery(webClient.getPage(fragment + "/read"), zip, cbz);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
