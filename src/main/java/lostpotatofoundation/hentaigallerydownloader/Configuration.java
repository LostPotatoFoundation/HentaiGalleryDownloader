package lostpotatofoundation.hentaigallerydownloader;

import hxckdms.hxcconfig.Config;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

@SuppressWarnings("WeakerAccess")
@Config
public class Configuration {
    @Config.category("Other")
    @Config.comment("These are run before compression step after downloading images, use this as you want the image arg is %IMAGE% will replace with path to image in command. Entire exe path only no system args.")
    public static List<String> preCompressCommands = new ArrayList<>();

    @Config.category("Other")
    @Config.comment("This will split titles on the | and use the second part, may break in some rare cases. Report those with gallery title or link please.")
    public static boolean attemptEnglish = true;

    @Config.category("Compression")
    @Config.comment("Zips/7zips images.")
    public static boolean compress = true;

    @Config.category("Compression")
    public static boolean deleteLoseFiles = true;

    @Config.category("Compression")
    public static String program7zPath = "C:/Program Files/7-Zip/7z.exe";

    @Config.category("Compression")
    @Config.comment("Current options cb7/7z and cbz/zip. can be zip/7z or cbz/cb7 extension. Notes: cb7 only works on windows for now and requires the program7zPath to be set correctly.")
    public static String compressionType = "cbz";

    @Config.category("Debug")
    @Config.comment("Debugging mode, use at own spam.")
    public static boolean debug = false;

    @Config.category("Connection")
    @Config.comment("DO NOT LEAVE DEFAULT MUST USE REAL COOKIES")
    public static LinkedHashMap<String, String> cookies = new LinkedHashMap<String, String>() {{
        put("igneous", "asdf");
        put("ipb_member_id", "123");
        put("ipb_pass_hash", "1234");
        put("s", "e11");
        put("uconfig", "dm_t");
        put("lv", "1123-3124");
    }};

    @Config.ignore
    private static String COOKIES = "";

    public static void initCookies() {
        StringBuilder builder = new StringBuilder();
        Configuration.cookies.forEach((k, v) -> builder.append(k).append("=").append(v).append("; "));
        COOKIES = builder.toString().substring(0, builder.toString().length() - 2);
    }

    public static synchronized String getCookies() {
        if (COOKIES.isEmpty()) initCookies();
        return COOKIES;
    }
}
