package com.example.mystyle

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.mystyle.image.ucrop.GlideApp
import com.example.mystyle.image.ucrop.ImagePickerActivity
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.layout_toolbar_profile.*
import java.io.IOException


class MainActivity : AppCompatActivity() {

    private val REQUEST_IMAGE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setTitle("cat")

        loadProfileDefault()
        setListener()
        ImagePickerActivity.clearCache(this)
    }

    private fun setListener() {
        img_plus.setOnClickListener {
            onProfileImageClick()
        }

        img_profile.setOnClickListener {
            onProfileImageClick()
        }
    }

    private fun loadProfileDefault() {
        GlideApp.with(this).load(R.drawable.baseline_account_circle_black_48)
            .into(img_profile)
        img_profile.setColorFilter(ContextCompat.getColor(this, android.R.color.transparent))
    }

    fun onProfileImageClick(){
        Dexter.withActivity(this)
            .withPermissions(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {

                    //granted
                    if (report!!.areAllPermissionsGranted()) {
                        showImagePickerOption()
                    }

                    //denied
                    if(report.isAnyPermissionPermanentlyDenied){
                        showSettingsDialog()
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: MutableList<PermissionRequest>?,
                    token: PermissionToken?
                ) {
                    token?.continuePermissionRequest()
                }
            }).check()

    }

    private fun showSettingsDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.dialog_permission_title)
        builder.setMessage(R.string.dialog_permission_message)
        builder.setPositiveButton(R.string.go_to_settings){
            dialog, _ ->
            dialog.cancel()
            openSettings()
        }
    }

    private fun openSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", packageName, null)
        intent.setData(uri)
        startActivityForResult(intent, 101)

    }

    private fun showImagePickerOption() {
        ImagePickerActivity.showImagePickerOption(this, object : ImagePickerActivity.PickerOptionListener{
            override fun onTakeCameraSelected() {
                launchCameraIntent()
            }

            override fun onChooseGallerySelected() {
                launchGalleryIntent()
            }
        })
    }

    private fun launchGalleryIntent() {
        val intent = Intent(this@MainActivity, ImagePickerActivity::class.java)
        intent.putExtra(ImagePickerActivity.INTENT_IMAGE_PICKER_OPTION, ImagePickerActivity.REQUEST_GALLERY_IMAGE);

        // setting aspect ratio
        intent.putExtra(ImagePickerActivity.INTENT_LOCK_ASPECT_RATIO, true);
        intent.putExtra(ImagePickerActivity.INTENT_ASPECT_RATIO_X, 1); // 16x9, 1x1, 3:4, 3:2
        intent.putExtra(ImagePickerActivity.INTENT_ASPECT_RATIO_Y, 1);
        startActivityForResult(intent, REQUEST_IMAGE);

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_IMAGE) {
            if (resultCode == Activity.RESULT_OK) {
                val uri: Uri = data!!.getParcelableExtra("path")
                try {
                    // You can update this bitmap to your server
                    val bitmap =
                        MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
                    // loading profile image from local cache
                    loadProfile(uri.toString())
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun loadProfile(url: String) {

        GlideApp.with(this).load(url)
            .into(img_profile);
        img_profile.setColorFilter(ContextCompat.getColor(this, android.R.color.transparent));

}

    private fun launchCameraIntent() {
        val intent =Intent(this@MainActivity, ImagePickerActivity::class.java)
        intent.putExtra(ImagePickerActivity.INTENT_IMAGE_PICKER_OPTION, ImagePickerActivity.REQUEST_IMAGE_CAPTURE);

        // setting aspect ratio
        intent.putExtra(ImagePickerActivity.INTENT_LOCK_ASPECT_RATIO, true);
        intent.putExtra(ImagePickerActivity.INTENT_ASPECT_RATIO_X, 1); // 16x9, 1x1, 3:4, 3:2
        intent.putExtra(ImagePickerActivity.INTENT_ASPECT_RATIO_Y, 1);

        // setting maximum bitmap width and height
        intent.putExtra(ImagePickerActivity.INTENT_SET_BITMAP_MAX_WIDTH_HEIGHT, true);
        intent.putExtra(ImagePickerActivity.INTENT_BITMAP_MAX_WIDTH, 1000);
        intent.putExtra(ImagePickerActivity.INTENT_BITMAP_MAX_HEIGHT, 1000);

        startActivityForResult(intent, REQUEST_IMAGE)
    }


}
