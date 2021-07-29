package com.rn.devicesinfo


import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.ActivityManager.RunningAppProcessInfo
import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.Signature
import android.graphics.drawable.Drawable
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.wifi.WifiManager
import android.os.Build
import android.os.SystemClock
import android.telephony.TelephonyManager
import android.telephony.gsm.GsmCellLocation
import android.text.TextUtils
import android.text.format.Formatter
import android.util.Log
import androidx.annotation.RequiresPermission
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.net.*
import java.util.*


object SystemTools {

    /**
     * no network
     */
    const val NETWORK_NO = -1

    /**
     * wifi network
     */
    const val NETWORK_WIFI = 1

    /**
     * "2G" networks
     */
    const val NETWORK_2G = 2

    /**
     * "3G" networks
     */
    const val NETWORK_3G = 3

    /**
     * "4G" networks
     */
    const val NETWORK_4G = 4

    /**
     * unknown network
     */
    const val NETWORK_UNKNOWN = 5
    private const val NETWORK_TYPE_GSM = 16
    private const val NETWORK_TYPE_TD_SCDMA = 17
    private const val NETWORK_TYPE_IWLAN = 18

    /**
     * 需添加权限
     *
     * @param context 上下文
     * @return 网络类型
     * @code <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"></uses-permission>
     *
     *
     * 它主要负责的是
     * 1 监视网络连接状态 包括（Wi-Fi, 2G, 3G, 4G）
     * 2 当网络状态改变时发送广播通知
     * 3 网络连接失败尝试连接其他网络
     * 4 提供API，允许应用程序获取可用的网络状态
     *
     *
     * netTyped 的结果
     * @link #NETWORK_NO      = -1; 当前无网络连接
     * @link #NETWORK_WIFI    =  1; wifi的情况下
     * @link #NETWORK_2G      =  2; 切换到2G环境下
     * @link #NETWORK_3G      =  3; 切换到3G环境下
     * @link #NETWORK_4G      =  4; 切换到4G环境下
     * @link #NETWORK_UNKNOWN =  5; 未知网络
     */
    @SuppressLint("MissingPermission")
    @JvmStatic
    fun getNetWorkType(context: Context): String {
        // 获取ConnectivityManager
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val ni = cm.activeNetworkInfo // 获取当前网络状态
        var netType = ""
        if (ni != null && ni.isConnectedOrConnecting) {
            when (ni.type) {
                ConnectivityManager.TYPE_WIFI -> {
                    netType = "NETWORK_WIFI"
                    // RxToast.success("切换到wifi环境下")
                }
                ConnectivityManager.TYPE_MOBILE -> when (ni.subtype) {
                    NETWORK_TYPE_GSM, TelephonyManager.NETWORK_TYPE_GPRS, TelephonyManager.NETWORK_TYPE_CDMA, TelephonyManager.NETWORK_TYPE_EDGE, TelephonyManager.NETWORK_TYPE_1xRTT, TelephonyManager.NETWORK_TYPE_IDEN -> {
                        netType = "NETWORK_2G"
                        //     RxToast.info("切换到2G环境下")
                    }
                    TelephonyManager.NETWORK_TYPE_EVDO_A, TelephonyManager.NETWORK_TYPE_UMTS, TelephonyManager.NETWORK_TYPE_EVDO_0, TelephonyManager.NETWORK_TYPE_HSDPA, TelephonyManager.NETWORK_TYPE_HSUPA, TelephonyManager.NETWORK_TYPE_HSPA, TelephonyManager.NETWORK_TYPE_EVDO_B, TelephonyManager.NETWORK_TYPE_EHRPD, TelephonyManager.NETWORK_TYPE_HSPAP, NETWORK_TYPE_TD_SCDMA -> {
                        netType = "NETWORK_3G"
                        //    RxToast.info("切换到3G环境下")
                    }
                    TelephonyManager.NETWORK_TYPE_LTE, NETWORK_TYPE_IWLAN -> {
                        netType = "NETWORK_4G"
                        //     RxToast.info("切换到4G环境下")
                    }
                    TelephonyManager.NETWORK_TYPE_NR -> {
                        netType = "NETWORK_5G"
                        //     RxToast.info("切换到4G环境下")
                    }
                    else -> {
                        val subtypeName = ni.subtypeName
                        netType = if (subtypeName.equals("TD-SCDMA", ignoreCase = true)
                                || subtypeName.equals("WCDMA", ignoreCase = true)
                                || subtypeName.equals("CDMA2000", ignoreCase = true)
                        ) {
                            "NETWORK_3G"
                        } else {
                            "NETWORK_UNKNOWN"
                        }
                        //   RxToast.normal("未知网络")
                    }
                }
                else -> {
                    netType = "NETWORK_UNKNOWN"
                    //   RxToast.normal("未知网络")
                }
            }
        } else {
            netType = "NETWORK_NO"
            //    RxToast.error(context, "当前无网络连接")?.show()
        }
        return netType
    }


    @RequiresPermission("android.permission.INTERNET")
    @JvmStatic
    fun getIPAddress(useIPv4: Boolean): String? {
        try {
            val nis =
                    NetworkInterface.getNetworkInterfaces()
            val adds = LinkedList<Any?>()
            label64@ while (true) {
                var ni: NetworkInterface
                do {
                    do {
                        if (!nis.hasMoreElements()) {
                            val var9: Iterator<*> = adds.iterator()
                            while (var9.hasNext()) {
                                val add = var9.next() as InetAddress
                                if (!add.isLoopbackAddress) {
                                    val hostAddress = add.hostAddress
                                    val isIPv4 =
                                            hostAddress.indexOf(58.toChar()) < 0
                                    if (useIPv4) {
                                        if (isIPv4) {
                                            return hostAddress
                                        }
                                    } else if (!isIPv4) {
                                        val index = hostAddress.indexOf(37.toChar())
                                        return if (index < 0) hostAddress.toUpperCase() else hostAddress.substring(
                                                0,
                                                index
                                        ).toUpperCase()
                                    }
                                }
                            }
                            break@label64
                        }
                        ni = nis.nextElement() as NetworkInterface
                    } while (!ni.isUp)
                } while (ni.isLoopback)
                val addresses: Enumeration<*> = ni.inetAddresses
                while (addresses.hasMoreElements()) {
                    adds.addFirst(addresses.nextElement())
                }
            }
        } catch (var8: SocketException) {
            var8.printStackTrace()
        }
        return ""
    }

    @RequiresPermission("android.permission.ACCESS_WIFI_STATE")
    @JvmStatic
    fun getWifiInfo(context: Context,type:String): String? {
        val wm = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
        return if (wm == null) {
            ""
        } else {
            return if (type.equals("netmask")){
                Formatter.formatIpAddress(wm.dhcpInfo.netmask)
            }else if (type.equals("gateway")){
                Formatter.formatIpAddress(wm.dhcpInfo.gateway)
            }else if (type.equals("ipAddress")){
                Formatter.formatIpAddress(wm.dhcpInfo.ipAddress)
            }else {
                Formatter.formatIpAddress(wm.dhcpInfo.serverAddress)
            }
        }
    }

    @JvmStatic
    fun getBroadcastIpAddress(): String? {
        try {
            val nis =
                    NetworkInterface.getNetworkInterfaces()
            LinkedList<Any?>()
            while (true) {
                var ni: NetworkInterface
                do {
                    do {
                        if (!nis.hasMoreElements()) {
                            return ""
                        }
                        ni = nis.nextElement() as NetworkInterface
                    } while (!ni.isUp)
                } while (ni.isLoopback)
                val ias =
                        ni.interfaceAddresses
                var i = 0
                val size = ias.size
                while (i < size) {
                    val ia = ias[i] as InterfaceAddress
                    val broadcast = ia.broadcast
                    if (broadcast != null) {
                        return broadcast.hostAddress
                    }
                    ++i
                }
            }
        } catch (var8: SocketException) {
            var8.printStackTrace()
            return ""
        }
    }

    @RequiresPermission("android.permission.ACCESS_WIFI_STATE")
    @JvmStatic
    fun getSSID(context: Context): String? {
        val wm = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
        return if (wm == null) {
            ""
        } else {
            val wi = wm.connectionInfo
            if (wi == null) {
                ""
            } else {
                val ssid = wi.ssid
                if (TextUtils.isEmpty(ssid)) {
                    ""
                } else {
                    if (ssid.length > 2 && ssid[0] == '"' && ssid[ssid.length - 1] == '"') ssid.substring(
                            1,
                            ssid.length - 1
                    ) else ssid
                }
            }
        }
    }


    /**
     * 判断网络连接是否可用
     */
    @SuppressLint("MissingPermission")
    @JvmStatic
    fun isNetworkAvailable(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (cm == null) {
        } else {
            //如果仅仅是用来判断网络连接
            //则可以使用 cm.getActiveNetworkInfo().isAvailable();
            val info = cm.allNetworkInfo
            if (info != null) {
                for (i in info.indices) {
                    if (info[i].state == NetworkInfo.State.CONNECTED) {
                        return true
                    }
                }
            }
        }
        return false
    }

    /**
     * 判断网络是否可用
     * 需添加权限
     *
     * @code <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"></uses-permission>
     */
    @JvmStatic
    fun isAvailable(context: Context): Boolean {
        val info = getActiveNetworkInfo(context)
        return info != null && info.isAvailable
    }


    @RequiresPermission("android.permission.INTERNET")
    @JvmStatic
    fun isAvailableByPing(): Boolean {
        return isAvailableByPing("")
    }

    @RequiresPermission("android.permission.INTERNET")
    fun isAvailableByPing(ip: String?): Boolean {
        val realIp = if (TextUtils.isEmpty(ip)) "223.5.5.5" else ip!!
        val result =
                AdbTools.execCmd(
                        String.format(
                                "ping -c 1 %s",
                                realIp
                        ), false
                )
        return result.result == 0
    }


    @RequiresPermission("android.permission.INTERNET")
    @JvmStatic
    fun isAvailableByDns(): Boolean {
        return isAvailableByDns("")
    }

    @RequiresPermission("android.permission.INTERNET")
    fun isAvailableByDns(domain: String?): Boolean {
        val realDomain = if (TextUtils.isEmpty(domain)) "www.baidu.com" else domain!!
        return try {
            val inetAddress = InetAddress.getByName(realDomain)
            inetAddress != null
        } catch (var4: UnknownHostException) {
            var4.printStackTrace()
            false
        }
    }


    @RequiresPermission(allOf = ["android.permission.ACCESS_WIFI_STATE", "android.permission.INTERNET"])
    @JvmStatic
    fun isWifiAvailable(context: Context): Boolean {
        return getWifiEnabled(context) && isAvailable(context)
    }

    @RequiresPermission("android.permission.ACCESS_WIFI_STATE")
    fun getWifiEnabled(context: Context): Boolean {
        val manager =
                context.getSystemService(Context.WIFI_SERVICE) as WifiManager
        return manager?.isWifiEnabled ?: false
    }


    /**
     * 判断wifi是否连接状态
     *
     * 需添加权限 `<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>`
     *
     * @param context 上下文
     * @return `true`: 连接<br></br>`false`: 未连接
     */
    @SuppressLint("MissingPermission")
    @JvmStatic
    fun isWifiConnected(context: Context): Boolean {
        val cm = context
                .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return cm != null && cm.activeNetworkInfo != null && cm.activeNetworkInfo!!.type == ConnectivityManager.TYPE_WIFI
    }



    /**
     * 判断网络是否是4G
     * 需添加权限
     *
     * @code <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"></uses-permission>
     */
    @JvmStatic
    fun is4G(context: Context): Boolean {
        val info = getActiveNetworkInfo(context)
        return info != null && info.isAvailable && info.subtype == TelephonyManager.NETWORK_TYPE_LTE
    }

    @JvmStatic
    fun is5G(context: Context): Boolean {
        val info = getActiveNetworkInfo(context)
        return info != null && info.isAvailable && info.subtype == TelephonyManager.NETWORK_TYPE_NR
    }


    /**
     * 获取活动网络信息
     *
     * @param context 上下文
     * @return NetworkInfo
     */
    @SuppressLint("MissingPermission")
    @JvmStatic
    fun getActiveNetworkInfo(context: Context): NetworkInfo? {
        try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            return cm.activeNetworkInfo!!
        }catch (e:Exception){
            e.printStackTrace()
        }
        return null
    }

    @SuppressLint("MissingPermission")
    @JvmStatic
    fun getMobileDataEnabled(context: Context): Boolean {
        try {
            val tm = context
                    .getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
                    ?: return false
            if (Build.VERSION.SDK_INT >= 26) {
                return tm.isDataEnabled
            }
            val getMobileDataEnabledMethod =
                    tm.javaClass.getDeclaredMethod("getDataEnabled")
            if (null != getMobileDataEnabledMethod) {
                return getMobileDataEnabledMethod.invoke(tm) as Boolean
            }
        } catch (var2: Exception) {
            Log.e("NetworkUtils", "getMobileDataEnabled: ", var2)
        }
        return false
    }
    /**
     * 获取移动网络运营商名称
     *
     * 如中国联通、中国移动、中国电信
     *
     * @param context 上下文
     * @return 移动网络运营商名称
     */
    @JvmStatic
    fun getNetworkOperatorName(context: Context): String? {
        val tm = context
                .getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        return tm.networkOperatorName
    }



    /**
     * 判断App是否是系统应用
     *
     * @param context 上下文
     * @return `true`: 是<br></br>`false`: 否
     */
    @JvmStatic
    fun isSystemApp(context: Context): Boolean {
        return isSystemApp(context, context.packageName)
    }

    /**
     * 判断App是否是系统应用
     *
     * @param context     上下文
     * @param packageName 包名
     * @return `true`: 是<br></br>`false`: 否
     */
    @JvmStatic
    fun isSystemApp(context: Context, packageName: String?): Boolean {
        return if (TextUtils.isEmpty(packageName)) false else try {
            val pm = context.packageManager
            val ai = pm.getApplicationInfo(packageName!!, 0)
            ai.flags and ApplicationInfo.FLAG_SYSTEM != 0
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            false
        }
    }

    /**
     * 判断App是否有root权限
     *
     * @return `true`: 是<br></br>`false`: 否
     */
    @JvmStatic
    fun isAppRoot(): Boolean {
        val su = "su"
        val locations = arrayOf(
            "/system/bin/",
            "/system/xbin/",
            "/sbin/",
            "/system/sd/xbin/",
            "/system/bin/failsafe/",
            "/data/local/xbin/",
            "/data/local/bin/",
            "/data/local/",
            "/system/sbin/",
            "/usr/bin/",
            "/vendor/bin/"
        )
        val var3 = locations.size
        for (var4 in 0 until var3) {
            val location = locations[var4]
            if (File(location + su).exists()) {
                return true
            }
        }
        return false
    }

    /**
     * 获取App包名
     *
     * @param context 上下文
     * @return App包名
     */
    @JvmStatic
    fun getAppPackageName(context: Context): String {
        return context.packageName
    }


    /**
     * 获取App名称
     *
     * @param context 上下文
     * @return App名称
     */
    @JvmStatic
    fun getAppName(context: Context): String? {
        return getAppName(context, context.packageName)
    }

    /**
     * 获取App名称
     *
     * @param context     上下文
     * @param packageName 包名
     * @return App名称
     */
    @JvmStatic
    fun getAppName(context: Context, packageName: String?): String? {
        return if (TextUtils.isEmpty(packageName)) null else try {
            val pm = context.packageManager
            val pi = pm.getPackageInfo(packageName!!, 0)
            pi?.applicationInfo?.loadLabel(pm)?.toString()
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 获取App图标
     *
     * @param context 上下文
     * @return App图标
     */
    @JvmStatic
    fun getAppIcon(context: Context): Drawable? {
        return getAppIcon(context, context.packageName)
    }

    /**
     * 获取App图标
     *
     * @param context     上下文
     * @param packageName 包名
     * @return App图标
     */
    @JvmStatic
    fun getAppIcon(context: Context, packageName: String?): Drawable? {
        return if (TextUtils.isEmpty(packageName)) null else try {
            val pm = context.packageManager
            val pi = pm.getPackageInfo(packageName!!, 0)
            pi?.applicationInfo?.loadIcon(pm)
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 获取App路径
     *
     * @param context 上下文
     * @return App路径
     */
    @JvmStatic
    fun getAppPath(context: Context): String? {
        return getAppPath(context, context.packageName)
    }

    /**
     * 获取App路径
     *
     * @param context     上下文
     * @param packageName 包名
     * @return App路径
     */
    @JvmStatic
    fun getAppPath(context: Context, packageName: String?): String? {
        return if (TextUtils.isEmpty(packageName)) null else try {
            val pm = context.packageManager
            val pi = pm.getPackageInfo(packageName!!, 0)
            pi?.applicationInfo?.sourceDir
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 获取App版本号
     *
     * @param context 上下文
     * @return App版本号
     */
    @JvmStatic
    fun getAppVersionName(context: Context): String? {
        return getAppVersionName(context, context.packageName)
    }

    /**
     * 获取App版本号
     *
     * @param context     上下文
     * @param packageName 包名
     * @return App版本号
     */
    @JvmStatic
    fun getAppVersionName(context: Context, packageName: String?): String? {
        return if (TextUtils.isEmpty(packageName)) null else try {
            val pm = context.packageManager
            val pi = pm.getPackageInfo(packageName!!, 0)
            pi?.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 获取App第一次安装时间
     *
     * @param context 上下文
     * @return App第一次安装时间
     */
    @JvmStatic
    fun getFirstInstallTime(context: Context): String? {
        return getFirstInstallTime(context, context.packageName)
    }

    /**
     * 获取App第一次安装时间
     *
     * @param context     上下文
     * @param packageName 包名
     * @return App第一次安装时间
     */
    @JvmStatic
    fun getFirstInstallTime(context: Context, packageName: String?): String? {
        return if (TextUtils.isEmpty(packageName)) null else try {
            val pm = context.packageManager
            val pi = pm.getPackageInfo(packageName!!, 0)
            pi?.firstInstallTime.toString()
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 获取App最后一次更新时间
     *
     * @param context 上下文
     * @return App最后一次更新时间
     */
    @JvmStatic
    fun getLastUpdateTime(context: Context): String? {
        return getLastUpdateTime(context, context.packageName)
    }

    /**
     * 获取App最后一次更新时间
     *
     * @param context     上下文
     * @param packageName 包名
     * @return App最后一次更新时间
     */
    @JvmStatic
    fun getLastUpdateTime(context: Context, packageName: String?): String? {
        return if (TextUtils.isEmpty(packageName)) null else try {
            val pm = context.packageManager
            val pi = pm.getPackageInfo(packageName!!, 0)
            pi?.lastUpdateTime.toString()
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            null
        }
    }


    /**
     * 获取App Uid
     *
     * @param context 上下文
     * @return App Uid
     */
    @JvmStatic
    fun getAppUid(context: Context): String? {
        return getAppUid(context, context.packageName)
    }

    /**
     * 获取App Uid
     *
     * @param context     上下文
     * @param packageName 包名
     * @return App Uid
     */
    @JvmStatic
    fun getAppUid(context: Context, packageName: String?): String? {
        return if (TextUtils.isEmpty(packageName)) null else try {
            val pm = context.packageManager
            val pi = pm.getApplicationInfo(packageName!!, 0)
            pi?.uid.toString()
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 获取App版本码
     *
     * @param context 上下文
     * @return App版本码
     */
    @JvmStatic
    fun getAppVersionCode(context: Context): Int {
        return getAppVersionCode(context, context.packageName)
    }

    /**
     * 获取App版本码
     *
     * @param context     上下文
     * @param packageName 包名
     * @return App版本码
     */
    @JvmStatic
    fun getAppVersionCode(context: Context, packageName: String?): Int {
        return if (TextUtils.isEmpty(packageName)) -1 else try {
            val pm = context.packageManager
            val pi = pm.getPackageInfo(packageName!!, 0)
            pi?.versionCode ?: -1
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            -1
        }
    }

    /**
     * 判断App是否是Debug版本
     *
     * @param context 上下文
     * @return `true`: 是<br></br>`false`: 否
     */
    @JvmStatic
    fun isAppDebug(context: Context): Boolean {
        return isAppDebug(context, context.packageName)
    }

    /**
     * 判断App是否是Debug版本
     *
     * @param context     上下文
     * @param packageName 包名
     * @return `true`: 是<br></br>`false`: 否
     */
    @JvmStatic
    fun isAppDebug(context: Context, packageName: String?): Boolean {
        return if (TextUtils.isEmpty(packageName)) false else try {
            val pm = context.packageManager
            val ai = pm.getApplicationInfo(packageName!!, 0)
            ai != null && ai.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            false
        }
    }



    /**
     * 获取App签名
     *
     * @param context 上下文
     * @return App签名
     */
    @JvmStatic
    fun getAppSignature(context: Context): Array<Signature>? {
        return getAppSignature(context, context.packageName)
    }

    /**
     * 获取App签名
     *
     * @param context     上下文
     * @param packageName 包名
     * @return App签名
     */
    @JvmStatic
    @SuppressLint("PackageManagerGetSignatures")
    fun getAppSignature(context: Context, packageName: String?): Array<Signature>? {
        return if (TextUtils.isEmpty(packageName)) null else try {
            val pm = context.packageManager
            val pi = pm.getPackageInfo(packageName!!, PackageManager.GET_SIGNATURES)
            pi?.signatures
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 获取应用签名的的SHA1值
     *
     * 可据此判断高德，百度地图key是否正确
     *
     * @param context 上下文
     * @return 应用签名的SHA1字符串, 比如：53:FD:54:DC:19:0F:11:AC:B5:22:9E:F1:1A:68:88:1B:8B:E8:54:42
     */
    @JvmStatic
    fun getAppSignatureSHA1(context: Context,type:String): String? {
        return getAppSignatureSHA1(context, type,context.packageName)
    }

    /**
     * 获取应用签名的的SHA1值
     *
     * 可据此判断高德，百度地图key是否正确
     *
     * @param context     上下文
     * @param packageName 包名
     * @return 应用签名的SHA1字符串, 比如：53:FD:54:DC:19:0F:11:AC:B5:22:9E:F1:1A:68:88:1B:8B:E8:54:42
     */
    @JvmStatic
    fun getAppSignatureSHA1(context: Context,type:String, packageName: String?): String? {
        val signature = getAppSignature(context, packageName) ?: return null
        return DisposeTools.encryptSHA1ToString(type,signature[0].toByteArray())
            .replace("(?<=[0-9A-F]{2})[0-9A-F]{2}".toRegex(), ":$0")
    }

    /**
     * 判断App是否处于前台
     *
     * @param context 上下文
     * @return `true`: 是<br></br>`false`: 否
     */
    @JvmStatic
    fun isAppForeground(context: Context): Boolean {
        val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val infos = manager.runningAppProcesses
        if (infos == null || infos.size == 0) return false
        for (info in infos) {
            if (info.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                return info.processName == context.packageName
            }
        }
        return false
    }


    /**
     * 获取当前App信息
     *
     * AppInfo（名称，图标，包名，版本号，版本Code，是否安装在SD卡，是否是用户程序）
     *
     * @param context 上下文
     * @return 当前应用的AppInfo
     */
    @JvmStatic
    fun getAppInfo(context: Context): AppInfo? {
        val pm = context.packageManager
        var pi: PackageInfo? = null
        try {
            pi = pm.getPackageInfo(context.applicationContext.packageName, 0)
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return pi?.let { getBean(pm, it) }
    }

    /**
     * 得到AppInfo的Bean
     *
     * @param pm 包的管理
     * @param pi 包的信息
     * @return AppInfo类
     */
    @JvmStatic
    private fun getBean(pm: PackageManager, pi: PackageInfo): AppInfo {
        val ai = pi.applicationInfo
        val name = ai.loadLabel(pm).toString()
        val icon = ai.loadIcon(pm)
        val packageName = pi.packageName
        val packagePath = ai.sourceDir
        val versionName = pi.versionName
        val firstInstallTime = pi.firstInstallTime.toString()
        val lastUpdateTime = pi.lastUpdateTime.toString()
        val versionCode = pi.versionCode
        val isSD = ApplicationInfo.FLAG_SYSTEM and ai.flags != ApplicationInfo.FLAG_SYSTEM
        val isUser = ApplicationInfo.FLAG_SYSTEM and ai.flags != ApplicationInfo.FLAG_SYSTEM
        return AppInfo(name, icon, packageName, packagePath, versionName,firstInstallTime,lastUpdateTime, versionCode, isSD, isUser)
    }

    /**
     * 获取所有已安装App信息
     *
     * [.getBean]（名称，图标，包名，包路径，版本号，版本Code，是否安装在SD卡，是否是用户程序）
     *
     * 依赖上面的getBean方法
     *
     * @param context 上下文
     * @return 所有已安装的AppInfo列表
     */
    @JvmStatic
    fun getAllAppsInfo(context: Context): List<AppInfo> {
        val list: MutableList<AppInfo> = ArrayList()
        val pm = context.packageManager
        // 获取系统中安装的所有软件信息
        val installedPackages = pm.getInstalledPackages(0)
        for (pi in installedPackages) {
            if (pi != null) {
                list.add(getBean(pm, pi))
            }
        }
        return list
    }

    /**
     * 判断当前App处于前台还是后台
     *
     * 需添加权限 `<uses-permission android:name="android.permission.GET_TASKS"/>`
     *
     * 并且必须是系统应用该方法才有效
     *
     * @param context 上下文
     * @return `true`: 后台<br></br>`false`: 前台
     */
    @JvmStatic
    fun isAppBackground(context: Context): Boolean {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val tasks = am.getRunningTasks(1)
        if (!tasks.isEmpty()) {
            val topActivity = tasks[0].topActivity
            return topActivity!!.packageName != context.packageName
        }
        return false
    }


    /**
     * 封装App信息的Bean类
     */
    class AppInfo(
        name: String?, icon: Drawable?, packageName: String?, packagePath: String?,
        versionName: String?,firstInstallTime: String?,lastUpdateTime: String?, versionCode: Int, isSD: Boolean, isUser: Boolean
    ) {
        var name: String? = null
        var icon: Drawable? = null
        var packageName: String? = null
        var packagePath: String? = null
        var firstInstallTime: String? = null
        var lastUpdateTime: String? = null
        var versionName: String? = null
        var versionCode = 0
        var isSD = false
        var isUser = false

        //        public String toString() {
        //            return getName() + "\n"
        //                    + getIcon() + "\n"
        //                    + getPackageName() + "\n"
        //                    + getPackagePath() + "\n"
        //                    + getVersionName() + "\n"
        //                    + getVersionCode() + "\n"
        //                    + isSD() + "\n"
        //                    + isUser() + "\n";
        //        }
        /**
         * @param name        名称
         * @param icon        图标
         * @param packageName 包名
         * @param packagePath 包路径
         * @param versionName 版本号
         * @param versionCode 版本Code
         * @param isSD        是否安装在SD卡
         * @param isUser      是否是用户程序
         */
        init {
            this.name = name
            this.icon = icon
            this.packageName = packageName
            this.packagePath = packagePath
            this.versionName = versionName
            this.versionCode = versionCode
            this.isSD = isSD
            this.isUser = isUser
        }
    }

    var ml: ArrayList<Any> = ArrayList<Any>()

    @JvmStatic
    fun getuptime(): Long {
        var uptime = 0L
        try {
            if (Build.VERSION.SDK_INT >= 17) {
                uptime = SystemClock.elapsedRealtime()
            }
        } catch (var3: Exception) {
        }
        return uptime
    }
    @JvmStatic
    val timezone: String
        get() {
            var timezone = ""
            try {
                val tz = TimeZone.getDefault()
                timezone = tz.getDisplayName(false, 0) + ", " + tz.id
            } catch (var2: Exception) {
            }
            return timezone
        }
    @JvmStatic
    fun isGpsEnabled(context: Context?): Boolean {
        val lm =context!!
                .getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return lm.isProviderEnabled("gps")
    }
    @JvmStatic
    val boottime: Long
        get() {
            var bootTime = 0L
            try {
                if (Build.VERSION.SDK_INT >= 17) {
                    bootTime =
                            System.currentTimeMillis() - SystemClock.elapsedRealtimeNanos() / 1000000L
                }
            } catch (var3: Exception) {
            }
            return bootTime
        }
    @JvmStatic
    fun getBatterytemp(context: Context?): Int {
        var temperature = 0
        try {
            val batteryInfoIntent = context!!
                    .registerReceiver(
                            null as BroadcastReceiver?,
                            IntentFilter("android.intent.action.BATTERY_CHANGED")
                    )
            temperature = batteryInfoIntent!!.getIntExtra("temperature", -1)
        } catch (var2: Exception) {
        }
        return temperature
    }
    @JvmStatic
    fun getBatteryStatus(context: Context?): Int {
        var temperature = 0
        try {
            val batteryInfoIntent = context!!
                    .registerReceiver(
                            null as BroadcastReceiver?,
                            IntentFilter("android.intent.action.BATTERY_CHANGED")
                    )
            temperature = batteryInfoIntent!!.getIntExtra("status", -1)
        } catch (var2: Exception) {
        }
        return temperature
    }
    @JvmStatic
    fun isPlugged(context: Context?): Boolean {
        var acPlugged = false
        var usbPlugged = false
        var wirePlugged = false
        try {
            val intentFilter = IntentFilter("android.intent.action.BATTERY_CHANGED")
            val intent = context!!
                    .registerReceiver(null as BroadcastReceiver?, intentFilter)
            val isPlugged = intent!!.getIntExtra("plugged", -1)
            acPlugged = 1 == isPlugged
            usbPlugged = 2 == isPlugged
            wirePlugged = 4 == isPlugged
        } catch (var6: Exception) {
        }
        return acPlugged || usbPlugged || wirePlugged
    }

    @JvmStatic
    fun isAcCharge(context: Context?): Int {
        var acPlugged = false
        var acCharge = 0
        try {
            val intentFilter = IntentFilter("android.intent.action.BATTERY_CHANGED")
            val intent = context!!
                    .registerReceiver(null as BroadcastReceiver?, intentFilter)
            val isPlugged = intent!!.getIntExtra("plugged", -1)
            acPlugged = 2 == isPlugged
            if (acPlugged){
                acCharge = 1
            }else{
                acCharge = 0
            }
        } catch (var6: Exception) {
        }
        return acCharge
    }

    @JvmStatic
    fun getWifiBSSID(context: Context?): String? {
        try {
            val wm =
                    context!!
                            .getSystemService(Context.WIFI_SERVICE) as WifiManager
            if (wm != null) {
                val winfo = wm.connectionInfo
                return winfo.bssid
            }
        } catch (var2: Exception) {
        }
        return null
    }
    @JvmStatic
    fun readArp(context: Context?): String {
        ml.clear()
        try {
            val br =
                    BufferedReader(FileReader("/proc/net/arp"))
            var line = ""
            var ip = ""
            var flag = ""
            var mac = ""
            while (br.readLine().also { line = it } != null) {
                try {
                    line = line.trim { it <= ' ' }
                    if (line.length >= 63 && !line.toUpperCase(Locale.US)
                                    .contains("IP")
                    ) {
                        ip = line.substring(0, 17).trim { it <= ' ' }
                        flag = line.substring(29, 32).trim { it <= ' ' }
                        mac = line.substring(41, 63).trim { it <= ' ' }
                        if (!mac.contains("00:00:00:00:00:00")) {
                            ml.add("mac=$mac ; ip= $ip ;flag= $flag")
                        }
                    }
                } catch (var6: Exception) {
                }
            }
            br.close()
        } catch (var7: Exception) {
        }
        return ml.toString()
    }
    @JvmStatic
    fun getBluetoothAddress(context: Context?): String? {
        return if (Build.VERSION.RELEASE == "10") {
            ""
        } else {
            try {
                val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
                val field =
                        bluetoothAdapter.javaClass.getDeclaredField("mService")
                field.isAccessible = true
                val bluetoothManagerService = field[bluetoothAdapter]
                if (bluetoothManagerService == null) {
                    null
                } else {
                    val method =
                            bluetoothManagerService.javaClass.getMethod("getAddress")
                    val address = method.invoke(bluetoothManagerService)
                    if (address != null && address is String) address else null
                }
            } catch (var5: Exception) {
                var5.printStackTrace()
                ""
            }
        }
    }
    @JvmStatic
    fun getCountryZipCode(context: Context?): String {
        var CountryZipCode = ""
        try {
            val locale =
                    context!!.resources
                            .configuration.locale
            CountryZipCode = locale.isO3Country
        } catch (var2: Exception) {
        }
        return CountryZipCode
    }

    @JvmStatic
    fun getNetworkCountryIso(context: Context?): String {
        var networkCountryIso = ""
        try {
            val tel = context!!
                    .getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            networkCountryIso = tel.networkCountryIso
        } catch (var2: Exception) {
        }
        return networkCountryIso
    }

    @JvmStatic
    fun getNetworkOperator(context: Context?): String {
        var networkOperator = ""
        try {
            val tel = context!!
                    .getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            networkOperator = tel.networkOperator
        } catch (var2: Exception) {
        }
        return networkOperator
    }

    @SuppressLint("MissingPermission")
    @JvmStatic
    fun getCellLocation(context: Context?): String {
        var cellLocation: GsmCellLocation? = null
        try {
            val tel = context!!
                    .getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            cellLocation = tel.cellLocation as GsmCellLocation
        } catch (var2: Exception) {
        }
        return cellLocation?.toString() ?: ""
    }


    @SuppressLint("MissingPermission")
    @JvmStatic
    fun getVoiceMailNumber(context: Context?): String {
        var voiceMailNumber = ""
        try {
            val tel = context!!.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            voiceMailNumber = tel.voiceMailNumber
        } catch (var2: Exception) {
            return ""
        }
        if(TextUtils.isEmpty( return voiceMailNumber)){
            return ""
        }else{
            return voiceMailNumber
        }
    }

}
