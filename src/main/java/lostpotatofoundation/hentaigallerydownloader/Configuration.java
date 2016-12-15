package lostpotatofoundation.hentaigallerydownloader;

import hxckdms.hxcconfig.Config;

import java.util.LinkedHashMap;

@SuppressWarnings("WeakerAccess")
@Config
public class Configuration {
    @Config.category("Compression")
    public static boolean compress = true;

    @Config.category("Compression")
    public static boolean deleteLoseFiles = true;

    @Config.category("Compression")
    public static String program7zPath = "C:/Program Files/7-Zip/7z.exe";

    @Config.category("Compression")
    @Config.comment("Current options cb7 and cbz. Notes: cb7 only works on windows for now and requires the program7zPath to be set correctly.")
    public static String compressionType = "cbz";

    @Config.category("Debug")
    public static boolean debug = false;

    @Config.category("Connection")
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
