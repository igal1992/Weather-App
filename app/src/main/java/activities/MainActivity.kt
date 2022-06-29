package activities

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.ContentValues
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.BaseColumns
import android.util.DisplayMetrics
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IAxisValueFormatter
import com.google.android.material.slider.Slider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import helpers.MyDBHelper
import xd.activities.R
import xd.activities.databinding.ActivityMainBinding
import java.time.LocalDateTime

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var database = FirebaseFirestore.getInstance()
    private lateinit var  firebaseAuth: FirebaseAuth



    @SuppressLint("NewApi")
    override fun onCreate(savedInstanceState: Bundle?) {
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
                this.saveData(uid, creationTime,dialogBinding, myDialog)
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
        myDialog.setContentView(dialogBinding)
        myDialog.setCancelable(true)
        myDialog?.show()
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
        val humidProgress = dialogBinding.findViewById<TextView>(R.id.humidityViewProgress)
        val sliderTemp = dialogBinding.findViewById<Slider>(R.id.sliderTemp)
        val tempProgress = dialogBinding.findViewById<TextView>(R.id.temperatureViewProgress)

        sliderHumid.addOnChangeListener{slider,value,fromUser ->
            humidProgress.text = "$value" + "h"
        }
        sliderTemp.addOnChangeListener{slider,value,fromUser ->
            tempProgress.text = "$value" + "c"
        }
    }
    fun saveData(uid:String,creationTime:String,dialogBinding:View,myDialog: Dialog){
        //insert the point into the local machine via sqlite
        val temperature = dialogBinding.findViewById<Slider>(R.id.sliderTemp).value.toString()
        val humidity = dialogBinding.findViewById<Slider>(R.id.sliderHumid).value.toString()
        var helper = MyDBHelper(applicationContext)
        var db = helper.readableDatabase
        var cv = ContentValues()
        cv.put("creationTime",creationTime)
        cv.put("UID",uid)
        cv.put("temperature",temperature)
        cv.put("humidity",humidity)
        db.insert("POINTS",null, cv)

        //insert the point into the firebase db
        val point: MutableMap<String,Any> = hashMapOf()
        point["creationTime"] = creationTime
        point["UID"] = uid
        point["temperature"] = temperature
        point["humidity"] = humidity
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
    }

    private fun setUpCHartLayout(uid:String){
        var barList1 = mutableListOf<BarEntry>()
        var barList2 = mutableListOf<BarEntry>()
        val labels = mutableListOf<String>()
        var helper = MyDBHelper(applicationContext)
        var db = helper.readableDatabase
        val selectQuery = "SELECT * FROM POINTS WHERE UID = ?"
        var rs = db.rawQuery( selectQuery, arrayOf(uid))
        var x = 0f
        with(rs) {
            while (moveToNext()) {
                labels.add(
                        rs.getString(1).slice(8..9)
                            +"/"+
                        rs.getString(1).slice(5..6)
                            +"/"+
                        rs.getString(1).slice(2..3)
                            +" T"+
                        rs.getString(1).slice(11..15))
                val temperature = rs.getString(2).toString()
                val humidity = rs.getString(3).toString()
                barList1.add(BarEntry(x, temperature.toFloat()))
                barList2.add(BarEntry(x, humidity.toFloat()))
                x = x + 1f
            }
        }
        rs.close()
        val xAxis = binding.chartLayout.xAxis
        var layout = binding.chartLayout
        layout.legend.textSize = 15f
        layout.axisLeft.xOffset = 15f
        layout.axisRight.xOffset = 15f
        layout.description.isEnabled = false
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 1f
        xAxis.setDrawGridLines(false)
        xAxis.valueFormatter = IAxisValueFormatter { value, axis -> labels[value.toInt()] }
        xAxis.textSize = 12f
        val barDataSet1 = BarDataSet(barList1,"Temperature")
        barDataSet1.color = Color.CYAN
        barDataSet1.valueTextSize = 10f
        val barDataSet2 = BarDataSet(barList2,"Humidity")
        barDataSet2.color = Color.RED
        barDataSet2.valueTextSize = 10f
        val barData = BarData(barDataSet1,barDataSet2)
        layout.data = barData
        layout.setVisibleXRangeMaximum(3f)
        barData.barWidth = 0.5f
    }
}
