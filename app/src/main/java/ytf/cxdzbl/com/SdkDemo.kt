package ytf.cxdzbl.com

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.View
import android.widget.Button
import android.widget.TextView

import com.baidu.location.BDLocation
import com.baidu.location.BDLocationListener
import com.baidu.location.LocationClient
import com.baidu.location.LocationClientOption
import com.baidu.location.Poi

/***
 * 此demo为定位基础示例
 * author zhh
 */
class SdkDemo : Activity() {
    private var locationResult: TextView? = null
    private var startLocation: Button? = null

    private var mLocationClient: LocationClient? = null
    private var mOption: LocationClientOption? = null

    /**
     * 默认高精度，设置定位模式
     * LocationMode.Hight_Accuracy 高精度定位模式：这种定位模式下，会同时使用网络定位和GPS定位，优先返回最高精度的定位结果
     * LocationMode.Battery_Saving 低功耗定位模式：这种定位模式下，不会使用GPS，只会使用网络定位（Wi-Fi和基站定位）
     * LocationMode.Device_Sensors 仅用设备定位模式：这种定位模式下，不需要连接网络，只使用GPS进行定位，这种模式下不支持室内环境的定位
     */
    /**
     * 默认gcj02，设置返回的定位结果坐标系，如果配合百度地图使用，建议设置为bd09ll;
     * 目前国内主要有以下三种坐标系：
     * 1. wgs84：目前广泛使用的GPS全球卫星定位系统使用的标准坐标系；
     * 2. gcj02：经过国测局加密的坐标；
     * 3. bd09：为百度坐标系，其中bd09ll表示百度经纬度坐标，bd09mc表示百度墨卡托米制坐标；
     * 海外地区定位结果默认、且只能是wgs84类型坐标
     */
    /**
     * 默认0，即仅定位一次；设置间隔需大于等于1000ms，表示周期性定位
     * 如果不在AndroidManifest.xml声明百度指定的Service，周期性请求无法正常工作
     * 这里需要注意的是：如果是室外gps定位，不用访问服务器，设置的间隔是1秒，那么就是1秒返回一次位置
     * 如果是WiFi基站定位，需要访问服务器，这个时候每次网络请求时间差异很大，设置的间隔是3秒，只能大概保证3秒左右会返回就一次位置，有时某次定位可能会5秒返回
     */
    /**
     * 默认false，设置是否需要地址信息
     * 返回省市区等地址信息，这个api用处很大，很多新闻类api会根据定位返回的市区信息推送用户所在市的新闻
     */
    /**
     * 默认是true，设置是否使用gps定位
     * 如果设置为false，即使mOption.setLocationMode(LocationMode.Hight_Accuracy)也不会gps定位
     */
    /**
     * 默认false，设置是否需要位置语义化结果
     * 可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
     *///
    /**
     * 默认false,设置是否需要设备方向传感器的方向结果
     * 一般在室外gps定位，时返回的位置信息是带有方向的，但是有时候gps返回的位置也不带方向，这个时候可以获取设备方向传感器的方向
     * wifi基站定位的位置信息是不带方向的，如果需要可以获取设备方向传感器的方向
     */
    /**
     * 默认false，设置是否当gps有效时按照1S1次频率输出GPS结果
     * 室外gps有效时，周期性1秒返回一次位置信息，其实就是设置了
     * locationManager.requestLocationUpdates中的minTime参数为1000ms，1秒回调一个gps位置
     */
    /**
     * 默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死
     * 如果你已经拿到了你要的位置信息，不需要再定位了，不杀死留着干嘛
     */
    /**
     * 默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
     * POI就是获取到的位置附近的一些商场、饭店、银行等信息
     */
    /**
     * 默认false，设置是否收集CRASH信息，默认收集
     */
    val defaultLocationClientOption: LocationClientOption
        get() {
            if (mOption == null) {
                mOption = LocationClientOption()
                mOption!!.locationMode = LocationClientOption.LocationMode.Hight_Accuracy
                mOption!!.setCoorType("bd09ll")
                mOption!!.setScanSpan(3000)
                mOption!!.setIsNeedAddress(true)
                mOption!!.isOpenGps = true
                mOption!!.setIsNeedLocationDescribe(true)
                mOption!!.setNeedDeviceDirect(false)
                mOption!!.isLocationNotify = false
                mOption!!.setIgnoreKillProcess(true)
                mOption!!.setIsNeedLocationPoiList(true)
                mOption!!.SetIgnoreCacheException(false)

            }
            return mOption!!
        }

    internal var handler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            locationResult!!.text = msg.obj as String
        }
    }

    /*****
     *
     * 定位结果回调，重写onReceiveLocation方法，可以直接拷贝如下代码到自己工程中修改
     *
     */
    private val mListener = BDLocationListener { location ->
        val sb = StringBuffer(256)
        sb.append("Thread 当前线程名字: " + Thread.currentThread().name)
        sb.append("\nphone 当前时间间隔ms: " + System.currentTimeMillis())
        sb.append("\ntime 时间: ")
        sb.append(location.time)
        sb.append("\nlocType 定位类型: ")// 定位类型
        sb.append(location.locType)
        sb.append("\nlatitude 纬度: ")// 纬度
        sb.append(location.latitude)
        sb.append("\nlontitude 经度: ")// 经度
        sb.append(location.longitude)
        sb.append("\nradius 半径: ")// 半径
        sb.append(location.radius)
        sb.append("\nCountryCode 国家码: ")// 国家码
        sb.append(location.countryCode)
        sb.append("\nCountry 国家名称: ")// 国家名称
        sb.append(location.country)
        sb.append("\ncitycode 城市编码: ")// 城市编码
        sb.append(location.cityCode)
        sb.append("\ncity 城市: ")// 城市
        sb.append(location.city)
        sb.append("\nDistrict  区: ")// 区
        sb.append(location.district)
        sb.append("\nStreet 街道: ")// 街道
        sb.append(location.street)
        sb.append("\naddr 地址信息: ")// 地址信息
        sb.append(location.addrStr)
        sb.append("\nDirection(not all devices have value): ")
        sb.append(location.direction)// 方向
        sb.append("\nlocationdescribe: ")
        sb.append(location.locationDescribe)// 位置语义化信息
        sb.append("\nPoi: ")// POI信息
        if (location.poiList != null && !location.poiList.isEmpty()) {
            for (i in 0 until location.poiList.size) {
                val poi = location.poiList[i] as Poi
                sb.append(poi.name + ";")
            }
        }
        if (location.locType == BDLocation.TypeGpsLocation) {// GPS定位结果
            sb.append("\nspeed 速度 单位：km/h: ")
            sb.append(location.speed)// 速度 单位：km/h
            sb.append("\nsatellite 卫星数目: ")
            sb.append(location.satelliteNumber)// 卫星数目
            sb.append("\nheight海拔高度 : ")
            sb.append(location.altitude)// 海拔高度 单位：米
            sb.append("\ndescribe : ")
            sb.append("gps定位成功")
        } else if (location.locType == BDLocation.TypeNetWorkLocation) {// 网络定位结果
            // 运营商信息
            if (location.hasAltitude()) {// *****如果有海拔高度*****
                sb.append("\nheight : ")
                sb.append(location.altitude)// 单位：米
            }
            sb.append("\noperationers : ")// 运营商信息
            sb.append(location.operators)
            sb.append("方向1：" + location.derect)
            sb.append("方向2：" + location.direction)
            sb.append("\ndescribe : ")
            sb.append("网络定位成功")
        } else if (location.locType == BDLocation.TypeOffLineLocation) {// 离线定位结果
            sb.append("\ndescribe : ")
            sb.append("离线定位成功，离线定位结果也是有效的")
        } else if (location.locType == BDLocation.TypeServerError) {
            sb.append("\ndescribe : ")
            sb.append("服务端网络定位失败，可以反馈IMEI号和大体定位时间到loc-bugs@baidu.com，会有人追查原因")
        } else if (location.locType == BDLocation.TypeNetWorkException) {
            sb.append("\ndescribe : ")
            sb.append("网络不同导致定位失败，请检查网络是否通畅")
        } else if (location.locType == BDLocation.TypeCriteriaException) {
            sb.append("\ndescribe : ")
            sb.append("无法获取有效定位依据导致定位失败，一般是由于手机的原因，处于飞行模式下一般会造成这种结果，可以试着重启手机")
        }
        logMsg(sb.toString())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sdk)
        initView()

        mLocationClient = LocationClient(application)
        mLocationClient!!.locOption = defaultLocationClientOption
        mLocationClient!!.registerLocationListener(mListener)

    }

    private fun initView() {

        locationResult = findViewById<View>(R.id.textView1) as TextView
        startLocation = findViewById<View>(R.id.addfence) as Button
        startLocation!!.setOnClickListener {
            if (startLocation!!.text.toString() == "开始定位") {

                if (mLocationClient != null && !mLocationClient!!.isStarted) {
                    mLocationClient!!.start()
                    startLocation!!.text = "停止定位"
                }

            } else {
                if (mLocationClient != null && mLocationClient!!.isStarted) {
                    mLocationClient!!.stop()
                    startLocation!!.text = "开始定位"
                }
            }
        }

    }

    fun logMsg(str: String) {
        try {
            if (locationResult != null) {
                val msg = Message.obtain()
                msg.obj = str
                handler.sendMessage(msg)
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }

    }


    override fun onDestroy() {
        mLocationClient!!.unRegisterLocationListener(mListener) // 注销掉监听
        if (mLocationClient != null && mLocationClient!!.isStarted) {
            mLocationClient!!.stop() // 停止定位服务
        }
        super.onDestroy()
    }

    override fun onStart() {
        super.onStart()

    }
}
