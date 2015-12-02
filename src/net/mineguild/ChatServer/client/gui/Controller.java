package net.mineguild.ChatServer.client.gui;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    @FXML
    private Parent rootPane;
    @FXML
    private TextArea messageArea;
    @FXML
    private TextField inputField;

    private App client;

    public Controller(App client) {
        this.client = client;
    }

    public Controller() {
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        messageArea.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String s, String s2) {

            }
        });
    }


    public void handleSend(ActionEvent event) {
        client.sendMessage(inputField.getText());
        inputField.clear();
    }

    public void handleClose(ActionEvent actionEvent) {
        Stage stage = (Stage) rootPane.getScene().getWindow();
        stage.fireEvent(
                new WindowEvent(
                        stage,
                        WindowEvent.WINDOW_CLOSE_REQUEST
                )
        );
    }

    public TextArea getTextArea() {
        System.out.println(messageArea);
        return messageArea;
    }

}
