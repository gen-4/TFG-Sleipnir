package com.android.sleipnir

import android.content.Context
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.gms.maps.model.LatLng
import io.data2viz.charts.chart.Chart
import io.data2viz.charts.chart.chart
import io.data2viz.charts.chart.discrete
import io.data2viz.charts.chart.mark.area
import io.data2viz.charts.chart.mark.line
import io.data2viz.charts.chart.quantitative
import io.data2viz.geom.Size
import io.data2viz.viz.VizContainerView
import org.json.JSONArray

class AltitudeChartActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_altitude_chart)
        val strPoints = intent.getStringExtra("points")
        val jsonPoints = JSONArray(strPoints)
        val altitudeList = ArrayList<Altitude>()
        val previousLoc = Location("")
        var distance = 0f

        for (i in 0 until jsonPoints.length()) {
            val point = jsonPoints.getJSONObject(i)

            if (i != 0) {
                val currentLoc = Location("")
                currentLoc.latitude = point.getDouble("y_coord")
                currentLoc.longitude = point.getDouble("x_coord")
                distance += previousLoc.distanceTo(currentLoc)
            }
            previousLoc.latitude = point.getDouble("y_coord")
            previousLoc.longitude = point.getDouble("x_coord")

            altitudeList.add(Altitude(distance.toDouble(), point.getDouble("altitude")))
        }

        setContentView(AltitudeChart(this, altitudeList))
    }
}

class AltitudeChart(context: Context, altitudeData: List<Altitude>) : VizContainerView(context) {

    private val chart: Chart<Altitude> = chart(altitudeData) {
        size = Size(vizSize, vizSize)
        title = context.getString(R.string.altitude_chat_title)

        // Create a discrete dimension for the year of the census
        val distance = quantitative({ domain.distance }) {
            name = context.getString(R.string.altitude_chat_x)
        }

        // Create a continuous numeric dimension for the population
        val altitude = quantitative({ domain.altitude }) {
            name = context.getString(R.string.altitude_chat_y)
        }

        // Using a discrete dimension for the X-axis and a continuous one for the Y-axis
        line(distance, altitude)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        chart.size = Size(vizSize, vizSize * h / w)
    }
}

const val vizSize = 500.0

data class Altitude(val distance: Double, val altitude: Double)