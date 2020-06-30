package com.bgk21.diss;

import java.util.ArrayList;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

class ConsoleArea extends VBox {
	private ArrayList<Label> rows = new ArrayList<Label>();

	public ConsoleArea() {
		HBox toolbar = new HBox();
		toolbar.setStyle("-fx-border-color:white; -fx-border-width:0 0 1 0");

		Label label = new Label("Console");
		label.setStyle("-fx-text-fill: white;");

		toolbar.getChildren().add(label);
		this.getChildren().add(toolbar);
		this.setStyle("-fx-background-color:black;");
		this.setAlignment(Pos.TOP_LEFT);
		this.setPadding(new Insets(5));
	}

	public void addRow(String text) {
		Label label = new Label("> " + text);
		label.setStyle("-fx-text-fill: white;");
		this.getChildren().add(label);
		rows.add(label);
	}

	public void removeRow(Label label) {
		this.getChildren().remove(label);
		rows.remove(label);
	}

	public ArrayList<Label> getRows() {
		return rows;
	}
}
