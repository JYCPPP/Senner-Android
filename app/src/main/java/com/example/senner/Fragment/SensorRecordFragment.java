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
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.senner.Helper.SharedPreferenceHelper;
import com.example.senner.R;
import com.example.senner.UI.ChartView;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LegendEntry;
import com.github.mikephil.charting.data.Entry;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SensorRecordFragment extends Fragment {

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

    private final HashMap<String, SensorEventListener> SensorListeners = new HashMap<>();

    private final SharedPreferenceHelper sharedPreferenceHelper = new SharedPreferenceHelper();
    //创建线程池
    private final ExecutorService threadPool = Executors.newFixedThreadPool(12);

    private final int MAX_POINT_SIZE = 1000;
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sensor_record, container, false);

        InitView(view);
        SetChartView();
        InitArrayList();

        return view;
    }

    /**
     * 方便以后查询使用
     */
    private void InitSensorContainer() {

        SensorListeners.put("Use Linear Accelerometer", linearacc_sensorEventListener);
        SensorListeners.put("Use Accelerometer", acc_sensorEventListener);
        SensorListeners.put("Use Gyroscope", gyro_sensorEventListener);
        SensorListeners.put("Use Rotation Vector Sensor", rotation_vector_sensorEventListener);
        SensorListeners.put("Use Geomagnetic Rotation Vector Sensor", magnetic_vector_sensorEventListener);
        SensorListeners.put("Use Magnetic Field Sensor", magnetic_sensorEventListener);
        SensorListeners.put("Use Step Counter", step_sensorEventListener);
        SensorListeners.put("Use Proximity Sensor", proximity_sensorEventListener);
        SensorListeners.put("Use Light Sensor", light_sensorEventListener);
        SensorListeners.put("Use Pressure Sensor", pressure_sensorEventListener);
        SensorListeners.put("Use Ambient Temperature Sensor", temperature_sensorEventListener);
        SensorListeners.put("Use Relative Humidity Sensor", humidity_sensorEventListener);


    }

    /**
     * 初始化控件
     * @param view 母布局文件
     */
    private void InitView(View view) {

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

        //先初始化监听器
        SetSensorListener();
        //再设置哈希表，这样保证哈希表是非空的
        InitSensorContainer();
        //注册传感器监听器
        SetUsedSensor();

    }

    /**
     * 根据HashMap注册传感器
     */
    private void SetUsedSensor() {

        final int sensorDelay = SensorManager.SENSOR_DELAY_NORMAL;
        for(Map.Entry<String, SensorEventListener> entry : SensorListeners.entrySet()){
            boolean UseSensor = sharedPreferenceHelper.getBoolean(requireActivity(), entry.getKey(), true);
            if(UseSensor){
                switch (entry.getKey()){
                    case "Use Linear Accelerometer":
                        mSManager.registerListener(entry.getValue(), mSManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION) ,sensorDelay);
                        break;
                    case "Use Accelerometer":
                        mSManager.registerListener(entry.getValue(), mSManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) ,sensorDelay);
                        break;
                    case "Use Gyroscope":
                        mSManager.registerListener(entry.getValue(), mSManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE) ,sensorDelay);
                        break;
                    case "Use Rotation Vector Sensor":
                        mSManager.registerListener(entry.getValue(), mSManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR) ,sensorDelay);
                        break;
                    case "Use Geomagnetic Rotation Vector Sensor":
                        mSManager.registerListener(entry.getValue(), mSManager.getDefaultSensor(Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR) ,sensorDelay);
                        break;
                    case "Use Magnetic Field Sensor":
                        mSManager.registerListener(entry.getValue(), mSManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) ,sensorDelay);
                        break;
                    case "Use Step Counter":
                        mSManager.registerListener(entry.getValue(), mSManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER) ,sensorDelay);
                        break;
                    case "Use Proximity Sensor":
                        mSManager.registerListener(entry.getValue(), mSManager.getDefaultSensor(Sensor.TYPE_PROXIMITY) ,sensorDelay);
                        break;
                    case "Use Light Sensor":
                        mSManager.registerListener(entry.getValue(), mSManager.getDefaultSensor(Sensor.TYPE_LIGHT) ,sensorDelay);
                        break;
                    case "Use Pressure Sensor":
                        mSManager.registerListener(entry.getValue(), mSManager.getDefaultSensor(Sensor.TYPE_PRESSURE) ,sensorDelay);
                        break;
                    case "Use Ambient Temperature Sensor":
                        mSManager.registerListener(entry.getValue(), mSManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE) ,sensorDelay);
                        break;
                    case "Use Relative Humidity Sensor":
                        mSManager.registerListener(entry.getValue(), mSManager.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY) ,sensorDelay);
                        break;
                }

            }
        }
    }


    /**
     * 分配数组的初始容量
     */
    private void InitArrayList() {
        //分配初始容量来保证性能
        linearaccX = new ArrayList<>(10000);
        linearaccY = new ArrayList<>(10000);
        linearaccZ = new ArrayList<>(10000);
        accX = new ArrayList<>(10000);
        accY = new ArrayList<>(10000);
        accZ = new ArrayList<>(10000);
        gyroX = new ArrayList<>(10000);
        gyroY = new ArrayList<>(10000);
        gyroZ = new ArrayList<>(10000);
        rotX = new ArrayList<>(10000);
        rotY = new ArrayList<>(10000);
        rotZ = new ArrayList<>(10000);
        mrotX = new ArrayList<>(10000);
        mrotY = new ArrayList<>(10000);
        mrotZ = new ArrayList<>(10000);
        magX = new ArrayList<>(10000);
        magY = new ArrayList<>(10000);
        magZ = new ArrayList<>(10000);
        step = new ArrayList<>(10000);
        proximity = new ArrayList<>(10000);
        light = new ArrayList<>(10000);
        temperature = new ArrayList<>(10000);
        pressure = new ArrayList<>(10000);
        humidity = new ArrayList<>(10000);
    }


    private void SetChartView() {

        LegendEntry legendEntry_linearaccX = new LegendEntry("AccX(m/s^2)", Legend.LegendForm.LINE, 12f, 2f, null, Color.RED);
        LegendEntry legendEntry_linearaccY = new LegendEntry("AccY(m/s^2)", Legend.LegendForm.LINE, 12f, 2f, null, Color.BLUE);
        LegendEntry legendEntry_linearaccZ = new LegendEntry("AccZ(m/s^2)", Legend.LegendForm.LINE, 12f, 2f, null, Color.GREEN);
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

        LegendEntry legendEntry_pressure = new LegendEntry("Pressure(hPa)", Legend.LegendForm.LINE, 12f, 2f, null, Color.RED);
        List<LegendEntry> entries_pressure = new ArrayList<>();
        entries_pressure.add(legendEntry_pressure);
        chartView.InitChartView(PressureChart, 5, 0,  entries_pressure);

        LegendEntry legendEntry_humidity = new LegendEntry("Humidity(%)", Legend.LegendForm.LINE, 12f, 2f, null, Color.RED);
        List<LegendEntry> entries_humidity = new ArrayList<>();
        entries_humidity.add(legendEntry_humidity);
        chartView.InitChartView(HumidityChart, 5, 0,  entries_humidity);

    }

    private File CreateDataFile(String filePath, String fileName) {

        File path = new File(filePath);
        if(!path.exists()){
            path.mkdirs();
        }
        //打开文件，如果不存在则创建
        File file = new File(filePath + fileName);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return file;
    }



    private void SetSensorListener() {

        String ProjectPath = sharedPreferenceHelper.getString(requireActivity(),"Project Path", "");
        //记录项目文件位置
        File LinearAccData = CreateDataFile(ProjectPath + "/Sensors Data", "/LinearAccData.txt");
        File AccData = CreateDataFile(ProjectPath + "/Sensors Data", "/AccData.txt");
        File GyroData = CreateDataFile(ProjectPath + "/Sensors Data", "/GyroData.txt");
        File RotData = CreateDataFile(ProjectPath + "/Sensors Data", "/RotationVectorData.txt");
        File MRotData = CreateDataFile(ProjectPath + "/Sensors Data", "/GeomagneticRotationVectorData.txt");
        File MagData = CreateDataFile(ProjectPath + "/Sensors Data", "/MagneticFieldData.txt");
        File ProximityData = CreateDataFile(ProjectPath + "/Sensors Data", "/ProximityData.txt");
        File LightData = CreateDataFile(ProjectPath + "/Sensors Data", "/LightData.txt");
        File TempData = CreateDataFile(ProjectPath + "/Sensors Data", "/AmbientTemperatureData.txt");
        File PressureData = CreateDataFile(ProjectPath + "/Sensors Data", "/PressureData.txt");
        File StepData = CreateDataFile(ProjectPath + "/Sensors Data", "/StepData.txt");
        File HumidityData = CreateDataFile(ProjectPath + "/Sensors Data", "/HumidityData.txt");

        linearacc_sensorEventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                String strData = "X: " + sensorEvent.values[0] + "m/s^2"
                        + "\nY: " + sensorEvent.values[1] + "m/s^2"
                        + "\nZ: " + sensorEvent.values[2] + "m/s^2" + "\n";
                linearacc_data.setText(strData);

                //子线程进行文件读写
                threadPool.submit(new Thread(() -> {
                    //如果存在则打开继续写入
                    //打开文件输出流

                    try {
                        FileOutputStream LinearAccDataOutputStream = new FileOutputStream(LinearAccData, true);
                         //第二个参数表示追加写入
                        //写入数据
                        String data = "X: " + sensorEvent.values[0] + "m/s^2"
                                + ", Y: " + sensorEvent.values[1] + "m/s^2"
                                + ", Z: " + sensorEvent.values[2] + "m/s^2" + "\n";
                        LinearAccDataOutputStream.write(data.getBytes());
                        LinearAccDataOutputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }));

                //设置表中数据样式
                //设置自动清零
                if(linearaccX.size() > MAX_POINT_SIZE){
                    linearaccX.clear();
                }
                if(linearaccY.size() > MAX_POINT_SIZE){
                    linearaccY.clear();
                }
                if(linearaccZ.size() > MAX_POINT_SIZE){
                    linearaccZ.clear();
                }
                chartView.SetLineChartData(LinearAccelerationChart,
                        linearaccX, linearaccY, linearaccZ,
                        sensorEvent.values[0], sensorEvent.values[1], sensorEvent.values[2],
                        "Linear AccX", "Linear AccY", "Linear AccZ",
                        Color.RED, Color.BLUE, Color.GREEN,
                        false);
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

                //子线程进行文件读写
                threadPool.submit(new Thread(() -> {
                    //如果存在则打开继续写入
                    try {
                        //打开文件输出流
                        FileOutputStream AccDataOutputStream = new FileOutputStream(AccData, true);
                        //第二个参数表示追加写入
                        //写入数据
                        String data = "X: " + sensorEvent.values[0] + "m/s^2"
                                + ", Y: " + sensorEvent.values[1] + "m/s^2"
                                + ", Z: " + sensorEvent.values[2] + "m/s^2" + "\n";
                        AccDataOutputStream.write(data.getBytes());
                        // 将文件写入 outputStream
                        AccDataOutputStream.close();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }));

                //设置表中数据样式
                //设置自动清零
                if(accX.size() > MAX_POINT_SIZE){
                    accX.clear();
                }
                if(accY.size() > MAX_POINT_SIZE){
                    accY.clear();
                }
                if(accZ.size() > MAX_POINT_SIZE){
                    accZ.clear();
                }
                chartView.SetLineChartData(AccelerationChart,
                        accX, accY, accZ,
                        sensorEvent.values[0], sensorEvent.values[1], sensorEvent.values[2],
                        "AccX", "AccY", "AccZ",
                        Color.RED, Color.BLUE, Color.GREEN,
                        false);
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };

        gyro_sensorEventListener = new SensorEventListener() {

            final float PI_FLOAT = (float) Math.PI;
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                String strData = "X: " + sensorEvent.values[0] * 180 / PI_FLOAT + "°"
                        + "\nY: " + sensorEvent.values[1] * 180 / PI_FLOAT + "°"
                        + "\nZ: " + sensorEvent.values[2] * 180 / PI_FLOAT + "°" + "\n";
                gyro_data.setText(strData);

                //子线程进行文件读写
                threadPool.submit(new Thread(() -> {
                    //如果存在则打开继续写入
                    //打开文件输出流
                    try {
                        FileOutputStream GyroDataOutputStream = new FileOutputStream(GyroData, true);
                        //第二个参数表示追加写入
                        //写入数据
                        String data = "X: " + sensorEvent.values[0] * 180 / PI_FLOAT + "°"
                                + ", Y: " + sensorEvent.values[1] * 180 / PI_FLOAT + "°"
                                + ", Z: " + sensorEvent.values[2] * 180 / PI_FLOAT + "°" + "\n";
                        GyroDataOutputStream.write(data.getBytes());
                        GyroDataOutputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }));

                //设置表中数据样式
                //设置自动清零
                if(gyroX.size() > MAX_POINT_SIZE){
                    gyroX.clear();
                }
                if(gyroY.size() > MAX_POINT_SIZE){
                    gyroY.clear();
                }
                if(gyroZ.size() > MAX_POINT_SIZE){
                    gyroZ.clear();
                }
                chartView.SetLineChartData(GyroscopeChart,
                        gyroX, gyroY, gyroZ,
                        sensorEvent.values[0] * 180 / PI_FLOAT, sensorEvent.values[1] * 180 / PI_FLOAT, sensorEvent.values[2] * 180 / PI_FLOAT,
                        "AngX", "AngY", "AngZ",
                        Color.RED, Color.BLUE, Color.GREEN,
                        false);
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
                //子线程进行文件读写
                threadPool.submit(new Thread(() -> {
                    //如果存在则打开继续写入
                    //打开文件输出流
                    try {
                        FileOutputStream RotDataOutputStream = new FileOutputStream(RotData, true);
                        //第二个参数表示追加写入
                        //写入数据
                        String data = "X: " + sensorEvent.values[0]
                                + ", Y: " + sensorEvent.values[1]
                                + ", Z: " + sensorEvent.values[2]  + "\n";
                        RotDataOutputStream.write(data.getBytes());
                        RotDataOutputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }));

                //设置表中数据样式
                //设置自动清零
                if(rotX.size() > MAX_POINT_SIZE){
                    rotX.clear();
                }
                if(rotY.size() > MAX_POINT_SIZE){
                    rotY.clear();
                }
                if(rotZ.size() > MAX_POINT_SIZE){
                    rotZ.clear();
                }
                chartView.SetLineChartData(RotationChart,
                        rotX, rotY, rotZ,
                        sensorEvent.values[0], sensorEvent.values[1], sensorEvent.values[2],
                        "RotX", "RotY", "RotZ",
                        Color.RED, Color.BLUE, Color.GREEN,
                        false);
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

                //子线程进行文件读写
                threadPool.submit(new Thread(() -> {
                    //如果存在则打开继续写入
                    //打开文件输出流
                    try {
                        FileOutputStream MRotDataOutputStream = new FileOutputStream(MRotData, true);
                        //第二个参数表示追加写入
                        //写入数据
                        String data = "X: " + sensorEvent.values[0]
                                + ", Y: " + sensorEvent.values[1]
                                + ", Z: " + sensorEvent.values[2]  + "\n";
                        MRotDataOutputStream.write(data.getBytes());
                        MRotDataOutputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }));

                //设置表中数据样式
                //设置自动清零
                if(mrotX.size() > MAX_POINT_SIZE){
                    mrotX.clear();
                }
                if(mrotY.size() > MAX_POINT_SIZE){
                    mrotY.clear();
                }
                if(mrotZ.size() > MAX_POINT_SIZE){
                    mrotZ.clear();
                }
                chartView.SetLineChartData(MagneticRotationChart,
                        mrotX, mrotY, mrotZ,
                        sensorEvent.values[0], sensorEvent.values[1], sensorEvent.values[2],
                        "MRotX", "MRotY", "MRotZ",
                        Color.RED, Color.BLUE, Color.GREEN,
                        false);
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

                //子线程进行文件读写
                threadPool.submit(new Thread(() -> {
                    //如果存在则打开继续写入
                    //打开文件输出流
                    try {
                        FileOutputStream MagDataOutputStream = new FileOutputStream(MagData, true);
                        //第二个参数表示追加写入
                        //写入数据
                        String data = "X: " + sensorEvent.values[0] + "μT"
                                + ", Y: " + sensorEvent.values[1] + "μT"
                                + ", Z: " + sensorEvent.values[2] + "μT"  + "\n";
                        MagDataOutputStream.write(data.getBytes());
                        MagDataOutputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }));

                //设置表中数据样式
                //设置自动清零
                if(magX.size() > MAX_POINT_SIZE){
                    magX.clear();
                }
                if(magY.size() > MAX_POINT_SIZE){
                    magY.clear();
                }
                if(magZ.size() > MAX_POINT_SIZE){
                    magZ.clear();
                }
                chartView.SetLineChartData(MagneticChart,
                        magX, magY, magZ,
                        sensorEvent.values[0], sensorEvent.values[1], sensorEvent.values[2],
                        "MagX", "MagY", "MagZ",
                        Color.RED, Color.BLUE, Color.GREEN,
                        false);
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

                //子线程进行文件读写
                threadPool.submit(new Thread(() -> {
                    //如果存在则打开继续写入
                    //打开文件输出流
                    try {
                        FileOutputStream StepDataOutputStream = new FileOutputStream(StepData, true);
                        //第二个参数表示追加写入
                        //写入数据
                        String data = "Steps: " + sensorEvent.values[0] + "\n";
                        StepDataOutputStream.write(data.getBytes());
                        StepDataOutputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }));

                //设置表中数据样式
                if(step.size() > MAX_POINT_SIZE){
                    step.clear();
                }
                chartView.SetLineChartData(StepChart,
                        step,
                        sensorEvent.values[0],
                        "Step",
                        Color.RED,
                        false);
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

                //子线程进行文件读写
                threadPool.submit(new Thread(() -> {
                    //如果存在则打开继续写入
                    //打开文件输出流
                    try {
                        FileOutputStream ProximityDataOutputStream = new FileOutputStream(ProximityData, true);
                        //第二个参数表示追加写入
                        //写入数据
                        String data = "Distance: " + sensorEvent.values[0] + "cm" +  "\n";
                        ProximityDataOutputStream.write(data.getBytes());
                        ProximityDataOutputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }));

                //设置表中数据样式
                if(proximity.size() > MAX_POINT_SIZE){
                    proximity.clear();
                }
                chartView.SetLineChartData(ProximityChart,
                        proximity,
                        sensorEvent.values[0],
                        "Proximity",
                        Color.RED,
                        false);
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

                //子线程进行文件读写
                threadPool.submit(new Thread(() -> {
                    //如果存在则打开继续写入
                    //打开文件输出流
                    try {
                        FileOutputStream LightDataOutputStream = new FileOutputStream(LightData, true);
                        //第二个参数表示追加写入
                        //写入数据
                        String data = "Illuminance: " + sensorEvent.values[0] + "lx" + "\n";
                        LightDataOutputStream.write(data.getBytes());
                        LightDataOutputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }));

                //设置表中数据样式
                if(light.size() > MAX_POINT_SIZE){
                    light.clear();
                }
                chartView.SetLineChartData(LightChart,
                        light,
                        sensorEvent.values[0],
                        "Illuminance",
                        Color.RED,
                        false);
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

                //子线程进行文件读写
                threadPool.submit(new Thread(() -> {
                    //如果存在则打开继续写入
                    //打开文件输出流
                    try {
                        FileOutputStream TempDataOutputStream = new FileOutputStream(TempData, true);
                        //第二个参数表示追加写入
                        //写入数据
                        String data = "Temperature: " + sensorEvent.values[0] + "°C" +  "\n";
                        TempDataOutputStream.write(data.getBytes());
                        TempDataOutputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }));

                //设置表中数据样式
                if(temperature.size() > MAX_POINT_SIZE){
                    temperature.clear();
                }
                chartView.SetLineChartData(TemperatureChart,
                        temperature,
                        sensorEvent.values[0],
                        "Temperature",
                        Color.RED,
                        false);
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

                //子线程进行文件读写
                threadPool.submit(new Thread(() -> {
                    //如果存在则打开继续写入
                    //打开文件输出流
                    try {
                        FileOutputStream PressureDataOutputStream = new FileOutputStream(PressureData, true);
                        //第二个参数表示追加写入
                        //写入数据
                        String data = "Pressure: " + sensorEvent.values[0] + "hPa" +  "\n";
                        PressureDataOutputStream.write(data.getBytes());
                        PressureDataOutputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }));

                //设置表中数据样式
                if(pressure.size() > MAX_POINT_SIZE){
                    pressure.clear();
                }
                chartView.SetLineChartData(PressureChart,
                        pressure,
                        sensorEvent.values[0],
                        "Pressure",
                        Color.RED,
                        false);
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

                //子线程进行文件读写
                threadPool.submit(new Thread(() -> {
                    //如果存在则打开继续写入
                    //打开文件输出流
                    try {
                        FileOutputStream HumidityDataOutputStream = new FileOutputStream(HumidityData, true);
                        //第二个参数表示追加写入
                        //写入数据
                        String data = "Humidity: " + sensorEvent.values[0] + "%" +  "\n";
                        HumidityDataOutputStream.write(data.getBytes());
                        HumidityDataOutputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }));

                //设置表中数据样式
                if(humidity.size() > MAX_POINT_SIZE){
                    humidity.clear();
                }
                chartView.SetLineChartData(HumidityChart,
                        humidity,
                        sensorEvent.values[0],
                        "Humidity",
                        Color.RED,
                        false);
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };


    }

    /**
     * 停止传感器记录时调用
     */
    private void StopEvent(){

        //注销监听器
        for (Map.Entry<String, SensorEventListener> entry : SensorListeners.entrySet()) {
            boolean useSensor = sharedPreferenceHelper.getBoolean(requireActivity(), entry.getKey(), true);
            if (useSensor) {
                mSManager.unregisterListener(entry.getValue());
                sharedPreferenceHelper.putBoolean(requireActivity(), entry.getKey(), false);
            }
        }

        int[] SensorTypes = new int[]{
                Sensor.TYPE_LINEAR_ACCELERATION,
                Sensor.TYPE_LINEAR_ACCELERATION,
                Sensor.TYPE_ACCELEROMETER, Sensor.TYPE_GYROSCOPE,
                Sensor.TYPE_ROTATION_VECTOR, Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR, Sensor.TYPE_MAGNETIC_FIELD,
                Sensor.TYPE_STEP_COUNTER, Sensor.TYPE_PROXIMITY, Sensor.TYPE_LIGHT,
                Sensor.TYPE_PRESSURE, Sensor.TYPE_AMBIENT_TEMPERATURE, Sensor.TYPE_RELATIVE_HUMIDITY
        };
        //清除传感器数据
        for(int sensorType : SensorTypes){
            clearSensorData(sensorType);
        }


        //再销毁线程池
        if(!threadPool.isShutdown()){
            threadPool.shutdown();
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

}
