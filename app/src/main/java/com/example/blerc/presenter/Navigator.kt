package com.example.blerc.presenter

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController


@Composable
fun Navigator(
   context: Context
){
   val navController = rememberNavController()
   NavHost(navController = navController, startDestination = Screen.MainScreen.route){
      composable(Screen.MainScreen.route){
         Main(navController)
      }

      composable(Screen.BleEnable.route){
         BluetoothEnableScreen(context)
      }
   }
}


sealed class Screen (val route: String){
   data object MainScreen: Screen("MainScreen")
   data object BleEnable: Screen("BleEnableScreen")
}
