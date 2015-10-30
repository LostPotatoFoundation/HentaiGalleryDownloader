package Slayer.DownBot;

import com.gargoylesoftware.htmlunit.html.HtmlPage;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static Slayer.DownBot.DownBot.*;

@SuppressWarnings("ConstantConditions")
public class ParsingUtils {
    private static int amount;
    private static boolean zip, cbz;
    public ParsingUtils(boolean doZip, boolean doCBZ, int amountOfPages) {
        zip = doZip;
        cbz = doCBZ;
        amount = amountOfPages;
    }

    void parseFakku(String link) throws IOException {
        HtmlPage page = webClient.getPage(link);
        String pageAsXML = page.asXml();
        if (link.contains("/read")) {
            getFakkuGallery(page);
        } else if (!link.contains("/read") && pageAsXML.contains("<div class=\"book\">")) {
            String[] pageFragments = pageAsXML.split("<div class=\"book\">");
            for (String fragment : pageFragments) {
                if (fragment.contains("<div class=\"book-cover\">")) {
                    fragment = fragment.substring(fragment.indexOf("href=") + 6);
                    fragment = fragment.substring(0, fragment.indexOf("\""));
                    fragment = "https://www.fakku.net" + fragment;
                    getFakkuGallery(webClient.getPage(fragment + "/read"));
                }
            }
        } else if (!link.contains("/read") && link.contains("tags") && pageAsXML.contains("<div class=\"images four columns\">")) {
            String[] pageFragments = pageAsXML.split("<div class=\"content-row doujinshi row\">");
            if (pageFragments.length == 0) pageFragments = pageAsXML.split("<div class=\"content-row manga row\">");
            for (String fragment : pageFragments) {
                if (fragment.contains("<div class=\"images four columns\">")) {
                    if (fragment.contains("<a href=\"/doujinshi/")) {
                        fragment = fragment.substring(fragment.indexOf("<a href=\"/doujinshi/") + 9);
                    } else {
                        fragment = fragment.substring(fragment.indexOf("<a href=\"/manga/") + 9);
                    }
                    fragment = fragment.substring(0, fragment.indexOf("\""));
                    fragment = "http://www.fakku.net" + fragment;
                    getFakkuGallery(webClient.getPage(fragment + "/read"));
                }
            }
        } else {
            getFakkuGallery(webClient.getPage(link + "/read"));
        }
        //http://readhentaimanga.com/siblings-sure-are-great-english/1/1/
        if (amount > 0 && pageAsXML.contains("<div class=\"results\">")) {
            String nlink;
            for (int i = 0; i < amount; i++) {
                if (!link.contains("/page/")) {
                    nlink = link + "/page/" + i + 2;
                } else {
                    nlink = link + link.substring(0, link.lastIndexOf("/")) + i + 2 + Integer.parseInt(link.substring(link.lastIndexOf("/")).replace("/", ""));
                }
                getFakkuGalleries(webClient.getPage(nlink));
            }
        }
    }

    @SuppressWarnings("unused")
    void parsePuru(String link) throws IOException {
        HtmlPage page = webClient.getPage(link);
        String pageAsXML = page.asXml();
        //TODO:PURU RETURNS
    }

    void parseEHentai(String link) throws IOException {
        HtmlPage page = webClient.getPage(link);
        String pageAsXML = page.asXml();
        if (link.contains("/g/")) {
            getEHGallery(page);
        } else if (!link.contains("/g/") && pageAsXML.contains("<div class=\"it5\">")) {
            String[] pageFragments = pageAsXML.split("<div class=\"it5\">");
            for (String fragment : pageFragments) {
                if (fragment.contains("<a href=\"http://g.e-hentai.org/g/")) {
                    fragment = fragment.substring(fragment.indexOf("<a href=\"") + 9);
                    fragment = fragment.substring(0, fragment.indexOf("\""));
                    System.out.println(fragment);
                    getEHGallery(webClient.getPage(fragment));
                }
            }
        }

        if (amount > 0 && pageAsXML.contains("<tbody><tr><td class=\"ptdd\">&lt;</td>")) {
            String nlink;
            for (int i = 1; i < amount+1; i++) {
                if (!link.contains("?page=")) {
                    nlink = link + "?page=" + i;
                } else {
                    nlink = link + link.substring(0, link.lastIndexOf("?page=")) + i + 2;
                }
                getEHGalleries(webClient.getPage(nlink));
            }
        }
    }

    void getFakkuGallery(HtmlPage page) {
        String pageAsXML = page.asXml();
        //fakku specifically has their gallery thumbnails in an area matching this..
        String[] pageFragment = pageAsXML.split("<a href=\"#page=");
        String galleryName = page.getTitleText().replaceAll("\"", "''");

        if (!galleryName.isEmpty() && galleryName.contains("Read"))
            galleryName = galleryName.substring(galleryName.indexOf("Read") + 4, galleryName.indexOf(" - ")).trim();

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
        mkGallery(galleryName, imagesToDL);
    }

    void getEHGallery(HtmlPage page) throws IOException {
        String pageAsXML = page.asXml();
        //fakku specifically has their gallery thumbnails in an area matching this..
        String[] pageFragment = pageAsXML.split("<div class=\"gdtm\"");
        String galleryName = page.getTitleText().replaceAll("\"", "''").replace(" - E-Hentai Galleries", "");
        List<String> imagesToDL = new ArrayList<>();
        for (String fragment : pageFragment) {
            //make sure I'm only getting the areas containing an image link
            if (fragment.contains("<a href=\"http://g.e-hentai.org/s/")) {
                fragment = fragment.substring(fragment.indexOf("<a href=\"http://g.e-hentai.org/s/") + 9);
                fragment = fragment.substring(0, fragment.indexOf("\">"));

                HtmlPage p = webClient.getPage(fragment);
                String px = p.asXml();
                px = px.substring(px.indexOf("</iframe>") + 9);
                px = px.substring(px.indexOf("<img src=\"") + 10);
                px = px.substring(0, px.indexOf("\" style=\""));
                imagesToDL.add(px);
            }
        }
        mkGallery(galleryName, imagesToDL);
    }

    void getEHGalleries(HtmlPage page) throws IOException {
        String pageAsXML = page.asXml();
        if (pageAsXML.contains("<tr class=\"gtr")) {
            String[] pageFragments = pageAsXML.split("<tr class=\"gtr");
            for (String fragment : pageFragments) {
                if (fragment.contains("<a href=\"http://g.e-hentai.org/g/")) {
                    fragment = fragment.substring(fragment.indexOf("<a href=\"") + 9);
                    fragment = fragment.substring(0, fragment.indexOf("\""));
                    getEHGallery(webClient.getPage(fragment));
                }
            }
        }
    }

    void getFakkuGalleries(HtmlPage page) {
        String pageAsXML = page.asXml();
        //fakku specifically has their gallery thumbnails in an area matching this..
        String[] pageFragments = pageAsXML.split("<div class=\"content-row doujinshi row\">");
        if (pageFragments.length == 0) pageFragments = pageAsXML.split("<div class=\"content-row manga row\">");
        if (pageFragments.length == 0) return;
        for (String fragment : pageFragments) {
            if (fragment.contains("<div class=\"images four columns\">")) {
                fragment = fragment.substring(fragment.indexOf("href=") + 6);
                fragment = fragment.substring(0, fragment.indexOf("\""));
                fragment = "https://www.fakku.net" + fragment;
                try {
                    getFakkuGallery(webClient.getPage(fragment + "/read"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    void mkGallery(String galleryName, List<String> imagesToDL) {
        galleryName.replaceAll("|", "").replaceAll("%?", "").replaceAll("%*", "").replaceAll("!", "");
        File gallery = new File(outputDir, "/" + galleryName + "/");
        if (!gallery.exists()) {
            gallery.mkdirs();
        }

        imagesToDL.forEach(img -> {
            try {
                URL url = new URL(img);
                Image image = ImageIO.read(url);
//                Image image = Toolkit.getDefaultToolkit().createImage(url);
                File imag = new File(gallery, img.substring(img.lastIndexOf("/")));
                if (img.contains("png"))
                    ImageIO.write(imageToBufferedImage(image), "PNG", imag);
                else if (img.contains("jpg"))
                    ImageIO.write(imageToBufferedImage(image), "JPG", imag);
                else if (img.contains("gif"))
                    ImageIO.write(imageToBufferedImage(image), "GIF", imag);
                else  ImageIO.write(imageToBufferedImage(image), "PNG", imag);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        if (zip && gallery.listFiles() != null && gallery.listFiles().length > 0) {
            zipFiles(Arrays.asList(gallery.listFiles()), galleryName, cbz);
        }
    }
}
