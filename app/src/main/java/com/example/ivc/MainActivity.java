package com.example.ivc;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener,
CompoundButton.OnCheckedChangeListener{

    private static final int REQUEST_ENABLE_BT = 10;//переменная для запроса находящихся Bluetooth устройств
    public static final int BT_BOUNDED         = 21;//переменная для анимации устройства, если оно найдено
    public static final int BT_SEARCH          = 22;//переменная для устройства, если оно найдено
    public static final int REQUEST_CODE_LOC   = 1;//переменная для геолокации
    public static final int VOICE_RECOGNITION_REQUEST_CODE = 40;//переменная для выбора голосового ассистента

    private static final int RELLE      =30;//переменая при выборе переключаетеля для реле
    private static final int PIR        =31;//переменая при выборе переключаетеля для автоматического режима

    private FrameLayout frameMessage;//объектная переменная для макета с сообщением
    private LinearLayout LinerList;//объектная переменная для макета с списком устройств
    private RelativeLayout linearLayout1;//объектная переменная для макета с дистанционным управлением
    private Button bt_Bluetooth;//объектная переменная для кнопки включения или выключения bluetooth
    private Button bt_search;//объектная переменная для кнопки поиска устройтв
    private Button bt_voise;//объектная переменная для кнопки голосового управдения
    private Button bt_Timer1min;//объектная переменная для кнопки таймера 1 минута
    private Button bt_Timer5min;//объектная переменная для кнопки таймера 5 минут
    private Button bt_Timer10min;//объектная переменная для кнопки таймера 10 минут
    private ProgressBar PrBar;//объектная переменная для анимации поиска
    private Switch switchRele;//объектная переменная для переключаетеля включения света
    private Switch switchPir;//объектная переменная для переключателя автоматического режима

    public  ListView BtListView;//объектная переменная для списка устройтсв Bluetooth

    public  BluetoothAdapter bluetoothAdapter;//объектная переменная для работы Bluetooth
    private List_bt list_bt;//объектная переменная для анимации списка устройств bluetooth
    public  ArrayList<BluetoothDevice> bluetoothDevices;//пересенная свзанного сиписка устройств bluetooth
    private boolean checked = false;//логическая переменная для переключения кнопки вкл/выкл bluetooth
    private BluetoothDevice device;//объектная переменная для bluetooth устройств


    public  ConnectThread connectThread = new ConnectThread(device);//инициализация переменной для внутреннего класса ConnectThread
    public ConnectedThread connectedThread;//объектная пременная для внутреннего класса ConnectedThread



    @Override
    protected void onCreate(Bundle savedInstanceState) {// необходимый метод при создании страницы или приложения
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        frameMessage = findViewById(R.id.Frame_layout_1);
        LinerList = findViewById(R.id.Liner_control);
        switchRele = findViewById(R.id.switch_rele);
        switchPir = findViewById(R.id.switchOFFpir_button);


        bt_Bluetooth = findViewById(R.id.button_bluetooth);
        bt_search = findViewById(R.id.search_button);
        PrBar = findViewById(R.id.proggress_search);
        BtListView = findViewById(R.id.list_item);
        linearLayout1 = findViewById(R.id.Liner_layout1);
        bt_voise = findViewById(R.id.voise_button);
        bt_Timer1min = findViewById(R.id.timer1_button);
        bt_Timer5min = findViewById(R.id.timer5_button);
        bt_Timer10min = findViewById(R.id.timer10_button);

        BtListView.setOnItemClickListener(this);

        bluetoothDevices =new ArrayList<>();

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        registerReceiver(receiver,filter);

        switchRele.setOnCheckedChangeListener(this);

        switchPir.setOnCheckedChangeListener(this);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();




        if(bluetoothAdapter==null){//проверка, если на устройсве bluetooth
            Toast toast = Toast.makeText(this, R.string.Bluetooth_not_have,
                    Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER,0,0);
            toast.show();
            finish();
        }

        if(bluetoothAdapter.isEnabled()){
            LISTdevicesShow();
            checked = true;
            setList(BT_BOUNDED);
        }

        BT_button();
        Search_BT();
        BT_voice();
        BT_timer1min();
        BT_timer5min();
        BT_timer10min();

    }

    @Override
    protected void onDestroy() {//метода при закрытии приложения или окна
        super.onDestroy();

        unregisterReceiver(receiver);

        if(connectThread!=null){
            connectThread.cancel();
        }
        if(connectedThread!=null){
            connectedThread.cancel();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {//метод необходимый при нажатии элемента списка устройств bluetooth
        if(parent.equals(BtListView)){
            device = bluetoothDevices.get(position);
            if(device != null){
                connectThread = new ConnectThread(device);
                connectThread.start();
                Toast.makeText(MainActivity.this,"Подключение к системе",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    private void Search_BT(){//метод при для нажатии кнопки поиска устройств
        bt_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IsSearch();
            }
        });
    }


    private void BT_button(){//метод при для нажатии кнопки вкл/выкл bluetoth
        bt_Bluetooth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checked) {
                    cutoutBT(checked);
                    LISTdevicesShow();
                }else {
                    OnFrameLayetMessage();
                    bluetoothAdapter.disable();
                }
                checked = !checked;
            }
        });
    }

    private void BT_timer1min(){//метод для нажатия кнопки таймера на 1 минуту
        bt_Timer1min.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connectedThread.write("timer1min#");
            }
        });
    }

    private void BT_timer5min(){//метод для нажатия кнопки таймера на 5 минут
        bt_Timer5min.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connectedThread.write("timer5min#");
            }
        });
    }

    private void BT_timer10min(){//метод для нажатия кнопки таймера на 5 минут
        bt_Timer10min.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connectedThread.write("timer10min#");
            }
        });
    }

    private void BT_voice(){//метод для нажатия кнопки голосового управления
        bt_voise.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSpeak();
            }
        });
    }

    public void startSpeak() {//метод для вызова голосового ассистента
        Intent intent =  new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH); // намерение для вызова формы обработки речи (ОР)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM); // сюда он слушает и запоминает
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Говорите команду");
        startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE); // вызываем активность ОР

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {//метод для возврата активити результата
        if (requestCode == REQUEST_ENABLE_BT) {
            if (requestCode == RESULT_OK && bluetoothAdapter.isEnabled()) {
                LISTdevicesShow();
                setList(BT_BOUNDED);
            } else if (requestCode == RESULT_CANCELED) {
                cutoutBT(true);
            }
        }
        else if(resultCode == RESULT_OK && requestCode == VOICE_RECOGNITION_REQUEST_CODE){
                ArrayList comandList = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                if(comandList.contains("light on")){
                    connectedThread.write("on#");
                }
                else if(comandList.contains("light off")){
                connectedThread.write("off#");
                }
                else if(comandList.contains("timer 1")){
                    connectedThread.write("timer1min#");
                }
                else if(comandList.contains("timer 5")){
                    connectedThread.write("timer5min#");
                }
                else if(comandList.contains("timer 10")){
                    connectedThread.write("timer10min#");
                }
                else if(comandList.contains("automatic")){
                    connectedThread.write("onPir#");
                }
                else if(comandList.contains("no automatic")){
                    connectedThread.write("offPir#");
                }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void OnFrameLayetMessage(){//метод для показывания на экране сообщение "Bluetooth выключен"
        linearLayout1.setVisibility(View.GONE);
        frameMessage.setVisibility(View.VISIBLE);
        LinerList.setVisibility(View.GONE);
    }

    private void LISTdevicesShow(){//метод для показывания на экране список Bluetooth устройств
        linearLayout1.setVisibility(View.GONE);
        frameMessage.setVisibility(View.GONE);
        LinerList.setVisibility(View.VISIBLE);
    }

    public void showFrameLedControls() {//метод для показывания на экране экран с дистанционным управлением
        linearLayout1.setVisibility(View.VISIBLE);
        frameMessage.setVisibility(View.GONE);
        LinerList.setVisibility(View.GONE);
    }

    private void cutoutBT(boolean check){//метод необходим, что делать устройству при нажатии на кнопку вкл/выкл Bluetooth
        if(check){
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent,REQUEST_ENABLE_BT);
        }else {
            bluetoothAdapter.disable();
        }
    }

    private void setList(int type){//метод формирующий списки с разной анимацией
        int iconTupe = R.drawable.ic_baseline_bluetooth_connected_24dp;
        bluetoothDevices.clear();

        switch (type){
            case BT_BOUNDED:
                bluetoothDevices = getBoundBluetoothDevices();
                iconTupe = R.drawable.ic_baseline_bluetooth_connected_24dp;
                //checkBound = true;
                break;
            case BT_SEARCH:
                
                iconTupe = R.drawable.ic_baseline_bluetooth_searching_24dp;
                break;
        }
        list_bt = new List_bt(this, bluetoothDevices, iconTupe);
        BtListView.setAdapter(list_bt);
    }

    private ArrayList<BluetoothDevice> getBoundBluetoothDevices(){//метод, возвращающий связанный список уже с ранне подключенными к устройству системами и устройствами
        Set<BluetoothDevice> deviceSet = bluetoothAdapter.getBondedDevices();
        ArrayList<BluetoothDevice> arrayListBT = new ArrayList<>();
        if(deviceSet.size()>0){
            for(BluetoothDevice btdevice : deviceSet){
                arrayListBT.add(btdevice);
            }
        }
        return arrayListBT;
    }

    private void IsSearch(){//метод необходим для нажатии кнопки кнопки поиска устройств
        if(bluetoothAdapter.isDiscovering()){
            bluetoothAdapter.cancelDiscovery();
        }else {
            accessLocationPermission();
            bluetoothAdapter.startDiscovery();
        }
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {//метод, что будет происходить с экраном при нажатии кнопки поиска устройств
            final String action = intent.getAction();

            switch (action){
                case BluetoothAdapter.ACTION_DISCOVERY_STARTED:
                    bt_search.setText("Остановить поиск");
                    PrBar.setVisibility(View.VISIBLE);
                    setList(BT_SEARCH);
                    break;
                case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                    bt_search.setText(R.string.Search_devices);
                    PrBar.setVisibility(View.GONE);
                    break;
                case BluetoothDevice.ACTION_FOUND:
                    BluetoothDevice Device =
                            intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    if(Device!= null){
                        bluetoothDevices.add(Device);
                        list_bt.notifyDataSetChanged();
                    }
                    break;
            }
        }
    };

    /**
     * Запрос на разрешение данных о местоположении (для Marshmallow 6.0)
     */
    private void accessLocationPermission() {//метод для разрешения пользования геолокации
        int accessCoarseLocation = this.checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION);
        int accessFineLocation   = this.checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION);

        List<String> listRequestPermission = new ArrayList<String>();

        if (accessCoarseLocation != PackageManager.PERMISSION_GRANTED) {
            listRequestPermission.add(android.Manifest.permission.ACCESS_COARSE_LOCATION);
        }
        if (accessFineLocation != PackageManager.PERMISSION_GRANTED) {
            listRequestPermission.add(android.Manifest.permission.ACCESS_FINE_LOCATION);
        }

        if (!listRequestPermission.isEmpty()) {
            String[] strRequestPermission = listRequestPermission.toArray(new String[listRequestPermission.size()]);
            this.requestPermissions(strRequestPermission, REQUEST_CODE_LOC);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {//метод для возвращения результата разрешения
        switch (requestCode) {
            case REQUEST_CODE_LOC:

                if (grantResults.length > 0) {
                    for (int gr : grantResults) {
                        // Check if request is granted or not
                        if (gr != PackageManager.PERMISSION_GRANTED) {
                            return;
                        }
                    }
                    //TODO - Add your code here to start Discovery
                }
                break;
            default:
                return;
        }
    }

    public class ConnectThread  extends  Thread implements Serializable{//внктренний класс для подключения к устройству
        private BluetoothSocket bluetoothSocket = null;
        private boolean success = false;

        public ConnectThread(BluetoothDevice device){//конструктор этого класса
            try {
                Method method = device.getClass().getMethod("createRfcommSocket", new Class[] {int.class});
                bluetoothSocket = (BluetoothSocket) method.invoke(device,1);
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        @Override
        public void run() {//метод для многопоточной работы
            try {
                bluetoothSocket.connect();
                success = true;
            }catch (IOException e){
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "Нет соединения",
                                Toast.LENGTH_SHORT).show();
                    }
                });

                cancel();

            }

            if(success){
                //TODO создаем эксземпляр класса
                connectedThread = new ConnectedThread(bluetoothSocket);
                connectedThread.start();
                runOnUiThread(new Runnable() {
                   @Override
                      public void run() {
                        showFrameLedControls();
                    }
                });
            }
        }

        public boolean isConnected(){
            return bluetoothSocket.isConnected();
        }//проверка подключения

        public void cancel(){//метод для отключения от системы
            try {
                bluetoothSocket.close();
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    public class ConnectedThread extends Thread implements Serializable{//внутренний для передачи команд, при подключенном устройстве через Bluetooth
        private transient OutputStream outputStream;

        public ConnectedThread(BluetoothSocket bluetoothSocket){//конструктор этого класса
            OutputStream outputStream = null;
            try {
                outputStream = bluetoothSocket.getOutputStream();
            }catch (IOException e){
                e.printStackTrace();
            }
            this.outputStream = outputStream;
        }

        public void write(String command){//метод для побитовой передачи команд
            byte[] bytes =  command.getBytes();
            if(outputStream != null){
                try {
                    outputStream.write(bytes);
                    outputStream.flush();
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        }

        public void cancel(){//команда для отключения и переставание передачи команд в систему
            try {
                outputStream.close();
            }catch (IOException e){
                e.printStackTrace();
            }
        }

    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {//метод для переключателей, как был нажат
        if(buttonView.equals(switchRele)){
            enableRele(RELLE, isChecked);
        }else if(buttonView.equals(switchPir)){
            enableRele(PIR,isChecked);
        }
    }

    public void enableRele(int rele, boolean state){//какие команды передаются, в зависимости от выбора переключателя
        if(connectedThread!= null && connectThread.isConnected()){
            String command = "";
            switch (rele){
                case RELLE:
                    command = (state) ? "on#" : "off#";
                    break;
                case PIR:
                    command = (state) ? "onPir#":"offPir#";
                    break;
            }
            connectedThread.write(command);
        }
    }
}