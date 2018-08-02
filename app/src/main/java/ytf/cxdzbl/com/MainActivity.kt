package ytf.cxdzbl.com

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.widget.ImageButton
import com.baidu.mapapi.SDKInitializer
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.toast


//import org.jetbrains.anko.alert

class MainActivity : AppCompatActivity() {
    var item_TAB_off :MutableList<Int> =mutableListOf(0,1,0)// YTF_main_class().item_TAB_off//   //初始化导航栏状态
    private var Ytf_tab_info=Ytf_Tab_Info()
    private var Ytf_tab_map=Ytf_Tab_Map()
    private var Ytf_tab_me=Ytf_Tab_Me()
    var currentFragment:Fragment?=null

    private var mReceiver: SDKReceiver? = null
    private val REQUEST_CODE_ACCESS_COARSE_LOCATION: Int = 0

    /**
     * 构造广播监听类，监听 SDK key 验证以及网络异常广播
     */
    inner class SDKReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            val s = intent.action
            if (s == SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_ERROR) {
                toast("apikey验证失败，地图功能无法正常使用")
            } else if (s == SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_OK) {
                toast("apikey验证成功")
            } else if (s == SDKInitializer.SDK_BROADCAST_ACTION_STRING_NETWORK_ERROR) {
                toast("\"网络错误\"")
            }
        }
    }
    /*
    * 设置导航栏的变化
    * */
    fun Tab_image_onof(item:Int){
        when(item){
            0-> {
                imageBut_a.setBackgroundResource(R.mipmap.ytf_message_news)
                imageBut_b.setBackgroundResource(R.mipmap.ytf_location_pin)
                 imageBut_c.setBackgroundResource(R.mipmap.ytf_user_alert)
                item_TAB_off[0]=1
                item_TAB_off[1]=0
                item_TAB_off[2]=0
                 }
            1->{
                imageBut_a.setBackgroundResource(R.mipmap.ytf_info)
                imageBut_b.setBackgroundResource(R.mipmap.ytf_tab_add)
                imageBut_c.setBackgroundResource(R.mipmap.ytf_user_alert)
                item_TAB_off[0]=0
                item_TAB_off[1]=1
                item_TAB_off[2]=0
            }
            2->{
                imageBut_a.setBackgroundResource(R.mipmap.ytf_info)
                imageBut_b.setBackgroundResource(R.mipmap.ytf_location_pin)
                imageBut_c.setBackgroundResource(R.mipmap.ytf_user_info)
                item_TAB_off[0] = 0
                item_TAB_off[1] = 0
                item_TAB_off[2] = 1
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SDKInitializer.initialize(applicationContext)
        setContentView(R.layout.activity_main)
        Ytf_jianbian.setBackgroundResource(R.mipmap.ytf_jianbian)
        // apikey的授权需要一定的时间，在授权成功之前地图相关操作会出现异常；apikey授权成功后会发送广播通知，我们这里注册 SDK 广播监听者
        val iFilter = IntentFilter()
        iFilter.addAction(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_OK)
        iFilter.addAction(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_ERROR)
        iFilter.addAction(SDKInitializer.SDK_BROADCAST_ACTION_STRING_NETWORK_ERROR)
        mReceiver = SDKReceiver()
        registerReceiver(mReceiver, iFilter)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //如果 API level 是大于等于 23(Android 6.0) 时
            //判断是否具有权限
            toast("判断是否具有权限")
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {}
                //判断是否需要向用户解释为什么需要申请该权限
                toast("是否向用户解释为什么需要该权限")
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                    //Toast.makeText(this@MainActivity, "自Android 6.0开始需要打开位置权限", Toast.LENGTH_SHORT).show()
                    toast("自Android 6.0开始需要打开位置权限")
                }
                //请求权限
                toast("请求权限")
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), REQUEST_CODE_ACCESS_COARSE_LOCATION)
              }
        }


        supportFragmentManager.beginTransaction().add(R.id.Ytf_FramLayout,Ytf_tab_map,"tab_map").commitAllowingStateLoss()
        var but_a:ImageButton = imageBut_a
        var but_b = imageBut_b
        var but_c = imageBut_c
       // var but_exit=imgbutleft
        currentFragment=Ytf_tab_map
        but_a.setOnClickListener() {
           if(item_TAB_off[0]==0){
           // it.setBackgroundResource(R.mipmap.ytf_location_pin)
               //message.setText("消  息")
               replaceFragment("tab_info")
            Tab_image_onof(0)
            }
        }
        but_b.setOnClickListener() {
                if(item_TAB_off[1]==0){
                    //message.setText("附  近")
                    replaceFragment("tab_map")
                    Tab_image_onof(1)
                }else{
                    toast("发布自己的信息")
                }
        }
        but_c.setOnClickListener() {
            if(item_TAB_off[2]==0) {
                //message.setText("我")
                if(Ytf_tab_me!=null) {
                    replaceFragment("tab_me")
                    Tab_image_onof(2)
                    }
                }
            }
    }

    fun replaceFragment(tag:String) {
        if (currentFragment != null) {
            supportFragmentManager.beginTransaction().hide(currentFragment!!).commitAllowingStateLoss()
        }
        currentFragment = supportFragmentManager.findFragmentByTag(tag);
        if (currentFragment == null) {
            when (tag) {
                "tab_info" ->{
                    currentFragment = Ytf_tab_info
                    // break
                }
                "tab_map" ->{
                    currentFragment = Ytf_tab_map
                }
            //break
                "tab_me" ->{
                    currentFragment = Ytf_tab_me
                }

            // break
            }
            supportFragmentManager.beginTransaction().add(R.id.Ytf_FramLayout, currentFragment!!,tag).commitAllowingStateLoss()
        }else {
            supportFragmentManager.beginTransaction().show(currentFragment!!).commitAllowingStateLoss()
        }
    }
}



/**
 * 获取图片名称获取图片的资源id的方法
 * @param imageName
 * @return
*/
//    fun getResource(imageName: String): Int {
//        val ctx = baseContext
//        return resources.getIdentifier(imageName, "mipmap", ctx.packageName)
//    }

//    companion object {
//        fun startHomeActivity(context: Context) {
//            val intent = Intent()
//            intent.setClass(context, HomeActivity::class.java!!)
//            context.startActivity(intent)
//        }
//    }
//-----------------------------------------------------------------
//        val dm = resources.displayMetrics //获取手机的高和宽
//        var heigth = dm.heightPixels
//        var width = dm.widthPixels
        //println("高：$heigth\n宽：$width")
        //message.setText("高：" + heigth.toString() + "宽：" + width.toString())