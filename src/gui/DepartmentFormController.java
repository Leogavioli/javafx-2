package gui;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import db.DbException;
import gui.listeners.DataChangeListener;
import gui.util.Alerts;
import gui.util.Constraints;
import gui.util.Utils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import model.entities.Department;
import model.exceptions.ValidationException;
import model.services.DepartmentService;

public class DepartmentFormController implements Initializable{
	
	private Department entity;//injecting depedency
	
	private DepartmentService service;
	
	private List<DataChangeListener> dataChangeListeners = new ArrayList<>();
	
	@FXML
	private TextField idTextField;
	
	@FXML
	private TextField nameTextField;
	
	@FXML
	private Label errorLabel;
	
	@FXML
	private Button saveButton;
	
	@FXML
	private Button cancelButton;
	
	public void setDepartment(Department entity) {
		this.entity=entity;
	}
	
	public void setDepartmentService(DepartmentService service) {
		this.service=service;
	}
	
	public void subscribeDataChangeListener(DataChangeListener listener) {
		dataChangeListeners.add(listener);
		//adds a listeners to the list of interested observers to 'watch out' for events
	}
	
	private void notifyDataChange() {
		for(DataChangeListener listener: dataChangeListeners) {
			listener.onDataChanged();
		}
	}
	
	@FXML
	public void onSaveBtAction(ActionEvent event) {
		if(entity==null)//if not instantiated
			throw new IllegalStateException("Entity was not instantiated!");
		if(service==null)//since not using framework for dependency injection
			throw new IllegalStateException("Service was null");
		try {
			entity=getFormData();//problem is in getFormData
			service.saveOrUpdate(entity);
			notifyDataChange();
			Utils.currentStage(event).close();
		}
		catch(ValidationException e) {
			setErrorMessages(e.getErrors());
			//setErrorMessages(e.getMessage()); could be simpler, like this
		}
		catch(DbException e) {
			Alerts.showAlert("DB Exception", null, e.getMessage(), AlertType.ERROR);
		}
	}
	
	

	private Department getFormData(){
		Department obj = new Department();
		
		//instantiate the custom exception
		ValidationException exception = new ValidationException("Validation Error");
		
		obj.setId(Utils.tryParseToInt(idTextField.getText()));
		
		//if no dep selected it will be null, this doesn't allow to be saved null
		if(nameTextField.getText()==null || nameTextField.getText().trim().equals("")) {
			exception.addError("name", "Field can't be empty");
			//throw exception;
		}
		obj.setName(nameTextField.getText());
		
		//could be inside upper if, however forms with more data require more ifs, so better practice...
		if(exception.getErrors().size()>0)
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
		Constraints.setTextFieldMaxLength(nameTextField, 30);
	}
	//since it will work for new/old departments - this method allows you to see selected deps
	public void updateFormData() {
		if(entity==null)
			throw new IllegalStateException("Department was null");
		idTextField.setText(String.valueOf(entity.getId()));
		nameTextField.setText(entity.getName());
	}
	
	private void setErrorMessages(Map<String,String> errors) {
		Set<String> set = errors.keySet();
		if(set.contains("name"))
			errorLabel.setText(errors.get("name"));
	}
	
	
	
	
	

}
