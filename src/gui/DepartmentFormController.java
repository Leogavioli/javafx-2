package gui;

import java.net.URL;
import java.util.ResourceBundle;

import db.DbException;
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
import model.services.DepartmentService;

public class DepartmentFormController implements Initializable{
	
	private Department entity;//injecting depedency
	
	private DepartmentService service;
	
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
	
	@FXML
	public void onSaveBtAction(ActionEvent event) {
		if(entity==null)//if not instantiated
			throw new IllegalStateException("Entity was not instantiated!");
		if(service==null)//since not using framwork for dependency injection
			throw new IllegalStateException("Service was null");
		try {
			entity=getFormData();
			service.saveOrUpdate(entity);
			Utils.currentStage(event).close();
		}catch(DbException e) {
			Alerts.showAlert("DB Exception", null, e.getMessage(), AlertType.ERROR);
		}
	}
	
	private Department getFormData() {
		Department obj = new Department();
		obj.setId(Utils.tryParseToInt(idTextField.getText()));
		obj.setName(nameTextField.getText());
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
	
	public void updateFormData() {
		if(entity==null)
			throw new IllegalStateException("Department was null");
		idTextField.setText(String.valueOf(entity.getId()));
		nameTextField.setText(entity.getName());
	}
	
	
	

}
