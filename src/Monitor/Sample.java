package Monitor;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.stage.Stage;

/**
 * Below project FDS
 * Created by Lee_rw on 2017/7/18.
 */
public class Sample {
    @FXML
    private Button node_info;
    @FXML
    private Button file_info;
    @FXML
    private ScrollPane Node_page;
    @FXML
    private ScrollPane file_page;
    @FXML
    public void OnFileButtonClicked(ActionEvent event){
        Node_page.setVisible(false);
        file_page.setVisible(true);
    }
    @FXML
    public void OnNodeButtonClicked(ActionEvent event){
        file_page.setVisible(false);
        Node_page.setVisible(true);
    }

}
