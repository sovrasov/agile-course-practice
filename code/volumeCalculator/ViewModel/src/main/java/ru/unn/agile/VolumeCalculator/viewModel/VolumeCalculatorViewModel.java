package ru.unn.agile.VolumeCalculator.viewModel;

import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class VolumeCalculatorViewModel {
    private final BooleanProperty isParam1Visible = new SimpleBooleanProperty(false);
    private final BooleanProperty isParam2Visible = new SimpleBooleanProperty(false);

    private final StringProperty param1Name = new SimpleStringProperty("Param1");
    private final StringProperty param2Name = new SimpleStringProperty("Param2");
    private final StringProperty param1Value = new SimpleStringProperty();
    private final StringProperty param2Value = new SimpleStringProperty();

    private final ObjectProperty<EVolumeTypes> selectedVolumeItem = new SimpleObjectProperty<>();
    private final ObjectProperty<ObservableList<EVolumeTypes>> volumeTypeListItems =
            new SimpleObjectProperty<>(FXCollections.observableArrayList(EVolumeTypes.values()));

    private final BooleanProperty isCalculateDisable = new SimpleBooleanProperty(true);

    private final StringProperty resultVolume = new SimpleStringProperty("");
    private final StringProperty validationMsg = new SimpleStringProperty("");

    private final StringProperty logs = new SimpleStringProperty();
    private IVolumeCalculatorLogger logger;

    private List<ValueCachingChangeListener> valueChangedListeners;

    public final void setLogger(final IVolumeCalculatorLogger logger) {
        if (logger == null) {
            throw new IllegalArgumentException("Logger parameter can't be null");
        }
        this.logger = logger;
    }

    public VolumeCalculatorViewModel() {
        init();
    }

    public VolumeCalculatorViewModel(final IVolumeCalculatorLogger logger) {
        setLogger(logger);
        init();
    }

    private void init() {
        selectedVolumeItem.addListener(new ChangeListener<EVolumeTypes>() {
            @Override
            public void changed(final ObservableValue<? extends EVolumeTypes> observableValue,
                                final  EVolumeTypes oldValue, final  EVolumeTypes newValue) {
                setParam1ValueProperty(null);
                setParam2ValueProperty(null);
                setResultVolumeProperty("");
                changeParametersVisible(newValue);
                changeParameterNames(newValue);
            }
        });

        param1Value.addListener(new ParamFieldListener());
        param2Value.addListener(new ParamFieldListener());

        final List<StringProperty> values = new ArrayList<StringProperty>() { {
            add(param1Value);
            add(param2Value);
        } };

        valueChangedListeners = new ArrayList<>();
        for (StringProperty value : values) {
            final ValueCachingChangeListener listener = new ValueCachingChangeListener();
            value.addListener(listener);
            valueChangedListeners.add(listener);
        }
    }

    public final void setParam1VisibleProperty(final boolean value) {
        isParam1Visible.setValue(value);
    }

    public BooleanProperty getParam1VisibleProperty() {
        return isParam1Visible;
    }

    public final void setParam2VisibleProperty(final boolean value) {
        isParam2Visible.setValue(value);
    }

    public BooleanProperty getParam2VisibleProperty() {
        return isParam2Visible;
    }

    public void setParam1Name(final String value) {
        param1Name.setValue(value);
    }

    public StringProperty getParam1Name() {
        return param1Name;
    }

    public void setParam2Name(final String value) {
        param2Name.setValue(value);
    }

    public StringProperty getParam2Name() {
        return param2Name;
    }

    public void setParam1ValueProperty(final String value) {
        param1Value.setValue(value);
    }

    public StringProperty getParam1ValueProperty() {
        return param1Value;
    }

    public void setParam2ValueProperty(final String value) {
        param2Value.setValue(value);
    }

    public StringProperty getParam2ValueProperty() {
        return param2Value;
    }

    public final void setSelectedVolumeItem(final EVolumeTypes item) {
        selectedVolumeItem.setValue(item);
    }

    public EVolumeTypes getSelectedVolumeItem() {
        return selectedVolumeItem.getValue();
    }

    public ObjectProperty<EVolumeTypes> getSelectedItemProperty() {
        return selectedVolumeItem;
    }

    public ObjectProperty<ObservableList<EVolumeTypes>> getVolumeTypeListItemsProperty() {
        return volumeTypeListItems;
    }

    public final ObservableList<EVolumeTypes> getVolumeTypeListItems() {
        return volumeTypeListItems.get();
    }

    public final void setCalculateDisableProperty(final Boolean value) {
        isCalculateDisable.setValue(value);
    }

    public BooleanProperty getCalculateDisableProperty() {
        return isCalculateDisable;
    }

    public final void setResultVolumeProperty(final String value) {
        resultVolume.setValue(value);
    }

    public StringProperty getResultVolumeProperty() {
        return resultVolume;
    }

    public void setValidationMsgProperty(final String value) {
        validationMsg.setValue(value);
    }

    public StringProperty getValidationMsgProperty() {
        return validationMsg;
    }

    public StringProperty logsProperty() {
        return logs;
    }

    public final String getLogs() {
        return logs.get();
    }

    public final List<String> getLog() {
        return logger.getLog();
    }

    private void changeParametersVisible(final EVolumeTypes newValue) {
        switch (newValue) {
            case TETRAHEDRON:
            case CUBE:
            case SPHERE:
                setParam1VisibleProperty(true);
                setParam2VisibleProperty(false);
                break;
            case PYRAMID:
            case CYLINDER:
            case CONE:
                setParam1VisibleProperty(true);
                setParam2VisibleProperty(true);
                break;
            default:
                setParam1VisibleProperty(false);
                setParam2VisibleProperty(false);
                break;
        }
    }

    private void changeParameterNames(final EVolumeTypes newValue) {
        switch (newValue) {
            case CUBE:
            case TETRAHEDRON:
                setParam1Name("Edge");
                break;
            case SPHERE:
                setParam1Name("Radius");
                break;
            case PYRAMID:
                setParam1Name("Area");
                setParam2Name("Height");
                break;
            case CONE:
            case CYLINDER:
                setParam1Name("Radius");
                setParam2Name("Height");
                break;
            default:
                setParam1Name("Param1");
                setParam2Name("Param2");
                break;
        }
    }

    private double tryParseDouble(final String s) {
        double result = 0;
        try {
            result = Double.parseDouble(s);
        } catch (Exception e) {
            return 0;
        }
        return result;
    }

    public void calculate() {
        double param1 = tryParseDouble(getParam1ValueProperty().getValue());
        double param2 = tryParseDouble(getParam2ValueProperty().getValue());
        double result = selectedVolumeItem.get().getVolume(param1, param2);
        setResultVolumeProperty(String.valueOf(result));

        StringBuilder message = new StringBuilder(LogMessages.CALCULATE_WAS_PRESSED);
        message.append("Arguments");
        if (getParam1VisibleProperty().get()) {
            message.append(": param1 = ").append(param1);
        }
        if (getParam2VisibleProperty().get()) {
            message.append("; param2 = ").append(param2);
        }
        message.append(" Volume Type: ").append(selectedVolumeItem.get().toString()).append(".");
        logger.addLog(message.toString());
        updateLogs();
    }

    private class ParamFieldListener implements ChangeListener<String> {
        @Override
        public void changed(final ObservableValue<? extends String> observableValue,
                            final String oldValue, final String newValue) {
            setCalculateDisableProperty(!validation());
        }
    }

    private boolean validation() {
        ArrayList<EVolumeTypes> typesWithOneParameter =
                new ArrayList<EVolumeTypes>(Arrays.asList(
                        EVolumeTypes.SPHERE,
                        EVolumeTypes.CUBE,
                        EVolumeTypes.TETRAHEDRON
                        ));
        try {
            if (getParam1ValueProperty().getValue() != null
                    && getParam1ValueProperty().getValue() != ""
                    && Double.parseDouble(getParam1ValueProperty().getValue()) < 0) {
                setValidationMsgProperty(getParam1Name().getValue() + " must be positive");
                return false;
            }
            setValidationMsgProperty("");
            if (typesWithOneParameter.contains(getSelectedVolumeItem())) {
                return true;
            }
        } catch (Exception e) {
            setValidationMsgProperty(getParam1Name().getValue() + " is not valid");
            return false;
        }

        try {
            if (getParam2ValueProperty().getValue() != null
                    && getParam2ValueProperty().getValue() != "") {
                if (Double.parseDouble(getParam2ValueProperty().getValue()) < 0) {
                    setValidationMsgProperty(getParam2Name().getValue() + " must be positive");
                    return false;
                }
                setValidationMsgProperty("");
                return true;
            }
        } catch (Exception e) {
            setValidationMsgProperty(getParam2Name().getValue() + " is not valid");
            return false;
        }

        setValidationMsgProperty("");
        return false;
    }

    private void updateLogs() {
        List<String> fullLog = logger.getLog();
        String record = new String();
        for (String log : fullLog) {
            record += log + "\n";
        }
        logs.set(record);
    }

    public void onVolumeTypeChanged(final EVolumeTypes oldValue, final EVolumeTypes newValue) {
        if (oldValue != null && oldValue.equals(newValue)) {
            return;
        }
        StringBuilder message = new StringBuilder(LogMessages.OPERATION_WAS_CHANGED);
        message.append(newValue.toString());
        logger.addLog(message.toString());
        updateLogs();
    }

    public void onFocusChanged(final Boolean oldValue, final Boolean newValue) {
        if (oldValue != null && !oldValue && newValue) {
            return;
        }

        for (ValueCachingChangeListener listener : valueChangedListeners) {
            if (listener.isChanged()) {
                StringBuilder message = new StringBuilder(LogMessages.EDITING_FINISHED);
                message.append("Input arguments are: [");
                if (getParam1VisibleProperty().get()) {
                    message.append(param1Value.get());
                }
                if (getParam2VisibleProperty().get()) {
                    message.append("; ").append(param2Value.get());
                }
                message.append("]");
                logger.addLog(message.toString());
                updateLogs();
                listener.cache();
                break;
            }
        }
    }

    private class ValueCachingChangeListener implements ChangeListener<String> {
        private String curValue = new String();
        private String prevValue = new String();
        @Override
        public void changed(final ObservableValue<? extends String> observableValue,
                            final String oldValue, final String newValue) {
            if (oldValue != null && oldValue.equals(newValue)) {
                return;
            }
            curValue = newValue;
        }
        public boolean isChanged() {
            return !prevValue.equals(curValue);
        }
        public void cache() {
            prevValue = curValue;
        }
    }
}

final class LogMessages {
    public static final String CALCULATE_WAS_PRESSED = "Calculate. ";
    public static final String OPERATION_WAS_CHANGED = "Operation was changed to ";
    public static final String EDITING_FINISHED = "Updated input. ";

    private LogMessages() { }
}
