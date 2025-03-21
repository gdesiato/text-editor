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
import javafx.stage.StageStyle;

import java.io.*;
import java.util.Stack;

public class TextEditor extends Application {

    private TabPane tabPane;
    private Stack<String> undoStack = new Stack<>();
    private Stack<String> redoStack = new Stack<>();
    private Label statusLabel;
    private Label wordCountLabel;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("JavaFX Text Editor");
        primaryStage.setResizable(false);
        primaryStage.initStyle(StageStyle.UNDECORATED);

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

        // Toolbar
        ToolBar toolBar = new ToolBar();
        Button newBtn = new Button("New");
        Button openBtn = new Button("Open");
        Button saveBtn = new Button("Save");
        Button undoBtn = new Button("Undo");
        Button redoBtn = new Button("Redo");
        newBtn.setOnAction(e -> createNewTab());
        openBtn.setOnAction(e -> openFile(primaryStage));
        saveBtn.setOnAction(e -> saveFile(primaryStage));
        undoBtn.setOnAction(e -> undoAction());
        redoBtn.setOnAction(e -> redoAction());
        toolBar.getItems().addAll(newBtn, openBtn, saveBtn, undoBtn, redoBtn);

        // Tab Pane for Multiple Files
        tabPane = new TabPane();
        createNewTab();

        // Status Bar with Word Count
        statusLabel = new Label("Line: 1, Column: 1");
        wordCountLabel = new Label("Words: 0");
        HBox statusBar = new HBox(10, statusLabel, wordCountLabel);

        // Layout
        VBox topContainer = new VBox(menuBar, toolBar);
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
        TextArea textArea = new TextArea();
        textArea.textProperty().addListener((observable, oldValue, newValue) -> {
            undoStack.push(oldValue);
            updateWordCount(newValue);
        });
        tab.setContent(textArea);
        tabPane.getTabs().add(tab);
        tabPane.getSelectionModel().select(tab);
    }

    private void updateWordCount(String text) {
        int wordCount = text.trim().isEmpty() ? 0 : text.trim().split("\\s+").length;
        wordCountLabel.setText("Words: " + wordCount);
    }

    private void undoAction() {
        if (!undoStack.isEmpty()) {
            redoStack.push(getActiveTextArea().getText());
            getActiveTextArea().setText(undoStack.pop());
            updateWordCount(getActiveTextArea().getText());
        }
    }

    private void redoAction() {
        if (!redoStack.isEmpty()) {
            undoStack.push(getActiveTextArea().getText());
            getActiveTextArea().setText(redoStack.pop());
            updateWordCount(getActiveTextArea().getText());
        }
    }

    private TextArea getActiveTextArea() {
        Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();
        if (selectedTab != null) {
            return (TextArea) selectedTab.getContent();
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
                getActiveTextArea().setText(content.toString());
                updateWordCount(content.toString());
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
                writer.write(getActiveTextArea().getText());
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
