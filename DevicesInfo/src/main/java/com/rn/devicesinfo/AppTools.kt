package com.rn.devicesinfo

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityManager
import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Point
import android.net.Uri
import android.os.*
import android.os.Build.VERSION
import android.os.storage.StorageManager
import android.os.storage.StorageVolume
import android.provider.Settings
import android.provider.Settings.Secure
import android.provider.Settings.SettingNotFoundException
import android.telephony.TelephonyManager
import android.text.TextUtils
import android.text.format.Formatter
import android.util.Log
import android.view.Surface
import android.view.WindowManager
import androidx.annotation.RequiresPermission
import com.rn.devicesinfo.FileTools.Companion.sDCardPath
import java.io.*
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.net.NetworkInterface
import java.util.*

object AppTools {

    private var mActivityManager: ActivityManager? = null
    var sStatus = Status()

    /**
     * 得到屏幕的高
     *
     * @param context 实体
     * @return 设备屏幕的高度
     */
    @JvmStatic
    fun getScreenHeight(context: Context): Int {
        val wm = (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager)
        return wm.defaultDisplay.height
    }

    /**
     * 得到屏幕的宽
     *
     * @param context 实体
     * @return 设备屏幕的宽度
     */
    @JvmStatic
    fun getScreenWidth(context: Context): Int {
        val wm = (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager)
        return wm.defaultDisplay.width
    }

    /**
     * 得到设备屏幕的宽度
     */
    @JvmStatic
    fun getScreenWidths(context: Context): Int {
        val point = Point()
        val wm = (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager)
        wm.defaultDisplay.getRealSize(point)
        return point.x
    }

    fun getTotalMemory(): Long {
        var totalMemorySize = 0L
        val dir = "/proc/meminfo"
        try {
            val fr = FileReader(dir)
            val br = BufferedReader(fr, 2048)
            val memoryLine = br.readLine()
            val subMemoryLine = memoryLine.substring(memoryLine.indexOf("MemTotal:"))
            br.close()
            totalMemorySize = subMemoryLine.replace("\\D+".toRegex(), "").toInt().toLong()
        } catch (var7: IOException) {
            var7.printStackTrace()
        }
        return totalMemorySize
    }

    @Synchronized
    fun getActivityManager(context: Context): ActivityManager? {
        if (mActivityManager == null) {
            mActivityManager =
                    context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        }
        return mActivityManager
    }

    @JvmStatic
    fun getUsedPercentValue(context: Context?): String? {
        val totalMemorySize =
                getTotalMemory()
        val availableSize = getAvailableMemory(context) / 1024L
        val percent =
                ((totalMemorySize - availableSize).toFloat() / totalMemorySize.toFloat() * 100.0f).toInt()
        return "$percent%"
    }

    @JvmStatic
    fun getAvailableMemory(context: Context?): Long {
        val mi = ActivityManager.MemoryInfo()
        getActivityManager(context!!)!!
                .getMemoryInfo(mi)
        return mi.availMem
    }

    @JvmStatic
    fun getTotalCpuRate(): Float {
        return if (VERSION.RELEASE != "8" && VERSION.RELEASE != "9" && VERSION.RELEASE != "10") {
            val totalCpuTime1 =
                    getTotalCpuTime().toFloat()
            val totalUsedCpuTime1 =
                    totalCpuTime1 - sStatus.idletime.toFloat()
            try {
                Thread.sleep(360L)
            } catch (var7: InterruptedException) {
                var7.printStackTrace()
                return 50.0f
            }
            val totalCpuTime2 =
                    getTotalCpuTime().toFloat()
            val totalUsedCpuTime2 =
                    totalCpuTime2 - sStatus.idletime.toFloat()
            var cpuRate =
                    100.0f * (totalUsedCpuTime2 - totalUsedCpuTime1) / (totalCpuTime2 - totalCpuTime1)
            val reg = Regex("^[0-9]+(.[0-9]+)?$")
            val s = cpuRate.toString() + ""
            cpuRate = if (s.matches(reg)) {
                cpuRate
            } else {
                0.0f
            }
            cpuRate
        } else {
            50.0f
        }
    }


    fun getTotalCpuTime(): Long {
        return if (VERSION.RELEASE != "8" && VERSION.RELEASE != "9" && VERSION.RELEASE != "10") {
            var cpuInfos: Array<String>? = null
            try {
                val reader = BufferedReader(
                        InputStreamReader(FileInputStream("/proc/stat")),
                        1000
                )
                val load = reader.readLine()
                reader.close()
                cpuInfos = load.split(" ".toRegex()).toTypedArray()
                sStatus.usertime =
                        cpuInfos[2].toLong()
                sStatus.nicetime =
                        cpuInfos[3].toLong()
                sStatus.systemtime =
                        cpuInfos[4].toLong()
                sStatus.idletime =
                        cpuInfos[5].toLong()
                sStatus.iowaittime =
                        cpuInfos[6].toLong()
                sStatus.irqtime =
                        cpuInfos[7].toLong()
                sStatus.softirqtime =
                        cpuInfos[8].toLong()
            } catch (var3: java.lang.Exception) {
                var3.printStackTrace()
                return 40L
            }
            sStatus.totalTime
        } else {
            40L
        }
    }

    fun getAppCpuTime(): Long {
        var cpuInfos: Array<String>? = null
        cpuInfos = try {
            val pid = Process.myPid()
            val reader = BufferedReader(
                    InputStreamReader(FileInputStream("/proc/$pid/stat")),
                    1000
            )
            val load = reader.readLine()
            reader.close()
            load.split(" ".toRegex()).toTypedArray()
        } catch (var4: IOException) {
            var4.printStackTrace()
            return 1L
        }
        return cpuInfos[13].toLong() + cpuInfos[14].toLong() + cpuInfos[15]
                .toLong() + cpuInfos[16].toLong()
    }

    @JvmStatic
    fun getCurProcessCpuRate(): Float {
        val totalCpuTime1 =
                getTotalCpuTime().toFloat()
        val processCpuTime1 =
                getAppCpuTime().toFloat()
        try {
            Thread.sleep(360L)
        } catch (var7: java.lang.Exception) {
            return 0.0f
        }
        val totalCpuTime2 =
                getTotalCpuTime().toFloat()
        val processCpuTime2 =
                getAppCpuTime().toFloat()
        var cpuRate =
                100.0f * (processCpuTime2 - processCpuTime1) / (totalCpuTime2 - totalCpuTime1)
        if (cpuRate > 100.0f) {
            cpuRate = 100.0f
        }
        val reg = Regex("^[0-9]+(.[0-9]+)?$")
        val s = cpuRate.toString() + ""
        cpuRate = if (s.matches(reg)) {
            cpuRate
        } else {
            0.0f
        }
        return cpuRate
    }

    /**
     * 得到设备屏幕的高度
     */
    @JvmStatic
    fun getScreenHeights(context: Context): Int {
        val point = Point()
        val wm = (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager)
        wm.defaultDisplay.getRealSize(point)
        return point.y
    }

    /**
     * 得到设备的密度
     */
    @JvmStatic
    fun getScreenDensity(context: Context, type: String): String {
        if (type.equals("dpi")) {
            return context.resources.displayMetrics.densityDpi.toString()
        }
        return context.resources.displayMetrics.density.toString()
    }


    /**
     * IMEI （唯一标识序列号）
     *
     * 需与[.isPhone]一起使用
     *
     * 需添加权限 `<uses-permission android:name="android.permission.READ_PHONE_STATE"/>`
     *
     * @param context 上下文
     * @return IMEI
     */
    @JvmStatic
    @RequiresPermission("android.permission.READ_PHONE_STATE")
    fun getIMEI(context: Context): String? {
        return getImeiOrMeid(context, true)
    }

    @JvmStatic
    @RequiresPermission("android.permission.READ_PHONE_STATE")
    fun getMEID(context: Context): String? {
        return getImeiOrMeid(context, false)
    }

    private fun getMinOne(s0: String, s1: String): String? {
        val empty0 = TextUtils.isEmpty(s0)
        val empty1 = TextUtils.isEmpty(s1)
        return if (empty0 && empty1) {
            ""
        } else if (!empty0 && !empty1) {
            if (s0.compareTo(s1) <= 0) s0 else s1
        } else {
            if (!empty0) s0 else s1
        }
    }


    @SuppressLint("HardwareIds", "MissingPermission")
    @RequiresPermission("android.permission.READ_PHONE_STATE")
    @JvmStatic
    fun getImeiOrMeid(context: Context, isImei: Boolean): String? {
        return if (VERSION.SDK_INT >= 29) {
            ""
        } else {
            val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            if (VERSION.SDK_INT >= 26) {
                if (isImei) {
                    var id = ""
                    try {
                        id = getMinOne(
                                tm.getImei(0),
                                tm.getImei(1)
                        )!!
                    } catch (e: Exception) {
                        id = ""
                    }
                    return id
                } else {
                    var id = ""
                    try {
                        id = getMinOne(
                                tm.getMeid(0),
                                tm.getMeid(1)
                        )!!
                    } catch (e: Exception) {
                        id = ""
                    }
                    return id
                }
            } else {
                val deviceId: String?
                if (VERSION.SDK_INT >= 21) {
                    deviceId =
                            getSystemPropertyByReflect(if (isImei) "ril.gsm.imei" else "ril.cdma.meid")
                    if (!TextUtils.isEmpty(deviceId)) {
                        val idArr = deviceId!!.split(",").toTypedArray()
                        if (idArr.size == 2) getMinOne(
                                idArr[0],
                                idArr[1]
                        ) else idArr[0]
                    } else {
                        var id0 = tm.deviceId
                        var id1 = ""
                        try {
                            val method =
                                    tm.javaClass.getMethod("getDeviceId", Integer.TYPE)
                            id1 = method.invoke(tm, if (isImei) 1 else 2) as String
                        } catch (var6: Exception) {
                            var6.printStackTrace()
                        }
                        if (isImei) {
                            if (id0 != null && id0.length < 15) {
                                id0 = ""
                            }
                            if (id1 != null && id1.length < 15) {
                                id1 = ""
                            }
                        } else {
                            if (id0 != null && id0.length == 14) {
                                id0 = ""
                            }
                            if (id1 != null && id1.length == 14) {
                                id1 = ""
                            }
                        }
                        getMinOne(id0, id1)
                    }
                } else {
                    deviceId = tm.deviceId
                    if (isImei) {
                        if (deviceId != null && deviceId.length >= 15) {
                            return deviceId
                        }
                    } else if (deviceId != null && deviceId.length == 14) {
                        return deviceId
                    }
                    ""
                }
            }
        }
    }


    private fun getSystemPropertyByReflect(key: String): String? {
        return try {
            val clz = Class.forName("android.os.SystemProperties")
            val getMethod = clz.getMethod(
                    "get",
                    String::class.java,
                    String::class.java
            )
            getMethod.invoke(clz, key, "") as String
        } catch (var3: java.lang.Exception) {
            ""
        }
    }

    /**
     * 获取设备的IMSI
     *
     * @param context
     * @return
     */
    @JvmStatic
    fun getIMSI(context: Context): String? {
        return getSubscriberId(context)
    }

    /**
     * 获取设备的IMEI
     *
     * @param context
     * @return
     */
    @JvmStatic
    @SuppressLint("HardwareIds", "MissingPermission")
    fun getDeviceIdIMEI(context: Context): String? {
        val id: String
        //android.telephony.TelephonyManager
        val mTelephony = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        id = if (mTelephony.deviceId != null) {
            mTelephony.deviceId
        } else {
            //android.provider.Settings;
            Settings.Secure.getString(
                    context.applicationContext.contentResolver,
                    Settings.Secure.ANDROID_ID
            )
        }
        return id
    }


    @SuppressLint("MissingPermission")
    @RequiresPermission("android.permission.READ_PHONE_STATE")
    @JvmStatic
    fun getIMEIOne(context: Context): String? {
        var imei1 = ""
        try {
            if (VERSION.SDK_INT >= 29) {
                return imei1
            }
            val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            if (VERSION.SDK_INT >= 26) {
                imei1 = tm.getImei(0)
            }
        } catch (var2: java.lang.Exception) {
        }
        return imei1
    }

    @SuppressLint("MissingPermission")
    @RequiresPermission("android.permission.READ_PHONE_STATE")
    @JvmStatic
    fun getIMEITwo(context: Context): String? {
        var imei2 = ""
        try {
            if (VERSION.SDK_INT >= 29) {
                return imei2
            }
            val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            if (VERSION.SDK_INT >= 26) {
                imei2 = tm.getImei(1)
            }
        } catch (var2: java.lang.Exception) {
        }
        return imei2
    }

    //操作系统语言
    @JvmStatic
    fun getCountryByLanguage(): String? {
        return Resources.getSystem().configuration.locale.country
    }

    //国家
    @JvmStatic
    fun getCountryCodeByLanguage(defaultValue: String?): String? {
        return getCountryCodeFromMap()!!.get(getCountryByLanguage())
                ?: defaultValue
    }

    @JvmStatic
    private fun getCountryCodeFromMap(): HashMap<String, String>? {
        var countryCodeMap: HashMap<String, String> = HashMap<String, String>(256)
        countryCodeMap!!["AL"] = "+355"
        countryCodeMap!!["DZ"] = "+213"
        countryCodeMap["AF"] = "+93"
        countryCodeMap["AR"] = "+54"
        countryCodeMap["AE"] = "+971"
        countryCodeMap["AW"] = "+297"
        countryCodeMap["OM"] = "+968"
        countryCodeMap["AZ"] = "+994"
        countryCodeMap["AC"] = "+247"
        countryCodeMap["EG"] = "+20"
        countryCodeMap["ET"] = "+251"
        countryCodeMap["IE"] = "+353"
        countryCodeMap["EE"] = "+372"
        countryCodeMap["AD"] = "+376"
        countryCodeMap["AO"] = "+244"
        countryCodeMap["AI"] = "+1"
        countryCodeMap["AG"] = "+1"
        countryCodeMap["AT"] = "+43"
        countryCodeMap["AX"] = "+358"
        countryCodeMap["AU"] = "+61"
        countryCodeMap["BB"] = "+1"
        countryCodeMap["PG"] = "+675"
        countryCodeMap["BS"] = "+1"
        countryCodeMap["PK"] = "+92"
        countryCodeMap["PY"] = "+595"
        countryCodeMap["PS"] = "+970"
        countryCodeMap["BH"] = "+973"
        countryCodeMap["PA"] = "+507"
        countryCodeMap["BR"] = "+55"
        countryCodeMap["BY"] = "+375"
        countryCodeMap["BM"] = "+1"
        countryCodeMap["BG"] = "+359"
        countryCodeMap["MP"] = "+1"
        countryCodeMap["BJ"] = "+229"
        countryCodeMap["BE"] = "+32"
        countryCodeMap["IS"] = "+354"
        countryCodeMap["PR"] = "+1"
        countryCodeMap["PL"] = "+48"
        countryCodeMap["BA"] = "+387"
        countryCodeMap["BO"] = "+591"
        countryCodeMap["BZ"] = "+501"
        countryCodeMap["BW"] = "+267"
        countryCodeMap["BT"] = "+975"
        countryCodeMap["BF"] = "+226"
        countryCodeMap["BI"] = "+257"
        countryCodeMap["KP"] = "+850"
        countryCodeMap["GQ"] = "+240"
        countryCodeMap["DK"] = "+45"
        countryCodeMap["DE"] = "+49"
        countryCodeMap["TL"] = "+670"
        countryCodeMap["TG"] = "+228"
        countryCodeMap["DO"] = "+1"
        countryCodeMap["DM"] = "+1"
        countryCodeMap["RU"] = "+7"
        countryCodeMap["EC"] = "+593"
        countryCodeMap["ER"] = "+291"
        countryCodeMap["FR"] = "+33"
        countryCodeMap["FO"] = "+298"
        countryCodeMap["PF"] = "+689"
        countryCodeMap["GF"] = "+594"
        countryCodeMap["VA"] = "+39"
        countryCodeMap["PH"] = "+63"
        countryCodeMap["FJ"] = "+679"
        countryCodeMap["FI"] = "+358"
        countryCodeMap["CV"] = "+238"
        countryCodeMap["FK"] = "+500"
        countryCodeMap["GM"] = "+220"
        countryCodeMap["CG"] = "+242"
        countryCodeMap["CD"] = "+243"
        countryCodeMap["CO"] = "+57"
        countryCodeMap["CR"] = "+506"
        countryCodeMap["GG"] = "+44"
        countryCodeMap["GD"] = "+1"
        countryCodeMap["GL"] = "+299"
        countryCodeMap["GE"] = "+995"
        countryCodeMap["CU"] = "+53"
        countryCodeMap["GP"] = "+590"
        countryCodeMap["GU"] = "+1"
        countryCodeMap["GY"] = "+592"
        countryCodeMap["KZ"] = "+7"
        countryCodeMap["HT"] = "+509"
        countryCodeMap["KR"] = "+82"
        countryCodeMap["NL"] = "+31"
        countryCodeMap["BQ"] = "+599"
        countryCodeMap["SX"] = "+1"
        countryCodeMap["ME"] = "+382"
        countryCodeMap["HN"] = "+504"
        countryCodeMap["KI"] = "+686"
        countryCodeMap["DJ"] = "+253"
        countryCodeMap["KG"] = "+996"
        countryCodeMap["GN"] = "+224"
        countryCodeMap["GW"] = "+245"
        countryCodeMap["CA"] = "+1"
        countryCodeMap["GH"] = "+233"
        countryCodeMap["GA"] = "+241"
        countryCodeMap["KH"] = "+855"
        countryCodeMap["CZ"] = "+420"
        countryCodeMap["ZW"] = "+263"
        countryCodeMap["CM"] = "+237"
        countryCodeMap["QA"] = "+974"
        countryCodeMap["KY"] = "+1"
        countryCodeMap["CC"] = "+61"
        countryCodeMap["KM"] = "+269"
        countryCodeMap["XK"] = "+383"
        countryCodeMap["CI"] = "+225"
        countryCodeMap["KW"] = "+965"
        countryCodeMap["HR"] = "+385"
        countryCodeMap["KE"] = "+254"
        countryCodeMap["CK"] = "+682"
        countryCodeMap["CW"] = "+599"
        countryCodeMap["LV"] = "+371"
        countryCodeMap["LS"] = "+266"
        countryCodeMap["LA"] = "+856"
        countryCodeMap["LB"] = "+961"
        countryCodeMap["LT"] = "+370"
        countryCodeMap["LR"] = "+231"
        countryCodeMap["LY"] = "+218"
        countryCodeMap["LI"] = "+423"
        countryCodeMap["RE"] = "+262"
        countryCodeMap["LU"] = "+352"
        countryCodeMap["RW"] = "+250"
        countryCodeMap["RO"] = "+40"
        countryCodeMap["MG"] = "+261"
        countryCodeMap["IM"] = "+44"
        countryCodeMap["MV"] = "+960"
        countryCodeMap["MT"] = "+356"
        countryCodeMap["MW"] = "+265"
        countryCodeMap["MY"] = "+60"
        countryCodeMap["ML"] = "+223"
        countryCodeMap["MK"] = "+389"
        countryCodeMap["MH"] = "+692"
        countryCodeMap["MQ"] = "+596"
        countryCodeMap["YT"] = "+262"
        countryCodeMap["MU"] = "+230"
        countryCodeMap["MR"] = "+222"
        countryCodeMap["US"] = "+1"
        countryCodeMap["AS"] = "+1"
        countryCodeMap["VI"] = "+1"
        countryCodeMap["MN"] = "+976"
        countryCodeMap["MS"] = "+1"
        countryCodeMap["BD"] = "+880"
        countryCodeMap["PE"] = "+51"
        countryCodeMap["FM"] = "+691"
        countryCodeMap["MM"] = "+95"
        countryCodeMap["MD"] = "+373"
        countryCodeMap["MA"] = "+212"
        countryCodeMap["MC"] = "+377"
        countryCodeMap["MZ"] = "+258"
        countryCodeMap["MX"] = "+52"
        countryCodeMap["NA"] = "+264"
        countryCodeMap["ZA"] = "+27"
        countryCodeMap["SS"] = "+211"
        countryCodeMap["NR"] = "+674"
        countryCodeMap["NI"] = "+505"
        countryCodeMap["NP"] = "+977"
        countryCodeMap["NE"] = "+227"
        countryCodeMap["NG"] = "+234"
        countryCodeMap["NU"] = "+683"
        countryCodeMap["NO"] = "+47"
        countryCodeMap["NF"] = "+672"
        countryCodeMap["PW"] = "+680"
        countryCodeMap["PT"] = "+351"
        countryCodeMap["JP"] = "+81"
        countryCodeMap["SE"] = "+46"
        countryCodeMap["CH"] = "+41"
        countryCodeMap["SV"] = "+503"
        countryCodeMap["WS"] = "+685"
        countryCodeMap["RS"] = "+381"
        countryCodeMap["SL"] = "+232"
        countryCodeMap["SN"] = "+221"
        countryCodeMap["CY"] = "+357"
        countryCodeMap["SC"] = "+248"
        countryCodeMap["SA"] = "+966"
        countryCodeMap["BL"] = "+590"
        countryCodeMap["CX"] = "+61"
        countryCodeMap["ST"] = "+239"
        countryCodeMap["SH"] = "+290"
        countryCodeMap["PN"] = "+870"
        countryCodeMap["KN"] = "+1"
        countryCodeMap["LC"] = "+1"
        countryCodeMap["MF"] = "+590"
        countryCodeMap["SM"] = "+378"
        countryCodeMap["PM"] = "+508"
        countryCodeMap["VC"] = "+1"
        countryCodeMap["LK"] = "+94"
        countryCodeMap["SK"] = "+421"
        countryCodeMap["SI"] = "+386"
        countryCodeMap["SJ"] = "+47"
        countryCodeMap["SZ"] = "+268"
        countryCodeMap["SD"] = "+249"
        countryCodeMap["SR"] = "+597"
        countryCodeMap["SB"] = "+677"
        countryCodeMap["SO"] = "+252"
        countryCodeMap["TJ"] = "+992"
        countryCodeMap["TH"] = "+66"
        countryCodeMap["TZ"] = "+255"
        countryCodeMap["TO"] = "+676"
        countryCodeMap["TC"] = "+1"
        countryCodeMap["TA"] = "+290"
        countryCodeMap["TT"] = "+1"
        countryCodeMap["TN"] = "+216"
        countryCodeMap["TV"] = "+688"
        countryCodeMap["TR"] = "+90"
        countryCodeMap["TM"] = "+993"
        countryCodeMap["TK"] = "+690"
        countryCodeMap["WF"] = "+681"
        countryCodeMap["VU"] = "+678"
        countryCodeMap["GT"] = "+502"
        countryCodeMap["VE"] = "+58"
        countryCodeMap["BN"] = "+673"
        countryCodeMap["UG"] = "+256"
        countryCodeMap["UA"] = "+380"
        countryCodeMap["UY"] = "+598"
        countryCodeMap["UZ"] = "+998"
        countryCodeMap["GR"] = "+30"
        countryCodeMap["ES"] = "+34"
        countryCodeMap["EH"] = "+212"
        countryCodeMap["SG"] = "+65"
        countryCodeMap["NC"] = "+687"
        countryCodeMap["NZ"] = "+64"
        countryCodeMap["HU"] = "+36"
        countryCodeMap["SY"] = "+963"
        countryCodeMap["JM"] = "+1"
        countryCodeMap["AM"] = "+374"
        countryCodeMap["YE"] = "+967"
        countryCodeMap["IQ"] = "+964"
        countryCodeMap["UM"] = "+1"
        countryCodeMap["IR"] = "+98"
        countryCodeMap["IL"] = "+972"
        countryCodeMap["IT"] = "+39"
        countryCodeMap["IN"] = "+91"
        countryCodeMap["ID"] = "+62"
        countryCodeMap["GB"] = "+44"
        countryCodeMap["VG"] = "+1"
        countryCodeMap["IO"] = "+246"
        countryCodeMap["JO"] = "+962"
        countryCodeMap["VN"] = "+84"
        countryCodeMap["ZM"] = "+260"
        countryCodeMap["JE"] = "+44"
        countryCodeMap["TD"] = "+235"
        countryCodeMap["GI"] = "+350"
        countryCodeMap["CL"] = "+56"
        countryCodeMap["CF"] = "+236"
        countryCodeMap["CN"] = "+86"
        countryCodeMap["MO"] = "+853"
        countryCodeMap["TW"] = "+886"
        countryCodeMap["HK"] = "+852"
        return countryCodeMap
    }

    /**
     * 获取ISO标准的国家码，即国际长途区号
     *
     * @param context
     * @return
     */
    @JvmStatic
    fun getNetworkCountryIso(context: Context): String {
        val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        return tm.networkCountryIso
    }

    /**
     * 获取设备的 MCC + MNC
     *
     * @param context
     * @return
     */
    @JvmStatic
    fun getNetworkOperator(context: Context): String {
        val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        return tm.networkOperator
    }

    /**
     * 获取(当前已注册的用户)的名字
     *
     * @param context
     * @return
     */
    @JvmStatic
    fun getNetworkOperatorName(context: Context): String {
        val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        return tm.networkOperatorName
    }

    /**
     * 获取当前使用的网络类型
     *
     * @param context
     * @return
     */
    @SuppressLint("MissingPermission")
    @JvmStatic
    fun getNetworkType(context: Context): Int {
        val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        return tm.networkType
    }

    /**
     * 获取手机类型
     *
     * @param context
     * @return
     */
    @JvmStatic
    fun getPhoneType(context: Context): Int {
        val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        return tm.phoneType
    }

    /**
     * 获取SIM卡的国家码
     *
     * @param context
     * @return
     */
    @JvmStatic
    fun getSimCountryIso(context: Context): String {
        val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        return tm.simCountryIso
    }

    /**
     * 获取SIM卡提供的移动国家码和移动网络码.5或6位的十进制数字
     *
     * @param context
     * @return
     */
    @JvmStatic
    fun getSimOperator(context: Context): String {
        val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        return tm.simOperator
    }

    /**
     * 获取服务商名称
     *
     * @param context
     * @return
     */
    @JvmStatic
    fun getSimOperatorName(context: Context): String {
        val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        return tm.simOperatorName
    }

    @SuppressLint("HardwareIds", "MissingPermission")
    @JvmStatic
    fun getSimSerialNumber(context: Context): String? {
        try {
            val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            if (TextUtils.isEmpty(tm.simSerialNumber)) {
                return ""
            } else {
                return tm.simSerialNumber
            }
        } catch (var1: Exception) {
            return ""
        }
    }

    @SuppressLint("HardwareIds", "MissingPermission")
    @RequiresPermission("android.permission.READ_PHONE_STATE")
    @JvmStatic
    fun getDeviceId(context: Context): String? {
        return if (VERSION.SDK_INT >= 29) {
            ""
        } else {
            val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            val deviceId = tm.deviceId
            if (!TextUtils.isEmpty(deviceId)) {
                deviceId
            } else if (VERSION.SDK_INT >= 26) {
                val imei = tm.imei
                if (!TextUtils.isEmpty(imei)) {
                    imei
                } else {
                    val meid = tm.meid
                    if (TextUtils.isEmpty(meid)) "" else meid
                }
            } else {
                ""
            }
        }
    }

    @JvmStatic
    fun getSimState(context: Context): Boolean {
        val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        return tm.simState == 5
    }

    @SuppressLint("HardwareIds", "MissingPermission")
    @JvmStatic
    fun getSubscriberId(context: Context): String? {
        try {
            val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            if (TextUtils.isEmpty(tm.subscriberId)) {
                return ""
            } else {
                return tm.subscriberId
            }
        } catch (e: Exception) {
            return ""
        }
    }


    @SuppressLint("HardwareIds")
    @JvmStatic
    fun getAndroidId(context: Context): String {
        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    }


    @JvmStatic
    val buildBrandModel: String
        get() = Build.MODEL


    @JvmStatic
    val buildMANUFACTURER: String
        get() = Build.MANUFACTURER


    @SuppressLint("HardwareIds", "MissingPermission")
    @RequiresPermission("android.permission.READ_PHONE_STATE")
    @JvmStatic
    fun getSerial(): String? {
        return if (VERSION.SDK_INT >= 29) {
            try {
                Build.getSerial()
            } catch (var1: SecurityException) {
                ""
            }
        } else {
            if (VERSION.SDK_INT >= 26) Build.getSerial() else Build.SERIAL
        }
    }


    @JvmStatic
    fun getABIs(): Array<String> {
        return if (VERSION.SDK_INT >= 21) {
            Build.SUPPORTED_ABIS
        } else {
            if (!TextUtils.isEmpty(Build.CPU_ABI2)) arrayOf(
                    Build.CPU_ABI,
                    Build.CPU_ABI2
            ) else arrayOf<String>(Build.CPU_ABI)
        }
    }

    @JvmStatic
    fun isTablet(): Boolean {
        return Resources.getSystem()
                .configuration.screenLayout and 15 >= 3
    }

    @JvmStatic
    fun isAdbEnabled(context: Context): Boolean {
        return Secure.getInt(
                context.contentResolver,
                "adb_enabled",
                0
        ) > 0
    }

    @JvmStatic
    fun isEmulator(context: Context): Boolean {
        val checkProperty =
                Build.FINGERPRINT.startsWith("generic") || Build.FINGERPRINT.toLowerCase()
                        .contains("vbox") || Build.FINGERPRINT.toLowerCase()
                        .contains("test-keys") || Build.MODEL.contains("google_sdk") || Build.MODEL.contains(
                        "Emulator"
                ) || Build.MODEL.contains("Android SDK built for x86") || Build.MANUFACTURER.contains("Genymotion") || Build.BRAND.startsWith(
                        "generic"
                ) && Build.DEVICE.startsWith("generic") || "google_sdk" == Build.PRODUCT
        return if (checkProperty) {
            true
        } else {
            var operatorName = ""
            val tm = context
                    .getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            if (tm != null) {
                val name = tm.networkOperatorName
                if (name != null) {
                    operatorName = name
                }
            }
            val checkOperatorName = operatorName.toLowerCase() == "android"
            if (checkOperatorName) {
                true
            } else {
                val url = "tel:123456"
                val intent = Intent()
                intent.data = Uri.parse(url)
                intent.action = "android.intent.action.DIAL"
                intent.resolveActivity(
                        context.applicationContext.packageManager
                ) == null
            }
        }
    }

    @JvmStatic
    fun getMacAddress(context: Context): String {
        var macAddress = ""
        var networkInterface: NetworkInterface? = null
        try {
            if (NetworkInterface.getByName("eth0") != null) {
                networkInterface = NetworkInterface.getByName("eth0")
            } else if (NetworkInterface.getByName("wlan0") != null) {
                networkInterface = NetworkInterface.getByName("wlan0")
            }

            if (networkInterface == null) {
                return macAddress
            }
            var macArr = networkInterface.hardwareAddress
            val buf = StringBuilder()
            for (b in macArr) {
                buf.append(String.format("%02X", b) + ":")
            }
            macAddress = buf.toString()
            return macAddress.substring(0, macAddress.length - 1)
        } catch (e: Exception) {
        }
        return macAddress
    }

    @JvmStatic
    fun isPhone(context: Context): Boolean {
        val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        return tm.phoneType != TelephonyManager.PHONE_TYPE_NONE
    }

    @JvmStatic
    fun isLandscape(context: Context): Boolean {
        return context.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    }

    @JvmStatic
    fun isPortrait(context: Context): Boolean {
        return context.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT
    }

    @JvmStatic
    fun isFullScreen(activity: Activity): Boolean {
        val fullScreenFlag = 1024
        return activity.window.attributes.flags and fullScreenFlag == fullScreenFlag
    }

    @JvmStatic
    fun getScreenRotation(activity: Activity): Int {
        return when (activity.windowManager.defaultDisplay.rotation) {
            Surface.ROTATION_0 -> 0
            Surface.ROTATION_90 -> 90
            Surface.ROTATION_180 -> 180
            Surface.ROTATION_270 -> 270
            else -> 0
        }
    }

    @JvmStatic
    fun isScreenLock(context: Context): Boolean {
        val km = context
                .getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        return km.inKeyguardRestrictedInputMode()
    }

    @JvmStatic
    fun getSleepDuration(context: Context): Int {
        return try {
            Settings.System.getInt(
                    context.applicationContext.contentResolver,
                    "screen_off_timeout"
            )
        } catch (var1: SettingNotFoundException) {
            var1.printStackTrace()
            -123
        }
    }

    @JvmStatic
    fun isAutoBrightnessEnabled(activity: Activity): Boolean {
        return try {
            val mode = Settings.System.getInt(
                    activity.applicationContext.contentResolver,
                    "screen_brightness_mode"
            )
            mode == 1
        } catch (var1: SettingNotFoundException) {
            var1.printStackTrace()
            false
        }
    }

    @JvmStatic
    fun getBrightness(activity: Activity): Int {
        return try {
            Settings.System.getInt(
                    activity.applicationContext.contentResolver,
                    "screen_brightness"
            )
        } catch (var1: SettingNotFoundException) {
            var1.printStackTrace()
            0
        }
    }

    @JvmStatic
    fun getSDCardInfo(context: Context): List<SDCardInfo?>? {
        val paths: MutableList<SDCardInfo?> = ArrayList()
        val sm = context
                .getSystemService(Context.STORAGE_SERVICE) as StorageManager
        return if (sm == null) {
            paths
        } else {
            val getPathMethod: Method
            if (VERSION.SDK_INT >= 24) {
                val storageVolumes: List<*> = sm.storageVolumes
                try {
                    getPathMethod = StorageVolume::class.java.getMethod("getPath")
                    val var4 = storageVolumes.iterator()
                    while (var4.hasNext()) {
                        val storageVolume = var4.next() as StorageVolume
                        val isRemovable = storageVolume.isRemovable
                        val state = storageVolume.state
                        val path =
                                getPathMethod.invoke(storageVolume) as String
                        paths.add(
                                SDCardInfo(
                                        path,
                                        state,
                                        isRemovable,
                                        readSDCard(0, context),
                                        readSDCard(1, context)
                                )
                        )
                    }
                } catch (var18: NoSuchMethodException) {
                    var18.printStackTrace()
                } catch (var19: IllegalAccessException) {
                    var19.printStackTrace()
                } catch (var20: InvocationTargetException) {
                    var20.printStackTrace()
                }
            } else {
                try {
                    val storageVolumeClazz =
                            Class.forName("android.os.storage.StorageVolume")
                    getPathMethod = storageVolumeClazz.getMethod("getPath")
                    val isRemovableMethod =
                            storageVolumeClazz.getMethod("isRemovable")
                    val getVolumeStateMethod =
                            StorageManager::class.java.getMethod(
                                    "getVolumeState",
                                    String::class.java
                            )
                    val getVolumeListMethod =
                            StorageManager::class.java.getMethod("getVolumeList")
                    val result = getVolumeListMethod.invoke(sm)
                    val length = java.lang.reflect.Array.getLength(result)
                    for (i in 0 until length) {
                        val storageVolumeElement =
                                java.lang.reflect.Array.get(result, i)
                        val path =
                                getPathMethod.invoke(storageVolumeElement) as String
                        val isRemovable =
                                isRemovableMethod.invoke(storageVolumeElement) as Boolean
                        val state =
                                getVolumeStateMethod.invoke(sm, path) as String
                        paths.add(
                                SDCardInfo(
                                        path,
                                        state,
                                        isRemovable,
                                        readSDCard(0, context),
                                        readSDCard(1, context)
                                )
                        )
                    }
                } catch (var14: ClassNotFoundException) {
                    var14.printStackTrace()
                } catch (var15: InvocationTargetException) {
                    var15.printStackTrace()
                } catch (var16: NoSuchMethodException) {
                    var16.printStackTrace()
                } catch (var17: IllegalAccessException) {
                    var17.printStackTrace()
                }
            }
            paths
        }
    }

    @JvmStatic
    fun getMountedSDCardPath(context: Context): List<String?>? {
        val path: MutableList<String?> = ArrayList()
        val sdCardInfo =
                getSDCardInfo(context)
        return if (sdCardInfo != null && !sdCardInfo.isEmpty()) {
            val var2: Iterator<*> = sdCardInfo.iterator()
            while (var2.hasNext()) {
                val cardInfo =
                        var2.next() as SDCardInfo
                val state = cardInfo.state
                if (state != null && "mounted" == state.toLowerCase()) {
                    path.add(cardInfo.path)
                }
            }
            path
        } else {
            path
        }
    }

    @JvmStatic
    fun getExternalTotalSize(): Long {
        return FileTools.getFsTotalSize(sDCardPath)
    }

    @JvmStatic
    fun getExternalAvailableSize(): Long {
        return FileTools.getFsAvailableSize(sDCardPath)
    }

    @JvmStatic
    fun getInternalTotalSize(): Long {
        return FileTools.getFsTotalSize(
                Environment.getDataDirectory().absolutePath
        )
    }

    @JvmStatic
    fun getInternalAvailableSize(): Long {
        return FileTools.getFsAvailableSize(
                Environment.getDataDirectory().absolutePath
        )
    }

    fun readSDCard(type: Int, context: Context): String {
        val blockSize: Long
        val totalBlocks: Long
        val avaibleBlocks: Long
        // 判断是否有插入并挂载存储卡
        if (Environment.getExternalStorageState() ==
                Environment.MEDIA_MOUNTED
        ) {
            val path = Environment.getExternalStorageDirectory()
            val statFs = StatFs(path.path)
            /*
             * Build.VERSION.SDK_INT:获取当前系统版本的等级
             * Build.VERSION_CODES.JELLY_BEAN_MR2表示安卓4.3，也就是18，这里直接写18也可以
             * 因为getBlockSizeLong()等三个方法是安卓4.3以后才有的，所以这里需要判断当前系统版本
             * 补充一个知识点：所有储存设备都被分成若干块，每一块都有固定大小。
             */if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                // 获取块的数量
                blockSize = statFs.blockSizeLong
                // 获取一共有多少块
                totalBlocks = statFs.blockCountLong
                // 可以活动的块
                avaibleBlocks = statFs.availableBlocksLong
                if (type == 0) {
                    return formatSize(totalBlocks * blockSize, context)
                } else {
                    return formatSize(avaibleBlocks * blockSize, context)
                }
                Log.i("aaaa", "总空间：" + formatSize(totalBlocks * blockSize, context))
                Log.i("aaaa", "可用空间：" + formatSize(avaibleBlocks * blockSize, context))
                Log.i("aaaa", "已用空间：" + formatSize(totalBlocks * blockSize
                        - avaibleBlocks * blockSize, context))
            } else {
                /*
                 * 黑线说明这三个API已经过时了。但是为了兼容4.3一下的系统，我们需要写上
                 */
                blockSize = statFs.blockSize.toLong()
                totalBlocks = statFs.blockCount.toLong()
                avaibleBlocks = statFs.availableBlocks.toLong()
                if (type == 0) {
                    return formatSize(totalBlocks * blockSize, context)
                } else {
                    return formatSize(avaibleBlocks * blockSize, context)
                }
                Log.i("aaaa", formatSize(avaibleBlocks * blockSize, context));
            }
        } else {
            return ""
        }
        return ""
    }

    private fun formatSize(size: Long, context: Context): String {
        // 格式化显示的数据。
        return Formatter.formatFileSize(context, size)
    }

    class Status {
        var usertime: Long = 0
        var nicetime: Long = 0
        var systemtime: Long = 0
        var idletime: Long = 0
        var iowaittime: Long = 0
        var irqtime: Long = 0
        var softirqtime: Long = 0
        val totalTime: Long
            get() = usertime + nicetime + systemtime + idletime + iowaittime + irqtime + softirqtime
    }


    class SDCardInfo internal constructor(
            val path: String,
            val state: String,
            val isRemovable: Boolean,
            val totalSize: String,
            val availableSize: String
    ) {
        override fun toString(): String {
            return "SDCardInfo(path='$path', state='$state', isRemovable=$isRemovable, totalSize='$totalSize', availableSize='$availableSize')"
        }
    }
}