package com.babali.test

import android.Manifest
import android.content.ContextWrapper
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.widget.Button
import android.widget.TextView
import androidx.core.app.ActivityCompat
import java.io.File

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.WRITE_EXTERNAL_STORAGE), 111)
        }

        val audioRecord = OnlyAudioRecorder.instance

        val filePcm = getFilePath2("RawAudio.pcm")
        val fileWav = getFilePath2("FinalAudio.wav")
        val stringPcm = getStringPath("RawAudio.pcm")
        val stringWav = getStringPath("FinalAudio.wav")

        audioRecord.setpath(stringWav,stringPcm,fileWav,filePcm)

        findViewById<Button>(R.id.btn_rec).setOnClickListener {
            audioRecord.startRecord()
            findViewById<TextView>(R.id.textView).text = "Start Recording"
        }
        findViewById<Button>(R.id.btn_stop).setOnClickListener {
            findViewById<TextView>(R.id.textView).text = "Stop Recording"
            audioRecord.stopRecord()
        }
    }

    private fun getStringPath(filename:String): String {
        val contextWrapper = ContextWrapper(this)
        val musicDir: File? = contextWrapper.getExternalFilesDir(Environment.DIRECTORY_MUSIC)
        val file =  File(musicDir, filename)
        return file.path
    }

    private fun getFilePath2(filename: String): File {
        val contextWrapper = ContextWrapper(this)
        val musicDir: File? = contextWrapper.getExternalFilesDir(Environment.DIRECTORY_MUSIC)
        val file =  File(musicDir, filename)
        return file
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 111 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            // TODO: 04/03/22
        }
    }
}