package com.example.cxhink.application

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.Message

import com.baidu.location.BDLocation
import com.baidu.location.BDLocationListener
import com.baidu.location.LocationClient
import com.baidu.location.LocationClientOption
import com.baidu.mapapi.SDKInitializer

/**
 * 用于定位的工具类
 * Created by zhuguohui on 2016/7/22.
 */
object LocationActivity {

    // 定位相关
    internal var mLocClient: LocationClient? = null
    private var myListener= MyLocationListener()
     var sLocation: BDLocation? = null
     val MSG_CHECK_TIMEOUT = 1
     var haveInited = false
    var test = false
    private val mHandler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                1 -> {
                    if (sLocation == null) {
                        if (mLocationListener != null) {
                            mLocationListener!!.onGetLocationTimeOut()
                        }
                    }
                    if (mLocClient!!.isStarted) {
                        mLocClient!!.stop()
                    }
                }
            }
        }
    }

    private var mLocationListener: LocationListener? = null
    private var sNeedAutoClose = true


    fun init(context: Context) {
        //百度地图初始化
        SDKInitializer.initialize(context.applicationContext)

        // 定位初始化
        mLocClient = LocationClient(context.applicationContext)
        mLocClient!!.registerLocationListener(myListener)
        val option = LocationClientOption()
        option.isOpenGps = true // 打开gps
        option.setCoorType("bd09ll") // 设置坐标类型
        option.setScanSpan(1000)
        mLocClient!!.locOption = option
        haveInited = true
    }


    /**
     * 定位SDK监听函数
     */
    private class MyLocationListener : BDLocationListener {

        override fun onReceiveLocation(location: BDLocation?) {
            // map view 销毁后不在处理新接收的位置
            if (location == null) {
                return
            }
            if (!test) {
                sLocation = location
                if (mLocationListener != null) {
                    mLocationListener!!.onReceiveLocation(sLocation!!)
                }
                if (sNeedAutoClose) {
                    if (mLocClient!!.isStarted) {
                        mLocClient!!.stop()
                    }
                }
            }


        }

    }

    interface LocationListener {

        fun onGetLocationStart()

        fun onReceiveLocation(location: BDLocation)

        fun onGetLocationTimeOut()
    }

    class LocationListenrAdatper : LocationListener {

        override fun onGetLocationStart() {

        }

        override fun onReceiveLocation(location: BDLocation) {

        }

        override fun onGetLocationTimeOut() {

        }
    }

    /**
     * 获取定位
     *
     * @param listener    回调
     * @param timeOut     超时时间:单位毫秒，-1表示不限时间。
     * @param forceUpdate 强制刷新
     */
    fun getLocation(listener: LocationListener?, timeOut: Long, forceUpdate: Boolean, autoClose: Boolean) {
        if (!haveInited) {
            throw RuntimeException("请先使用init()方法进行初始化")
        }

        if (forceUpdate || sLocation == null) {
            if (mLocationListener != null) {
                mLocationListener!!.onGetLocationStart()
            }
        }
        //不要求强制刷新的时候，使用已有的定位
        if (!forceUpdate && sLocation != null) {
            listener?.onReceiveLocation(sLocation!!)
        }

        //开始定位
        sNeedAutoClose = autoClose
        sLocation = null
        mLocationListener = listener
        mLocClient!!.start()

        if (timeOut > -1) {
            mHandler.sendEmptyMessageDelayed(MSG_CHECK_TIMEOUT, timeOut)
        }

    }

    /**
     * 获取一次定位
     * @param listener
     */
    fun getLocation(listener: LocationListener, forceUpdate: Boolean) {
        getLocation(listener, -1, forceUpdate, true)
    }


    fun stopLoacation() {
        if (mLocClient != null && mLocClient!!.isStarted) {
            mLocClient!!.stop()
        }
    }
}
