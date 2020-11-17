package gui;

import java.net.URL;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import db.DbException;
import gui.listeners.DataChangeListener;
import gui.util.Alerts;
import gui.util.Constraints;
import gui.util.Utils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.util.Callback;
import model.entities.Department;
import model.entities.Seller;
import model.exceptions.ValidationException;
import model.services.DepartmentService;
import model.services.SellerService;

public class SellerFormController implements Initializable {

	private Seller entity;

	private SellerService service;

	private DepartmentService depService;

	private List<DataChangeListener> dataChangeListeners = new ArrayList<>();

	@FXML
	private TextField idTextField;

	@FXML
	private TextField nameTextField;

	@FXML
	private TextField emailTextField;

	@FXML
	private DatePicker dpBirthDate;

	@FXML
	private TextField baseSalaryTextField;

	@FXML
	private ComboBox<Department> departmentComboBox;

	@FXML
	private Label nameErrorLabel;

	@FXML
	private Label emailErrorLabel;

	@FXML
	private Label birthdateErrorLabel;

	@FXML
	private Label salaryErrorLabel;

	@FXML
	private Button saveButton;

	@FXML
	private Button cancelButton;

	private ObservableList<Department> obsList;

	public void setSeller(Seller entity) {
		this.entity = entity;
	}

	public void setServices(SellerService service, DepartmentService depService) {
		this.service = service;
		this.depService = depService;
	}

	public void subscribeDataChangeListener(DataChangeListener listener) {
		dataChangeListeners.add(listener);
	}

	private void notifyDataChange() {
		for (DataChangeListener listener : dataChangeListeners) {
			listener.onDataChanged();
		}
	}

	@FXML
	public void onSaveBtAction(ActionEvent event) {
		if (entity == null)
			throw new IllegalStateException("Entity was not instantiated!");
		if (service == null)
			throw new IllegalStateException("Service was null");
		try {
			entity = getFormData();
			service.saveOrUpdate(entity);
			notifyDataChange();
			Utils.currentStage(event).close();
		} catch (ValidationException e) {
			setErrorMessages(e.getErrors());
		} catch (DbException e) {
			Alerts.showAlert("DB Exception", null, e.getMessage(), AlertType.ERROR);
		}
	}

	private Seller getFormData() {
		Seller obj = new Seller();
		ValidationException exception = new ValidationException("Validation Error");

		obj.setId(Utils.tryParseToInt(idTextField.getText()));
		
		if (nameTextField.getText() == null || nameTextField.getText().trim().equals(""))
			exception.addError("name", "Field can't be empty");

		obj.setName(nameTextField.getText());

		if (emailTextField.getText() == null || emailTextField.getText().trim().equals(""))
			exception.addError("email", "Field can't be empty");

		obj.setEmail(emailTextField.getText());
		
		if(dpBirthDate.getValue()==null)
			exception.addError("birthdate", "Field can't be empty");
		else {
			//convert day from computer time to instant
			Instant instant = Instant.from(dpBirthDate.getValue().atStartOfDay(ZoneId.systemDefault()));
			obj.setBirthDate(Date.from(instant));
		}

		if (baseSalaryTextField.getText() == null || baseSalaryTextField.getText().trim().equals(""))
			exception.addError("baseSalary", "Field can't be empty");
		
		obj.setBaseSalary(Utils.tryParseToDouble(baseSalaryTextField.getText()));
		
		obj.setDepartment(departmentComboBox.getValue());

		if (exception.getErrors().size() > 0)
			throw exception;

		return obj;
	}

	@FXML
	public void onCancelBtAction(ActionEvent event) {
		Utils.currentStage(event).close();
	}

	@Override
	public void initialize(URL url, ResourceBundle rb) {
		initializeNodes();
	}

	private void initializeNodes() {
		Constraints.setTextFieldInteger(idTextField);
		Constraints.setTextFieldMaxLength(nameTextField, 70);
		Constraints.setTextFieldDouble(baseSalaryTextField);
		Constraints.setTextFieldMaxLength(emailTextField, 50);
		Utils.formatDatePicker(dpBirthDate, "dd/MM/yyyy");
		initializeComboBoxDepartment();
	}

	public void loadAssociatedObjects() {
		if(depService==null)
			throw new IllegalStateException("DepartmentService was not instantiated");
		obsList = FXCollections.observableArrayList(depService.findAll());
		departmentComboBox.setItems(obsList);
	}
	
	// since it will work for new/old departments - this method allows you to see
	// selected sellers
	public void updateFormData() {
		if (entity == null)
			throw new IllegalStateException("Seller was null");
		idTextField.setText(String.valueOf(entity.getId()));
		nameTextField.setText(entity.getName());
		emailTextField.setText(entity.getEmail());
		if (entity.getBirthDate() != null)
			dpBirthDate.setValue(LocalDate.ofInstant(entity.getBirthDate().toInstant(), ZoneId.systemDefault()));
		baseSalaryTextField.setText(String.format("%.2f", entity.getBaseSalary()));
		if(entity.getDepartment()==null) {
			//standard for new sellers which have no department yet/in process of registration
			departmentComboBox.getSelectionModel().selectFirst();
		}
		else
			departmentComboBox.setValue(entity.getDepartment());
	}

	private void setErrorMessages(Map<String, String> errors) {
		Set<String> set = errors.keySet();
		
		nameErrorLabel.setText(set.contains("name")?errors.get("name"): "");
		
		emailErrorLabel.setText(set.contains("email")?errors.get("email"): "");
		
		salaryErrorLabel.setText(set.contains("baseSalary")?errors.get("baseSalary"): "");
		
		birthdateErrorLabel.setText(set.contains("birthdate")?errors.get("birthdate"): "");
	}
	
	//pre-tested method to initialize ComboBoxes 
	private void initializeComboBoxDepartment() {
		Callback<ListView<Department>, ListCell<Department>> factory = lv -> new ListCell<Department>() {
			@Override
			protected void updateItem(Department item, boolean empty) {
				super.updateItem(item, empty);
				setText(empty ? "" : item.getName());
			}
		};
		departmentComboBox.setCellFactory(factory);
		departmentComboBox.setButtonCell(factory.call(null));
	}

}
