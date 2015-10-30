package Slayer.DownBot;

import javafx.event.ActionEvent;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import static Slayer.DownBot.DownBot.outputDir;

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
    public TextField amount;
    ParsingUtils p;

    public void downloadImages(ActionEvent event) {
        //make sure the links provided match requirements
        if (input != null && input.getText() != null && !input.getText().isEmpty() && input.getText().contains("http")) {
            if (!outputDir.exists()) {
                //make sure the output dir exists so no io exceptions
                outputDir.mkdirs();
            }
            p = new ParsingUtils(doZip(), doCBZ(), getAmount());
            //get all the links from input
            String[] links = input.getText().split(",");
            if (links.length == 0)
                links = new String[]{input.getText()};

            for (String link : links) {
                link = link.replace("https", "http");
                //for each link grab the gallery
                try {
                    if (link.contains("fakku.net")) {
                        p.parseFakku(link);
                    } else if (link.contains("g.e-hentai.org")) {
                        p.parseEHentai(link);
                    } else if (link.contains("exhentai.org")) {
                        link = link.replace("exhentai", "g.e-hentai");
                        p.parseEHentai(link);
                    }
                } catch (Exception e) {e.printStackTrace();}
            }
        }
    }
    
    public boolean doZip() {
        return zip.isSelected();
    }
    public boolean doCBZ() {
        return cbz.isSelected();
    }
    public int getAmount() {
        return Integer.parseInt(amount.getText());
    }
}
