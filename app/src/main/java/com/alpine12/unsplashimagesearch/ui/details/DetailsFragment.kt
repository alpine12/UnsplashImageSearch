package com.alpine12.unsplashimagesearch.ui.details

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Environment.DIRECTORY_PICTURES
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.alpine12.unsplashimagesearch.R
import com.alpine12.unsplashimagesearch.databinding.FragmentDetailBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

class DetailsFragment : Fragment(R.layout.fragment_detail), EasyPermissions.PermissionCallbacks {

    companion object {
        private const val REQUEST_WRITE_STORAGE = 124;
    }

    private val TAG = "DetailsFragment"
    private val args by navArgs<DetailsFragmentArgs>()

    private var imageName: String? = null
    private var imgView: View? = null
    private val imageFolder = "Unsplash"
    private lateinit var binding: FragmentDetailBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentDetailBinding.bind(view)

        binding.apply {
            val photo = args.photo

            val path: String =
                Environment.getExternalStoragePublicDirectory(DIRECTORY_PICTURES).toString()
            val file: File = File("$path/$imageFolder/${args.photo.id}.jpg")

            if (file.exists()) {
                Glide.with(this@DetailsFragment).load(file)
                    .into(imageView)

                progressBar.isVisible = false
                textViewCreator.isVisible = true
                buttonDownload.apply {
                    isVisible = true
                    isEnabled = false
                }
                textViewDescription.isVisible = photo.description != null

                Log.d(TAG, "onViewCreated: Is Opened")
            } else {

                Glide.with(this@DetailsFragment).load(photo.urls.full)
                    .error(R.drawable.ic_error)
                    .listener(object : RequestListener<Drawable> {
                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: Target<Drawable>?,
                            isFirstResource: Boolean
                        ): Boolean {
                            progressBar.isVisible = false
                            return false
                        }

                        override fun onResourceReady(
                            resource: Drawable?,
                            model: Any?,
                            target: Target<Drawable>?,
                            dataSource: DataSource?,
                            isFirstResource: Boolean
                        ): Boolean {
                            progressBar.isVisible = false
                            textViewCreator.isVisible = true
                            buttonDownload.isVisible = true
                            textViewDescription.isVisible = photo.description != null
                            return false
                        }
                    }).into(imageView)
                Log.d(TAG, "onViewCreated: tidak tersimpan")

            }

            textViewDescription.text = photo.description

            val uri = Uri.parse(photo.user.attributionUrl)
            val intent = Intent(Intent.ACTION_VIEW, uri)

            textViewCreator.apply {
                text = "Photo by ${photo.user.name} on Unsplash"
                setOnClickListener {
                    context.startActivity(intent)
                }
                paint.isUnderlineText = true
            }

            imageName = photo.id
            imgView = imageView

            buttonDownload.setOnClickListener {
                requestStorage()
            }
        }
    }

    private fun downloadImage() {
        val image = getBitmapFromView(imgView!!)

        if (android.os.Build.VERSION.SDK_INT >= 29) {
            val values = contentValues()
            values.put(MediaStore.Images.Media.DISPLAY_NAME, imageName)
            values.put(MediaStore.Images.Media.IS_PENDING, true)
            // RELATIVE_PATH and IS_PENDING are introduced in API 29.

            val uri: Uri? = context?.contentResolver?.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                values
            )
            if (uri != null) {
                saveImagesToStream(image, context?.contentResolver?.openOutputStream(uri))
                values.put(MediaStore.Images.Media.IS_PENDING, false)
                context?.contentResolver?.update(uri, values, null, null)
                Toast.makeText(context, "Tersimpan di ${uri.pathSegments}", Toast.LENGTH_SHORT)
                    .show()

                binding?.buttonDownload?.isEnabled = false

            }
        } else {
            val directory =
                File(
                    Environment.getExternalStorageDirectory()
                        .toString() + File.separator + imageFolder
                )
            // getExternalStorageDirectory is deprecated in API 29
            if (!directory.exists()) {
                directory.mkdir()
            }

            val fileName = "$imageName.jpg"
            val file = File(directory, fileName)
            saveImagesToStream(image, FileOutputStream(file))
            if (file.absolutePath != null) {
                val values = contentValues()
                values.put(MediaStore.Images.Media.DATA, file.absolutePath)
                // .DATA is deprecated in API 29
                context?.contentResolver?.insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    values
                )
            }
            Toast.makeText(context, "Tersimpan di ${file.absolutePath}", Toast.LENGTH_SHORT).show()
            binding?.buttonDownload?.isEnabled = false
        }
    }

    private fun contentValues(): ContentValues {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis() / 1000)
        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis())
        values.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/" + imageFolder)
        return values
    }

    private fun saveImagesToStream(bitmap: Bitmap, outputStream: OutputStream?) {
        if (outputStream != null) {
            try {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                outputStream.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun getBitmapFromView(view: View): Bitmap {
        return Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888).apply {
            Canvas(this).apply {
                view.draw(this)
            }
        }
    }

    private var isGranted = false

    @AfterPermissionGranted(REQUEST_WRITE_STORAGE)
    private fun requestStorage() {
        val permission = EasyPermissions.hasPermissions(
            requireContext(),
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
        )
        if (permission) {
            Toast.makeText(context, "Access", Toast.LENGTH_SHORT).show()
            downloadImage()
        } else {
            EasyPermissions.requestPermissions(
                this,
                "Minta tolong accept",
                REQUEST_WRITE_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )

        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        Log.d(TAG, "onPermissionsGranted: $requestCode : ${perms.size}")
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        Log.d(TAG, "onPermissionsDenied: $requestCode : ${perms.size}")
    }
}