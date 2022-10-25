package service.remote.api

import com.beust.klaxon.Json


data class AppState(
    @Json("panelCount") val panelCount: Int,
    @Json("cpuTemp") val cpuTemp: Int,
    @Json("version") val version: String,
    @Json("status") val status: String,
    @Json("panelsSorted") val panelsSorted: Boolean,
    @Json("panelSortingActive") val panelSortingActive: Boolean,
    @Json("intresteConnected") val intresteConnected: Boolean,
    @Json("ledPanelConnected") val ledPanelConnected: Boolean,
    @Json("pingSign") val pingSign: String,
)