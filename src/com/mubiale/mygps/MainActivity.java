package com.mubiale.mygps;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Iterator;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

public class MainActivity extends Activity {

	private LocationManager lm = null;
	// �ı���ʾ��
	private EditText editText = null;
	private EditText baiduResponse = null;
	// Button��
	private Button btnGetQuery = null;
	private Button btnPostQuery = null;
	private Button startSample = null;
	// RadioGroup��
	private RadioGroup chooseBus = null;
	private RadioGroup chooseDirection = null;
	private RadioGroup chooseBianhao = null;
	// RadioButton��
	private RadioButton firstBus = null;// ·��
	private RadioButton secondBus = null;
	private RadioButton thirdBus = null;
	private RadioButton bianhao1 = null;// ���
	private RadioButton bianhao2 = null;
	private RadioButton bianhao3 = null;
	private RadioButton bianhao4 = null;
	private RadioButton forthDirection = null; // ����
	private RadioButton backDirection = null;
	// location ��
	private Location location = null;
	// flag������
	private boolean busIsSet = false;// ·��ѡ��
	private boolean directionIsSet = false;// ����ѡ��
	private boolean numberIsSet = false;// ���ѡ��
	private boolean start = false;// ��ʼ��־
	// ��ű�����
	private int busRoute = -1;// У��·��
	private int busNumber = -1;// У�����
	private String busDirection = null;// ���з���
	private int refresh = 0;
	// ������
	private static final String TAG = "GpsActivity";
	private static final String key = "n0AEHrtu2f1HDifNW0EpYtZt";
	private static final String route[] = {// ·��
	"31962", "46643", "46644" };
	private static final String number[][] = {// ���
	{ "47853133", "47850053", "47850147", "48164230" },
			{ "48164258", "48164260", "48164263", "48164268" },
			{ "48164314", "48164317", "48164323", "48164329" } };

	private OnClickListener onClickListener = new OnClickListener() {

		public void onClick(View v) {
			try {
				if (startSample == v) {
					if (!start) {
						start = true;
						startSample.setText("�ɼ�ing");
					} else {
						start = false;
						startSample.setText("��ʼ");
					}
				}
				// startSample.setBackgroundColor("#000000");
				if (btnGetQuery == v) {
					/*
					 * ��Ϊ��GET����������Ҫ�����������ӵ�URL�󣬲��һ���Ҫ����URL���� URL =
					 * http://192.168
					 * .0.103:8080/Server/PrintServlet?name=%E6%88%91&age=20
					 * �˴���Ҫ����URL������Ϊ������ύʱ�Զ�����URL����
					 */
					StringBuilder buf = new StringBuilder(
							"http://api.map.baidu.com/geodata/v2/poi/detail");
					buf.append("?");
					// buf.append("name="+URLEncoder.encode(name.getText().toString(),"UTF-8")+"&");
					// buf.append("age="+URLEncoder.encode(age.getText().toString(),"UTF-8"));
					buf.append("geotable_id=" + route[busRoute] + "&");
					buf.append("id=" + number[busRoute][busNumber] + "&");
					buf.append("ak=" + key);
					URL url = new URL(buf.toString());
					HttpURLConnection conn = (HttpURLConnection) url
							.openConnection();
					// ��������������
					conn.setDoOutput(true);
					conn.setDoInput(true);
					// POST������ʹ�û���
					conn.setUseCaches(false);
					// ��������ʽΪPOST
					conn.setRequestMethod("GET");
					conn.connect(); // May not be needed
					// �õ���ȡ������(��)
					InputStreamReader in = new InputStreamReader(
							conn.getInputStream());
					// Ϊ�������BufferedReader
					BufferedReader buffer = new BufferedReader(in);
					String inputLine = null;
					String resultData = null;
					// ʹ��ѭ������ȡ��õ�����
					while (((inputLine = buffer.readLine()) != null)) {
						// ������ÿһ�к������һ��"\n"������
						resultData += inputLine + "\n";
					}
					// �ر�InputStreamReader
					in.close();
					// �ر�http����

					if (conn.getResponseCode() == 200) {// ���������ص�code
						Toast.makeText(MainActivity.this, "GET�ύ�ɹ�",
								Toast.LENGTH_SHORT).show();
					} else {
						Toast.makeText(MainActivity.this, "GET�ύʧ��",
								Toast.LENGTH_SHORT).show();
					}

					baiduResponse.setText("Http��������\n");
					// baiduResponse.append(String.valueOf(conn.getOutputStream()));//
					// ���������ص���Ϣ
					baiduResponse.append(resultData);// ���������ص���Ϣ

					if (conn != null) {
						// �ر����� ������ http.keepAlive = false;
						conn.disconnect();
					}
				}
				if (btnPostQuery == v) {
					postService();
				}

			} catch (Exception e) {
				e.printStackTrace();

			}

		}
	};

	// ���ϵظ�������
	public void postService() throws IOException {
		StringBuilder buf = new StringBuilder();

		// URL�ɱ䲿��
		buf.append("geotable_id=" + route[busRoute] + "&");// ��ID��У��·��
		buf.append("id=" + number[busRoute][busNumber] + "&");// У�����
		buf.append("latitude=" + String.valueOf(location.getLatitude()) + "&");// ����
		buf.append("longitude=" + String.valueOf(location.getLongitude()) + "&");// γ��
		buf.append("speed=" + String.valueOf(location.getSpeed()) + "&");// �ٶ�
		buf.append("direction=" + busDirection + "&");// ����

		// URL�̶�����
		buf.append("coord_type=" + "1" + "&");// �������ͣ�GPS
		buf.append("ak=" + key);

		byte[] data = buf.toString().getBytes("UTF-8");
		URL url = new URL("http://api.map.baidu.com/geodata/v2/poi/update");
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("POST");
		conn.setDoOutput(true); // ���Ҫ������������ϴ˾�
		OutputStream out = conn.getOutputStream();
		out.write(data);

		InputStreamReader in = new InputStreamReader(conn.getInputStream());
		// Ϊ�������BufferedReader
		BufferedReader buffer = new BufferedReader(in);
		String inputLine = null;
		String resultData = null;
		// ʹ��ѭ������ȡ��õ�����
		while (((inputLine = buffer.readLine()) != null)) {
			// ������ÿһ�к������һ��"\n"������
			resultData += inputLine + "\n";
		}
		// �ر�InputStreamReader
		in.close();

		conn.getInputStream();
		if (conn.getResponseCode() == 200) {
			Toast.makeText(MainActivity.this, "POST�ύ�ɹ�", Toast.LENGTH_SHORT)
					.show();
		} else {
			Toast.makeText(MainActivity.this, "POST�ύʧ��", Toast.LENGTH_SHORT)
					.show();
		}

		baiduResponse.setText("Http��������\n");
		// baiduResponse.append(String.valueOf(conn.getOutputStream()));//
		// ���������ص���Ϣ
		baiduResponse.append(resultData);// ���������ص���Ϣ

		if (conn != null) {
			// �ر����� ������ http.keepAlive = false;
			conn.disconnect();
		}

	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		// RadioGroup�ؼ�
		chooseBus = (RadioGroup) findViewById(R.id.chooseBus);// ѡ��·��
		chooseBianhao = (RadioGroup) findViewById(R.id.chooseBianhao);// ѡ����
		chooseDirection = (RadioGroup) findViewById(R.id.chooseDirection);// ѡ����
		// RadioButton�ؼ�
		firstBus = (RadioButton) findViewById(R.id.firstBus);// ·��
		secondBus = (RadioButton) findViewById(R.id.secondBus);
		thirdBus = (RadioButton) findViewById(R.id.thirdBus);
		bianhao1 = (RadioButton) findViewById(R.id.bianhao1);// ���
		bianhao2 = (RadioButton) findViewById(R.id.bianhao2);
		bianhao3 = (RadioButton) findViewById(R.id.bianhao3);
		bianhao4 = (RadioButton) findViewById(R.id.bianhao4);
		forthDirection = (RadioButton) findViewById(R.id.forthDirection);// ����
		backDirection = (RadioButton) findViewById(R.id.backDirection);
		// �ı��ؼ�
		editText = (EditText) findViewById(R.id.editText);// GPS��Ϣ��ʾ
		baiduResponse = (EditText) findViewById(R.id.baiduResponse);// GPS��Ϣ��ʾ
		// �����ؼ�
		btnGetQuery = (Button) findViewById(R.id.btnGetQuery);// �ύget����
		startSample = (Button) findViewById(R.id.startSample);// ��ʼ�ɼ�
		btnPostQuery = (Button) findViewById(R.id.btnPostQuery);// �ύpost����
		// ��������
		btnGetQuery.setOnClickListener(onClickListener);// ���ü���
		btnPostQuery.setOnClickListener(onClickListener);// ���ü���,ÿ��button��������һ������
		startSample.setOnClickListener(onClickListener);// ���ü���,ÿ��button��������һ������
		// RadioGroup ����
		chooseBus
				.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {// ��μ���
					@Override
					public void onCheckedChanged(RadioGroup group, int checkedId) {
						// TODO Auto-generated method stub
						if (firstBus.getId() == checkedId)
							busRoute = 0;// 1·У��
						else if (secondBus.getId() == checkedId)
							busRoute = 1;
						else if (thirdBus.getId() == checkedId)
							busRoute = 2;

						busIsSet = true;
					}

				});

		chooseBianhao
				.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {// ��μ���
					@Override
					public void onCheckedChanged(RadioGroup group, int checkedId) {
						// TODO Auto-generated method stub
						if (bianhao1.getId() == checkedId)
							busNumber = 0;// 1·У��
						else if (bianhao2.getId() == checkedId)
							busNumber = 1;
						else if (bianhao3.getId() == checkedId)
							busNumber = 2;
						else if (bianhao4.getId() == checkedId)
							busNumber = 3;
						numberIsSet = true;

					}

				});

		chooseDirection
				.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {// �������
					@Override
					public void onCheckedChanged(RadioGroup group, int checkedId) {
						if (forthDirection.getId() == checkedId)
							busDirection = "��������";
						else if (backDirection.getId() == checkedId)
							busDirection = "���ط���";
						directionIsSet = true;
						// TODO Auto-generated method stub

					}
				});

		lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		// �ж�GPS�Ƿ���������
		if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			Toast.makeText(this, "�뿪��GPS����...", Toast.LENGTH_SHORT).show();
			// ���ؿ���GPS�������ý���
			Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);// ��������
			startActivityForResult(intent, 0);
			return;
		}

		// Ϊ��ȡ����λ����Ϣʱ���ò�ѯ����,Returns the name of the provider that best meets the
		// given criteria.
		String bestProvider = lm.getBestProvider(getCriteria(), true);
		// ��ȡλ����Ϣ
		// ��������ò�ѯҪ��getLastKnownLocation�������˵Ĳ���ΪLocationManager.GPS_PROVIDER
		// Returns a Location indicating the data from the last known location
		// fix obtained from the given provider.
		location = lm.getLastKnownLocation(bestProvider);
//		updateView(location);
		// ����״̬
		lm.addGpsStatusListener(listener);
		// �󶨼�������4������
		// ����1���豸����GPS_PROVIDER��NETWORK_PROVIDER����
		// ����2��λ����Ϣ�������ڣ���λ����
		// ����3��λ�ñ仯��С���룺��λ�þ���仯������ֵʱ��������λ����Ϣ
		// ����4������
		// ��ע������2��3���������3��Ϊ0�����Բ���3Ϊ׼������3Ϊ0����ͨ��ʱ������ʱ���£�����Ϊ0������ʱˢ��

		// 1�����һ�Σ�����Сλ�Ʊ仯����1�׸���һ�Σ�
		// ע�⣺�˴�����׼ȷ�ȷǳ��ͣ��Ƽ���service��������һ��Thread����run��sleep(10000);Ȼ��ִ��handler.sendMessage(),����λ��
		// requestLocationUpdates(String provider, long minTime, float
		// minDistance, LocationListener listener)
		// Register for location updates using the named provider, and a pending
		// intent.
		lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000, 5,
				locationListener);//3���Ӹ���һ��λ����Ϣ�� �ƶ�5mˢ��һ��
	}

	// Activity ���ٷ���
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		lm.removeUpdates(locationListener);
	}

	// λ�ü���
	private LocationListener locationListener = new LocationListener() {

		/**
		 * λ����Ϣ�仯ʱ����
		 */
		public void onLocationChanged(Location location) {
			updateView(location);
			// ���ȫ�����ã���ʼ����ɼ�ģʽ
			if (busIsSet && directionIsSet && numberIsSet && start)
				try {
					postService();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}// ����λ������

			Log.i(TAG, "ʱ�䣺" + location.getTime());
			Log.i(TAG, "���ȣ�" + location.getLongitude());
			Log.i(TAG, "γ�ȣ�" + location.getLatitude());
			Log.i(TAG, "���Σ�" + location.getAltitude());
		}

		/**
		 * GPS״̬�仯ʱ����
		 */
		public void onStatusChanged(String provider, int status, Bundle extras) {
			switch (status) {
			// GPS״̬Ϊ�ɼ�ʱ
			case LocationProvider.AVAILABLE:
				Log.i(TAG, "��ǰGPS״̬Ϊ�ɼ�״̬");
				break;
			// GPS״̬Ϊ��������ʱ
			case LocationProvider.OUT_OF_SERVICE:
				Log.i(TAG, "��ǰGPS״̬Ϊ��������״̬");
				break;
			// GPS״̬Ϊ��ͣ����ʱ
			case LocationProvider.TEMPORARILY_UNAVAILABLE:
				Log.i(TAG, "��ǰGPS״̬Ϊ��ͣ����״̬");
				break;
			}
		}

		/**
		 * GPS����ʱ����
		 */
		public void onProviderEnabled(String provider) {
			Location location = lm.getLastKnownLocation(provider);
			updateView(location);
		}

		/**
		 * GPS����ʱ����
		 */
		public void onProviderDisabled(String provider) {
			updateView(null);
		}

	};

	// ״̬����
	GpsStatus.Listener listener = new GpsStatus.Listener() {
		public void onGpsStatusChanged(int event) {
			switch (event) {
			// ��һ�ζ�λ
			case GpsStatus.GPS_EVENT_FIRST_FIX:
				Log.i(TAG, "��һ�ζ�λ");
				refresh();
				editText.append("\n��һ�ζ�λ");

				break;
			// ����״̬�ı�
			case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
				Log.i(TAG, "����״̬�ı�");
				refresh();
				editText.append("\n����״̬�ı�");
				// ��ȡ��ǰ״̬
				GpsStatus gpsStatus = lm.getGpsStatus(null);
				// ��ȡ���ǿ�����Ĭ�����ֵ
				int maxSatellites = gpsStatus.getMaxSatellites();
				// ����һ��������������������
				Iterator<GpsSatellite> iters = gpsStatus.getSatellites()
						.iterator();
				int count = 0;
				while (iters.hasNext() && count <= maxSatellites) {
					// GpsSatellite s = iters.next();
					iters.next();
					count++;
				}
				System.out.println("��������" + count + "������");
				refresh();
				editText.append("��������" + count + "������");
				break;
			// ��λ����
			case GpsStatus.GPS_EVENT_STARTED:
				Log.i(TAG, "��λ����");
				break;
			// ��λ����
			case GpsStatus.GPS_EVENT_STOPPED:
				Log.i(TAG, "��λ����");
				break;
			}
		};
	};

	/**
	 * ʵʱ�����ı�����
	 * 
	 * @param location
	 */
	private void updateView(Location location) {
		if (location != null) {
			editText.getEditableText().clear();// ���������д
			editText.setText("�豸λ����Ϣ\n��ʷ��λʱ�䣺");
			editText.append(formateTimeStempToString(location.getTime()));
			editText.append("\n���ȣ�");
			editText.append(String.valueOf(location.getLongitude()));
			editText.append("\nγ�ȣ�");
			editText.append(String.valueOf(location.getLatitude()));
			editText.append("\n�ٶȣ�");
			editText.append(String.valueOf(location.getSpeed()));
			editText.append("\nУ��·�ߣ�");
			editText.append(String.valueOf(busRoute));
			editText.append("\n���з���");
			editText.append(String.valueOf(busDirection));
			editText.append("\n���ȣ�");
			editText.append(String.valueOf(location.getAccuracy()));
			editText.append("\n��λ��ʽ��");
			editText.append(String.valueOf(location.getProvider()) + "  ��λ");
			refresh = refresh + 12;
		} else {
			// ���EditText����
			editText.getEditableText().clear();
		}
	};

	private void refresh() {
		
		if (refresh >= 12) {
			editText.getEditableText().clear();
			editText.setText("У��GPS��Ϣ");
			refresh = 0;
		}
		refresh ++;
	}

	/**
	 * ���ز�ѯ����
	 * 
	 * @return
	 */
	private Criteria getCriteria() {
		Criteria criteria = new Criteria();
		// ���ö�λ��ȷ�� Criteria.ACCURACY_COARSE�Ƚϴ��ԣ�Criteria.ACCURACY_FINE��ȽϾ�ϸ
		criteria.setAccuracy(Criteria.ACCURACY_COARSE);
		// �����Ƿ�Ҫ���ٶ�
		criteria.setSpeedRequired(true);
		// �����Ƿ�������Ӫ���շ�
		criteria.setCostAllowed(true);
		// �����Ƿ���Ҫ��λ��Ϣ Indicates whether the provider must provide bearing
		// information.
		criteria.setBearingRequired(true);
		// �����Ƿ���Ҫ������Ϣ
		criteria.setAltitudeRequired(true);
		// ���öԵ�Դ������
		criteria.setPowerRequirement(Criteria.POWER_LOW);
		return criteria;
	}

	// ��ʱ���ʽ��
	public static String formateTimeStempToString(long timeStemp) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return sdf.format(timeStemp);
	}
}