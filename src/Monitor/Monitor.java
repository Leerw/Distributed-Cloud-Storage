package Monitor;


import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


/**
 * Created by lee_rw on 2017/7/11.
 */
public class Monitor extends Application {



    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        primaryStage.setTitle("FM monitor");
        primaryStage.setScene(new Scene(root, 600, 400
        ));
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
        ReceiveData receiveData = new ReceiveData();
        ScheduledExecutorService receiveDataService = Executors.newSingleThreadScheduledExecutor();
        receiveDataService.scheduleAtFixedRate(receiveData,10,200, TimeUnit.MILLISECONDS);
    }


}
