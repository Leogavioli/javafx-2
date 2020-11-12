package gui;

import java.io.IOException;
import java.net.URL;
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
import model.services.DepartmentService;

public class DepartmentListController implements Initializable, DataChangeListener {

	private DepartmentService service;

	@FXML
	private TableView<Department> tableViewDepartment;

	@FXML
	private TableColumn<Department, Integer> tableColumnId;

	@FXML
	private TableColumn<Department, String> tableColumnName;

	@FXML
	private TableColumn<Department, Department> tableColumnEdit;

	@FXML
	private TableColumn<Department, Department> tableColumnRemove;

	@FXML
	private Button newBt;

	// for table view visualization
	private ObservableList<Department> obsList;

	@FXML
	public void onNewBtAction(ActionEvent event) {
		Stage stage = Utils.currentStage(event);
		Department dep = new Department();
		createDialogForm(dep, "/gui/DepartmentForm.fxml", stage);
	}

	// dependency injection
	public void setDepartmentService(DepartmentService service) {
		this.service = service;
	}

	@Override
	public void initialize(URL url, ResourceBundle rb) {
		initializeNodes();
		/*
		 * could be initialized here however it's better practice to add it directly to
		 * the loadView method through a Consumer<T>
		 * 
		 * setDepartmentService(new DepartmentService()); updateTableView();
		 */
	}

	private void initializeNodes() {
		// JavaFX standard to initialize TableView
		tableColumnId.setCellValueFactory(new PropertyValueFactory<>("id"));
		tableColumnName.setCellValueFactory(new PropertyValueFactory<>("name"));
		// Bind TableView Height with stage height
		Stage stage = (Stage) Main.getMainScene().getWindow();
		tableViewDepartment.prefHeightProperty().bind(stage.heightProperty());

	}

	public void updateTableView() {
		// check if service was initialized since it's hard coded/no framework
		if (service == null)
			throw new IllegalStateException("Service not initialized");

		// List<Department>list = service.findAll(); -- you can call it from the
		// method's argument
		// how to instantiate a obsList AND findAll returns a list
		obsList = FXCollections.observableArrayList(service.findAll());
		tableViewDepartment.setItems(obsList);

		// would also work in initializeNodes
		initEditButtons();
		initRemoveButtons();

	}

	private void createDialogForm(Department obj, String absoluteName, Stage parentStage) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource(absoluteName));
			Pane pane = loader.load();

			DepartmentFormController controller = loader.getController();// url passed DepForm.fxml-gets contr
			controller.setDepartment(obj);
			controller.setDepartmentService(new DepartmentService());
			controller.subscribeDataChangeListener(this);
			controller.updateFormData();

			// create a new stage to load DepartmentForm dialog box
			Stage dialogStage = new Stage();
			// config stage
			dialogStage.setTitle("Enter Department data");
			dialogStage.setScene(new Scene(pane));// pane is the parent of the new scene
			dialogStage.setResizable(false);
			dialogStage.initOwner(parentStage);// establish the parent so it locks the window
			dialogStage.initModality(Modality.WINDOW_MODAL);
			dialogStage.showAndWait();// makes it show the window

		} catch (IOException e) {
			Alerts.showAlert("IO Exception", "Error loading view", e.getMessage(), AlertType.ERROR);
		}

	}

	@Override
	public void onDataChanged() {
		updateTableView();
	}

	// framework specific method for creating an element inside a cell
	private void initEditButtons() {
		tableColumnEdit.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue()));
		tableColumnEdit.setCellFactory(param -> new TableCell<Department, Department>() {
			private final Button button = new Button("edit");

			@Override
			protected void updateItem(Department obj, boolean empty) {
				super.updateItem(obj, empty);
				if (obj == null) {
					setGraphic(null);
					return;
				}
				setGraphic(button);
				button.setOnAction(
						event -> createDialogForm(obj, "/gui/DepartmentForm.fxml", Utils.currentStage(event)));
			}
		});
	}

	private void initRemoveButtons() {
		tableColumnRemove.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue()));
		tableColumnRemove.setCellFactory(param -> new TableCell<Department, Department>() {
			private final Button button = new Button("remove");

			@Override
			protected void updateItem(Department obj, boolean empty) {
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

	private void removeEntity(Department obj) {
		Optional<ButtonType> confirmation = Alerts.showConfirmation("Department deletion",
				"Are you sure you want to delete this department?");
		if (confirmation.get() == ButtonType.OK) {
			if (service == null)
				throw new IllegalStateException("Service not instantiated.");
			try {
				service.remove(obj);
				updateTableView();
			} catch (DbIntegrityException e) {
				Alerts.showAlert("Error removing department", null, e.getMessage(), AlertType.ERROR);
			}
		}
	}

}
