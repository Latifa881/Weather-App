package com.example.weatherapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import kotlinx.coroutines.*
import org.json.JSONObject
import java.lang.Exception
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt


class MainActivity : AppCompatActivity() {
    lateinit var tvCityCountry: TextView
    lateinit var tvUpdate: TextView
    lateinit var tvStatus: TextView
    lateinit var tvTemperature: TextView
    lateinit var tvSunrise: TextView
    lateinit var tvSunset: TextView
    lateinit var tvWind: TextView
    lateinit var tvPressure: TextView
    lateinit var tvHumidity: TextView
    lateinit var llRefresh: LinearLayout
    lateinit var llZip: LinearLayout
    lateinit var llWeatherDetails:LinearLayout
    lateinit var etZip: EditText
    lateinit var etCityName:EditText
    lateinit var btZip: Button
    lateinit var tvLowTemperature:TextView
    lateinit var tvHighTemperature:TextView
    lateinit var llFailureFetch:LinearLayout
    lateinit var btRetry:Button

     var isZipCode=true
    var cityName="dammam"
    var zipCode = "10001"//the Zip code for New York
    val API_KEY="f2763f64328617a339894577e9107052"
    //https://api.openweathermap.org/data/2.5/weather?q=Dammam&appid=f2763f64328617a339894577e9107052
    //https://api.openweathermap.org/data/2.5/weather?zip=10001&units=metric&appid=f2763f64328617a339894577e9107052
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvCityCountry = findViewById(R.id.tvCityCountry)
        tvUpdate = findViewById(R.id.tvUpdate)
        tvStatus = findViewById(R.id.tvStatus)
        tvTemperature = findViewById(R.id.tvTemperature)
        tvSunrise = findViewById(R.id.tvSunrise)
        tvSunset = findViewById(R.id.tvSunset)
        tvWind = findViewById(R.id.tvWind)
        tvPressure = findViewById(R.id.tvPressure)
        tvHumidity = findViewById(R.id.tvHumidity)
        llRefresh = findViewById(R.id.llRefresh)
        llZip = findViewById(R.id.llZIP)
        llWeatherDetails=findViewById(R.id.llWeatherDetails)
        etZip = findViewById(R.id.etZIP)
        etCityName=findViewById(R.id.etCityName)
        btZip = findViewById(R.id.btZIP)
        tvLowTemperature=findViewById(R.id.tvLowTemperature)
        tvHighTemperature=findViewById(R.id.tvHighTemperature)
        llFailureFetch=findViewById(R.id.llFailureFetch)
        btRetry=findViewById(R.id.btRetry)

        requestApi()
        //Allow the user to enter a new Zip Code
        tvCityCountry.setOnClickListener {
            llWeatherDetails.visibility=View.GONE
            llZip.visibility=View.VISIBLE
        }

        btZip.setOnClickListener {
            val city_Name=etCityName.text.toString()
            val zip_Code=etZip.text.toString()
            when{
                zip_Code.isEmpty()&&city_Name.isEmpty()->{ Toast.makeText(this, "Enter the  Zip code/City name", Toast.LENGTH_SHORT).show()}
                zip_Code.isNotEmpty()&&city_Name.isNotEmpty()->{Toast.makeText(this, "You can't choose both of them", Toast.LENGTH_SHORT).show()
                    etZip.text.clear()
                    etCityName.text.clear() }
              else ->{
                  if(zip_Code.isNotEmpty())
                  {
                      isZipCode=true
                      zipCode = etZip.text.toString()
                  }else{
                      isZipCode=false
                      cityName=etCityName.text.toString()

                  }
                  llWeatherDetails.visibility=View.VISIBLE
                  llZip.visibility=View.GONE
                  requestApi()
                  etZip.text.clear()
                  etCityName.text.clear()
              }

            }



        }
        btRetry.setOnClickListener {
            llWeatherDetails.visibility=View.VISIBLE
            llFailureFetch.visibility=View.GONE
            zipCode = "10001"
             cityName="dammam"
            requestApi()
        }


    }
    private fun requestApi()
    {

        CoroutineScope(Dispatchers.IO).launch {
            updateStatus(-1)
            val data = async {

                fetchWeatherData()

            }.await()

            if (data.isNotEmpty())
            {
                updateStatus(0)
                updateWeatherData(data)
            }else{
                updateStatus(1)
            }

        }

    }
    private fun fetchWeatherData():String{
        var weatherUrl=""
        if(isZipCode){
            weatherUrl="https://api.openweathermap.org/data/2.5/weather?zip=$zipCode&units=metric&appid=$API_KEY"
        }else{

         weatherUrl="https://api.openweathermap.org/data/2.5/weather?q=$cityName&appid=$API_KEY"}

        var response=""
        try {
            response = URL(weatherUrl).readText(Charsets.UTF_8)

        }catch (e: Exception)
        {
            println("Error $e")

        }
        return response

    }
    private suspend fun updateWeatherData(data:String)
    {
        withContext(Dispatchers.Main)
        {

            val jsonObject = JSONObject(data)
            val cityName= jsonObject.getString("name")
            val updateDate=convertLongToTime(jsonObject.getLong("dt"),"d/MM/yyyy hh:mm a")
            val main = jsonObject.getJSONObject("main")
                val temp=main.getDouble("temp")
                val temp_min=main.getString("temp_min")
                val temp_max=main.getString("temp_max")
                val pressure=main.getString("pressure")
                val humidity=main.getString("humidity")
            val sys = jsonObject.getJSONObject("sys")
                val country=sys.getString("country")
                val sunrise=convertLongToTime(sys.getLong("sunrise"),"HH:mm a")
                val sunset=convertLongToTime(sys.getLong("sunset"),"HH:mm a")
            val weatherStatus=jsonObject.getJSONArray("weather").getJSONObject(0).getString("description").capitalize()

            val wind=jsonObject.getJSONObject("wind").getString("speed")

            tvCityCountry.text="$cityName,$country"
            tvUpdate.text=updateDate
            tvStatus.text=weatherStatus
            tvTemperature.text=temp.toInt().toString()+"°C" //Take the integer part only 16.25°C ->16°C
            tvLowTemperature.text="Low: $temp_min°C"
            tvHighTemperature.text="High: $temp_max°C"
            tvSunrise.text=sunrise
            tvSunset.text=sunset
            tvWind.text=wind
            tvPressure.text=pressure
            tvHumidity.text=humidity
            llRefresh.setOnClickListener{requestApi()}
            tvTemperature.setOnClickListener {
                val currentTemp=tvTemperature.text.toString()
                //Take the number only
                var tempNumber=currentTemp.substring(0,currentTemp.indexOf("°"))
                //check if the number is in C
                if(currentTemp.substring(currentTemp.indexOf("°")+1)=="C"){
                    tvTemperature.text=convertCelsiusToFahrenheit(tempNumber.toInt())+"°F"
                }else{
                    tvTemperature.text=convertFahrenheitToCelsius(tempNumber.toInt())+"°C"
                }
            }


        }

    }
    fun convertLongToTime(time: Long,pattern:String): String {
        val date = Date(time*1000)
        val dateFormat = SimpleDateFormat(pattern,Locale.ENGLISH)
        return dateFormat.format(date)
    }
    fun convertCelsiusToFahrenheit(celsius:Int):String{
        return ((celsius * 9/5.0) + 32).roundToInt().toString()

    }
    fun convertFahrenheitToCelsius(fahrenheit:Int):String{
        return ((fahrenheit - 32)* 5/9.0).roundToInt().toString()

    }
    private suspend fun updateStatus(state: Int){
        //states: -1 = loading, 0 = loaded, 1 = error
        withContext(Dispatchers.Main){
            when{
                state <0-> {
                    llFailureFetch.visibility=View.GONE
                    llWeatherDetails.visibility=View.GONE
                }
                state == 0 -> {

                    llWeatherDetails.visibility = View.VISIBLE
                }
                state > 0 -> {
                    llFailureFetch.visibility = View.VISIBLE
                }
            }
        }
    }
}
