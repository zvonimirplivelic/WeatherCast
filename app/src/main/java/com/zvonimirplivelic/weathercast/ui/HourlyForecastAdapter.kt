package com.zvonimirplivelic.weathercast.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import com.zvonimirplivelic.weathercast.R
import com.zvonimirplivelic.weathercast.model.DetailedWeatherResponse
import timber.log.Timber
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.math.roundToInt

class HourlyForecastAdapter :
    RecyclerView.Adapter<HourlyForecastAdapter.HourlyForecastViewHolder>() {

    inner class HourlyForecastViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    private val differCallback =
        object : DiffUtil.ItemCallback<DetailedWeatherResponse.HourlyWeather>() {
            override fun areItemsTheSame(
                oldItem: DetailedWeatherResponse.HourlyWeather,
                newItem: DetailedWeatherResponse.HourlyWeather
            ): Boolean {
                return oldItem.dt == newItem.dt
            }

            override fun areContentsTheSame(
                oldItem: DetailedWeatherResponse.HourlyWeather,
                newItem: DetailedWeatherResponse.HourlyWeather
            ): Boolean {
                return oldItem == newItem
            }
        }

    val differ = AsyncListDiffer(this, differCallback)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): HourlyForecastViewHolder {
        return HourlyForecastViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.hourly_forecast_list_item,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(
        holder: HourlyForecastViewHolder,
        position: Int
    ) {
        val hourlyWeather = differ.currentList[position]
        holder.itemView.apply {
            val tvTime: TextView = findViewById(R.id.tv_hourly_forecast_time)
            val ivWeatherImage: ImageView = findViewById(R.id.iv_hourly_temperature_image)
            val tvTemperature: TextView = findViewById(R.id.tv_hourly_temperature)
            val weatherImageURLString =
                "https://openweathermap.org/img/wn/${hourlyWeather.weather[0].icon}@2x.png"

            Timber.d("Icon: ${hourlyWeather.weather[0].icon}")
            Timber.d("String: $weatherImageURLString")
            tvTime.text = convertTime(hourlyWeather.dt)
            tvTemperature.text = convertTemperature(hourlyWeather.temp)
            Picasso.get().load(weatherImageURLString).into(ivWeatherImage)
        }
    }

    override fun getItemCount() = 24

    private fun convertTime(time: Int, format: String = "HH:mm"): String? {
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