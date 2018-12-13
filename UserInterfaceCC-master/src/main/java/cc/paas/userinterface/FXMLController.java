package cc.paas.userinterface;

import com.google.gson.Gson;
import com.sun.glass.ui.Window;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.ResourceBundle;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class FXMLController implements Initializable {

    @FXML
    private VBox dragTargetDIC;
    @FXML
    private VBox dragTargetEAPOL;
    @FXML
    private Label successLabelDIC;
    @FXML
    private Label successLabelEAPOL;
    @FXML
    private ProgressBar progressBar;
    @FXML
    private Label passwordLabel;
    private File dictionary;
    private File eapol;
    private Stage primarystage;
    private String mMic;
    private double status = 0;
    private boolean doPing = false;
    private String passwordFound = "N/A";
    private double allEntries = 0;

	@FXML
    private void handleButtonActionDIC(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("PCAP files (*.pcap)", "*.pcap");
        fileChooser.getExtensionFilters().add(extFilter);
        File file = fileChooser.showOpenDialog(primarystage);
        if(file.getName().substring(file.getName().lastIndexOf(".")).equals("pcap")) {
            ParsePCAP(file);
        }
        successLabelDIC.setText(file.toString() + "\nready to upload");
    }
    
    @FXML
    private void handleButtonActionEAPOL(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("PCAP files (*.pcap)", "*.pcap");
        fileChooser.getExtensionFilters().add(extFilter);
        File file = fileChooser.showOpenDialog(primarystage);
        if(file.getName().substring(file.getName().lastIndexOf(".")).equals("pcap")) {
            ParsePCAP(file);
        }
        successLabelEAPOL.setText(file.toString() + "\nready to upload");
    }

    @FXML
    private void handleDragOverDIC(DragEvent event) {
        if (event.getGestureSource() != dragTargetDIC
                && event.getDragboard().hasFiles()) {
            /* allow for both copying and moving, whatever user chooses */
            event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
        }
        event.consume();
    }
    
    @FXML
    private void handleDragOverEAPOL(DragEvent event) {
        if (event.getGestureSource() != dragTargetEAPOL
                && event.getDragboard().hasFiles()) {
            /* allow for both copying and moving, whatever user chooses */
            event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
        }
        event.consume();
    }

    @FXML
    public void handleDragDroppedDIC(DragEvent event) {
        Dragboard db = event.getDragboard();
        boolean success = false;
        if (db.hasFiles()) {
        	successLabelDIC.setText(db.getFiles().toString() + "\nready to upload");
            dictionary = db.getFiles().get(0);
            success = true;
        }
        /* let the source know whether the string was successfully 
                 * transferred and used */
        event.setDropCompleted(success);
        System.out.println("drop : " + success);
        event.consume();
    }
    
    @FXML
    public void handleDragDroppedEAPOL(DragEvent event) {
        Dragboard db = event.getDragboard();
        boolean success = false;
        if (db.hasFiles()) {
        	successLabelEAPOL.setText(db.getFiles().toString() + "\nready to upload");
            eapol = db.getFiles().get(0);
            success = true;
        }
        /* let the source know whether the string was successfully 
                 * transferred and used */
        event.setDropCompleted(success);
        System.out.println("drop : " + success);
        event.consume();
    }

    @FXML
    public void Upload() {
        // UPLOAD CODE TODO
        // uploadedFile -> Send to server
        //progressBar.setProgress(progressBar.getProgress()+0.1);
    	System.out.println(dictionary);
    	
    	HashMap<String, Object> data = ParsePCAP(eapol);
    	mMic = data.get("mic").toString();
    	
    	try {
    		CloseableHttpClient httpClient = HttpClients.createDefault();
        	HttpPost uploadFile = new HttpPost("https://cc-paas-assignment.appspot.com/master");
        	MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        	builder.addTextBody("STA", data.get("clientMac").toString(), ContentType.TEXT_PLAIN);
        	builder.addTextBody("BSSID", data.get("bssid").toString(), ContentType.TEXT_PLAIN);
        	builder.addTextBody("ANonce", data.get("aNonce").toString(), ContentType.TEXT_PLAIN);
        	builder.addTextBody("SNonce", data.get("sNonce").toString(), ContentType.TEXT_PLAIN);
        	builder.addTextBody("MIC", data.get("mic").toString(), ContentType.TEXT_PLAIN);
        	builder.addTextBody("Version", data.get("version").toString(), ContentType.TEXT_PLAIN);
        	builder.addTextBody("SSID", data.get("ssid").toString(), ContentType.TEXT_PLAIN);
        	builder.addTextBody("secondFrame", data.get("auth").toString(), ContentType.TEXT_PLAIN);
        	
        	// This attaches the file to the POST:
        	builder.addBinaryBody(
        	    "dictionary",
        	    new FileInputStream(dictionary),
        	    ContentType.APPLICATION_OCTET_STREAM,
        	    "dictionary"
        	);

        	HttpEntity multipart = builder.build();
        	uploadFile.setEntity(multipart);
        	CloseableHttpResponse response = httpClient.execute(uploadFile);
        	HttpEntity responseEntity = response.getEntity();
        	
        	doPing = true;
    	} catch (Exception e) {
    	}
    }
    
    public double ProgressUpdate() {
    	try  {
	    	CloseableHttpClient httpclient = HttpClients.createDefault();
	    	HttpGet httpget = new HttpGet("http://cc-paas-assignment.appspot.com/master?MIC="+mMic);
	    	
	    	CloseableHttpResponse response = httpclient.execute(httpget);
	    	HttpEntity entity = response.getEntity();
	    	
	    	InputStream instream = entity.getContent();
	    	Gson gson = new Gson();
	    	InputStreamReader instreader = new InputStreamReader(instream);
	    	Result result = gson.fromJson(instreader, Result.class);
	 	    	
	    	double value = result.EntriesChecked.doubleValue();
	    	allEntries = result.DictionaryEntries.doubleValue();
	    	status = value/allEntries;
	    	passwordFound = result.Password;
	        
	        instream.close();
	        
	        return value;
    	} catch(Exception e) {
    		
    	}
    	return 0.0;
    }
    
    public void ProgressUpdateValue(double value) {
    	this.progressBar.setProgress(value);
    }
    
    @FXML
    public void Exit() {
        System.exit(0);
    }
    @FXML
    public void Help() {
        Alert alert = new Alert(AlertType.INFORMATION, "Upload a PCAP type dictionary file to crack password");
        alert.show();
    }
    
    private HashMap<String, Object> ParsePCAP(File pcap) {
        HashMap<String, Object> data = PcapReader.pcapReader(pcap.getAbsolutePath());

        data.forEach((key, value) -> System.out.println(key + " = " + value));
        
        return data;
    }
    @Override
    public void initialize(URL url, ResourceBundle rb) {

    }

    void setStageAndSetupListeners(Stage stage) {
        this.primarystage = stage;
        // Set the percentage size of the drag zone
        dragTargetDIC.prefWidthProperty().bind(primarystage.widthProperty().multiply(0.3));
        dragTargetDIC.prefHeightProperty().bind(primarystage.heightProperty().multiply(0.3));
        dragTargetEAPOL.prefWidthProperty().bind(primarystage.widthProperty().multiply(0.3));
        dragTargetEAPOL.prefHeightProperty().bind(primarystage.heightProperty().multiply(0.3));
    }
    
    public double getStatus() {
		return status;
	}

	public void setStatus(double status) {
		this.status = status;
	}

	public boolean isDoPing() {
		return doPing;
	}

	public void setDoPing(boolean doPing) {
		this.doPing = doPing;
	}

	public ProgressBar getProgressBar() {
		return progressBar;
	}

	public void setProgressBar(ProgressBar progressBar) {
		this.progressBar = progressBar;
	}

	public String getPasswordFound() {
		return passwordFound;
	}

	public void setPasswordFound(String passwordFound) {
		this.passwordFound = passwordFound;
	}

	public Label getPasswordLabel() {
		return passwordLabel;
	}

	public void setPasswordLabel(Label passwordLabel) {
		this.passwordLabel = passwordLabel;
	}

	public double getAllEntries() {
		return allEntries;
	}

	public void setAllEntries(double allEntries) {
		this.allEntries = allEntries;
	}
}
