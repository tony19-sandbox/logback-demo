package com.github.tony19.logbackdemo

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.github.tony19.logbackdemo.databinding.FragmentFirstBinding
import org.slf4j.LoggerFactory

private const val PERMS_STORAGE = 1337

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonFirst.setOnClickListener {
            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
        }

        binding.buttonRequestPermission.setOnClickListener {
            requestStoragePermissions()
        }
        binding.buttonWriteLog.setOnClickListener {
            writeLog()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun writeLog() {
        if ((Build.VERSION.SDK_INT >= 30 && !hasAllFilesPermission()) || (Build.VERSION.SDK_INT < 30 && !hasStoragePermission())) {
            Toast.makeText(context, "Permission denied", Toast.LENGTH_SHORT).show()
            return
        }

        val logger = LoggerFactory.getLogger(javaClass.simpleName)
        logger.info("hello world")
        Toast.makeText(context, "Log written", Toast.LENGTH_SHORT).show()
    }

    private fun requestStoragePermissions(): Boolean {
        if (Build.VERSION.SDK_INT < 30) {
            if (hasStoragePermission()) {
                Toast.makeText(context, "Permission already granted", Toast.LENGTH_SHORT).show()
                return true
            }

            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE),
                PERMS_STORAGE
            )

            return false
        }

        if (hasAllFilesPermission()) {
            Toast.makeText(context, "Permission already granted", Toast.LENGTH_SHORT).show()
            return true
        }

        val uri = Uri.parse("package:${BuildConfig.APPLICATION_ID}")
        startActivity(Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, uri))
        return false
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun hasAllFilesPermission() = Environment.isExternalStorageManager()

    private fun hasStoragePermission() =
        checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                PackageManager.PERMISSION_GRANTED &&
        checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) ==
                PackageManager.PERMISSION_GRANTED

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == PERMS_STORAGE) {
            writeLog()
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

}