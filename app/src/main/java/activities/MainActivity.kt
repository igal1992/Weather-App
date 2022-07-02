package activities

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.ContentValues
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.BaseColumns
import android.text.Editable
import android.text.TextWatcher
import android.util.DisplayMetrics
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.text.set
import androidx.core.widget.addTextChangedListener
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IAxisValueFormatter
import com.google.android.material.slider.Slider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import helpers.MyDBHelper
import org.json.JSONArray
import org.json.JSONObject
import xd.activities.R
import xd.activities.databinding.ActivityMainBinding
import java.lang.Exception
import java.math.RoundingMode
import java.net.URL
import java.text.DecimalFormat
import java.time.LocalDateTime

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var database = FirebaseFirestore.getInstance()
    private lateinit var  firebaseAuth: FirebaseAuth

    @SuppressLint("NewApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        //basic settings of the class
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        //get an instance from the db
        firebaseAuth = FirebaseAuth.getInstance()
        setUpCHartLayout(firebaseAuth.currentUser?.uid.toString())




        //handle logout
        binding.logoutButton.setOnClickListener {logout()}

        //handle the add new data section
        binding.addNewDataButton.setOnClickListener {
            val dialogBinding = layoutInflater.inflate(R.layout.input_file, null)
            val myDialog = Dialog(this)
            val cancelBtn = dialogBinding.findViewById<TextView>(R.id.cancelButton)
            val saveBtn = dialogBinding.findViewById<Button>(R.id.saveButton)
            val uid = firebaseAuth.currentUser?.uid.toString()
            val creationTime = LocalDateTime.now().toString()

            /*------------------------------------------------------------------------------------*/
            //set the input file settings after launch
            /*------------------------------------------------------------------------------------*/

            this.viewSettings(myDialog,dialogBinding)

            /*------------------------------------------------------------------------------------*/
            //set the sliders listeners
            /*------------------------------------------------------------------------------------*/

            this.sliderHandler(dialogBinding)

            /*------------------------------------------------------------------------------------*/
            //set the save and cancel listeners
            /*------------------------------------------------------------------------------------*/

            cancelBtn.setOnClickListener {
                myDialog.dismiss()
            }

            saveBtn.setOnClickListener {
                this.saveData(uid, creationTime,dialogBinding, myDialog,false)
            }

            /*------------------------------------------------------------------------------------*/

        }

        //handle the add new data section
        binding.addApiButton.setOnClickListener {
            val dialogBinding = layoutInflater.inflate(R.layout.weather_api_input_file, null)
            val myDialog = Dialog(this)
            val cancelBtn = dialogBinding.findViewById<TextView>(R.id.cancelButtonApi)
            val saveBtn = dialogBinding.findViewById<Button>(R.id.saveButtonApi)
            val uid = firebaseAuth.currentUser?.uid.toString()
            val creationTime = LocalDateTime.now().toString()

            /*------------------------------------------------------------------------------------*/
            //set the input file settings after launch
            /*------------------------------------------------------------------------------------*/

            this.viewSettings(myDialog,dialogBinding)


            /*------------------------------------------------------------------------------------*/
            //set the save and cancel listeners
            /*------------------------------------------------------------------------------------*/

            cancelBtn.setOnClickListener {
                myDialog.dismiss()
            }

            saveBtn.setOnClickListener {
                this.saveData(uid, creationTime,dialogBinding, myDialog,true)
            }

            /*------------------------------------------------------------------------------------*/

        }
    }
    fun logout(){
        firebaseAuth.signOut()
        val intent = Intent(this, Login::class.java)
        startActivity(intent)
    }

    fun viewSettings(myDialog: Dialog,dialogBinding: View){
        //input layout launch
        myDialog.setContentView(dialogBinding)
        myDialog.setCancelable(true)
        myDialog?.show()
        //layout settings
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val displayWidth = displayMetrics.widthPixels
        val layoutParams = WindowManager.LayoutParams()
        layoutParams.copyFrom(myDialog?.window?.attributes)
        val dialogWindowWidth = (displayWidth * 0.9f).toInt()
        layoutParams.width = dialogWindowWidth
        myDialog?.window?.attributes = layoutParams
    }
    fun sliderHandler(dialogBinding: View){
        val sliderHumid = dialogBinding.findViewById<Slider>(R.id.sliderHumid)
        val humidProgress = dialogBinding.findViewById<EditText>(R.id.humidityViewProgress)
        val sliderTemp = dialogBinding.findViewById<Slider>(R.id.sliderTemp)
        val tempProgress = dialogBinding.findViewById<EditText>(R.id.temperatureViewProgress)

        //create a listener for humidity slider for when a user stops touching the slider
        sliderHumid.addOnSliderTouchListener(object : Slider.OnSliderTouchListener{
            override fun onStartTrackingTouch(slider: Slider) {
            }

            override fun onStopTrackingTouch(slider: Slider) {
                val value = slider.value
                humidProgress.setText("$value" + "h")
            }

        })

        //create a listener when a user inputs a value into the edit text field of humidity
        humidProgress.addTextChangedListener {
            val value = humidProgress.text.toString()
            if(value.toFloatOrNull() != null && value.toFloat() >=0 && value.toFloat() <= 100){
                sliderHumid.value = value.toFloat()
            }
        }

        //create a listener for temperature slider for when a user stops touching the slider
        sliderTemp.addOnSliderTouchListener(object : Slider.OnSliderTouchListener{
            override fun onStartTrackingTouch(slider: Slider) {
            }

            override fun onStopTrackingTouch(slider: Slider) {
                val value = slider.value
                tempProgress.setText("$value" + "h")
            }

        })

        //create a listener when a user inputs a value into the edit text field of temperature
        tempProgress.addTextChangedListener {
            val value = tempProgress.text.toString()
            if(value.toFloatOrNull() != null && value.toFloat() >=-10 && value.toFloat() <= 50){
                sliderTemp.value = value.toFloat()
            }
        }
    }
    fun saveData(uid:String,creationTime:String,dialogBinding:View,myDialog: Dialog,isApi: Boolean){
        //insert the point into the local machine via sqlite
        var cv = ContentValues()
        val point: MutableMap<String,Any> = hashMapOf()
        if(!isApi){
            val temperature = dialogBinding.findViewById<Slider>(R.id.sliderTemp).value.toString()
            val humidity = dialogBinding.findViewById<Slider>(R.id.sliderHumid).value.toString()
            cv.put("temperature",temperature)
            cv.put("humidity",humidity)
            point["temperature"] = temperature
            point["humidity"] = humidity
            var helper = MyDBHelper(applicationContext)
            var db = helper.readableDatabase
            cv.put("creationTime",creationTime)
            cv.put("UID",uid)
            db.insert("POINTS",null, cv)
            //insert the point into the firebase db
            point["creationTime"] = creationTime
            point["UID"] = uid

            database.collection("Points")
                .add(point)
                .addOnSuccessListener {
                    Toast.makeText(this,"Successfully Saved",Toast.LENGTH_SHORT).show()
                }.addOnFailureListener {
                    Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show()
                }
            myDialog.dismiss()
            finish();
            startActivity(getIntent());
        }else{
            this.getApiWeatherOpen(myDialog,dialogBinding,creationTime,uid)
        }
    }



    private fun getApiWeatherOpen(myDialog: Dialog,dialogBinding: View,creationTime: String,uid: String){
        var cv = ContentValues()
        val point: MutableMap<String,Any> = hashMapOf()
        val cityField = dialogBinding.findViewById<EditText>(R.id.cityField).text.toString()
        val stateCode = 376
        val key = "48339ce7035a2d5c45177ee7cf076e5a"
        try{
            val queue = Volley.newRequestQueue(this)
            //works only for israel cause of state code
            val url = "https://api.openweathermap.org/geo/1.0/direct?q=${cityField},${stateCode}&appid=${key}"
            val stringRequest = StringRequest(
                Request.Method.GET, url,
                Response.Listener<String> { response ->
                    if(response.equals("[]")){//if did not find the city
                        Toast.makeText(this,"Could Not Find The City",Toast.LENGTH_SHORT).show()
                    }else{
                     //all values setup
                    val jsonCoords = JSONArray(response)
                    val mainCoords = jsonCoords.getJSONObject(0)
                    val df = DecimalFormat("#.##")
                    df.roundingMode = RoundingMode.DOWN
                    val lat = df.format(mainCoords.getString("lat").toFloat())
                    val lon = df.format(mainCoords.getString("lon").toFloat())
                    val urlNext = "https://api.openweathermap.org/data/2.5/weather?lat=${lat}&lon=${lon}&appid=${key}"
                    val stringRequestTwo = StringRequest(
                        Request.Method.GET, urlNext,
                        Response.Listener<String> { response ->
                            //all values set up
                            val jsonWeatherApi = JSONObject(response)
                            val mainWeatherApi = jsonWeatherApi.getJSONObject("main")
                            val temperature = df.format(mainWeatherApi.getString("temp_min").toFloat() - 273.15).toString()
                            val humidity = df.format(mainWeatherApi.getString("humidity").toFloat())
                            cv.put("temperature",temperature)
                            cv.put("humidity",humidity)
                            point["temperature"] = temperature
                            point["humidity"] = humidity
                            var helper = MyDBHelper(applicationContext)
                            var db = helper.readableDatabase
                            cv.put("creationTime",creationTime)
                            cv.put("UID",uid)
                            db.insert("POINTS",null, cv)
                            //insert the point into the firebase db
                            point["creationTime"] = creationTime
                            point["UID"] = uid
                            database.collection("Points")
                                    .add(point)
                                    .addOnSuccessListener {
                                        Toast.makeText(this,"Successfully Saved",Toast.LENGTH_SHORT).show()
                                    }.addOnFailureListener {
                                        Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show()
                                    }
                            myDialog.dismiss()
                            finish();
                            startActivity(getIntent());
                        },
                        Response.ErrorListener {
                            Toast.makeText(this,"Could Not Find The City",Toast.LENGTH_SHORT).show()
                        }
                    )
                    queue.add(stringRequestTwo)
                    }
                },
                Response.ErrorListener {
                    Toast.makeText(this,"Could Not Find The City",Toast.LENGTH_SHORT).show()
                }
            )
            queue.add(stringRequest)
        }catch (NetworkOnMainThreadException: Exception){
            Toast.makeText(this,"Could Not Found the City",Toast.LENGTH_SHORT).show()
        }
    }



    private fun setUpCHartLayout(uid:String){
        //bar lists datasets x y1 y2
        val barList1 = mutableListOf<BarEntry>()
        val barList2 = mutableListOf<BarEntry>()
        val labels = mutableListOf<String>()

        //data base helper class definition with query
        val helper = MyDBHelper(applicationContext)
        val db = helper.readableDatabase
        val selectQuery = "SELECT * FROM POINTS WHERE UID = ?"
        val rs = db.rawQuery( selectQuery, arrayOf(uid))

        //layout settings definitions
        val xAxis = binding.chartLayout.xAxis
        val layout = binding.chartLayout
        val barDataSet1 : BarDataSet
        val barDataSet2 : BarDataSet
        val barData : BarData
        val grup_space = 1.6f
        val bar_space = 0f

        // temp x value for the bar data sets
        var x = 0f

        //run on all the datasets from the sqlite db
        with(rs) {
            while (moveToNext()) {
                //create the time based of israeli timeline
                labels.add(
                    rs.getString(1).slice(8..9)
                            +"/"+
                            rs.getString(1).slice(5..6)
                            +"/"+
                            rs.getString(1).slice(2..3)
                            +" - "+
                        rs.getString(1).slice(11..15))

                //rest of data
                val temperature = rs.getString(2).toString()
                val humidity = rs.getString(3).toString()

                //add the data to the bar lists accordingly
                barList1.add(BarEntry(x, temperature.toFloat()))
                barList2.add(BarEntry(x, humidity.toFloat()))
                x = x + 1f
            }
        }
        rs.close()
        //layout settings
        layout.legend.textSize = 15f
        layout.axisLeft.xOffset = 15f
        layout.axisRight.xOffset = 15f
        layout.description.isEnabled = false

        //x axis settings
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 3f
        xAxis.setDrawGridLines(false)
        xAxis.valueFormatter = IAxisValueFormatter { value, axis ->
            if(value.toInt() >=0 && value.toInt()/xAxis.granularity.toInt() <= labels.size-1){
                labels[value.toInt()/xAxis.granularity.toInt()]
            }else{
            ("").toString()
            }
        }
        xAxis.textSize = 12f
        xAxis.axisMinimum=-1.5f

        //bar data sets settings
        barDataSet1 = BarDataSet(barList1,"Temperature")
        barDataSet2 = BarDataSet(barList2,"Humidity")
        barDataSet2.color = Color.RED
        barDataSet2.valueTextSize = 14f
        barDataSet1.color = Color.CYAN
        barDataSet1.valueTextSize = 14f
        barData = BarData(barDataSet1,barDataSet2)
        barData.barWidth = 0.7f


        //show data and final setting
        layout.data = barData
        layout.isDragEnabled = true
        layout.setVisibleXRangeMaximum(3f)
        layout.groupBars(-1.5f,grup_space,bar_space)
        xAxis.axisMaximum = 0f+ layout.barData.getGroupWidth(grup_space,bar_space)* labels.size
        layout.invalidate()

    }
}
