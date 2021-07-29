package com.rn.devicesinfo

import android.annotation.SuppressLint
import android.os.Build.VERSION
import android.os.Environment
import android.os.StatFs
import android.text.TextUtils
import java.io.*
import java.lang.Exception


class FileTools {

    companion object {

        /**
         * 判断SD卡是否可用
         *
         * @return true : 可用<br></br>false : 不可用
         */
        val isSDCardEnable: Boolean
            get() = Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()

        /**
         * 获取SD卡路径
         *
         * 一般是/storage/emulated/0/
         *
         * @return SD卡路径
         */
        @JvmStatic
        val sDCardPath: String
            get() = if (!isSDCardEnable) {
                "sdcard unable!"
            } else Environment.getExternalStorageDirectory().path + File.separator

        @JvmStatic
        fun getFsTotalSize(anyPathInFs: String?): Long {
            return if (TextUtils.isEmpty(anyPathInFs)) {
                0L
            } else {
                val statFs = StatFs(anyPathInFs)
                val blockSize: Long
                val totalSize: Long
                try{
                    if (VERSION.SDK_INT >= 18) {
                        blockSize = statFs.blockSizeLong
                        totalSize = statFs.blockCountLong
                    } else {
                        blockSize = statFs.blockSize.toLong()
                        totalSize = statFs.blockCount.toLong()
                    }
                    blockSize * totalSize
                }catch (e:Exception){
                    0L
                }
            }
        }
        @JvmStatic
        fun getFsAvailableSize(anyPathInFs: String?): Long {
            return if (TextUtils.isEmpty(anyPathInFs)) {
                0L
            } else {
                val statFs = StatFs(anyPathInFs)
                val blockSize: Long
                val availableSize: Long
                try {
                    if (VERSION.SDK_INT >= 18) {
                        blockSize = statFs.blockSizeLong
                        availableSize = statFs.availableBlocksLong
                    } else {
                        blockSize = statFs.blockSize.toLong()
                        availableSize = statFs.availableBlocks.toLong()
                    }
                    blockSize * availableSize
                }catch (e:Exception){
                    0L
                }
            }
        }

        @SuppressLint("DefaultLocale")
        @JvmStatic
        fun byte2FitMemorySize(byteSize: Long): String? {
            return byte2FitMemorySize(byteSize, 3)
        }
        @SuppressLint("DefaultLocale")
        @JvmStatic
        fun byte2FitMemorySize(byteSize: Long, precision: Int): String? {
            return if (precision < 0) {
                throw IllegalArgumentException("precision shouldn't be less than zero!")
            } else if (byteSize < 0L) {
                throw IllegalArgumentException("byteSize shouldn't be less than zero!")
            } else if (byteSize < 1024L) {
                String.format("%." + precision + "fB", byteSize.toDouble())
            } else if (byteSize < 1048576L) {
                String.format(
                    "%." + precision + "fKB",
                    byteSize.toDouble() / 1024.0
                )
            } else {
                if (byteSize < 1073741824L) String.format(
                    "%." + precision + "fMB",
                    byteSize.toDouble() / 1048576.0
                ) else String.format(
                    "%." + precision + "fGB",
                    byteSize.toDouble() / 1.073741824E9
                )
            }
        }

        /**
         * SD卡是否可用.
         */
        @JvmStatic
        fun sdCardIsAvailable(): Boolean {
            return if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
                val sd = File(Environment.getExternalStorageDirectory().path)
                sd.canWrite()
            } else {
                false
            }
        }





        //----------------------------------------------------------------------------------------------
        /**
         * 根据文件路径获取文件
         *
         * @param filePath 文件路径
         * @return 文件
         */
        @JvmStatic
        fun getFileByPath(filePath: String?): File? {
            return if (TextUtils.isEmpty(filePath)) null else File(filePath)
        }
        //==============================================================================================

        /**
         * 判断文件是否存在
         *
         * @param file 文件
         * @return `true`: 存在<br></br>`false`: 不存在
         */
        @JvmStatic
        fun isFileExists(file: File?): Boolean {
            return file != null && file.exists()
        }



        /**
         * 判断目录是否存在，不存在则判断是否创建成功
         *
         * @param file 文件
         * @return `true`: 存在或创建成功<br></br>`false`: 不存在或创建失败
         */
        @JvmStatic
        fun createOrExistsDir(file: File?): Boolean {
            // 如果存在，是目录则返回true，是文件则返回false，不存在则返回是否创建成功
            return file != null && if (file.exists()) file.isDirectory else file.mkdirs()
        }


    }
}