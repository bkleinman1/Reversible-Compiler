package com.bgk21.diss;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map.Entry;
import java.util.Stack;

import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;

public class Display extends Application {

	Emulator emulator;
	CodeArea codeArea;
	static ConsoleArea consoleArea;
	ScrollPane scroll;

	Stage stage;
	BorderPane root;

	TableView<String[]> table;
	String[][] data;

	int currentChar = 2;

	boolean saved = false;

	@Override
	public void start(Stage stage) throws Exception {
		this.stage = stage;
		root = new BorderPane();
		root.setMinSize(500, 500);
		setupScreen();

		Scene scene = new Scene(root, 800, 500);
		scene.getStylesheets().add(this.getClass().getResource("style.css").toExternalForm());
		stage.setResizable(false);
		stage.setScene(scene);
		stage.show();
	}

	public void setupScreen() {
		root.setRight(addRegisterBox());
		root.setTop(addTopBox());
		root.setCenter(addCodeBox());
	}

	public VBox addCodeBox() {
		VBox vbox = new VBox();

		codeArea = new CodeArea();
		codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));
		codeArea.setEditable(true);
		if(emulator != null)
			codeArea.insertText(0, emulator.p.text);
		codeArea.setOnKeyPressed(e -> {
			if(this.saved) {
				this.saved = false;
			}
		});

		StackPane stack = new StackPane(new VirtualizedScrollPane<>(codeArea));
		stack.setMinHeight(350);

		scroll = new ScrollPane();
		consoleArea = new ConsoleArea();
		scroll.setContent(consoleArea);
		scroll.setFitToWidth(true);
		scroll.setFitToHeight(true);
		scroll.setPrefHeight(150);
		scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

		vbox.getChildren().add(stack);
		vbox.getChildren().add(scroll);

		return vbox;
	}

	public void updateHighlight() {
		if (emulator.currentStatement < emulator.p.AST.exps.size()) {
			int lineNo = emulator.p.AST.exps.get(emulator.currentStatement).lineNumber;
			int lineCounter = 1;
			int charCounter = 0;
			while (charCounter < emulator.p.text.length()) {
				if (emulator.p.text.charAt(charCounter) == '\n') {
					lineCounter++;
				}
				if (lineCounter == lineNo) {
					break;
				}
				charCounter++;
			}
			codeArea.setStyleClass(0, emulator.p.text.length() - 1, "black");
			codeArea.setStyleClass(0, charCounter, "red");
		}
		if (emulator.currentStatement == emulator.p.AST.exps.size()) {
			codeArea.setStyleClass(0, emulator.p.text.length(), "red");
		}
	}
	
	public HBox addTopBox() {
		HBox topBar = new HBox();
		topBar.setPadding(new Insets(5, 5, 5, 5));
		topBar.setSpacing(10);

		HBox topBarLeft = new HBox();
		topBarLeft.setSpacing(10);
		
		Button newFile = new Button("New file");
		topBarLeft.getChildren().add(newFile);

		Button openFile = new Button("Open file...");
		topBarLeft.getChildren().add(openFile);

		Button saveFile = new Button("Save/Compile");
		topBarLeft.getChildren().add(saveFile);

		HBox topBarRight = new HBox();
		topBarRight.setSpacing(10);

		Button run = new Button("Run Forwards");
		Button runBack = new Button("Run in Reverse");
		Button runNextLine = new Button("Run Next Line");
		Button runPrevLine = new Button("Run Previous Line");
		
		newFile.setOnAction(e -> {
			emulator = null;
			setupScreen();
		});

		openFile.setOnAction(e -> {
			FileChooser fileChooser = new FileChooser();
			File selectedFile = fileChooser.showOpenDialog(stage);
			try {
				emulator = new Emulator(selectedFile.getPath());
				setupScreen();
			} catch (Exception e1) {
				if (e1 instanceof NullPointerException) {
					e1.printStackTrace();
				}
			}
		});

		saveFile.setOnAction(e -> {
			String file;
			if(emulator == null) {
				FileChooser fileChooser = new FileChooser();
			    File selectedFile = fileChooser.showSaveDialog(null);
			    file = selectedFile.getPath() + ".txt";
			} else {
				file = emulator.p.fileURL;
			}
			try {
				File f = new File(file);
				if (f.exists()) {
					f.delete();
				}
				BufferedWriter writer = new BufferedWriter(new FileWriter(file));
				writer.write(codeArea.getText());
				writer.close();
				emulator = new Emulator(file);
				try {
					emulator.p.lexAndParse();
					setupScreen();
					this.saved = true;
				} catch (Exception se) {
					System.out.println(emulator.p.errors);
					for (String s : emulator.p.errors)
						consoleArea.addRow(s);
					this.saved = false;
					se.printStackTrace();
				}
				
			} catch (IOException e1) {
				e1.printStackTrace();
			} catch (Exception e1) {
				e1.printStackTrace();
			}

		});

		runNextLine.setOnAction(e -> {
			try {
				if (this.saved) {
					emulator.executeNextStatement(true);
					updateHighlight();
					updateVarTable();
					scroll.setVvalue(1);
				} else {
					consoleArea.addRow("Save and compile before executing");
				}
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		});

		runPrevLine.setOnAction(e -> {
			try {
				if (this.saved) {
					emulator.executeNextStatement(false);
					updateHighlight();
					updateVarTable();
					scroll.setVvalue(1);
				} else {
					consoleArea.addRow("Save and compile before executing");
				}
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		});

		run.setOnAction(e -> {
			try {
				if (this.saved) {
					emulator.execute(true);
					updateVarTable();
					updateHighlight();
					scroll.setVvalue(1);
				} else {
					consoleArea.addRow("Save and compile before executing");
				}
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		});

		runBack.setOnAction(e -> {
			try {
				if (this.saved) {
					emulator.execute(false);
					updateVarTable();
					updateHighlight();
					scroll.setVvalue(1);
				} else {
					consoleArea.addRow("Save and compile before executing");
				}
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		});

		topBarRight.getChildren().add(run);
		topBarRight.getChildren().add(runBack);
		topBarRight.getChildren().add(runNextLine);
		topBarRight.getChildren().add(runPrevLine);

		topBar.getChildren().add(topBarLeft);
		topBar.getChildren().add(topBarRight);

		return topBar;
	}

	@SuppressWarnings("unchecked")
	public TableView<String[]> addRegisterBox() {

		data = new String[1][2];

		// Create the table and columns
		table = new TableView<String[]>();
		table.setEditable(false);
		table.setPlaceholder(new Label("No variables to show."));
		table.setSelectionModel(null);

		TableColumn<String[], String> nameColumn = new TableColumn<String[], String>();
		nameColumn.setText("Variable");

		TableColumn<String[], String> valueColumn = new TableColumn<String[], String>();
		valueColumn.setText("Value");

		table.getColumns().addAll(nameColumn, valueColumn);

		nameColumn.setCellValueFactory(
				(Callback<CellDataFeatures<String[], String>, ObservableValue<String>>) new Callback<TableColumn.CellDataFeatures<String[], String>, ObservableValue<String>>() {
					@Override
					public ObservableValue<String> call(TableColumn.CellDataFeatures<String[], String> p) {
						String[] x = p.getValue();
						if (x != null && x.length > 0) {
							return new SimpleStringProperty(x[0]);
						} else {
							return new SimpleStringProperty("<no name>");
						}
					}
				});
		nameColumn.setSortable(false);

		valueColumn.setCellValueFactory(
				new Callback<TableColumn.CellDataFeatures<String[], String>, ObservableValue<String>>() {
					@Override
					public ObservableValue<String> call(TableColumn.CellDataFeatures<String[], String> p) {
						String[] x = p.getValue();
						if (x != null && x.length > 1) {
							return new SimpleStringProperty(x[1]);
						} else {
							return new SimpleStringProperty("<no value>");
						}
					}
				});
		valueColumn.setSortable(false);

		table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

		table.getItems().addAll(Arrays.asList(data));

		return table;
	}

	public void updateVarTable() {
		table.getItems().clear();
		data = new String[emulator.register.size()][2];
		int count = 0;
		for (Entry<String, Stack<Operation>> entry : emulator.register.entrySet()) {
			if(!entry.getKey().endsWith("_TEMP") && !entry.getKey().startsWith("_COUNTER")) {
				data[count][0] = entry.getKey();
				data[count][1] = Double.toString(Operation.sum(entry.getValue(), true));
				count++;
			}
		}
		table.getItems().addAll(Arrays.asList(data));
	}

	public static void main(String[] args) {
		launch(args);
	}
}