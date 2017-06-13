package lostpotatofoundation.hentaigallerydownloader.gui;

import hxckdms.hxcconfig.HxCConfig;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;
import javafx.stage.Stage;
import lostpotatofoundation.hentaigallerydownloader.Configuration;
import lostpotatofoundation.hentaigallerydownloader.TwoDimensionalValueHashMap;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.HashSet;

import static lostpotatofoundation.hentaigallerydownloader.GalleryDownloader.downloadDir;

public class MainGui extends Application {
    private double initialWidth, initialHeight;
    public static HxCConfig mainConfig;
    public static void main(String[] args) {
        URLClassLoader classLoader = (URLClassLoader) MainGui.class.getClassLoader();
        try {
            Method method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
            method.setAccessible(true);

            File file = new File("libraries/ConfigurationAPI-1.3.jar");

            method.invoke(classLoader, file.toURI().toURL());
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | MalformedURLException e) {
            e.printStackTrace();
        }

        System.out.println(classLoader);

        mainConfig = new HxCConfig(Configuration.class, "configuration", new File(System.getProperty("user.dir")), "cfg", "galleryDownloader");
        mainConfig.initConfiguration();
        Configuration.initCookies();

        if (!downloadDir.exists()) if (!downloadDir.mkdirs()) throw new RuntimeException("Couldn't create directories.");
        launch();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader initialLoader = new FXMLLoader(getClass().getResource("/lostpotatofoundation/hentaigallerydownloader/innerGUI.fxml"));

        Pane root = new Pane();
        primaryStage.setTitle("Hentai downloader");

        Pane pane = initialLoader.load();
        pane.setId("0:0");
        root.getChildren().add(pane);

        root.heightProperty().addListener((observable, oldValue, newValue) -> onWindowResize_H(primaryStage, root, newValue));
        root.widthProperty().addListener(((observable, oldValue, newValue) -> onWindowResize_W(primaryStage, root, newValue)));


        primaryStage.setScene(new Scene(root));
        primaryStage.show();

        if (!Configuration.cssSheets.isEmpty()) {
            File f = new File("themes/" + Configuration.cssSheets.get(0));
            primaryStage.getScene().getStylesheets().clear();
            System.out.println(f.getAbsolutePath());
            primaryStage.getScene().getStylesheets().add("file:///" + f.getAbsolutePath().replace("\\", "/"));
        }

        initialWidth = primaryStage.getWidth();
        initialHeight = primaryStage.getHeight();

        primaryStage.setMinHeight(primaryStage.getHeight());
        primaryStage.setMinWidth(primaryStage.getWidth());
    }

    private HashMap<Pane, FXMLLoader> loaders = new HashMap<>();

    private TwoDimensionalValueHashMap<Integer, Pane> panes2d = new TwoDimensionalValueHashMap<>();
    private TwoDimensionalValueHashMap<Integer, Line> verticalLines2d = new TwoDimensionalValueHashMap<>();
    private TwoDimensionalValueHashMap<Integer, Line> horizontalLines2d = new TwoDimensionalValueHashMap<>();

    private int extraDownloaders_H, extraDownloaders_W;

    @SuppressWarnings("SuspiciousMethodCalls")
    private void updateClientWindows(Stage stage, Pane root) {
        double minSizeX = initialWidth, minSizeY = initialHeight;

        horizontalLines2d.clear(); verticalLines2d.clear();

        for (Integer key1 : new HashSet<>(panes2d.keySet())) {
            for (Integer key2 : new HashSet<>(panes2d.get(key1).keySet())) {
                if (loaders.containsKey(panes2d.get(key2, key1)) && !((MainController) loaders.get(panes2d.get(key2, key1)).getController()).isRunning()) {
                    panes2d.remove2D(key2, key1);
                } else if (loaders.containsKey(panes2d.get(key2, key1))) {
                    minSizeX = Math.max(panes2d.get(key2, key1).getTranslateX() + minSizeX, minSizeX);
                    minSizeY = Math.max(panes2d.get(key2, key1).getTranslateY() + minSizeY, minSizeY);
                }
            }
        }


        for (int w = 0; w <= extraDownloaders_W; w++) {
            for (int h = 0; h <= extraDownloaders_H; h++) {
                if (h <= 0 && w <= 0) continue;

                if (h > 0) root.getChildren().add(horizontalLines2d.put(w, h, new Line(600 * w, 172 * h, 600 * (w + 1), 172 * h)));
                if (w > 0) root.getChildren().add(verticalLines2d.put(w, h, new Line(600 * w, 172 * h, 600 * w, 172 * (h + 1))));

                if (panes2d.containsKey(w, h)) continue;

                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/lostpotatofoundation/hentaigallerydownloader/innerGUI.fxml"));
                    Pane newPane = loader.load();

                    newPane.setTranslateX(newPane.getPrefWidth() * w);
                    newPane.setTranslateY(newPane.getPrefHeight() * h);

                    newPane.setId(w + ":" + h);

                    loaders.put(newPane, loader);
                    root.getChildren().add(panes2d.put(w, h, newPane));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        stage.setMinWidth(minSizeX); stage.setMinHeight(minSizeY);
        root.getChildren().removeIf(node -> node instanceof Line && !verticalLines2d.containsValue(node) && !horizontalLines2d.containsValue(node));
        root.getChildren().removeIf(node -> node instanceof Pane && !node.getId().equalsIgnoreCase("0:0") && !panes2d.containsValue(node));
    }

    private void onWindowResize_W(Stage stage, Pane root, Number newValue) {
        extraDownloaders_W = Math.max((int) Math.floor((newValue.doubleValue() - 600D) / 600D), 0);
        updateClientWindows(stage, root);
    }

    private void onWindowResize_H(Stage stage, Pane root, Number newValue) {
        extraDownloaders_H = Math.max((int) Math.floor((newValue.doubleValue() - 172D) / 172D), 0);
        updateClientWindows(stage, root);
    }
}
