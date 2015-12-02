package net.mineguild.ChatServer.client.gui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Optional;

public class App extends Application {

    private Socket socket;
    private PrintWriter output;
    private String name;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws IOException {

        /*Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText("This is a test alert!");
        alert.show();*/
        Optional<String> stringOptional;
        TextInputDialog inputDialog = new TextInputDialog();
        inputDialog.getEditor().setOnInputMethodTextChanged((event)->{
            String text = inputDialog.getEditor().getText();
        });
        inputDialog.setHeaderText("Choose a name");
        inputDialog.setContentText("Please enter a username:");
        inputDialog.setTitle("Username");
        stringOptional = inputDialog.showAndWait();
        if(!stringOptional.isPresent()){
            Platform.exit();
        }
        if(stringOptional.get().isEmpty()){
            Platform.exit();
        }
        name = stringOptional.get();
        socket = openConnection("play.mineguild.net", 7785);
        if (socket == null) return;

        FXMLLoader loader = new FXMLLoader(getClass().getResource("App_Style.fxml"));
        Controller controller = new Controller(this);
        loader.setController(controller);
        Parent root = loader.load();
        Scene scene = new Scene(root);
        stage.setTitle("Chat-Client");
        stage.setScene(scene);
        stage.show();

        ConnectionInputProcess connectionInputProcess = new ConnectionInputProcess(socket, name, controller.getTextArea());
        connectionInputProcess.start();
        stage.addEventHandler(WindowEvent.WINDOW_CLOSE_REQUEST, (event -> {
            try {
                sendMessage("/bye");
                socket.close();
                connectionInputProcess.join();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }));
    }

    public Socket openConnection(String host, int port) {
        Socket s = null;
        try {
            s = new Socket(host, port);
            output = new PrintWriter(s.getOutputStream());
        } catch (IOException e) {
            ExceptionAlert.create(e).showAndWait();
        }
        return s;
    }

    public void sendMessage(String message) {
        output.println(message);
        output.flush();
    }
}
