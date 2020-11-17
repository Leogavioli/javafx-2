package gui;

import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.Optional;
import java.util.ResourceBundle;

import application.Main;
import db.DbIntegrityException;
import gui.listeners.DataChangeListener;
import gui.util.Alerts;
import gui.util.Utils;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.entities.Department;
import model.entities.Seller;
import model.services.DepartmentService;
import model.services.SellerService;

public class SellerListController implements Initializable, DataChangeListener{

	private SellerService service;
	
	@FXML
	private Button newButton;
	
	@FXML
	private TableView<Seller> tableViewSeller;
	
	@FXML
	private TableColumn<Seller, Integer> tableColumnId;
	
	@FXML
	private TableColumn<Seller,String> tableColumnName;
	
	@FXML
	private TableColumn<Seller,String> tableColumnEmail;
	
	@FXML
	
	//works as String as well - for the table view
	private TableColumn<Seller,Date> tableColumnBirthdate;
	
	@FXML
	private TableColumn<Seller,Double> tableColumnSalary;
	
	@FXML
	private TableColumn<Seller,Department> tableColumnDepartment;
	
	@FXML
	private TableColumn<Seller, Seller> tableColumnEdit;

	@FXML
	private TableColumn<Seller, Seller> tableColumnRemove;
	
	private ObservableList<Seller> obsList;
	
	@FXML
	public void onNewButtonAction(ActionEvent event) {
		Stage stage = Utils.currentStage(event);
		Seller seller = new Seller();
		createDialogForm(seller, "/gui/SellerForm.fxml", stage);
	}
	
	public void setSellerService(SellerService service) {
		this.service=service;
	}
	
	@Override
	public void initialize(URL url, ResourceBundle rb) {
		initializeNodes();
		
	}
	
	private void initializeNodes() {
		tableColumnId.setCellValueFactory(new PropertyValueFactory<>("id"));
		tableColumnName.setCellValueFactory(new PropertyValueFactory<>("name"));
		tableColumnEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
		tableColumnBirthdate.setCellValueFactory(new PropertyValueFactory<>("birthDate"));
		Utils.formatTableColumnDate(tableColumnBirthdate, "dd/MM/yyyy");
		tableColumnSalary.setCellValueFactory(new PropertyValueFactory<>("baseSalary"));
		Utils.formatTableColumnDouble(tableColumnSalary, 2);
		tableColumnDepartment.setCellValueFactory(new PropertyValueFactory<>("department"));

		
		Stage stage = (Stage) Main.getMainScene().getWindow();
		tableViewSeller.prefHeightProperty().bind(stage.heightProperty());
	}
	
	public void updateTableView() {
		if(service==null)
			throw new IllegalStateException("Service not instantiated.");
		obsList=FXCollections.observableArrayList(service.findAll());
		tableViewSeller.setItems(obsList);
		initEditButtons();
		initRemoveButtons();
	}
	
	private void createDialogForm(Seller obj, String absoluteName, Stage parentStage) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource(absoluteName));
			Pane pane = loader.load();

			SellerFormController controller = loader.getController();// url passed DepForm.fxml-gets contr
			controller.setSeller(obj);
			controller.setServices(new SellerService(), new DepartmentService());
			controller.subscribeDataChangeListener(this);
			controller.loadAssociatedObjects();
			controller.updateFormData();

			// create a new stage to load SellerForm dialog box
			Stage dialogStage = new Stage();
			// config stage
			dialogStage.setTitle("Enter Seller data");
			dialogStage.setScene(new Scene(pane));// pane is the parent of the new scene
			dialogStage.setResizable(false);
			dialogStage.initOwner(parentStage);// establish the parent so it locks the window
			dialogStage.initModality(Modality.WINDOW_MODAL);
			dialogStage.showAndWait();// makes it show the window

		} catch (IOException e) {
			e.printStackTrace();
			Alerts.showAlert("IO Exception", "Error loading view", e.getMessage(), AlertType.ERROR);
		}

	}
	
	@Override
	public void onDataChanged() {
		updateTableView();
	}
	
	private void initEditButtons() {
		tableColumnEdit.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue()));
		tableColumnEdit.setCellFactory(param -> new TableCell<Seller, Seller>() {
			private final Button button = new Button("edit");

			@Override
			protected void updateItem(Seller obj, boolean empty) {
				super.updateItem(obj, empty);
				if (obj == null) {
					setGraphic(null);
					return;
				}
				setGraphic(button);
				button.setOnAction(
						event -> createDialogForm(obj, "/gui/SellerForm.fxml", Utils.currentStage(event)));
			}
		});
	}

	private void initRemoveButtons() {
		tableColumnRemove.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue()));
		tableColumnRemove.setCellFactory(param -> new TableCell<Seller, Seller>() {
			private final Button button = new Button("remove");

			@Override
			protected void updateItem(Seller obj, boolean empty) {
				super.updateItem(obj, empty);
				if (obj == null) {
					setGraphic(null);
					return;
				}
				setGraphic(button);
				button.setOnAction(event -> removeEntity(obj));
			}
		});
	}

	private void removeEntity(Seller obj) {
		Optional<ButtonType> confirmation = Alerts.showConfirmation("Seller deletion",
				"Are you sure you want to delete this seller?");
		if (confirmation.get() == ButtonType.OK) {
			if (service == null)
				throw new IllegalStateException("Service not instantiated.");
			try {
				service.remove(obj);
				updateTableView();
			} catch (DbIntegrityException e) {
				Alerts.showAlert("Error removing seller", null, e.getMessage(), AlertType.ERROR);
			}
		}
	}

}
