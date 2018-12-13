package cc.paas.userinterface;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.*;
import static javafx.application.Application.launch;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ProgressBar;
import javafx.stage.Stage;


public class MainApp extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Scene.fxml"));
        Parent root = (Parent)loader.load();
        
        Scene scene = new Scene(root);
        scene.getStylesheets().add("/styles/Styles.css");
        
        FXMLController controller = (FXMLController)loader.getController();
        controller.setStageAndSetupListeners(stage);
        
        stage.setTitle("cc-paas");
        stage.setScene(scene);
        stage.show();
        
        Task task = new Task<Void>() {
            @Override public Void call() {
            	while(!controller.isDoPing()) {
            		try {
                        Thread.sleep(200);
                    } catch (InterruptedException ex) {
                    }
                }
            	
            	try {
                    Thread.sleep(8000);
                } catch (InterruptedException ex) {
                }
            	
            	while(controller.getStatus() < 1) {
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException ex) {
                    }
                    
                    double value = controller.ProgressUpdate();
                    updateProgress(value, controller.getAllEntries());
                    if(!controller.getPasswordFound().equals("N/A")) {  
                        Platform.runLater(new Runnable() {
                            @Override public void run() {
                            	controller.getPasswordLabel().setText("Password: " + controller.getPasswordFound());
                            }
                    	});
            	    }
                }
            	return null;
            }
        };

        ProgressBar bar = controller.getProgressBar();
        bar.progressProperty().bind(task.progressProperty());
        new Thread(task).start();
    }
    
    /**
     * The main() method is ignored in correctly deployed JavaFX application.
     * main() serves only as fallback in case the application can not be
     * launched through deployment artifacts, e.g., in IDEs with limited FX
     * support. NetBeans ignores main().
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

}
