package org.editor;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;

import java.io.*;

public class TextEditor extends Application {

    private TabPane tabPane;
    private Label statusLabel;
    private Label wordCountLabel;
    private Stage mainStage;

    @Override
    public void start(Stage primaryStage) {
        this.mainStage = primaryStage;
        primaryStage.setTitle("JavaFX Text Editor");
        primaryStage.setResizable(true);

        // Menu Bar
        MenuBar menuBar = new MenuBar();
        Menu fileMenu = new Menu("File");
        MenuItem newFile = new MenuItem("New");
        MenuItem openFile = new MenuItem("Open");
        MenuItem saveFile = new MenuItem("Save");
        MenuItem exitApp = new MenuItem("Exit");
        fileMenu.getItems().addAll(newFile, openFile, saveFile, new SeparatorMenuItem(), exitApp);
        menuBar.getMenus().add(fileMenu);

        // Edit Menu for Undo/Redo
        Menu editMenu = new Menu("Edit");
        MenuItem undo = new MenuItem("Undo");
        MenuItem redo = new MenuItem("Redo");
        undo.setAccelerator(new KeyCodeCombination(KeyCode.Z, KeyCombination.CONTROL_DOWN));
        redo.setAccelerator(new KeyCodeCombination(KeyCode.Y, KeyCombination.CONTROL_DOWN));
        undo.setOnAction(e -> undoAction());
        redo.setOnAction(e -> redoAction());
        editMenu.getItems().addAll(undo, redo);
        menuBar.getMenus().add(editMenu);

        // Tab Pane for Multiple Files
        tabPane = new TabPane();
        createNewTab();

        // Status Bar with Word Count
        statusLabel = new Label("Line: 1, Column: 1");
        wordCountLabel = new Label("Words: 0");
        HBox statusBar = new HBox(10, statusLabel, wordCountLabel);

        // Layout
        VBox topContainer = new VBox(menuBar);
        BorderPane layout = new BorderPane();
        layout.setTop(topContainer);
        layout.setCenter(tabPane);
        layout.setBottom(statusBar);

        // Scene
        Scene scene = new Scene(layout, 900, 600);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void createNewTab() {
        Tab tab = new Tab("Untitled");
        CodeArea codeArea = new CodeArea();
        codeArea.setStyle("-fx-font-family: 'Courier New'; -fx-font-size: 13px; -fx-text-fill: black; -fx-font-weight: bold;");
        codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));
        codeArea.textProperty().addListener((obs, oldText, newText) -> updateWordCount(newText));
        tab.setContent(new VirtualizedScrollPane<>(codeArea));
        tabPane.getTabs().add(tab);
        tabPane.getSelectionModel().select(tab);
    }

    private void updateWordCount(String text) {
        int wordCount = text.trim().isEmpty() ? 0 : text.trim().split("\\s+").length;
        wordCountLabel.setText("Words: " + wordCount);
    }

    private void undoAction() {
        CodeArea codeArea = getActiveCodeArea();
        if (codeArea != null) {
            codeArea.undo();
        }
    }

    private void redoAction() {
        CodeArea codeArea = getActiveCodeArea();
        if (codeArea != null) {
            codeArea.redo();
        }
    }

    private CodeArea getActiveCodeArea() {
        Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();
        if (selectedTab != null && selectedTab.getContent() instanceof VirtualizedScrollPane) {
            VirtualizedScrollPane<?> scrollPane = (VirtualizedScrollPane<?>) selectedTab.getContent();
            return (CodeArea) scrollPane.getContent();
        }
        return null;
    }

    private void openFile(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                StringBuilder content = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line).append("\n");
                }
                createNewTab();
                Tab currentTab = tabPane.getSelectionModel().getSelectedItem();
                currentTab.setText(file.getName());
                getActiveCodeArea().replaceText(content.toString());
                updateWordCount(content.toString());
                mainStage.setTitle(file.getAbsolutePath());
            } catch (IOException e) {
                showAlert("Could not open file.");
            }
        }
    }

    private void saveFile(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));
        File file = fileChooser.showSaveDialog(stage);
        if (file != null) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                writer.write(getActiveCodeArea().getText());
                mainStage.setTitle(file.getAbsolutePath());
                Tab currentTab = tabPane.getSelectionModel().getSelectedItem();
                currentTab.setText(file.getName());
            } catch (IOException e) {
                showAlert("Could not save file.");
            }
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
