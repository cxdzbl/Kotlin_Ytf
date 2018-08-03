package ytf.cxdzbl.com

import android.animation.ObjectAnimator
import java.io.Serializable
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import android.content.Context
import android.content.Intent
import android.graphics.Point
import android.hardware.SensorManager
import android.graphics.BitmapFactory
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory

import org.jetbrains.anko.alert
import android.widget.*

import com.baidu.location.BDLocationListener
import com.baidu.location.LocationClient
import com.baidu.location.LocationClientOption
import com.baidu.location.BDLocation

import com.baidu.mapapi.map.*
import com.baidu.mapapi.model.LatLng
import com.baidu.mapapi.map.MyLocationConfiguration.LocationMode
import kotlinx.android.synthetic.main.ytf__tab__map.*
import android.text.method.TextKeyListener.clear
import android.util.DisplayMetrics
import android.util.Log
import android.widget.TextView
import com.baidu.mapapi.map.Marker
import org.jetbrains.anko.toast
import com.baidu.mapapi.map.BitmapDescriptorFactory


import com.baidu.mapapi.map.BaiduMap
import com.baidu.mapapi.utils.DistanceUtil
import org.jetbrains.anko.find
import ytf.cxdzbl.com.R.id.content


var mMapView: MapView? = null
var mBaiduMap:BaiduMap?=null
var isFirstLoc = true
var mLocClient:LocationClient?=null
private var mOption: LocationClientOption? = null

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 *
 */
private var mCurrentMode: LocationMode? = null
internal var mCurrentMarker: BitmapDescriptor? = null
private val accuracyCircleFillColor = -0x55000078
private val accuracyCircleStrokeColor = -0x55ff0100
private var mSensorManager: SensorManager? = null
private var lastX: Double? = 0.0
private var mCurrentDirection = 0
private var mCurrentLat = 0.0
private var mCurrentLon = 0.0
private var mCurrentAccracy: Float = 0.toFloat()
//internal var isFirstLoc = true // 是否首次定位
private var locData: MyLocationData? = null
private val direction: Float = 0.toFloat()
private var showMarker=true
private var rl_marker:RelativeLayout?=null
private var marker:Marker?=null
private var infos = ArrayList<MarkerInfoUtil>()

var andm_y:Float? =null

class Ytf_Tab_Map : Fragment(){
    var myListener =MyLocationListenner()
    var cxd=5
    val cxdddddd:String="kdjfkjkfkfjk"
//住码云上更新代码测试
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.ytf__tab__map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//       if (activity !=null){
//           mMapView!!.onResume()
//       }
        // UI相关
        //var radioButtonListener: RadioGroup.OnCheckedChangeListener
        var requestLocButton: Button
        andm_y =rl_marker.height.toFloat()
        setMarkerInfo()
        //activity!!.toast("$andm_y")
        /**下面是点击打开另外界面*/
        loc_dw.setOnClickListener() {
            val intent = Intent(activity, SdkDemo::class.java)
            startActivity(intent)
        }

        requestLocButton =view.findViewById(R.id.button1)
      // mSensorManager = activity!!.getSystemService(Context.SENSOR_SERVICE) as SensorManager// 获取传感器管理服务

        mCurrentMode = LocationMode.NORMAL
        requestLocButton.text = "普通"

        val btnClickListener = View.OnClickListener {
            when (mCurrentMode) {
                MyLocationConfiguration.LocationMode.NORMAL -> {
                    requestLocButton.text = "跟随"
                    mCurrentMode = LocationMode.FOLLOWING
                    mBaiduMap!!.setMyLocationConfiguration(
                            MyLocationConfiguration(mCurrentMode, true, mCurrentMarker))
                    val builder = MapStatus.Builder()
                    builder.overlook(0f)
                    mBaiduMap!!.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()))
                }
                MyLocationConfiguration.LocationMode.COMPASS -> {
                    requestLocButton.text = "普通"
                    mCurrentMode = LocationMode.NORMAL
                    mBaiduMap!!.setMyLocationConfiguration(
                            MyLocationConfiguration(mCurrentMode, true, mCurrentMarker))
                    val builder1 = MapStatus.Builder()
                    builder1.overlook(0f)
                    mBaiduMap!!.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder1.build()))
                }
                MyLocationConfiguration.LocationMode.FOLLOWING -> {
                    requestLocButton.text = "罗盘"
                    mCurrentMode = LocationMode.COMPASS
                    mBaiduMap!!.setMyLocationConfiguration(
                            MyLocationConfiguration(mCurrentMode, true, mCurrentMarker))
                }
                else -> {
                }
            }
        }

        requestLocButton.setOnClickListener(btnClickListener)
//
//        val group = view.findViewById<RadioGroup>(R.id.radioGroup)
//        radioButtonListener = RadioGroup.OnCheckedChangeListener { group, checkedId ->
//            if (checkedId == R.id.defaulticon) {
//                // 传入null则，恢复默认图标
//                mCurrentMarker = null
//                mBaiduMap!!.setMyLocationConfiguration(MyLocationConfiguration(mCurrentMode, true, null))
//            }
//            if (checkedId == R.id.customicon) {
//                // 修改为自定义marker
//                mCurrentMarker =BitmapDescriptorFactory.fromResource(R.mipmap.ytf_map_marker_48)
//                mBaiduMap!!.setMyLocationConfiguration(MyLocationConfiguration(mCurrentMode, true, mCurrentMarker))
////                mBaiduMap!!.setMyLocationConfiguration(MyLocationConfiguration(mCurrentMode, true, mCurrentMarker,
////                        accuracyCircleFillColor, accuracyCircleStrokeColor))
//            }
//        }
//        group.setOnCheckedChangeListener(radioButtonListener)

        // -----------------地图初始化---------------------------
        mMapView =view.findViewById(R.id.bmapView)
        mBaiduMap = mMapView!!.getMap()
        // 开启定位图层
        mBaiduMap?.setMyLocationEnabled(true)
        //地图上比例尺
        mMapView?.showScaleControl(false);
        // 隐藏缩放控件
        //mMapView?.showZoomControls(false);
        //关闭手势滑动切换地图视角
        var ytf_map_seting:UiSettings= mBaiduMap!!.uiSettings
        ytf_map_seting.isOverlookingGesturesEnabled=false
        //屏蔽旋转
        ytf_map_seting.isRotateGesturesEnabled=false

        // 隐藏logo
        var child = mMapView?.getChildAt(1);
        if (child != null && (child is ImageView || child is ZoomControls)){
             child.setVisibility(View.INVISIBLE);
        }
        //地图监听事件
        mBaiduMap!!.setOnMapClickListener(object : BaiduMap.OnMapClickListener {
            /**发布信息的时候有用*/
            override fun onMapClick(latLng: LatLng) {
                //点击地图某个位置获取经纬度latLng.latitude、latLng.longitude
               //activity!!.toast("纬度值:${latLng.latitude.toString()}\n经度值:${latLng.longitude.toString()}")
                andm_y=rl_marker.height.toFloat()
                //activity!!.toast("$andm_y")
                ObjectAnimator.ofFloat(rl_marker, "translationY", -andm_y!!).start()

            }
            override fun onMapPoiClick(mapPoi: MapPoi): Boolean {
                //点击地图上的poi图标获取描述信息：mapPoi.name，经纬度：mapPoi.position
                activity!!. alert(mapPoi.name+"\n"+mapPoi.position.toString(),"位置信息").show()
                return false
            }

        })
//-----获取百度地图在手机屏幕中心点的坐标,此事件很重要--------------
        mBaiduMap!!.setOnMapStatusChangeListener(object : BaiduMap.OnMapStatusChangeListener {
            override fun onMapStatusChangeStart(mapStatus: MapStatus) {}
            override fun onMapStatusChangeStart(mapStatus: MapStatus, i: Int) {}
            override fun onMapStatusChange(mapStatus: MapStatus) {}
            override fun onMapStatusChangeFinish(mapStatus: MapStatus) {
         //----------------------------
                //var latLng_1 = mapStatus.target
                //activity!!.toast("中心坐标：$latLng_1")
                 //addMyOverlay(latLng.longitude,latLng.latitude);

     //---------------获取手机屏幕左上和右下角的经纬度方法------
                updateMapState(mapStatus)
               // var latLng_2=LatLng(34.435770, 112.444898)
//                if (DistanceUtil.getDistance(latLng_1,latLng_2)>5000){
//                    mBaiduMap!!.clear()
//                    //activity!!.toast("${DistanceUtil.getDistance(latLng_1,latLng_2)}米,标记已被清除")
//                }
            }


            fun updateMapState(status: MapStatus) {
                val mCenterLatLng = status.target
            //手机屏幕中心点坐标
                val lat = mCenterLatLng.latitude
                val lng = mCenterLatLng.longitude
                //activity!!.toast("$lat\n$lng")
                val dm = resources.displayMetrics //获取手机的高和宽
                var height = dm.heightPixels
                var width = dm.widthPixels

                val pt = Point()
                pt.x = 0
                pt.y = 0
                val ll = mBaiduMap!!.getProjection().fromScreenLocation(pt)
               // activity!!.toast("左上角经纬度${ll.latitude.toString()}\n${ll.longitude}")

                val ptr = Point()
                ptr.x = width
                ptr.y = height
                val llr = mBaiduMap!!.getProjection().fromScreenLocation(ptr)
               // activity!!.toast("右下角经纬度${llr.latitude.toString()} \n${llr.longitude}")

                val bitmap = BitmapDescriptorFactory.fromResource(R.mipmap.ytf_map_marker_97)
                var latLng: LatLng? = null
                var options: OverlayOptions?=null
                for (info in infos) {
                    //以下判断marker是否在屏幕中并且在10公里内,如果在的话显示，否则删除
                    if (info.latitude in llr.latitude.. ll.latitude && info.longitude in ll.longitude..llr.longitude && DistanceUtil.getDistance(mCenterLatLng,LatLng(info.latitude, info.longitude))<10000){
                        latLng = LatLng(info.latitude, info.longitude)
                        //设置marker
                        options = MarkerOptions()
                                .position(latLng)//设置位置
                                .icon(bitmap)//设置图标样式
                                .zIndex(9) // 设置marker所在层级
                                .draggable(true) // 设置手势拖拽;
                        //添加marker
                        marker = mBaiduMap?.addOverlay(options) as Marker

                        //使用marker携带info信息，当点击事件的时候可以通过marker获得info信息
                        var bundle = Bundle()
                        //info必须实现序列化接口
                        bundle.putSerializable("info", info)
                        //val infoUtil = bundle.getSerializable("info") as MarkerInfoUtil
                        marker!!.extraInfo = bundle

          //--------------------------在地图上添加该文字对象并显示
                        var textOption:OverlayOptions  = TextOptions()
                                .bgColor(0xff68798B.toInt())
                                .fontSize(35)
                                .fontColor(0xFFFFffFF.toInt())
                                .text(info.name)
                                //  .rotate(64f)
                                .position(latLng)
                        mBaiduMap?.addOverlay(textOption)

                    }else{
                       // marker.remove()
                       // mBaiduMap.getMarkersInBounds(info.latitude,info.longitude)

                    }
                }

//                val shared = getSharedPreferences("point", 0)
//                val editor = shared.edit()
//                editor.putString("leftPointx", String.valueOf(ll.latitude))
//                editor.putString("leftPointy", String.valueOf(ll.longitude))
//                editor.putString("rightPointx", String.valueOf(llr.latitude))
//                editor.putString("rightPointy", String.valueOf(llr.longitude))
//                editor.commit()
            }
        })
        mBaiduMap?.setOnMarkerClickListener(){
            //从marker中获取info信息
            var bundle = it.extraInfo
            //activity!!.toast("${bundle}")
            val infoUtil = bundle.getSerializable("info") as MarkerInfoUtil

            //将信息显示在界面上
            //activity!!.toast("${infoUtil.name}")
            val iv_img2 = rl_marker.findViewById(R.id.iv_img) as ImageView
            // iv_img2.setBackgroundResource(infoUtil.imgId)

            //得到资源文件的BitMap
            val image = BitmapFactory.decodeResource(getResources(), infoUtil.imgId)
            //创建RoundedBitmapDrawable对象
            val roundImg = RoundedBitmapDrawableFactory.create(getResources(), image)
            //抗锯齿
            roundImg.setAntiAlias(true)
            //设置圆角半径
            roundImg.setCornerRadius(50f)
            //设置显示图片
            iv_img2!!.setImageDrawable(roundImg)

            val tv_name = rl_marker.findViewById(R.id.tv_name) as TextView
            tv_name.text = infoUtil.name
            val tv_description = rl_marker.findViewById(R.id.tv_description) as TextView
            tv_description.text = infoUtil.description
            tv_haoma.text=infoUtil.phone
            //将布局显示出来
            rl_marker.visibility = View.VISIBLE
            andm_y= rl_marker.height.toFloat()
            //activity!!.toast("$andm_y")
            ObjectAnimator.ofFloat(rl_marker, "translationY",andm_y!!).start()

            return@setOnMarkerClickListener true
        }



//                //---------------infowindow的布局-------------
////                    val tv = TextView(this.activity)
////                    tv.setBackgroundResource(R.mipmap.ytf_overlay_bg)
////                    tv.setPadding(20, 10, 20, 20)
////                    tv.setTextColor(android.graphics.Color.WHITE)
////                    tv.text = infoUtil.name
////                    tv.textSize=10f
////                    tv.gravity = Gravity.CENTER
////                    bitmapDescriptor = BitmapDescriptorFactory.fromView(tv)
////                    //infowindow位置
////                    val latLng = LatLng(infoUtil.latitude, infoUtil.longitude)
////                    //infowindow点击事件
////                    val listener = OnInfoWindowClickListener {
////                        //隐藏infowindow
////                        mBaiduMap!!.hideInfoWindow()
////                    }
////                    //显示infowindow
////                    val infoWindow = InfoWindow(bitmapDescriptor, latLng, 68, listener)
////                    mBaiduMap!!.showInfoWindow(infoWindow)

        val tvinput=view.findViewById(R.id.tv_input) as TextView
        tvinput.setOnClickListener(){
            activity!!.toast("进入详细内容")
        }

        // 定位初始化
        mLocClient = LocationClient(this.activity)
       mLocClient!!.registerLocationListener(myListener)
        val option = LocationClientOption()
        option.isOpenGps = true // 打开gps
        option.setCoorType("bd09ll") // 设置坐标类型
        option.setScanSpan(1000)
        option.setNeedDeviceDirect(true)
        mLocClient!!.setLocOption(option)
        mLocClient!!.start()

//        fun onSensorChanged(sensorEvent: SensorEvent) {
//            //每次方向改变，重新给地图设置定位数据，用上一次onReceiveLocation得到的经纬度、精度
//            val x = sensorEvent.values[SensorManager.AXIS_MINUS_X].toDouble()
//            if (Math.abs(x - lastX!!) > 1.0) {// 方向改变大于1度才设置，以免地图上的箭头转动过于频繁
//                mCurrentDirection = x.toInt()
//                locData = MyLocationData.Builder().accuracy(mCurrentAccracy)
//                        // 此处设置开发者获取到的方向信息，顺时针0-360
//                        .direction(mCurrentDirection.toFloat()).latitude(mCurrentLat).longitude(mCurrentLon).build()
//                mBaiduMap!!.setMyLocationData(locData)
//            }
//            lastX = x
//        }
//        fun onAccuracyChanged(sensor: Sensor, i: Int) {
//
//        }
    }

    /**
     * 定位SDK监听函数
     */
    class MyLocationListenner : BDLocationListener {

        override fun onReceiveLocation(location: BDLocation?) {
            // map view 销毁后不在处理新接收的位置
            if (location == null || mMapView == null) {
                return
            }
            mCurrentLat = location.latitude
            mCurrentLon = location.longitude
            mCurrentAccracy = location.radius
            locData = MyLocationData.Builder().accuracy(location.radius)
                    // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(mCurrentDirection.toFloat()).latitude(location.latitude).longitude(location.longitude).build()
            mBaiduMap!!.setMyLocationData(locData)
            if (isFirstLoc) {
                isFirstLoc = false
                val str = location.latitude.toString() + location.longitude.toString()
                  //toast("str")

                val ll = LatLng(location.latitude, location.longitude)
                val builder = MapStatus.Builder()
                builder.target(ll).zoom(15.0f)
                mBaiduMap!!.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()))
            }
        }

    }

    override fun onAttach(context: Context?) {//onAttach()回调将在Fragment与其Activity关联之后调用
       /* * 获取方向传感器
        * 通过SensorManager对象获取相应的Sensor类型的对象
        */
      //var  sensor:Sensor  = mSensorManager!!.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        //应用在前台时候注册监听器
        // 为系统的方向传感器注册监听器
       // mSensorManager.registerListener(this, mSensorManager!!.getDefaultSensor(Sensor.TYPE_ORIENTATION),SensorManager.SENSOR_DELAY_UI)
      //mSensorManager.registerListener(this.myListener, sensor, SensorManager.SENSOR_DELAY_GAME)
        super.onAttach(context)
    }

    override fun onDetach() {//Fragme生命周期最后回调函数，调用后，Fragment不再与Activity绑定，释放资源。
        // 取消注册传感器监听
       //mSensorManager.unregisterListener(myListener) //.unregisterListener(this.myListener)
        super.onDetach()
    }

    override fun onDestroy() {//不再使用Fragment时调用。（备注：Fragment仍然附加到Activity并任然可以找到，但是不能执行其他操作）
        // 退出时销毁定位
      mLocClient!!.unRegisterLocationListener(myListener)
        mLocClient!!.stop()
        // 关闭定位图层
        mBaiduMap!!.setMyLocationEnabled(false)
        mMapView!!.onDestroy()
        mMapView = null

        super.onDestroy()
    }

    //标注的序列
    private fun setMarkerInfo() {
        infos = ArrayList<MarkerInfoUtil>()
        infos.add(MarkerInfoUtil(1,34.437770, 112.444898, "天津站", "18337911223",R.drawable.tianjinzhan, "天津站，俗称天津东站，隶属北京铁路局管辖"))
        infos.add(MarkerInfoUtil(2,34.434420, 112.442519, "南开大学","13903888101", R.drawable.nankai, "正式成立于1919年，是由严修、张伯苓秉承教育救国理念创办的综合性大学。"))
        infos.add(MarkerInfoUtil(3,34.433983, 112.451069, "天津水上公园","15896501333" ,R.drawable.afx, "天津水上公园原称青龙潭，1951年7月1日正式对游客开放，有北方的小西子之称。"))
    }
}


/**
 * 地图标注信息实体类
 * @author jing__jie
 */
class MarkerInfoUtil : Serializable {

    //getter setter
    var Id:Int?=null //id
    var latitude: Double = 0.toDouble()//纬度
    var longitude: Double = 0.toDouble()//经度
    var name: String? = ""//名字
    var phone:String?=""//电话号码
    var imgId: Int = 0//图片
    var description: String? = ""//描述

    //构造方法
    constructor() {}

    constructor(id:Int,latitude: Double, longitude: Double, name: String,phone:String, imgId: Int, description: String) : super() {
        this.Id=id
        this.latitude = latitude
        this.longitude = longitude
        this.name = name
        this.phone=phone
        this.imgId = imgId
        this.description = description
    }

    //toString方法
    override fun toString(): String {
        return ("MarkerInfoUtil [id="+Id+",latitude=" + latitude + ", longitude=" + longitude + ", name=" + name +",phone="+phone+ ", imgId="
                + imgId + ", description=" + description + "]")
    }

    companion object {
        private const val serialVersionUID = 8633299996744734593L
    }
}



//val defaultLocationClientOption: LocationClientOption
//    get() {
//        if (mOption == null) {
//            mOption = LocationClientOption() //新建一个定位选项（设置）
//            /**
//             * 默认高精度，设置定位模式
//             * LocationMode.Hight_Accuracy 高精度定位模式：这种定位模式下，会同时使用网络定位和GPS定位，优先返回最高精度的定位结果
//             * LocationMode.Battery_Saving 低功耗定位模式：这种定位模式下，不会使用GPS，只会使用网络定位（Wi-Fi和基站定位）
//             * LocationMode.Device_Sensors 仅用设备定位模式：这种定位模式下，不需要连接网络，只使用GPS进行定位，这种模式下不支持室内环境的定位
//             */
//            mOption!!.locationMode = LocationClientOption.LocationMode.Hight_Accuracy
//            /**
//             * 默认gcj02，设置返回的定位结果坐标系，如果配合百度地图使用，建议设置为bd09ll;
//             * 目前国内主要有以下三种坐标系：
//             * 1. wgs84：目前广泛使用的GPS全球卫星定位系统使用的标准坐标系；
//             * 2. gcj02：经过国测局加密的坐标；
//             * 3. bd09：为百度坐标系，其中bd09ll表示百度经纬度坐标，bd09mc表示百度墨卡托米制坐标；
//             * 海外地区定位结果默认、且只能是wgs84类型坐标
//             */
//            mOption!!.setCoorType("bd09ll")
//            /**
//             * 默认0，即仅定位一次；设置间隔需大于等于1000ms，表示周期性定位
//             * 如果不在AndroidManifest.xml声明百度指定的Service，周期性请求无法正常工作
//             * 这里需要注意的是：如果是室外gps定位，不用访问服务器，设置的间隔是1秒，那么就是1秒返回一次位置
//             * 如果是WiFi基站定位，需要访问服务器，这个时候每次网络请求时间差异很大，设置的间隔是3秒，只能大概保证3秒左右会返回就一次位置，有时某次定位可能会5秒返回
//             */
//            mOption!!.setScanSpan(0) //
//            /**
//             * 默认false，设置是否需要地址信息
//             * 返回省市区等地址信息，这个api用处很大，很多新闻类api会根据定位返回的市区信息推送用户所在市的新闻
//             */
//            mOption!!.setIsNeedAddress(true)
//            /**
//             * 默认是true，设置是否使用gps定位
//             * 如果设置为false，即使mOption.setLocationMode(LocationMode.Hight_Accuracy)也不会gps定位
//             */
//            mOption!!.isOpenGps = true
//            /**
//             * 默认false，设置是否需要位置语义化结果
//             * 可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
//             *///
//            mOption!!.setIsNeedLocationDescribe(true)
//            /**
//             * 默认false,设置是否需要设备方向传感器的方向结果
//             * 一般在室外gps定位，时返回的位置信息是带有方向的，但是有时候gps返回的位置也不带方向，这个时候可以获取设备方向传感器的方向
//             * wifi基站定位的位置信息是不带方向的，如果需要可以获取设备方向传感器的方向
//             */
//            mOption!!.setNeedDeviceDirect(false)
//            /**
//             * 默认false，设置是否当gps有效时按照1S1次频率输出GPS结果
//             * 室外gps有效时，周期性1秒返回一次位置信息，其实就是设置了
//             * locationManager.requestLocationUpdates中的minTime参数为1000ms，1秒回调一个gps位置
//             */
//            mOption!!.isLocationNotify = false
//            /**
//             * 默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死
//             * 如果你已经拿到了你要的位置信息，不需要再定位了，不杀死留着干嘛
//             */
//            mOption!!.setIgnoreKillProcess(true)
//            /**
//             * 默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
//             * POI就是获取到的位置附近的一些商场、饭店、银行等信息
//             */
//            mOption!!.setIsNeedLocationPoiList(true)
//            /**
//             * 默认false，设置是否收集CRASH信息，默认收集
//             */
//            mOption!!.SetIgnoreCacheException(false)
//        }
//        return mOption!!
//    }