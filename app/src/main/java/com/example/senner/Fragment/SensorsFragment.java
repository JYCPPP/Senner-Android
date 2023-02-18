package com.example.senner.Fragment;

import static android.hardware.Sensor.TYPE_ALL;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.senner.R;
import com.example.senner.UI.ChartView;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LegendEntry;
import com.github.mikephil.charting.data.Entry;
import com.ramotion.foldingcell.FoldingCell;

import java.util.ArrayList;
import java.util.List;

public class SensorsFragment extends Fragment {

    //sensor manager
    private SensorManager mSManager;
    public List<Sensor> listofSensor;

    //view
    private TextView linearacc_data;
    private TextView acc_data;
    private TextView gyro_data;
    private TextView step_data;
    private TextView rotation_vector_data;
    private TextView magnetic_rotation_data;
    private TextView magnetic_data;
    private TextView proximity_data;
    private TextView light_data;
    private TextView temperature_data;
    private TextView pressure_data;
    private TextView humidity_data;

    private FoldingCell linearacc_card,
                        acc_card,
                        gyro_card,
                        step_card,
                        rotation_vector_card,
                        magnetic_vector_card,
                        magnetic_card,
                        proximity_card,
                        light_card,
                        temperature_card,
                        pressure_card,
                        humidity_card;
    //charts
    private LineChart   LinearAccelerationChart,
                        AccelerationChart,
                        GyroscopeChart,
                        StepChart,
                        RotationChart,
                        MagneticRotationChart,
                        MagneticChart,
                        ProximityChart,
                        LightChart,
                        TemperatureChart,
                        PressureChart,
                        HumidityChart;


   private final ChartView chartView = new ChartView();
   private SensorEventListener linearacc_sensorEventListener,
                               acc_sensorEventListener,
                               gyro_sensorEventListener,
                               step_sensorEventListener,
                               rotation_vector_sensorEventListener,
                               magnetic_vector_sensorEventListener,
                               magnetic_sensorEventListener,
                               proximity_sensorEventListener,
                               light_sensorEventListener,
                               temperature_sensorEventListener,
                               pressure_sensorEventListener,
                               humidity_sensorEventListener;

   private ArrayList<Entry> linearaccX, linearaccY, linearaccZ,
                            accX, accY, accZ,
                            gyroX, gyroY, gyroZ,
                            step,
                            rotX, rotY, rotZ,
                            mrotX, mrotY, mrotZ,
                            magX, magY, magZ,
                            proximity,
                            light,
                            temperature,
                            pressure,
                            humidity;

   private SeekBar seekBar_linearacc,
                    seekBar_acc,
                    seekBar_gyro,
                    seekBar_rot,
                    seekBar_mrot,
                    seekBar_mag,
                    seekBar_step,
                    seekBar_proximity,
                    seekBar_light,
                    seekBar_temp,
                    seekBar_pressure,
                    seekBar_humidity;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(com.example.senner.R.layout.fragment_sensors, container, false);

        InitView(view);
        SetChartView();
        InitArrayList();


        return view;
    }
    /**
     * 初始化控件
     * @param view 母布局文件
     */
    private void InitView(View view) {

        //绑定卡片视图
        linearacc_card = view.findViewById(R.id.folding_cell_linearacc);
        acc_card = view.findViewById(R.id.folding_cell_acc);
        gyro_card = view.findViewById(R.id.folding_cell_gyroscope);
        step_card = view.findViewById(R.id.folding_cell_step);
        rotation_vector_card = view.findViewById(R.id.folding_cell_rotation);
        magnetic_vector_card = view.findViewById(R.id.folding_cell_magnetic_rotation);
        magnetic_card = view.findViewById(R.id.folding_cell_magnetic);
        proximity_card = view.findViewById(R.id.folding_cell_proximity);
        light_card = view.findViewById(R.id.folding_cell_light);
        temperature_card = view.findViewById(R.id.folding_cell_temp);
        pressure_card = view.findViewById(R.id.folding_cell_pressure);
        humidity_card = view.findViewById(R.id.folding_cell_humidity);

        //绑定文字视图
        linearacc_data = view.findViewById(R.id.linearaccelerationData);
        acc_data = view.findViewById(R.id.accelerationData);
        gyro_data = view.findViewById(R.id.gyroscopeData);
        step_data = view.findViewById(R.id.stepData);
        rotation_vector_data = view.findViewById(R.id.rotationData);
        magnetic_rotation_data = view.findViewById(R.id.magnetic_rotationData);
        magnetic_data = view.findViewById(R.id.magneticData);
        proximity_data = view.findViewById(R.id.proximityData);
        light_data = view.findViewById(R.id.lightSensorData);
        temperature_data = view.findViewById(R.id.tempSensorData);
        pressure_data = view.findViewById(R.id.pressureSensorData);
        humidity_data = view.findViewById(R.id.humiditySensorData);

        TextView linearacc_card_description = view.findViewById(R.id.carddescription_linearacc);
        TextView acc_card_description = view.findViewById(R.id.carddescription_acc);
        TextView gyro_card_description = view.findViewById(R.id.carddescription_gyro);
        TextView step_card_description = view.findViewById(R.id.carddescription_step);
        TextView rotation_vector_card_description = view.findViewById(R.id.carddescription_rotation);
        TextView magnetic_rotation_card_description = view.findViewById(R.id.carddescription_magnetic_rotation);
        TextView magnetic_card_description = view.findViewById(R.id.carddescription_magnetic);
        TextView proximity_card_description = view.findViewById(R.id.carddescription_proximity);
        TextView light_card_description = view.findViewById(R.id.carddescription_light);
        TextView temperature_card_description = view.findViewById(R.id.carddescription_temp);
        TextView pressure_card_description = view.findViewById(R.id.carddescription_pressure);
        TextView humidity_card_description = view.findViewById(R.id.carddescription_humidity);

        //绑定图表视图
        LinearAccelerationChart = view.findViewById(R.id.linearaccelerationChart);
        AccelerationChart = view.findViewById(R.id.accelerationChart);
        GyroscopeChart = view.findViewById(R.id.gyroscopeChart);
        StepChart = view.findViewById(R.id.stepChart);
        RotationChart = view.findViewById(R.id.rotationChart);
        MagneticRotationChart = view.findViewById(R.id.magnetic_rotationChart);
        MagneticChart = view.findViewById(R.id.magneticChart);
        ProximityChart = view.findViewById(R.id.proximityChart);
        LightChart = view.findViewById(R.id.lightChart);
        TemperatureChart = view.findViewById(R.id.tempChart);
        PressureChart = view.findViewById(R.id.pressureChart);
        HumidityChart = view.findViewById(R.id.humidityChart);

        //绑定SeekBar
        seekBar_linearacc = view.findViewById(R.id.seekbar_linearacc);
        seekBar_acc = view.findViewById(R.id.seekbar_acc);
        seekBar_gyro = view.findViewById(R.id.seekbar_gyro);
        seekBar_rot = view.findViewById(R.id.seekbar_rot);
        seekBar_mrot = view.findViewById(R.id.seekbar_mrot);
        seekBar_mag = view.findViewById(R.id.seekbar_mag);
        seekBar_step = view.findViewById(R.id.seekbar_step);
        seekBar_proximity = view.findViewById(R.id.seekbar_proximity);
        seekBar_light = view.findViewById(R.id.seekbar_light);
        seekBar_temp = view.findViewById(R.id.seekbar_temp);
        seekBar_pressure = view.findViewById(R.id.seekbar_pressure);
        seekBar_humidity = view.findViewById(R.id.seekbar_humidity);

        //初始化传感器管理器
        mSManager = (SensorManager) requireActivity().getSystemService(Context.SENSOR_SERVICE);
        listofSensor = mSManager.getSensorList(TYPE_ALL);
        for(Sensor s:listofSensor){
            switch (s.getType()){
                case Sensor.TYPE_ACCELEROMETER: SetTextDescription(acc_card_description, s);break;
                case Sensor.TYPE_LINEAR_ACCELERATION:SetTextDescription(linearacc_card_description, s);break;
                case Sensor.TYPE_GYROSCOPE:SetTextDescription(gyro_card_description, s);break;
                case Sensor.TYPE_ROTATION_VECTOR:SetTextDescription(rotation_vector_card_description, s);break;
                case Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR:SetTextDescription(magnetic_rotation_card_description, s);break;
                case Sensor.TYPE_MAGNETIC_FIELD:SetTextDescription(magnetic_card_description, s);break;
                case Sensor.TYPE_STEP_COUNTER:SetTextDescription(step_card_description, s);break;
                case Sensor.TYPE_LIGHT:SetTextDescription(light_card_description, s);break;
                case Sensor.TYPE_PRESSURE:SetTextDescription(pressure_card_description, s);break;
                case Sensor.TYPE_PROXIMITY:SetTextDescription(proximity_card_description, s);break;
                case Sensor.TYPE_RELATIVE_HUMIDITY:SetTextDescription(humidity_card_description, s);break;
                case Sensor.TYPE_AMBIENT_TEMPERATURE:SetTextDescription(temperature_card_description, s);break;
            }
        }
    }

    /**
     * 设置描述文字
     */
    @SuppressLint("SetTextI18n")
    private void SetTextDescription(TextView view, Sensor sensor) {

        view.setText(
                "Version: " + sensor.getVersion() +
                        "\nVendor: " + sensor.getVendor() +
                        "\nResolution: " + sensor.getResolution() +
                        "\nMaximumRange: " + sensor.getMaximumRange() +
                        "\nPower: " + sensor.getPower());
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SetSensorListener();
        SetCardView();
        SetSeekBar();

    }


    private final SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @SuppressLint("NonConstantResourceId")
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            switch (seekBar.getId()) {
                case R.id.seekbar_linearacc:
                    chartView.SetThreshold(LinearAccelerationChart, (float)progress, (float)-progress);
                    break;
                case R.id.seekbar_acc:
                    chartView.SetThreshold(AccelerationChart, (float)progress, (float)-progress);
                    break;
                case R.id.seekbar_gyro:
                    chartView.SetThreshold(GyroscopeChart, (float)progress, (float)-progress);
                    break;
                case R.id.seekbar_rot:
                    chartView.SetThreshold(RotationChart, (float)progress, (float)-progress);
                    break;
                case R.id.seekbar_mrot:
                    chartView.SetThreshold(MagneticRotationChart, (float)progress, (float)-progress);
                    break;
                case R.id.seekbar_mag:
                    chartView.SetThreshold(MagneticChart, (float)progress, (float)-progress);
                    break;
                case R.id.seekbar_step:
                    chartView.SetThreshold(StepChart, (float)progress);
                    break;
                case R.id.seekbar_proximity:
                    chartView.SetThreshold(ProximityChart, (float)progress);
                    break;
                case R.id.seekbar_light:
                    chartView.SetThreshold(LightChart, (float)progress);
                    break;
                case R.id.seekbar_temp:
                    chartView.SetThreshold(TemperatureChart, (float)progress);
                    break;
                case R.id.seekbar_pressure:
                    chartView.SetThreshold(PressureChart, (float)progress);
                    break;
                case R.id.seekbar_humidity:
                    chartView.SetThreshold(HumidityChart, (float)progress);
                    break;
                default:
                    break;
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
        }
    };

    private void SetSeekBar() {
        seekBar_linearacc.setOnSeekBarChangeListener(seekBarChangeListener);
        seekBar_acc.setOnSeekBarChangeListener(seekBarChangeListener);
        seekBar_gyro.setOnSeekBarChangeListener(seekBarChangeListener);
        seekBar_rot.setOnSeekBarChangeListener(seekBarChangeListener);
        seekBar_mrot.setOnSeekBarChangeListener(seekBarChangeListener);
        seekBar_mag.setOnSeekBarChangeListener(seekBarChangeListener);
        seekBar_step.setOnSeekBarChangeListener(seekBarChangeListener);
        seekBar_proximity.setOnSeekBarChangeListener(seekBarChangeListener);
        seekBar_light.setOnSeekBarChangeListener(seekBarChangeListener);
        seekBar_temp.setOnSeekBarChangeListener(seekBarChangeListener);
        seekBar_pressure.setOnSeekBarChangeListener(seekBarChangeListener);
        seekBar_humidity.setOnSeekBarChangeListener(seekBarChangeListener);
    }

    private void InitArrayList() {
        linearaccX = new ArrayList<>();
        linearaccY = new ArrayList<>();
        linearaccZ = new ArrayList<>();
        accX = new ArrayList<>();
        accY = new ArrayList<>();
        accZ = new ArrayList<>();
        gyroX = new ArrayList<>();
        gyroY = new ArrayList<>();
        gyroZ = new ArrayList<>();
        rotX = new ArrayList<>();
        rotY = new ArrayList<>();
        rotZ = new ArrayList<>();
        mrotX = new ArrayList<>();
        mrotY = new ArrayList<>();
        mrotZ = new ArrayList<>();
        magX = new ArrayList<>();
        magY = new ArrayList<>();
        magZ = new ArrayList<>();
        step = new ArrayList<>();
        proximity = new ArrayList<>();
        light = new ArrayList<>();
        temperature = new ArrayList<>();
        pressure = new ArrayList<>();
        humidity = new ArrayList<>();
    }

    /**
     * 绑定卡片视图
     */
    private void SetCardView() {

        final int sensorDelay = SensorManager.SENSOR_DELAY_NORMAL;

        linearacc_card.setOnClickListener(v -> handleSensorCardClick(linearacc_card, Sensor.TYPE_LINEAR_ACCELERATION, linearacc_sensorEventListener, sensorDelay));
        gyro_card.setOnClickListener(v -> handleSensorCardClick(gyro_card, Sensor.TYPE_GYROSCOPE, gyro_sensorEventListener, sensorDelay));
        acc_card.setOnClickListener(v -> handleSensorCardClick(acc_card, Sensor.TYPE_ACCELEROMETER, acc_sensorEventListener, sensorDelay));
        rotation_vector_card.setOnClickListener(v -> handleSensorCardClick(rotation_vector_card, Sensor.TYPE_ROTATION_VECTOR, rotation_vector_sensorEventListener, sensorDelay));
        magnetic_vector_card.setOnClickListener(v -> handleSensorCardClick(magnetic_vector_card, Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR, magnetic_vector_sensorEventListener, sensorDelay));
        magnetic_card.setOnClickListener(v -> handleSensorCardClick(magnetic_card, Sensor.TYPE_MAGNETIC_FIELD, magnetic_sensorEventListener, sensorDelay));
        light_card.setOnClickListener(v -> handleSensorCardClick(light_card, Sensor.TYPE_LIGHT, light_sensorEventListener, sensorDelay));
        step_card.setOnClickListener(v -> handleSensorCardClick(step_card, Sensor.TYPE_STEP_COUNTER, step_sensorEventListener, sensorDelay));
        proximity_card.setOnClickListener(v -> handleSensorCardClick(proximity_card, Sensor.TYPE_PROXIMITY, proximity_sensorEventListener, sensorDelay));
        pressure_card.setOnClickListener(v -> handleSensorCardClick(pressure_card, Sensor.TYPE_PRESSURE, pressure_sensorEventListener, sensorDelay));
        humidity_card.setOnClickListener(v -> handleSensorCardClick(humidity_card, Sensor.TYPE_RELATIVE_HUMIDITY, humidity_sensorEventListener, sensorDelay));
        temperature_card.setOnClickListener(v -> handleSensorCardClick(temperature_card, Sensor.TYPE_AMBIENT_TEMPERATURE, temperature_sensorEventListener, sensorDelay));
    }

    private void handleSensorCardClick(@NonNull FoldingCell card, int sensorType, SensorEventListener listener, int sensorDelay) {

        card.initialize(30, 500, Color.TRANSPARENT, 3);
        card.toggle(false);
        if (!card.isUnfolded()) {
            Sensor sensor = mSManager.getDefaultSensor(sensorType);
            if (sensor != null) {
                mSManager.registerListener(listener, sensor, sensorDelay);
            }
        } else {
            mSManager.unregisterListener(listener);
            clearSensorData(sensorType);
        }
    }

    private void clearSensorData(int sensorType) {
        switch (sensorType) {
            case Sensor.TYPE_LINEAR_ACCELERATION:
                linearaccX.clear();
                linearaccY.clear();
                linearaccZ.clear();
                break;
            case Sensor.TYPE_GYROSCOPE:
                gyroX.clear();
                gyroY.clear();
                gyroZ.clear();
                break;
            case Sensor.TYPE_ACCELEROMETER:
                accX.clear();
                accY.clear();
                accZ.clear();
                break;
            case Sensor.TYPE_ROTATION_VECTOR:
                rotX.clear();
                rotY.clear();
                rotZ.clear();
                break;
            case Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR:
                mrotX.clear();
                mrotY.clear();
                mrotZ.clear();
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                magX.clear();
                magY.clear();
                magZ.clear();
                break;
            case Sensor.TYPE_LIGHT:
                light.clear();
                break;
            case Sensor.TYPE_STEP_COUNTER:
                step.clear();
                break;
            case Sensor.TYPE_PROXIMITY:
                proximity.clear();
                break;
            case Sensor.TYPE_PRESSURE:
                pressure.clear();
                break;
            case Sensor.TYPE_RELATIVE_HUMIDITY:
                humidity.clear();
                break;
            case Sensor.TYPE_AMBIENT_TEMPERATURE:
                temperature.clear();
                break;
            default:
                break;
        }
    }

    private void SetChartView() {

        LegendEntry legendEntry_linearaccX = new LegendEntry("Linear AccX(m/s^2)", Legend.LegendForm.LINE, 12f, 2f, null, Color.RED);
        LegendEntry legendEntry_linearaccY = new LegendEntry("Linear AccY(m/s^2)", Legend.LegendForm.LINE, 12f, 2f, null, Color.BLUE);
        LegendEntry legendEntry_linearaccZ = new LegendEntry("Linear AccZ(m/s^2)", Legend.LegendForm.LINE, 12f, 2f, null, Color.GREEN);
        List<LegendEntry> entries_linearacc = new ArrayList<>();
        entries_linearacc.add(legendEntry_linearaccX);
        entries_linearacc.add(legendEntry_linearaccY);
        entries_linearacc.add(legendEntry_linearaccZ);
        chartView.InitChartView(LinearAccelerationChart, 5, 0, entries_linearacc);

        LegendEntry legendEntry_accX = new LegendEntry("AccX(m/s^2)", Legend.LegendForm.LINE, 12f, 2f, null, Color.RED);
        LegendEntry legendEntry_accY = new LegendEntry("AccY(m/s^2)", Legend.LegendForm.LINE, 12f, 2f, null, Color.BLUE);
        LegendEntry legendEntry_accZ = new LegendEntry("AccZ(m/s^2)", Legend.LegendForm.LINE, 12f, 2f, null, Color.GREEN);
        List<LegendEntry> entries_acc = new ArrayList<>();
        entries_acc.add(legendEntry_accX);
        entries_acc.add(legendEntry_accY);
        entries_acc.add(legendEntry_accZ);
        chartView.InitChartView(AccelerationChart, 5, 0,  entries_acc);

        LegendEntry legendEntry_gyroX = new LegendEntry("AngX(°/s)", Legend.LegendForm.LINE, 12f, 2f, null, Color.RED);
        LegendEntry legendEntry_gyroY = new LegendEntry("AngX(°/s)", Legend.LegendForm.LINE, 12f, 2f, null, Color.BLUE);
        LegendEntry legendEntry_gyroZ = new LegendEntry("AngX(°/s)", Legend.LegendForm.LINE, 12f, 2f, null, Color.GREEN);
        List<LegendEntry> entries_gyro = new ArrayList<>();
        entries_gyro.add(legendEntry_gyroX);
        entries_gyro.add(legendEntry_gyroY);
        entries_gyro.add(legendEntry_gyroZ);
        chartView.InitChartView(GyroscopeChart, 30, 0,  entries_gyro);

        LegendEntry legendEntry_step = new LegendEntry("Steps", Legend.LegendForm.LINE, 12f, 2f, null, Color.RED);
        List<LegendEntry> entries_step = new ArrayList<>();
        entries_step.add(legendEntry_step);
        chartView.InitChartView(StepChart, 5, 0,  entries_step);

        LegendEntry legendEntry_rotX = new LegendEntry("RotX", Legend.LegendForm.LINE, 12f, 2f, null, Color.RED);
        LegendEntry legendEntry_rotY = new LegendEntry("RotY", Legend.LegendForm.LINE, 12f, 2f, null, Color.BLUE);
        LegendEntry legendEntry_rotZ = new LegendEntry("RotZ", Legend.LegendForm.LINE, 12f, 2f, null, Color.GREEN);
        List<LegendEntry> entries_rot = new ArrayList<>();
        entries_rot.add(legendEntry_rotX);
        entries_rot.add(legendEntry_rotY);
        entries_rot.add(legendEntry_rotZ);
        chartView.InitChartView(RotationChart, 10, 0,  entries_rot);

        LegendEntry legendEntry_mrotX = new LegendEntry("RotX", Legend.LegendForm.LINE, 12f, 2f, null, Color.RED);
        LegendEntry legendEntry_mrotY = new LegendEntry("RotY", Legend.LegendForm.LINE, 12f, 2f, null, Color.BLUE);
        LegendEntry legendEntry_mrotZ = new LegendEntry("RotZ", Legend.LegendForm.LINE, 12f, 2f, null, Color.GREEN);
        List<LegendEntry> entries_mrot = new ArrayList<>();
        entries_mrot.add(legendEntry_mrotX);
        entries_mrot.add(legendEntry_mrotY);
        entries_mrot.add(legendEntry_mrotZ);
        chartView.InitChartView(MagneticRotationChart, 10, 0,  entries_mrot);

        LegendEntry legendEntry_mX = new LegendEntry("MagX(μT)", Legend.LegendForm.LINE, 12f, 2f, null, Color.RED);
        LegendEntry legendEntry_mY = new LegendEntry("MagY(μT)", Legend.LegendForm.LINE, 12f, 2f, null, Color.BLUE);
        LegendEntry legendEntry_mZ = new LegendEntry("MagZ(μT)", Legend.LegendForm.LINE, 12f, 2f, null, Color.GREEN);
        List<LegendEntry> entries_m = new ArrayList<>();
        entries_m.add(legendEntry_mX);
        entries_m.add(legendEntry_mY);
        entries_m.add(legendEntry_mZ);
        chartView.InitChartView(MagneticChart, 10, 0,  entries_m);

        LegendEntry legendEntry_proximity = new LegendEntry("Dis(cm)", Legend.LegendForm.LINE, 12f, 2f, null, Color.RED);
        List<LegendEntry> entries_proximity = new ArrayList<>();
        entries_proximity.add(legendEntry_proximity);
        chartView.InitChartView(ProximityChart, 5, 0,  entries_proximity);

        LegendEntry legendEntry_light = new LegendEntry("Illuminance(lx)", Legend.LegendForm.LINE, 12f, 2f, null, Color.RED);
        List<LegendEntry> entries_light = new ArrayList<>();
        entries_light.add(legendEntry_light);
        chartView.InitChartView(LightChart, 5, 0,  entries_light);

        LegendEntry legendEntry_temp = new LegendEntry("Temperature(°C)", Legend.LegendForm.LINE, 12f, 2f, null, Color.RED);
        List<LegendEntry> entries_temp = new ArrayList<>();
        entries_temp.add(legendEntry_temp);
        chartView.InitChartView(TemperatureChart, 5, 0,  entries_temp);

        LegendEntry legendEntry_pressure = new LegendEntry("Pressure(hpa)", Legend.LegendForm.LINE, 12f, 2f, null, Color.RED);
        List<LegendEntry> entries_pressure = new ArrayList<>();
        entries_pressure.add(legendEntry_pressure);
        chartView.InitChartView(PressureChart, 5, 0,  entries_pressure);

        LegendEntry legendEntry_humidity = new LegendEntry("Humidity(%)", Legend.LegendForm.LINE, 12f, 2f, null, Color.RED);
        List<LegendEntry> entries_humidity = new ArrayList<>();
        entries_humidity.add(legendEntry_humidity);
        chartView.InitChartView(HumidityChart, 5, 0,  entries_humidity);

    }


    private void SetSensorListener(){

        linearacc_sensorEventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                String strData = "X: " + sensorEvent.values[0] + "m/s^2"
                        + "\nY: " + sensorEvent.values[1] + "m/s^2"
                        + "\nZ: " + sensorEvent.values[2] + "m/s^2" + "\n";
                linearacc_data.setText(strData);

                //设置表中数据样式
                chartView.SetLineChartData(LinearAccelerationChart,
                        linearaccX, linearaccY, linearaccZ,
                        sensorEvent.values[0], sensorEvent.values[1], sensorEvent.values[2],
                        "Linear AccX", "Linear AccY", "Linear AccZ",
                        Color.RED, Color.BLUE, Color.GREEN,
                        true);
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };

        acc_sensorEventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                String strData = "X: " + sensorEvent.values[0] + "m/s^2"
                        + "\nY: " + sensorEvent.values[1] + "m/s^2"
                        + "\nZ: " + sensorEvent.values[2] + "m/s^2" + "\n";
                acc_data.setText(strData);

                //设置表中数据样式
                chartView.SetLineChartData(AccelerationChart,
                        accX, accY, accZ,
                        sensorEvent.values[0], sensorEvent.values[1], sensorEvent.values[2],
                        "AccX", "AccY", "AccZ",
                        Color.RED, Color.BLUE, Color.GREEN,
                        true);
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };

        gyro_sensorEventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                String strData = "X: " + sensorEvent.values[0] * 180 / Math.PI + "°"
                        + "\nY: " + sensorEvent.values[1] * 180 / Math.PI + "°"
                        + "\nZ: " + sensorEvent.values[2] * 180 / Math.PI + "°" + "\n";
                gyro_data.setText(strData);
                //设置表中数据样式
                chartView.SetLineChartData(GyroscopeChart,
                        gyroX, gyroY, gyroZ,
                        (float) (sensorEvent.values[0] * 180 / Math.PI), (float) (sensorEvent.values[1] * 180 / Math.PI), (float) (sensorEvent.values[2] * 180 / Math.PI),
                        "AngX", "AngY", "AngZ",
                        Color.RED, Color.BLUE, Color.GREEN,
                        true);
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };

        rotation_vector_sensorEventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                String strData = "X: " + sensorEvent.values[0]
                        + "\nY: " + sensorEvent.values[1]
                        + "\nZ: " + sensorEvent.values[2]  + "\n";
                rotation_vector_data.setText(strData);
                //设置表中数据样式
                //设置表中数据样式
                chartView.SetLineChartData(RotationChart,
                        rotX, rotY, rotZ,
                        sensorEvent.values[0], sensorEvent.values[1], sensorEvent.values[2],
                        "RotX", "RotY", "RotZ",
                        Color.RED, Color.BLUE, Color.GREEN,
                        true);
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };

        magnetic_vector_sensorEventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                String strData = "X: " + sensorEvent.values[0]
                        + "\nY: " + sensorEvent.values[1]
                        + "\nZ: " + sensorEvent.values[2]  + "\n";
                magnetic_rotation_data.setText(strData);
                //设置表中数据样式
                chartView.SetLineChartData(MagneticRotationChart,
                        mrotX, mrotY, mrotZ,
                        sensorEvent.values[0], sensorEvent.values[1], sensorEvent.values[2],
                        "MRotX", "MRotY", "MRotZ",
                        Color.RED, Color.BLUE, Color.GREEN,
                        true);
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };

        magnetic_sensorEventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                String strData = "X: " + sensorEvent.values[0] + "μT"
                        + "\nY: " + sensorEvent.values[1] + "μT"
                        + "\nZ: " + sensorEvent.values[2] + "μT"  + "\n";
                magnetic_data.setText(strData);
                //设置表中数据样式
                chartView.SetLineChartData(MagneticChart,
                        magX, magY, magZ,
                        sensorEvent.values[0], sensorEvent.values[1], sensorEvent.values[2],
                        "MagX", "MagY", "MagZ",
                        Color.RED, Color.BLUE, Color.GREEN,
                        true);
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };

        step_sensorEventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                String strData = "Steps: " + sensorEvent.values[0] + "\n";
                step_data.setText(strData);
                //设置表中数据样式
                chartView.SetLineChartData(StepChart,
                        step,
                        sensorEvent.values[0],
                        "Step",
                        Color.RED,
                        true);
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };

        proximity_sensorEventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                String strData = "Distance: " + sensorEvent.values[0] + "cm" +  "\n";
                proximity_data.setText(strData);
                //设置表中数据样式
                chartView.SetLineChartData(ProximityChart,
                        proximity,
                        sensorEvent.values[0],
                        "Proximity",
                        Color.RED,
                        true);
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };

        light_sensorEventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                String strData = "Illuminance: " + sensorEvent.values[0] + "lx" + "\n";
                light_data.setText(strData);
                //设置表中数据样式
                chartView.SetLineChartData(LightChart,
                        light,
                        sensorEvent.values[0],
                        "Illuminance",
                        Color.RED,
                        true);
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };

        temperature_sensorEventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                String strData = "Temperature: " + sensorEvent.values[0] + "°C" +  "\n";
                temperature_data.setText(strData);
                //设置表中数据样式
                chartView.SetLineChartData(TemperatureChart,
                        temperature,
                        sensorEvent.values[0],
                        "Temperature",
                        Color.RED,
                        true);
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };

        pressure_sensorEventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                String strData = "Pressure: " + sensorEvent.values[0] + "hPa" +  "\n";
                pressure_data.setText(strData);
                //设置表中数据样式
                chartView.SetLineChartData(PressureChart,
                        pressure,
                        sensorEvent.values[0],
                        "Pressure",
                        Color.RED,
                        true);
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };

        humidity_sensorEventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                String strData = "Humidity: " + sensorEvent.values[0] + "%" +  "\n";
                humidity_data.setText(strData);
                //设置表中数据样式
                chartView.SetLineChartData(HumidityChart,
                        humidity,
                        sensorEvent.values[0],
                        "Humidity",
                        Color.RED,
                        true);
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };


    }

}

