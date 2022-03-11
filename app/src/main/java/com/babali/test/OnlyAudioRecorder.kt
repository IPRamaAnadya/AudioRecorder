import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class OnlyAudioRecorder private constructor(){

    companion object{
        private const val TAG:String = "OnlyAudioRecorder"
        private const val AudioSource = MediaRecorder.AudioSource.MIC//Student source
        private const val SampleRate = 16000//sampling rate
        private const val Channel = AudioFormat.CHANNEL_IN_MONO//Mono channel
        private const val EncodingType = AudioFormat.ENCODING_PCM_16BIT//data format
//        private val PCMPath = Environment.getExternalStorageDirectory().path.toString()+"/zzz/RawAudio.pcm"
//        private val WAVPath = Environment.getExternalStorageDirectory().path.toString()+"/zzz/FinalAudio.wav"

        var filePathPcm: File? = null
        var filePathWav: File? = null
        var stringPathPcm: String? = null
        var stringPathWav: String? = null
        private var PCMPath = "stringPathPcm"
        private var WAVPath = "stringPathWav"
        //Single example of double check
        val instance:OnlyAudioRecorder by lazy (mode = LazyThreadSafetyMode.SYNCHRONIZED){
            OnlyAudioRecorder()
        }
    }

    fun setpath(stringWav: String, stringPcm: String, fileWav: File, filePcm: File){
        filePathPcm = filePcm
        filePathWav = fileWav
        stringPathPcm = stringPcm
        stringPathWav = stringWav
        PCMPath = stringPathPcm as String
        WAVPath = stringPathWav as String
    }

    private var bufferSizeInByte:Int = 0//Minimum recording buffer
    private var audioRecorder:AudioRecord? = null//Recording object
    private var isRecord = false

    @SuppressLint("MissingPermission")
    private fun initRecorder() {//Initializing the audioRecord object

        bufferSizeInByte = AudioRecord.getMinBufferSize(SampleRate, Channel, EncodingType)
        audioRecorder = AudioRecord(AudioSource, SampleRate, Channel,
            EncodingType, bufferSizeInByte)
    }

    fun startRecord():Int {

        if (isRecord) {
            return -1
        } else{

            audioRecorder?: initRecorder()
            audioRecorder?.startRecording()
            isRecord = true

            AudioRecordToFile().start()
            return 0
        }
    }

    fun stopRecord() {

        audioRecorder?.stop()
        audioRecorder?.release()
        isRecord = false
        audioRecorder = null
    }

    @Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
    private fun writeDateTOFile() {

        var audioData = ByteArray(bufferSizeInByte)
        println("audioData     " + audioRecorder!!.read(audioData, 0, bufferSizeInByte).toString())
        val file = File(PCMPath)
        if (!file.parentFile.exists()) {
            file.parentFile.mkdirs()
        }
        if (file.exists()) {
            file.delete()
        }
        file.createNewFile()
        val out = BufferedOutputStream(FileOutputStream(file))
        var length = 0
        while (isRecord && audioRecorder!=null) {
            length = audioRecorder?.read(audioData, 0, bufferSizeInByte)  ?: 1280//Get audio data
            println("AudioRecord "+AudioRecord.ERROR_INVALID_OPERATION+AudioRecord.ERROR+AudioRecord.SUCCESS)
            if (AudioRecord.ERROR_INVALID_OPERATION != length) {
                out.write(audioData, 0, length)//write file
                out.flush()
            }
        }
        out.close()
    }

    //Converting pcm files to WAV files
    private fun copyWaveFile(pcmPath: String, wavPath: String) {

        var fileIn = FileInputStream(pcmPath)
        var fileOut = FileOutputStream(wavPath)
        val data = ByteArray(bufferSizeInByte)
        val totalAudioLen = fileIn.channel.size()
        val totalDataLen = totalAudioLen + 36
        writeWaveFileHeader(fileOut, totalAudioLen, totalDataLen)
        var count = fileIn.read(data, 0, bufferSizeInByte)
        while (count != -1) {
            fileOut.write(data, 0, count)
            fileOut.flush()
            count = fileIn.read(data, 0, bufferSizeInByte)
        }
        fileIn.close()
        fileOut.close()
    }

    //Add file header in WAV format
    private fun writeWaveFileHeader(out:FileOutputStream , totalAudioLen:Long,
                                    totalDataLen:Long){

        val channels = 1
        val byteRate = 16 * SampleRate * channels / 8
        val header = ByteArray(44)
        header[0] = 'R'.code.toByte()
        header[1] = 'I'.code.toByte()
        header[2] = 'F'.code.toByte()
        header[3] = 'F'.code.toByte()
        header[4] = (totalDataLen and 0xff).toByte()
        header[5] = (totalDataLen shr 8 and 0xff).toByte()
        header[6] = (totalDataLen shr 16 and 0xff).toByte()
        header[7] = (totalDataLen shr 24 and 0xff).toByte()
        header[8] = 'W'.code.toByte()
        header[9] = 'A'.code.toByte()
        header[10] = 'V'.code.toByte()
        header[11] = 'E'.code.toByte()
        header[12] = 'f'.code.toByte() // 'fmt ' chunk
        header[13] = 'm'.code.toByte()
        header[14] = 't'.code.toByte()
        header[15] = ' '.code.toByte()
        header[16] = 16 // 4 bytes: size of 'fmt ' chunk
        header[17] = 0
        header[18] = 0
        header[19] = 0
        header[20] = 1 // format = 1
        header[21] = 0
        header[22] = channels.toByte()
        header[23] = 0
        header[24] = (SampleRate and 0xff).toByte()
        header[25] = (SampleRate shr 8 and 0xff).toByte()
        header[26] = (SampleRate shr 16 and 0xff).toByte()
        header[27] = (SampleRate shr 24 and 0xff).toByte()
        header[28] = (byteRate and 0xff).toByte()
        header[29] = (byteRate shr 8 and 0xff).toByte()
        header[30] = (byteRate shr 16 and 0xff).toByte()
        header[31] = (byteRate shr 24 and 0xff).toByte()
        header[32] = (2 * 16 / 8).toByte() // block align
        header[33] = 0
        header[34] = 16 // bits per sample
        header[35] = 0
        header[36] = 'd'.code.toByte()
        header[37] = 'a'.code.toByte()
        header[38] = 't'.code.toByte()
        header[39] = 'a'.code.toByte()
        header[40] = (totalAudioLen and 0xff).toByte()
        header[41] = (totalAudioLen shr 8 and 0xff).toByte()
        header[42] = (totalAudioLen shr 16 and 0xff).toByte()
        header[43] = (totalAudioLen shr 24 and 0xff).toByte()
        out.write(header, 0, 44)
    }

    private inner class AudioRecordToFile : Thread() {
        @Volatile
        var running = true

        override fun run() {
            super.run()

            writeDateTOFile()
            copyWaveFile(PCMPath, WAVPath)
            if (!running) return;
        }
    }
}