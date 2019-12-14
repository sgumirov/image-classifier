/*
 * (c) 2019 by Shamil Gumirov
 * Licensed under GNU GPL 3.0
 */
package com.gumirov.shamil.whatsthat

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

class PermissionRequestFragment
  : Fragment()
{
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    if (!hasPermissions(requireContext())) {
      requestPermissions(PERMISSIONS_REQUIRED, PERMISSIONS_REQUEST_CODE)
    } else {
      findNavController().navigate(PermissionRequestFragmentDirections.actionPermissionsToCamera())
    }
  }

  override fun onRequestPermissionsResult(
    requestCode: Int, permissions: Array<String>, grantResults: IntArray
  ) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    if (requestCode == PERMISSIONS_REQUEST_CODE) {
      if (PackageManager.PERMISSION_GRANTED == grantResults.firstOrNull()) {
        Toast.makeText(context, "Permission request granted", Toast.LENGTH_LONG).show()
        findNavController().navigate(PermissionRequestFragmentDirections.actionPermissionsToCamera())
      } else {
        Toast.makeText(context, "Permission request denied", Toast.LENGTH_LONG).show()
      }
    }
  }

  companion object {
    private const val PERMISSIONS_REQUEST_CODE = 10
    private val PERMISSIONS_REQUIRED = arrayOf(Manifest.permission.CAMERA)

    fun hasPermissions(context: Context) = PERMISSIONS_REQUIRED.all {
      ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }
  }
}
