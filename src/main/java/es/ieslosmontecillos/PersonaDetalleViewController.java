package es.ieslosmontecillos;

import javafx.event.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.util.StringConverter;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

public class PersonaDetalleViewController {
    @FXML
    private TextField textFieldNombre;
    @FXML
    private TextField textFieldApellidos;
    @FXML
    private TextField textFieldTelefono;
    @FXML
    private TextField textFieldEmail;
    @FXML
    private DatePicker datePickerFechaNacimiento;
    @FXML
    private TextField textFieldNumHijos;
    @FXML
    private RadioButton radioButtonSoltero;
    @FXML
    private RadioButton radioButtonCasado;
    @FXML
    private RadioButton radioButtonViudo;
    @FXML
    private TextField textFieldSalario;
    @FXML
    private CheckBox checkBoxJubilado;
    @FXML
    private ImageView imageViewFoto;
    @FXML
    private ComboBox<Provincia> comboBoxProvincia;

    private Pane rootAgendaView;
    private TableView tableViewPrevio;
    private Persona persona;
    private DataUtil dataUtil;
    private boolean nuevaPersona;
    public static final char CASADO = 'C';
    public static final char SOLTERO = 'S';
    public  static final char VIUDO = 'V';
    public static final String CARPETA_FOTOS = "img";

    @FXML
    private AnchorPane rootPersonaDetalleView;

    public void setTableViewPrevio(TableView tableViewPrevio) {
        this.tableViewPrevio = tableViewPrevio;
    }

    public void setPersona(Persona persona, Boolean nuevaPersona) {
        if (!nuevaPersona) {
            this.persona = persona;
        } else {
            this.persona = new Persona();
        }
        this.nuevaPersona = nuevaPersona;
    }

    public void mostrarDatos() throws ParseException {
        textFieldNombre.setText(persona.getNombre());
        textFieldApellidos.setText(persona.getApellidos());
        textFieldTelefono.setText(persona.getTelefono());
        textFieldEmail.setText(persona.getEmail());
        if (persona.getNumHijos() != null) {
            textFieldNumHijos.setText(persona.getNumHijos().toString());
        }
        if (persona.getSalario() != null) {
            textFieldSalario.setText(persona.getSalario().toString());
        }
        if (persona.getJubilado() != null && persona.getJubilado() == 1) {
            checkBoxJubilado.setSelected(true);
        } else {
            checkBoxJubilado.setSelected(false);
        }
        if (persona.getEstadoCivil() != null) {
            switch (persona.getEstadoCivil()) {
                case "C":
                    radioButtonCasado.setSelected(true);
                    break;
                case "S":
                    radioButtonSoltero.setSelected(true);
                    break;
                case "V":
                    radioButtonViudo.setSelected(true);
                    break;
            }
        }
        if (persona.getFechaNacimiento() != null) {
            SimpleDateFormat formato = new SimpleDateFormat("yyyy-MM-dd");
            Date fecNac = formato.parse(persona.getFechaNacimiento());
            LocalDate fechaNac =
                    fecNac.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            datePickerFechaNacimiento.setValue(fechaNac);
        }
        comboBoxProvincia.setItems(dataUtil.getOlProvincias());
        if (persona.getProvincia() != null) {
            comboBoxProvincia.setValue(persona.getProvincia());
        }
        comboBoxProvincia.setCellFactory(
                (ListView<Provincia> l) -> new ListCell<Provincia>() {
                    @Override
                    protected void updateItem(Provincia provincia, boolean empty) {
                        super.updateItem(provincia, empty);
                        if (provincia == null || empty) {
                            setText("");
                        } else {
                            setText(provincia.getCodigo() + "-" + provincia.getNombre());
                        }
                    }
                });
        comboBoxProvincia.setConverter(new StringConverter<Provincia>() {
            @Override
            public String toString(Provincia provincia) {
                if (provincia == null) {
                    return null;
                } else {
                    return provincia.getCodigo() + "-" + provincia.getNombre();
                }
            }

            @Override
            public Provincia fromString(String userId) {
                return null;
            }
        });
        if (persona.getFoto() != null){
            String imageFileName=persona.getFoto();
            File file = new File(CARPETA_FOTOS+"/"+imageFileName);
            if (file.exists()){
                Image image = new Image(file.toURI().toString());
                imageViewFoto.setImage(image);
            } else {
                Alert alert=new Alert(Alert.AlertType.INFORMATION,"No se encuentra la imagen en "+file.toURI().toString());
                        alert.showAndWait();
            }
        }
    }

    public void setRootAgendaView(Pane rootAgendaView) {
        this.rootAgendaView = rootAgendaView;
    }

    @FXML
    private void onActionButtonExaminar(ActionEvent event){
        File carpetaFotos = new File(CARPETA_FOTOS);
        if (!carpetaFotos.exists()){
            carpetaFotos.mkdir();
        }
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar imagen");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Imágenes (jpg, png)", "*.jpg","*.png"),
                new FileChooser.ExtensionFilter("Todos los archivos","*.*"));
        File file = fileChooser.showOpenDialog(
                rootPersonaDetalleView.getScene().getWindow());
        if (file != null){
            try {
                Files.copy(file.toPath(),new File(CARPETA_FOTOS+
                        "/"+file.getName()).toPath());
                persona.setFoto(file.getName());
                Image image = new Image(file.toURI().toString());
                imageViewFoto.setImage(image);
            } catch (FileAlreadyExistsException ex){
                Alert alert = new Alert(Alert.AlertType.WARNING,"Nombre de archivo duplicado");
                        alert.showAndWait();
            } catch (IOException ex){
                Alert alert = new Alert(Alert.AlertType.WARNING,"No se ha podido guardar la imagen");
                        alert.showAndWait();
            }
        }
    }

    @FXML
    public void onActionButtonGuardar(ActionEvent event) {

        boolean errorFormato = false;

        if (!textFieldNumHijos.getText().isEmpty()){
            try {
                persona.setNumHijos(Integer.valueOf(textFieldNumHijos.getText()));
            } catch(NumberFormatException ex){
                errorFormato = true;
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Número de hijos no válido");
                alert.showAndWait();
                textFieldNumHijos.requestFocus();
            }
        }

        if (!textFieldSalario.getText().isEmpty()){
            try {
                Double dSalario =
                        Double.valueOf(Double.valueOf(textFieldSalario.getText()).doubleValue()
                        );
                persona.setSalario(dSalario);
            } catch(NumberFormatException ex) {
                errorFormato = true;
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Salario no válido");
                        alert.showAndWait();
                textFieldSalario.requestFocus();
            }
        }

        if(checkBoxJubilado.isSelected()){
            Integer jubilado = 1;
            persona.setJubilado(jubilado);
        };

        if (radioButtonCasado.isSelected()){
            persona.setEstadoCivil(String.valueOf(CASADO));
        } else if (radioButtonSoltero.isSelected()){
            persona.setEstadoCivil(String.valueOf(SOLTERO));
        } else if (radioButtonViudo.isSelected()){
            persona.setEstadoCivil(String.valueOf(VIUDO));
        }

        if (datePickerFechaNacimiento.getValue() != null){
            LocalDate localDate = datePickerFechaNacimiento.getValue();
            ZonedDateTime zonedDateTime =
                    localDate.atStartOfDay(ZoneId.systemDefault());
            Instant instant = zonedDateTime.toInstant();
            Date date = Date.from(instant);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String fechaComoCadena = sdf.format(date);
            persona.setFechaNacimiento(fechaComoCadena);
        } else {
            persona.setFechaNacimiento(null);
        }

        if (comboBoxProvincia.getValue() != null){
            persona.setProvincia(comboBoxProvincia.getValue());
        } else {
            Alert alert = new Alert(Alert.AlertType.INFORMATION,"Debe indicar una provincia");
                    alert.showAndWait();
            errorFormato = true;
        }

        if (!errorFormato) { // Los datos introducidos son correctos

            try {
                StackPane rootMain = (StackPane) rootPersonaDetalleView.getScene().getRoot();
                rootMain.getChildren().remove(rootPersonaDetalleView);
                rootAgendaView.setVisible(true);

                persona.setNombre(textFieldNombre.getText());
                persona.setApellidos(textFieldApellidos.getText());
                persona.setTelefono(textFieldTelefono.getText());
                persona.setEmail(textFieldEmail.getText());
                if (nuevaPersona) {
                    dataUtil.addPersona(persona);
                } else {
                    dataUtil.actualizarPersona(persona);
                }
                int numFilaSeleccionada;
                if (nuevaPersona) {
                    tableViewPrevio.getItems().add(persona);
                    numFilaSeleccionada = tableViewPrevio.getItems().size() - 1;
                    tableViewPrevio.getSelectionModel().select(numFilaSeleccionada);
                    tableViewPrevio.scrollTo(numFilaSeleccionada);
                } else {
                    numFilaSeleccionada =
                            tableViewPrevio.getSelectionModel().getSelectedIndex();
                    tableViewPrevio.getItems().set(numFilaSeleccionada, persona);
                }
                TablePosition pos = new TablePosition(tableViewPrevio,
                        numFilaSeleccionada, null);
                tableViewPrevio.getFocusModel().focus(pos);
                tableViewPrevio.requestFocus();
            } catch (Exception ex) { //Los datos introducidos no cumplen requisitos
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setHeaderText("No se han podido guardar los cambios. " + "Compruebe que los datos cumplen los requisitos");
                        alert.setContentText(ex.getLocalizedMessage());
                alert.showAndWait();
            }
        }

    }

    @FXML
    @Deprecated
    private void onActionButtonCancelar(ActionEvent event) {
        StackPane rootMain = (StackPane) rootPersonaDetalleView.getScene().getRoot();
        rootMain.getChildren().remove(rootPersonaDetalleView);
        rootAgendaView.setVisible(true);

        int numFilaSeleccionada = tableViewPrevio.getSelectionModel().getSelectedIndex();
        TablePosition pos = new TablePosition(tableViewPrevio,
                numFilaSeleccionada, null);
        tableViewPrevio.getFocusModel().focus(pos);
        tableViewPrevio.requestFocus();
    }

    @FXML
    public void initialize() {
    }

    public void setRootAgendaView(AnchorPane rootAgendaView) {
        this.rootAgendaView = rootAgendaView;
    }

    public AnchorPane getRootAgendaView() {
        return (AnchorPane) rootAgendaView;
    }

    public void setDataUtil(DataUtil dataUtil) {
        this.dataUtil = dataUtil;
    }

    public DataUtil getDataUtil() {
        return dataUtil;
    }
}