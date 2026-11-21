package com.rinin.algoviz;

import com.rinin.algoviz.ui.SortingView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;

/** Entry point: a window with one tab per kind of algorithm. */
public class App extends Application {

    @Override
    public void start(Stage stage) {
        Tab sorting = new Tab("Sorting", new SortingView());
        TabPane tabs = new TabPane(sorting);
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        Scene scene = new Scene(tabs, 1100, 700);
        scene.getStylesheets().add(App.class.getResource("/styles.css").toExternalForm());

        stage.setTitle("Algorithm Visualizer");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
