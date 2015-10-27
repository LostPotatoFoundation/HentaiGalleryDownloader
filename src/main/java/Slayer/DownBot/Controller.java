package Slayer.DownBot;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import javafx.event.ActionEvent;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import static Slayer.DownBot.DownBot.*;
import static Slayer.DownBot.ParsingUtils.*;

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

    public void downloadImages(ActionEvent event) {
        //make sure the links provided match requirements
        if (input != null && input.getText() != null && !input.getText().isEmpty() && input.getText().contains("http") && input.getText().contains("fakku")) {
            if (!outputDir.exists()) {
                //make sure the output dir exists so no io exceptions
                outputDir.mkdirs();
            }
            //get all the links from input
            String[] links = input.getText().split(",");
            if (links.length == 0)
                links = new String[]{input.getText()};

            for (String link : links) {
                link = link.replace("https", "http");
                //for each link grab the gallery
                try (final WebClient webClient = new WebClient(BrowserVersion.FIREFOX_38)) {
                    HtmlPage page = webClient.getPage(link);
                    String pageAsXML = page.asXml();

                    if (link.contains("/read")) {
                        getFakkuGallery(page, zip.isSelected(), cbz.isSelected());
                    } else if (!link.contains("/read") && pageAsXML.contains("<div class=\"book\">")) {
                        String[] pageFragments = pageAsXML.split("<div class=\"book\">");
                        for (String fragment : pageFragments) {
                            if (fragment.contains("<div class=\"book-cover\">")) {
                                fragment = fragment.substring(fragment.indexOf("href=") + 6);
                                fragment = fragment.substring(0, fragment.indexOf("\""));
                                fragment = "https://www.fakku.net" + fragment;
                                getFakkuGallery(webClient.getPage(fragment + "/read"), zip.isSelected(), cbz.isSelected());
                            }
                        }
                    } else if (!link.contains("/read") && link.contains("tags") && pageAsXML.contains("<div class=\"images four columns\">")) {
                        String[] pageFragments = pageAsXML.split("<div class=\"content-row doujinshi row\">");
                        if (pageFragments.length == 0) pageFragments = pageAsXML.split("<div class=\"content-row manga row\">");
                        if (pageFragments.length == 0) break;
                        for (String fragment : pageFragments) {
                            if (fragment.contains("<div class=\"images four columns\">")) {
                                if (fragment.contains("<a href=\"/doujinshi/")) {
                                    fragment = fragment.substring(fragment.indexOf("<a href=\"/doujinshi/") + 9);
                                } else {
                                    fragment = fragment.substring(fragment.indexOf("<a href=\"/manga/") + 9);
                                }
                                fragment = fragment.substring(0, fragment.indexOf("\""));
                                fragment = "http://www.fakku.net" + fragment;
                                getFakkuGallery(webClient.getPage(fragment + "/read"), zip.isSelected(), cbz.isSelected());
                            }
                        }
                    } else {
                        getFakkuGallery(webClient.getPage(link + "/read"), zip.isSelected(), cbz.isSelected());
                    }
                    //http://readhentaimanga.com/siblings-sure-are-great-english/1/1/
                    if (amount.getText().charAt(0) != '-' && Integer.parseInt(amount.getText()) != 0 && pageAsXML.contains("<div class=\"results\">")) {
                        String nlink;
                        for (int i = 0; i < Integer.parseInt(amount.getText()); i++) {
                            if (!link.contains("/page/")) {
                                nlink = link + "/page/" + i + 2;
                            } else {
                                nlink = link + link.substring(0, link.lastIndexOf("/")) + i + 2 + Integer.parseInt(link.substring(link.lastIndexOf("/")).replace("/", ""));
                            }
                            getFakkuGalleries(webClient, webClient.getPage(nlink), zip.isSelected(), cbz.isSelected());
                        }
                    }
                } catch(Exception e){
                    e.printStackTrace();
                }
            }
        }
    }
}
