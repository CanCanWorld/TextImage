package com.zrq.textimage

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.SeekBar
import androidx.core.content.FileProvider
import com.zrq.textimage.databinding.ActivityMainBinding
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var mBinding: ActivityMainBinding

    private var saveFile: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        initData()
        initEvent()
    }

    private fun initData() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE), 110)
        }
    }

    private fun initEvent() {
        mBinding.apply {
            btnJump.setOnClickListener {
                val intent = Intent(
                    Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                )
                startActivityForResult(intent, REQUEST_CODE_IMAGE)
            }
            image.setOnClickListener {
                mBinding.etCode.clearFocus()
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(mBinding.etCode.windowToken, 0)
            }
            image.setOnLongClickListener {
                if (saveFile == null) return@setOnLongClickListener false
                val intent = createShareIntent(this@MainActivity, saveFile!!)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(Intent.createChooser(intent, "Share Image"))
                false
            }
            seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                @SuppressLint("SetTextI18n")
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    tvSize.text = "字号：${progress + 1}"
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                }

            })
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CODE_IMAGE) {
                data?.data?.let { image ->
                    val filePathColumns = arrayOf(MediaStore.Images.Media.DATA)
                    val cursor: Cursor? = contentResolver.query(image, filePathColumns, null, null, null)
                    cursor?.let { c ->
                        c.moveToFirst()
                        val columnIndex: Int = c.getColumnIndex(filePathColumns[0])
                        val imagePath: String? = c.getString(columnIndex)
                        Log.d(TAG, "imagePath: $imagePath")
                        imagePath?.let { handleImage(imagePath) }
                        c.close()
                    }
                }
            }
        }
    }

    private fun handleImage(imagePath: String) {
        val loadingDialog = LoadingDialog(this)
        loadingDialog.show()
        Thread {
            val bitmap = BitmapFactory.decodeFile(imagePath)
            val textBitmap = ImageUtil.getTextBitmap(bitmap, mBinding.etCode.text.toString(), mBinding.seekBar.progress + 1)
            val format = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINA).format(Date())
            saveFile = saveBitmapToFile(this, textBitmap, "pic_$format.png")
            runOnUiThread {
                mBinding.image.setImageBitmap(textBitmap)
                loadingDialog.dismiss()
            }
        }.start()
    }

    private fun saveBitmapToFile(context: Context, bitmap: Bitmap, filename: String): File {
        val file = File(context.externalCacheDir, filename)
        val out = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        out.flush()
        out.close()
        return file
    }

    private fun createShareIntent(context: Context, file: File): Intent {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        return Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, uri)
            type = "image/png"
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    companion object {
        private const val TAG = "TextImageActivity"

        private const val REQUEST_CODE_IMAGE = 1
    }

}