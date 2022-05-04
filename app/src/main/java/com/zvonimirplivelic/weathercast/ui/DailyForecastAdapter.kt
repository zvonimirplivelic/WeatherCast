package com.zvonimirplivelic.weathercast.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import com.zvonimirplivelic.weathercast.R
import com.zvonimirplivelic.weathercast.model.DetailedWeatherResponse
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.math.roundToInt

class DailyForecastAdapter: RecyclerView.Adapter<DailyForecastAdapter.DailyForecastViewHolder>() {
    inner class DailyForecastViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    private val differCallback =
        object : DiffUtil.ItemCallback<DetailedWeatherResponse.DailyWeather>() {
            override fun areItemsTheSame(
                oldItem: DetailedWeatherResponse.DailyWeather,
                newItem: DetailedWeatherResponse.DailyWeather
            ): Boolean {
                return oldItem.dt == newItem.dt
            }

            override fun areContentsTheSame(
                oldItem: DetailedWeatherResponse.DailyWeather,
                newItem: DetailedWeatherResponse.DailyWeather
            ): Boolean {
                return oldItem == newItem
            }
        }

    val differ = AsyncListDiffer(this, differCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DailyForecastViewHolder {
        return DailyForecastViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.daily_forecast_list_item,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: DailyForecastViewHolder, position: Int) {
        val dailyWeather = differ.currentList[position]
        holder.itemView.apply {
            val tvDayOfTheWeek: TextView = findViewById(R.id.tv_daily_date)
            val tvDailyMinMaxTemperature: TextView = findViewById(R.id.tv_daily_min_max_temperature)
            val ivDailyWeatherImage: ImageView = findViewById(R.id.iv_daily_weather_image)

            val weatherImageURLString =
                "https://openweathermap.org/img/wn/${dailyWeather.weather[0].icon}@2x.png"

            tvDayOfTheWeek.text = convertTime(dailyWeather.dt)

            tvDailyMinMaxTemperature.text = resources.getString(
                R.string.min_max_temperature_string_daily,
                convertTemperature(dailyWeather.temp.min),
                convertTemperature(dailyWeather.temp.max)
            )

            Picasso.get().load(weatherImageURLString).into(ivDailyWeatherImage)
        }
    }

    override fun getItemCount() = 7

    private fun convertTime(time: Int, format: String = "EEE dd.MM.yyyy."): String? {
        val instant =
            Instant.ofEpochSecond(time.toLong()).atZone(ZoneId.systemDefault()).toLocalDateTime()
        val formatter = DateTimeFormatter.ofPattern(format, Locale.ROOT)
        return instant.format(formatter)
    }

    private fun convertTemperature(value: Double): String {
        val convertedTemperature = (value - 273.15).roundToInt()
        return convertedTemperature.toString()
    }
}