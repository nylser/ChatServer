package net.mineguild.ChatServer.client.gui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import javax.net.ssl.*;
import java.io.*;
import java.net.Socket;
import java.security.KeyStore;
import java.util.Arrays;
import java.util.Optional;

public class App extends Application {

    private Socket socket;
    private PrintWriter output;
    private String name;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {

        /*Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText("This is a test alert!");
        alert.show();*/
        //System.setProperty("https.protocols", "TLSv1");
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
        ChoiceDialog<String> choiceDialog = new ChoiceDialog("SSL", "Normal", "SSL");
        choiceDialog.showAndWait();
        if(choiceDialog.getSelectedItem().equalsIgnoreCase("SSL")) {
            socket = openSSLConnection("localhost", 7885);
        } else {
            socket = openConnection("localhost", 7785);
        }
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
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }));
    }

    public Socket openSSLConnection(String host, int port) {
        Socket s = null;
        try {
            KeyStore ks = KeyStore.getInstance("JKS");
            InputStream trustStore = App.class.getResourceAsStream("/ClientKey.store");
            ks.load(trustStore, "MG2015".toCharArray());
            trustStore.close();
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(ks, "MG2015".toCharArray());

            SSLContext sc = SSLContext.getInstance("SSL");
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(ks);
            TrustManager[] trustManagers = tmf.getTrustManagers();
            System.out.println(Arrays.toString(trustManagers));
            sc.init(kmf.getKeyManagers(), trustManagers, null);

            SSLSocketFactory ssf = sc.getSocketFactory();
            s = ssf.createSocket(host, port);
            output = new PrintWriter(s.getOutputStream());
        } catch (Exception e) {
            ExceptionAlert.create(e).showAndWait();
        }
        return s;
    }

    public Socket openConnection(String host, int port) {
        Socket s = null;
        try {
            s = new Socket(host, port);
            output = new PrintWriter(s.getOutputStream());
        } catch (IOException e){
            ExceptionAlert.create(e).showAndWait();
        }
        return s;
    }

    public void sendMessage(String message) {
        output.println(message);
        output.flush();
    }
}
