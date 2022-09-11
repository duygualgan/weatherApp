package com.example.weatherapp

import android.annotation.SuppressLint
import android.location.Geocoder
import android.location.Location
import android.os.AsyncTask
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract
import android.view.View
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.weatherapp.adapter.WeatherAdapter
import com.example.weatherapp.databinding.ActivityMainBinding
import com.example.weatherapp.model.WeatherData
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import org.json.JSONObject
import java.lang.Exception
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding

    var CITY :String =""
    var API :String = "3ba4f22d70d89cf0b2635e1d9106bfdf"

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private lateinit var weatherList : ArrayList<WeatherData>
    private lateinit var weatherAdapter: WeatherAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
       // setContentView (view)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        obtainLocation()

        binding.weatherBtn.setOnClickListener {
            var city_name = binding.cityName.text.toString()
            if (!city_name.equals("")){
                CITY = city_name
                WeatherTask().execute()
            }
        }

    }


    @SuppressLint("MissingPermission")
    private fun obtainLocation() {

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location : Location? ->
                var current_city_name = getCityName(location?.longitude, location?.latitude)
                CITY = current_city_name
                WeatherTask().execute() }

    }

    private fun getCityName(longitude: Double?, latitude: Double?): String {
        var cityName = "not found"
        val gcd : Geocoder = Geocoder(this@MainActivity,Locale.getDefault())

        try {

            val address = gcd.getFromLocation(latitude!!, longitude!!,10)
            for (adr in address){
                if (adr !=null){
                    val city = adr.locality
                    if(city != null && !city.equals(""))
                        cityName = city
                }
            }
        }
        catch (e:Exception){
            e.printStackTrace()
        }

        return cityName
    }

    inner class WeatherTask () : AsyncTask<String, Void, String>() {

        override fun onPreExecute() {
            super.onPreExecute()
            binding.loader.visibility =View.VISIBLE
            binding.mainContainer.visibility = View.GONE
            binding.errorText.visibility = View.GONE


        }

        override fun doInBackground(vararg p0: String?): String {

            var response : String?

            try {
                response = URL ("https://api.openweathermap.org/data/2.5/weather?p=${CITY.lowercase()}&units=metric&appid=$API").readText(
                    Charsets.UTF_8
                )
            }
            catch (e: Exception){
                response = null
            }

            return response !!
        }

        @RequiresApi (Build.VERSION_CODES.N)
        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            try {
                val jsonObj = JSONObject(result)
                val main = jsonObj.getJSONObject("main")
                val sys = jsonObj.getJSONObject("sys")
                val wind = jsonObj.getJSONObject("wind")
                val weather = jsonObj.getJSONArray("weather").getJSONObject(0)


                val updatedAt :Long = jsonObj.getLong("dt")
                val updatedAtText = "updated at : "+ SimpleDateFormat("dd/MM/yyyy hh : mm a", Locale.getDefault())
                    .format(Date(updatedAt * 1000))
                val temp = main.getString("temp") + "°C"
                val tempMin = "Min Temp :"+main.getString("temp_min")+ "°C"
                val tempMax = "Max Temp :"+main.getString("temp_max")+ "°C"
                val pressure = main.getString("pressure")
                val humidity = main.getString("humidity")
                val sunrise = sys.getLong("sunrise")
                val sunset = sys.getLong("sunset")
                val windSpeed = wind.getString("speed")

                val weatherDescription = weather.getString("description")

                val maddress = jsonObj.getString("name")+ "," + sys.getString("country")

                binding.address.text = maddress
                binding.updatedAt.text = updatedAtText
                binding.statuss.text = weatherDescription.capitalize()

                binding.temps.text = temp
                binding.tempMin.text = tempMin
                binding.tempMax.text = tempMax

                weatherList = ArrayList()

                weatherAdapter = WeatherAdapter(this@MainActivity, weatherList)

                binding.tempInfoRec.layoutManager = LinearLayoutManager(
                    this@MainActivity,
                    LinearLayoutManager.HORIZONTAL,
                    false)

                binding.tempInfoRec.adapter = weatherAdapter

                weatherList.add(
                    WeatherData(
                    R.drawable.sunrise,
                    "Sunrise",
                    SimpleDateFormat("hh:mm a", Locale.getDefault())
                        .format(Date(sunrise*1000))))

                weatherList.add(
                    WeatherData(
                        R.drawable.sunset,
                        "Sunset",
                        SimpleDateFormat("hh:mm a", Locale.getDefault())
                            .format(Date(sunset*1000))))

                weatherList.add(
                    WeatherData(
                        R.drawable.wind,
                        "Wind",
                        windSpeed))

                weatherList.add(
                    WeatherData(
                        R.drawable.pressure,
                        "Pressure",
                        pressure))

                weatherList.add(
                    WeatherData(
                        R.drawable.humidity,
                        "Humidity",
                        humidity))

                binding.loader.visibility = View.GONE
                binding.mainContainer.visibility = View.VISIBLE
            }
            catch (e:Exception){
                binding.loader.visibility = View.GONE
                binding.errorText.visibility = View.VISIBLE
            }
        }

    }
}


