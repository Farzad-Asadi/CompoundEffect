package com.example.compoundeffectV1_01.utils

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Abc
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.AccessAlarm
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.AdUnits
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.AddAlert
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.AirplanemodeActive
import androidx.compose.material.icons.filled.AirportShuttle
import androidx.compose.material.icons.filled.AllInclusive
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.filled.Anchor
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Approval
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Architecture
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.AutoDelete
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.BakeryDining
import androidx.compose.material.icons.filled.Balance
import androidx.compose.material.icons.filled.Bathtub
import androidx.compose.material.icons.filled.BeachAccess
import androidx.compose.material.icons.filled.Bed
import androidx.compose.material.icons.filled.Biotech
import androidx.compose.material.icons.filled.Blender
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Boy
import androidx.compose.material.icons.filled.Brightness5
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CandlestickChart
import androidx.compose.material.icons.filled.Cases
import androidx.compose.material.icons.filled.Castle
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Chair
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Deck
import androidx.compose.material.icons.filled.Desk
import androidx.compose.material.icons.filled.Dining
import androidx.compose.material.icons.filled.EnergySavingsLeaf
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.Fence
import androidx.compose.material.icons.filled.FireExtinguisher
import androidx.compose.material.icons.filled.Garage
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Hardware
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.House
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LocalMall
import androidx.compose.material.icons.filled.LunchDining
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.ModeNight
import androidx.compose.material.icons.filled.OutdoorGrill
import androidx.compose.material.icons.filled.Outlet
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Shower
import androidx.compose.material.icons.filled.Sports
import androidx.compose.material.icons.filled.SportsBasketball
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.SportsTennis
import androidx.compose.material.icons.filled.SportsVolleyball
import androidx.compose.material.icons.filled.TableBar
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Vaccines
import androidx.compose.material.icons.filled.Villa
import androidx.compose.material.icons.filled.Warehouse
import androidx.compose.material.icons.filled.Window
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector


data class IconOption(val key: String, val icon: ImageVector)
data class IconSection(val title: String, val options: List<IconOption>)


fun buildIconSections(): List<IconSection> = listOf(
    IconSection(
        title = "Buildings & furniture",
        options = listOf(
            IconOption("Home", Icons.Filled.Home),
            IconOption("Apartment", Icons.Filled.Apartment),
            IconOption("Business", Icons.Filled.Business),
            IconOption("Warehouse", Icons.Filled.Warehouse),
        )
    ),
    IconSection(
        title = "People",
        options = listOf(
            IconOption("Person", Icons.Filled.Person),
            IconOption("School", Icons.Filled.School),
            IconOption("Engineering", Icons.Filled.Engineering),
            IconOption("Groups", Icons.Filled.Groups),
        )
    ),
    IconSection(
        title = "Food & cooking",
        options = listOf(
            IconOption("Restaurant", Icons.Filled.Restaurant),
            IconOption("LocalCafe", Icons.Filled.LocalCafe),
            IconOption("LunchDining", Icons.Filled.LunchDining),
            IconOption("BakeryDining", Icons.Filled.BakeryDining),
        )
    ),
    IconSection(
        title = "Shopping & clothes",
        options = listOf(
            IconOption("ShoppingCart", Icons.Filled.ShoppingCart),
            IconOption("Checkroom", Icons.Filled.Checkroom),
            IconOption("LocalMall", Icons.Filled.LocalMall),
            IconOption("Sell", Icons.Filled.Sell),
        )
    ),
    IconSection(
        title = "Sport",
        options = listOf(
            IconOption("SportsSoccer", Icons.Filled.SportsSoccer),
            IconOption("SportsBasketball", Icons.Filled.SportsBasketball),
            IconOption("SportsTennis", Icons.Filled.SportsTennis),
            IconOption("SportsVolleyball", Icons.Filled.SportsVolleyball),
        )
    ),
    IconSection(
        title = "Medicine & health",
        options = listOf(
            IconOption("MedicalServices", Icons.Filled.MedicalServices),
            IconOption("LocalHospital", Icons.Filled.LocalHospital),
            IconOption("Vaccines", Icons.Filled.Vaccines),
            IconOption("HealthAndSafety", Icons.Filled.HealthAndSafety),
        )
    ),
)

fun iconFromKey(key: String): ImageVector {
    return when (key) {
        "Home" -> Icons.Filled.Home
        "Apartment" -> Icons.Filled.Apartment
        "Business" -> Icons.Filled.Business
        "Warehouse" -> Icons.Filled.Warehouse

        "Person" -> Icons.Filled.Person
        "School" -> Icons.Filled.School
        "Engineering" -> Icons.Filled.Engineering
        "Groups" -> Icons.Filled.Groups

        "Restaurant" -> Icons.Filled.Restaurant
        "LocalCafe" -> Icons.Filled.LocalCafe
        "LunchDining" -> Icons.Filled.LunchDining
        "BakeryDining" -> Icons.Filled.BakeryDining

        "ShoppingCart" -> Icons.Filled.ShoppingCart
        "Checkroom" -> Icons.Filled.Checkroom
        "LocalMall" -> Icons.Filled.LocalMall
        "Sell" -> Icons.Filled.Sell

        "SportsSoccer" -> Icons.Filled.SportsSoccer
        "SportsBasketball" -> Icons.Filled.SportsBasketball
        "SportsTennis" -> Icons.Filled.SportsTennis
        "SportsVolleyball" -> Icons.Filled.SportsVolleyball

        "MedicalServices" -> Icons.Filled.MedicalServices
        "LocalHospital" -> Icons.Filled.LocalHospital
        "Vaccines" -> Icons.Filled.Vaccines
        "HealthAndSafety" -> Icons.Filled.HealthAndSafety

        else -> Icons.Filled.Category
    }
}





















val topic_iconMap= mapOf(
    "ForAppOnly" to mapOf(
        "Icons.Filled.AccountTree" to Icons.Filled.AccountTree,    // for Route
        "Icons.Filled.QuestionMark" to Icons.Filled.QuestionMark,
    ),
    "building & furniture" to mapOf(
        "Icons.Filled.House" to Icons.Filled.House,
        "Icons.Filled.Chair" to Icons.Filled.Chair,
        "Icons.Filled.Bed" to Icons.Filled.Bed,
        "Icons.Filled.Kitchen" to Icons.Filled.Kitchen,
        "Icons.Filled.Window" to Icons.Filled.Window,
        "Icons.Filled.Garage" to Icons.Filled.Garage,
        "Icons.Filled.Villa" to Icons.Filled.Villa,
        "Icons.Filled.OutdoorGrill" to Icons.Filled.OutdoorGrill,
        "Icons.Filled.Fence" to Icons.Filled.Fence,
        "Icons.Filled.Desk" to Icons.Filled.Desk,
        "Icons.Filled.Apartment" to Icons.Filled.Apartment,
        "Icons.Filled.Castle" to Icons.Filled.Castle,
    ),
    "people" to  mapOf(
        "Icons.Filled.AccountCircle" to Icons.Filled.AccountCircle,
        "Icons.Filled.Accessibility" to Icons.Filled.Accessibility,
        "Icons.Filled.Boy" to Icons.Filled.Boy,
    ),
    "food & cooking" to  mapOf(
        "Icons.Filled.Coffee" to Icons.Filled.Coffee,
        "Icons.Filled.Dining" to Icons.Filled.Dining,
        "Icons.Filled.Blender" to Icons.Filled.Blender,
        "Icons.Filled.Cake" to Icons.Filled.Cake,
    ),
    "shopping & clothes" to  mapOf(
        "Icons.Filled.Checkroom" to Icons.Filled.Checkroom,
    ),
    "sport" to  mapOf(
        "Icons.Filled.Sports" to Icons.Filled.Sports,
    ),
    "medicine & health" to  mapOf(
        "Icons.Filled.Shower" to Icons.Filled.Shower,
        "Icons.Filled.Bathtub" to Icons.Filled.Bathtub,
    ),
    "finance" to  mapOf(
        "Icons.Filled.AccountBalance" to Icons.Filled.AccountBalance,
        "Icons.Filled.AccountBalanceWallet" to Icons.Filled.AccountBalanceWallet,
        "Icons.Filled.Approval" to Icons.Filled.Approval,
        "Icons.Filled.Balance" to Icons.Filled.Balance,
        "Icons.Filled.CandlestickChart" to Icons.Filled.CandlestickChart,
        "Icons.Filled.Cases" to Icons.Filled.Cases,
    ),
    "science & education" to  mapOf(
        "Icons.Filled.AutoStories" to Icons.Filled.AutoStories,
        "Icons.Filled.Biotech" to Icons.Filled.Biotech,
    ),
    "transport" to  mapOf(
        "Icons.Filled.Agriculture" to Icons.Filled.Agriculture,
        "Icons.Filled.AirplanemodeActive" to Icons.Filled.AirplanemodeActive,
        "Icons.Filled.AirportShuttle" to Icons.Filled.AirportShuttle,
    ),
    "devices" to  mapOf(
        "Icons.Filled.Thermostat" to Icons.Filled.Thermostat,
        "Icons.Filled.EnergySavingsLeaf" to Icons.Filled.EnergySavingsLeaf,
        "Icons.Filled.Tv" to Icons.Filled.Tv,
        "Icons.Filled.AccessAlarm" to Icons.Filled.AccessAlarm,
        "Icons.Filled.AdUnits" to Icons.Filled.AdUnits,
        "Icons.Filled.AddAPhoto" to Icons.Filled.AddAPhoto,
        "Icons.Filled.AddAlert" to Icons.Filled.AddAlert,
        "Icons.Filled.Computer" to Icons.Filled.Computer,
        "Icons.Filled.ContentCut" to Icons.Filled.ContentCut,
    ),
    "software" to  mapOf(
        "Icons.Filled.Android" to Icons.Filled.Android,
        "Icons.Filled.Apps" to Icons.Filled.Apps,
        "Icons.Filled.Backup" to Icons.Filled.Backup,
        "Icons.Filled.Bluetooth" to Icons.Filled.Bluetooth,
    ),
    "animals" to  mapOf(

    ),
    "nature" to  mapOf(
        "Icons.Filled.Grass" to Icons.Filled.Grass,
        "Icons.Filled.Deck" to Icons.Filled.Deck,
        "Icons.Filled.ModeNight" to Icons.Filled.ModeNight,
        "Icons.Filled.TableBar" to Icons.Filled.TableBar,
        "Icons.Filled.BeachAccess" to Icons.Filled.BeachAccess,
        "Icons.Filled.Brightness5" to Icons.Filled.Brightness5,
        "Icons.Filled.Cloud" to Icons.Filled.Cloud,
    ),
    "tools" to  mapOf(
        "Icons.Filled.Hardware" to Icons.Filled.Hardware,
        "Icons.Filled.FireExtinguisher" to Icons.Filled.FireExtinguisher,
        "Icons.Filled.Brush" to Icons.Filled.Brush,
        "Icons.Filled.Build" to Icons.Filled.Build,
        "Icons.Filled.CameraAlt" to Icons.Filled.CameraAlt,
        "Icons.Filled.CleaningServices" to Icons.Filled.CleaningServices,
    ),
    "signs" to  mapOf(
        "Icons.Filled.Air" to Icons.Filled.Air,
        "Icons.Filled.Abc" to Icons.Filled.Abc,
        "Icons.Filled.AcUnit" to Icons.Filled.AcUnit,
        "Icons.Filled.AccountTree" to Icons.Filled.AccountTree,
        "Icons.Filled.AddAlert" to Icons.Filled.AddAlert,
        "Icons.Filled.AllInclusive" to Icons.Filled.AllInclusive,
        "Icons.Filled.Anchor" to Icons.Filled.Anchor,
        "Icons.Filled.AlternateEmail" to Icons.Filled.AlternateEmail,
        "Icons.Filled.Architecture" to Icons.Filled.Architecture,
        "Icons.Filled.AttachFile" to Icons.Filled.AttachFile,
        "Icons.Filled.AttachMoney" to Icons.Filled.AttachMoney,
        "Icons.Filled.Audiotrack" to Icons.Filled.Audiotrack,
        "Icons.Filled.AutoDelete" to Icons.Filled.AutoDelete,
        "Icons.Filled.AutoFixHigh" to Icons.Filled.AutoFixHigh,
        "Icons.Filled.Call" to Icons.Filled.Call,
        "Icons.Filled.Celebration" to Icons.Filled.Celebration,
    ),
    "smiles" to  mapOf(
        "Icons.Filled.Outlet" to Icons.Filled.Outlet,
    ),
)


val colorsOfCategory :List<Color> = listOf(
    Color(0xFFF44336),
    Color(0xFFF44D40),
    Color(0xFFF4574A),
    Color(0xFFF46B5E),

    Color(0xFFE91E63),
    Color(0xFFE91E63),
    Color(0xFFE91E63),
    Color(0xFFE91E63),

    Color(0xFF9C27B0),
    Color(0xFF9C27B0),
    Color(0xFF9C27B0),
    Color(0xFF9C27B0),

    Color(0xFF673AB7),
    Color(0xFF673AB7),
    Color(0xFF673AB7),
    Color(0xFF673AB7),

    Color(0xFF3F51B5),
    Color(0xFF3F51B5),
    Color(0xFF3F51B5),
    Color(0xFF3F51B5),

    Color(0xFF2196F3),
    Color(0xFF2196F3),
    Color(0xFF2196F3),
    Color(0xFF2196F3),

    Color(0xFF00BCD4),
    Color(0xFF00BCD4),
    Color(0xFF00BCD4),
    Color(0xFF00BCD4),

    Color(0xFF009688),
    Color(0xFF009688),
    Color(0xFF009688),
    Color(0xFF009688),

    Color(0xFF4CAF50),
    Color(0xFF4CAF50),
    Color(0xFF4CAF50),
    Color(0xFF4CAF50),

    Color(0xFFCDDC39),
    Color(0xFFCDDC39),
    Color(0xFFCDDC39),
    Color(0xFFCDDC39),

    Color(0xFFFF9800),
    Color(0xFFFF9800),
    Color(0xFFFF9800),
    Color(0xFFFF9800),

    Color(0xFFFF5722),
    Color(0xFFFF5722),
    Color(0xFFFF5722),
    Color(0xFFFF5722),

)

