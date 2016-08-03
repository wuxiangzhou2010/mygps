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
	// 文本显示类
	private EditText editText = null;
	private EditText baiduResponse = null;
	// Button类
	private Button btnGetQuery = null;
	private Button btnPostQuery = null;
	private Button startSample = null;
	// RadioGroup类
	private RadioGroup chooseBus = null;
	private RadioGroup chooseDirection = null;
	private RadioGroup chooseBianhao = null;
	// RadioButton类
	private RadioButton firstBus = null;// 路线
	private RadioButton secondBus = null;
	private RadioButton thirdBus = null;
	private RadioButton bianhao1 = null;// 编号
	private RadioButton bianhao2 = null;
	private RadioButton bianhao3 = null;
	private RadioButton bianhao4 = null;
	private RadioButton forthDirection = null; // 方向
	private RadioButton backDirection = null;
	// location 类
	private Location location = null;
	// flag变量类
	private boolean busIsSet = false;// 路线选择
	private boolean directionIsSet = false;// 方向选择
	private boolean numberIsSet = false;// 编号选择
	private boolean start = false;// 开始标志
	// 标号变量类
	private int busRoute = -1;// 校车路线
	private int busNumber = -1;// 校车编号
	private String busDirection = null;// 运行方向
	private int refresh = 0;
	// 常量类
	private static final String TAG = "GpsActivity";
	private static final String key = "n0AEHrtu2f1HDifNW0EpYtZt";
	private static final String route[] = {// 路线
	"31962", "46643", "46644" };
	private static final String number[][] = {// 编号
	{ "47853133", "47850053", "47850147", "48164230" },
			{ "48164258", "48164260", "48164263", "48164268" },
			{ "48164314", "48164317", "48164323", "48164329" } };

	private OnClickListener onClickListener = new OnClickListener() {

		public void onClick(View v) {
			try {
				if (startSample == v) {
					if (!start) {
						start = true;
						startSample.setText("采集ing");
					} else {
						start = false;
						startSample.setText("开始");
					}
				}
				// startSample.setBackgroundColor("#000000");
				if (btnGetQuery == v) {
					/*
					 * 因为是GET请求，所以需要将请求参数添加到URL后，并且还需要进行URL编码 URL =
					 * http://192.168
					 * .0.103:8080/Server/PrintServlet?name=%E6%88%91&age=20
					 * 此处需要进行URL编码因为浏览器提交时自动进行URL编码
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
					// 设置输入和输出流
					conn.setDoOutput(true);
					conn.setDoInput(true);
					// POST请求不能使用缓存
					conn.setUseCaches(false);
					// 设置请求方式为POST
					conn.setRequestMethod("GET");
					conn.connect(); // May not be needed
					// 得到读取的内容(流)
					InputStreamReader in = new InputStreamReader(
							conn.getInputStream());
					// 为输出创建BufferedReader
					BufferedReader buffer = new BufferedReader(in);
					String inputLine = null;
					String resultData = null;
					// 使用循环来读取获得的数据
					while (((inputLine = buffer.readLine()) != null)) {
						// 我们在每一行后面加上一个"\n"来换行
						resultData += inputLine + "\n";
					}
					// 关闭InputStreamReader
					in.close();
					// 关闭http连接

					if (conn.getResponseCode() == 200) {// 服务器返回的code
						Toast.makeText(MainActivity.this, "GET提交成功",
								Toast.LENGTH_SHORT).show();
					} else {
						Toast.makeText(MainActivity.this, "GET提交失败",
								Toast.LENGTH_SHORT).show();
					}

					baiduResponse.setText("Http返回数据\n");
					// baiduResponse.append(String.valueOf(conn.getOutputStream()));//
					// 服务器返回的信息
					baiduResponse.append(resultData);// 服务器返回的信息

					if (conn != null) {
						// 关闭连接 即设置 http.keepAlive = false;
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

	// 不断地更新数据
	public void postService() throws IOException {
		StringBuilder buf = new StringBuilder();

		// URL可变部分
		buf.append("geotable_id=" + route[busRoute] + "&");// 表ID，校车路线
		buf.append("id=" + number[busRoute][busNumber] + "&");// 校车编号
		buf.append("latitude=" + String.valueOf(location.getLatitude()) + "&");// 精度
		buf.append("longitude=" + String.valueOf(location.getLongitude()) + "&");// 纬度
		buf.append("speed=" + String.valueOf(location.getSpeed()) + "&");// 速度
		buf.append("direction=" + busDirection + "&");// 方向

		// URL固定部分
		buf.append("coord_type=" + "1" + "&");// 坐标类型，GPS
		buf.append("ak=" + key);

		byte[] data = buf.toString().getBytes("UTF-8");
		URL url = new URL("http://api.map.baidu.com/geodata/v2/poi/update");
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("POST");
		conn.setDoOutput(true); // 如果要输出，则必须加上此句
		OutputStream out = conn.getOutputStream();
		out.write(data);

		InputStreamReader in = new InputStreamReader(conn.getInputStream());
		// 为输出创建BufferedReader
		BufferedReader buffer = new BufferedReader(in);
		String inputLine = null;
		String resultData = null;
		// 使用循环来读取获得的数据
		while (((inputLine = buffer.readLine()) != null)) {
			// 我们在每一行后面加上一个"\n"来换行
			resultData += inputLine + "\n";
		}
		// 关闭InputStreamReader
		in.close();

		conn.getInputStream();
		if (conn.getResponseCode() == 200) {
			Toast.makeText(MainActivity.this, "POST提交成功", Toast.LENGTH_SHORT)
					.show();
		} else {
			Toast.makeText(MainActivity.this, "POST提交失败", Toast.LENGTH_SHORT)
					.show();
		}

		baiduResponse.setText("Http返回数据\n");
		// baiduResponse.append(String.valueOf(conn.getOutputStream()));//
		// 服务器返回的信息
		baiduResponse.append(resultData);// 服务器返回的信息

		if (conn != null) {
			// 关闭连接 即设置 http.keepAlive = false;
			conn.disconnect();
		}

	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		// RadioGroup控件
		chooseBus = (RadioGroup) findViewById(R.id.chooseBus);// 选择路线
		chooseBianhao = (RadioGroup) findViewById(R.id.chooseBianhao);// 选择编号
		chooseDirection = (RadioGroup) findViewById(R.id.chooseDirection);// 选择方向
		// RadioButton控件
		firstBus = (RadioButton) findViewById(R.id.firstBus);// 路线
		secondBus = (RadioButton) findViewById(R.id.secondBus);
		thirdBus = (RadioButton) findViewById(R.id.thirdBus);
		bianhao1 = (RadioButton) findViewById(R.id.bianhao1);// 编号
		bianhao2 = (RadioButton) findViewById(R.id.bianhao2);
		bianhao3 = (RadioButton) findViewById(R.id.bianhao3);
		bianhao4 = (RadioButton) findViewById(R.id.bianhao4);
		forthDirection = (RadioButton) findViewById(R.id.forthDirection);// 方向
		backDirection = (RadioButton) findViewById(R.id.backDirection);
		// 文本控件
		editText = (EditText) findViewById(R.id.editText);// GPS信息显示
		baiduResponse = (EditText) findViewById(R.id.baiduResponse);// GPS信息显示
		// 按键控件
		btnGetQuery = (Button) findViewById(R.id.btnGetQuery);// 提交get请求
		startSample = (Button) findViewById(R.id.startSample);// 开始采集
		btnPostQuery = (Button) findViewById(R.id.btnPostQuery);// 提交post请求
		// 按键监听
		btnGetQuery.setOnClickListener(onClickListener);// 设置监听
		btnPostQuery.setOnClickListener(onClickListener);// 设置监听,每个button单独设置一个监听
		startSample.setOnClickListener(onClickListener);// 设置监听,每个button单独设置一个监听
		// RadioGroup 监听
		chooseBus
				.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {// 班次监听
					@Override
					public void onCheckedChanged(RadioGroup group, int checkedId) {
						// TODO Auto-generated method stub
						if (firstBus.getId() == checkedId)
							busRoute = 0;// 1路校车
						else if (secondBus.getId() == checkedId)
							busRoute = 1;
						else if (thirdBus.getId() == checkedId)
							busRoute = 2;

						busIsSet = true;
					}

				});

		chooseBianhao
				.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {// 班次监听
					@Override
					public void onCheckedChanged(RadioGroup group, int checkedId) {
						// TODO Auto-generated method stub
						if (bianhao1.getId() == checkedId)
							busNumber = 0;// 1路校车
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
				.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {// 方向监听
					@Override
					public void onCheckedChanged(RadioGroup group, int checkedId) {
						if (forthDirection.getId() == checkedId)
							busDirection = "出发方向";
						else if (backDirection.getId() == checkedId)
							busDirection = "返回方向";
						directionIsSet = true;
						// TODO Auto-generated method stub

					}
				});

		lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		// 判断GPS是否正常启动
		if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			Toast.makeText(this, "请开启GPS导航...", Toast.LENGTH_SHORT).show();
			// 返回开启GPS导航设置界面
			Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);// 设置网络
			startActivityForResult(intent, 0);
			return;
		}

		// 为获取地理位置信息时设置查询条件,Returns the name of the provider that best meets the
		// given criteria.
		String bestProvider = lm.getBestProvider(getCriteria(), true);
		// 获取位置信息
		// 如果不设置查询要求，getLastKnownLocation方法传人的参数为LocationManager.GPS_PROVIDER
		// Returns a Location indicating the data from the last known location
		// fix obtained from the given provider.
		location = lm.getLastKnownLocation(bestProvider);
//		updateView(location);
		// 监听状态
		lm.addGpsStatusListener(listener);
		// 绑定监听，有4个参数
		// 参数1，设备：有GPS_PROVIDER和NETWORK_PROVIDER两种
		// 参数2，位置信息更新周期，单位毫秒
		// 参数3，位置变化最小距离：当位置距离变化超过此值时，将更新位置信息
		// 参数4，监听
		// 备注：参数2和3，如果参数3不为0，则以参数3为准；参数3为0，则通过时间来定时更新；两者为0，则随时刷新

		// 1秒更新一次，或最小位移变化超过1米更新一次；
		// 注意：此处更新准确度非常低，推荐在service里面启动一个Thread，在run中sleep(10000);然后执行handler.sendMessage(),更新位置
		// requestLocationUpdates(String provider, long minTime, float
		// minDistance, LocationListener listener)
		// Register for location updates using the named provider, and a pending
		// intent.
		lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000, 5,
				locationListener);//3秒钟更新一次位置信息。 移动5m刷新一次
	}

	// Activity 销毁方法
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		lm.removeUpdates(locationListener);
	}

	// 位置监听
	private LocationListener locationListener = new LocationListener() {

		/**
		 * 位置信息变化时触发
		 */
		public void onLocationChanged(Location location) {
			updateView(location);
			// 如果全部设置，则开始进入采集模式
			if (busIsSet && directionIsSet && numberIsSet && start)
				try {
					postService();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}// 发送位置数据

			Log.i(TAG, "时间：" + location.getTime());
			Log.i(TAG, "经度：" + location.getLongitude());
			Log.i(TAG, "纬度：" + location.getLatitude());
			Log.i(TAG, "海拔：" + location.getAltitude());
		}

		/**
		 * GPS状态变化时触发
		 */
		public void onStatusChanged(String provider, int status, Bundle extras) {
			switch (status) {
			// GPS状态为可见时
			case LocationProvider.AVAILABLE:
				Log.i(TAG, "当前GPS状态为可见状态");
				break;
			// GPS状态为服务区外时
			case LocationProvider.OUT_OF_SERVICE:
				Log.i(TAG, "当前GPS状态为服务区外状态");
				break;
			// GPS状态为暂停服务时
			case LocationProvider.TEMPORARILY_UNAVAILABLE:
				Log.i(TAG, "当前GPS状态为暂停服务状态");
				break;
			}
		}

		/**
		 * GPS开启时触发
		 */
		public void onProviderEnabled(String provider) {
			Location location = lm.getLastKnownLocation(provider);
			updateView(location);
		}

		/**
		 * GPS禁用时触发
		 */
		public void onProviderDisabled(String provider) {
			updateView(null);
		}

	};

	// 状态监听
	GpsStatus.Listener listener = new GpsStatus.Listener() {
		public void onGpsStatusChanged(int event) {
			switch (event) {
			// 第一次定位
			case GpsStatus.GPS_EVENT_FIRST_FIX:
				Log.i(TAG, "第一次定位");
				refresh();
				editText.append("\n第一次定位");

				break;
			// 卫星状态改变
			case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
				Log.i(TAG, "卫星状态改变");
				refresh();
				editText.append("\n卫星状态改变");
				// 获取当前状态
				GpsStatus gpsStatus = lm.getGpsStatus(null);
				// 获取卫星颗数的默认最大值
				int maxSatellites = gpsStatus.getMaxSatellites();
				// 创建一个迭代器保存所有卫星
				Iterator<GpsSatellite> iters = gpsStatus.getSatellites()
						.iterator();
				int count = 0;
				while (iters.hasNext() && count <= maxSatellites) {
					// GpsSatellite s = iters.next();
					iters.next();
					count++;
				}
				System.out.println("搜索到：" + count + "颗卫星");
				refresh();
				editText.append("搜索到：" + count + "颗卫星");
				break;
			// 定位启动
			case GpsStatus.GPS_EVENT_STARTED:
				Log.i(TAG, "定位启动");
				break;
			// 定位结束
			case GpsStatus.GPS_EVENT_STOPPED:
				Log.i(TAG, "定位结束");
				break;
			}
		};
	};

	/**
	 * 实时更新文本内容
	 * 
	 * @param location
	 */
	private void updateView(Location location) {
		if (location != null) {
			editText.getEditableText().clear();// 先清空在填写
			editText.setText("设备位置信息\n历史定位时间：");
			editText.append(formateTimeStempToString(location.getTime()));
			editText.append("\n经度：");
			editText.append(String.valueOf(location.getLongitude()));
			editText.append("\n纬度：");
			editText.append(String.valueOf(location.getLatitude()));
			editText.append("\n速度：");
			editText.append(String.valueOf(location.getSpeed()));
			editText.append("\n校车路线：");
			editText.append(String.valueOf(busRoute));
			editText.append("\n运行方向：");
			editText.append(String.valueOf(busDirection));
			editText.append("\n精度：");
			editText.append(String.valueOf(location.getAccuracy()));
			editText.append("\n定位方式：");
			editText.append(String.valueOf(location.getProvider()) + "  定位");
			refresh = refresh + 12;
		} else {
			// 清空EditText对象
			editText.getEditableText().clear();
		}
	};

	private void refresh() {
		
		if (refresh >= 12) {
			editText.getEditableText().clear();
			editText.setText("校车GPS信息");
			refresh = 0;
		}
		refresh ++;
	}

	/**
	 * 返回查询条件
	 * 
	 * @return
	 */
	private Criteria getCriteria() {
		Criteria criteria = new Criteria();
		// 设置定位精确度 Criteria.ACCURACY_COARSE比较粗略，Criteria.ACCURACY_FINE则比较精细
		criteria.setAccuracy(Criteria.ACCURACY_COARSE);
		// 设置是否要求速度
		criteria.setSpeedRequired(true);
		// 设置是否允许运营商收费
		criteria.setCostAllowed(true);
		// 设置是否需要方位信息 Indicates whether the provider must provide bearing
		// information.
		criteria.setBearingRequired(true);
		// 设置是否需要海拔信息
		criteria.setAltitudeRequired(true);
		// 设置对电源的需求
		criteria.setPowerRequirement(Criteria.POWER_LOW);
		return criteria;
	}

	// 将时间格式化
	public static String formateTimeStempToString(long timeStemp) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return sdf.format(timeStemp);
	}
}